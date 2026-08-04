package com.yd.todo.user.model.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.yd.todo.user.model.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByProviderAndProviderId(String provider, String providerId);
}
