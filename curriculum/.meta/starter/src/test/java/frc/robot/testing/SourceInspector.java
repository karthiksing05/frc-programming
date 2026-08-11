package frc.robot.testing;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

/**
 * Reads the project's own source files so a rubric can grade <em>structure</em>, not just behaviour.
 *
 * <p>Most of what this curriculum checks is behavioural: press this button, the motor does that.
 * Some lessons, though, are refactors — lesson 04 moves hardware out of {@code Robot}, lesson 14
 * moves bindings out of {@code RobotContainer} — and after a correct refactor the robot behaves
 * exactly as it did before. Behaviour cannot tell you whether the refactor happened.
 *
 * <p>So those rubrics read the file. It is a blunt instrument and it is used sparingly: a check that
 * a class no longer mentions {@code PWMSparkMax} is honest, whereas a check on how somebody phrased
 * a comment would be tyranny.
 *
 * <p>Gradle runs tests with the working directory set to the project root, which is what makes these
 * relative paths resolve.
 */
public final class SourceInspector {
  private SourceInspector() {}

  /**
   * @param relativePath path from the project root, e.g. {@code
   *     src/main/java/frc/robot/Robot.java}
   * @return the file's full text
   */
  public static String read(String relativePath) {
    try {
      return Files.readString(Path.of(relativePath));
    } catch (IOException e) {
      throw new UncheckedIOException(
          "Could not read " + relativePath + " — is the working directory the project root?", e);
    }
  }

  /**
   * Counts lines that actually do something: no blanks, no {@code //} comments, no Javadoc or block
   * comments, no lone braces.
   *
   * <p>Lesson 14's rubric is "keep {@code RobotContainer} small". Counting raw lines would punish
   * exactly the thing this curriculum is trying to encourage — explaining yourself — so it counts
   * code instead.
   *
   * @param source the file text
   * @return the number of substantive lines
   */
  public static int countCodeLines(String source) {
    boolean[] inBlockComment = {false};
    return (int)
        Arrays.stream(source.split("\n", -1))
            .map(String::strip)
            .filter(
                line -> {
                  if (inBlockComment[0]) {
                    if (line.contains("*/")) {
                      inBlockComment[0] = false;
                    }
                    return false;
                  }
                  if (line.startsWith("/*")) {
                    inBlockComment[0] = !line.contains("*/");
                    return false;
                  }
                  return !line.isEmpty()
                      && !line.startsWith("//")
                      && !line.startsWith("*")
                      && !line.equals("}")
                      && !line.equals("{")
                      && !line.equals("});")
                      && !line.equals(")");
                })
            .count();
  }

  /**
   * @param relativePath the file to inspect
   * @param needle text that must not appear
   * @return true if the file does not mention {@code needle} outside of comments
   */
  public static boolean mentionsOutsideComments(String relativePath, String needle) {
    boolean inBlock = false;
    for (String raw : read(relativePath).split("\n", -1)) {
      String line = raw.strip();
      if (inBlock) {
        if (line.contains("*/")) {
          inBlock = false;
        }
        continue;
      }
      if (line.startsWith("/*")) {
        inBlock = !line.contains("*/");
        continue;
      }
      if (line.startsWith("//") || line.startsWith("*")) {
        continue;
      }
      if (line.contains(needle)) {
        return true;
      }
    }
    return false;
  }
}
