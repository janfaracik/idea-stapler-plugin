package io.jenkins.stapler.idea.jelly.css;

import static org.junit.Assert.assertEquals;

import java.util.Set;
import org.junit.Test;

public class UtilsTest {

    @Test
    public void testParseCss() {
        String input =
                """
            .jenkins-app-bar {
                background: red;
            }

            .jenkins-\\ !-margin-0 {
                margin: 0;
            }

            .jenkins-should-show {
                background: green;

                .jenkins-should-also-show {
                    background: orange;
                }
            }
            """;

        assertEquals(
                Set.of("jenkins-app-bar", "jenkins-!-margin-0", "jenkins-should-show", "jenkins-should-also-show"),
                Utils.parseCss(input));
    }

    @Test
    public void testParseCss_noSpaces() {
        String input =
                """
            .jenkins-app-bar {
                background: red;
            }.jenkins-\\ !-margin-0 {
                margin: 0;
            }.jenkins-should-show {
                background: green;

                .jenkins-should-also-show {
                    background: orange;
                }
            }
            """;

        assertEquals(
                Set.of("jenkins-app-bar", "jenkins-!-margin-0", "jenkins-should-show", "jenkins-should-also-show"),
                Utils.parseCss(input));
    }
}
