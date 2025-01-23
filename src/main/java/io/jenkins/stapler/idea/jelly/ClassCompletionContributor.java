package io.jenkins.stapler.idea.jelly;

import static com.intellij.patterns.PlatformPatterns.psiElement;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.xml.XmlElementType;

public class ClassCompletionContributor extends CompletionContributor {

    public ClassCompletionContributor() {
        extend(
                CompletionType.BASIC,
                psiElement(XmlElementType.XML_ATTRIBUTE_VALUE_TOKEN)
                        .withParent(XmlPatterns.xmlAttributeValue())
                        .withSuperParent(2, XmlPatterns.xmlAttribute().withName("class", "classes", "clazz")),
                new TailwindCompletionProvider());
    }
}
