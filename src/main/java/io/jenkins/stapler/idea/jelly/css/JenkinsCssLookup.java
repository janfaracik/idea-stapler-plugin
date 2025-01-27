package io.jenkins.stapler.idea.jelly.css;

import static io.jenkins.stapler.idea.jelly.css.CssParser.getClassNames;
import static org.kohsuke.stapler.idea.MavenProjectHelper.isJenkinsCore;

import com.intellij.openapi.project.Project;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Objects;
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
                .map(e -> new ClassName(e, "jenkins"))
                .collect(Collectors.toSet());
    }

    private Set<String> extractClassesFromStyles() {
        Set<String> classes = new HashSet<>();

        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(STYLES_CSS);
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(Objects.requireNonNull(inputStream), StandardCharsets.UTF_8))) {

            StringBuilder fileContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                fileContent.append(line).append("\n");
            }

            return getClassNames(fileContent.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return classes;
    }
}
