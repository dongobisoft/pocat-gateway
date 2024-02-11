package io.pocat.utils.clazz;

public class InstantiationFailedException extends Exception {
    public InstantiationFailedException(String msg, Throwable e) {
        super(msg, e);
    }
}
