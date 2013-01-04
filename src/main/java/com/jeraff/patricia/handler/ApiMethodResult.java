package com.jeraff.patricia.handler;

import com.jeraff.patricia.conf.Core;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;

public class ApiMethodResult {
    private HashMap<String, Object> headers = new HashMap<String, Object>();
    private Object body;
    private int status = HttpServletResponse.SC_OK;

    ApiMethodResult(Object body) {
        this.body = body;
    }

    public ApiMethodResult() {

    }

    public HashMap<String, Object> getHeaders() {
        return headers;
    }

    public Object getBody() {
        return body;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void addHeader(String name, Object value) {
        headers.put(name, value);
    }

    public void setBody(Core body) {
        this.body = body;
    }
}
