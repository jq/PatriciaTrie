package com.jeraff.patricia.client;

import com.jeraff.patricia.conf.Core;
import com.jeraff.patricia.server.handler.ApiHandler;
import com.jeraff.patricia.server.handler.Params;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
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

public class PatriciaClient {
    public static final int CONNECTIONS_PER_HOST = 20;
    public static final String DEFAULT_CORE = "/";

    private List<PatriciaHost> servers;
    private AbstractHttpClient httpClient;

    private static final ObjectMapper objectMapper = new ObjectMapper();
    static {
        objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

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

    public List<String> get(String prefix) {
        return get(DEFAULT_CORE, prefix);
    }

    public List<String> get(String core, String prefix) {
        final URIBuilder builder = new URIBuilder()
                .setScheme("http")
                .setHost(getHostForCore(core))
                .setPath(getApiPathForCore(core))
                .setParameter(Params.PARAM_S, prefix);

        HttpGet httpget = null;

        try {
            httpget = new HttpGet(builder.build());
            httpget.setHeader(ApiHandler.HEADER_ACCEPT_ENCODING, ApiHandler.GZIP);

            HttpResponse response = httpClient.execute(httpget);
            StringList strings = objectMapper.readValue(response.getEntity().getContent(), StringList.class);
            return strings;
        } catch (Exception e) {
            return new StringList();
        } finally {
            if (httpget != null) {
                httpget.releaseConnection();
            }
        }
    }

    private void discover() {
        for (PatriciaHost server : servers) {
            HashMap<String, Core> coreConfig = getCoreConfig(server);
            System.out.println(coreConfig);
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

    private String getApiPathForCore(String core) {
        return null;
    }

    private String getHostForCore(String core) {
        return null;
    }
}
