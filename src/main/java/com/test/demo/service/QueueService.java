package com.test.demo.service;

import com.test.demo.exception.JobSubscriptionException;
import com.test.demo.model.JobSubscriptionDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.binding.BinderAwareChannelResolver;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.http.MediaType;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@EnableBinding(QueueServiceSource.class)
@Service
public class QueueService implements SubscriptionService {
    @Autowired
    private StreamBridge streamBridge;
    @Autowired
    private QueueServiceSource queueServiceSource;
    @Autowired
    private EmailService emailService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private BinderAwareChannelResolver resolver;
    private static Logger logger = LoggerFactory.getLogger(QueueService.class);

    @Override
    public void callService(Object request) throws JobSubscriptionException {
        JobSubscriptionDto requestDto=(JobSubscriptionDto)request;
   /* if(requestDto.getParameters().get(1).getValue().equals(MessageBrokerType.RABBIT.name())){
        logger.info("Pushing message to rabbit exchange");

        queueServiceSource.pushMessageToRabbit().send(MessageBuilder.withPayload(requestDto.getParameters().get(0).getValue()).build());
    }else if(requestDto.getParameters().get(1).getValue().equals(MessageBrokerType.KAFKA.name())){
        logger.info("Pushing message to kafka topic");

        queueServiceSource.pushMessageToKafka().send(MessageBuilder.withPayload(requestDto.getParameters().get(0).getValue()).build());

    }*/
       // queueServiceSource.pushMessageToKafka().send(MessageBuilder.withPayload(requestDto.getParameters().get(0).getValue()).build());

        //queueServiceSource.pushMessage().send(MessageBuilder.withPayload(requestDto.getParameters().get(0).getValue()).build());

    try {

    resolver.resolveDestination(requestDto.getEndpoint()).send(MessageBuilder.createMessage(requestDto.getParameters().get(0).getValue(),
            new MessageHeaders(Collections.singletonMap(MessageHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))));
    }catch (MessageDeliveryException me){
        logger.error("No subscribers for channel",me.getMessage());
    }
    }






}
