package com.list.todo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "user_settings")
@Data
@NoArgsConstructor
@RequiredArgsConstructor()
@AllArgsConstructor
@EqualsAndHashCode(callSuper=true)
@ToString
@Builder
public class UserSettings extends BaseEntity {

    @NonNull
    private Boolean isEnableEmailNotification;

    @NonNull
    private Boolean isEnableWebSocketNotification;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;
}
