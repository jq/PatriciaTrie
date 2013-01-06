package com.jeraff.patricia.client;

import com.jeraff.patricia.server.handler.ApiHandler;

import java.util.HashMap;

public class HeadResponse extends HashMap<String, String> {

    public int getCount() {
        return Integer.valueOf(get(ApiHandler.HEADER_PREFIX_COUNT));
    }
}
