package com.ashpreet.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ashpreet.model.User;
import com.ashpreet.repository.UserRepository;

@Service("userService")
public class UserService {
	
private UserRepository userRepository;
	
    @Autowired
    public UserService(UserRepository userRepository) { 
      this.userRepository = userRepository;
    }
    
	public User findByEmail(String email) {
		return userRepository.findByEmail(email);
	}
	
	public User findByPassword(String password) {
		return userRepository.findByPassword(password);
	}
	
	public User findByConfirmationToken(String confirmationToken) {
		return userRepository.findByConfirmationToken(confirmationToken);
	}
	
	public void saveUser(User user) {
		userRepository.save(user);
	}

}
