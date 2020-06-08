package com.bisnode.opa.junit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.annotation.Nullable;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

public final class OpaToJunitConverter {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private OpaToJunitConverter() {
    }

    public static Document fromOpaTestJson(String opaTestJson) {
        List<OpaTestCase> opaTestCases = getOpaTestCases(opaTestJson);

        Document document = createDocument();
        Element testsuites = document.createElement("testsuites");
        document.appendChild(testsuites);

        Map<String, Map<String, Long>> perSuiteCounter = new HashMap<>(8);
        Map<String, List<Element>> testcasesBySuite = new HashMap<>(32);

        for (OpaTestCase opaTestCase : opaTestCases) {
            String pkg = opaTestCase.getPackage();

            if (!testcasesBySuite.containsKey(pkg)) {
                testcasesBySuite.put(pkg, new ArrayList<>(64));
            }

            Element testCase = document.createElement("testcase");
            testCase.setAttribute("name", opaTestCase.getName());
            testCase.setAttribute("classname", opaTestCase.getLocation().getFile());
            testCase.setAttribute("time", nanosToSeconds(opaTestCase.getDuration()));

            if (!perSuiteCounter.containsKey(pkg)) {
                perSuiteCounter.put(pkg, createCounter());
            }

            Map<String, Long> counter = perSuiteCounter.get(pkg);

            counter.computeIfPresent("tests", (k, v) -> v + 1);
            counter.computeIfPresent("time", (k, v) -> v + opaTestCase.getDuration());
            if (Boolean.TRUE == opaTestCase.getFailure()) {
                counter.computeIfPresent("failures", (k, v) -> v + 1);
                testCase.appendChild(document.createElement("failure"));
            }
            if (opaTestCase.getError() != null) {
                counter.computeIfPresent("errors", (k, v) -> v + 1);

                Element error = document.createElement("error");
                error.setAttribute("message", opaTestCase.getError().getMessage());
                error.setAttribute("type", opaTestCase.getError().getCode());
                testCase.appendChild(error);
            }

            testcasesBySuite.get(pkg).add(testCase);
        }

        testcasesBySuite.forEach((name, tests) -> {
            Element suite = document.createElement("testsuite");
            suite.setAttribute("name", name);
            suite.setAttribute("hostname", getHostName());
            tests.forEach(suite::appendChild);
            Map<String, Long> counter = perSuiteCounter.get(name);
            Stream.of("tests", "failures", "errors")
                    .forEach(attr -> suite.setAttribute(attr, String.valueOf(counter.get(attr))));
            suite.setAttribute("time", nanosToSeconds(counter.get("time")));
            testsuites.appendChild(suite);
        });

        Stream.of("tests", "failures", "errors")
                .forEach(attr -> testsuites.setAttribute(attr, String.valueOf(sumCounter(attr, perSuiteCounter))));
        testsuites.setAttribute("time", nanosToSeconds(sumCounter("time", perSuiteCounter)));

        return document;
    }

    public static void write(Document document, OutputStream outputStream) {
        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult(outputStream);
        try {
            createTransformer().transform(source, result);
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("OverlyBroadCatchBlock")
    private static List<OpaTestCase> getOpaTestCases(@Nullable String opaTestJson) {
        if (opaTestJson == null || opaTestJson.trim().equals("null") || opaTestJson.trim().isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return Arrays.asList(objectMapper.readValue(opaTestJson, OpaTestCase[].class));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "localhost";
        }
    }

    private static long sumCounter(String type, Map<String, ? extends Map<String, Long>> counters) {
        return counters.values().stream()
                .map(t -> t.get(type))
                .mapToLong(Long::longValue).sum();
    }

    private static String nanosToSeconds(long nanos) {
        return String.format(Locale.US, "%.3f", nanos / 1_000_000_000.0);
    }

    private static Map<String, Long> createCounter() {
        Map<String, Long> counter = new HashMap<>(4);
        counter.put("tests", 0L);
        counter.put("errors", 0L);
        counter.put("failures", 0L);
        counter.put("time", 0L);

        return counter;
    }

    private static Document createDocument() {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dbuilder;
        try {
            dbuilder = dbFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }

        return dbuilder.newDocument();
    }

    private static Transformer createTransformer() {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        try {
            return transformerFactory.newTransformer();
        } catch (TransformerConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unused")
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class OpaTestCase {
        private final Location location;
        private final String pkg;
        private final String name;
        private final long duration;
        @Nullable
        private final Boolean failure;
        @Nullable
        private final Error error;

        OpaTestCase(
                @JsonProperty("location") Location location,
                @JsonProperty("package") String pkg,
                @JsonProperty("name") String name,
                @JsonProperty("duration") long duration,
                @JsonProperty("fail") @Nullable Boolean failure,
                @JsonProperty("error") @Nullable Error error
        ) {
            this.location = location;
            this.pkg = pkg;
            this.name = name;
            this.duration = duration;
            this.failure = failure;
            this.error = error;
        }

        public Location getLocation() {
            return location;
        }

        public String getPackage() {
            return pkg;
        }

        public String getName() {
            return name;
        }

        public long getDuration() {
            return duration;
        }

        @Nullable
        public Boolean getFailure() {
            return failure;
        }

        @Nullable
        public Error getError() {
            return error;
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        static class Location {
            private final String file;
            private final long row;
            private final long col;

            Location(
                    @JsonProperty("file") String file,
                    @JsonProperty("row") long row,
                    @JsonProperty("col") long col
            ) {
                this.file = file;
                this.row = row;
                this.col = col;
            }

            public String getFile() {
                return file;
            }

            public long getRow() {
                return row;
            }

            public long getCol() {
                return col;
            }
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        static class Error {
            private final String code;
            private final String message;
            private final Location location;

            Error(
                    @JsonProperty("code") String code,
                    @JsonProperty("message") String message,
                    @JsonProperty("location") Location location
            ) {
                this.code = code;
                this.message = message;
                this.location = location;
            }

            public String getCode() {
                return code;
            }

            public String getMessage() {
                return message;
            }

            public Location getLocation() {
                return location;
            }
        }

    }

}
