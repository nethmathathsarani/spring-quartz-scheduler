package com.test.demo.service;

import com.test.demo.exception.JobSubscriptionException;
import com.test.demo.model.JobSubscriptionDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

@Service
public class WebService implements SubscriptionService {

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private EmailService emailService;
    private static Logger logger = LoggerFactory.getLogger(WebService.class);

    @Override
    public void callService(Object request) throws JobSubscriptionException {
        JobSubscriptionDto requestDto=(JobSubscriptionDto)request;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            HttpEntity<String> entity = new HttpEntity<String>(headers);
            logger.info("[SBREQ] - Querying request : {} ", requestDto.getEndpoint());
            String response = restTemplate.exchange(requestDto.getEndpoint(), HttpMethod.valueOf(requestDto.getParameters().get(0).getValue()), entity, String.class).getBody();
            logger.info("[SBRES] - response " + response);
        } catch (HttpStatusCodeException he) {
            logger.error("[SBRES] - httpStatusCode: {} , resultBody: {}", he.getStatusCode(), he.getResponseBodyAsString());
            throw new JobSubscriptionException(he.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR,requestDto);
        }catch(Exception e){
            logger.error("[SBRES] - error occurred while calling web service ",e.getMessage());
            throw new JobSubscriptionException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR,requestDto);
        }
    }



}
