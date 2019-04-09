package com.list.todo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper=true, exclude = "todoList")
@ToString(exclude = "todoList")
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Task extends BaseEntity {
	
	@NotNull
	private String body;
	
	@NotNull
	private Boolean isComplete;

	@Column(nullable = false, updatable = false)
	@CreatedBy
	private String createdBy;

	@Column(nullable = false, updatable = false)
	@CreatedDate
	private Long createdDate;

	private Long completedDate;

	private Long durationTime;

	@ManyToOne
	@JoinColumn(name = "todolist_id")
	@JsonIgnore
	private TodoList todoList;
}
