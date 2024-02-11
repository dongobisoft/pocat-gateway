package io.pocat.gateway.config;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.List;

public class LogHandlerType {
    @JacksonXmlProperty(localName = "type")
    private String type;
    @JacksonXmlElementWrapper(localName = "params")
    @JacksonXmlProperty(localName = "param")
    private List<NameValueType> params;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<NameValueType> getParams() {
        return params;
    }

    public void setParams(List<NameValueType> params) {
        this.params = params;
    }
}
