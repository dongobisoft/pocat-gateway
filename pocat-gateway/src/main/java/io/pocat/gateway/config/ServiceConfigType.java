package io.pocat.gateway.config;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.List;

public class ServiceConfigType {
    private String name;
    private String protocol;
    @JacksonXmlProperty(localName = "route-group")
    private String routeGroup;
    @JacksonXmlProperty(localName = "access-logger")
    private AccessLoggerType accessLogger;

    @JacksonXmlElementWrapper(localName = "service-params")
    @JacksonXmlProperty(localName = "service-param")
    private List<NameValueType> params;
    @JacksonXmlElementWrapper(localName = "connectors")
    @JacksonXmlProperty(localName = "connector")
    private List<ConnectorConfigType> connectors;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getRouteGroup() {
        return routeGroup;
    }

    public void setRouteGroup(String routeGroup) {
        this.routeGroup = routeGroup;
    }

    public AccessLoggerType getAccessLogger() {
        return accessLogger;
    }

    public void setAccessLogger(AccessLoggerType accessLogger) {
        this.accessLogger = accessLogger;
    }

    public List<NameValueType> getServiceParams() {
        return params;
    }

    public void setServiceParams(List<NameValueType> params) {
        this.params = params;
    }

    public List<ConnectorConfigType> getConnectors() {
        return connectors;
    }

    public void setConnectors(List<ConnectorConfigType> connectors) {
        this.connectors = connectors;
    }
}
