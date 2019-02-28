package com.list.todo.repositories;

import com.list.todo.entity.User;
import com.list.todo.entity.UserSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserSettingsRepository extends JpaRepository<UserSettings, Long> {

    Optional<UserSettings> getUserSettingsByUserId(Long userId);
}
