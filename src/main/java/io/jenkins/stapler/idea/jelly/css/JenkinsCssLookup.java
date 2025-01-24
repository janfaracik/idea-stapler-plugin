package io.jenkins.stapler.idea.jelly.css;

import static io.jenkins.stapler.idea.jelly.css.Utils.parseCss;
import static org.kohsuke.stapler.idea.MavenProjectHelper.getArtifactId;

import com.intellij.openapi.project.Project;
import io.jenkins.stapler.idea.jelly.symbols.Symbol;
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
    public Set<Symbol> getClasses(Project project) {
        if (Objects.equals(getArtifactId(project), "jenkins-parent")) {
            return Set.of();
        }

        return extractClassesFromStyles().stream()
                .map(e -> new Symbol(e.className(), e.className(), "jenkins"))
                .collect(Collectors.toSet());
    }

    private Set<Utils.Thingy> extractClassesFromStyles() {
        Set<Utils.Thingy> classes = new HashSet<>();

        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(STYLES_CSS);
                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            StringBuilder fileContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                fileContent.append(line).append("\n");
            }

            return parseCss(fileContent.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return classes;
    }
}
