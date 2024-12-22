package pres.raefing.ntp.netty.sp;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import pres.raefing.ntp.netty.sp.api.NtMessageHandler;
import pres.raefing.ntp.netty.base.NtMessageCodec;
import pres.raefing.ntp.netty.sp.handler.NtMessageHandlerChain;

import java.util.List;

@Slf4j
public class NtServer {

    private int bindPost;
    private EventLoopGroup boos;
    private EventLoopGroup workers;
    private ServerBootstrap bootstrap;

    public NtServer(int port, List<NtMessageHandler> messageHandlers) {
        this.bindPost = port;
        boos = new NioEventLoopGroup();
        workers = new NioEventLoopGroup();
        bootstrap = new ServerBootstrap();
        bootstrap.group(boos, workers)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 10 * 1024)
                .option(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) {
                        socketChannel.pipeline()
                                .addLast(new LoggingHandler(LogLevel.DEBUG))
                                .addLast(new IdleStateHandler(0, 0, 30))
                                .addLast(new NtMessageCodec())
                                .addLast(new NtMessageHandlerChain(messageHandlers));
                    }
                });
    }

    public void start() {
        try {
            ChannelFuture future = bootstrap.bind(bindPost).sync();
            if (future.isSuccess()) {
                log.info("Server started on port {}", bindPost);
            } else {
                log.error("Server failed to start on port {}", bindPost);
            }
        } catch (InterruptedException e) {
            log.error("Server failed to start on port {}", bindPost, e);
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        boos.shutdownGracefully();
        workers.shutdownGracefully();
    }

}
