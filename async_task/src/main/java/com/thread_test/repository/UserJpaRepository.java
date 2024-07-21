package com.thread_test.repository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import com.thread_test.entity.User;

@Component
public interface UserJpaRepository extends CrudRepository<User, Long>{

}
