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
        Long todolistsAmount = 0L;
        Long tasksAmount = 0L;
        Long completedTaskAmount = 0L;
        int followersAmount = 0;
        int followedUsersAmount = 0;

        if (user.isPresent()){
            todolistsAmount = todoListService.countTodolistsByCreatedBy(user.get().getUsername());
            tasksAmount = taskService.countTasksByCreatedBy(user.get().getUsername());
            completedTaskAmount =
                    taskService.countTasksByCreatedByAndIsComplete(user.get().getUsername(), true);
            List<UserSummary> followersList = followerService.getFollowersUserSummariesByUserId(userId);
            List<UserSummary> followedUsersList = followerService.getFollowedUserSummariesByUserId(userId);

            followersAmount = followersList.size();
            followedUsersAmount = followedUsersList.size();
        }

        return new UserStatistics(todolistsAmount, tasksAmount, completedTaskAmount, followersAmount, followedUsersAmount);
    }
}
