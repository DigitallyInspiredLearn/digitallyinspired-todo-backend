package com.list.todo.entity;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "project")
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

    public Project() {
    }

    public Project(String projectName, Long userOwnerId) {
		this.projectName = projectName;
		this.userOwnerId = userOwnerId;
		/*this.startDate = startDate;
        this.endDate = endDate;*/
	}

	public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

/*    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }*/

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }

	public Long getUserOwnerId() {
		return userOwnerId;
	}

	public void setUserOwnerId(Long userOwnerId) {
		this.userOwnerId = userOwnerId;
	}

	public Set<Task> getTasks() {
		return tasks;
	}

	public void setTasks(Set<Task> tasks) {
		this.tasks = tasks;
	}
	
	
    
}
