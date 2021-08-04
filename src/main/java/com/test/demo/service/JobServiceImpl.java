package com.test.demo.service;

import ch.qos.logback.classic.Logger;
import com.test.demo.configuration.PersistableCronTriggerFactoryBean;
import com.test.demo.constant.JobStatus;
import com.test.demo.entity.JobSubscriptionEntity;
import com.test.demo.entity.NotificationSubscriptionEntity;
import com.test.demo.model.JobDto;
import com.test.demo.model.JobResponseDto;
import com.test.demo.exception.JobServiceException;
import com.test.demo.model.NotificationDto;
import com.test.demo.repository.JobSubscriptionRepository;
import com.test.demo.repository.NotificationSubscriptionRepository;
import com.test.demo.repository.UserDetailsRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.quartz.*;
import org.quartz.Trigger.TriggerState;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.quartz.impl.triggers.SimpleTriggerImpl;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.quartz.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


@Service
public class JobServiceImpl implements JobService {

    @Autowired
    @Lazy
    SchedulerFactoryBean schedulerFactoryBean;

    @Autowired
    private ApplicationContext context;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private EmailService emailService;
    @Autowired
    private UserDetailsRepository userDatailsRepository;
    @Autowired
    private JobSubscriptionRepository jobSubscriptionRepository;
    @Autowired
    private NotificationSubscriptionRepository notificationSubscriptionRepository;
    @Value("${app.scheduler.dateformat}")
    private String dateFormat;
    private static final String repeatIf = "REPEAT_INDEFINITELY";

    private static org.slf4j.Logger logger = LoggerFactory.getLogger(JobServiceImpl.class);

