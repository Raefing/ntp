package pres.raefing.ntp.netty.sp.api;

import pres.raefing.ntp.netty.exception.NtException;

@FunctionalInterface
public interface NtMessageHandler {

    void handleMessage(HandleContext context) throws NtException;

    default int order() {
        return 0;
    }
}
