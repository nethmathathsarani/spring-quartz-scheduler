package com.test.demo.component;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class JobsListener implements JobListener {

    private static Logger logger = LoggerFactory.getLogger(JobsListener.class);


    @Override
    public String getName() {
        return "globalJob";
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext context) {
        logger.info("JobsListener.jobToBeExecuted()");
    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {
        logger.info("JobsListener.jobExecutionVetoed()");
    }

    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        logger.info("JobsListener.jobWasExecuted()");
    }

}
