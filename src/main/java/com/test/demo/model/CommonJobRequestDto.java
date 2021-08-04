package com.test.demo.model;

import java.io.Serializable;

public class CommonJobRequestDto implements Serializable{
	private String jobName;

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}
	
	
}
