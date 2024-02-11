package io.pocat.gateway.route.http.config;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.List;

public class RouteFilterType {
    private String clazz;
    @JacksonXmlElementWrapper(localName = "init-params")
    @JacksonXmlProperty(localName = "init-param")
    private List<FilterParam> filterParams;
    @JacksonXmlElementWrapper(localName = "resource-refs")
    @JacksonXmlProperty(localName = "resource-ref")
    private List<ResourceRefType> resourceRefs;

    public List<FilterParam> getFilterParams() {
        return filterParams;
    }

    public void setFilterParams(List<FilterParam> filterParams) {
        this.filterParams = filterParams;
    }

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    public List<ResourceRefType> getResourceRefs() {
        return resourceRefs;
    }

    public void setResourceRefs(List<ResourceRefType> resourceRefs) {
        this.resourceRefs = resourceRefs;
    }

    public static class FilterParam {
        private String name;
        private String value;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
