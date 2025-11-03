package gca.caps.doc.maven.plugin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestDocMojoFullLog {


    @TempDir
    Path tempDir;

    @Test
    void testMultiModuleProject() throws Exception {
        File pom = new File("src/test/resources/test-multi-module-project/pom.xml");
        MavenProjectLoader.SimpleProject project = MavenProjectLoader.loadProject(pom);
        LogDocMojo mojo = new LogDocMojo();
        Path outputDir = tempDir.resolve("log-docs");

        mojo.setOutputDirectory(outputDir.toFile());
        mojo.execute(project.basedir(), project.model());

        // ───────────────────────────────
        // 4️⃣ Vérifications
        // ───────────────────────────────
        Path docsRoot = tempDir.resolve("log-docs");
        assertTrue(Files.exists(docsRoot), "Répertoire log-docs manquant");
        assertTrue(Files.exists(docsRoot.resolve("index.md")), "index.md global manquant");

        // module-a
        Path moduleAIndex = docsRoot.resolve("module-a/index.md");
        Path moduleADoc1 = docsRoot.resolve("module-a/trace-dictionary.md");
        assertTrue(Files.exists(moduleAIndex), "index.md module-a manquant");
        assertTrue(Files.exists(moduleADoc1), "trace-dictionary.md manquant dans module-a");

        var expectedResultModuleADoc1 = readResourceFile("module-a-dictionary-expected-file.md");
        var contentModuleADoc1 = Files.readString(moduleADoc1).trim();
        assertThat(contentModuleADoc1).isEqualToNormalizingNewlines(expectedResultModuleADoc1);

        // module-b
        Path moduleBIndex = docsRoot.resolve("module-b/index.md");
        Path moduleBDoc1 = docsRoot.resolve("module-b/trace-dictionary.md");
        assertTrue(Files.exists(moduleBIndex), "index.md module-b manquant");
        assertTrue(Files.exists(moduleBDoc1), "trace-dictionary.md manquant dans module-b");

        var expectedResultModuleBDoc1 = readResourceFile("module-b-dictionary-expected-file.md");
        var contentModuleBDoc1 = Files.readString(moduleBDoc1).trim();
        assertThat(contentModuleBDoc1).isEqualToNormalizingNewlines(expectedResultModuleBDoc1);

    }

    @Test
    void testSimpleProject() throws Exception {
        File pom = new File("src/test/resources/test-simple-project/pom.xml");
        MavenProjectLoader.SimpleProject project = MavenProjectLoader.loadProject(pom);
        LogDocMojo mojo = new LogDocMojo();
        Path outputDir = tempDir.resolve("log-docs");

        mojo.setOutputDirectory(outputDir.toFile());
        mojo.execute(project.basedir(), project.model());

        // ───────────────────────────────
        // 4️⃣ Vérifications
        // ───────────────────────────────
        Path docsRoot = tempDir.resolve("log-docs");
        assertTrue(Files.exists(docsRoot), "Répertoire test-docs manquant");
        assertTrue(Files.exists(docsRoot.resolve("index.md")), "index.md global manquant");

        Path doc1 = docsRoot.resolve("trace-dictionary.md");
        assertTrue(Files.exists(doc1), "trace-dictionary.md manquant");

        var expectedResultDoc1 = readResourceFile("simple-project-dictionary-expected-file.md");
        var contentDoc1 = Files.readString(doc1).trim();
        assertThat(contentDoc1).isEqualToNormalizingNewlines(expectedResultDoc1);

    }

    private static String readResourceFile(String resourcePath) throws IOException, URISyntaxException {
        Path path = Path.of(TestDocMojoFullLog.class.getClassLoader().getResource(resourcePath).toURI());
        return Files.readString(path).trim();
    }

}
