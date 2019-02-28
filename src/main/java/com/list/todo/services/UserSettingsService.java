package com.list.todo.services;

import com.list.todo.entity.User;
import com.list.todo.entity.UserSettings;
import com.list.todo.payload.UserSettingsInput;
import com.list.todo.repositories.UserSettingsRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class UserSettingsService {

    private final UserSettingsRepository userSettingsRepository;

    public Optional<UserSettings> getUserSettingsByUserId(Long userId){
        return userSettingsRepository.getUserSettingsByUserId(userId);
    }

    public void addUserSettings(User user){
        UserSettings userSettings = UserSettings.builder()
                .isEnableEmailNotification(true)
                .isEnableWebSocketNotification(true)
                .user(user)
                .build();

        userSettingsRepository.save(userSettings);
    }

    public Optional<UserSettings> updateUserSettings(UserSettingsInput userSettingsInput, Long userId){
        return getUserSettingsByUserId(userId)
                .map(userSettings1 -> {
                    userSettings1.setIsEnableEmailNotification(userSettingsInput.getIsEnableEmailNotification());
                    userSettings1.setIsEnableWebSocketNotification(userSettingsInput.getIsEnableWebSocketNotification());
                    return userSettingsRepository.save(userSettings1);
                });
    }

}
