package pres.raefing.ntp.netty.base.msg;

import lombok.Data;

@Data
public class NtErrorMessage {
    private String code;
    private String message;
}
