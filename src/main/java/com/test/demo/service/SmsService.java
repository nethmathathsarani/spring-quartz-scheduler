package com.test.demo.service;

import com.test.demo.exception.JobSubscriptionException;
import org.springframework.stereotype.Service;

@Service
public class SmsService implements SubscriptionService{


    @Override
    public void callService(Object request) throws JobSubscriptionException {

    }
}
