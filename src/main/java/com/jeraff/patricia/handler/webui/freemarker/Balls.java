package com.jeraff.patricia.handler.webui.freemarker;

import freemarker.template.Configuration;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class Balls {
    public Balls() {
        Configuration freemarkerConfig = new Configuration();

        try {
            final URL resource = getClass().getResource("/ftl/");
            freemarkerConfig.setDirectoryForTemplateLoading(new File(resource.getFile()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
