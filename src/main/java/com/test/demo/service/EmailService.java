package com.test.demo.service;

import com.test.demo.exception.JobSubscriptionException;
import com.test.demo.model.JobSubscriptionDto;
import com.test.demo.model.ParameterDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;


@Service
public class EmailService implements SubscriptionService {

    @Autowired
    private JavaMailSender mailSender;
    private static Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Override
    public void callService(Object request) throws JobSubscriptionException {
        JobSubscriptionDto requestDto=(JobSubscriptionDto)request;
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom("noreply@scheduler.com");
            helper.setTo(requestDto.getEndpoint());
            ParameterDto subjectObject=requestDto.getParameters().stream().filter(o-> o.getName().equals("subject")).findFirst().orElse(null);
            ParameterDto bodyObject=requestDto.getParameters().stream().filter(o-> o.getName().equals("body")).findFirst().orElse(null);
            helper.setSubject(subjectObject.getValue());
            helper.setText(bodyObject.getValue());
            logger.info("[SBREQ] - preparing to send email, to: {}, body:{}", requestDto.getEndpoint(), requestDto.getParameters().get(0).getValue());
            mailSender.send(message);
        } catch (MessagingException e) {
            logger.error("[SBRES] - Mail sending failed : {}", e);
            throw new JobSubscriptionException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR,requestDto);
        } catch (Exception e){
            logger.error("[SBRES] - Error occured while sending mail : {}", e.getMessage());
            throw new JobSubscriptionException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR,requestDto);
        }
    }




}
