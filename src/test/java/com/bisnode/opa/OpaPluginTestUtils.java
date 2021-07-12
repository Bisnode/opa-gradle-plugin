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

    static String getManyRegoPolicyTests() {
        StringBuilder regoTestsBuilder = new StringBuilder();
        for(int i = 0; i < 300; i++) {
            regoTestsBuilder.append("\n");
            regoTestsBuilder.append("test_allow_is_false_" + i + " {\n");
            regoTestsBuilder.append("    not allow\n");
            regoTestsBuilder.append("}");
        }

        return "package test\n" + regoTestsBuilder;
    }

}
