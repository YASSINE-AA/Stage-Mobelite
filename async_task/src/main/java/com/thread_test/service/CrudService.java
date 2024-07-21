package com.thread_test.service;

import com.thread_test.entity.User;
import com.thread_test.repository.UserJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CrudService {

    @Autowired
    private UserJpaRepository userJpaRepository;


    User insert(User user) {
        return userJpaRepository.save(user);
    }
}
