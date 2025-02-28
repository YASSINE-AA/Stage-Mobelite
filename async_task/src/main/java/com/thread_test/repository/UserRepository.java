package com.thread_test.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import com.thread_test.entity.User;

public interface UserRepository extends JpaRepository<User,Long> {

    User findByUsername(String username);
}