    /**
     * Schedule a job by jobName at given date.
     */
    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public ResponseEntity<JobResponseDto> scheduleSimpleJob(JobDto jobDto, Class<? extends QuartzJobBean> jobClass) throws JobServiceException, JsonProcessingException {
        String jobKey = jobDto.getName();
        if (jobDto.getName() == null || jobDto.getName().trim().equals("")) {
            return new ResponseEntity<>(new JobResponseDto(jobKey, jobDto.getApplication(), JobStatus.NONE, "job name can't be null"), HttpStatus.BAD_REQUEST);
        }
        try {
            JobDetail jobDetail = createJob(jobClass, true, context, jobDto);
            SimpleDateFormat dateFormat = new SimpleDateFormat(this.dateFormat);
            logger.info("[NBREQ] - creating trigger for key : {}, at date : {} ", jobKey, jobDto.getStart());
            Trigger cronTriggerBean = createSingleTrigger(jobKey, dateFormat.parse(jobDto.getStart()), dateFormat.parse(jobDto.getEnd()), Long.parseLong(jobDto.getFrequency().getValue()), getMisfireInstructionForSimpleJobs(jobDto));
            Scheduler scheduler = schedulerFactoryBean.getScheduler();
            scheduler.scheduleJob(jobDetail, cronTriggerBean);
            logger.info("[NBREQ] - Job with key jobKey : {}, and group : {} scheduled successfully", jobKey, jobDto.getApplication());
            return new ResponseEntity<>(new JobResponseDto(jobKey, jobDto.getApplication(), JobStatus.SCHEDULED, "Job scheduled"), HttpStatus.OK);
        } catch (SchedulerException e) {
            logger.error("[NBRES] - SchedulerException while scheduling job with key : {}, message : {}", jobKey, e.getMessage());
            throw new JobServiceException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (ParseException pe) {
            logger.error("[NBRES] - Date parse exception while scheduling job with key : {},message : {} ", jobKey, pe.getMessage());
            throw new JobServiceException(pe.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private int getMisfireInstructionForSimpleJobs(JobDto jobDto) {
        int misFireInstruction = SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_REMAINING_COUNT;
        if (!jobDto.getMisfires().isIgnore()) {
            misFireInstruction = jobDto.getMisfires().getInstruction().getValue();
        }
        return misFireInstruction;
    }

    private int getMisfireInstructionForCronJobs(JobDto jobDto) {
        int misFireInstruction = CronTrigger.MISFIRE_INSTRUCTION_DO_NOTHING;
        if (!jobDto.getMisfires().isIgnore()) {
            misFireInstruction = jobDto.getMisfires().getInstruction().getValue();
        }
        return misFireInstruction;
    }

   /* private String getUserDetailsFromToken(String token) {
        String tokenVal = token.split(" ")[1];
        String[] chunks = tokenVal.split("\\.");

        Base64.Decoder decoder = Base64.getDecoder();
        String header = new String(decoder.decode(chunks[0]));
        String payload = new String(decoder.decode(chunks[1]));
        JSONObject jsonObject = new JSONObject(payload);

        return userDatailsRepository.findByUserName((String) jsonObject.get("user_name")).getEmail();

    }*/

    /*
     * Schedule a job by jobName at given date.
     */
    @Override
    public ResponseEntity<JobResponseDto> scheduleCronJob(JobDto jobDto, Class<? extends QuartzJobBean> jobClass) throws JobServiceException, JsonProcessingException {
        logger.info("Request received to scheduleJob: {}", jobDto.getName());
        String jobKey = jobDto.getName();
        if (jobDto.getName() == null || jobDto.getName().trim().equals("")) {
            return new ResponseEntity<>(new JobResponseDto(jobKey, jobDto.getApplication(), JobStatus.NONE, "job name can't be null"), HttpStatus.BAD_REQUEST);
        }
        if (!isJobWithNamePresent(jobDto.getName())) {
            try {
                JobDetail jobDetail = createJob(jobClass, true, context, jobDto);
                SimpleDateFormat dateFormat = new SimpleDateFormat(this.dateFormat);
                logger.info("[NBREQ] - creating trigger for key : {}, at date : {} ", jobKey, jobDto.getStart());
                Trigger cronTriggerBean = createCronTrigger(jobKey, dateFormat.parse(jobDto.getStart()), dateFormat.parse(jobDto.getEnd()), jobDto.getFrequency().getValue(), getMisfireInstructionForCronJobs(jobDto));
                Scheduler scheduler = schedulerFactoryBean.getScheduler();
                scheduler.scheduleJob(jobDetail, cronTriggerBean);
                logger.info("[NBREQ] - Job with key jobKey : {}, and group : {} scheduled successfully", jobKey, jobDto.getApplication());
                return new ResponseEntity<>(new JobResponseDto(jobKey, jobDto.getApplication(), JobStatus.SCHEDULED, "Job scheduled"), HttpStatus.OK);
            } catch (SchedulerException e) {
                logger.error("[NBRES] - SchedulerException while scheduling job with key : {}, message : {}", jobKey, e.getMessage());
                throw new JobServiceException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            } catch (ParseException pe) {
                logger.error("[NBRES] - Date parse exception while scheduling job with key : {},message : {} ", jobKey, pe.getMessage());
                throw new JobServiceException(pe.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>(new JobResponseDto(jobKey, jobDto.getApplication(), JobStatus.NONE, "job with same name exist"), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Update one time scheduled job.
     */
    @Override
    public ResponseEntity<JobDto> updateOneTimeJob(JobDto jobDto) throws JobServiceException {
        logger.info("Request received for updating one time job: {}", jobDto.getName());

        String jobKey = jobDto.getName();
        int misFireInstruction = getMisfireInstructionForSimpleJobs(jobDto);
        logger.info("Request received for updating one time job: {}", jobDto.getName());
        try {
            SimpleDateFormat formatter1 = new SimpleDateFormat(dateFormat);
            Date startTimiTri = formatter1.parse(jobDto.getStart());
            Date endTimeTri = formatter1.parse(jobDto.getEnd());
            Trigger newTrigger = createSingleTrigger(jobKey, startTimiTri, endTimeTri, Long.parseLong(jobDto.getFrequency().getValue()), misFireInstruction);
            Date dt = schedulerFactoryBean.getScheduler().rescheduleJob(TriggerKey.triggerKey(jobKey), newTrigger);
            logger.info("Trigger associated with jobKey : {} rescheduled successfully for date : {}", dt);
            return new ResponseEntity<>(jobDto, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("SchedulerException while scheduling job with key : {} , message : {} ", jobKey, e.getMessage());
            throw new JobServiceException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Update scheduled cron job.
     */
    @Override
    public ResponseEntity<JobDto> updateCronJob(JobDto jobDto) throws JobServiceException {
        logger.info("Request received for updating cron job: {}", jobDto.getName());
        String jobKey = jobDto.getName();
        int misFireInstruction = getMisfireInstructionForCronJobs(jobDto);
        logger.info("Parameters received for updating cron jobKey: {}", jobKey);

        try {
            SimpleDateFormat formatter1 = new SimpleDateFormat(dateFormat);
            Trigger newTrigger = createCronTrigger(jobKey, formatter1.parse(jobDto.getStart()), formatter1.parse(jobDto.getEnd()), jobDto.getFrequency().getValue(), misFireInstruction);
            Date dt = schedulerFactoryBean.getScheduler().rescheduleJob(TriggerKey.triggerKey(jobKey), newTrigger);
            logger.info("Trigger associated with jobKey : {} rescheduled successfully for date : {}", jobKey, dt);
            return new ResponseEntity<>(jobDto, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("SchedulerException while scheduling job with key : {} , message : {} ", jobKey, e.getMessage());
            throw new JobServiceException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Remove the indicated Trigger from the scheduler.
     * If the related job does not have any other triggers, and the job is not durable, then the job will also be deleted.
     */
    @Override
    public boolean unScheduleJob(String jobName) {
        logger.info("Request received for Unscheduleding job: {}", jobName);
        String jobKey = jobName;
        TriggerKey triggerKey = new TriggerKey(jobKey);
        try {
            boolean status = schedulerFactoryBean.getScheduler().unscheduleJob(triggerKey);
            logger.info("Trigger associated with jobKey: {} unscheduled with status : {}", jobName, status);
            return status;
        } catch (SchedulerException e) {
            logger.error("SchedulerException while scheduling job with key : {} , message : {} ", jobKey, e.getMessage());
            return false;
        }
    }

    private String getGroupKey(String jobKeyRequested) {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        try {
            for (String groupName : scheduler.getJobGroupNames()) {
                for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
                    if (jobKey.getName().equals(jobKeyRequested)) {
                        return jobKey.getGroup();
                    }
                }
            }
        } catch (SchedulerException se) {
            logger.error("SchedulerException while getting job key name ");
        }
        return "";
    }

    /**
     * Delete the identified Job from the Scheduler - and any associated Triggers.
     */
    @Override
    public boolean deleteJob(String jobName) {
        logger.info("Request received for deleting job : {}", jobName);
        String jobKey = jobName;
        String groupKey = getGroupKey(jobName);
        JobKey jkey = new JobKey(jobKey, groupKey);

        try {
            boolean status = schedulerFactoryBean.getScheduler().deleteJob(jkey);
            logger.info("Job with jobKey : {} deleted", jobName);
            return status;
        } catch (SchedulerException e) {
            logger.error("SchedulerException while scheduling job with key : {} , message : {} ", jobKey, e.getMessage());
            return false;
        }
    }


    /**
     * Pause a job
     */
    @Override
    public ResponseEntity<JobResponseDto> pauseJob(String jobName) throws JobServiceException {
        String jobKey = jobName;
        String groupKey = getGroupKey(jobName);
        JobKey jkey = new JobKey(jobKey, groupKey);
        if (isJobWithNamePresent(jobName)) {

            boolean isJobRunning = isJobRunning(jobName);
            if (isJobRunning) {
                logger.info("[NBREQ] - Request received for pausing jobKey : {}, groupKey : {} ", jobKey, groupKey);
                try {
                    schedulerFactoryBean.getScheduler().pauseJob(jkey);
                    logger.info("[NBREQ] - Job with key jobKey : {}, and group : {} paused successfully", jobKey, groupKey);
                    return new ResponseEntity<>(new JobResponseDto(jobName, groupKey, JobStatus.PAUSED, "Job paused"), HttpStatus.OK);
                } catch (SchedulerException e) {
                    logger.error("[NBRES] - SchedulerException while pausing job with key : {}, message : {}", jobKey, e.getMessage());
                    throw new JobServiceException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
                }
            } else {
                return new ResponseEntity<>(new JobResponseDto(jobKey, groupKey, JobStatus.NONE, "Job is not running"), HttpStatus.BAD_REQUEST);
            }
        } else {
            return new ResponseEntity<>(new JobResponseDto(jobKey, groupKey, JobStatus.NONE, "job not found"), HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Resume paused job
     */
    @Override
    public ResponseEntity<JobResponseDto> resumeJob(String jobName) throws JobServiceException {
        String jobKey = jobName;
        String groupKey = getGroupKey(jobName);

        if (isJobWithNamePresent(jobName)) {
            if (getJobState(jobName).equals(JobStatus.PAUSED)) {

                JobKey jKey = new JobKey(jobKey, groupKey);
                logger.info("[NBREQ] - Request received for resuming jobKey : {}, groupKey : {} ", jobKey, groupKey);
                try {
                    schedulerFactoryBean.getScheduler().resumeJob(jKey);
                    logger.info("[NBREQ] - Job with key jobKey : {}, and group : {} resumed successfully", jobKey, groupKey);
                    return new ResponseEntity<>(new JobResponseDto(jobName, groupKey, JobStatus.SCHEDULED, "Job resumed"), HttpStatus.OK);
                } catch (SchedulerException e) {
                    logger.error("[NBRES] - SchedulerException while resuming job with key : {}, message : {}", jobKey, e.getMessage());
                    throw new JobServiceException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
                }
            } else {
                return new ResponseEntity<>(new JobResponseDto(jobKey, groupKey, JobStatus.NONE, "Job is not in paused state"), HttpStatus.BAD_REQUEST);
            }
        } else {
            return new ResponseEntity<>(new JobResponseDto(jobKey, groupKey, JobStatus.NONE, "job not found"), HttpStatus.NOT_FOUND);
        }
    }


    /**
     * Start a job now
     */
    @Override
    public Map<String, Object> startJobNow(String jobName) throws JobServiceException {
        logger.info("Request received for starting job now : {}", jobName);

        String jobKey = jobName;
        String groupKey = getGroupKey(jobName);
        Map<String, Object> map = new HashMap<>();
        JobKey jKey = new JobKey(jobKey, groupKey);


        try {
            schedulerFactoryBean.getScheduler().triggerJob(jKey);
            logger.info("Job started successfully : {}", jobName);
            return map;
        } catch (SchedulerException e) {
            logger.error("SchedulerException while scheduling job with key : {} , message : {} ", jobKey, e.getMessage());
            throw new JobServiceException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Check if job is already running
     */
    @Override
    public boolean isJobRunning(String jobName) {
        String jobKey = jobName;
        String groupKey = getGroupKey(jobName);

        try {
            List<JobExecutionContext> currentJobs = schedulerFactoryBean.getScheduler().getCurrentlyExecutingJobs();
            if (currentJobs != null) {
                for (JobExecutionContext jobCtx : currentJobs) {
                    String jobNameDB = jobCtx.getJobDetail().getKey().getName();
                    String groupNameDB = jobCtx.getJobDetail().getKey().getGroup();
                    if (jobKey.equalsIgnoreCase(jobNameDB) && groupKey.equalsIgnoreCase(groupNameDB)) {
                        return true;
                    }
                }
            }
        } catch (SchedulerException e) {
            logger.error("SchedulerException while scheduling job with key : {} , message : {} ", jobKey, e.getMessage());
            return false;
        }
        return false;
    }

    /**
     * Get all jobs
     */

    @Override
    public List<Map<String, Object>> getAllJobs() {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        try {
            Scheduler scheduler = schedulerFactoryBean.getScheduler();

            for (String groupName : scheduler.getJobGroupNames()) {
                for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {

                    String jobName = jobKey.getName();
                    String jobGroup = jobKey.getGroup();


                    //get job's trigger
                    List<Trigger> triggers = (List<Trigger>) scheduler.getTriggersOfJob(jobKey);
                    Date scheduleTime = triggers.get(0).getStartTime();
                    Date nextFireTime = triggers.get(0).getNextFireTime();
                    Date lastFiredTime = triggers.get(0).getPreviousFireTime();

                    Map<String, Object> map = new HashMap<String, Object>();
                    Map<String, Object> endPointmap = new HashMap<String, Object>();

                    map.put("jobName", jobName);
                    map.put("groupName", jobGroup);
                    map.put("scheduleTime", scheduleTime);
                    map.put("lastFiredTime", lastFiredTime);
                    map.put("nextFireTime", nextFireTime);
                    map.put("jobSubscription", endPointmap);

                    if (isJobRunning(jobName)) {
                        map.put("jobStatus", "RUNNING");
                    } else {
                        String jobState = getJobState(jobName).name();
                        map.put("jobStatus", jobState);
                    }
                    list.add(map);
                }
            }
        } catch (SchedulerException e) {
            logger.error("SchedulerException while fetching all jobs. error message", e.getMessage());
        }
        return list;
    }

    /**
     * Check job exist with given name
     */
    @Override
    public boolean isJobWithNamePresent(String jobName) {
        try {
            String groupKey = getGroupKey(jobName);
            JobKey jobKey = new JobKey(jobName, groupKey);
            Scheduler scheduler = schedulerFactoryBean.getScheduler();
            if (scheduler.checkExists(jobKey)) {
                return true;
            }
        } catch (SchedulerException e) {
            logger.error("SchedulerException while checking job name");
        }
        return false;
    }

    /**
     * Get the current state of job
     */
    public JobStatus getJobState(String jobName) {
        logger.info("Request received for get job state : {}", jobName);

        try {
            String groupKey = getGroupKey(jobName);
            JobKey jobKey = new JobKey(jobName, groupKey);
            Scheduler scheduler = schedulerFactoryBean.getScheduler();
            JobDetail jobDetail = scheduler.getJobDetail(jobKey);

            List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobDetail.getKey());
            if (triggers != null && triggers.size() > 0) {
                for (Trigger trigger : triggers) {
                    TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());
                    if (TriggerState.PAUSED.equals(triggerState)) {
                        return JobStatus.PAUSED;
                    } else if (TriggerState.BLOCKED.equals(triggerState)) {
                        return JobStatus.BLOCKED;
                    } else if (TriggerState.COMPLETE.equals(triggerState)) {
                        return JobStatus.COMPLETE;
                    } else if (TriggerState.ERROR.equals(triggerState)) {
                        return JobStatus.ERROR;
                    } else if (TriggerState.NONE.equals(triggerState)) {
                        return JobStatus.NONE;
                    } else if (TriggerState.NORMAL.equals(triggerState)) {
                        return JobStatus.SCHEDULED;
                    }
                }
            }
        } catch (SchedulerException e) {
            logger.error("SchedulerException while checking job with name and group exist:{}", e.getMessage());
        }
        return null;
    }

    /**
     * Stop a job
     */
    @Override
    public Map<String, Object> stopJob(String jobName) throws JobServiceException {
        logger.info("Request received for stop job : {}", jobName);

        try {
            String jobKey = jobName;
            String groupKey = getGroupKey(jobName);

            Scheduler scheduler = schedulerFactoryBean.getScheduler();
            JobKey jkey = new JobKey(jobKey, groupKey);

            Map<String, Object> map = new HashMap<String, Object>();
            Map<String, Object> endPointmap = new HashMap<String, Object>();
            map.put("jobName", jobName);
            map.put("jobSubscription", endPointmap);
            map.put("jobStatus", JobStatus.BLOCKED);
            scheduler.interrupt(jkey);
            return map;

        } catch (SchedulerException e) {
            logger.error("SchedulerException while checking job with name and group exist:{}", e.getMessage());
            throw new JobServiceException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }


    private JobDetail createJob(Class<? extends QuartzJobBean> jobClass, boolean isDurable,
                                ApplicationContext context, JobDto jobDto) throws JsonProcessingException {
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(jobClass);
        factoryBean.setDurability(isDurable);
        factoryBean.setApplicationContext(context);
        factoryBean.setName(jobDto.getName());
        factoryBean.setGroup(jobDto.getApplication());

        saveJobSubscriptionDetails(jobDto);
        saveNotificationSubscriptionDetails(jobDto);
        JobDataMap jobDataMap = new JobDataMap();
        //jobDataMap.put("subscriptionDetails", objectMapper.writeValueAsString(jobDto.getSubscriptions()));
        //jobDataMap.put("userDetails",getUserDetailsFromToken(token));
        //jobDataMap.put("notificationDetails",objectMapper.writeValueAsString(jobDto.getNotifications()));
        //factoryBean.setJobDataMap(jobDataMap);

        factoryBean.afterPropertiesSet();
        return factoryBean.getObject();
    }

    private void saveJobSubscriptionDetails(JobDto jobDto){
        jobDto.getSubscriptions().stream().forEach(subscription -> {

            JobSubscriptionEntity entity=new JobSubscriptionEntity();
            entity.setId(UUID.randomUUID().toString());
            entity.setJobName(jobDto.getName());
            entity.setJobGroup(jobDto.getApplication());
            entity.setEndPoint(subscription.getEndpoint());
            entity.setProtocol(subscription.getProtocol().name());
            try {
                entity.setParam(objectMapper.writeValueAsString((subscription).getParameters()));
            } catch (JsonProcessingException e) {
                logger.error("Error occurred");
            }
            jobSubscriptionRepository.save(entity);

        });
    }

    private void saveNotificationSubscriptionDetails(JobDto jobDto){

        jobDto.getNotifications().stream().forEach(notificationObject -> {

            NotificationDto notification=notificationObject;
            notification.getSubscriptions().stream().forEach(subscription -> {
                NotificationSubscriptionEntity entity=new NotificationSubscriptionEntity();
                entity.setId(UUID.randomUUID().toString());
                entity.setJobName(jobDto.getName());
                entity.setJobGroup(jobDto.getApplication());
                entity.setEndPoint(subscription.getEndpoint());
                entity.setProtocol(subscription.getProtocol().name());
                entity.setType(notification.getType());
                try {
                    entity.setParam(objectMapper.writeValueAsString(subscription.getParameters()));
                } catch (JsonProcessingException e) {
                    logger.error("Error occurred");
                }
                notificationSubscriptionRepository.save(entity);
            });

        });
    }


    /**
     * Create cron trigger.
     *
     * @param triggerName        Trigger name.
     * @param startTime          Trigger start time.
     * @param cronExpression     Cron expression.
     * @param misFireInstruction Misfire instruction (what to do in case of misfire happens).
     * @return Trigger
     */
    private Trigger createCronTrigger(String triggerName, Date startTime, Date endTime, String cronExpression, int misFireInstruction) throws ParseException {
        PersistableCronTriggerFactoryBean factoryBean = new PersistableCronTriggerFactoryBean();
        CronTriggerFactoryBean fb = new CronTriggerFactoryBean();
        CronTriggerImpl ci = new CronTriggerImpl();
        ci.setName(triggerName);
        ci.setStartTime(startTime);
        ci.setCronExpression(cronExpression);
        ci.setMisfireInstruction(misFireInstruction);
        ci.setEndTime(endTime);
        return ci;
    }

    /**
     * Create a Single trigger.
     *
     * @param triggerName        Trigger name.
     * @param startTime          Trigger start time.
     * @param misFireInstruction Misfire instruction (what to do in case of misfire happens).
     * @return Trigger
     */
    private Trigger createSingleTrigger(String triggerName, Date startTime, Date endTime, long repeatInterval, int misFireInstruction) {
        SimpleTriggerImpl si = new SimpleTriggerImpl();
        si.setName(triggerName);
        si.setStartTime(startTime);
        si.setRepeatInterval(repeatInterval);
        si.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);
        si.setMisfireInstruction(misFireInstruction);
        si.setEndTime(endTime);
        return si;

        /*SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        factoryBean.setName(triggerName);
        factoryBean.setStartTime(startTime);
        factoryBean.setMisfireInstruction(misFireInstruction);
        factoryBean.setRepeatCount(4);
        factoryBean.setRepeatInterval(120000);
        factoryBean.afterPropertiesSet();
        return factoryBean.getObject();*/

    }


}

