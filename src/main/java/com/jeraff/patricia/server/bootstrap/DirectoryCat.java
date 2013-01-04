package com.jeraff.patricia.server.bootstrap;

import com.jeraff.patricia.conf.Core;
import com.jeraff.patricia.server.ops.PatriciaOps;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import java.io.File;
import java.io.FileFilter;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DirectoryCat implements Bootstrap {
    protected static final Logger log = Logger.getLogger(DirectoryCat.class.getCanonicalName());

    private Core core;
    private PatriciaOps patriciaTrieOps;

    public DirectoryCat(Core core, PatriciaOps patriciaTrieOps) {
        this.core = core;
        this.patriciaTrieOps = patriciaTrieOps;
    }

    @Override
    public boolean bootstrap() throws Exception {
        final com.jeraff.patricia.conf.DirectoryCat dirCat = core.getDirCat();
        final File dir = new File(dirCat.getDirectory());
        final FileFilter fileFilter = new WildcardFileFilter(dirCat.getPattern());
        final File[] files = dir.listFiles(fileFilter);

        int i = 0;
        for (File file : files) {
            final LineIterator iterator = FileUtils.lineIterator(file, dirCat.getEncoding());
            while (iterator.hasNext()) {
                final String string = iterator.next();
                patriciaTrieOps.put(new String[]{string});
                i++;

                if (log.isLoggable(Level.INFO)) {
                    log.log(Level.INFO, "Bootstrap: {0} in {1}", new Object[]{string, core});
                }
            }
        }

        log.log(Level.INFO, "Inserted {0} strings in {1}", new Object[]{i, core});
        return true;
    }
}
