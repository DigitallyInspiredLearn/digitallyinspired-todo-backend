package com.list.todo.entity;

import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Data @EqualsAndHashCode(callSuper=true) 
public class Task extends BaseEntity {
	
	@NotNull
	private String body;
	
	@NotNull
	private Boolean isComplete;
	
	@NotNull
	private Long todoListId;
}
