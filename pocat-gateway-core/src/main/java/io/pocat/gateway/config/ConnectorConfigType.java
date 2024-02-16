package io.pocat.gateway.config;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import io.pocat.gateway.connector.ServerConnectorOptions;

public class ConnectorConfigType {
    private String name;
    private int port;
    private int acceptor = Runtime.getRuntime().availableProcessors();
    private int selector = Runtime.getRuntime().availableProcessors()*4;
    @JacksonXmlProperty(localName = "tls-config")
    private TLSConfigType tlsConfig;
    @JacksonXmlProperty(localName = "enable-websocket")
    private boolean enableWebSocket = false;
    private ServerConnectorOptions options = new ServerConnectorOptions();

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPort() {
        return this.port;
    }

    public void setPort(int port) {
        this.port = port;
    }


    public int getAcceptor() {
        return acceptor;
    }

    public void setAcceptor(int acceptor) {
        this.acceptor = acceptor;
    }

    public int getSelector() {
        return selector;
    }

    public void setSelector(int selector) {
        this.selector = selector;
    }

    public ServerConnectorOptions getOptions() {
        return this.options;
    }

    public void setOptions(ServerConnectorOptions options) {
        this.options = options;
    }

    public TLSConfigType getTlsConfig() {
        return this.tlsConfig;
    }

    public void setTlsConfig(TLSConfigType tlsConfig) {
        this.tlsConfig = tlsConfig;
    }

    public boolean isEnableWebsocket() {
        return this.enableWebSocket;
    }

    public void setEnableWebSocket(boolean enableWebSocket) {
        this.enableWebSocket = enableWebSocket;
    }
}
