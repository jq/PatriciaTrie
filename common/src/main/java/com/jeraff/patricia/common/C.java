package com.jeraff.patricia.common;

public class C {
    public static class Params {
        public static final String S = "s";
    }

    public static class Headers {
        public static final String X_PREFIX_COUNT = "X-Patricia-Prefix-Count";
        public static final String ACCEPT_ENCODING = "Accept-Encoding";
        public static final String APPLICATION_JSON = "application/json; charset=utf-8";
        public static final String CONNECTION = "Connection";
        public static final String KEEP_ALIVE = "Keep-Alive";
    }

    public static class Strings {
        public static final String GZIP = "gzip";
        public static final String UTF_8 = "UTF-8";
        public static final String QUEUED = "queued";
    }

    public static class Defaults {
        public static final int CONNECTOR_PORT = 8666;
    }
}
