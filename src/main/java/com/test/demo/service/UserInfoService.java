package com.test.demo.service;

import java.util.List;

import com.test.demo.repository.UserDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.test.demo.model.UserInfo;

@Repository
@Transactional
public class UserInfoService {

	@Autowired
	private UserDetailsRepository userDetailsRepository;

	public UserInfo getUserInfoByUserName(String userName) {
		short enabled = 1;
		return userDetailsRepository.findByUserNameAndEnabled(userName, enabled);
	}

	public List<UserInfo> getAllActiveUserInfo() {
		return userDetailsRepository.findAllByEnabled((short) 1);
	}

	public UserInfo getUserInfoById(String userName) {
		return userDetailsRepository.findByUserName(userName);
	}

	public UserInfo addUser(UserInfo userInfo) {
		//userInfo.setPassword(new BCryptPasswordEncoder().encode(userInfo.getPassword()));
		return userDetailsRepository.save(userInfo);
	}

	public UserInfo updateUser(String userName, UserInfo userRecord) {
		UserInfo userInfo = userDetailsRepository.findByUserName(userName);
		userInfo.setUserName(userRecord.getUserName());
		//userInfo.setPassword(new BCryptPasswordEncoder().encode(userRecord.getPassword()));
		userInfo.setRole(userRecord.getRole());
		userInfo.setEnabled(userRecord.getEnabled());
		return userDetailsRepository.save(userInfo);
	}

	public void deleteUser(String userName) {
		userDetailsRepository.deleteByUserName(userName);
	}

	public UserInfo updatePassword(String userName, UserInfo userRecord) {
		UserInfo userInfo = userDetailsRepository.findByUserName(userName);
		//userInfo.setPassword(new BCryptPasswordEncoder().encode(userRecord.getPassword()));
		return userDetailsRepository.save(userInfo);
	}

	public UserInfo updateRole(String userName, UserInfo userRecord) {
		UserInfo userInfo = userDetailsRepository.findByUserName(userName);
		userInfo.setRole(userRecord.getRole());
		return userDetailsRepository.save(userInfo);
	}
}