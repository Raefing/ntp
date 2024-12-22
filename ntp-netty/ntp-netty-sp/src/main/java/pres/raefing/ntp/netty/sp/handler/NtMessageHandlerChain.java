package pres.raefing.ntp.netty.sp.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import pres.raefing.ntp.netty.base.NtMessage;
import pres.raefing.ntp.netty.base.msg.NtErrorMessage;
import pres.raefing.ntp.netty.exception.NtException;
import pres.raefing.ntp.netty.sp.api.HandleContext;
import pres.raefing.ntp.netty.sp.api.HandleState;
import pres.raefing.ntp.netty.sp.api.NtMessageHandler;

import java.util.Comparator;
import java.util.List;

@Slf4j
public class NtMessageHandlerChain extends ChannelInboundHandlerAdapter {

    private List<NtMessageHandler> handlers;

    public NtMessageHandlerChain(List<NtMessageHandler> handlers) {
        if (handlers != null) {
            this.handlers = handlers;
            this.handlers.sort(Comparator.comparingInt(NtMessageHandler::order));
        }
    }

    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (handlers != null) {
            if (msg instanceof NtMessage) {
                NtMessage ntMessage = (NtMessage) msg;
                HandleContext context = new HandleContext();
                context.setRequest(ntMessage.getData());
                context.setState(HandleState.NORMAL);
                try {
                    for (NtMessageHandler handler : handlers) {
                        handler.handleMessage(context);
                        if (context.interrupted()) {
                            break;
                        }
                    }
                    ntMessage.setData(context.getResponse());
                } catch (Exception e) {
                    NtErrorMessage errorMessage = new NtErrorMessage();
                    if (e instanceof NtException) {
                        NtException exception = (NtException) e;
                        errorMessage.setCode(exception.getCode());
                        errorMessage.setMessage(exception.getMessage());
                    }
                    ntMessage.setData(errorMessage);
                    log.error("execute handlers has error:", e);
                } finally {
                    ctx.writeAndFlush(ntMessage);
                }
            }
        } else {
            ctx.read();
        }
    }


    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.debug("client [{}] connected", ctx.channel().id());
    }

    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.debug("client [{}] disconnected", ctx.channel().id());
    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.warn("close client [{}] on error:{}", ctx.channel().id(), cause.getMessage());
        ctx.close();
    }

}
