package com.list.todo.entity;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.*;

@Entity
@Table(name = "todoList")
@Data @EqualsAndHashCode(callSuper=true, exclude = "tasks")
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class TodoList extends BaseEntity {
	
	@NotNull
	@Size(max = 100)
    private String todoListName;

    @NotNull
    private Long userOwnerId;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "todoList")
    private Set<Task> tasks = new LinkedHashSet<>();



}
