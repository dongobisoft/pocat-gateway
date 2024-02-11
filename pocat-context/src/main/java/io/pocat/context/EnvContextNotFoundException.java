package io.pocat.context;

import java.io.IOException;

public class EnvContextNotFoundException extends IOException {
    public EnvContextNotFoundException(String message) {
        super(message);
    }

    public EnvContextNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
