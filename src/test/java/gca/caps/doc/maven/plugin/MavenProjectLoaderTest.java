package gca.caps.doc.maven.plugin;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class MavenProjectLoaderTest {

    @Test
    void testLoadProjectAndModules() throws Exception {
        File pom = new File("src/test/resources/test-multi-module-project/pom.xml");
        MavenProjectLoader.SimpleProject project = MavenProjectLoader.loadProject(pom);
        assertNotNull(project.basedir());
        assertTrue(project.basedir().isDirectory());

        assertEquals("com.example", project.model().getGroupId());
        assertTrue(project.model().getModules().contains("module-a"));
        assertTrue(project.model().getModules().contains("module-b"));

        System.out.println("Base dir = " + project.basedir().getAbsolutePath());
        System.out.println("Modules = " + project.model().getModules());
    }

}