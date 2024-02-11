package io.pocat.gateway;

import io.pocat.context.EnvContextManager;
import io.pocat.gateway.config.GatewayConfigType;
import io.pocat.gateway.context.GatewayContextProviderFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class GatewayLauncher {
    private Gateway gateway;

    public static void main(String[] args) {
        if(args.length < 1) {
            throw new IllegalArgumentException("Not found gateway config file path.");
        }

        GatewayLauncher launcher = new GatewayLauncher();
        launcher.launch(args);
    }

    private void launch(String[] args) {
        EnvContextManager.getInstance().setContextProviderFactory(new GatewayContextProviderFactory());
        GatewayConfigType gatewayConfig;
        try (FileInputStream fis = new FileInputStream(args[0])){
            gatewayConfig = GatewayConfigType.parse(fis);
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("Gateway config file [" + args[0] + "] does not exist.");
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot read gateway config file [" + args[0] + "].", e);
        }

        this.gateway = new Gateway();

        try {
            this.gateway.init(gatewayConfig);
            this.gateway.start();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to start gateway", e);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(this::destroy));

        this.gateway.join();
    }

    private void destroy() {
        gateway.stop();
    }
}
