package pres.raefing.ntp.netty.sp.handler;

import pres.raefing.ntp.netty.base.msg.NtIdleMessage;
import pres.raefing.ntp.netty.exception.NtException;
import pres.raefing.ntp.netty.sp.api.HandleContext;
import pres.raefing.ntp.netty.sp.api.NtMessageHandler;

public class NtIdleHandler implements NtMessageHandler {
    @Override
    public void handleMessage(HandleContext context) throws NtException {
        Object request = context.getRequest();
        if (request instanceof NtIdleMessage) {
            context.setResponse(request);
            context.interrupt();
        }
    }

    @Override
    public int order() {
        return Integer.MIN_VALUE;
    }
}
