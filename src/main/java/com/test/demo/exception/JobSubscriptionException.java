package com.test.demo.exception;

import com.test.demo.model.JobSubscriptionDto;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@ToString
public class JobSubscriptionException extends Exception {

    private final String errorText;
    private final HttpStatus httpStatusCode;
    private final JobSubscriptionDto object;


    public JobSubscriptionException(String errorText, HttpStatus httpStatusCode,JobSubscriptionDto object) {
        super(errorText);
        this.errorText = errorText;
        this.httpStatusCode = httpStatusCode;
        this.object=object;
    }

    public String getErrorText() {
        return errorText;
    }

    public HttpStatus getHttpStatusCode() {
        return httpStatusCode;
    }

    public Object getObject() {
        return object;
    }
}

