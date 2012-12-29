package com.jeraff.patricia.handler;

import com.google.gson.Gson;
import com.jeraff.patricia.conf.Config;
import com.jeraff.patricia.conf.Core;
import com.jeraff.patricia.util.Method;
import org.eclipse.jetty.server.Request;
import org.limewire.collection.PatriciaTrie;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

public class ApiHandler extends BaseHandler {
    public static final String HEADER_PREFIX_COUNT = "X-Patricia-Prefix-Count";
    public static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
    public static final String HEADER_CONTENT_TYPE_JSON = "application/json";
    public static final String GZIP = "gzip";
    public static final String UTF_8 = "UTF-8";

    public ApiHandler(PatriciaTrie<String, String> patriciaTrie, Core core, Config config) {
        super(patriciaTrie, core, config);
    }

    public ApiMethodResult get(Params params, HttpServletRequest request, HttpServletResponse response) throws IOException {
        final List<String> prefixedBy = patriciaTrieOps.getPrefixedBy(params.getFirstKey());
        return new ApiMethodResult(new HashMap<String, Object>(), prefixedBy);
    }

    public ApiMethodResult putPost(Params params, HttpServletRequest request, HttpServletResponse response) throws IOException {
        final HashMap<String, ArrayList<String>> result = patriciaTrieOps.put(params.getStrings());
        return new ApiMethodResult(result);
    }

    public ApiMethodResult delete(Params params, HttpServletRequest request, HttpServletResponse response) throws IOException {
        return new ApiMethodResult(patriciaTrieOps.remove(params.getStrings()));
    }

    public void head(Params params, HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String firstKey = params.getFirstKey();
        int count;

        if (firstKey == null) {
            count = patriciaTrieOps.size();
        } else {
            count = patriciaTrieOps.getPrefixedByCount(firstKey);
        }

        if (count == 0 && firstKey != null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }

        response.setHeader(HEADER_PREFIX_COUNT, String.valueOf(count));
    }

    private void write(HttpServletRequest request, HttpServletResponse response, Object o) throws IOException {
        write(request, response, null, o);
    }

    private void write(HttpServletRequest request, HttpServletResponse response, HashMap<String, Object> headers, Object o) throws IOException {
        HttpServletResponse resp = (request.getHeader(HEADER_ACCEPT_ENCODING).contains(GZIP))
                ? new GZIPResponseWrapper(response)
                : response;

        if (o != null) {
            final Gson gson = new Gson();
            final String json = gson.toJson(o);
            resp.setContentLength(json.getBytes().length);
            resp.getWriter().write(json);
        }

        if (headers != null) {
            for (Map.Entry<String, Object> header : headers.entrySet()) {
                resp.addHeader(header.getKey(), header.getValue().toString());
            }
        }

        resp.setContentType(HEADER_CONTENT_TYPE_JSON);
        resp.setCharacterEncoding(UTF_8);
        resp.getWriter().close();
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest httpServletRequest, HttpServletResponse response) throws IOException, ServletException {
        final Params params = new Params(httpServletRequest);
        final Method method = Method.valueOf(baseRequest.getMethod());

        try {
            params.validate(method);
        } catch (ParamValidationError validationError) {
            handleValidationError(httpServletRequest, validationError, response);
            baseRequest.setHandled(true);
            return;
        }

        switch (method) {
            case GET:
                write(httpServletRequest, response, get(params, httpServletRequest, response));
                break;
            case DELETE:
                write(httpServletRequest, response, delete(params, httpServletRequest, response));
                break;
            case HEAD:
                head(params, httpServletRequest, response);
                break;
            case POST:
            case PUT:
            default:
                write(httpServletRequest, response, putPost(params, httpServletRequest, response).getResult());
                break;
        }

        baseRequest.setHandled(true);
    }

    public void handleValidationError(HttpServletRequest httpServletRequest, ParamValidationError validationError, HttpServletResponse response) throws IOException {
        response.setStatus(validationError.code);
        write(httpServletRequest, response, validationError.getErrorMap());
    }

}
