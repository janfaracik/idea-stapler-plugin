package io.jenkins.stapler.idea.jelly;

import static com.intellij.patterns.PlatformPatterns.psiElement;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlElementType;
import com.intellij.util.ProcessingContext;
import io.jenkins.stapler.idea.jelly.css.ClassName;
import io.jenkins.stapler.idea.jelly.css.CssFinder;
import org.jetbrains.annotations.NotNull;

/** Provides code completions for Jelly class attributes, supported attributes are "class", "classes", and "clazz". */
public class ClassCompletionContributor extends CompletionContributor {

    public ClassCompletionContributor() {
        extend(
                CompletionType.BASIC,
                psiElement(XmlElementType.XML_ATTRIBUTE_VALUE_TOKEN)
                        .withParent(XmlPatterns.xmlAttributeValue())
                        .withSuperParent(2, XmlPatterns.xmlAttribute().withName("class", "classes", "clazz")),
                new CompletionProvider<>() {
                    @Override
                    protected void addCompletions(
                            @NotNull CompletionParameters parameters,
                            @NotNull ProcessingContext context,
                            @NotNull CompletionResultSet result) {
                        CompletionResultSet resultSet = result;

                        // The class attribute supports multiple classes (separated with a space),
                        // so we need to handle the completion results ourselves
                        PsiElement position = parameters.getPosition();
                        Project project = position.getProject();
                        String[] prefixes =
                                resultSet.getPrefixMatcher().getPrefix().split(" ");
                        String prefix = prefixes[prefixes.length - 1];
                        resultSet = resultSet.withPrefixMatcher(prefix);

                        for (ClassName className :
                                CssFinder.getInstance(project).getAvailableClasses()) {
                            LookupElementBuilder element = LookupElementBuilder.create(className.name())
                                    .withTypeText(className.type())
                                    .withIcon(AllIcons.Nodes.Class);
                            resultSet.addElement(element);
                        }
                    }
                });
    }
}
