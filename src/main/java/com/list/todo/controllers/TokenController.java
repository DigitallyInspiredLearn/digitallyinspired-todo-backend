package com.list.todo.controllers;

import com.list.todo.payload.LoginRequest;
import com.list.todo.security.UserPrincipal;
import com.list.todo.services.AuthenticationService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/token")
@AllArgsConstructor
public class TokenController {

    private final AuthenticationService authenticationService;

    @PostMapping("/update")
    public ResponseEntity<?> updateAccessToken(@AuthenticationPrincipal UserPrincipal currentUser) {

        return ResponseEntity.ok(authenticationService.updateAccessToken());
    }
}
