package io.pocat.gateway.config;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.List;

public class AccessLoggerType {
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "handler")
    private List<LogHandlerType> handlers;

    public List<LogHandlerType> getHandlers() {
        return handlers;
    }

    public void setHandlers(List<LogHandlerType> handlers) {
        this.handlers = handlers;
    }
}
