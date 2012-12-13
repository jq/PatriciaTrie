package com.jeraff.patricia;

import com.jeraff.patricia.handler.GenericHandler;
import org.eclipse.jetty.server.Server;
import org.limewire.collection.CharSequenceKeyAnalyzer;
import org.limewire.collection.PatriciaTrie;

public class PatriciaServer {
    public static void main(String[] args) throws Exception {
        final PatriciaTrie<String, String> patriciaTrie = new PatriciaTrie<String, String>(new CharSequenceKeyAnalyzer());
        final Server server = new Server(8666);

        server.setHandler(new GenericHandler(patriciaTrie));
        server.start();
        server.join();
    }
}
