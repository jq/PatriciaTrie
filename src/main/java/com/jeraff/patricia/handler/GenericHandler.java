package com.jeraff.patricia.handler;

import com.google.gson.Gson;
import com.jeraff.patricia.util.Method;
import com.jeraff.patricia.util.WordUtil;
import org.eclipse.jetty.server.Request;
import org.limewire.collection.PatriciaTrie;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

public class GenericHandler extends PatriciaHandler<String, String> {
    public static final String METHOD_GET = "METHOD_GET";
    public static final String METHOD_DELETE = "METHOD_DELETE";
    public static final String METHOD_HEAD = "METHOD_HEAD";

    public GenericHandler(PatriciaTrie<String, String> patriciaTrie) {
        super(patriciaTrie);
    }

    public void get(Params params, HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String[] keys = params.keys;
        final String key = keys[0];

        final SortedMap<String, String> prefixedBy = patriciaTrie.getPrefixedBy(WordUtil.clean(key));
        ArrayList<String> result = null;

        if (prefixedBy.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } else {
            result = new ArrayList<String>(new TreeSet<String>(prefixedBy.values()));
        }

        write(response, result);
    }

    public void putPost(Params params, HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String[] keys = params.keys;
        final int length = keys.length;
        final HashMap<String, ArrayList<String>> result = new HashMap<String, ArrayList<String>>(length);

        for (int i = 0; i < length; i++) {
            final String key = keys[i];
            final ArrayList<String> grams = WordUtil.getGramsFormPut(key);
            final ArrayList<String> strings = new ArrayList<String>();

            for (String gram : grams) {
                final String put = patriciaTrie.put(WordUtil.clean(gram), key);
                strings.add(gram);
            }

            result.put(key, strings);
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
        final boolean contains = patriciaTrie.containsKey(keys[0]);

        if (!contains) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }

        write(response, null);
    }

    private void write(HttpServletResponse response, Object o) throws IOException {
        if (o != null) {
            final Gson gson = new Gson();
            final String json = gson.toJson(o);
            response.setContentLength(json.getBytes().length);
            response.getWriter().print(json);
        }

        response.setContentType("application/json");
        response.getWriter().close();
    }

    @Override
    public void handle(String s, Request request, HttpServletRequest httpServletRequest, HttpServletResponse response) throws IOException, ServletException {
        final Params params = new Params(httpServletRequest);
        final Method method = Method.valueOf(request.getMethod());

        try {
            params.validate(method);
        } catch (ParamValidationError validationError) {
            handleValidationError(validationError, response);
            return;
        }

        switch (method) {
            case GET:
                get(params, httpServletRequest, response);
                break;
            case DELETE:
                delete(params, httpServletRequest, response);
                break;
            case HEAD:
                head(params, httpServletRequest, response);
                break;
            default:
                putPost(params, httpServletRequest, response);
                break;
        }
    }

    private void handleValidationError(ParamValidationError validationError, HttpServletResponse response) throws IOException {
        response.setStatus(validationError.code);
        write(response, validationError);
    }

    private class Params {
        public static final String PARAM_OFFSET = "offset";
        public static final String PARAM_LIMIT = "limit";
        public static final String PARAM_KEY = "key";

        public static final int DEFAULT_LIMIT = 25;

        private static final String ERROR_MESSAGE_S_REQUIRED = "'s' is a required parameter";
        private static final String ERROR_MESSAGE_S_SINGLE = "Method only accepts a single 's' parameter";

        private String[] keys = new String[]{};
        private int offset = 0;
        private int limit = DEFAULT_LIMIT;

        public Params(HttpServletRequest request) {
            final Map<String, String[]> parameterMap = request.getParameterMap();

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

        public void validate(Method method) throws ParamValidationError {
            switch (method) {
                case HEAD:
                    validateHead();
                    break;
                case DELETE:
                    validateDelete();
                    break;
                case GET:
                    validateGet();
                    break;
                default:
                    validatePutPost();
                    break;
            }
        }

        private void validatePutPost() throws ParamValidationError {
            if (keys.length == 0) {
                throw new ParamValidationError(HttpServletResponse.SC_BAD_REQUEST, ERROR_MESSAGE_S_REQUIRED);
            }
        }

        private void validateGet() throws ParamValidationError {
            if (keys.length == 0) {
                throw new ParamValidationError(HttpServletResponse.SC_BAD_REQUEST, ERROR_MESSAGE_S_REQUIRED);
            } else if (keys.length != 1) {
                throw new ParamValidationError(HttpServletResponse.SC_BAD_REQUEST, ERROR_MESSAGE_S_SINGLE);
            }
        }

        private void validateDelete() throws ParamValidationError {
            if (keys.length == 0) {
                throw new ParamValidationError(HttpServletResponse.SC_BAD_REQUEST, ERROR_MESSAGE_S_REQUIRED);
            }
        }

        private void validateHead() throws ParamValidationError {
            if (keys.length == 0) {
                throw new ParamValidationError(HttpServletResponse.SC_BAD_REQUEST, ERROR_MESSAGE_S_REQUIRED);
            } else if (keys.length != 1) {
                throw new ParamValidationError(HttpServletResponse.SC_BAD_REQUEST, ERROR_MESSAGE_S_SINGLE);
            }
        }
    }

    private class ParamValidationError extends Exception {
        private int code;
        private String message;

        private ParamValidationError(int code, String message) {
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
}
