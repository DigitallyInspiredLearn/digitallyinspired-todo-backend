package com.list.todo.entity;

import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "todoList")
@Data
@EqualsAndHashCode(callSuper = true, exclude = "tasks")
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class TodoList extends BaseEntity {

    @NotNull
    @Size(max = 100)
    private String todoListName;

    @Column(nullable = false, updatable = false)
    @CreatedDate
    private Long createdDate;

    @LastModifiedDate
    private Long modifiedDate;

    @Column(nullable = false, updatable = false)
    @CreatedBy
    private String createdBy;

    @LastModifiedBy
    private String modifiedBy;

    @NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TodoListStatus todoListStatus;

    @NotNull
    private String comment;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "todoList")
    private Set<Task> tasks = new LinkedHashSet<>();
}
