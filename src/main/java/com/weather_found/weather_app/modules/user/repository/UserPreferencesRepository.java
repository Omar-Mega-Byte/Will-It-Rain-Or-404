package com.weather_found.weather_app.modules.user.repository;

import com.weather_found.weather_app.modules.user.model.UserPreferences;
import com.weather_found.weather_app.modules.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserPreferencesRepository extends JpaRepository<UserPreferences, Long> {
    Optional<UserPreferences> findByUser(User user);
}
