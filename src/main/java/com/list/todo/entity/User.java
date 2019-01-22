package com.list.todo.entity;

import lombok.*;

import org.hibernate.annotations.NaturalId;
import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Data @NoArgsConstructor
@RequiredArgsConstructor
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank
	@NonNull
	@Size(max = 40)
	private String name;

	@NotBlank
	@NonNull
	@Size(max = 15)
	private String username;

	@NaturalId
	@NotBlank
	@NonNull
	@Size(max = 40)
	@Email
	private String email;

	@NotBlank
	@NonNull
	@Size(max = 100)
	private String password;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), 
		inverseJoinColumns = @JoinColumn(name = "role_id"))
	private Set<Role> roles = new HashSet<>();

	@ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	@JoinTable(name = "user_todolist", joinColumns = { @JoinColumn(name = "user_id") }, 
		inverseJoinColumns = {@JoinColumn(name = "todolist_id") })
	private Set<TodoList> todoLists = new HashSet<>();

	@Override
	public String toString() {
		return "User [id=" + id + ", name=" + name + ", username=" + username + ", email=" + email + ", password="
				+ password + ", roles=" + roles + ", todoLists=" + todoLists + "]";
	}

	
}
