package com.jeraff.patricia.server.handler;

import com.jeraff.patricia.conf.Core;
import org.limewire.collection.PatriciaTrie;

public class CoreData implements CoreDataMBean {
    private PatriciaTrie<String, String> patriciaTrie;
    private Core core;

    public CoreData() {
    }

    public CoreData(PatriciaTrie<String, String> patriciaTrie, Core core) {
        this.patriciaTrie = patriciaTrie;
        this.core = core;
    }

    public String getContextPath() {
        return core.getPath();
    }

    public String getAnalyzerClass() {
        return core.getAnalyzer().getCanonicalName();
    }

    public int getTrieSize() {
        return patriciaTrie.size();
    }

}
