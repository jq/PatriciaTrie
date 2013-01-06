package com.jeraff.patricia.client;

import com.jeraff.patricia.conf.Core;
import com.jeraff.patricia.server.handler.ApiHandler;
import com.jeraff.patricia.server.handler.Params;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class PatriciaClient {
    public static final int CONNECTIONS_PER_HOST = 20;
    public static final String DEFAULT_CORE = "/";

    private List<PatriciaHost> servers;
    private AbstractHttpClient httpClient;
    private HashMap<String, List<String>> coreToApiUrl = new HashMap<String, List<String>>();

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private Random random = new Random();

    public PatriciaClient() {
        servers = new ArrayList<PatriciaHost>(1);
        servers.add(new PatriciaHost());

        setupHttpClient();
        discover();
    }

    public PatriciaClient(List<PatriciaHost> servers) {
        this.servers = servers;

        setupHttpClient();
        discover();
    }

    public PatriciaClient(List<PatriciaHost> servers, AbstractHttpClient httpClient) {
        this.servers = servers;
        this.httpClient = httpClient;

        discover();
    }

    public PatriciaClient(List<PatriciaHost> servers, ClientConnectionManager clientConnectionManager) {
        this.servers = servers;
        this.httpClient = new DefaultHttpClient(clientConnectionManager);

        discover();
    }

    private void setupHttpClient() {
        SchemeRegistry schemeRegistry = new SchemeRegistry();

        for (PatriciaHost server : servers) {
            schemeRegistry.register(new Scheme("http", server.getPort(), PlainSocketFactory.getSocketFactory()));
        }

        PoolingClientConnectionManager cm = new PoolingClientConnectionManager(schemeRegistry);
        cm.setMaxTotal(CONNECTIONS_PER_HOST * servers.size());
        cm.setDefaultMaxPerRoute(CONNECTIONS_PER_HOST);

        httpClient = new DefaultHttpClient(cm);
    }

    private <T> T executeHttpMethod(HttpRequestBase method, Class<? extends T> responseClass)
            throws HttpNotFoundException, DeserializationException {

        method.setHeader(ApiHandler.HEADER_ACCEPT_ENCODING, ApiHandler.GZIP);
        HttpResponse response = null;

        try {
            response = httpClient.execute(method);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            method.releaseConnection();
        }

        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND) {
            throw new HttpNotFoundException();
        }

        final Header ce = response.getFirstHeader(ApiHandler.HEADER_CONTENT_ENCODING);
        if (ce != null && ce.getValue().equals(ApiHandler.GZIP)) {
            response.setEntity(new GzipDecompressingEntity(response.getEntity()));
        }

        try {
            if (responseClass != null) {
                return objectMapper.readValue(response.getEntity().getContent(), responseClass);
            } else {
                return null;
            }
        } catch (IOException e) {
            throw new DeserializationException(e);
        }
    }

    public List<String> get(String prefix) {
        return get(DEFAULT_CORE, prefix);
    }

    public List<String> get(String core, String prefix) {
        if (!core.startsWith("/")) {
            core = String.format("/%s", core);
        }

        try {
            final URIBuilder builder = new URIBuilder(getApiUriForCore(core)).setParameter(Params.PARAM_S, prefix);
            final HttpGet httpget = new HttpGet(builder.build());
            return executeHttpMethod(httpget, StringList.class);
        } catch (HttpNotFoundException e) {
            return new StringList();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean put(String... strings) {
        return put(DEFAULT_CORE, strings);
    }

    public boolean put(String core, String... strings) {
        try {
            final URIBuilder builder = new URIBuilder(getApiUriForCore(core));
            for (String string : strings) {
                builder.addParameter(Params.PARAM_S, string);
            }
            executeHttpMethod(new HttpPut(builder.build()), null);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public HashMap<String, List<String>> post(String... strings) {
        return post(DEFAULT_CORE, strings);
    }

    public HashMap<String, List<String>> post(String core, String... strings) {
        try {
            URIBuilder builder = new URIBuilder(getApiUriForCore(core));
            for (String string : strings) {
                builder.addParameter(Params.PARAM_S, string);
            }

            HttpPost post = new HttpPost(builder.build());
            return executeHttpMethod(post, PostResponseBody.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public HashMap<String, String> delete(String... strings) {
        return delete(DEFAULT_CORE, strings);
    }

    public HashMap<String, String> delete(String core, String... strings) {
        try {
            URIBuilder builder = new URIBuilder(getApiUriForCore(core));
            for (String string : strings) {
                builder.addParameter(Params.PARAM_S, string);
            }

            HttpDelete delete = new HttpDelete(builder.build());
            return executeHttpMethod(delete, DeleteResponseBody.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public HashMap<String, String> head(String string) {
        return head(DEFAULT_CORE, string);
    }

    public HashMap<String, String> head(String core, String string) {
        HttpHead head = null;
        try {
            final URIBuilder builder = new URIBuilder(getApiUriForCore(core)).setParameter(Params.PARAM_S, string);
            final HttpResponse response = httpClient.execute(new HttpHead(builder.build()));
            final Header[] allHeaders = response.getAllHeaders();
            final HashMap<String, String> headers = new HashMap<String, String>();

            for (Header header : allHeaders) {
                headers.put(header.getName(), header.getValue());
            }

            return headers;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (head != null) {
                head.releaseConnection();
            }
        }
    }

    private void discover() {
        for (PatriciaHost server : servers) {
            final CoreConfig coreConfig = getCoreConfig(server);
            for (Core core : coreConfig.values()) {

                List<String> mapping = coreToApiUrl.get(core.getPath());
                if (mapping == null) {
                    mapping = new ArrayList<String>();
                    coreToApiUrl.put(core.getPath(), mapping);
                }

                mapping.add(server.getCoreApilUrl(core));
            }
        }
    }

    private CoreConfig getCoreConfig(PatriciaHost server) {
        try {
            final URI configUri = server.getConfigUri();
            HttpGet get = new HttpGet(configUri);
            final HttpResponse response = httpClient.execute(get);
            final CoreConfig coreConfig = objectMapper.readValue(response.getEntity().getContent(), CoreConfig.class);
            return coreConfig;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getApiUriForCore(String core) {
        final List<String> urls = coreToApiUrl.get(core);
        if (urls == null || urls.isEmpty()) {
            throw new RuntimeException("invalid core");
        }

        final int size = urls.size();
        if (size == 0) {
            return urls.get(0);
        }

        return urls.get(random.nextInt(size));
    }

    private class HttpNotFoundException extends Exception {
    }

    private class DeserializationException extends Exception {
        private DeserializationException(Throwable throwable) {
            super(throwable);
        }
    }
}
