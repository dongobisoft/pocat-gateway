package io.pocat.gateway.route.http.config;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class ResourceRefType {
    @JacksonXmlProperty(localName = "ref-name")
    private String refName;
    @JacksonXmlProperty(localName = "resource-name")
    private String resourceName;

    public String getRefName() {
        return refName;
    }

    public void setRefName(String refName) {
        this.refName = refName;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }
}
