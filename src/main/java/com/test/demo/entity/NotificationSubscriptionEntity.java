package com.test.demo.entity;

import com.test.demo.util.NotificationType;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Setter
@Getter
@Entity
@Table(name = "notification_SUBSCRIPTION")
public class NotificationSubscriptionEntity implements Serializable {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "job_name")
    private  String jobName;

    @Column(name = "job_group")
    private  String  jobGroup;

    @Column(name = "type")
    private NotificationType type;

    @Column(name = "endpoint")
    private  String  endPoint;

    @Column(name = "protocol")
    private  String  protocol;

    @Lob
    @Column(name = "param")
    private String param;

}
