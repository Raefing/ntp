package pres.raefing.ntp.netty.base;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.extern.slf4j.Slf4j;
import pres.raefing.ntp.netty.base.msg.NtErrorMessage;
import pres.raefing.ntp.netty.base.msg.NtIdleMessage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/********************************
 * <br/>
 * <span>
 * |8   |32     |1    |2    |n  |<br/>
 * |版本 |ID     |TYPE |长度 |数据|
 * </span>
 ********************************/
@Slf4j
public class NtMessageSerial {

    private static final int VERSION_LEG = 4;
    private static final int ID_LEG = 32;

    private final Map<Short, Class<?>> classMap = new HashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();

    public NtMessageSerial() {
        mapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        SimpleModule simpleModule = new SimpleModule();
        //可以替换序列化和反序列化实现，以达到数据的额外处理
        //simpleModule.addDeserializer(String.class, new StringDeserializer());
        //simpleModule.addSerializer(String.class, new StringSerializer());
        //mapper.registerModule(simpleModule);
        classMap.put((short) 997, NtIdleMessage.class);
        classMap.put((short) 998, String.class);
        classMap.put((short) 999, NtErrorMessage.class);
    }

    public void registerModule(short id, Class<?> clazz) {
        if (classMap.containsKey(id)) {
            log.warn("duplicate id {} found in classMap,will replica", id);
        }
        classMap.put(id, clazz);
    }

    public byte[] encode(NtMessage obj) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Object msgData = obj.getData();
        writeString(outputStream, obj.getVersion(), VERSION_LEG);
        writeString(outputStream, obj.getMessageId(), ID_LEG);
        byte[] data = encodeBody(msgData);
        outputStream.write(data);
        return outputStream.toByteArray();
    }


    public NtMessage decode(byte[] msg) throws Exception {
        ByteBuffer byteBuffer = ByteBuffer.wrap(msg);
        String version = readString(byteBuffer, VERSION_LEG);
        String id = readString(byteBuffer, ID_LEG);
        Object obj = decodeBody(byteBuffer);
        NtMessage message = new NtMessage();
        message.setVersion(version);
        message.setMessageId(id);
        message.setData(obj);
        return message;
    }

    private String readString(ByteBuffer byteBuffer, int length) throws Exception {
        byte[] data = new byte[length];
        byteBuffer.get(data);
        return new String(data).trim();
    }

    private void writeString(OutputStream outputStream, String data, int len) throws IOException {
        byte[] bytes = new byte[len];
        byte[] source = data.getBytes();
        System.arraycopy(source, 0, bytes, 0, source.length);
        if (source.length < len) {
            for (int i = source.length; i < len; i++) {
                bytes[i] = ' ';
            }
        }
        outputStream.write(bytes);
    }

    private Object decodeBody(ByteBuffer byteBuffer) throws Exception {
        short typeId = byteBuffer.getShort();
        int bodyLen = byteBuffer.getInt();
        byte[] data = new byte[bodyLen];
        byteBuffer.get(data);
        byte[] orgData = Base64.getDecoder().decode(data);
        try {
            return mapper.readValue(orgData, classMap.get(typeId));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] encodeBody(Object data) {
        try {
            short id = 99;
            for (Short map : classMap.keySet()) {
                Class<?> clazz = classMap.get(map);
                if (clazz.equals(data.getClass())) {
                    id = map;
                    break;
                }
            }
            byte[] objBytes = Base64.getEncoder().encode(mapper.writeValueAsBytes(data));
            ByteBuffer byteBuffer = ByteBuffer.allocate(objBytes.length + 6);
            byteBuffer.putShort(id);
            byteBuffer.putInt(objBytes.length);
            byteBuffer.put(objBytes);
            return byteBuffer.array();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
