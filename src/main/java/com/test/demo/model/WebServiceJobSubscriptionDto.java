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
public class WebServiceJobSubscriptionDto {
    private List<ParameterDto> parameters;
    private String endpoint;
    private SubscriptionProtocolEnum protocol;
}
