package io.jenkins.stapler.idea.jelly.css;

import static io.jenkins.stapler.idea.jelly.css.CssParser.getClassNames;
import static org.kohsuke.stapler.idea.MavenProjectHelper.isJenkinsCore;

import com.intellij.openapi.project.Project;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class JenkinsCssLookup implements CssLookup {

    private static final String STYLES_CSS = "io/jenkins/stapler/idea/jelly/css/styles.css";

    /** Adds the core Jenkins classes if the project is a plugin */
    @Override
    public Set<ClassName> getClasses(Project project) {
        if (isJenkinsCore(project)) {
            return Set.of();
        }

        return extractClassesFromStyles().stream()
                .filter(e -> e.startsWith("jenkins-"))
                .map(e -> new ClassName(e, "jenkins"))
                .collect(Collectors.toSet());
    }

    private Set<String> extractClassesFromStyles() {
        Set<String> classes = new HashSet<>();

        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(STYLES_CSS)) {
            String fileContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            return getClassNames(fileContent);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return classes;
    }
}
