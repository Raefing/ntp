package pres.reafing.ntp.sp.test;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import pres.raefing.ntp.netty.sp.api.NtMessageHandler;
import pres.raefing.ntp.netty.exception.NtException;
import pres.raefing.ntp.netty.sp.NtServer;
import pres.raefing.ntp.netty.sp.handler.NtIdleHandler;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpTestApplication.class)
public class SpTester {

    private NtServer ntServer;

    @Before
    public void startServer(){
        List<NtMessageHandler> messageHandlers = new ArrayList<>();
        messageHandlers.add(new NtIdleHandler());
        messageHandlers.add((msg) -> {
            System.err.println(msg.toString());
            throw new NtException("9999","runtimeException",null);
        });
        ntServer = new NtServer(10010, messageHandlers);
        ntServer.start();
    }

    @Test
    public void waitS(){
        try {
            Thread.sleep(1000000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void stop(){
        ntServer.stop();
    }

}
