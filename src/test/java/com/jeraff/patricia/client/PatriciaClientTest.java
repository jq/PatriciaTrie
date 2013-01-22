package com.jeraff.patricia.client;

import com.jeraff.patricia.server.ops.Entry;
import junit.framework.Assert;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;

public class PatriciaClientTest {
    private static PatriciaClient patriciaClient;

    @BeforeClass
    public static void setup() {
        patriciaClient = new PatriciaClient();
    }

    @Test
    public void testNotFound() {
        final String md5 = DigestUtils.md5Hex(String.valueOf(System.currentTimeMillis()));
        final GetResponse strings = patriciaClient.get(md5);

        Assert.assertTrue(strings.isEmpty());
    }

    @Test
    public void testFound() {
        patriciaClient.post("a");
        final GetResponse strings = patriciaClient.get("a");

        Assert.assertFalse(strings.isEmpty());
        for (Entry s : strings) {
            Assert.assertTrue(s.getS().toLowerCase().contains("a"));
        }
    }

    @Test
    public void testPostResponseSingle() {
        String s = "arin was here";
        PostResponseBody map = patriciaClient.post(s);
        Assert.assertTrue(map.containsKey(s));
    }

    @Test
    public void testPostResponseMultiple() {
        String[] s = new String[]{
                "string 1",
                "string 2",
                "string 3"
        };

        PostResponseBody map = patriciaClient.post(s);

        for (String s1 : s) {
            Assert.assertTrue(map.containsKey(s1));
        }
    }

    @Test
    public void testPostThenGet() {
        String time = String.valueOf(System.currentTimeMillis());

        String[] s = new String[]{
                time + " string 1",
                time + " string 2",
                time + " string 3"
        };

        PostResponseBody map = patriciaClient.post(s);
        GetResponse getResponse = patriciaClient.get(time);

        Assert.assertEquals(s.length, getResponse.size());
    }

    @Test
    public void testDelete() {
        String s = String.valueOf(System.currentTimeMillis()) + " string 1";
        PostResponseBody map = patriciaClient.post(s);
        HashMap<String, String> delete = patriciaClient.delete(s);

        Assert.assertTrue(delete.containsKey(s));
        Assert.assertEquals(s, delete.get(s));
    }

    @Test
    public void testPostThenGetThenDelete() {
        String s = String.valueOf(System.currentTimeMillis()) + " string 1";

        PostResponseBody map = patriciaClient.post(s);
        GetResponse getResponse = patriciaClient.get(s);

        Assert.assertEquals(1, getResponse.size());
        Assert.assertEquals(s, getResponse.get(0).getS());

        patriciaClient.delete(s);
        getResponse = patriciaClient.get(s);
        Assert.assertEquals(0, getResponse.size());
    }

    @Test
    public void testHead() {
        String time = String.valueOf(System.currentTimeMillis());

        String[] s = new String[]{
                time + " string 1",
                time + " string 2",
                time + " string 3"
        };

        PostResponseBody map = patriciaClient.post(s);
        HeadResponse head = patriciaClient.head(time);

        Assert.assertEquals(s.length, head.getCount());
    }
}
