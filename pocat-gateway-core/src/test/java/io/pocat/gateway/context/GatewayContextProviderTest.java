package io.pocat.gateway.context;

import org.junit.Test;

import java.io.IOException;
import java.util.Set;

import static org.junit.Assert.assertNull;

public class GatewayContextProviderTest {
    @Test
    public void testProviderInit() throws IOException {
        GatewayContextProvider provider = new GatewayContextProvider(System.getProperties());
        assertNull(provider.lookup("/env/test"));
    }
}
