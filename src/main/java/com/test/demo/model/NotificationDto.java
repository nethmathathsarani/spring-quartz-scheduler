package com.test.demo.model;

import com.test.demo.util.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDto implements Serializable {
    private NotificationType type;
    private List<JobSubscriptionDto> subscriptions;
}
