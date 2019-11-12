package com.bisnode.opa;

import org.gradle.api.Project;
import org.gradle.api.plugins.ExtraPropertiesExtension.UnknownPropertyException;

import javax.annotation.Nullable;

final class OpaPluginUtils {

    private OpaPluginUtils() {
    }

    static boolean stopOpaProcess(Project project) {
        try {
            @Nullable Object object = project.getExtensions().getExtraProperties().get("opaProcess");
            if (object instanceof Process) {
                Process process = (Process) object;
                process.destroy();
                return true;
            }
        } catch (UnknownPropertyException ignored) {
        }
        return false;
    }

}
