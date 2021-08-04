package com.test.demo.model;


import com.test.demo.constant.JobStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JobResponseDto implements Serializable {
    private String jobName;
    private String appName;
    private JobStatus jobStatus;
    private String message;
}
