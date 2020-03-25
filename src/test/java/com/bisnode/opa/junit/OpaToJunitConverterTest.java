package com.bisnode.opa.junit;

import org.gradle.internal.impldep.com.google.common.io.Files;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class OpaToJunitConverterTest {

    private static final String opaTestJson = "test/resources/opa-test-output.json";

    private static Element document;

    @BeforeClass
    public static void setupClass() {
        String testJson = getOpaTestJson();
        document = OpaToJunitConverter.fromOpaTestJson(testJson).getDocumentElement();
    }

    @Test
    public void conversionCreatesTestSuites() {
        assertThat(document.getTagName(), is("testsuites"));
        assertThat(document.getAttribute("tests"), is("22"));
        assertThat(document.getAttribute("errors"), is("1"));
        assertThat(document.getAttribute("failures"), is("6"));
        assertThat(document.getAttribute("time"), is("0.015"));
    }

    @Test
    public void conversionCreatesTestSuiteTestCases() {
        Node testsuite = document.getChildNodes().item(0);
        assertThat(testsuite.getNodeName(), is("testsuite"));

        List<Element> testcases = asElementList(document.getFirstChild().getChildNodes());

        assertThat(testcases.size(), is(22));

        long errors = testcases.stream()
                .filter(el -> el.getFirstChild() != null && "error".equals(el.getFirstChild().getNodeName()))
                .count();

        assertThat(errors, is(1L));

        long failures = testcases.stream()
                .filter(el -> el.getFirstChild() != null && "failure".equals(el.getFirstChild().getNodeName()))
                .count();

        assertThat(failures, is(6L));
    }

    private static String getOpaTestJson() {
        @Nullable URL url = OpaToJunitConverterTest.class.getResource(opaTestJson);
        try {
            if (url == null) {
                url = new File(System.getProperty("user.dir") + "/src/test/resources/opa-test-output.json").toURI().toURL();
            }
            return String.join("\n", Files.readLines(new File(url.toURI()), UTF_8));
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<Element> asElementList(NodeList nodeList) {
        List<Element> elements = new ArrayList<>(64);
        int length = nodeList.getLength();
        for (int i = 0; i < length; i++) {
            if (nodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                elements.add((Element) nodeList.item(i));
            }
        }
        return elements;
    }

}
