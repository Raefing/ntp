package pres.raefing.ntp.netty.exception;


import lombok.Getter;

public class NtException extends Exception{
    @Getter
    private String code;

    public NtException(String code,String message,Throwable throwable){
        super(message,throwable);
        this.code = code;
    }
}
