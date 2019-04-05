package com.list.todo.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserStatistics {

    private Long todolistsAmount;
    private Long tasksAmount;
    private Long completedTasksAmount;
    private int followersAmount;
    private int followedUsersAmount;

}
