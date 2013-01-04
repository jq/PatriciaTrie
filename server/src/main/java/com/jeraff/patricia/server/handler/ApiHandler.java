package com.jeraff.patricia.server.handler;

import com.jeraff.patricia.common.C;
import com.jeraff.patricia.config.Config;
import com.jeraff.patricia.config.Core;
import com.jeraff.patricia.util.Method;
import org.eclipse.jetty.server.Request;
import org.limewire.collection.PatriciaTrie;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class ApiHandler extends BaseHandler {
    public ApiHandler(PatriciaTrie<String, String> patriciaTrie, Core core, Config config) {
        super(patriciaTrie, core, config);

        try {
            final ObjectName name = core.getMBeanName();
            final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            mbs.registerMBean(new CoreData(patriciaTrie, core), name);
        } catch (Exception e) {
            log.log(Level.WARNING, "Couldn't register mbean for core: " + core.getPath(), e);
        }
    }

    public ApiHandler() {
    }

    public ApiMethodResult get(Params params) throws IOException {
        final List<String> prefixedBy = patriciaTrieOps.getPrefixedBy(params.getFirstKey());
        final ApiMethodResult apiMethodResult = new ApiMethodResult(prefixedBy);

        if (prefixedBy.isEmpty()) {
            apiMethodResult.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }

        return apiMethodResult;
    }

    public ApiMethodResult post(Params params) throws IOException {
        final HashMap<String, ArrayList<String>> result = patriciaTrieOps.put(params.getStrings());
        return new ApiMethodResult(result);
    }

    public ApiMethodResult put(Params params) throws IOException {
        final String[] strings = params.getStrings();
        patriciaTrieOps.enqueue(strings);

        final HashMap<String, Integer> result = new HashMap<String, Integer>();
        result.put(C.Strings.QUEUED, strings.length);

        return new ApiMethodResult(result);
    }

    public ApiMethodResult delete(Params params) throws IOException {
        return new ApiMethodResult(patriciaTrieOps.remove(params.getStrings()));
    }

    public ApiMethodResult head(Params params) throws IOException {
        final ApiMethodResult apiMethodResult = new ApiMethodResult();
        final String firstKey = params.getFirstKey();

        int count = (firstKey == null)
                ? patriciaTrieOps.size()
                : patriciaTrieOps.getPrefixedByCount(firstKey);

        if (count == 0 && firstKey != null) {
            apiMethodResult.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }

        apiMethodResult.addHeader(C.Headers.X_PREFIX_COUNT, String.valueOf(count));
        return apiMethodResult;
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        final Params params = new Params(request);
        final Method method = Method.valueOf(baseRequest.getMethod());

        try {
            params.validate(method);
        } catch (ParamValidationError validationError) {
            handleValidationError(validationError, response);
            baseRequest.setHandled(true);
            return;
        }

        ApiMethodResult apiMethodResult = null;
        switch (method) {
            case GET:
                apiMethodResult = get(params);
                break;
            case DELETE:
                apiMethodResult = delete(params);
                break;
            case HEAD:
                apiMethodResult = head(params);
                break;
            case POST:
                apiMethodResult = post(params);
                break;
            case PUT:
                apiMethodResult = put(params);
                break;
        }

        writeApiResponse(request, response, apiMethodResult);
        baseRequest.setHandled(true);
    }

    public void writeApiResponse(HttpServletRequest request, HttpServletResponse response, ApiMethodResult apiMethodResult) throws IOException {
        Object body = apiMethodResult.getBody();
        final String acceptEncodingHeader = request.getHeader(C.Headers.ACCEPT_ENCODING);
        final HttpServletResponse resp = (acceptEncodingHeader != null && acceptEncodingHeader.contains(C.Strings.GZIP))
                ? new GZIPResponseWrapper(response)
                : response;

        resp.setStatus(apiMethodResult.getStatus());
        resp.setContentType(C.Headers.APPLICATION_JSON);
        resp.setCharacterEncoding(C.Strings.UTF_8);

        final String connectionHeader = request.getHeader(C.Headers.CONNECTION);
        if (connectionHeader != null && connectionHeader.equalsIgnoreCase(C.Headers.KEEP_ALIVE)) {
            apiMethodResult.addHeader(C.Headers.CONNECTION, C.Headers.KEEP_ALIVE);
        }

        HashMap<String, Object> headers = apiMethodResult.getHeaders();
        if (!headers.isEmpty()) {
            for (Map.Entry<String, Object> header : headers.entrySet()) {
                resp.addHeader(header.getKey(), header.getValue().toString());
            }
        }

        PrintWriter writer = resp.getWriter();

        if (body != null) {
            objectMapper.writeValue(writer, body);
        }

        writer.close();
    }

    public void handleValidationError(ParamValidationError validationError, HttpServletResponse response) throws IOException {
        response.setStatus(validationError.code);
        objectMapper.writeValue(response.getWriter(), validationError.getErrorMap());
    }

}
