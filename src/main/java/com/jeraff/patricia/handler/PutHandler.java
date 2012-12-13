package com.jeraff.patricia.handler;

import org.eclipse.jetty.server.Request;
import org.limewire.collection.PatriciaTrie;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class PutHandler extends PatriciaHandler {

    public PutHandler(PatriciaTrie<String, String> patriciaTrie) {
        super(patriciaTrie);
    }

    public void handle(String s, Request request, HttpServletRequest httpServletRequest,
                       HttpServletResponse response) throws IOException, ServletException {

    }
}
