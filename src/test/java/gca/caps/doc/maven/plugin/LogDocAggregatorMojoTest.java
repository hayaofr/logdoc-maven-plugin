package gca.caps.doc.maven.plugin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class LogDocAggregatorMojoTest {

    @TempDir
    Path tempDir;

    @Test
    void testMultiModuleProject() throws Exception {
        //Given
        Path outputDir = tempDir.resolve("log-docs");

        File pomModuleA = new File("src/test/resources/test-multi-module-project/module-a/pom.xml");
        MavenProjectLoader.SimpleProject projectA = MavenProjectLoader.loadProject(pomModuleA);
        LogDocMojo logDocMojoModuleA = new LogDocMojo();
        logDocMojoModuleA.setOutputDirectory(outputDir.toFile());

        logDocMojoModuleA.execute(projectA.basedir());

        File pomModuleB = new File("src/test/resources/test-multi-module-project/module-b/pom.xml");
        MavenProjectLoader.SimpleProject projectB = MavenProjectLoader.loadProject(pomModuleB);
        LogDocMojo logDocMojoModuleB = new LogDocMojo();
        logDocMojoModuleB.setOutputDirectory(outputDir.toFile());

        logDocMojoModuleB.execute(projectB.basedir());


        LogDocAggregatorMojo aggregatorMojo = new LogDocAggregatorMojo();

        aggregatorMojo.setOutputDirectory(outputDir.toFile());

        //When
        aggregatorMojo.execute();

        //Then

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

    private static String readResourceFile(String resourcePath) throws IOException, URISyntaxException {
        Path path = Path.of(LogDocAggregatorMojoTest.class.getClassLoader().getResource(resourcePath).toURI());
        return Files.readString(path).trim();
    }
}