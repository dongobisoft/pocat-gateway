package io.pocat.gateway;

import io.pocat.gateway.config.AccessLoggerType;
import io.pocat.gateway.config.LogHandlerType;

public class AccessLogger {
    private static final Builder builder = new Builder();

    public static Builder builder() {
        return builder;
    }

    private AccessLogger() {

    }

    public static AccessLogger createAccessLogger(AccessLoggerType accessLoggerConfig) {
        AccessLogger logger = new AccessLogger();
        for(LogHandlerType handlerCfg:accessLoggerConfig.getHandlers()) {
            // todo setup log handler
        }
        return logger;
    }

    public void log(AccessLogRecord accessLogRecord) {

    }

    public static class Builder {
        public AccessLogger build() {
            return new AccessLogger();
        }
    }
}
