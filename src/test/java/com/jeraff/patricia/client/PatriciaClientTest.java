package com.jeraff.patricia.client;

import junit.framework.Assert;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;

import java.util.List;

public class PatriciaClientTest {

    @Test
    public void testConnection() {
        PatriciaClient c = new PatriciaClient();
    }


    @Test
    public void testNotFound() {
        final PatriciaClient patriciaClient = new PatriciaClient();
        final String md5 = DigestUtils.md5Hex(String.valueOf(System.currentTimeMillis()));
        final List<String> strings = patriciaClient.get(md5);

        Assert.assertTrue(strings.isEmpty());
    }

}
