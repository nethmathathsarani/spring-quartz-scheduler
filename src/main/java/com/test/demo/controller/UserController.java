package com.test.demo.controller;

import java.util.List;

import com.test.demo.service.UserInfoService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.test.demo.model.UserInfo;

@RestController
public class UserController {
	@Autowired
	private UserInfoService userService;
	private static org.slf4j.Logger logger = LoggerFactory.getLogger(UserController.class);

	@GetMapping("/user")
	public Object getAllUser(@RequestHeader HttpHeaders requestHeader,@RequestHeader (name="Authorization") String token) {
		List<UserInfo> userInfos = userService.getAllActiveUserInfo();
		if (userInfos == null || userInfos.isEmpty()) {
			return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
		}
		return userInfos;
	}

	@PostMapping("/user")
	public UserInfo addUser(@RequestBody UserInfo userRecord,@RequestHeader (name="Authorization") String token) {
		return userService.addUser(userRecord);
	}

	@PutMapping("/user/{userName}")
	public UserInfo updateUser(@RequestBody UserInfo userRecord, @PathVariable String userName,@RequestHeader (name="Authorization") String token) {
		return userService.updateUser(userName,userRecord);
	}
	
	@PutMapping("/user/changePassword/{userName}")
	public UserInfo updateUserPassword(@RequestBody UserInfo userRecord, @PathVariable String userName,@RequestHeader (name="Authorization") String token) {
		return userService.updatePassword(userName,userRecord);
	}
	
	@PutMapping("/user/changeRole/{userName}")
	public UserInfo updateUserRole(@RequestBody UserInfo userRecord, @PathVariable String userName,@RequestHeader (name="Authorization") String token) {
		return userService.updateRole(userName,userRecord);
	}

	@DeleteMapping("/user/{userName}")
	public void deleteUser(@PathVariable String userName,@RequestHeader (name="Authorization") String token) {
		userService.deleteUser(userName);
	}

	@GetMapping("/user/{userName}")
	public ResponseEntity<UserInfo> getUserByUserName(@PathVariable String  userName,@RequestHeader (name="Authorization") String token) {
		UserInfo userInfo = userService.getUserInfoById(userName);
		if (userInfo == null) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(userInfo, HttpStatus.OK);
	}
}
