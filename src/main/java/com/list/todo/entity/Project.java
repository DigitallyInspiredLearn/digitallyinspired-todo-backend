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
@Table(name = "project")
@Getter @Setter @NoArgsConstructor @ToString
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String projectName;

    @Column(nullable=false)
    private Long userOwnerId;
    
    @OneToMany(cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            mappedBy = "project")
    private Set<Task> tasks = new HashSet<>();
    
/*    @Temporal(TemporlType.TIMESTAMP)
    private Date startDate;
    
    private Date endDate;*/

    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE}, mappedBy = "projects")
    private Set<User> users = new HashSet<>();

}
