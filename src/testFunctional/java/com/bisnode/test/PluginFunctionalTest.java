package com.bisnode.test;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.BuildTask;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
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
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@SuppressWarnings({"resource", "DuplicatedCode"})
public class PluginFunctionalTest {

    @Rule
    public TemporaryFolder testProjectDir = new TemporaryFolder();
    private File buildFile;

    @Before
    public void setup() throws IOException {
        String buildFileContent = "plugins {\n" +
                "    id 'com.bisnode.opa'\n" +
                "}\n";
        buildFile = testProjectDir.newFile("build.gradle");
        Files.write(buildFile.toPath(), buildFileContent.getBytes(UTF_8));
    }

    @Test
    public void testRunningSuccessfulTestProducesJUnitXMLOutput() throws IOException {
        String directory = testProjectDir.getRoot().getAbsolutePath();

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

        assertThat(task, is(notNullValue()));
        assertThat(Objects.requireNonNull(task).getOutcome(), is(TaskOutcome.SUCCESS));
        assertThat(path.toFile(), notNullValue());
        assertThat(path.toFile().listFiles(), notNullValue());

        Path testResultPath = path.resolve("build/test-results/opa");
        assertThat(testResultPath.toFile(), notNullValue());
        assertThat(testResultPath.toFile().exists(), is(true));

        Path opaJunitXMLReportPath = testResultPath.resolve("TEST-opa-tests.xml");
        assertThat(opaJunitXMLReportPath.toFile(), notNullValue());
        assertThat(opaJunitXMLReportPath.toFile().exists(), is(true));

        Document document = readXmlDocument(opaJunitXMLReportPath.toFile());

        Function<String, String> attributes = attributeRetriever(document);

        assertThat(attributes.apply("tests"), is("1"));
        assertThat(attributes.apply("errors"), is("0"));
        assertThat(attributes.apply("failures"), is("0"));
    }

    @Test
    public void testRunningFailingTestProducesJUnitXMLOutput() throws IOException {
        String directory = testProjectDir.getRoot().getAbsolutePath();

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

        assertThat(task, is(notNullValue()));
        assertThat(Objects.requireNonNull(task).getOutcome(), is(TaskOutcome.FAILED));
        assertThat(path.toFile(), notNullValue());
        assertThat(path.toFile().listFiles(), notNullValue());

        Path testResultPath = path.resolve("build/test-results/opa");
        assertThat(testResultPath.toFile(), notNullValue());
        assertThat(testResultPath.toFile().exists(), is(true));

        Path opaJunitXMLReportPath = testResultPath.resolve("TEST-opa-tests.xml");
        assertThat(opaJunitXMLReportPath.toFile(), notNullValue());
        assertThat(opaJunitXMLReportPath.toFile().exists(), is(true));

        Document document = readXmlDocument(opaJunitXMLReportPath.toFile());

        Function<String, String> attributes = attributeRetriever(document);

        assertThat(attributes.apply("tests"), is("1"));
        assertThat(attributes.apply("errors"), is("0"));
        assertThat(attributes.apply("failures"), is("1"));
    }

    @Test
    public void testRunningTestRegoCoverageTaskWithoutArgumentsWork() throws IOException {
        String directory = testProjectDir.getRoot().getAbsolutePath();
        String buildFileContent = "opa {\n" +
                "    srcDir '" + directory + "'\n" +
                "    testDir '" + directory + "'\n" +
                "}";
        Files.write(buildFile.toPath(), buildFileContent.getBytes(UTF_8), StandardOpenOption.APPEND);

        BuildResult result =prepareRunner(new StringWriter(), "testRegoCoverage").build();
        @Nullable BuildTask task = result.task(":testRegoCoverage");

        assertThat(task, is(notNullValue()));
        assertThat(Objects.requireNonNull(task).getOutcome(), is(TaskOutcome.SUCCESS));
    }

    private GradleRunner prepareRunner(StringWriter writer, String... tasks) {
        return GradleRunner.create()
                .withProjectDir(testProjectDir.getRoot())
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
