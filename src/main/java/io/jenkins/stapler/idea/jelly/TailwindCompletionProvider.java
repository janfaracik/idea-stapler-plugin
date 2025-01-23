package io.jenkins.stapler.idea.jelly;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import io.jenkins.stapler.idea.jelly.css.CssFinder;
import io.jenkins.stapler.idea.jelly.symbols.Symbol;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class TailwindCompletionProvider extends CompletionProvider<CompletionParameters> {

    @Override
    protected void addCompletions(
            @NotNull CompletionParameters parameters,
            @NotNull ProcessingContext context,
            @NotNull CompletionResultSet result) {
        CompletionResultSet betterResultSet = result;

        PsiElement position = parameters.getPosition();
        Project project = position.getProject();
        String[] prefixes = betterResultSet.getPrefixMatcher().getPrefix().split(" ");
        String prefix = prefixes[prefixes.length - 1];
        betterResultSet = betterResultSet.withPrefixMatcher(prefix);

        List<String> groups = new ArrayList<>(List.of(prefix.split(":")));

        if (groups.size() == 1) {
            for (Symbol cls : CssFinder.getInstance(project).getAvailableClasses()) {
                LookupElementBuilder element = LookupElementBuilder.create(cls.name())
                        .withTypeText(cls.group())
                        .withIcon(AllIcons.Nodes.Class);
                betterResultSet.addElement(element);
            }
        }
    }
}
