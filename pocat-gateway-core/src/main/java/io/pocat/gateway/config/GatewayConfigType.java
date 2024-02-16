package io.pocat.gateway.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class GatewayConfigType {
    @JacksonXmlProperty(localName = "name")
    private String name;
    @JacksonXmlProperty(localName = "worker-pool-size")
    private int workerPoolSize;
    @JacksonXmlProperty(localName = "response-channel")
    private String responseChannelName;

    @JacksonXmlProperty(localName = "access-logger")
    private AccessLoggerType accessLogger;

    @JacksonXmlElementWrapper(localName = "services")
    @JacksonXmlProperty(localName = "service")
    private List<ServiceConfigType> services;


    public static GatewayConfigType parse(InputStream is) {
        if(is == null) {
            throw new IllegalStateException("Config file does not exist.");
        }
        ObjectMapper mapper = new XmlMapper();
        try {
            return mapper.readValue(is, GatewayConfigType.class);
        } catch (IOException e) {
            throw new IllegalStateException("Invalid config file.", e);
        }
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getWorkerPoolSize() {
        return this.workerPoolSize;
    }

    public void setWorkerPoolSize(int workerPoolSize) {
        this.workerPoolSize = workerPoolSize;
    }

    public String getResponseChannelName() {
        return this.responseChannelName;
    }

    public void setResponseChannelName(String responseChannelName) {
        this.responseChannelName = responseChannelName;
    }

    public AccessLoggerType getAccessLogger() {
        return accessLogger;
    }

    public void setAccessLogger(AccessLoggerType accessLogger) {
        this.accessLogger = accessLogger;
    }

    public List<ServiceConfigType> getServices() {
        return this.services;
    }

    public void setServices(List<ServiceConfigType> services) {
        this.services = services;
    }

}
