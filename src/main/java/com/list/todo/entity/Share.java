package com.list.todo.entity;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor 
@NoArgsConstructor
public class Share extends BaseEntity {

	@NotNull
	private Long sharedUserId;

	@ManyToOne
	@JoinColumn(name = "sharedtodolist_id")
	@NotNull
	private TodoList sharedTodoList;

}
