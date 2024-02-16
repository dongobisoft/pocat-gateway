package io.pocat.gateway;

import io.pocat.gateway.route.DownStreamProcedure;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DownStreamProcedureRegistry {
    private static final DownStreamProcedureRegistry INSTANCE = new DownStreamProcedureRegistry();

    private final Map<String, DownStreamProcedure> procedures = new ConcurrentHashMap<>();

    private DownStreamProcedureRegistry() {

    }

    public static DownStreamProcedureRegistry getInstance() {
        return INSTANCE;
    }

    public DownStreamProcedure unregister(String txId) {
        return procedures.remove(txId);
    }

    public void register(String txId, DownStreamProcedure procedure) {
        procedures.put(txId, procedure);
    }
}
