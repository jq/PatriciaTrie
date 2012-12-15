package com.jeraff.patricia.handler;

import com.google.gson.Gson;
import com.jeraff.patricia.conf.Config;
import com.jeraff.patricia.util.DistanceComparator;
import com.jeraff.patricia.util.Method;
import com.jeraff.patricia.util.WordUtil;
import org.eclipse.jetty.server.Request;
import org.limewire.collection.PatriciaTrie;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

public class ApiHandler extends BaseHandler {
    public static final String HEADER_PREFIX_TOTAL = "X-Patricia-Prefix-Total";
    public static final String HEADER_PATRICIA_TRIE_SIZE = "X-Patricia-Trie-Size";
    public static final String CONTEXT_PATH = "/api";

    public ApiHandler(PatriciaTrie<String, String> patriciaTrie, Config config) {
        super(patriciaTrie, config);
    }

    public ApiMethodResult get(Params params, HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String[] keys = params.getKeys();
        final String key = keys[0];

        final SortedMap<String, String> prefixedBy = patriciaTrie.getPrefixedBy(WordUtil.clean(key));
        List<String> result = null;
        HashMap<String, Object> headers = null;

        if (prefixedBy.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } else {
            result = new ArrayList<String>(new TreeSet<String>(prefixedBy.values()));
            final int total = result.size();

            headers = new HashMap<String, Object>(){{
                put(HEADER_PREFIX_TOTAL, total);
            }};

            if (total > 25) {
                result = result.subList(0, 25);
            }

            Collections.sort(result, new DistanceComparator(key));
        }

        return new ApiMethodResult(headers, result);
    }

    public ApiMethodResult putPost(Params params, HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String[] keys = params.getKeys();
        final int length = keys.length;
        final HashMap<String, ArrayList<String>> result = new HashMap<String, ArrayList<String>>(length);

        for (int i = 0; i < length; i++) {
            final String key = keys[i];
            final HashSet<String> grams = WordUtil.getGramsForPut(key);
            final ArrayList<String> strings = new ArrayList<String>();

            for (String gram : grams) {
                final String clean = WordUtil.clean(gram);
                final String put = patriciaTrie.put(clean, key);
                strings.add(gram);
            }

            result.put(key, strings);
        }

        return new ApiMethodResult(result);
    }

    public ApiMethodResult delete(Params params, HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String[] keys = params.getKeys();
        final int length = keys.length;
        final HashMap<String, String> result = new HashMap<String, String>(length);

        for (String key : keys) {
            result.put(key, patriciaTrie.remove(key));
        }

        return new ApiMethodResult(result);
    }

    public void head(Params params, HttpServletRequest request, HttpServletResponse response) throws IOException {
        final SortedMap<String, String> prefixedBy = patriciaTrie.getPrefixedBy(WordUtil.clean(params.getKeys()[0]));
        if (prefixedBy.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }

        response.setHeader(HEADER_PREFIX_TOTAL, String.valueOf(prefixedBy.size()));
        response.setHeader(HEADER_PATRICIA_TRIE_SIZE, String.valueOf(patriciaTrie.size()));
    }

    private void write(HttpServletResponse response, ApiMethodResult methodResult) throws IOException {
        write(response, methodResult.headers, methodResult.result);
    }

    private void write(HttpServletResponse response, Object o) throws IOException {
        write(response, null, o);
    }

    private void write(HttpServletResponse response, HashMap<String, Object> headers, Object o) throws IOException {
        if (o != null) {
            final Gson gson = new Gson();
            final String json = gson.toJson(o);
            response.setContentLength(json.getBytes().length);
            response.getWriter().print(json);
        }

        if (headers != null) {
            for (Map.Entry<String, Object> header : headers.entrySet()) {
                response.addHeader(header.getKey(), header.getValue().toString());
            }
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().close();
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest httpServletRequest, HttpServletResponse response) throws IOException, ServletException {
        final Params params = new Params(httpServletRequest);
        final Method method = Method.valueOf(baseRequest.getMethod());

        try {
            params.validate(method);
        } catch (ParamValidationError validationError) {
            handleValidationError(validationError, response);
            baseRequest.setHandled(true);
            return;
        }

        switch (method) {
            case GET:
                write(response, get(params, httpServletRequest, response));
                break;
            case DELETE:
                write(response, delete(params, httpServletRequest, response));
                break;
            case HEAD:
                head(params, httpServletRequest, response);
                break;
            default:
                write(response, putPost(params, httpServletRequest, response));
                break;
        }

        baseRequest.setHandled(true);
    }

    public void handleValidationError(ParamValidationError validationError, HttpServletResponse response) throws IOException {
        response.setStatus(validationError.code);
        write(response, validationError.getErrorMap());
    }

}
