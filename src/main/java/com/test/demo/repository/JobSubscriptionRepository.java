package com.test.demo.repository;

import com.test.demo.entity.JobSubscriptionEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
@Transactional
public interface JobSubscriptionRepository extends CrudRepository<JobSubscriptionEntity,String>{
	List<JobSubscriptionEntity> findByJobName(String jobName);
	//JobSubscription findByUserName(String userName);
}
