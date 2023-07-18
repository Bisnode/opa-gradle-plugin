package com.bisnode.opa.configuration;

import java.nio.file.Path;

public enum OpaPlatform {

    MAC_OS_AMD64("darwin_amd64", ""),
    LINUX_AMD64("linux_amd64_static", ""),
    WINDOWS_AMD64("windows_amd64", ".exe");

    private final String platformQualifier;
    private final String executableExtension;

    OpaPlatform(final String platformQualifier, final String executableExtension) {
        this.platformQualifier = platformQualifier;
        this.executableExtension = executableExtension;
    }

    public String getDownloadUrl(final String opaVersion) {
        return String.format("https://openpolicyagent.org/downloads/v%s/opa_%s%s",
                opaVersion,
                platformQualifier,
                executableExtension);
    }

    public Path getExecutablePath(final Path rootPath, final String version) {
        return rootPath.resolve("opa").resolve(version).resolve(String.format("opa%s", executableExtension));
    }

    public static OpaPlatform getPlatform() {
        final String osName = System.getProperty("os.name");
        final String osArch = System.getProperty("os.arch");
        if (osName.contains("win")) {
            if (osArch.equals("amd64")) {
                return WINDOWS_AMD64;
            }
        } else if (osName.contains("Mac")) {
            if (osArch.equals("x86_64")) {
                return MAC_OS_AMD64;
            }
        } else if (osName.contains("Linux")) {
            if (osArch.equals("amd64")) {
                return LINUX_AMD64;
            }
        }
        throw new IllegalStateException(String.format("Unsupported combination of OS/arch: %s/%s",
                osName,
                osArch));
    }
}
