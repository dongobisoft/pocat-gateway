package io.pocat.message.channel.endpoint.http;

import io.pocat.message.endpoint.MessageEndpointConfig;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class HttpConnector {
    private final HttpClient httpClient;
    private MessageEndpointConfig config;
    private String baseUrl;

    public HttpConnector(MessageEndpointConfig config) {
        this.config = config;
        this.baseUrl = config.getArgument("base-url");
        httpClient = HttpClient.newBuilder().build();
    }


    public CompletableFuture<HttpResponse<byte[]>> send(String uri, String method, Map<String, Object> headers, byte[] payload) {
        HttpRequest request;
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(new URI(baseUrl + uri))
                    .method(method, HttpRequest.BodyPublishers.ofByteArray(payload));
            for(String key:headers.keySet()) {
                builder.header(key, headers.get(key).toString());
            }
            request = builder.build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Invalid url");
        }
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray());
    }
}
