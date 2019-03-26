package org.mallen.test.common.utils;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;

/**
 * 使用jackson对json数据进行操作的工具类
 *
 * @author mallen
 * @date 11/07/18
 */
public class JSONUtil {

    private static final Logger logger = LoggerFactory.getLogger(JSONUtil.class);

    private static final ObjectMapper objectMapper;


    static {
        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        objectMapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    /**
     * 获取全局变量objectMapper，如果需要重新配置objectMapper，可以通过该方法获取到objectMapper，然后对其进行配置。
     * 需要注意的是，对objectMapper的修改将会体现到所有的使用JSONUtil的地方
     *
     * @return
     */
    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public static String writeValueAsString(Object value) {
        if (value == null) {
            return null;
        }
        String result = null;
        try {
            result = objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            logger.error("", e);
        }
        return result;
    }

    public static <T> T readValue(URL url, Class<T> valueType) {
        if (url == null) {
            return null;
        }
        T result = null;
        try {
            result = objectMapper.readValue(url, valueType);
        } catch (IOException e) {
            logger.error("", e);
        }
        return result;
    }

    public static <T> T readValue(URL url, TypeReference valueTypeRef) {
        if (url == null) {
            return null;
        }
        T result = null;
        try {
            result = objectMapper.readValue(url, valueTypeRef);
        } catch (IOException e) {
            logger.error("", e);
        }
        return result;
    }

    public static <T> T readValue(DataInput dataInput, Class<T> valueType) {
        if (dataInput == null) {
            return null;
        }
        T result = null;
        try {
            result = objectMapper.readValue(dataInput, valueType);
        } catch (IOException e) {
            logger.error("", e);
        }
        return result;
    }

    public static <T> T readValue(Reader reader, Class<T> valueType) {
        if (reader == null) {
            return null;
        }
        T result = null;
        try {
            result = objectMapper.readValue(reader, valueType);
        } catch (IOException e) {
            logger.error("", e);
        }
        return result;
    }

    public static <T> T readValue(Reader reader, TypeReference valueTypeRef) {
        if (reader == null) {
            return null;
        }
        T result = null;
        try {
            result = objectMapper.readValue(reader, valueTypeRef);
        } catch (IOException e) {
            logger.error("", e);
        }
        return result;
    }

    public static <T> T readValue(InputStream is, Class<T> valueType) {
        if (is == null) {
            return null;
        }
        T result = null;
        try {
            result = objectMapper.readValue(is, valueType);
        } catch (IOException e) {
            logger.error("", e);
        }
        return result;
    }

    public static <T> T readValue(InputStream is, TypeReference valueTypeRef) {
        if (is == null) {
            return null;
        }
        T result = null;
        try {
            result = objectMapper.readValue(is, valueTypeRef);
        } catch (IOException e) {
            logger.error("", e);
        }
        return result;
    }

    public static <T> T readValue(File file, Class<T> valueType) {
        if (file == null || !file.exists()) {
            return null;
        }
        T result = null;
        try {
            result = objectMapper.readValue(file, valueType);
        } catch (IOException e) {
            logger.error("", e);
        }
        return result;
    }

    public static <T> T readValue(File file, TypeReference valueTypeRef) {
        if (file == null || !file.exists()) {
            return null;
        }
        T result = null;
        try {
            result = objectMapper.readValue(file, valueTypeRef);
        } catch (IOException e) {
            logger.error("", e);
        }
        return result;
    }

    public static <T> T readValue(byte[] val, Class<T> valueType) {
        if (val == null || val.length < 1) {
            return null;
        }
        T result = null;
        try {
            result = objectMapper.readValue(val, valueType);
        } catch (IOException e) {
            logger.error("", e);
        }
        return result;
    }

    public static <T> T readValue(String content, Class<T> valueType) {
        if (content == null) {
            return null;
        }
        T result = null;
        try {
            result = objectMapper.readValue(content, valueType);
        } catch (IOException e) {
            logger.error("", e);
        }
        return result;
    }

    public static <T> T readValue(String content, TypeReference valueTypeRef) {
        if (content == null) {
            return null;
        }
        T result = null;
        try {
            result = objectMapper.readValue(content, valueTypeRef);
        } catch (IOException e) {
            logger.error("", e);
        }
        return result;
    }

    public static <T> T readValue(byte[] content, TypeReference valueTypeRef) {
        if (content == null) {
            return null;
        }
        T result = null;
        try {
            result = objectMapper.readValue(content, valueTypeRef);
        } catch (IOException e) {
            logger.error("", e);
        }
        return result;
    }

}
