package gca.caps.doc.maven.plugin;

import org.apache.maven.model.Model;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Mojo(name = "aggregate-logdoc", defaultPhase = LifecyclePhase.VERIFY, threadSafe = true, aggregator = true)
public class LogDocAggregatorMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter(defaultValue = "${project.build.directory}/logdoc", required = true)
    private File outputDirectory;

    public void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public void execute() throws MojoExecutionException {
        execute(project.getBasedir(), project.getModel());
    }

    protected void execute(File baseDir, Model model) throws MojoExecutionException {
        try {
            if (!outputDirectory.exists()) {
                Files.createDirectories(outputDirectory.toPath());
            }

            List<String> moduleLinks = new ArrayList<>();

            // Parcourir les modules du projet
            for (var module :model.getModules()) {
                File moduleDir = new File(baseDir, module);
                File logDocDir = new File(moduleDir, "target/logdoc");
                File traceDictionaryFile = new File(logDocDir, module + "/trace-dictionary.md");

                if (!logDocDir.exists() || !traceDictionaryFile.exists()) {
                    getLog().warn("Aucun répertoire logdoc trouvé dans " + moduleDir.getAbsolutePath() + " ou fichier trace-dictionary.md manquant");
                    continue;
                }

                // Copier le répertoire logdoc dans outputDirectory
                copyDirectory(logDocDir.toPath(), outputDirectory.toPath());

                moduleLinks.add(String.format("- [%s](%s/trace-dictionary.md)", module, module));
            }

            // Générer l’index global
            File indexFile = new File(outputDirectory, "index.md");
            try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(indexFile.toPath()))) {
                writer.println("# Index global des traces\n");
                for (String link : moduleLinks) {
                    writer.println(link);
                }
            }
            getLog().info("Index global généré : " + indexFile.getAbsolutePath());

        } catch (Exception e) {
            throw new MojoExecutionException("Erreur lors de l'agrégation des docs de traces", e);
        }
    }

    private void copyDirectory(Path source, Path target) throws IOException {
        try (Stream<Path> walk = Files.walk(source)) {
            walk.forEach(sourcePath -> {
                try {
                    Path targetPath = target.resolve(source.relativize(sourcePath));
                    if (Files.isDirectory(sourcePath)) {
                        Files.createDirectories(targetPath);
                    } else {
                        Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Erreur lors de la copie du répertoire", e);
                }
            });
        }
    }
}
