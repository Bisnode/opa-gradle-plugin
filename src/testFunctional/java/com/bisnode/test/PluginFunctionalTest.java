package com.bisnode.test;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.BuildTask;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.annotation.Nullable;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.function.Function;

import static com.bisnode.test.OpaPluginFunctionalTestUtils.getRegoPolicy;
import static com.bisnode.test.OpaPluginFunctionalTestUtils.getRegoPolicyTest;
import static com.bisnode.test.OpaPluginFunctionalTestUtils.getRegoPolicyTestFail;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings({"DuplicatedCode"})
public class PluginFunctionalTest {

    @TempDir
    File tmpDir;

    private File buildFile;

    @BeforeEach
    public void setup() throws IOException {
        String buildFileContent = "plugins {\n" +
                "    id 'com.bisnode.opa'\n" +
                "}\n";

        buildFile = new File(tmpDir, "build.gradle");
        Files.write(buildFile.toPath(), buildFileContent.getBytes(UTF_8));
    }

    @Test
    public void testRunningSuccessfulTestProducesJUnitXMLOutput() throws IOException {
        String directory =  tmpDir.getAbsolutePath();

        Path path = Paths.get(directory);
        Files.copy(new ByteArrayInputStream(getRegoPolicy().getBytes(UTF_8)), path.resolve("policy.rego"));
        Files.copy(new ByteArrayInputStream(getRegoPolicyTest().getBytes(UTF_8)), path.resolve("policy_test.rego"));

        String buildFileContent = "opa {\n" +
                "    srcDir '" + directory + "'\n" +
                "    testDir '" + directory + "'\n" +
                "}";
        Files.write(buildFile.toPath(), buildFileContent.getBytes(UTF_8), StandardOpenOption.APPEND);

        BuildResult result = prepareRunner(new StringWriter(), "testRego").build();
        @Nullable BuildTask task = result.task(":testRego");

        assertNotNull(task);
        assertEquals(task.getOutcome(), TaskOutcome.SUCCESS);
        assertNotNull(path.toFile());
        assertNotNull(path.toFile().listFiles());

        Path testResultPath = path.resolve("build/test-results/opa");
        assertNotNull(testResultPath.toFile());
        assertTrue(testResultPath.toFile().exists());

        Path opaJunitXMLReportPath = testResultPath.resolve("TEST-opa-tests.xml");
        assertNotNull(opaJunitXMLReportPath.toFile());
        assertTrue(opaJunitXMLReportPath.toFile().exists());

        Document document = readXmlDocument(opaJunitXMLReportPath.toFile());

        Function<String, String> attributes = attributeRetriever(document);

        assertEquals("1", attributes.apply("tests"));
        assertEquals("0", attributes.apply("errors"));
        assertEquals("0", attributes.apply("failures"));
    }

    @Test
    public void testRunningFailingTestProducesJUnitXMLOutput() throws IOException {
        String directory = tmpDir.getAbsolutePath();

        Path path = Paths.get(directory);
        Files.copy(new ByteArrayInputStream(getRegoPolicy().getBytes(UTF_8)), path.resolve("policy.rego"));
        Files.copy(new ByteArrayInputStream(getRegoPolicyTestFail().getBytes(UTF_8)), path.resolve("policy_test.rego"));

        String buildFileContent = "opa {\n" +
                "    srcDir '" + directory + "'\n" +
                "    testDir '" + directory + "'\n" +
                "}";
        Files.write(buildFile.toPath(), buildFileContent.getBytes(UTF_8), StandardOpenOption.APPEND);

        BuildResult result = prepareRunner(new StringWriter(), "testRego").buildAndFail();
        @Nullable BuildTask task = result.task(":testRego");

        assertNotNull(task);
        assertEquals(task.getOutcome(), TaskOutcome.FAILED);
        assertNotNull(path.toFile());
        assertNotNull(path.toFile().listFiles());

        Path testResultPath = path.resolve("build/test-results/opa");
        assertNotNull(testResultPath.toFile());
        assertTrue(testResultPath.toFile().exists());

        Path opaJunitXMLReportPath = testResultPath.resolve("TEST-opa-tests.xml");
        assertNotNull(opaJunitXMLReportPath.toFile());
        assertTrue(opaJunitXMLReportPath.toFile().exists());

        Document document = readXmlDocument(opaJunitXMLReportPath.toFile());

        Function<String, String> attributes = attributeRetriever(document);

        assertEquals("1", attributes.apply("tests"));
        assertEquals("0", attributes.apply("errors"));
        assertEquals("1", attributes.apply("failures"));
    }

    @Test
    public void testRunningTestRegoCoverageTaskWithoutArgumentsWork() throws IOException {
        String directory = tmpDir.getAbsolutePath();
        String buildFileContent = "opa {\n" +
                "    srcDir '" + directory + "'\n" +
                "    testDir '" + directory + "'\n" +
                "}";
        Files.write(buildFile.toPath(), buildFileContent.getBytes(UTF_8), StandardOpenOption.APPEND);

        BuildResult result =prepareRunner(new StringWriter(), "testRegoCoverage").build();
        @Nullable BuildTask task = result.task(":testRegoCoverage");

        assertNotNull(task);
        assertEquals(Objects.requireNonNull(task).getOutcome(), TaskOutcome.SUCCESS);
    }

    private GradleRunner prepareRunner(StringWriter writer, String... tasks) {
        return GradleRunner.create()
                .withProjectDir(tmpDir)
                .forwardStdOutput(writer)
                .forwardStdError(writer)
                .withPluginClasspath()
                .withArguments(tasks);
    }

    private static Document readXmlDocument(File file) {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

            return documentBuilder.parse(file);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Function<String, String> attributeRetriever(Document document) {
        return attribute -> document.getElementsByTagName("testsuites")
                .item(0)
                .getAttributes()
                .getNamedItem(attribute)
                .getNodeValue();
    }

}
