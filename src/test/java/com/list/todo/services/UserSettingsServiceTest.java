package com.list.todo.services;

import com.list.todo.entity.User;
import com.list.todo.entity.UserSettings;
import com.list.todo.payload.UserSettingsInput;
import com.list.todo.repositories.UserSettingsRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static com.list.todo.util.ObjectsProvider.createUser;
import static com.list.todo.util.ObjectsProvider.createUserSettings;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UserSettingsServiceTest {

    private static final Long CURRENT_USER_ID = 1L;

    @Mock
    private UserSettingsRepository userSettingsRepository;

    @InjectMocks
    private UserSettingsService userSettingsService;


    @Test
    public void getUserSettingsByUserId_OnExistentUser_ReturnsAnOptionalOfUserSettings() {
        //arrange
        UserSettings userSettings = createUserSettings();
        when(userSettingsRepository.getUserSettingsByUserId(CURRENT_USER_ID)).thenReturn(Optional.of(userSettings));

        //act
        Optional<UserSettings> returnedUserSettings = userSettingsService.getUserSettingsByUserId(CURRENT_USER_ID);

        //assert
        Assert.assertEquals(userSettings, returnedUserSettings.get());
        verify(userSettingsRepository).getUserSettingsByUserId(CURRENT_USER_ID);
    }

    @Test
    public void addUserSettings_OnExistentUser_SuccessfulAdding() {
        //arrange
        User user = createUser(1);
        user.setId(CURRENT_USER_ID);
        UserSettings userSettings = createUserSettings();
        userSettings.setUser(user);

        //act
        userSettingsService.addUserSettings(user);

        //assert
        verify(userSettingsRepository).save(userSettings);
    }

    @Test
    public void updateUserSettings_OnExistentUserSettings_ReturnsAnOptionalOfUserSettings() {
        //arrange
        UserSettingsInput userSettingsInput = new UserSettingsInput(false, true);
        UserSettings updatedUserSettings = new UserSettings(
                userSettingsInput.getIsEnableEmailNotification(),
                userSettingsInput.getIsEnableWebSocketNotification());
        UserSettings userSettings = mock(UserSettings.class);
        when(userSettingsRepository.getUserSettingsByUserId(CURRENT_USER_ID)).thenReturn(Optional.of(userSettings));
        when(userSettingsRepository.save(userSettings)).thenReturn(updatedUserSettings);

        //act
        Optional<UserSettings> returnedUserSettings = userSettingsService.updateUserSettings(userSettingsInput, CURRENT_USER_ID);

        //assert
        Assert.assertEquals(updatedUserSettings, returnedUserSettings.get());
        verify(userSettings).setIsEnableEmailNotification(userSettingsInput.getIsEnableEmailNotification());
        verify(userSettings).setIsEnableWebSocketNotification(userSettingsInput.getIsEnableWebSocketNotification());
        verify(userSettingsRepository).getUserSettingsByUserId(CURRENT_USER_ID);
        verify(userSettingsRepository).save(userSettings);
    }

    @Test
    public void updateUserSettings_OnNonExistentUserSettings_ReturnsAnEmptyOptional() {
        //arrange
        UserSettingsInput userSettingsInput = new UserSettingsInput(true, true);
        when(userSettingsRepository.getUserSettingsByUserId(CURRENT_USER_ID)).thenReturn(Optional.empty());

        //act
        Optional<UserSettings> returnedUserSettings = userSettingsService.updateUserSettings(userSettingsInput, CURRENT_USER_ID);

        //assert
        Assert.assertTrue(!returnedUserSettings.isPresent());
        verify(userSettingsRepository).getUserSettingsByUserId(CURRENT_USER_ID);
        verify(userSettingsRepository, times(0)).save(any(UserSettings.class));
    }
}
