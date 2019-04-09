package com.list.todo.entity;

import lombok.*;

import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class Tag extends BaseEntity{

    @NotNull
    private String tagName;

    @NotNull
    private Long ownerId;
}
