package com.jeraff.patricia.handler;

import java.util.HashMap;

public class ParamValidationError extends Exception {
    int code;
    String message;

    ParamValidationError(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    @Override
    public String getMessage() {
        return String.format("Status code: %d. %s", code, message);
    }

    public HashMap<String, Object> getErrorMap() {
        final HashMap<String, Object> error = new HashMap<String, Object>();
        error.put("code", code);
        error.put("message", message);

        return error;
    }
}
