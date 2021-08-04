package com.test.demo.model;

import com.test.demo.util.SubscriptionProtocolEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class QueueJobSubscriptionDto {
    private String endpoint;
    private SubscriptionProtocolEnum protocol;
    private String message;




}
