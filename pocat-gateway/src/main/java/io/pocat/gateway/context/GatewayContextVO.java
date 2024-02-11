package io.pocat.gateway.context;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "resource-context")
class GatewayContextVO {
    @JacksonXmlElementWrapper(localName = "endpoints")
    @JacksonXmlProperty(localName = "endpoint")
    private List<Endpoint> endpoints;
    @JacksonXmlElementWrapper(localName = "channels")
    @JacksonXmlProperty(localName = "channel")
    private List<Channel> channels;
    @JacksonXmlElementWrapper(localName = "resources")
    @JacksonXmlProperty(localName = "resource")
    private List<Resource> resources;

    public List<Endpoint> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(List<Endpoint> endpoints) {
        this.endpoints = endpoints;
    }

    public List<Channel> getChannels() {
        return channels;
    }

    public void setChannels(List<Channel> channels) {
        this.channels = channels;
    }

    public List<Resource> getResources() {
        return resources;
    }

    public void setResources(List<Resource> resources) {
        this.resources = resources;
    }

    public static class Resource {
        private String name;
        private String type;
        private List<ContextArgument> args;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public List<ContextArgument> getArgs() {
            return args;
        }

        public void setArgs(List<ContextArgument> args) {
            this.args = args;
        }
    }

    public static class Endpoint {
        private String name;
        private String type;
        private List<ContextArgument> args;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public List<ContextArgument> getArgs() {
            return args;
        }

        public void setArgs(List<ContextArgument> args) {
            this.args = args;
        }
    }

    public static class Channel {
        private String name;
        @JacksonXmlProperty(localName = "endpoint-ref")
        private String endpointRef;
        private List<ContextArgument> args;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEndpointRef() {
            return endpointRef;
        }

        public void setEndpointRef(String endpointRef) {
            this.endpointRef = endpointRef;
        }

        public List<ContextArgument> getArgs() {
            return args;
        }

        public void setArgs(List<ContextArgument> args) {
            this.args = args;
        }
    }

    public static class ContextArgument {
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
