package com.test.demo.model;

import com.test.demo.util.FrequencyType;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FrequencyDto {
    @NotNull(message = "{frequencyType.notnull}")
    private FrequencyType type;
    @NotNull(message = "{frequencyValue.notnull}")
    @ApiModelProperty(
            value = "frequency in no of milliseconds or cron expression",
            name = "frequency value",
            dataType = "String")
    private String  value;
}
