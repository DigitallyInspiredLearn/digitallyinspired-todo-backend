package com.list.todo.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "todoList")
@Data @EqualsAndHashCode(callSuper=true)
public class TodoList extends BaseEntity {
	
	@NotNull
	@Size(max = 100)
    private String todoListName;

    @NotNull
    private Long userOwnerId;
    
}
