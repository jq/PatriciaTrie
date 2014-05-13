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
    public static final String PARAM_K = "k";
    public static final String PARAM_V = "v";

    public static final int DEFAULT_LIMIT = 25;

    private static final String ERROR_MESSAGE_KV_REQUIRED = "\"k\" & \"v\" are required parameter";
    private static final String ERROR_MESSAGE_S_REQUIRED = "\"s\" is a required parameter";
    private static final String ERROR_MESSAGE_S_SINGLE = "Method only accepts a single \"s\" parameter";

    private String[] strings;
    private int offset = 0;
    private int limit = DEFAULT_LIMIT;
    private String k;
    private String v;

    public Params(HttpServletRequest request) {
        final Map<String, String[]> parameterMap = request.getParameterMap();

        if (parameterMap.containsKey(PARAM_T)) {
            final String[] strings = request.getParameterValues(PARAM_T);
            if (strings.length != 0) {
                setStrings(StringUtils.split(strings[0], "\n"));
            }
        } else if (parameterMap.containsKey(PARAM_S)) {
            setStrings(parameterMap.get(PARAM_S));
        } else if (parameterMap.containsKey(PARAM_K)) {
            setKeyValue(parameterMap.get(PARAM_K), parameterMap.get(PARAM_V));
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
        if (k != null || v != null) {
            if (k == null || v == null) {
                throw new ParamValidationError(HttpServletResponse.SC_BAD_REQUEST, ERROR_MESSAGE_KV_REQUIRED);
            } else {
                return;
            }
        }

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
        if (strings != null && strings.length != 1) {
            throw new ParamValidationError(HttpServletResponse.SC_BAD_REQUEST, ERROR_MESSAGE_S_REQUIRED);
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

    public void setKeyValue(String[] k, String[] v) {
        if (k.length == 1 && v.length == 1) {
            this.k = StringUtils.trim(StringUtils.chomp(k[0]));
            this.v = StringUtils.trim(StringUtils.chomp(v[0]));
        }
    }

    public String getK() {
        return k;
    }

    public boolean isKeyValue() {
        return k != null || v != null;
    }

    public String getV() {
        return v;
    }
}
