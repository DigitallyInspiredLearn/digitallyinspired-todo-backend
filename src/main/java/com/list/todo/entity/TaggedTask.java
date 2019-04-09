package com.list.todo.entity;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class TaggedTask extends BaseEntity {

    @NotNull
    private Long taskId;

    @ManyToOne
    @JoinColumn(name = "taggedTask_tag_id")
    @NotNull
    private Tag tag;
    
}
