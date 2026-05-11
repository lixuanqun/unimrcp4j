package io.github.javamrcp.server.netty;

import io.github.javamrcp.server.MrcpServer;
import io.github.javamrcp.server.MrcpServerConfig;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Netty transport bootstrap for SIP signaling and MRCPv2 control channels.
 */
public final class NettyMrcpServer implements MrcpServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyMrcpServer.class);

    private final MrcpServerConfig config;

    private EventLoopGroup sipGroup;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel sipChannel;
    private Channel mrcpChannel;
    private boolean running;

    public NettyMrcpServer(MrcpServerConfig config) {
        this.config = Objects.requireNonNull(config, "config");
    }

    @Override
    public synchronized CompletionStage<Void> start() {
        if (running) {
            return CompletableFuture.completedFuture(null);
        }

        sipGroup = new NioEventLoopGroup(1);
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();

        try {
            sipChannel = bindSipChannel();
            mrcpChannel = bindMrcpChannel();
            running = true;
            LOGGER.info(
                    "Started Netty MRCP server: SIP UDP {}:{}, MRCP TCP {}:{}",
                    config.sipHost(),
                    localPort(sipChannel),
                    config.mrcpHost(),
                    localPort(mrcpChannel));
            return CompletableFuture.completedFuture(null);
        } catch (RuntimeException ex) {
            stop();
            return CompletableFuture.failedFuture(ex);
        }
    }

    @Override
    public synchronized CompletionStage<Void> stop() {
        if (!running && sipGroup == null && bossGroup == null && workerGroup == null) {
            return CompletableFuture.completedFuture(null);
        }

        closeChannel(sipChannel);
        closeChannel(mrcpChannel);
        sipChannel = null;
        mrcpChannel = null;

        shutdownGroup(sipGroup);
        shutdownGroup(workerGroup);
        shutdownGroup(bossGroup);
        sipGroup = null;
        workerGroup = null;
        bossGroup = null;
        running = false;
        LOGGER.info("Stopped Netty MRCP server");
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public synchronized boolean isRunning() {
        return running;
    }

    private Channel bindSipChannel() {
        Bootstrap bootstrap = new Bootstrap()
                .group(sipGroup)
                .channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_REUSEADDR, true)
                .handler(new SipDatagramHandler());

        ChannelFuture bindFuture = bootstrap.bind(new InetSocketAddress(config.sipHost(), config.sipPort()));
        return bindFuture.syncUninterruptibly().channel();
    }

    private Channel bindMrcpChannel() {
        ServerBootstrap bootstrap = new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel channel) {
                        channel.pipeline().addLast(new MrcpControlFrameHandler());
                    }
                });

        ChannelFuture bindFuture = bootstrap.bind(new InetSocketAddress(config.mrcpHost(), config.mrcpPort()));
        return bindFuture.syncUninterruptibly().channel();
    }

    private void closeChannel(Channel channel) {
        if (channel != null) {
            channel.close().syncUninterruptibly();
        }
    }

    private void shutdownGroup(EventLoopGroup group) {
        if (group != null) {
            group.shutdownGracefully(
                            config.shutdownQuietPeriod().toMillis(),
                            config.shutdownTimeout().toMillis(),
                            TimeUnit.MILLISECONDS)
                    .syncUninterruptibly();
        }
    }

    private int localPort(Channel channel) {
        if (channel.localAddress() instanceof InetSocketAddress address) {
            return address.getPort();
        }
        return -1;
    }
}
