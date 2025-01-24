package io.jenkins.stapler.idea.jelly.css;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    public static Set<Thingy> parseCss(String css) {
        Set<Thingy> response = new HashSet<>();

        // Match top-level class definitions and their content
        Pattern classPattern = Pattern.compile("^\\s*\\.((?:\\\\.|[\\w!\\-])+)[^{]*\\{([^}]*)\\}", Pattern.MULTILINE);
        Matcher matcher = classPattern.matcher(css);

        while (matcher.find()) {
            String className = matcher.group(1);

            // Replace escaped backslashes (\ ) with nothing
            className = className.replace("\\ ", "");

            // Extract content inside the class
            String classContent = matcher.group(2).trim();

            if (!className.contains("-!-")) {
                classContent = null;
            }

            response.add(new Thingy(className, classContent));
        }

        return response;
    }

    public record Thingy(String className, String helpDocs) {}
}
