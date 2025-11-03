package gca.caps.doc.maven.plugin;

import org.apache.maven.model.Model;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Mojo(name = "generate-logdoc", defaultPhase = LifecyclePhase.VERIFY, threadSafe = true)
public class LogDocMojo extends AbstractMojo {

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

    private void processModule(File moduleOutDir, File baseDir) throws Exception {
        Path xmlPath = baseDir.toPath().resolve("target/generated-sources/annotations/Dictonnaire-des-traces.xml");

        if (!Files.exists(xmlPath)) {
            getLog().warn("Aucun fichier de traces trouvé pour le module " + baseDir.getName());
            return;
        }

        List<TraceEntry> traces = parseTraces(xmlPath.toFile());
        if (traces.isEmpty()) {
            getLog().warn("Aucune trace trouvée dans " + xmlPath);
            return;
        }

        Files.createDirectories(moduleOutDir.toPath());
        File mdFile = new File(moduleOutDir, "trace-dictionary.md");
        generateMarkdown(mdFile, traces, baseDir.getName());

        // Crée un index local pour le module
        File indexFile = new File(moduleOutDir, "index.md");
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(indexFile.toPath()))) {
            writer.printf("# Traces du module %s%n%n", baseDir.getName());
            writer.println("- [Dictionnaire des traces](trace-dictionary.md)");
        }

        getLog().info("Doc de traces générée pour le module : " + baseDir.getName());
    }

    private List<TraceEntry> parseTraces(File xmlFile) throws Exception {
        List<TraceEntry> traces = new ArrayList<>();
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xmlFile);
        NodeList list = doc.getElementsByTagName("trace");

        for (int i = 0; i < list.getLength(); i++) {
            Element trace = (Element) list.item(i);
            String code = trace.getAttribute("codeApplication");
            String id = trace.getAttribute("id");
            String niveau = trace.getAttribute("niveau");
            String message = trace.getAttribute("message");
            traces.add(new TraceEntry(code + id, niveau, message));
        }
        return traces;
    }

    private void generateMarkdown(File file, List<TraceEntry> traces, String moduleName) throws IOException {
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(file.toPath()))) {
            writer.printf("# Dictionnaire des traces de %s%n", moduleName);
            writer.printf("%n");
            writer.println("| Code complet | Niveau | Message |");
            writer.println("|---------------|---------|----------|");

            for (TraceEntry t : traces) {
                writer.printf("| %s | %s | %s |%n", t.code, t.niveau, t.message);
            }
        }
    }

    protected void execute(File baseDir, Model model) throws MojoExecutionException {
        try {
            if (!outputDirectory.exists()) {
                Files.createDirectories(outputDirectory.toPath());
            }

            if (model.getModules().isEmpty()) {
                // Cas d’un module simple
                processModule(outputDirectory, baseDir);
            } else {
                // Cas d’un projet parent multi-module
                List<String> moduleLinks = new ArrayList<>();

                for (String module : model.getModules()) {
                    File moduleDir = new File(baseDir, module);
                    File modulePom = new File(moduleDir, "pom.xml");
                    if (!modulePom.exists()) {
                        getLog().warn("Pas de pom.xml pour le module " + module);
                        continue;
                    }

                    File moduleOutDir = new File(outputDirectory, module);
                    Files.createDirectories(moduleOutDir.toPath());
                    processModule(moduleOutDir, moduleDir);

                    moduleLinks.add(String.format("- [%s](%s/index.md)", module, module));
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
            }

        } catch (Exception e) {
            throw new MojoExecutionException("Erreur lors de la génération des docs de traces", e);
        }
    }

    private record TraceEntry(String code, String niveau, String message) {
    }
}
