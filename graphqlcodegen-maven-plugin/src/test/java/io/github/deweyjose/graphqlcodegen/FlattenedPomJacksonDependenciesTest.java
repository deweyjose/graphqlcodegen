package io.github.deweyjose.graphqlcodegen;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Guards issue #353: jackson-databind 2.22+ references {@code JsonSerializeAs} (added in
 * jackson-annotations 2.21). The published POM is flattened with {@code flattenMode=ossrh}, which
 * strips {@code dependencyManagement}, so Jackson must be declared as direct dependencies or Maven
 * mediation of {@code graphql-dgs-codegen-core}'s older jackson-annotations wins.
 */
class FlattenedPomJacksonDependenciesTest {

  private static final String JACKSON_GROUP = "com.fasterxml.jackson.core";
  private static final String[] REQUIRED_ARTIFACTS = {
    "jackson-databind", "jackson-annotations", "jackson-core"
  };

  @Test
  void sourcePomDeclaresJacksonArtifactsAsDirectDependencies() throws Exception {
    File pom = new File("pom.xml");
    assertTrue(pom.isFile(), "Expected plugin pom.xml at " + pom.getAbsolutePath());

    Map<String, String> direct = directJacksonDependencies(parse(pom));
    for (String artifact : REQUIRED_ARTIFACTS) {
      assertTrue(
          direct.containsKey(artifact),
          artifact
              + " must be a direct <dependency> so it survives flattenMode=ossrh"
              + " (found: "
              + direct.keySet()
              + ")");
    }
  }

  @Test
  void jsonSerializeAsIsLoadable() {
    // Canary that the reactor classpath itself has annotations >= 2.21.
    assertNotNull(com.fasterxml.jackson.annotation.JsonSerializeAs.class);
  }

  @Test
  void flattenedPomKeepsJacksonDirectDependenciesWithVersions() throws Exception {
    File flattened = new File("target/flattened-pom/.flattened-pom.xml");
    assumeTrue(
        flattened.isFile(),
        "Flattened POM is produced in process-resources; skip when tests run without Maven");

    Document doc = parse(flattened);
    assertFalse(
        hasDependencyManagement(doc),
        "ossrh flatten mode should strip dependencyManagement; if it is present the published"
            + " POM may hide Jackson mediation bugs again");

    Map<String, String> direct = directJacksonDependencies(doc);
    for (String artifact : REQUIRED_ARTIFACTS) {
      String version = direct.get(artifact);
      assertTrue(
          version != null && !version.isBlank(),
          artifact
              + " must appear in the flattened POM with an interpolated version"
              + " (found: "
              + direct
              + ")");
    }

    String annotationsVersion = direct.get("jackson-annotations");
    assertTrue(
        isAtLeast(annotationsVersion, 2, 21),
        "jackson-annotations must be >= 2.21 for JsonSerializeAs, was " + annotationsVersion);
  }

  private static Document parse(File pom) throws Exception {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    return factory.newDocumentBuilder().parse(pom);
  }

  private static Map<String, String> directJacksonDependencies(Document doc) throws Exception {
    XPath xpath = XPathFactory.newInstance().newXPath();
    NodeList nodes =
        (NodeList)
            xpath.evaluate(
                "/*[local-name()='project']/*[local-name()='dependencies']"
                    + "/*[local-name()='dependency']",
                doc,
                XPathConstants.NODESET);
    Map<String, String> jackson = new LinkedHashMap<>();
    for (int i = 0; i < nodes.getLength(); i++) {
      Element dep = (Element) nodes.item(i);
      if (!JACKSON_GROUP.equals(childText(dep, "groupId"))) {
        continue;
      }
      jackson.put(childText(dep, "artifactId"), childText(dep, "version"));
    }
    return jackson;
  }

  private static boolean hasDependencyManagement(Document doc) throws Exception {
    XPath xpath = XPathFactory.newInstance().newXPath();
    NodeList nodes =
        (NodeList)
            xpath.evaluate(
                "/*[local-name()='project']/*[local-name()='dependencyManagement']",
                doc,
                XPathConstants.NODESET);
    return nodes.getLength() > 0;
  }

  private static String childText(Element parent, String localName) {
    NodeList children = parent.getElementsByTagNameNS("*", localName);
    if (children.getLength() == 0) {
      children = parent.getElementsByTagName(localName);
    }
    if (children.getLength() == 0) {
      return "";
    }
    return children.item(0).getTextContent().trim();
  }

  private static boolean isAtLeast(String version, int major, int minor) {
    if (version == null || version.isBlank()) {
      return false;
    }
    String[] parts = version.split("[.-]");
    int vMajor = parts.length > 0 ? parseIntOrZero(parts[0]) : 0;
    int vMinor = parts.length > 1 ? parseIntOrZero(parts[1]) : 0;
    return vMajor > major || (vMajor == major && vMinor >= minor);
  }

  private static int parseIntOrZero(String part) {
    try {
      return Integer.parseInt(part);
    } catch (NumberFormatException e) {
      return 0;
    }
  }
}
