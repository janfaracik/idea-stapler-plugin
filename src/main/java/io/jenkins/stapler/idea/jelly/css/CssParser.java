package io.jenkins.stapler.idea.jelly.css;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CssParser {

    private static final Pattern PATTERN = Pattern.compile("(?<=\\.)[a-zA-Z0-9_-]+(?:\\\\ [^{}]+)?");

    /**
     * @param css the body of text to parse
     * @return retrieves all class names in the given CSS body
     */
    public static Set<String> getClassNames(String css) {
        Set<String> response = new HashSet<>();
        Matcher matcher = PATTERN.matcher(css);

        while (matcher.find()) {
            String className = matcher.group();

            // Replace escaped backslashes (\ ) with nothing and trim
            className = className.replace("\\ ", "").trim();

            response.add(className);
        }

        return response;
    }
}
