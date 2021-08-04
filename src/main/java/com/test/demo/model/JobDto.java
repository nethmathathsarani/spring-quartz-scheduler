package com.test.demo.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JobDto implements Serializable{
	@NotBlank(message = "{name.notblank}")
	@ApiModelProperty(value = "job name", dataType = "String")
	private String name;
	@NotBlank(message = "{start.notblank}")
	@ApiModelProperty(value = "job start date and time",example = "19-05-2021 14:02:00")
	private String start;
	@NotBlank(message = "{end.notblank}")
	@ApiModelProperty(value = "job end date and time",example = "19-05-2021 14:02:00")
	private String end;
	@NotBlank(message = "{application.notblank}")
	@ApiModelProperty(value = "app name", dataType = "String")
	private String application;
	@ApiModelProperty(value = "job misfire instruction details")
	private MisfireInstructionDto misfires;
	@NotEmpty(message = "{subscriptions.notempty}")
	@NotNull(message = "{subscriptions.notnull}")
	@ApiModelProperty(value = "job subscription details")
	private List<JobSubscriptionDto> subscriptions;
	@NotEmpty(message = "{frequency.notempty}")
	@NotNull(message = "{frequency.notnull}")
	@ApiModelProperty(value = "job frequency details")
	private FrequencyDto frequency;
	@ApiModelProperty(value = "job notification details")
	private List<NotificationDto> notifications;



	
	
}
