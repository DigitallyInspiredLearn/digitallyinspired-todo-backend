package com.list.todo.entity;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "todoList")
@Getter @Setter @NoArgsConstructor @ToString
public class TodoList {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String todoListName;

    @Column(nullable=false)
    private Long userOwnerId;
    
    @OneToMany(cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            mappedBy = "todoList")
    private Set<Task> tasks = new HashSet<>();
    
/*    @Temporal(TemporlType.TIMESTAMP)
    private Date startDate;
    
    private Date endDate;*/

    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE}, mappedBy = "todoLists")
    private Set<User> users = new HashSet<>();

}
