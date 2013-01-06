package com.jeraff.patricia.client;

import junit.framework.Assert;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

public class PatriciaClientTest {
    private static PatriciaClient patriciaClient;

    @BeforeClass
    public static void setup() {
        patriciaClient = new PatriciaClient();
    }

    @Test
    public void testNotFound() {
        final String md5 = DigestUtils.md5Hex(String.valueOf(System.currentTimeMillis()));
        final List<String> strings = patriciaClient.get(md5);

        Assert.assertTrue(strings.isEmpty());
    }

    @Test
    public void testFound() {
        final List<String> strings = patriciaClient.get("a");

        Assert.assertFalse(strings.isEmpty());
        for (String s : strings) {
            Assert.assertTrue(s.toLowerCase().contains("a"));
        }
    }

    @Test
    public void testPostResponseSingle() {
        String s = "arin was here";
        HashMap<String, List<String>> map = patriciaClient.post(s);
        Assert.assertTrue(map.containsKey(s));
    }

    @Test
    public void testPostResponseMultiple() {
        String[] s = new String[]{
                "string 1",
                "string 2",
                "string 3"
        };

        HashMap<String, List<String>> map = patriciaClient.post(s);

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

        HashMap<String, List<String>> map = patriciaClient.post(s);
        List<String> stringList = patriciaClient.get(time);

        Assert.assertEquals(s.length, stringList.size());
    }

    @Test
    public void testDelete() {
        String s = String.valueOf(System.currentTimeMillis()) + " string 1";
        HashMap<String, List<String>> map = patriciaClient.post(s);
        HashMap<String, String> delete = patriciaClient.delete(s);

        Assert.assertTrue(delete.containsKey(s));
        Assert.assertEquals(s, delete.get(s));
    }

    @Test
    public void testPostThenGetThenDelete() {
        String s = String.valueOf(System.currentTimeMillis()) + " string 1";

        HashMap<String, List<String>> map = patriciaClient.post(s);
        List<String> stringList = patriciaClient.get(s);

        Assert.assertEquals(1, stringList.size());
        Assert.assertEquals(s, stringList.get(0));

        patriciaClient.delete(s);
        stringList = patriciaClient.get(s);
        Assert.assertEquals(0, stringList.size());
    }

    @Test
    public void testHead() {
        String time = String.valueOf(System.currentTimeMillis());

        String[] s = new String[]{
                time + " string 1",
                time + " string 2",
                time + " string 3"
        };

        HashMap<String, List<String>> map = patriciaClient.post(s);
        HeadResponse head = patriciaClient.head(time);

        Assert.assertEquals(s.length, head.getCount());
    }
}
