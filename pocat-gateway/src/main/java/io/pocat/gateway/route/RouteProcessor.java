package io.pocat.gateway.route;

import io.pocat.gateway.connector.Exchange;
import io.pocat.utils.ExpireRegistry;

import java.util.Iterator;
import java.util.List;

public class RouteProcessor {
    private final Route route;
    private final List<RouteProcedure> procedures;
    private final ExpireRegistry<String, Exchange> exchangeRegistry;

    public RouteProcessor(Route route, List<RouteProcedure> procedures, ExpireRegistry<String, Exchange> exchangeRegistry) {
        this.route = route;
        this.procedures = procedures;
        this.exchangeRegistry = exchangeRegistry;
    }

    public void process(Exchange exchange) {
        exchangeRegistry.register(exchange.getTxId(), exchange, route.getExpireIn()-(System.currentTimeMillis() - exchange.getCreatedAt()));
        RouteProcedureChain chain = new RouteProcedureChainImpl(procedures.iterator());
        chain.doNext(exchange);
    }

    private static class RouteProcedureChainImpl implements RouteProcedureChain {
        private final Iterator<RouteProcedure> procItr;

        public RouteProcedureChainImpl(Iterator<RouteProcedure> procItr) {
            this.procItr = procItr;
        }

        public void doNext(Exchange exchange) {
            if(!exchange.isClosed()) {
                if (procItr.hasNext()) {
                    RouteProcedure procedure = procItr.next();
                    procedure.call(exchange, this);
                }
            }
        }
    }
}
