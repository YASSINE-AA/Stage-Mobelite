package com.thread_test.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.thread_test.entity.User;
import com.thread_test.repository.UserRepository;

@Service
public class CrudService {

    @Autowired
    private UserRepository userJpaRepository;


    User insert(User user) {
        return userJpaRepository.save(user);
    }
}
