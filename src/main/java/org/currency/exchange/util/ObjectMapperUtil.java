package org.currency.exchange.util;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ObjectMapperUtil {
    private final ObjectMapper objectMapper;

    private ObjectMapperUtil() {
        this.objectMapper = new ObjectMapper();
    }

    private static class ObjectMapperUtilHelper {
        private static final ObjectMapperUtil INSTANCE = new ObjectMapperUtil();
    }

    public static ObjectMapper getInstance() {
        return ObjectMapperUtilHelper.INSTANCE.objectMapper;
    }
}
