package com.ashpreet.controller;

import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ashpreet.model.User;
import com.ashpreet.service.EmailService;
import com.ashpreet.service.UserService;
import com.nulabinc.zxcvbn.Strength;
import com.nulabinc.zxcvbn.Zxcvbn;




@Controller
public class RegisterController {
	
	@Autowired
	UserService userService;
	
	@Autowired
	EmailService emailService;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	
//    public RegisterController(BCryptPasswordEncoder bCryptPasswordEncoder,UserService userService, EmailService emailService) {
//      
//      this.userService = userService;
//      this.emailService = emailService;
//      this.bCryptPasswordEncoder=bCryptPasswordEncoder;
//    }
	
	
	
	
	@RequestMapping(value="/register", method = RequestMethod.GET)
		public ModelAndView showRegistrationPage(ModelAndView modelAndView, User user){
			modelAndView.addObject("user", user);
			modelAndView.setViewName("register");
			return modelAndView;
	}
	 
	
	@RequestMapping(value = "/register", method = RequestMethod.POST)
	public ModelAndView processRegistrationForm(ModelAndView modelAndView, @Valid User user, 
			BindingResult bindingResult, HttpServletRequest request) {
				
		
		User userExists = userService.findByEmail(user.getEmail());
		
		System.out.println(userExists);
		
		if (userExists != null) {
			modelAndView.addObject("alreadyRegisteredMessage", "User is already registered");
			modelAndView.setViewName("register");
			bindingResult.reject("email");
		}
			
		if (bindingResult.hasErrors()) { 
			modelAndView.setViewName("register");		
		} else { // new user so we create user and send confirmation e-mail
					
			// Disable user until they click on confirmation link in email
//		    user.setEnabled(false);
		      
		    // Generate random 36-character string token for confirmation link
		    user.setConfirmationToken(UUID.randomUUID().toString());
		        
		    userService.saveUser(user);
			System.out.println(request.getServerName());
			String appUrl = request.getScheme() + "://" + request.getServerName()+":"+request.getLocalPort();
			
			SimpleMailMessage registrationEmail = new SimpleMailMessage();
			registrationEmail.setTo(user.getEmail());
			registrationEmail.setSubject("Registration Confirmation");
			registrationEmail.setText("To confirm your e-mail address, please click the link below:\n"
					+ appUrl + "/confirm?token=" + user.getConfirmationToken());
//			registrationEmail.setText("To confirm your e-mail address, please click the link below:\n");
			registrationEmail.setFrom("ashpreetsaluja@gmail.com");
			
			emailService.sendEmail(registrationEmail);
			modelAndView.addObject("confirmationMessage", "A confirmation e-mail has been sent to " + user.getEmail());
			modelAndView.setViewName("register");
		}
			
		return modelAndView;
	}
	
	
	// Process confirmation link
		@RequestMapping(value="/confirm", method = RequestMethod.GET)
		public ModelAndView showConfirmationPage(ModelAndView modelAndView, @RequestParam("token") String token) {
				
			User user = userService.findByConfirmationToken(token);
				
			if (user == null) { // No token found in DB
				modelAndView.addObject("invalidToken", "Oops!  This is an invalid confirmation link.");
			} else { // Token found
				System.out.println("Token is valid");
				modelAndView.addObject("confirmationToken", user.getConfirmationToken());
			}
				
			modelAndView.setViewName("confirm");
			return modelAndView;		
		}
	
		
		// Process confirmation link for setting password
		@RequestMapping(value="/confirm", method = RequestMethod.POST)
		public ModelAndView processConfirmationForm(ModelAndView modelAndView, BindingResult bindingResult, 
				@RequestParam Map requestParams, RedirectAttributes redir) {
					
			modelAndView.setViewName("confirm");
			
			Zxcvbn passwordCheck = new Zxcvbn();
			
			Strength strength = passwordCheck.measure(requestParams.get("password").toString());
			String encryptpass = bCryptPasswordEncoder.encode(requestParams.get("password").toString());
			
			if (strength.getScore() < 3) {
				bindingResult.reject("password");
				
				redir.addFlashAttribute("errorMessage", "Your password is weak.");

				modelAndView.setViewName("redirect:confirm?token=" + requestParams.get("token"));
				System.out.println(requestParams.get("token"));
				return modelAndView;
			}
		
			User user = userService.findByConfirmationToken(requestParams.get("token").toString());

			// Set new password
			user.setPassword(encryptpass);

			// Set user to enabled
			user.setEnabled(true);
			
			// Save user
			userService.saveUser(user);
			
			modelAndView.addObject("successMessage", "Your password has been set!");
			return modelAndView;		
		}
	
	
	

}
