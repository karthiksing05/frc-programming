package frc.robot;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import edu.wpi.first.hal.HAL;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The smoke test — deliberately <em>not</em> tagged as a lesson.
 *
 * <p>Every lesson rubric carries {@code @Tag("lesson")}, and the default {@code test} task excludes
 * that tag. So {@code ./gradlew build} on a freshly copied project runs exactly this file and nothing
 * else, and it goes green even though sixteen lessons are still unsolved.
 *
 * <p>That matters more than it sounds. A student's very first interaction with the project should be
 * a build that succeeds, proving their WPILib install, their JDK, and their offline Maven cache all
 * work — before they have written a line of code that could be blamed. Install problems and code
 * problems must never arrive at the same time.
 *
 * <p>What it checks is therefore about the <em>environment</em>, not about anybody's answers.
 */
class ProjectHealthTest {

  @BeforeAll
  static void initHal() {
    assertTrue(
        HAL.initialize(500, 0),
        "The WPILib Hardware Abstraction Layer would not start. This almost always means the "
            + "native simulation libraries did not download or extract — check that "
            + "`includeDesktopSupport` is true in build.gradle.");
  }

  @AfterAll
  static void shutdownHal() {
    HAL.shutdown();
  }

  @Test
  @DisplayName("The simulation HAL and WPILib math libraries load")
  void wpilibIsUsable() {
    // Touching a JNI-backed class proves the native libraries really extracted,
    // which is the single most common thing to be broken on a fresh install.
    var pose = new edu.wpi.first.math.geometry.Pose2d(1.0, 2.0, new edu.wpi.first.math.geometry.Rotation2d());
    assertTrue(pose.getX() == 1.0, "wpimath is not behaving");
    assertTrue(
        edu.wpi.first.wpilibj.RobotBase.isSimulation(),
        "Tests should always report as running in simulation");
  }

  @Test
  @DisplayName("Every lesson in the manifest has its content files on disk")
  void lessonContentExists() {
    String manifest = read("lessons/manifest.json");

    // Deliberately hand-rolled rather than pulling in a JSON library: this test
    // must not be the reason the project needs an extra dependency, because an
    // extra dependency is an extra thing to fail to download.
    Matcher dirs = Pattern.compile("\"dir\"\\s*:\\s*\"([^\"]+)\"").matcher(manifest);
    int count = 0;
    while (dirs.find()) {
      count++;
      String dir = dirs.group(1);
      for (String file : new String[] {"README.md", "hints.md", "lesson.json"}) {
        Path p = Path.of("lessons", dir, file);
        assertTrue(
            Files.exists(p),
            "lessons/"
                + dir
                + "/"
                + file
                + " is missing. The manifest promises this lesson exists; `frcprog read` will "
                + "fail on it.");
      }
    }
    assertTrue(count >= 30, "Expected the full curriculum in the manifest, found " + count);
  }

  @Test
  @DisplayName("Every starter TODO names a lesson that exists")
  void todosAreWellFormed() {
    // A TODO that says "LESSON 42" sends a student looking for a lesson that was
    // never written. Cheap to check, annoying to discover the hard way.
    Pattern todo = Pattern.compile("TODO \\(LESSON ([0-9A-Z]+)\\)");
    String manifest = read("lessons/manifest.json");

    try (var stream = Files.walk(Path.of("src/main/java"))) {
      stream
          .filter(p -> p.toString().endsWith(".java"))
          .forEach(
              p -> {
                Matcher m = todo.matcher(read(p.toString()));
                while (m.find()) {
                  String id = m.group(1);
                  assertTrue(
                      manifest.contains("\"id\": \"" + id + "\""),
                      p + " has a TODO for lesson " + id + ", which is not in the manifest");
                }
              });
    } catch (IOException e) {
      fail("Could not walk src/main/java: " + e.getMessage());
    }
  }

  private static String read(String path) {
    try {
      return Files.readString(Path.of(path));
    } catch (IOException e) {
      throw new AssertionError(
          "Could not read "
              + path
              + ". Gradle runs tests from the project root, so this usually means the file is "
              + "genuinely missing.",
          e);
    }
  }
}
