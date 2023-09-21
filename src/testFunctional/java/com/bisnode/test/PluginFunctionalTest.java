package com.bisnode.test;

import com.bisnode.opa.configuration.ExecutableMode;
import com.bisnode.opa.configuration.OpaPlatform;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings({"DuplicatedCode", "VisibilityModifier"})
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
        String directory = tmpDir.getAbsolutePath();

        Path path = Paths.get(directory);
        Files.copy(new ByteArrayInputStream(getRegoPolicy().getBytes(UTF_8)), path.resolve("policy.rego"));
        Files.copy(new ByteArrayInputStream(getRegoPolicyTest().getBytes(UTF_8)), path.resolve("policy_test.rego"));

        Files.write(buildFile.toPath(), getOpaBlockConfig(directory).getBytes(UTF_8), StandardOpenOption.APPEND);

        BuildResult result = prepareRunner(new StringWriter(), "testRego").build();
        @Nullable BuildTask task = result.task(":testRego");

        assertEquals(1, result.getTasks().size());
        assertNotNull(task);
        assertEquals(TaskOutcome.SUCCESS, task.getOutcome());
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
    public void testRunningTestWithExistingReportsDirectoryWorks() throws IOException {
        String directory = tmpDir.getAbsolutePath();

        Path path = Paths.get(directory);
        Files.copy(new ByteArrayInputStream(getRegoPolicy().getBytes(UTF_8)), path.resolve("policy.rego"));
        Files.copy(new ByteArrayInputStream(getRegoPolicyTest().getBytes(UTF_8)), path.resolve("policy_test.rego"));

        Files.write(buildFile.toPath(), getOpaBlockConfig(directory).getBytes(UTF_8), StandardOpenOption.APPEND);

        Files.createDirectories(buildFile.toPath().getParent().resolve("build/test-results/opa"));

        BuildResult result = prepareRunner(new StringWriter(), "testRego").build();
        @Nullable BuildTask task = result.task(":testRego");

        assertEquals(1, result.getTasks().size());
        assertNotNull(task);
        assertEquals(TaskOutcome.SUCCESS, task.getOutcome());
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

        Files.write(buildFile.toPath(), getOpaBlockConfig(directory).getBytes(UTF_8), StandardOpenOption.APPEND);

        BuildResult result = prepareRunner(new StringWriter(), "testRego").buildAndFail();
        @Nullable BuildTask task = result.task(":testRego");

        assertEquals(1, result.getTasks().size());
        assertNotNull(task);
        assertEquals(TaskOutcome.FAILED, task.getOutcome());
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
    public void testProvidingTaskPropertiesOverridesDefaults() throws IOException {
        String directory = tmpDir.getAbsolutePath();
        Path path = Paths.get(directory);
        Path policyDirPath = path.resolve("policy");
        File policyDir = new File(policyDirPath.toString());
        assert policyDir.mkdir();

        Files.copy(new ByteArrayInputStream(getRegoPolicy().getBytes(UTF_8)), policyDirPath.resolve("policy.rego"));
        Files.copy(new ByteArrayInputStream(getRegoPolicyTest().getBytes(UTF_8)), policyDirPath.resolve("policy_test.rego"));

        String buildFileContent = getOpaBlockConfig("/tmp") + "\n\n" +
                "testRego {\n" +
                "    srcDir '" + policyDirPath.toAbsolutePath() + "'\n" +
                "    testDir '" + policyDirPath.toAbsolutePath() + "'\n" +
                "}";

        Files.write(buildFile.toPath(), buildFileContent.getBytes(UTF_8), StandardOpenOption.APPEND);

        BuildResult result = prepareRunner(new StringWriter(), "testRego").build();
        @Nullable BuildTask task = result.task(":testRego");

        assertEquals(1, result.getTasks().size());
        assertNotNull(task);
        assertEquals(TaskOutcome.SUCCESS, task.getOutcome());
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
    public void testRunningTestRegoCoverageTaskWithoutArgumentsWork() throws IOException {
        String directory = tmpDir.getAbsolutePath();
        String buildFileContent = "opa {\n" +
                "    srcDir '" + directory + "'\n" +
                "    testDir '" + directory + "'\n" +
                "}";
        Files.write(buildFile.toPath(), buildFileContent.getBytes(UTF_8), StandardOpenOption.APPEND);

        BuildResult result = prepareRunner(new StringWriter(), "testRegoCoverage").build();
        @Nullable BuildTask task = result.task(":testRegoCoverage");

        assertEquals(1, result.getTasks().size());
        assertNotNull(task);
        assertEquals(TaskOutcome.SUCCESS, Objects.requireNonNull(task).getOutcome());
    }

    @Test
    public void testRunningTestWithDownloadModeWorks() throws IOException {
        String directory = tmpDir.getAbsolutePath();
        String buildFileContent = "opa {\n" +
                "    mode '" + ExecutableMode.DOWNLOAD + "'\n" +
                "    version '0.54.0'\n" +
                "    srcDir '" + directory + "'\n" +
                "    testDir '" + directory + "'\n" +
                "}";
        Files.write(buildFile.toPath(), buildFileContent.getBytes(UTF_8), StandardOpenOption.APPEND);

        BuildResult result = prepareRunner(new StringWriter(), "testRegoCoverage").build();
        @Nullable BuildTask task = result.task(":testRegoCoverage");
        @Nullable BuildTask downloadTask = result.task(":downloadOpa_0.54.0");

        assertEquals(2, result.getTasks().size());
        assertNotNull(task);
        assertEquals(TaskOutcome.SUCCESS, Objects.requireNonNull(task).getOutcome());
        assertNotNull(downloadTask);
        assertEquals(TaskOutcome.SUCCESS, Objects.requireNonNull(downloadTask).getOutcome());
        assertTrue(Files.exists(OpaPlatform.getPlatform().getExecutablePath(tmpDir.toPath().resolve("build"), "0.54.0")));
    }

    @Test
    public void testDownloadModeInMultiModuleWorks() throws IOException {
        Files.delete(buildFile.toPath());
        String settingFileContent = "include 'module1', 'module2'";
        File settingFile = new File(tmpDir, "settings.gradle");
        Files.write(settingFile.toPath(), settingFileContent.getBytes(UTF_8));
        String subBuildFileContent = "plugins {\n" +
                "    id 'com.bisnode.opa'\n" +
                "}\n\n" +
                "opa {\n" +
                "    mode '" + ExecutableMode.DOWNLOAD + "'\n" +
                "    version '0.54.0'\n" +
                "    srcDir 'src'\n" +
                "    testDir 'test'\n" +
                "}";
        File module1Directory = new File(tmpDir, "module1");
        Files.createDirectories(module1Directory.toPath().resolve("src"));
        Files.createDirectories(module1Directory.toPath().resolve("test"));
        File module1BuildFile = new File(module1Directory, "build.gradle");
        Files.write(module1BuildFile.toPath(), subBuildFileContent.getBytes(UTF_8));
        File module2Directory = new File(tmpDir, "module2");
        Files.createDirectories(module2Directory.toPath().resolve("src"));
        Files.createDirectories(module2Directory.toPath().resolve("test"));
        File module2BuildFile = new File(module2Directory, "build.gradle");
        Files.write(module2BuildFile.toPath(), subBuildFileContent.getBytes(UTF_8));

        BuildResult result = prepareRunner(new StringWriter(), "testRegoCoverage").build();
        @Nullable BuildTask taskModule1 = result.task(":module1:testRegoCoverage");
        @Nullable BuildTask taskModule2 = result.task(":module2:testRegoCoverage");
        @Nullable BuildTask downloadTask = result.task(":downloadOpa_0.54.0");

        assertEquals(3, result.getTasks().size());
        assertNotNull(taskModule1);
        assertEquals(TaskOutcome.SUCCESS, Objects.requireNonNull(taskModule1).getOutcome());
        assertNotNull(taskModule2);
        assertEquals(TaskOutcome.SUCCESS, Objects.requireNonNull(taskModule2).getOutcome());
        assertNotNull(downloadTask);
        assertEquals(TaskOutcome.SUCCESS, Objects.requireNonNull(downloadTask).getOutcome());
        assertTrue(Files.exists(OpaPlatform.getPlatform().getExecutablePath(tmpDir.toPath().resolve("build"), "0.54.0")));
    }

    private GradleRunner prepareRunner(StringWriter writer, String... tasks) {
        return GradleRunner.create()
                .withProjectDir(tmpDir)
                .forwardStdOutput(writer)
                .forwardStdError(writer)
                .withPluginClasspath()
                .withArguments(tasks);
    }

    private static String getOpaBlockConfig(String directory) {
        return "opa {\n" +
                "    srcDir '" + directory + "'\n" +
                "    testDir '" + directory + "'\n" +
                "}";
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
