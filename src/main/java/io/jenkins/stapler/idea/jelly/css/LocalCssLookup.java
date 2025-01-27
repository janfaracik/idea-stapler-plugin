package io.jenkins.stapler.idea.jelly.css;

import static io.jenkins.stapler.idea.jelly.css.CssParser.getClassNames;
import static org.kohsuke.stapler.idea.MavenProjectHelper.isJenkinsCore;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.fileTypes.UnknownFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class LocalCssLookup implements CssLookup {

    /** Adds project-specific classes */
    @Override
    public Set<ClassName> getClasses(Project project) {
        if (isJenkinsCore(project)) {
            return retrieveJenkinsCoreClassNames(project);
        }

        return retrievePluginClassNames(project);
    }

    /** Retrieves class names from the generated {@code styles.css} in Jenkins Core */
    private Set<ClassName> retrieveJenkinsCoreClassNames(Project project) {
        Set<ClassName> response = new HashSet<>();
        String basePath = project.getBasePath();

        if (basePath == null) {
            return response;
        }

        VirtualFile stylesheet =
                LocalFileSystem.getInstance().findFileByPath(basePath + "/war/src/main/webapp/jsbundles/styles.css");

        if (stylesheet == null) {
            return response;
        }

        processFile(stylesheet, response);

        return response;
    }

    /** If the project is a plugin, index and process stylesheets dynamically */
    private Set<ClassName> retrievePluginClassNames(Project project) {
        Set<ClassName> response = new HashSet<>();

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

        return response.stream().filter(e -> !e.name().startsWith("jenkins-")).collect(Collectors.toSet());
    }

    private void processFile(VirtualFile file, Set<ClassName> response) {
        try {
            String fileContent = new String(file.contentsToByteArray(), StandardCharsets.UTF_8);

            response.addAll(getClassNames(fileContent).stream()
                    .map(e -> new ClassName(e, null))
                    .collect(Collectors.toSet()));
        } catch (IOException e) {
            System.err.println("Error reading file: " + file.getPath() + " - " + e.getMessage());
        }
    }
}
