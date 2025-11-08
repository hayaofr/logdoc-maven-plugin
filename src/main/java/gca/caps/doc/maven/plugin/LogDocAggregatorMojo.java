package gca.caps.doc.maven.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@Mojo(name = "aggregate-logdoc", defaultPhase = LifecyclePhase.VERIFY, threadSafe = true)
public class LogDocAggregatorMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter(defaultValue = "${project.build.directory}/logdoc", required = true)
    private File outputDirectory;

    public void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public void execute() throws MojoExecutionException {
        try {
            if (!outputDirectory.exists()) {
                Files.createDirectories(outputDirectory.toPath());
            }

            List<String> moduleLinks = new ArrayList<>();

            // Parcourir les répertoires dans outputDirectory
            File[] moduleDirs = outputDirectory.listFiles(File::isDirectory);
            if (moduleDirs == null || moduleDirs.length == 0) {
                getLog().warn("Aucun répertoire trouvé dans " + outputDirectory.getAbsolutePath());
                return;
            }

            for (File moduleDir : moduleDirs) {
                File traceDictionaryFile = new File(moduleDir, "trace-dictionary.md");
                if (!traceDictionaryFile.exists()) {
                    getLog().warn("Aucun fichier trace-dictionary.md trouvé dans " + moduleDir.getAbsolutePath());
                    continue;
                }

                moduleLinks.add(String.format("- [%s](%s/trace-dictionary.md)", moduleDir.getName(), moduleDir.getName()));
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
}
