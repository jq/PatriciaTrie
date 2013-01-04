package com.jeraff.patricia.client;

import com.jeraff.patricia.common.C;
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
import org.codehaus.jackson.map.ObjectMapper;

import java.util.List;

public class Client {
    public static final int CONNECTIONS_PER_HOST = 20;
    public static final String DEFAULT_CORE = "/";

    private List<HostPort> servers;
    private AbstractHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public Client() {
        servers.add(new HostPort());
        this.objectMapper = new ObjectMapper();
        setupHttpClient();
    }

    public Client(List<HostPort> servers) {
        this.servers = servers;
        this.objectMapper = new ObjectMapper();
        setupHttpClient();
    }

    public Client(List<HostPort> servers, AbstractHttpClient httpClient) {
        this.servers = servers;
        this.httpClient = httpClient;
        this.objectMapper = new ObjectMapper();
    }

    public Client(List<HostPort> servers, ClientConnectionManager clientConnectionManager) {
        this.servers = servers;
        this.httpClient = new DefaultHttpClient(clientConnectionManager);
        this.objectMapper = new ObjectMapper();
    }

    private void setupHttpClient() {
        SchemeRegistry schemeRegistry = new SchemeRegistry();

        for (HostPort server : servers) {
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
                .setParameter(C.Params.S, prefix);

        HttpGet httpget = null;

        try {
            httpget = new HttpGet(builder.build());
            httpget.setHeader(C.Headers.ACCEPT_ENCODING, C.Strings.GZIP);

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

    private String getApiPathForCore(String core) {
        return null;
    }

    private String getHostForCore(String core) {
        return null;
    }
}
