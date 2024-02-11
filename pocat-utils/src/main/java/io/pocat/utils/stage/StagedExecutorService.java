package io.pocat.utils.stage;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

// todo Create StagedExecutor
public class StagedExecutorService extends AbstractExecutorService implements Stage {
    private static final int DEFAULT_THRESHOLD = 5;

    private final String name;
    private final ExecutorService mainPool;
    private final BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<>();

    private final int threshold;
    private final ClassLoader contextClassLoader;
    private final AtomicInteger workerNum = new AtomicInteger(0);

    private boolean isShutdown = false;

    public StagedExecutorService(String name, ExecutorService mainPool) {
        this(name, mainPool, DEFAULT_THRESHOLD);
    }

    public StagedExecutorService(String name, ExecutorService mainPool, int threshold) {
        this(name, mainPool, threshold, Thread.currentThread().getContextClassLoader());
    }

    public StagedExecutorService(String name, ExecutorService mainPool, int threshold, ClassLoader contextClassLoader) {
        this.name = name;
        this.mainPool = mainPool;
        this.threshold = threshold;
        this.contextClassLoader = contextClassLoader;
    }

    @Override
    public void shutdown() {
        throw new UnsupportedOperationException("Cannot shutdown stage.");
    }

    @Override
    public List<Runnable> shutdownNow() {
        throw new UnsupportedOperationException("Cannot shutdown stage.");
    }

    @Override
    public boolean isShutdown() {
        return mainPool.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return mainPool.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
       return mainPool.awaitTermination(timeout, unit);
    }

    @Override
    public void execute(Runnable command) {
        if(!isShutdown) {
            this.taskQueue.add(command);
            if (this.taskQueue.size() > this.threshold) {
                mainPool.execute(() -> {
                    int current = this.workerNum.getAndIncrement();

                    String oldName = Thread.currentThread().getName();
                    ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();

                    Thread.currentThread().setName(name + "-borrow-worker-" + current + "-" + oldName);
                    Thread.currentThread().setContextClassLoader(contextClassLoader);
                    while (!(isShutdown && taskQueue.isEmpty())) {
                        Runnable task = taskQueue.poll();
                        if (task == null) {
                            break;
                        }
                        task.run();
                    }

                    Thread.currentThread().setContextClassLoader(oldClassLoader);
                    Thread.currentThread().setName(oldName);

                    this.workerNum.decrementAndGet();
                });
            }
        }
    }

    @Override
    public void start() {
        mainPool.execute(() -> {
            int current = workerNum.getAndIncrement();
            String oldName = Thread.currentThread().getName();
            ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();

            Thread.currentThread().setName(name + "-main-worker-" + current);
            Thread.currentThread().setContextClassLoader(contextClassLoader);

            while(!(isShutdown && taskQueue.isEmpty())) {
                try {
                    Runnable task = taskQueue.take();
                    task.run();
                } catch (InterruptedException ignored) {
                    // do nothing
                }
            }

            Thread.currentThread().setContextClassLoader(oldClassLoader);
            Thread.currentThread().setName(oldName);
            workerNum.decrementAndGet();
        });
    }

    @Override
    public void stop() {
        isShutdown = true;
    }
}
