package com.test.demo.service;

import com.test.demo.exception.JobSubscriptionException;

public interface SubscriptionService<T> {

    void callService(T request) throws JobSubscriptionException;
}
