package com.list.todo.entity;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
public class Share extends BaseEntity {

	@NotNull
	private Long sharedUserId;

	@ManyToOne
	@JoinColumn(name = "sharedtodolist_id")
	@NotNull
	private TodoList sharedTodoList;

}
