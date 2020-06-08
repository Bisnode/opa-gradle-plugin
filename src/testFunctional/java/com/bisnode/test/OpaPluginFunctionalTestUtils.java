package com.bisnode.test;

final class OpaPluginFunctionalTestUtils {

    private OpaPluginFunctionalTestUtils() {
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

    static String getRegoPolicyTestFail() {
        return "package test\n" +
                "\n" +
                "test_allow_is_false {\n" +
                "    allow\n" +
                "}";
    }

}
