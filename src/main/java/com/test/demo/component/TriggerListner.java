package com.test.demo.component;

import com.test.demo.entity.TriggerHistoryEntity;
import com.test.demo.repository.TriggerHistoryRepository;
import org.modelmapper.ModelMapper;
import org.quartz.JobExecutionContext;
import org.quartz.Trigger;
import org.quartz.Trigger.CompletedExecutionInstruction;
import org.quartz.TriggerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TriggerListner implements TriggerListener {

    @Autowired
    private TriggerHistoryRepository triggerHistoryRepository;
    @Autowired
    private ModelMapper modelMapper;
    private static Logger logger = LoggerFactory.getLogger(TriggerListner.class);

    @Override
    public String getName() {
        return "globalTrigger";
    }

    @Override
    public void triggerFired(Trigger trigger, JobExecutionContext context) {
        logger.info("TriggerListner.triggerFired()");
    }

    @Override
    public boolean vetoJobExecution(Trigger trigger, JobExecutionContext context) {
        logger.info("TriggerListner.vetoJobExecution()");
        return false;
    }

    @Override
    public void triggerMisfired(Trigger trigger) {
        logger.info("TriggerListner.triggerMisfired()");
        String jobName = trigger.getJobKey().getName();
        logger.info("Job name: " + jobName + " is misfired");
    }

    @Override
    public void triggerComplete(Trigger trigger, JobExecutionContext context, CompletedExecutionInstruction triggerInstructionCode) {
        logger.info("TriggerListner.triggerComplete()");
        triggerHistoryRepository.save(ConvertTriggerToEntity(trigger));
    }

    private TriggerHistoryEntity ConvertTriggerToEntity(Trigger trigger) {
        TriggerHistoryEntity triggerHistoryEntity = new TriggerHistoryEntity();
        triggerHistoryEntity.setTrigger_name(trigger.getKey().getName());
        triggerHistoryEntity.setTrigger_group(trigger.getKey().getGroup());
        triggerHistoryEntity.setJob_name(trigger.getJobKey().getName());
        triggerHistoryEntity.setJob_group(trigger.getJobKey().getGroup());
        triggerHistoryEntity.setDescription(trigger.getDescription());
        triggerHistoryEntity.setNextFireTime(trigger.getNextFireTime());
        triggerHistoryEntity.setPrevFireTime(trigger.getPreviousFireTime());
        triggerHistoryEntity.setPriority(trigger.getPriority());
        triggerHistoryEntity.setStartTime(trigger.getStartTime());
        triggerHistoryEntity.setEndTime(trigger.getEndTime());
        triggerHistoryEntity.setMisfireInstruction(trigger.getMisfireInstruction());
        triggerHistoryEntity.setJob_data(trigger.getJobDataMap().toString());
        return triggerHistoryEntity;
    }
}
