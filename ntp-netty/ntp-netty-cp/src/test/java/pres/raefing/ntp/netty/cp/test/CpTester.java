package pres.raefing.ntp.netty.cp.test;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import pres.raefing.ntp.commons.NtMinoCache;
import pres.raefing.ntp.commons.NtUIdMaker;
import pres.raefing.ntp.netty.cp.NtpClient;

import java.util.concurrent.TimeoutException;

public class CpTester {

    private NtpClient client;

    @Before
    public void init() {
        NtMinoCache ntMinoCache = new NtMinoCache();
        client = new NtpClient("127.0.0.1", 10010, ntMinoCache);
        client.setUIdMaker(new NtUIdMaker(1));
        client.connect();
    }

    @Test
    public void test() {
        /*try {
            Object obj = client.send("aaaaaaaaaa", 3000);
            System.err.println(obj);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }*/
    }

    @After
    public void close(){
        //client.close();
        try {
            Thread.sleep(300000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
