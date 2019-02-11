package com.list.todo.payload;

import lombok.*;

@Getter @Setter @AllArgsConstructor
public class JwtAuthenticationResponse {
    private String accessToken;
    private final String tokenType = "Bearer";


}