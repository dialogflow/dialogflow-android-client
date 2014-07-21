package com.speaktoit.ai;

public class AIServiceException extends Exception {
    public AIServiceException() {
    }

    public AIServiceException(final String detailMessage, final Throwable throwable) {
        super(detailMessage, throwable);
    }

    public AIServiceException(final String detailMessage) {
        super(detailMessage);
    }
}
