package io.pocat.gateway;

import io.pocat.context.EnvContextProvider;
import io.pocat.context.descriptor.Argument;
import io.pocat.context.descriptor.MessageChannelDescriptor;
import io.pocat.context.descriptor.MessageEndpointDescriptor;
import io.pocat.gateway.route.DownStreamProcedure;
import io.pocat.message.channel.ConsumableMessageChannel;
import io.pocat.message.channel.DirectMessageChannel;
import io.pocat.message.channel.MessageChannel;
import io.pocat.message.channel.adaptor.ChannelAdaptorFactoryProvider;
import io.pocat.message.channel.adaptor.ChannelConfigBuilder;
import io.pocat.message.channel.adaptor.InboundChannelAdaptor;
import io.pocat.message.endpoint.MessageEndpoint;
import io.pocat.message.endpoint.MessageEndpointConfigBuilder;
import io.pocat.message.endpoint.MessageEndpointProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class ResponseHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResponseHandler.class);
    private final String gatewayId;
    private final EnvContextProvider envProvider;
    private InboundChannelAdaptor responseChannelAdaptor;
    private ConsumableMessageChannel responseChannel;

    public ResponseHandler(String gatewayId, EnvContextProvider envProvider) {
        this.gatewayId = gatewayId;
        this.envProvider = envProvider;
    }

    public void init(String channelName, ExecutorService executor) {
        try {

            responseChannelAdaptor = createInboundChannelAdaptor(channelName, this.gatewayId);
            this.responseChannel = new DirectMessageChannel(channelName);
            responseChannelAdaptor.setChannel(responseChannel);

            responseChannelAdaptor.setChannel(responseChannelAdaptor.getChannel());
            responseChannelAdaptor.setExecutor(executor);
            responseChannel.consume(message -> {
                DownStreamProcedure procedure = DownStreamProcedureRegistry.getInstance().unregister(message.getTxId());
                if(procedure == null) {
                    LOGGER.warn("Response for txid [" + message.getTxId() + "] not exist. Already time-outed.");
                } else {
                    procedure.call(message);
                }
            });
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private InboundChannelAdaptor createInboundChannelAdaptor(String channelName, String consumerName) throws IOException {
        MessageChannelDescriptor channelDescriptor = (MessageChannelDescriptor) envProvider.lookup("/env/context/channels/" + channelName);
        if(channelDescriptor == null) {
            throw new IOException("Channel context [" + channelName + "] does not exist.");
        }
        String endpointName = channelDescriptor.getEndpointRef();
        MessageEndpointDescriptor endpointDescriptor = (MessageEndpointDescriptor) envProvider.lookup("/env/context/endpoints/" + endpointName);
        if(endpointDescriptor == null) {
            throw new IOException("Endpoint context [" + endpointName + "] does not exist.");
        }
        MessageEndpointConfigBuilder endpointConfigBuilder = new MessageEndpointConfigBuilder(endpointDescriptor.getName(), endpointDescriptor.getType());
        List<Argument> endpointArgs = endpointDescriptor.getArguments();
        for(Argument arg:endpointArgs) {
            endpointConfigBuilder.addArgument(arg.getName(), arg.getValue());
        }
        MessageEndpoint endpoint = MessageEndpointProvider.getInstance().getChannelEndpoint(endpointConfigBuilder.build());
        ChannelConfigBuilder channelConfigBuilder = new ChannelConfigBuilder(channelDescriptor.getChannelName());
        channelConfigBuilder.setEndpoint(endpoint);
        List<Argument> channelArgs = channelDescriptor.getArguments();
        for(Argument arg:channelArgs) {
            channelConfigBuilder.addArgument(arg.getName(), arg.getValue());
        }
        return ChannelAdaptorFactoryProvider.getInstance().getInboundChannelAdaptorFactory(channelConfigBuilder.build()).createInboundChannelAdaptor(consumerName);
    }


    public void start() throws IOException {
        responseChannelAdaptor.start();
    }

    public void stop() throws IOException {
        responseChannelAdaptor.stop();
    }

    public MessageChannel getChannel() {
        return responseChannel;
    }
}
