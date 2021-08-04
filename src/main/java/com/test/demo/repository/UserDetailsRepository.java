package com.test.demo.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.test.demo.model.UserInfo;

@Repository
@Transactional
public interface UserDetailsRepository extends CrudRepository<UserInfo, String> {
	public UserInfo findByUserNameAndEnabled(String userName, short enabled);
	public UserInfo findByUserName(String userName);
	public List<UserInfo> findAllByEnabled(short enabled);

//
//	@Override
//	public UserInfo save(UserInfo userInfo);

	public void deleteByUserName(String userName);
}
