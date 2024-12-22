package pres.raefing.ntp.netty.base;

import lombok.Data;


@Data
public class NtMessage {
    /**
     *
     */
    private String version;
    /**
     *
     */
    private String messageId;
    /**
     *
     */
    private Object data;

    public static NtMessage v1(Object obj, String messageId) {
        NtMessage msg = new NtMessage();
        msg.version = "1.0";
        msg.messageId = messageId;
        msg.data = obj;
        return msg;
    }

}
