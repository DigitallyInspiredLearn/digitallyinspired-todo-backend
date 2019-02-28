package com.list.todo.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSettingsInput {

    @NotBlank
    private Boolean isEnableEmailNotification;

    @NotBlank
    private Boolean isEnableWebSocketNotification;
}
