package com.list.todo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import javax.validation.constraints.Size;

@Entity
@Table(name = "users")
@Data @NoArgsConstructor
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper=true)
public class User extends BaseEntity {
	
	@NonNull
	@Size(max = 40)
	private String name;

	@NonNull
	@Size(max = 30)
	private String username;

	@NonNull
	@Size(max = 40)
	@Email
	@JsonIgnore
	private String email;

	@NonNull
	@Size(max = 100)
	@JsonIgnore
	private String password;

	@JsonIgnore
	private RoleName role;
}
