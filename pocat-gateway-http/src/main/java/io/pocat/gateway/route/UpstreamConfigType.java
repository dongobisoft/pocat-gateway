package io.pocat.gateway.route;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import io.pocat.gateway.route.UpstreamConfig;

public class UpstreamConfigType implements UpstreamConfig {
    @JacksonXmlProperty(localName = "channel")
    private String requestChannel;
    @JacksonXmlProperty(localName = "topic")
    private String requestTopic;

    @Override
    public String getRequestChannel() {
        return requestChannel;
    }

    public void setRequestChannel(String requestChannel) {
        this.requestChannel = requestChannel;
    }

    @Override
    public String getRequestTopic() {
        return requestTopic;
    }

    public void setRequestTopic(String requestTopic) {
        this.requestTopic = requestTopic;
    }
}
