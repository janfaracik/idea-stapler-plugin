package io.jenkins.stapler.idea.jelly.css;

import com.intellij.openapi.project.Project;
import java.util.Set;

public interface CssLookup {

    Set<ClassName> getClasses(Project project);
}
