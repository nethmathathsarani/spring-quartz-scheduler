package com.test.demo.model;

import com.test.demo.util.SubscriptionProtocolEnum;
import lombok.*;

import java.util.List;

@ToString
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JobSubscriptionDto {
    private List<ParameterDto> parameters;
    private String endpoint;
    private SubscriptionProtocolEnum protocol;

}
