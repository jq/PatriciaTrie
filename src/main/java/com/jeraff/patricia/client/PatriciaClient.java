package com.jeraff.patricia.client;

import com.jeraff.patricia.conf.Core;
import com.jeraff.patricia.server.handler.ApiHandler;
import com.jeraff.patricia.server.handler.Params;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.entity.GzipDecompressingEntity;
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
import java.net.URISyntaxException;
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

    public List<String> get(String prefix) {
        return get(DEFAULT_CORE, prefix);
    }

    public List<String> get(String core, String prefix) {
        if (!core.startsWith("/")) {
            core = String.format("/%s", core);
        }

        final URIBuilder builder;
        try {
            builder = new URIBuilder(getApiUriForCore(core)).setParameter(Params.PARAM_S, prefix);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        HttpGet httpget = null;

        try {
            httpget = new HttpGet(builder.build());
            httpget.setHeader(ApiHandler.HEADER_ACCEPT_ENCODING, ApiHandler.GZIP);

            final HttpResponse response = httpClient.execute(httpget);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                return new ArrayList<String>();
            }

            final Header ce = response.getFirstHeader(ApiHandler.HEADER_CONTENT_ENCODING);
            if (ce != null && ce.getValue().equals(ApiHandler.GZIP)) {
                response.setEntity(new GzipDecompressingEntity(response.getEntity()));
            }

            return objectMapper.readValue(response.getEntity().getContent(), StringList.class);
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
}
