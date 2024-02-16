package io.pocat.gateway.connector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private final List<ServerConnector> connectors = new ArrayList<>();

    private ServerHandler handler;
    private ExecutorService executor;
    private boolean useInsideExecutor = false;

    public void addConnector(ServerConnector serverConnector) {
        serverConnector.setServer(this);
        connectors.add(serverConnector);
    }

    public void setHandler(ServerHandler serverHandler) {
        this.handler = serverHandler;
    }

    public void start() {
        if(executor == null) {
            executor = Executors.newWorkStealingPool();
            useInsideExecutor = true;
        }
        for(ServerConnector connector:this.connectors) {
            connector.start();
        }
    }

    public void stop() {
        for(ServerConnector connector:this.connectors) {
            connector.stop();
        }
        if(useInsideExecutor) {
            executor.shutdown();
        }
    }

    public ExecutorService getExecutor() {
        return this.executor;
    }

    public void setExecutor(ExecutorService executor) {
        this.executor = executor;
    }

    public ServerHandler getHandler() {
        return this.handler;
    }
}
