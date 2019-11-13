package com.bisnode.opa;

import org.gradle.api.Project;
import org.gradle.api.plugins.ExtraPropertiesExtension.UnknownPropertyException;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    static String toAbsoluteProjectPath(Project project, String pathComponent) {
        Path path = Paths.get(pathComponent);
        return path.isAbsolute() ?
                path.toString() :
                Paths.get(project.getRootDir().getPath(), pathComponent).toString();
    }

}
