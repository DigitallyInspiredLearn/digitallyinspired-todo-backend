package com.list.todo.entity;

import javax.persistence.*;

import lombok.Data;

@Entity
@Inheritance(strategy=InheritanceType.TABLE_PER_CLASS)
@Data
public class BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
}
