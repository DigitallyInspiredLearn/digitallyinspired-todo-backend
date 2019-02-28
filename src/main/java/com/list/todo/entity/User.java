package com.list.todo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.Size;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@RequiredArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper=true)
@ToString
@Builder
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

	@NonNull
	private String gravatarHash;

	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "user")
	private UserSettings userSettings;
}
