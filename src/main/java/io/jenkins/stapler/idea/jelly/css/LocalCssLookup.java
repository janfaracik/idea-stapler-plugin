package io.jenkins.stapler.idea.jelly.css;

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
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LocalCssLookup implements CssLookup {

    /** Adds project-specific classes */
    @Override
    public Set<Symbol> getClasses(Project project) {
        Set<Symbol> response = new HashSet<>();

        var cssFileType = FileTypeManager.getInstance().getFileTypeByExtension("css");
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
                GlobalSearchScope.projectScope(project));

        return response;
    }

    private void processFile(VirtualFile file, Set<Symbol> response) {
        try {
            // Read file contents
            String fileContent = new String(file.contentsToByteArray(), StandardCharsets.UTF_8);

            // Regex for top-level CSS classes (match classes not inside curly braces)
            Pattern topLevelClassPattern = Pattern.compile("^\\s*\\.(\\w[\\w-]*)\\s*\\{", Pattern.MULTILINE);
            Matcher matcher = topLevelClassPattern.matcher(fileContent);

            while (matcher.find()) {
                response.add(
                        new Symbol(matcher.group(1), matcher.group(1), null)); // Add the class name without the dot
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + file.getPath() + " - " + e.getMessage());
        }
    }
}
