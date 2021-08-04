package com.test.demo.repository;

import com.test.demo.entity.NotificationSubscriptionEntity;
import com.test.demo.util.NotificationType;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
@Transactional
public interface NotificationSubscriptionRepository extends CrudRepository<NotificationSubscriptionEntity,String> {

 List<NotificationSubscriptionEntity> findByJobNameAndType(String jobName, NotificationType type);
}
