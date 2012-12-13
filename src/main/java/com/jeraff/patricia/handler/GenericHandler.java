package com.jeraff.patricia.handler;

import com.google.gson.Gson;
import org.eclipse.jetty.server.Request;
import org.limewire.collection.PatriciaTrie;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

public class GenericHandler extends PatriciaHandler<String, String> {
    public GenericHandler(PatriciaTrie<String, String> patriciaTrie) {
        super(patriciaTrie);
    }

    public void get(Params params, HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String[] keys = params.keys;
        final int length = keys.length;
        final HashMap<String, ArrayList<String>> result = new HashMap<String, ArrayList<String>>(length);

        for (String key : keys) {
            final SortedMap<String, String> prefixedBy = patriciaTrie.getPrefixedBy(key, params.offset, params.limit);
            final Collection<String> values = prefixedBy.values();
            result.put(key, new ArrayList<String>(values));
        }

        write(response, result);
    }

    public void putPost(Params params, HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String[] keys = params.keys;
        final int length = keys.length;
        final HashMap<String, String> result = new HashMap<String, String>(length);

        for (int i = 0; i < length; i++) {
            final String key = keys[0];
            result.put(key, patriciaTrie.put(key, key));
        }

        write(response, result);
    }

    public void delete(Params params, HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String[] keys = params.keys;
        final int length = keys.length;
        final HashMap<String, String> result = new HashMap<String, String>(length);

        for (String key : keys) {
            result.put(key, patriciaTrie.remove(key));
        }

        write(response, result);
    }

    public void head(Params params, HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String[] keys = params.keys;
        final int length = keys.length;
        final HashMap<String, Boolean> result = new HashMap<String, Boolean>(length);

        for (String key : keys) {
            result.put(key, patriciaTrie.containsKey(key));
        }

        write(response, result);
    }

    private void write(HttpServletResponse response, Object o) throws IOException {
        final Gson gson = new Gson();
        final String json = gson.toJson(o);

        response.setContentType("application/json");
        response.setContentLength(json.getBytes().length);
        response.getWriter().print(json);
        response.getWriter().close();
    }

    @Override
    public void handle(String s, Request request, HttpServletRequest httpServletRequest, HttpServletResponse response) throws IOException, ServletException {
        final String method = request.getMethod();
        final StringBuilder sb = new StringBuilder();

        Params params = new Params(httpServletRequest.getParameterMap());

        if ("GET".equals(method)) {
            get(params, httpServletRequest, response);
        } else if ("DELETE".equals(method)) {
            delete(params, httpServletRequest, response);
        } else if (("HEAD".equals(method))) {
            head(params, httpServletRequest, response);
        } else {
            putPost(params, httpServletRequest, response);
        }
    }

    private class Params {
        public static final String PARAM_OFFSET = "offset";
        public static final String PARAM_LIMIT = "limit";
        public static final String PARAM_KEY = "key";

        public static final int DEFAULT_LIMIT = 25;

        private String[] keys = new String[]{};
        private int offset = 0;
        private int limit = DEFAULT_LIMIT;

        public Params(Map<String, String[]> parameterMap) {
            if (parameterMap.containsKey(PARAM_KEY)) {
                this.keys = parameterMap.get(PARAM_KEY);
            }

            final String[] offsets = parameterMap.get(PARAM_OFFSET);
            if (offsets != null && offsets.length != 0) {
                try {
                    offset = Integer.parseInt(offsets[0]);
                } catch (NumberFormatException nfe) {
                }
            }

            final String[] limits = parameterMap.get(PARAM_LIMIT);
            if (limits != null && limits.length != 0) {
                try {
                    limit = Integer.parseInt(limits[0]);
                } catch (NumberFormatException nfe) {
                }
            }
        }
    }
}
