package com.list.todo.entity;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import lombok.*;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor 
@NoArgsConstructor
@ToString
@Builder
public class Share extends BaseEntity {

	@NotNull
	private Long sharedUserId;

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "sharedtodolist_id")
	@NotNull
	private TodoList sharedTodoList;

}
