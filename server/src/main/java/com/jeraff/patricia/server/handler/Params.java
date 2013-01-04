package com.jeraff.patricia.server.handler;

import com.jeraff.patricia.util.Method;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public class Params {
    public static final String PARAM_OFFSET = "offset";
    public static final String PARAM_LIMIT = "limit";
    public static final String PARAM_S = "s";
    public static final String PARAM_T = "t";

    public static final int DEFAULT_LIMIT = 25;

    private static final String ERROR_MESSAGE_S_REQUIRED = "\"s\" is a required parameter";
    private static final String ERROR_MESSAGE_S_SINGLE = "Method only accepts a single \"s\" parameter";
    private static final String ERROR_MESSAGE_S_ZERO_ONE = "Method only accepts zero or one \"s\" parameter";

    private String[] strings;
    private int offset = 0;
    private int limit = DEFAULT_LIMIT;

    public Params(HttpServletRequest request) {
        final Map<String, String[]> parameterMap = request.getParameterMap();

        if (parameterMap.containsKey(PARAM_T)) {
            final String[] strings = request.getParameterValues(PARAM_T);
            if (strings.length != 0) {
                setStrings(StringUtils.split(strings[0], "\n"));
            }
        } else if (parameterMap.containsKey(PARAM_S)) {
            setStrings(parameterMap.get(PARAM_S));
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
        if (strings == null || strings.length == 0) {
            throw new ParamValidationError(HttpServletResponse.SC_BAD_REQUEST, ERROR_MESSAGE_S_REQUIRED);
        }
    }

    private void validateGet() throws ParamValidationError {
        if (strings == null || strings.length == 0) {
            throw new ParamValidationError(HttpServletResponse.SC_BAD_REQUEST, ERROR_MESSAGE_S_REQUIRED);
        } else if (strings.length != 1) {
            throw new ParamValidationError(HttpServletResponse.SC_BAD_REQUEST, ERROR_MESSAGE_S_SINGLE);
        }
    }

    private void validateDelete() throws ParamValidationError {
        if (strings == null || strings.length == 0) {
            throw new ParamValidationError(HttpServletResponse.SC_BAD_REQUEST, ERROR_MESSAGE_S_REQUIRED);
        }
    }

    private void validateHead() throws ParamValidationError {
        if (strings != null && strings.length > 1) {
            throw new ParamValidationError(HttpServletResponse.SC_BAD_REQUEST, ERROR_MESSAGE_S_ZERO_ONE);
        }
    }

    public String[] getStrings() {
        return strings;
    }

    public String getFirstKey() {
        return (strings != null && strings.length != 0)
                ? strings[0]
                : null;
    }

    public void setStrings(String[] strings) {
        if (strings != null && strings.length != 0) {
            this.strings = new String[strings.length];
            for (int i = 0; i < strings.length; i++) {
                this.strings[i] = StringUtils.trim(StringUtils.chomp(strings[i]));
            }
        }
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}
