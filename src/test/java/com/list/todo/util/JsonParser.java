package com.list.todo.util;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.list.todo.entity.BaseEntity;

import java.io.IOException;
import java.util.List;

public class JsonParser<T extends BaseEntity> {

    private final Class<T> type;

    public JsonParser(Class<T> type) {
        this.type = type;
    }

    public Class<T> getType() {
        return type;
    }

    public List<T> getListOfObjectsFromJsonResponse(String response) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();

        JavaType type = objectMapper.getTypeFactory().constructCollectionType(List.class, this.getType());

        return objectMapper.readValue(response, type);
    }
}
