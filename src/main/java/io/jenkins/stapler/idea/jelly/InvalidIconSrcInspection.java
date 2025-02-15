package io.jenkins.stapler.idea.jelly;

import static io.jenkins.stapler.idea.jelly.symbols.ClosestStringFinder.findClosestString;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.XmlElementVisitor;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import io.jenkins.stapler.idea.jelly.symbols.Symbol;
import io.jenkins.stapler.idea.jelly.symbols.SymbolFinder;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class InvalidIconSrcInspection extends LocalInspectionTool {

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new XmlElementVisitor() {
            @Override
            public void visitXmlAttribute(@NotNull XmlAttribute attribute) {
                if (validAttributeToScan(attribute)) {
                    Set<String> symbols =
                            SymbolFinder.getInstance(attribute.getProject()).getAvailableSymbols().stream()
                                    .map(Symbol::name)
                                    .collect(Collectors.toSet());
                    if (!symbols.contains(attribute.getValue())) {
                        String closestSymbol = findClosestString(attribute.getValue(), symbols);

                        if (closestSymbol == null) {
                            holder.registerProblem(
                                    attribute.getValueElement(),
                                    String.format("'%s' isn't a valid symbol", attribute.getValue()));
                        } else {
                            holder.registerProblem(
                                    attribute.getValueElement(),
                                    String.format("'%s' isn't a valid symbol", attribute.getValue()),
                                    new LocalQuickFix() {
                                        @Override
                                        public @IntentionFamilyName @NotNull String getFamilyName() {
                                            return "Replace with '" + closestSymbol + "'";
                                        }

                                        @Override
                                        public void applyFix(
                                                @NotNull Project project, @NotNull ProblemDescriptor descriptor) {
                                            PsiElement element = descriptor.getPsiElement();
                                            if (element != null
                                                    && element.getParent() instanceof XmlAttribute attribute) {
                                                attribute.setValue(closestSymbol);
                                            }
                                        }
                                    });
                        }
                    }
                }
            }
        };
    }

    private boolean validAttributeToScan(@NotNull XmlAttribute attribute) {
        String attributeName = attribute.getName();
        String attributeValue = attribute.getValue();

        // Ignore values that aren't symbols and ignore cases where symbols are dynamically generated
        if (attributeValue == null || !attributeValue.startsWith("symbol-") || attributeValue.contains("${")) {
            return false;
        }

        // If "src" is inside <l:icon>, allow it
        if ("src".equals(attributeName)) {
            PsiElement parent = attribute.getParent();
            if (parent instanceof XmlTag xmlTag) {
                return "icon".equals(xmlTag.getLocalName()) && "/lib/layout".equals(xmlTag.getNamespace());
            }
            return false;
        }

        // "icon" is allowed anywhere, as long as it meets the value conditions
        return "icon".equals(attributeName);
    }
}
