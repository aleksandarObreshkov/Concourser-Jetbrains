package org.alex.concourserjetbrains;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;

@Service(Service.Level.PROJECT)
public final class ConfigurationService {

    private Configuration CONFIGURATION;

    public static ConfigurationService get(Project project) {
        return project.getService(ConfigurationService.class);
    }

    public Configuration getConfig() {
        return CONFIGURATION;
    }

    public void setConfig(Configuration config) {
        this.CONFIGURATION = config;
    }
}
