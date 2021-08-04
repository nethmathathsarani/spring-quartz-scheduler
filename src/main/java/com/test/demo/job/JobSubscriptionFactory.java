package com.test.demo.job;

import com.test.demo.service.*;
import com.test.demo.util.SubscriptionProtocolEnum;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JobSubscriptionFactory {

    @Autowired
    private EmailService emailService;
    @Autowired
    private WebService webService;
    @Autowired
    private QueueService queueService;
    @Autowired
    private SmsService smsService;


    public SubscriptionService getSubscription(String subscriptionType){
        if(subscriptionType.equalsIgnoreCase(null)){
            return null;
        }
        if(subscriptionType.equalsIgnoreCase(SubscriptionProtocolEnum.HTTP.name()) || subscriptionType.equalsIgnoreCase(SubscriptionProtocolEnum.HTTPS.name()) ){
            return webService;
        } else if(subscriptionType.equalsIgnoreCase(SubscriptionProtocolEnum.EMAIL.name())){
            return emailService;
        }else if(subscriptionType.equalsIgnoreCase(SubscriptionProtocolEnum.QUEUE.name())){
            return queueService;
        }else if(subscriptionType.equalsIgnoreCase(SubscriptionProtocolEnum.SMS.name())){
            return smsService;
        }
        return  null;
    }
}
