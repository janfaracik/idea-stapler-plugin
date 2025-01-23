package io.jenkins.stapler.idea.jelly.css;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.AsyncFileListener;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import io.jenkins.stapler.idea.jelly.symbols.Symbol;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Service(Service.Level.PROJECT)
public final class CssFinder implements Disposable {

    private final Project project;

    private Set<Symbol> ICONS_CACHE = null;

    private final List<CssLookup> CSS_LOOKUPS = List.of(new LocalCssLookup(), new JenkinsCssLookup());

    public CssFinder(@NotNull Project project) {
        this.project = project;

        VirtualFileManager.getInstance()
                .addAsyncFileListener(
                        new AsyncFileListener() {
                            @Override
                            public @Nullable ChangeApplier prepareChange(
                                    @NotNull List<? extends @NotNull VFileEvent> events) {
                                boolean fileChanged = events.stream()
                                        .anyMatch(event -> event.getFile() != null
                                                && event.getFile().getName().endsWith(".css"));

                                if (fileChanged) {
                                    return new ChangeApplier() {
                                        @Override
                                        public void afterVfsChange() {
                                            invalidateCache();
                                        }
                                    };
                                }

                                return null;
                            }
                        },
                        this);
    }

    public static CssFinder getInstance(Project project) {
        return project.getService(CssFinder.class);
    }

    public Set<Symbol> getAvailableClasses() {
        if (ICONS_CACHE == null) {
            ICONS_CACHE = computeSymbols();
        }
        return ICONS_CACHE;
    }

    public void invalidateCache() {
        ICONS_CACHE = null;
    }

    private Set<Symbol> computeSymbols() {
        return CSS_LOOKUPS.stream()
                .flatMap(finder -> finder.getClasses(project).stream())
                .collect(Collectors.toSet());
    }

    @Override
    public void dispose() {
        // Needn't implement this, 'addVirtualFileListener' will dispose when this class does
    }
}
