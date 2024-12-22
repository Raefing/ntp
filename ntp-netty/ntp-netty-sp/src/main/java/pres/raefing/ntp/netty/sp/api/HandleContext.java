package pres.raefing.ntp.netty.sp.api;

import lombok.Data;

@Data
public class HandleContext {
    private HandleState state = HandleState.NORMAL;
    private Object request;
    private Object response;

    public void interrupt() {
        state = HandleState.INTERRUPT;
    }

    public boolean interrupted() {
        return state == HandleState.INTERRUPT;
    }
}
