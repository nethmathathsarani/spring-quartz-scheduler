package com.test.demo.repository;

import com.test.demo.entity.TriggerHistoryEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
@Transactional
public interface TriggerHistoryRepository extends CrudRepository<TriggerHistoryEntity,String> {
}
