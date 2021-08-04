package com.test.demo.service;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

public interface QueueServiceSource {

    @Output("output1")
    MessageChannel pushMessageToRabbit();
    @Output("output2")
    MessageChannel pushMessageToKafka();



}
