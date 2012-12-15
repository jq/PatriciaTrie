package com.jeraff.patricia.handler;

import java.util.HashMap;

public class ApiMethodResult {
    HashMap<String, Object> headers;
    Object result;

    ApiMethodResult(HashMap<String, Object> headers, Object result) {
        this.headers = headers;
        this.result = result;
    }

    ApiMethodResult(Object result) {
        this.result = result;
    }

    public HashMap<String, Object> getHeaders() {
        return headers;
    }

    public Object getResult() {
        return result;
    }
}
