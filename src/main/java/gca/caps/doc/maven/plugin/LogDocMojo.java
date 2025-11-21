package gca.caps.doc.maven.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Mojo(name = "generate-logdoc", defaultPhase = LifecyclePhase.PROCESS_SOURCES, threadSafe = true)
public class LogDocMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter(defaultValue = "${project.build.directory}/logdoc", readonly = true, required = true)
    private File outputDirectory;

    public void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public void execute() throws MojoExecutionException {
        execute(project.getBasedir());
    }

    private void processModule(File moduleOutDir, File baseDir) throws ParserConfigurationException, IOException, SAXException {
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

    private List<TraceEntry> parseTraces(File xmlFile) throws ParserConfigurationException, IOException, SAXException {
        List<TraceEntry> traces = new ArrayList<>();
        Document doc = DocumentBuilderFactory.newDefaultInstance().newDocumentBuilder().parse(xmlFile);
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

    protected void execute(File baseDir) throws MojoExecutionException {
        try {
            if (!outputDirectory.exists()) {
                Files.createDirectories(outputDirectory.toPath());
            }

            File moduleOutDir = new File(outputDirectory, baseDir.getName());
            processModule(moduleOutDir, baseDir);

        } catch (Exception e) {
            throw new MojoExecutionException("Erreur lors de la génération des docs de traces", e);
        }
    }

    private record TraceEntry(String code, String niveau, String message) {
    }
}
