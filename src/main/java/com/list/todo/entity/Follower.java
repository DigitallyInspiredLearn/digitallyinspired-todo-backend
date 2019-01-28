package com.list.todo.entity;

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
    private Long followerUserId;

    @ManyToOne
    @JoinColumn(name = "followeduser_id")
    @NotNull
    private User followedUser;

}
