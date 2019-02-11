package com.list.todo.entity;

import com.list.todo.payload.UserSummary;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Follower extends BaseEntity {


    @NotNull
    private Long followedUserId;

    @ManyToOne
    @JoinColumn(name = "followeruser_id")
    @NotNull
    private User follower;

    public UserSummary getFollowerUserSumm(){
        return new UserSummary(follower.getUsername(), follower.getName(), follower.getEmail());
    }
}
