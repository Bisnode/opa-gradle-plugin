package com.bisnode.opa;

final class OpaPluginTestUtils {

    private OpaPluginTestUtils() {
    }

    static String getRegoPolicy() {
        return "package test\n" +
                "\n" +
                "default allow = false";
    }

    static String getRegoPolicyTest() {
        return "package test\n" +
                "\n" +
                "test_allow_is_false {\n" +
                "    not allow\n" +
                "}";
    }

}
