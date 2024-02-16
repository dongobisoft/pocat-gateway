package io.pocat.gateway;

import io.pocat.context.EnvContextManager;
import io.pocat.gateway.config.GatewayConfigType;
import io.pocat.gateway.context.GatewayContextProviderFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class GatewayLauncher {
    private Gateway gateway;

    public static void main(String[] args) throws Exception {
        if(args.length < 1) {
            throw new IllegalArgumentException("Not found gateway config file path.");
        }

        GatewayLauncher launcher = new GatewayLauncher();
        launcher.launch(args);
    }

    private void launch(String[] args) throws Exception {
        EnvContextManager.getInstance().setContextProviderFactory(new GatewayContextProviderFactory());
        GatewayConfigType gatewayConfig;
        try (FileInputStream fis = new FileInputStream(args[0])){
            gatewayConfig = GatewayConfigType.parse(fis);
        } catch (FileNotFoundException e) {
            throw new IOException("Gateway config file [" + args[0] + "] does not exist.");
        } catch (IOException e) {
            throw new IOException("Cannot read gateway config file [" + args[0] + "].", e);
        }

        this.gateway = new Gateway();

        this.gateway.init(gatewayConfig);
        this.gateway.start();

        Runtime.getRuntime().addShutdownHook(new Thread(this::destroy));

        this.gateway.join();
    }

    private void destroy() {
        gateway.stop();
    }
}
