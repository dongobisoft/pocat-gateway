package io.pocat.gateway.connector;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.AttributeKey;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class ServerConnector {
    private static final int DEFAULT_ACCEPTOR_NUM = Runtime.getRuntime().availableProcessors();
    private static final int DEFAULT_SELECTOR_NUM = Runtime.getRuntime().availableProcessors() *4;

    private final String name;

    private int acceptorNum = DEFAULT_ACCEPTOR_NUM;
    private int selectorNum = DEFAULT_SELECTOR_NUM;

    private Server server;
    private int port = 0;
    private ServerConnectorOptions options = new ServerConnectorOptions();

    private ChannelFuture channelFuture;
    private final List<ConnectionHandler> connectionHandlers = new ArrayList<>();

    private boolean isRunning = false;

    public ServerConnector(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public int getPort() {
        return this.port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getAcceptorNum() {
        return this.acceptorNum;
    }

    public void setAcceptorNum(int acceptorNum) {
        this.acceptorNum = acceptorNum;
    }

    public int getSelectorNum() {
        return this.selectorNum;
    }

    public void setSelectorNum(int selectorNum) {
        this.selectorNum = selectorNum;
    }

    public ServerConnectorOptions getOptions() {
        return options;
    }

    public void setServerConnectorOptions(ServerConnectorOptions options) {
        this.options = options;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void addConnectionHandlers(List<ConnectionHandler> connectionHandlers) {
        this.connectionHandlers.addAll(connectionHandlers);
    }

    public void addConnectionHandler(ConnectionHandler connectionHandler) {
        this.connectionHandlers.add(connectionHandler);
    }

    void start() {
        if(this.server == null) {
            throw new IllegalStateException("No server selected.");
        }
        ServerBootstrap sb = new ServerBootstrap();

        EventLoopGroup acceptorGroup = createEventLoopGroup(acceptorNum, server.getExecutor());
        EventLoopGroup selectorGroup = createEventLoopGroup(selectorNum, server.getExecutor());

        sb.group(acceptorGroup, selectorGroup)
                .channel(getChannelType())
                .option(ChannelOption.SO_BACKLOG, options.getBackLog())
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, options.getConnectTimeOutMillis())
                .childOption(ChannelOption.SO_RCVBUF, options.getRcvBuf())
                .childOption(ChannelOption.SO_SNDBUF, options.getSndBuf())
                .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(options.getLowWaterMark(), options.getHighWaterMark()))
                .childOption(ChannelOption.TCP_NODELAY, options.isTcpNoDelay())
                .childOption(ChannelOption.SO_REUSEADDR, options.isReuseAddress())
                .childHandler(new ChannelInitializer<>() {
                    @Override
                    protected void initChannel(Channel channel) throws Exception {
                        for(ConnectionHandler handler: connectionHandlers) {
                            handler.handleConnection(channel);
                        }
                        channel.pipeline().addLast(new ChannelInboundHandlerAdapter(){
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                if(!(msg instanceof Exchange)) {
                                    throw new IllegalStateException("Invalid exchange type [" + msg.getClass() + "]");
                                }
                                Exchange exchange = (Exchange) msg;

                                Connection connection = new ConnectionImpl(ctx.channel());
                                exchange.setContext(new ExchangeContext() {
                                    @Override
                                    public Connection getConnection() {
                                        return connection;
                                    }

                                    @Override
                                    public void write(Exchange exchange) {
                                        ctx.writeAndFlush(exchange);
                                    }
                                });
                                server.getHandler().handle((Exchange) msg);
                            }

                            @Override
                            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                                cause.printStackTrace();
                                ctx.channel().close();
                            }
                        });
                    }
                });
            channelFuture = sb.bind(port).addListener((ChannelFutureListener) future -> {
                channelFuture.channel().closeFuture().addListener(channelFuture -> {
                    acceptorGroup.shutdownGracefully();
                    selectorGroup.shutdownGracefully();
                });
                isRunning = true;
            });

    }

    public void stop() {
        channelFuture.channel().close();
    }

    private EventLoopGroup createEventLoopGroup(int workerNum, ExecutorService executor) {
        return Epoll.isAvailable()?new EpollEventLoopGroup(workerNum, executor):new NioEventLoopGroup(workerNum, executor);
    }

    private Class<? extends ServerChannel> getChannelType() {
        return Epoll.isAvailable()? EpollServerSocketChannel.class: NioServerSocketChannel.class;
    }


    private static class ConnectionImpl implements Connection {
        private final Channel channel;

        public ConnectionImpl(Channel channel) {
            this.channel = channel;
        }

        @Override
        public void setAttribute(String attrName, Object attrObj) {
            channel.attr(AttributeKey.newInstance(attrName)).set(attrObj);
        }

        @Override
        public Object getAttribute(String attrName) {
            return channel.attr(AttributeKey.newInstance(attrName)).get();
        }

        @Override
        public InetSocketAddress getLocalAddress() {
            return (InetSocketAddress) channel.localAddress();
        }

        @Override
        public InetSocketAddress getRemoteAddress() {
            return (InetSocketAddress) channel.remoteAddress();
        }

        @Override
        public void close() {
            channel.close();
        }
    }
}
