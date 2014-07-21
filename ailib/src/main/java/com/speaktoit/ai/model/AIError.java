package com.speaktoit.ai.model;

import com.speaktoit.ai.AIServiceException;

import java.io.Serializable;

public class AIError implements Serializable {
    private final String message;
    private AIServiceException exception;

    public AIError(final String message) {
        this.message = message;
    }

    public AIError(final AIServiceException e) {
        message = e.getMessage();
        exception = e;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        if (exception != null) {
            return exception.toString();
        } else {
            return message;
        }
    }
}
