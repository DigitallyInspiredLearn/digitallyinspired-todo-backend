package com.list.todo.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import javax.validation.constraints.Size;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

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
	private String email;

	@NonNull
	@Size(max = 100)
	private String password;

	private RoleName role;
}
