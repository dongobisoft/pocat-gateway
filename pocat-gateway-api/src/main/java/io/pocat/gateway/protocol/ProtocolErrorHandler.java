package io.pocat.gateway.protocol;

import io.pocat.gateway.connector.Exchange;
import io.pocat.gateway.route.ErrorTemplate;
import io.pocat.gateway.route.RouteProcessException;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class ProtocolErrorHandler {
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{([^}]+)}");

    private static final String ERROR_CODE_NAME = "error.code";
    private static final String ERROR_MESSAGE_NAME = "error.message";
    private static final String HEADER_PREFIX = "header:";

    private final Map<Integer, ErrorTemplate> errorTemplates = new HashMap<>();
    private ProtocolErrorHandler parent;

    protected ProtocolErrorHandler() {
        this(null);
    }

    protected ProtocolErrorHandler(ProtocolErrorHandler parent) {
        this.parent = parent;
    }

    public void addErrorTemplate(ErrorTemplate errorTemplate) {
        if(!validateTemplate(errorTemplate)) {
            throw new IllegalArgumentException("Invalid error template for error code [" + errorTemplate.getErrorCode() + "].");
        }
        errorTemplates.put(errorTemplate.getErrorCode(), errorTemplate);
    }

    public void setParentHandler(ProtocolErrorHandler parentErrorHandler) {
        this.parent = parentErrorHandler;
    }

    public abstract void handleError(Exchange exchange, RouteProcessException e);

    protected final ErrorTemplate findErrorTemplate(int errorCode) {
        if(errorTemplates.containsKey(errorCode)) {
            return errorTemplates.get(errorCode);
        } else {
            if(parent != null) {
                return parent.findErrorTemplate(errorCode);
            }
            return null;
        }
    }

    protected String replaceVariables(String template, Exchange exchange, RouteProcessException e) {
        String result = template;
        Matcher matcher = VARIABLE_PATTERN.matcher(template);
        while(matcher.find()) {
            if(matcher.group(1).equals(ERROR_CODE_NAME)) {
                result = result.replace(matcher.group(), String.valueOf(e.getErrorCode()));
            } else if (matcher.group(1).equals(ERROR_MESSAGE_NAME)) {
                result = result.replace(matcher.group(), String.valueOf(e.getMessage()));
            } else if(matcher.group(1).startsWith(HEADER_PREFIX)) {
                String value = exchange.getRequestHeader(matcher.group(1).substring(HEADER_PREFIX.length()));
                if(value != null) {
                    result = result.replace(matcher.group(), value);
                }
            } else {
                if(exchange.getAttribute(matcher.group(1)) != null) {
                    String value = (String) exchange.getAttribute(matcher.group(1));
                    result = result.replace(matcher.group(), value);
                }
            }
        }
        return result;
    }

    protected abstract boolean validateTemplate(ErrorTemplate errorTemplate);
    protected abstract void writeDefaultErrorResponse(Exchange exchange, RouteProcessException e);
}
