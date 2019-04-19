package com.list.todo.payload;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
// TODO: remove this class in production
public class ApiResponse {
    private Boolean success;
    private String message;
}