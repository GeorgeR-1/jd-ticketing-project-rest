package com.cybertek.controller;

import com.cybertek.annotation.DefaultExceptionMessage;
import com.cybertek.dto.UserDTO;
import com.cybertek.entity.ResponseWrapper;
import com.cybertek.entity.User;
import com.cybertek.entity.common.AuthenticationRequest;
import com.cybertek.exception.TicketingProjectException;
import com.cybertek.mapper.UserMapper;
import com.cybertek.service.UserService;
import com.cybertek.util.JWTUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Authentication Controller",description = "Authenticate API")
public class LoginController {

	private AuthenticationManager authenticationManager;
	private UserService userService;
	private UserMapper userMapper;
	private JWTUtil jwtUtil;

	public LoginController(AuthenticationManager authenticationManager,UserService userService,
						   UserMapper userMapper, JWTUtil jwtUtil) {

		this.authenticationManager = authenticationManager;
		this.userService = userService;
		this.userMapper = userMapper;
		this.jwtUtil = jwtUtil;
	}

	@PostMapping("/authenticate")
	@DefaultExceptionMessage(defaultMassage = "Bad credentials")
	@Operation(summary = "Login to application")
	public ResponseEntity<ResponseWrapper> doLogin(@RequestBody AuthenticationRequest authenticationRequest) throws TicketingProjectException {

		String password = authenticationRequest.getPassword();
		String username = authenticationRequest.getUsername();

		UsernamePasswordAuthenticationToken authentication =
				new UsernamePasswordAuthenticationToken(username,password);

		authenticationManager.authenticate(authentication);

		UserDTO foundUser = userService.findByUserName(username);
		User convertedUser = userMapper.convertToEntity(foundUser);

		if(!foundUser.isEnabled()){
			throw new TicketingProjectException("Please verify your user");
		}

		String jwtToken = jwtUtil.generateToken(convertedUser);

		return ResponseEntity.ok(new ResponseWrapper("Login Successful",jwtToken));

	}

	@DefaultExceptionMessage(defaultMassage = "Something went wrong, try again!")
	@PostMapping("/create-user")
	@Operation(summary = "Create new account")
	private ResponseEntity<ResponseWrapper> doRegister(@RequestBody UserDTO userDTO){

		UserDTO createdUser = userService.save(userDTO);



	}



}
