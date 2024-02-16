package io.pocat.gateway;

import io.pocat.context.EnvContextProvider;
import io.pocat.context.InitialEnvContextProvider;
import io.pocat.gateway.config.GatewayConfigType;
import io.pocat.gateway.config.ServiceConfigType;
import io.pocat.message.channel.MessageChannel;
import io.pocat.utils.stage.Stage;
import io.pocat.utils.stage.StagedExecutorService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

public class Gateway {
    public static final String FILTER_EXECUTOR_NAME = "filter-stage";
    public static final String UPSTREAM_EXECUTOR_NAME = "upstream-stage";
    public static final String RESPONSE_EXECUTOR_NAME = "response-stage";
    public static final String DISPATCHER_EXECUTOR_NAME = "dispatcher-stage";

    private static final String[] STAGE_NAMES = new String[]{FILTER_EXECUTOR_NAME, UPSTREAM_EXECUTOR_NAME, RESPONSE_EXECUTOR_NAME, DISPATCHER_EXECUTOR_NAME};

    private final String gatewayId;
    private AccessLogger accessLogger;

    private final Object statusLock = new Object();
    private final Map<String, ServiceContainer> containers = new HashMap<>();

    private ExecutorService executor;
    private boolean isRunning = true;
    private StageManager stageManager;
    private ResourceManager resourceManager;

    private EnvContextProvider envProvider;
    private ResponseHandler responseHandler;

    public Gateway() {
        this.gatewayId = UUID.randomUUID().toString().replaceAll("-", "") + System.currentTimeMillis();
    }

    public void init(GatewayConfigType gatewayConfig) {
        this.accessLogger = AccessLogger.builder().build();
        this.executor = Executors.newFixedThreadPool(gatewayConfig.getWorkerPoolSize());
        this.envProvider = new InitialEnvContextProvider();
        Map<String, Stage> stages = new HashMap<>();
        for(String stageName:STAGE_NAMES) {
            stages.put(stageName, new StagedExecutorService(stageName, executor));
        }

        this.stageManager = new StageManager(stages);
        this.resourceManager = new ResourceManager(this.envProvider);
        this.responseHandler = new ResponseHandler(this.gatewayId, this.envProvider);
        this.responseHandler.init(gatewayConfig.getResponseChannelName(), this.stageManager.getExecutor(RESPONSE_EXECUTOR_NAME));

        for(ServiceConfigType serviceConfig: gatewayConfig.getServices()) {
            ServiceContainer container = new ServiceContainer(this);
            container.init(serviceConfig);
            this.containers.put(serviceConfig.getName(), container);
        }
    }

    public void start() {
        synchronized (this.statusLock) {
            this.isRunning = true;
            this.stageManager.start();
            try {
                responseHandler.start();
            } catch (IOException e) {
                throw new IllegalStateException("Cannot start response handler", e);
            }
            for (ServiceContainer container : this.containers.values()) {
                container.start();
            }
        }
    }

    public void join() {
        while(!Thread.interrupted()) {
            try {
                if(this.executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS)) {
                    break;
                }
            } catch (InterruptedException ignored) {
                //ignored
            }
        }
    }

    public void stop() {
        synchronized (this.statusLock) {
            if (!this.isRunning) {
                return;
            }
            for (ServiceContainer container : this.containers.values()) {
                container.stop();
            }

            this.stageManager.stop();
            this.resourceManager.close();

            ChannelAdaptorRegistry.getInstance().clear();

            this.executor.shutdown();
            this.envProvider.close();
            this.isRunning = false;
        }
    }

    public StageManager getExecutorManager() {
        return stageManager;
    }

    public ResourceManager getResourceManager() {
        return resourceManager;
    }

    public AccessLogger getAccessLogger() {
        return accessLogger;
    }

    public EnvContextProvider getEnvProvider() {
        return envProvider;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public MessageChannel getResponseChannel() {
        return responseHandler.getChannel();
    }

    public ExecutorService getExecutor() {
        return executor;
    }
}
