package io.pocat.gateway.route;

public interface UpstreamConfig {
    String getRequestChannel();
    String getRequestTopic();
}
