package gca.caps.doc.maven.plugin;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;

import java.io.File;
import java.io.FileReader;

public class MavenProjectLoader {

    public record SimpleProject(File basedir, Model model) {
    }

    public static SimpleProject loadProject(File pomFile) throws Exception {
        try (FileReader reader = new FileReader(pomFile)) {
            Model model = new MavenXpp3Reader().read(reader);
            File basedir = pomFile.getParentFile();
            return new SimpleProject(basedir, model);
        }
    }
}