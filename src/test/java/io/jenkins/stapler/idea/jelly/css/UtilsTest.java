package io.jenkins.stapler.idea.jelly.css;

import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertEquals;

public class UtilsTest {

    @Test
    public void testParseCss() {
        String input = """
            .jenkins-app-bar {
                background: red;
            }
            
            .jenkins-\\ !-margin-0 {
                margin: 0;
            }
            
            .jenkins-should-show {
                background: green;

                .jenkins-should-not-show {
                    background: orange;
                }
            }
            """;

        assertEquals(Set.of(
            new Utils.Thingy("jenkins-app-bar", null),
            new Utils.Thingy("jenkins-!-margin-0", "margin: 0;"),
            new Utils.Thingy("jenkins-should-show", null)
        ), Utils.parseCss(input));
    }
}
