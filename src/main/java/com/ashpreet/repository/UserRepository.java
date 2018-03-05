package com.ashpreet.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.ashpreet.model.User;



@Repository("userRepository")
public interface UserRepository extends CrudRepository<User,Long>{
	
	User findByEmail(String email);
    User findByConfirmationToken(String confirmationToken);
    User findByPassword(String password);
}
