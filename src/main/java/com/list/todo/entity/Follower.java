package com.list.todo.entity;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class Follower extends BaseEntity {

    @NotNull
    private Long followedUserId;

    @ManyToOne
    @JoinColumn(name = "follower_user_id")
    @NotNull
    private User follower;
}
