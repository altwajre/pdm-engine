package cn.betasoft.pdm.engine.monitor.stream;

import cn.betasoft.pdm.engine.monitor.listener.CollectStatListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class WrapperSerde<T> implements Serde<T> {

    private final ObjectMapper mapper = new ObjectMapper();
    private final Class<T> cls;

    private static final Logger logger = LoggerFactory.getLogger(WrapperSerde.class);

    public WrapperSerde(Class<T> cls) {
        this.cls = cls;
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public void close() {

    }

    @Override
    public Serializer<T> serializer() {
        return new Serializer<T>() {

            @Override
            public void configure(Map<String, ?> configs, boolean isKey) {

            }

            @Override
            public byte[] serialize(String topic, T data) {
                try {
                    return mapper.writeValueAsBytes(data);
                } catch (Exception e) {
                    throw new SerializationException("Error serializing JSON message", e);
                }
            }

            @Override
            public void close() {

            }
        };

    }

    @Override
    public Deserializer<T> deserializer() {
        return new Deserializer<T>() {
            @Override
            public void configure(Map<String, ?> configs, boolean isKey) {

            }

            @Override
            public T deserialize(String topic, byte[] data) {
                T result;
                try {
                    return result = mapper.readValue(data, cls);
                } catch (Exception e) {
                    logger.info("Can't deserialize data [" + new String(data) +
                            "] from topic [" + topic + "]", e);
                    return null;
                }
            }

            @Override
            public void close() {

            }
        };
    }
}

