package gca.caps.doc.maven.plugin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogDocAggregatorMojoTest {

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        Path sourceDir = Path.of("src/test/resources/test-multi-module-project");
        Path targetDir = tempDir.resolve("test-multi-module-project");
        copyDirectory(sourceDir, targetDir);
    }

    private void copyDirectory(Path source, Path target) throws IOException {
        try (Stream<Path> walk = Files.walk(source)) {
            walk.forEach(sourcePath -> {
                try {
                    Path targetPath = target.resolve(source.relativize(sourcePath));
                    if (Files.isDirectory(sourcePath)) {
                        Files.createDirectories(targetPath);
                    } else {
                        Files.copy(sourcePath, targetPath);
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Erreur lors de la copie du répertoire", e);
                }
            });
        }
    }

    @Test
    void testMultiModuleProject() throws Exception {
        //Given
        Path outputDirModuleA = tempDir.resolve("test-multi-module-project/module-a/target/logdoc");

        File pomModuleA = new File(tempDir.toFile(), "test-multi-module-project/module-a/pom.xml");
        MavenProjectLoader.SimpleProject projectA = MavenProjectLoader.loadProject(pomModuleA);
        LogDocMojo logDocMojoModuleA = new LogDocMojo();
        logDocMojoModuleA.setOutputDirectory(outputDirModuleA.toFile());

        logDocMojoModuleA.execute(projectA.basedir());

        Path outputDirModuleB = tempDir.resolve("test-multi-module-project/module-b/target/logdoc");
        File pomModuleB = new File(tempDir.toFile(), "test-multi-module-project/module-b/pom.xml");
        MavenProjectLoader.SimpleProject projectB = MavenProjectLoader.loadProject(pomModuleB);
        LogDocMojo logDocMojoModuleB = new LogDocMojo();
        logDocMojoModuleB.setOutputDirectory(outputDirModuleB.toFile());

        logDocMojoModuleB.execute(projectB.basedir());


        Path outputDir = tempDir.resolve("test-multi-module-project/target/logdoc");
        File pom = new File(tempDir.toFile(), "test-multi-module-project/pom.xml");
        MavenProjectLoader.SimpleProject project = MavenProjectLoader.loadProject(pom);
        LogDocAggregatorMojo aggregatorMojo = new LogDocAggregatorMojo();

        aggregatorMojo.setOutputDirectory(outputDir.toFile());

        //When
        aggregatorMojo.execute(project.basedir(), project.model());

        //Then

        assertTrue(Files.exists(outputDir), "Répertoire logdoc manquant");
        assertTrue(Files.exists(outputDir.resolve("index.md")), "index.md global manquant");

        // module-a
        Path moduleAIndex = outputDir.resolve("module-a/index.md");
        Path moduleADoc1 = outputDir.resolve("module-a/trace-dictionary.md");
        assertTrue(Files.exists(moduleAIndex), "index.md module-a manquant");
        assertTrue(Files.exists(moduleADoc1), "trace-dictionary.md manquant dans module-a");

        var expectedResultModuleADoc1 = readResourceFile("module-a-dictionary-expected-file.md");
        var contentModuleADoc1 = Files.readString(moduleADoc1).trim();
        assertThat(contentModuleADoc1).isEqualToNormalizingNewlines(expectedResultModuleADoc1);

        // module-b
        Path moduleBIndex = outputDir.resolve("module-b/index.md");
        Path moduleBDoc1 = outputDir.resolve("module-b/trace-dictionary.md");
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