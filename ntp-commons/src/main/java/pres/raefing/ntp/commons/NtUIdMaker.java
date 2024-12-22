package pres.raefing.ntp.commons;

import pres.raefing.ntp.commons.utils.SnowflakeGen;

import java.util.UUID;

public class NtUIdMaker {

    private final SnowflakeGen snowflakeGen;

    public NtUIdMaker(int workId) {
        snowflakeGen = new SnowflakeGen(workId);
    }

    public long snowflakeId() {
        return snowflakeGen.nextId();
    }

    public String formatedSnowflakeId(int len) {
        long id = snowflakeId();
        String formated = String.format("%0" + len + "d", id);
        if (formated.length() > len) {
            throw new RuntimeException("length not enough");
        }
        return formated;
    }

    public String uuid() {
        return UUID.randomUUID().toString();
    }

}
