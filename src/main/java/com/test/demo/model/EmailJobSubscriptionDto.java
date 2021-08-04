package com.test.demo.model;

import com.test.demo.util.SubscriptionProtocolEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EmailJobSubscriptionDto {
    private SubscriptionProtocolEnum protocol;
    private String emailSubject;
    private String emailBody;
    private List<String> endpoint;
}
