package com.jeraff.patricia.handler.rest;

import com.google.gson.Gson;
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

public class ApiHandler extends AbstractApiHandler<String, String> {
    public static final String HEADER_TOTAL = "X-Patricia-Total";
    public static final String CONTEXT_PATH = "/api";

    public ApiHandler(PatriciaTrie<String, String> patriciaTrie) {
        super(patriciaTrie);
    }

    public void get(Params params, HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String[] keys = params.keys;
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
                put(HEADER_TOTAL, total);
            }};

            if (total > 25) {
                result = result.subList(0, 25);
            }

            Collections.sort(result, new DistanceComparator(key));
        }

        write(response, headers, result);
    }

    public void putPost(Params params, HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String[] keys = params.keys;
        final int length = keys.length;
        final HashMap<String, ArrayList<String>> result = new HashMap<String, ArrayList<String>>(length);

        for (int i = 0; i < length; i++) {
            final String key = keys[i];
            final HashSet<String> grams = WordUtil.getGramsFormPut(key);
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
    public void handle(String target, Request request, HttpServletRequest httpServletRequest, HttpServletResponse response) throws IOException, ServletException {
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
        write(response, validationError.getErrorMap());
    }

}
