package io.jenkins.stapler.idea.jelly.css;

import static io.jenkins.stapler.idea.jelly.css.Utils.parseCss;
import static org.kohsuke.stapler.idea.MavenProjectHelper.getArtifactId;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.fileTypes.UnknownFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import io.jenkins.stapler.idea.jelly.symbols.Symbol;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class LocalCssLookup implements CssLookup {

    /** Adds project-specific classes */
    @Override
    public Set<Symbol> getClasses(Project project) {
        Set<Symbol> response = new HashSet<>();

        // If the project is Jenkins core, look up its known stylesheet
        if (Objects.equals(getArtifactId(project), "jenkins-parent")) {
            VirtualFile stylesheet =
                    project.getBaseDir().findFileByRelativePath("war/src/main/webapp/jsbundles/styles.css");

            if (stylesheet == null) {
                return response;
            }

            processFile(stylesheet, response);
            return response;
        }

        // Otherwise if the project is a plugin, index and process stylesheets
        FileType cssFileType = FileTypeManager.getInstance().getFileTypeByExtension("css");

        if (cssFileType == UnknownFileType.INSTANCE) {
            return Set.of();
        }

        FileTypeIndex.processFiles(
                cssFileType,
                file -> {
                    if ("styles.css".equals(file.getName())) {
                        processFile(file, response);
                    }
                    return true;
                },
                GlobalSearchScope.everythingScope(project));

        return response;
    }

    private void processFile(VirtualFile file, Set<Symbol> response) {
        try {
            String fileContent = new String(file.contentsToByteArray(), StandardCharsets.UTF_8);

            response.addAll(parseCss(fileContent).stream()
                .map(e -> new Symbol(e.className(), e.className(), null))
                .collect(Collectors.toSet()));
        } catch (IOException e) {
            System.err.println("Error reading file: " + file.getPath() + " - " + e.getMessage());
        }
    }
}
