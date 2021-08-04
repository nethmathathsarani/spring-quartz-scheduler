package com.test.demo.job;

import ch.qos.logback.classic.Logger;
import com.test.demo.controller.JobController;
import com.test.demo.entity.JobSubscriptionEntity;
import com.test.demo.entity.NotificationSubscriptionEntity;
import com.test.demo.exception.JobSubscriptionException;
import com.test.demo.model.JobSubscriptionDto;
import com.test.demo.model.ParameterDto;
import com.test.demo.repository.JobSubscriptionRepository;
import com.test.demo.repository.NotificationSubscriptionRepository;
import com.test.demo.service.EmailService;
import com.test.demo.service.SubscriptionService;
import com.test.demo.util.NotificationType;
import com.test.demo.util.SubscriptionProtocolEnum;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.modelmapper.ModelMapper;
import org.quartz.*;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import java.io.IOException;
import java.util.List;


public class CronJob extends QuartzJobBean implements Job {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private JobSubscriptionFactory jobSubscriptionFactory;
    @Autowired
    private EmailService emailService;
    @Autowired
    private JobSubscriptionRepository jobSubscriptionRepository;
    @Autowired
    private NotificationSubscriptionRepository NotificationSubscriptionRepository;
    private static org.slf4j.Logger logger = LoggerFactory.getLogger(CronJob.class);


    @SneakyThrows
    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        JobKey key = jobExecutionContext.getJobDetail().getKey();
        logger.info("[NBREQ] - Cron Job started with key " + key.getName());
        List<JobSubscriptionEntity> subObjectList = jobSubscriptionRepository.findByJobName(key.getName());
        subObjectList.forEach(subscriptionEntity -> {
            try {
                SubscriptionService subscriptionService = jobSubscriptionFactory.getSubscription(subscriptionEntity.getProtocol());
                subscriptionService.callService(convertSubscriptionEntityToDto(subscriptionEntity));
            } catch (JobSubscriptionException e) {
                List<NotificationSubscriptionEntity> notificationList = NotificationSubscriptionRepository.findByJobNameAndType(key.getName(), NotificationType.FAILED);
                notificationList.forEach(notification -> {
                    SubscriptionService notificationService = jobSubscriptionFactory.getSubscription(notification.getProtocol());
                    try {
                        notificationService.callService(convertSubscriptionEntityToDto(notification));
                    } catch (JobSubscriptionException jobSubscriptionException) {
                        logger.error("Error occurred");
                    }
                });
            }
        });
    }



    private JobSubscriptionDto convertSubscriptionEntityToDto(JobSubscriptionEntity entity) {
        JobSubscriptionDto JobSubscriptionDto=new JobSubscriptionDto();
        JobSubscriptionDto.setEndpoint(entity.getEndPoint());
        JobSubscriptionDto.setProtocol(SubscriptionProtocolEnum.valueOf(entity.getProtocol()));
        List<ParameterDto> paramDtoList= null;
        try {
            paramDtoList = objectMapper.readValue(entity.getParam(), new TypeReference<List<ParameterDto>>(){});
        } catch (IOException e) {
            logger.error("Error occurred");
        }
        JobSubscriptionDto.setParameters(paramDtoList);
        return JobSubscriptionDto;
    }

    private JobSubscriptionDto convertSubscriptionEntityToDto(NotificationSubscriptionEntity entity) {
        JobSubscriptionDto notificationSubscriptionDto=new JobSubscriptionDto();
        notificationSubscriptionDto.setEndpoint(entity.getEndPoint());
        notificationSubscriptionDto.setProtocol(SubscriptionProtocolEnum.valueOf(entity.getProtocol()));
        List<ParameterDto> paramDtoList= null;
        try {
            paramDtoList = objectMapper.readValue(entity.getParam(), new TypeReference<List<ParameterDto>>(){});
        } catch (IOException e) {
            logger.error("Error occurred");
        }
        notificationSubscriptionDto.setParameters(paramDtoList);
        return notificationSubscriptionDto;
    }

}