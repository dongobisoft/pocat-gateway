package io.pocat.gateway.route.http;

import io.pocat.gateway.protocol.ProtocolErrorHandler;
import io.pocat.gateway.route.RouteProcessException;
import io.pocat.gateway.connector.Exchange;
import io.pocat.gateway.connector.http.HttpExchange;
import io.pocat.gateway.route.ErrorTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HttpProtocolErrorHandler extends ProtocolErrorHandler {
    private static final int NOT_ACCEPTABLE = 406;

    public HttpProtocolErrorHandler() {

    }

    public HttpProtocolErrorHandler(ProtocolErrorHandler parent) {
        super(parent);
    }

    @Override
    public void handleError(Exchange exchange, RouteProcessException e) {
        HttpExchange httpExchange = (HttpExchange) exchange;
        HttpErrorTemplate errorTemplate = (HttpErrorTemplate) findErrorTemplate(e.getErrorCode());
        if(errorTemplate == null) {
            writeDefaultErrorResponse(httpExchange, e);
            return;
        }
        httpExchange.setResponseStatus(errorTemplate.getStatusCode());
        String mimeType = httpExchange.getAcceptableMimeType(errorTemplate.getMimeTypes());
        if(mimeType == null) {
            httpExchange.setResponseStatus(NOT_ACCEPTABLE);
            return;
        }
        HttpResponseFormat format = errorTemplate.getResponseFormat(mimeType);
        if(format == null) {
            httpExchange.setResponseStatus(NOT_ACCEPTABLE);
            return;
        }

        Map<String, String> headers = format.getResponseHeaders();
        for(String headerName:headers.keySet()) {
            String headerValue = headers.get(headerName);
            String result = replaceVariables(headerValue, httpExchange, e);
            httpExchange.setResponseHeader(headerName, result);
        }

        String result = replaceVariables(format.getResponseBody(), httpExchange, e);
        httpExchange.setResponseContents(result.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    protected boolean validateTemplate(ErrorTemplate errorTemplate) {
        return errorTemplate instanceof HttpErrorTemplate;
    }

    @Override
    protected void writeDefaultErrorResponse(Exchange exchange, RouteProcessException e) {
        ((HttpExchange)exchange).setResponseStatus(500);
    }
}
