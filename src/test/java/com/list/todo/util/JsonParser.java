package com.list.todo.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.list.todo.entity.TodoList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JsonParser {

    public static List<TodoList> getArrayOfTodoListsFromJsonResponse(String response, int numberOfTodoLists) throws JSONException, IOException {
        List<TodoList> returnedTodoLists = new ArrayList<>();
        JSONArray jsonArray = new JSONArray(response);
        ObjectMapper objectMapper = new ObjectMapper();

        for (int i=0; i<numberOfTodoLists; i++){
            TodoList returnedTodoList = objectMapper
                    .readValue(jsonArray.get(i).toString(), TodoList.class);
            returnedTodoLists.add(returnedTodoList);
        }
        return returnedTodoLists;
    }

    public static TodoList getTodoListFromJsonResponse(String response) throws JSONException, IOException {
        JSONObject jsonObject = new JSONObject(response);
        ObjectMapper objectMapper = new ObjectMapper();

        return objectMapper.readValue(jsonObject.toString(), TodoList.class);
    }
}
