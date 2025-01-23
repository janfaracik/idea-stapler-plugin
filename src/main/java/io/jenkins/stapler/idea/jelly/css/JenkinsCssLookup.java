package io.jenkins.stapler.idea.jelly.css;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class JenkinsCssLookup implements CssLookup {

    private final static String STYLES_CSS = "io/jenkins/stapler/idea/jelly/css/styles.css";

    /** Adds the core Jenkins classes if the project is a plugin */
    @Override
    public Set<Symbol> getClasses(Project project) {
        if (Objects.equals(getArtifactId(project), "jenkins-parent")) {
            return Set.of();
        }

        return extractClassesFromStyles().stream()
                .map(e -> new Symbol(e, e, "jenkins"))
                .collect(Collectors.toSet());
    }

    public Set<String> extractClassesFromStyles() {
        Set<String> classes = new HashSet<>();

        try (InputStream inputStream = getClass()
                        .getClassLoader()
                        .getResourceAsStream(STYLES_CSS);
                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            StringBuilder fileContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                fileContent.append(line).append("\n");
            }

            Pattern classPattern = Pattern.compile("\\.(\\w[\\w-]*)\\s*\\{"); // Matches `.classname {`
            Matcher matcher = classPattern.matcher(fileContent.toString());

            while (matcher.find()) {
                String className = matcher.group(1);
                if (className.startsWith("jenkins-")) {
                    classes.add(matcher.group(1));
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return classes;
    }
}
