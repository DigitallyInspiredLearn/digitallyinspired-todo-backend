package com.list.todo.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserStatistics {

    private Long todoListsNumber;
    private Long tasksNumber;
    private Long completedTasksNumber;
    private int followersNumber;
    private int followedUsersNumber;

}
