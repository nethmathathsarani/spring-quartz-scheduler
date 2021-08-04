package com.test.demo.service;

import com.test.demo.constant.JobStatus;
import com.test.demo.model.JobDto;
import com.test.demo.model.JobResponseDto;
import com.test.demo.exception.JobServiceException;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

public interface JobService {

    ResponseEntity<JobResponseDto> scheduleSimpleJob(JobDto jobDto, Class<? extends QuartzJobBean> jobClass) throws ParseException, JobServiceException, JsonProcessingException;

    ResponseEntity<JobResponseDto> scheduleCronJob(JobDto jobName, Class<? extends QuartzJobBean> jobClass) throws JobServiceException, JsonProcessingException;

    ResponseEntity<JobDto> updateOneTimeJob(JobDto jobDto) throws JobServiceException;

    ResponseEntity<JobDto> updateCronJob(JobDto jobDto) throws JobServiceException;

    boolean unScheduleJob(String jobName);

    boolean deleteJob(String jobName);

    ResponseEntity<JobResponseDto> pauseJob(String jobName) throws JobServiceException;

    ResponseEntity<JobResponseDto> resumeJob(String jobName) throws JobServiceException;

    Map<String, Object> startJobNow(String jobName) throws JobServiceException;

    boolean isJobRunning(String jobName);

    List<Map<String, Object>> getAllJobs();

    boolean isJobWithNamePresent(String jobName);

    JobStatus getJobState(String jobName);

    Map<String, Object> stopJob(String jobName) throws JobServiceException;
	/*
	List<Map<String, Object>> getJobsByExternalRef(int externalRefId,String externalRefType);
*/

}
