package com.list.todo.controllers;

import com.list.todo.entity.Tag;
import com.list.todo.services.TagService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/tags")
@AllArgsConstructor
@PreAuthorize("hasRole('ROLE_USER')")
public class TagController {

    private final TagService tagService;

    @PostMapping
    public ResponseEntity<Optional<Tag>> addTag(){
        return null;
    }


}
