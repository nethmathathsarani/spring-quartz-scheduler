package com.test.demo.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;

@Setter
@Getter
@Entity
@Table(name = "trigger_HISTORY")
public class TriggerHistoryEntity implements Serializable {


    @Id
    private String trigger_name;
    private String trigger_group;
    private String job_name;
    private String job_group;
    private String description;
    private Date nextFireTime;
    private Date prevFireTime;
    private int priority;
    private Date startTime;
    private Date endTime;
    private int misfireInstruction;
    @Lob
    private String job_data ;

}
