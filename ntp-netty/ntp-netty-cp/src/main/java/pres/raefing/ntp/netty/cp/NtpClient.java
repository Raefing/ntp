package pres.raefing.ntp.netty.cp;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import pres.raefing.ntp.commons.NtMinoCache;
import pres.raefing.ntp.commons.NtUIdMaker;
import pres.raefing.ntp.netty.base.NtMessage;
import pres.raefing.ntp.netty.base.NtMessageCodec;
import pres.raefing.ntp.netty.base.msg.NtIdleMessage;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
public class NtpClient {

    private final String host;
    private final int port;
    private final EventLoopGroup group;
    private final Bootstrap bootstrap;
    private Channel channel;
    @Setter
    private NtUIdMaker uIdMaker;
    private final NtMinoCache minoCache;

    public NtpClient(String host, int port, NtMinoCache minoCache) {
        this.host = host;
        this.port = port;
        this.minoCache = minoCache;
        group = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                                .addLast(new LoggingHandler(LogLevel.DEBUG))
                                .addLast(new IdleStateHandler(0, 0, 5))
                                .addLast(new NtMessageCodec())
                                .addLast(new NtpCpInboundHandler(minoCache));

                    }
                });
    }

    public void connect() {
        try {
            ChannelFuture future = bootstrap.connect(host, port).sync();
            if (future.isSuccess()) {
                channel = future.channel();
                log.info("Connected to {}:{}", host, port);
            } else {
                log.warn("Failed to connect to {}:{}", host, port);
            }
        } catch (InterruptedException e) {
            log.error("Failed to connect to {}:{}", host, port, e);
            throw new RuntimeException(e);
        }
    }

    public String send(Object obj) {
        String msgId = uIdMaker.formatedSnowflakeId(32);
        NtMessage message = NtMessage.v1(obj, msgId);
        channel.writeAndFlush(message);
        return msgId;
    }

    public Object receive(String id, int timeout) throws TimeoutException {
        return minoCache.get(id, timeout, TimeUnit.MILLISECONDS);
    }

    public Object send(Object obj, int timeout) throws TimeoutException {
        String msgId = uIdMaker.formatedSnowflakeId(32);
        NtMessage message = NtMessage.v1(obj, msgId);
        channel.writeAndFlush(message);
        System.err.println("send:" + msgId);
        return receive(msgId, timeout);
    }

    public void close() {
        channel.close();
        group.shutdownGracefully();
    }

    class NtpCpInboundHandler extends ChannelInboundHandlerAdapter {

        NtMinoCache minoCache;

        NtpCpInboundHandler(NtMinoCache ntMinoCache) {
            this.minoCache = ntMinoCache;
        }

        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (msg instanceof NtMessage) {
                NtMessage ntMessage = (NtMessage) msg;
                if (minoCache != null) {
                    System.err.println("receive:" + ntMessage.getMessageId());
                    minoCache.put(ntMessage.getMessageId(), ntMessage.getData());
                }
            }
        }

        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if (evt instanceof IdleStateEvent) {
                IdleStateEvent stateEvent = (IdleStateEvent) evt;
                if (stateEvent.state() == IdleState.ALL_IDLE) {
                    NtIdleMessage ret = (NtIdleMessage) send(new NtIdleMessage(), 5000);
                    log.debug("client heartbeat response:{}", ret);
                }
            }
        }
    }


}
