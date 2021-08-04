package com.test.demo.entity;

import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;
import java.io.Serializable;

@Setter
@Getter
@Entity
@Table(name = "job_SUBSCRIPTION")
public class JobSubscriptionEntity implements Serializable {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "job_name")
    private  String jobName;

    @Column(name = "job_group")
    private  String  jobGroup;

    @Column(name = "endpoint")
    private  String  endPoint;

    @Column(name = "protocol")
    private  String  protocol;

    @Lob
    @Column(name = "param")
    private String param;




}
