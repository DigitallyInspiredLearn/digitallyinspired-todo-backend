package com.list.todo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

@Entity
@Data @EqualsAndHashCode(callSuper=true, exclude = "todoList")
public class Task extends BaseEntity {
	
	@NotNull
	private String body;
	
	@NotNull
	private Boolean isComplete;

	@ManyToOne
	@JoinColumn(name = "todolist_id")
	@JsonIgnore
	private TodoList todoList;
}
