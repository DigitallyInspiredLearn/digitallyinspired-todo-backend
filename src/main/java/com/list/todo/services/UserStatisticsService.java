package com.list.todo.services;

import com.list.todo.entity.User;
import com.list.todo.payload.UserStatistics;
import com.list.todo.payload.UserSummary;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UserStatisticsService {

    private FollowerService followerService;
    private TodoListService todoListService;
    private TaskService taskService;
    private UserService userService;

    public UserStatistics getUserStatisticsByUserId(Long userId){
        Optional<User> user = userService.getUserById(userId);
        Long todoListsNumber = 0L;
        Long tasksNumber = 0L;
        Long completedTaskNumber = 0L;
        int followersNumber = 0;
        int followedUsersNumber = 0;

        if (user.isPresent()){
            todoListsNumber = todoListService.countTodolistsByCreatedBy(user.get().getUsername());
            tasksNumber = taskService.countTasksByCreatedBy(user.get().getUsername());
            completedTaskNumber =
                    taskService.countTasksByCreatedByAndIsComplete(user.get().getUsername(), true);
            List<UserSummary> followersList = followerService.getFollowersUserSummariesByUserId(userId);
            List<UserSummary> followedUsersList = followerService.getFollowedUserSummariesByUserId(userId);

            followersNumber = followersList.size();
            followedUsersNumber = followedUsersList.size();
        }

        return new UserStatistics(todoListsNumber, tasksNumber, completedTaskNumber, followersNumber, followedUsersNumber);
    }
}
