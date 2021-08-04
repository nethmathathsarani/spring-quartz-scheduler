package com.test.demo.controller;

import com.test.demo.constant.JobStatus;
import com.test.demo.model.CommonJobRequestDto;
import com.test.demo.model.JobDto;
import com.test.demo.model.JobResponseDto;
import com.test.demo.model.ServerResponse;
import com.test.demo.exception.JobServiceException;
import com.test.demo.job.CronJob;
import com.test.demo.job.SimpleJob;
import com.test.demo.service.JobService;
import com.test.demo.util.FrequencyType;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.text.ParseException;
import java.util.List;
import java.util.Map;


@RestController
@Api(value = "spring quartz scheduler", description = "Operations related to job scheduling")
public class JobController {

    @Autowired
    @Lazy
    JobService jobService;
    private static org.slf4j.Logger logger = LoggerFactory.getLogger(JobController.class);

    @ApiOperation(value = "Schedule a job", response = JobDto.class)
    @PostMapping("schedule")
    public ResponseEntity<JobResponseDto> schedule(@RequestBody JobDto jobDto) throws ParseException, JobServiceException, JsonProcessingException {
        if (!jobService.isJobWithNamePresent(jobDto.getName())) {
            if(jobDto.getFrequency().getType().equals(FrequencyType.REPEAT_INT)){
                return jobService.scheduleSimpleJob(jobDto, SimpleJob.class);
            }else{
                return jobService.scheduleCronJob(jobDto, CronJob.class);
            }
        } else {
            return new ResponseEntity<>(new JobResponseDto(jobDto.getName(), jobDto.getApplication(), JobStatus.NONE, "job with same name exist"), HttpStatus.BAD_REQUEST);
        }
    }


    @ApiOperation(value = "Delete a job", response = Boolean.class)
    @DeleteMapping("delete")
    public Boolean delete(@RequestParam("jobName") String jobName) throws JobServiceException {
        logger.info("Delete job : {}", jobName);
        if (jobService.isJobWithNamePresent(jobName)) {
            boolean isJobRunning = jobService.isJobRunning(jobName);
            if (!isJobRunning) {
                boolean status = jobService.deleteJob(jobName);
                if (status) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            throw new JobServiceException("job not found", HttpStatus.NOT_FOUND);
        }
    }

    @ApiOperation(value = "Pause a job", response = JobResponseDto.class)
    @PatchMapping("pause")
    public ResponseEntity<JobResponseDto> pause(@RequestBody CommonJobRequestDto commonJobRequestDto) throws JobServiceException {
        logger.info("Pausing job : {}", commonJobRequestDto.getJobName());
        return jobService.pauseJob(commonJobRequestDto.getJobName());
    }

    @ApiOperation(value = "Resume a job", response = JobResponseDto.class)
    @PatchMapping("resume")
    public ResponseEntity<JobResponseDto> resume(@RequestBody CommonJobRequestDto commonJobRequestDto) throws JobServiceException {
        logger.info("Resuming job : {}", commonJobRequestDto.getJobName());
        return jobService.resumeJob(commonJobRequestDto.getJobName());
    }

    @ApiOperation(value = "Update a job", response = JobDto.class)
    @PutMapping("update")
    public ResponseEntity<JobDto> updateJob(@RequestBody JobDto jobDto) throws JobServiceException {
        logger.info("Updating job : {}", jobDto.getName());
        if (jobService.isJobWithNamePresent(jobDto.getName())) {
            if(jobDto.getFrequency().getType().equals(FrequencyType.REPEAT_INT)) {
                return jobService.updateOneTimeJob(jobDto);
            }else{
                return jobService.updateCronJob(jobDto);
            }
        } else {
            throw new JobServiceException("job not found", HttpStatus.NOT_FOUND);
        }
    }


    @ApiOperation(value = "Get all jobs ", response = List.class)
    @GetMapping("jobs/all")
    public List<Map<String, Object>> getAllJobs() {
        logger.info("Getting all jobs");
        List<Map<String, Object>> list = jobService.getAllJobs();
        return list;
    }
	
	/*@ApiOperation(value = "Get jobs by external reference",response = List.class)
	@GetMapping("jobs")
	public List<Map<String, Object>> getJobsByExternalRef(@RequestParam("externalRefId") int externalRefId,@RequestParam("externalRefType") String externalRefType){
		System.out.println("JobController.getAllJobs()");

		List<Map<String, Object>> list = jobService.getJobsByExternalRef(externalRefId,externalRefType);
		return list;
		//return getServerResponse(ServerResponseCode.SUCCESS, list);
	}*/

    @ApiOperation(value = "Check job name existence", response = Boolean.class)
    @GetMapping("checkJobName")
    public boolean checkJobName(@RequestParam("jobName") String jobName) {
        logger.info("Check job name :{}", jobName);
        if (jobName == null || jobName.trim().equals("")) {
            return false;
        }
        boolean status = jobService.isJobWithNamePresent(jobName);
        return status;
    }

    @ApiOperation(value = "Check if job is running", response = Boolean.class)
    @GetMapping("isJobRunning")
    public boolean isJobRunning(@RequestParam("jobName") String jobName) {
        logger.info("Check is job running : {}", jobName);
        boolean status = jobService.isJobRunning(jobName);
        return status;
    }

    @Operation
    @ApiOperation(value = "Get job state", response = JobStatus.class)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the book",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = JobStatus.class))}),
    })
    @GetMapping("jobState")
    public JobStatus getJobState(@RequestParam("jobName") String jobName) {
        logger.info("Get job state : {}", jobName);
        JobStatus jobState = jobService.getJobState(jobName);
        return jobState;
    }

    @ApiOperation(value = "Stop a job", response = JobDto.class)
    @PatchMapping("stop")
    public Map<String, Object> stopJob(@RequestBody CommonJobRequestDto commonJobRequestDto) throws JobServiceException {
        logger.info("Stopping job : {}", commonJobRequestDto.getJobName());
        if (jobService.isJobWithNamePresent(commonJobRequestDto.getJobName())) {
            if (jobService.isJobRunning(commonJobRequestDto.getJobName())) {
                return jobService.stopJob(commonJobRequestDto.getJobName());
            } else {
                throw new JobServiceException("job not in running state", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            throw new JobServiceException("job not found", HttpStatus.NOT_FOUND);
        }
    }

    @ApiOperation(value = "Start a job", response = JobDto.class)
    @PatchMapping("start")
    public Map<String, Object> startJobNow(@RequestBody CommonJobRequestDto commonJobRequestDto) throws JobServiceException {
        logger.info("Starting job now : {}", commonJobRequestDto.getJobName());

        if (jobService.isJobWithNamePresent(commonJobRequestDto.getJobName())) {
            if (!jobService.isJobRunning(commonJobRequestDto.getJobName())) {
                return jobService.startJobNow(commonJobRequestDto.getJobName());
            } else {
                throw new JobServiceException("job already in running state", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            throw new JobServiceException("job not found", HttpStatus.NOT_FOUND);
        }
    }

    public ServerResponse getServerResponse(int responseCode, Object data) {
        ServerResponse serverResponse = new ServerResponse();
        serverResponse.setStatusCode(responseCode);
        serverResponse.setData(data);
        return serverResponse;
    }
}
