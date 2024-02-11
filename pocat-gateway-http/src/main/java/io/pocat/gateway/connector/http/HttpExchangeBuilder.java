package io.pocat.gateway.connector.http;

import io.pocat.gateway.connector.AbstractExchange;

import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.stream.Collectors;

class HttpExchangeBuilder {
    private static final String ALL_PATTERN_STR = "(.*)";
    private static final String ALL_PATTERN = "(.*)/(.*)";

    private String method;
    private final Map<String, String> headers = new HashMap<>();
    private Map<String, List<String>> queryParams;
    private String requestPath;
    private String queryString;
    private List<String> accepts;
    private final ByteArrayOutputStream baos = new ByteArrayOutputStream();

    public HttpExchange build() {
        this.accepts = buildAccepts(headers.get("Accept"));
        return new HttpExchangeImpl(this);
    }

    private List<String> buildAccepts(String acceptStr) {
        if(acceptStr == null) {
            return Collections.singletonList(ALL_PATTERN);
        }
        String[] accepts = acceptStr.split(",");
        List<HttpAccept> acceptHeaders = new ArrayList<>(accepts.length);
        for(String accept:accepts) {
            double weight = 1.0;
            accept = accept.trim();

            String[] mimeWeight = accept.split(";");
            String mime = mimeWeight[0];
            String mimePattern = mime.replace("*", ALL_PATTERN_STR);
            if(mimeWeight.length > 1) {
                for(int i = 1; i < mimeWeight.length; i++) {
                    if(mimeWeight[i].startsWith("q=")) {
                        weight = Double.parseDouble(mimeWeight[i].substring("q=".length()));
                    }
                }

            }
            acceptHeaders.add(new HttpAccept(mimePattern, weight, acceptHeaders.size()));
        }

        acceptHeaders.sort((o1, o2) -> {
            // sort by weight with descending order
            if(o1.weight > o2.weight) {
                return -1;
            } else if(o1.weight < o2.weight) {
                return 1;
            } else {
                // sort by index with ascending order
                return Integer.compare(o1.index, o2.index);
            }
        });
        return acceptHeaders.stream().map(httpAccept -> httpAccept.pattern).collect(Collectors.toList());
    }

    public String getMethod() {
        return method;
    }

    public void setRequestMethod(String method) {
        this.method = method;
    }

    public void setRequestHeader(String key, String value) {
        headers.put(key, value);
    }

    public void setQueryParams(Map<String, List<String>> queryParams) {
        this.queryParams = queryParams;
    }

    public String getRequestPath() {
        return requestPath;
    }

    public void setRequestPath(String requestPath) {
        this.requestPath = requestPath;
    }

    public void writeRequestContents(byte[] bytes) {
        if(bytes != null) {
            baos.writeBytes(bytes);
        }
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    private static class HttpExchangeImpl extends AbstractExchange implements HttpExchange {
        private final HttpExchangeBuilder builder;
        private final byte[] requestContents;

        private final Map<String, String> responseHeaders = new HashMap<>();
        private int responseStatus = 200;
        private byte[] responseContents = new byte[0];

        public HttpExchangeImpl(HttpExchangeBuilder builder) {
            this.builder = builder;
            this.requestContents = builder.baos.toByteArray();
            if(builder.headers.containsKey(CORRELATION_ID_HEADER_NAME)) {
                responseHeaders.put(CORRELATION_ID_HEADER_NAME, builder.headers.get(CORRELATION_ID_HEADER_NAME));
            }
        }

        @Override
        public String getCorrelationId() {
            return builder.headers.get(CORRELATION_ID_HEADER_NAME);
        }

        @Override
        public String getRequestPath() {
            return builder.getRequestPath();
        }

        @Override
        public String getRequestMethod() {
            return builder.getMethod();
        }

        @Override
        public String getRequestHeader(String headerName) {
            return builder.headers.get(headerName);
        }

        @Override
        public Set<String> getRequestHeaderNames() {
            return Collections.unmodifiableSet(builder.headers.keySet());
        }

        @Override
        public String getRequestParam(String paramName) {
            try {
                return builder.queryParams.containsKey(paramName) ? builder.queryParams.get(paramName).get(0) : null;
            } catch (IndexOutOfBoundsException e) {
                return null;
            }
        }

        @Override
        public Set<String> getRequestParamNames() {
            return Collections.unmodifiableSet(builder.queryParams.keySet());
        }

        @Override
        public List<String> getRequestParams(String paramName) {
            return builder.queryParams.containsKey(paramName)?Collections.unmodifiableList(builder.queryParams.get(paramName)):null;
        }

        @Override
        public String getQueryString() {
            return builder.queryString;
        }

        @Override
        public String getAcceptableMimeType(Set<String> types) {
            for(String pattern:builder.accepts) {
                for(String type:types) {
                    if(type.matches(pattern)) {
                        return type;
                    }
                }
            }
            return null;
        }

        @Override
        public byte[] getRequestContents() {
            return requestContents;
        }

        @Override
        public int getResponseStatus() {
            return responseStatus;
        }

        @Override
        public void setResponseStatus(int status) {
            this.responseStatus = status;
        }

        @Override
        public Set<String> getResponseHeaderNames() {
            return Collections.unmodifiableSet(responseHeaders.keySet());
        }

        @Override
        public String getResponseHeader(String headerName) {
            return responseHeaders.get(headerName);
        }

        @Override
        public void setResponseHeader(String headerName, String headerValue) {
            responseHeaders.put(headerName, headerValue);
        }

        @Override
        public byte[] getResponseContents() {
            return responseContents;
        }

        @Override
        public void setResponseContents(byte[] contents) {
            this.responseContents = contents;
        }
    }

    private static class HttpAccept {
        private final String pattern;
        private final double weight;
        private final int index;

        private HttpAccept(String pattern, double weight, int index) {
            this.pattern = pattern;
            this.weight = weight;
            this.index = index;
        }
    }
}
