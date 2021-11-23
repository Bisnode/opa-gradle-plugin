package com.bisnode.opa.configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Environment {

    private static final Map<String, String> environment;
    static {
        environment = new HashMap<>(System.getenv());
    }

    public static void put(String key, String value) {
        environment.put(key, value);
    }

    public static Optional<String> get(String key) {
        return Optional.ofNullable(environment.get(key));
    }

}
