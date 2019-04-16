package com.list.todo.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSummary implements Comparable<UserSummary> {
    private String username;
    private String name;
    private String email;
    private String gravatarUrl;

    @Override
    public int compareTo(@NotNull UserSummary o) {
        return this.username.compareTo(o.username);
    }
}
