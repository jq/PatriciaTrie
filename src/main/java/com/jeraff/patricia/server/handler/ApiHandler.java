package com.jeraff.patricia.server.handler;

import com.jeraff.patricia.client.IndexEntry;
import com.jeraff.patricia.conf.Config;
import com.jeraff.patricia.conf.Core;
import com.jeraff.patricia.server.ops.Entry;
import com.jeraff.patricia.util.Method;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class ApiHandler extends BaseHandler {
    public static final String HEADER_PREFIX_COUNT = "X-Patricia-Prefix-Count";
    public static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
    public static final String HEADER_CONTENT_ENCODING = "Content-Encoding";
    public static final String HEADER_CONTENT_TYPE_JSON = "application/json; charset=utf-8";
    public static final String HEADER_CONNECTION = "Connection";
    public static final String HEADER_CONNECTION_KEEP_ALIVE = "Keep-Alive";
    public static final String HEADER_HASH = "X-Patricia-HASH";

    public static final String GZIP = "gzip";
    public static final String UTF_8 = "UTF-8";
    public static final String QUEUED = "queued";

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
        final List<Entry> prefixedBy = patriciaTrieOps.getPrefixedBy(params.getPrefix());
        final ApiMethodResult apiMethodResult = new ApiMethodResult(prefixedBy);

        if (prefixedBy.isEmpty()) {
            apiMethodResult.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }

        return apiMethodResult;
    }

    public ApiMethodResult post(Params params) throws IOException {
        NameValuePair nvp = new BasicNameValuePair(params.getK(), params.getV());
        final HashMap<String, IndexEntry> result = patriciaTrieOps.put(nvp);
        return new ApiMethodResult(result);
    }

    public ApiMethodResult delete(Params params) throws IOException {
        String[] strings = new String[]{params.getK()};
        return new ApiMethodResult(patriciaTrieOps.remove(strings));
    }

    public ApiMethodResult head(Params params) throws IOException {
        return get(params);
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
        }

        writeApiResponse(request, response, apiMethodResult);
        baseRequest.setHandled(true);
    }

    public void writeApiResponse(HttpServletRequest request, HttpServletResponse response, ApiMethodResult apiMethodResult) throws IOException {
        Object body = apiMethodResult.getBody();
        final String acceptEncodingHeader = request.getHeader(HEADER_ACCEPT_ENCODING);
        final HttpServletResponse resp = (acceptEncodingHeader != null && acceptEncodingHeader.contains(GZIP))
                ? new GZIPResponseWrapper(response)
                : response;

        resp.setStatus(apiMethodResult.getStatus());
        resp.setContentType(HEADER_CONTENT_TYPE_JSON);
        resp.setCharacterEncoding(UTF_8);

        final String connectionHeader = request.getHeader(HEADER_CONNECTION);
        if (connectionHeader != null && connectionHeader.equalsIgnoreCase(HEADER_CONNECTION_KEEP_ALIVE)) {
            apiMethodResult.addHeader(HEADER_CONNECTION, HEADER_CONNECTION_KEEP_ALIVE);
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
