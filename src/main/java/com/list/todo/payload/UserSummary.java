package com.list.todo.payload;

import lombok.*;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(exclude = "gravatarUrl")
public class UserSummary {
    private String username;
    private String name;
    private String email;
    private String gravatarUrl;
}
