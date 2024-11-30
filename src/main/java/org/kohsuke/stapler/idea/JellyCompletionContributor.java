package org.kohsuke.stapler.idea;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.XmlElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.xml.TagNameReference;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlToken;
import com.intellij.util.ProcessingContext;
import com.intellij.xml.XmlElementDescriptor;
import org.jetbrains.annotations.NotNull;
import org.kohsuke.stapler.idea.descriptor.StaplerCustomJellyTagLibraryXmlNSDescriptor;
import org.kohsuke.stapler.idea.icons.Icons;


/**
 * Tag name completion for Jelly tag libraries defined as tag files.
 *
 * <p>
 * One would think that contributing {@link StaplerCustomJellyTagLibraryXmlNSDescriptor} and
 * implementing {@link StaplerCustomJellyTagLibraryXmlNSDescriptor#getRootElementsDescriptors(XmlDocument)} is enough
 * to cause the IDEA XML module to show tag name completions, but apparently it is not.
 *
 * <p>
 * The problem is when we have text like the following:
 *
 * <pre><xmp>
   <j:jelly xmlns:t="/lib" xmlns:j="jelly:core">
       <html>
           <body>
               <!-- hit t, ctrl+space to make sure completion kicks in  -->
               <t:
           </body>
       </html>
   </j:jelly>
 * </xmp></pre>
 *
 * <p>
 * The html/body tags get dtd.XmlElementDescriptorImpl as their {@link XmlElementDescriptor}
 * (they represent in-memory for-this-document-only temporary DTDs), and unless the body tag
 * appear elsewhere and have some children, it will return null from its
 * {@link XmlElementDescriptor#getElementDescriptor(XmlTag, XmlTag)} when given &lt;t:something/>,
 * and {@link TagNameReference}, which calls {@link StaplerCustomJellyTagLibraryXmlNSDescriptor#getRootElementsDescriptors(XmlDocument)}
 * to list up possible children, uses this as a signal that &lt;t:something/>
 * is not a valid child in this context.
 *
 * <p>
 * While the above doesn't really explain why &lt;j:*> can be completed in this context,
 * I eventually decided that just writing another {@link CompletionContributor} on its own
 * is the easiest thing to do here.
 *
 * <p>
 * So what this {@link CompletionContributor} does is to list up all the legal
 * Jelly tags available in the current namespace bindings as completion candidates.
 *
 * @author Kohsuke Kawaguchi
 */
public class JellyCompletionContributor extends CompletionContributor {

    // JAN NOTE
    // This seems inferior to the (maybe built in?) IntelliJ completion contributor
    // It doesnt automatically add required props
    // It causes the <l:l:card /> but with duplicate prefixes

    public JellyCompletionContributor() {
        extend(CompletionType.BASIC, // in case of XML completion, this always seems to be BASIC
                XML_ELEMENT_NAME_PATTERN,
                new CompletionProvider<>() {
                    // REFERENCE: spring plugin adds CompletionContributor as well.
                    @Override
                    protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
                        XmlElement name = (XmlElement)parameters.getPosition();

                        // this pseudo-tag represents the tag being completed.
                        XmlTag tag = (XmlTag) name.getParent();
                        Module module = ModuleUtil.findModuleForPsiElement(tag);

                        /*
                        When completion is invoked after text like "<p:",
                        the following code gives us "p:".
                        This would potentially speed up the following "knownNamespaces" loop.
                        --------------------
                        System.out.println("Completing tag name for "+tag.getName());
                        PsiElement opos = parameters.getOriginalPosition();
                        System.out.println("originalPos:"+ opos);
                        if (opos instanceof XmlToken) {
                            XmlToken originalToken = (XmlToken) opos;
                            System.out.println(originalToken.getParent());
                        }
                        */

                        // just add every available jelly tags in the current namespaace
                        String[] uris = tag.knownNamespaces();
                        for (String uri : uris) {
                            String prefix = tag.getPrefixByNamespace(uri);
                            if(prefix!=null && !prefix.isEmpty())
                                prefix+=':';
                            StaplerCustomJellyTagLibraryXmlNSDescriptor d = StaplerCustomJellyTagLibraryXmlNSDescriptor.get(uri, module);
                            if(d!=null) {
                                for( XmlElementDescriptor e : d.getRootElementsDescriptors(null/*I'm not using this parameter*/)) {
                                    result.addElement(LookupElementBuilder.create(prefix + e.getName()).withIcon(Icons.JELLY));
                                }
                            }
                        }
                    }
                }
        );
    }

    /**
     * {@link ElementPattern} that matches attribute names.
     */
    private static final ElementPattern<? extends PsiElement> XML_ELEMENT_NAME_PATTERN =
            new XmlElementPattern(XmlToken.class) {}.withParent(XmlTag.class);
}
