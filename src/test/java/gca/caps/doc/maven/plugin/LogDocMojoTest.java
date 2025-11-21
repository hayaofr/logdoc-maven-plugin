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

class LogDocMojoTest {

    @TempDir
    Path tempDir;

    @Test
    void testSimpleProject() throws Exception {
        File pom = new File("src/test/resources/test-simple-project/pom.xml");
        MavenProjectLoader.SimpleProject project = MavenProjectLoader.loadProject(pom);
        LogDocMojo mojo = new LogDocMojo();
        Path outputDir = tempDir.resolve("logdoc");

        mojo.setOutputDirectory(outputDir.toFile());
        mojo.execute(project.basedir());

        // ───────────────────────────────
        // 4️⃣ Vérifications
        // ───────────────────────────────
        Path docsRoot = tempDir.resolve("logdoc/test-simple-project");
        assertTrue(Files.exists(docsRoot), "Répertoire test-docs manquant");
        assertTrue(Files.exists(docsRoot.resolve("index.md")), "index.md global manquant");

        Path doc1 = docsRoot.resolve("trace-dictionary.md");
        assertTrue(Files.exists(doc1), "trace-dictionary.md manquant");

        var expectedResultDoc1 = readResourceFile("simple-project-dictionary-expected-file.md");
        var contentDoc1 = Files.readString(doc1).trim();
        assertThat(contentDoc1).isEqualToNormalizingNewlines(expectedResultDoc1);

    }

    private static String readResourceFile(String resourcePath) throws IOException, URISyntaxException {
        Path path = Path.of(LogDocMojoTest.class.getClassLoader().getResource(resourcePath).toURI());
        return Files.readString(path).trim();
    }

}
