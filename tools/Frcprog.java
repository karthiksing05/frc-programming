/*
 * frcprog — the curriculum's command line.
 *
 * WHY THIS IS A SINGLE .java FILE
 *     Java 11 introduced "source-file mode": `java Frcprog.java args...` compiles
 *     and runs a source file in memory, no build step. That matters here, because
 *     the one thing every student on this curriculum is guaranteed to have is the
 *     JDK that ships inside their WPILib install. A Node CLI would be nicer to
 *     write and would fail on the first laptop without Node; a Python CLI would
 *     fail on Windows. This fails nowhere.
 *
 *     The cost is a ~0.5 s compile on every invocation and no third-party
 *     libraries — including no JSON parser, which is why there is a small one at
 *     the bottom of this file. That trade is worth it. A tool that does not run
 *     is worth nothing, however elegant.
 *
 * INVOCATION
 *     Students run ./tools/frcprog (or tools\frcprog.cmd on Windows), which is a
 *     three-line wrapper that locates the WPILib JDK and hands off to here.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

public final class Frcprog {

  // ─── Terminal colours ──────────────────────────────────────────────────────
  // Disabled when output is redirected or NO_COLOR is set, so piping to a file
  // does not produce a mess of escape codes.
  private static final boolean COLOR =
      System.console() != null && System.getenv("NO_COLOR") == null;

  private static String c(String code, String s) {
    return COLOR ? "\u001b[" + code + "m" + s + "\u001b[0m" : s;
  }

  private static String bold(String s) {
    return c("1", s);
  }

  private static String green(String s) {
    return c("32", s);
  }

  private static String red(String s) {
    return c("31", s);
  }

  private static String yellow(String s) {
    return c("33", s);
  }

  private static String dim(String s) {
    return c("2", s);
  }

  private static String cyan(String s) {
    return c("36", s);
  }

  private static Path root;

  public static void main(String[] args) throws Exception {
    root = findProjectRoot();

    String command = args.length == 0 ? "next" : args[0];
    String[] rest = args.length <= 1 ? new String[0] : Arrays.copyOfRange(args, 1, args.length);

    try {
      switch (command) {
        case "list" -> cmdList();
        case "next" -> cmdNext();
        case "read" -> cmdRead(rest, "README.md");
        case "hints" -> cmdRead(rest, "hints.md");
        case "hint" -> cmdHint(rest);
        case "watch" -> System.exit(cmdWatch(rest));
        case "check" -> System.exit(cmdCheck(rest));
        case "sim" -> System.exit(cmdSim());
        case "build" -> System.exit(cmdBuild(rest));
        case "scope" -> cmdScope();
        case "site" -> cmdSite();
        case "solution" -> cmdSolution(rest);
        case "reset" -> cmdReset(rest);
        case "progress" -> cmdProgress();
        case "doctor" -> System.exit(cmdDoctor());
        case "new-lesson" -> cmdNewLesson(List.of(rest));
        case "help", "-h", "--help" -> usage();
        default -> {
          System.err.println(red("Unknown command: " + command));
          usage();
          System.exit(2);
        }
      }
    } catch (UserError e) {
      System.err.println(red("✗ " + e.getMessage()));
      System.exit(1);
    }
  }

  private static void usage() {
    System.out.println(
        String.join(
            "\n",
            bold("frcprog") + " — the FRCProgramming curriculum, offline",
            "",
            bold("Working through a lesson"),
            "  " + cyan("frcprog watch") + "               "
                + bold("re-runs the rubric every time you save"),
            "  " + cyan("frcprog next") + "                what to do now, and where",
            "  " + cyan("frcprog read <lesson>") + "       the lesson text, in your terminal",
            "  " + cyan("frcprog check <lesson>") + "      run the rubric and grade yourself",
            "  " + cyan("frcprog hint <lesson>") + "       one more hint than last time",
            "  " + cyan("frcprog hints <lesson>") + "      all four at once",
            "",
            bold("Seeing it move"),
            "  " + cyan("frcprog sim") + "                 launch the robot simulator",
            "  " + cyan("frcprog scope") + "               launch AdvantageScope",
            "  " + cyan("frcprog site") + "                serve the lesson site at localhost:8000",
            "",
            bold("Where am I"),
            "  " + cyan("frcprog list") + "                every lesson and its status",
            "  " + cyan("frcprog progress") + "            how far through you are",
            "  " + cyan("frcprog check --all") + "         run every rubric (your local CI)",
            "",
            bold("Getting unstuck"),
            "  " + cyan("frcprog reset <lesson>") + "      restore the starter code for a lesson",
            "  " + cyan("frcprog solution <lesson>") + "   overwrite with the reference answer",
            "  " + cyan("frcprog doctor") + "              check your install before blaming code",
            "",
            bold("Writing lessons") + dim("  (mentors and curriculum authors)"),
            "  " + cyan("frcprog new-lesson <NN>-<slug>") + "  scaffold a lesson that already lints",
            "",
            dim("  <lesson> can be an id (07) or a slug (07-tank-drive)."),
            dim("  Everything here works with no network connection."),
            ""));
  }

  // ═══════════════════════════════════════════════════════════════════════════
  //  Commands
  // ═══════════════════════════════════════════════════════════════════════════

  private static void cmdList() throws IOException {
    List<Lesson> lessons = loadLessons();
    Map<String, Boolean> done = loadProgress();

    String stage = null;
    for (Lesson l : lessons) {
      if (!l.stage.equals(stage)) {
        stage = l.stage;
        System.out.println();
        System.out.println(bold("  Stage " + stage));
      }
      System.out.printf(
          "    %s  %-4s %-42s %s%n",
          statusIcon(l, done),
          l.id,
          l.title,
          dim(l.track.equals("extension") ? "needs a download" : l.estimatedMinutes + " min"));
    }
    System.out.println();
    System.out.println(
        dim("    ✓ passed   ○ not yet   ‣ next up   ◇ nothing to grade   ⬇ extension"));
    System.out.println();
  }

  private static String statusIcon(Lesson l, Map<String, Boolean> done) {
    if (l.track.equals("extension")) {
      return dim("⬇");
    }
    if (!l.graded) {
      return dim("◇");
    }
    if (Boolean.TRUE.equals(done.get(l.id))) {
      return green("✓");
    }
    return "○";
  }

  private static void cmdNext() throws IOException {
    List<Lesson> lessons = loadLessons();
    Map<String, Boolean> done = loadProgress();

    Optional<Lesson> next =
        lessons.stream()
            .filter(l -> l.graded && !Boolean.TRUE.equals(done.get(l.id)))
            .findFirst();

    if (next.isEmpty()) {
      System.out.println();
      System.out.println(green(bold("  Every graded lesson is passing.")));
      System.out.println();
      System.out.println("  What is left is the extension track — the lessons that need a");
      System.out.println("  vendor library downloaded once. See " + cyan("lessons/EXTENSIONS.md") + ".");
      System.out.println();
      return;
    }

    Lesson l = next.get();
    Map<String, Object> meta = readLessonJson(l);

    System.out.println();
    System.out.println(bold("  Lesson " + l.id + " — " + l.title));
    System.out.println(dim("  Stage " + l.stage + " · about " + l.estimatedMinutes + " minutes"));
    System.out.println();

    List<Object> edits = asList(meta.get("edits"));
    if (!edits.isEmpty()) {
      System.out.println("  " + bold("Edit"));
      for (Object e : edits) {
        System.out.println("    " + cyan(String.valueOf(e)));
      }
      System.out.println();
    }

    System.out.println("  " + bold("Then"));
    System.out.println("    " + cyan("./tools/frcprog read " + l.dir) + dim("     the full lesson"));
    System.out.println("    " + cyan("./tools/frcprog check " + l.dir) + dim("    grade yourself"));
    System.out.println();

    // Only promise a TODO marker if one is actually in the file. Integration
    // lessons like the capstone edit existing code and have none, and sending a
    // student hunting for a comment that was never written is a small lie that
    // costs them real time.
    if (hasTodoMarker(l, edits)) {
      System.out.println(
          dim("  Look for the TODO (LESSON " + l.id + ") comment. It tells you what to write."));
    } else {
      System.out.println(
          dim("  No TODO marker in this one — it builds on code you have already written."));
      System.out.println(dim("  The lesson text is the instructions."));
    }
    System.out.println();
  }

  /**
   * @return true when at least one of the lesson's edit targets really contains its TODO marker
   */
  private static boolean hasTodoMarker(Lesson l, List<Object> edits) {
    String marker = "TODO (LESSON " + l.id + ")";
    for (Object e : edits) {
      try {
        if (Files.readString(root.resolve(String.valueOf(e))).contains(marker)) {
          return true;
        }
      } catch (IOException ignored) {
        // A missing edit target is reported by `gradlew checkLessons`, not here.
      }
    }
    return false;
  }


  // ═══════════════════════════════════════════════════════════════════════════
  //  watch — the tight loop
  //
  //  Edit, save, and the rubric re-runs. No command to remember, no window to
  //  switch to. Borrowed straight from Rustlings, because the gap between
  //  "changed something" and "found out whether it worked" is most of what makes
  //  a learning loop feel good or feel like homework.
  //
  //  Polls file timestamps rather than using WatchService. On macOS, WatchService
  //  is itself implemented as a poller with a latency of up to ten seconds, which
  //  would defeat the point entirely.
  // ═══════════════════════════════════════════════════════════════════════════


  /**
   * Deletes a lesson's previous JUnit XML.
   *
   * <p>Gradle leaves the last run's reports in place. If the next build fails before
   * any test executes, which is what a compile error does, reading that directory
   * reports the PREVIOUS verdict. A student who has just broken the syntax gets told
   * their checks pass. Clearing first makes "no results" mean "nothing ran".
   */
  private static void clearResults(String reportDir) {
    Path dir = root.resolve("build/test-results").resolve(reportDir);
    if (!Files.isDirectory(dir)) {
      return;
    }
    try (var files = Files.list(dir)) {
      for (Path f : files.toList()) {
        Files.deleteIfExists(f);
      }
    } catch (IOException ignored) {
      // Worst case we read a stale file, which is what we had before.
    }
  }

  private static final long POLL_MS = 350;
  private static final long SETTLE_MS = 250;

  private static int cmdWatch(String[] args) throws Exception {
    Lesson lesson = args.length > 0 ? requireLesson(args) : firstUnfinished();
    if (lesson == null) {
      System.out.println();
      System.out.println(green(bold("  Every graded lesson is passing. Nothing to watch.")));
      System.out.println();
      return 0;
    }

    Map<Path, Long> stamps = sourceStamps();
    runWatchPass(lesson, "watching");

    while (true) {
      Thread.sleep(POLL_MS);
      Map<Path, Long> now = sourceStamps();
      if (now.equals(stamps)) {
        continue;
      }

      // Editors write in bursts, and some write a temp file then rename. Wait
      // for the dust to settle so one save triggers one run.
      Map<Path, Long> settled;
      do {
        settled = now;
        Thread.sleep(SETTLE_MS);
        now = sourceStamps();
      } while (!now.equals(settled));

      stamps = now;

      if (runWatchPass(lesson, "saved")) {
        Lesson next = firstUnfinished();
        if (next == null) {
          System.out.println(green(bold("  That was the last one. All graded lessons pass.")));
          System.out.println();
          return 0;
        }
        if (!next.id.equals(lesson.id)) {
          lesson = next;
          System.out.println();
          System.out.println("  Moving on to " + bold(lesson.id + " — " + lesson.title));
          System.out.println("     " + cyan("./tools/frcprog read " + lesson.dir));
          System.out.println();
        }
      }
    }
  }

  /**
   * Runs one lesson's rubric and redraws the screen.
   *
   * @return true if it passed
   */
  private static boolean runWatchPass(Lesson lesson, String why) throws Exception {
    clearScreen();
    System.out.println();
    System.out.println(
        dim("  watch")
            + "  "
            + bold(lesson.id + " — " + lesson.title)
            + dim("   (" + why + ")"));
    System.out.println(dim("  ".repeat(1)) + dim("─".repeat(66)));
    System.out.println();

    clearResults("lesson-" + lesson.id);
    Captured build = gradleCaptured("lesson" + lesson.id);
    Results results = readResults("lesson-" + lesson.id);

    if (results.total == 0) {
      System.out.println(red("  ✗ Does not compile."));
      System.out.println();
      for (String line : compileErrors(build.output)) {
        System.out.println("    " + line);
      }
      System.out.println();
      footer(lesson, false);
      return false;
    }

    if (build.exitCode != 0 && results.failures.isEmpty()) {
      System.out.println(red("  ✗ The build failed, but no check reported a failure."));
      System.out.println();
      for (String line : compileErrors(build.output)) {
        System.out.println("    " + line);
      }
      System.out.println();
      footer(lesson, false);
      return false;
    }

    if (build.exitCode == 0) {
      System.out.println(
          green(bold("  ✓ Passed")) + dim("   " + results.total + " checks"));
      recordProgress(lesson.id, true);
      System.out.println();
      footer(lesson, true);
      return true;
    }

    System.out.println(
        red(bold("  ✗ " + results.passed + " of " + results.total + " checks passing")));
    System.out.println();
    for (Failure f : results.failures) {
      System.out.println("  " + red("✗") + " " + bold(f.name));
      for (String line : f.message.split("\n")) {
        if (!line.isBlank()) {
          System.out.println("      " + line.strip());
        }
      }
      System.out.println();
    }
    recordProgress(lesson.id, false);
    footer(lesson, false);
    return false;
  }

  private static void footer(Lesson lesson, boolean passed) {
    if (passed) {
      System.out.println(dim("  Save again to re-check, or Ctrl-C to stop."));
    } else {
      System.out.println(
          dim("  Save to re-run.  ")
              + cyan("frcprog hint " + lesson.dir)
              + dim(" in another terminal for a nudge.  Ctrl-C to stop."));
    }
    System.out.println();
  }

  /** Pulls the javac errors out of a Gradle log, dropping the build chatter. */
  private static List<String> compileErrors(String output) {
    List<String> out = new ArrayList<>();
    for (String line : output.split("\n")) {
      String t = line.strip();
      if (t.contains(".java:") && (t.contains("error:") || t.contains("warning:"))) {
        // Trim the absolute path down to something readable.
        out.add(t.replace(root.toString() + "/", ""));
      } else if (t.startsWith("symbol:") || t.startsWith("location:")) {
        out.add("  " + t);
      }
      if (out.size() > 14) {
        out.add(dim("  ... run ./gradlew build to see the rest"));
        break;
      }
    }
    if (out.isEmpty()) {
      out.add("Run " + cyan("./gradlew build") + " to see the compiler output.");
    }
    return out;
  }

  private static void clearScreen() {
    if (COLOR) {
      System.out.print("\033[2J\033[H");
      System.out.flush();
    } else {
      System.out.println("\n".repeat(3));
    }
  }

  /** Modification times of every Java source file, so a save is detectable. */
  private static Map<Path, Long> sourceStamps() {
    Map<Path, Long> out = new TreeMap<>();
    for (String dir : new String[] {"src/main/java", "src/test/java"}) {
      Path base = root.resolve(dir);
      if (!Files.isDirectory(base)) {
        continue;
      }
      try (var walk = Files.walk(base)) {
        walk.filter(p -> p.toString().endsWith(".java"))
            .forEach(
                p -> {
                  try {
                    out.put(p, Files.getLastModifiedTime(p).toMillis());
                  } catch (IOException ignored) {
                    // A file vanishing mid-walk just means the next poll sees it.
                  }
                });
      } catch (IOException ignored) {
        // Same.
      }
    }
    return out;
  }

  private static Lesson firstUnfinished() throws IOException {
    Map<String, Boolean> done = loadProgress();
    for (Lesson l : loadLessons()) {
      if (l.graded && !Boolean.TRUE.equals(done.get(l.id))) {
        return l;
      }
    }
    return null;
  }

  // ═══════════════════════════════════════════════════════════════════════════
  //  hint — one at a time
  //
  //  `hints` prints the whole file, answer included. That is the right thing when
  //  somebody has decided they want it. It is the wrong default, because the
  //  cheapest hint is always the last one and reading it costs you the lesson.
  //
  //  `hint` gives you one more than last time, and remembers.
  // ═══════════════════════════════════════════════════════════════════════════

  private static void cmdHint(String[] args) throws IOException {
    List<String> rest = new ArrayList<>(Arrays.asList(args));
    boolean reset = rest.remove("--reset");

    Lesson lesson =
        rest.isEmpty() ? firstUnfinished() : requireLesson(rest.toArray(new String[0]));
    if (lesson == null) {
      throw new UserError("Which lesson? Try `frcprog list`.");
    }

    List<String> hints = splitHints(lesson);
    if (hints.isEmpty()) {
      throw new UserError("Lesson " + lesson.id + " has no hints file to read.");
    }

    if (reset) {
      recordHintsSeen(lesson.id, 0);
      System.out.println();
      System.out.println(dim("  Hints for lesson " + lesson.id + " reset."));
      System.out.println();
      return;
    }

    int seen = loadHintsSeen().getOrDefault(lesson.id, 0);
    if (seen >= hints.size()) {
      System.out.println();
      System.out.println(
          yellow("  That was the last hint for lesson " + lesson.id + "."));
      System.out.println();
      System.out.println("  " + cyan("./tools/frcprog hints " + lesson.dir) + dim("   read them all again"));
      System.out.println("  " + cyan("./tools/frcprog hint " + lesson.dir + " --reset") + dim("   start over"));
      System.out.println();
      return;
    }

    System.out.println();
    System.out.println(
        dim("  Lesson " + lesson.id + "  ·  hint " + (seen + 1) + " of " + hints.size()));
    System.out.println();
    for (String line : hints.get(seen).split("\n")) {
      System.out.println("  " + renderMarkdownLine(line));
    }
    System.out.println();

    recordHintsSeen(lesson.id, seen + 1);

    if (seen + 1 < hints.size()) {
      System.out.println(
          dim("  Stuck still? ")
              + cyan("./tools/frcprog hint " + lesson.dir)
              + dim("  for the next one."));
    }
    System.out.println();
  }

  /** Splits hints.md on its `## Hint N` headings. */
  private static List<String> splitHints(Lesson lesson) throws IOException {
    Path p = root.resolve("lessons").resolve(lesson.dir).resolve("hints.md");
    if (!Files.exists(p)) {
      return List.of();
    }
    List<String> out = new ArrayList<>();
    StringBuilder current = null;
    for (String line : Files.readString(p).split("\n")) {
      if (line.startsWith("## Hint")) {
        if (current != null) {
          out.add(current.toString().strip());
        }
        current = new StringBuilder();
      }
      if (current != null) {
        current.append(line).append('\n');
      }
    }
    if (current != null) {
      out.add(current.toString().strip());
    }
    return out;
  }

  private static Map<String, Integer> loadHintsSeen() {
    Map<String, Integer> out = new LinkedHashMap<>();
    try {
      if (!Files.exists(progressFile())) {
        return out;
      }
      @SuppressWarnings("unchecked")
      Map<String, Object> m = (Map<String, Object>) Json.parse(Files.readString(progressFile()));
      if (m.get("hints") instanceof Map<?, ?> hm) {
        hm.forEach(
            (k, v) -> out.put(String.valueOf(k), v instanceof Number n ? n.intValue() : 0));
      }
    } catch (Exception ignored) {
      // A corrupt progress file must never block a student.
    }
    return out;
  }

  private static void recordHintsSeen(String id, int count) {
    try {
      Map<String, Boolean> lessons = loadProgress();
      Map<String, Integer> hints = loadHintsSeen();
      hints.put(id, count);

      StringBuilder sb = new StringBuilder("{\n  \"lessons\": {\n");
      int i = 0;
      for (var e : lessons.entrySet()) {
        sb.append("    \"").append(e.getKey()).append("\": ").append(e.getValue());
        sb.append(++i < lessons.size() ? ",\n" : "\n");
      }
      sb.append("  },\n  \"hints\": {\n");
      i = 0;
      for (var e : hints.entrySet()) {
        sb.append("    \"").append(e.getKey()).append("\": ").append(e.getValue());
        sb.append(++i < hints.size() ? ",\n" : "\n");
      }
      sb.append("  }\n}\n");

      Files.createDirectories(progressFile().getParent());
      Files.writeString(progressFile(), sb.toString());
    } catch (IOException ignored) {
      // Tracking is a convenience, not a gate.
    }
  }

  private static void cmdRead(String[] args, String file) throws IOException {
    Lesson l = requireLesson(args);
    Path p = root.resolve("lessons").resolve(l.dir).resolve(file);
    if (!Files.exists(p)) {
      throw new UserError(p + " does not exist");
    }
    System.out.println();
    for (String line : Files.readAllLines(p)) {
      System.out.println("  " + renderMarkdownLine(line));
    }
    System.out.println();
  }

  /** Just enough Markdown rendering to make a terminal read nicely. Not a parser. */
  private static String renderMarkdownLine(String line) {
    if (line.startsWith("# ")) {
      return bold(line.substring(2));
    }
    if (line.startsWith("## ")) {
      return bold(line.substring(3));
    }
    if (line.startsWith("### ")) {
      return bold(line.substring(4));
    }
    if (line.startsWith("> ")) {
      return dim(line);
    }
    if (line.startsWith("```")) {
      return dim("─".repeat(Math.max(3, 60)));
    }
    return line;
  }

  private static int cmdCheck(String[] args) throws Exception {
    if (args.length > 0 && args[0].equals("--all")) {
      return checkAll();
    }

    Lesson l = requireLesson(args);
    if (!l.graded) {
      System.out.println();
      System.out.println(
          yellow("  Lesson " + l.id + " has nothing to grade.")
              + " It is a reading or a setup lesson.");
      System.out.println("  " + cyan("./tools/frcprog read " + l.dir) + " to see what it asks for.");
      System.out.println();
      return 0;
    }

    System.out.println();
    System.out.println(bold("  Grading lesson " + l.id + " — " + l.title));
    System.out.println();

    // Run Gradle silently and read the JUnit XML it leaves behind, so that the
    // only thing the student sees is the report below. If the XML is missing,
    // the build failed before any test ran — almost always a compile error —
    // and THAT output is worth showing verbatim, so it is replayed instead.
    clearResults("lesson-" + l.id);
    Captured build = gradleCaptured("lesson" + l.id);
    Results results = readResults("lesson-" + l.id);

    if (results.total == 0) {
      System.out.println(red("  The project did not compile, so no rubric could run."));
      System.out.println();
      System.out.println(build.output.strip());
      System.out.println();
      return build.exitCode == 0 ? 1 : build.exitCode;
    }
    int exit = build.exitCode;

    System.out.println();
    if (exit == 0) {
      System.out.println(green(bold("  ✓ Lesson " + l.id + " passed")) + dim("  (" + results.total + " checks)"));
      recordProgress(l.id, true);
      suggestNext(l);
    } else {
      System.out.println(
          red(bold("  ✗ Lesson " + l.id + " not passing yet"))
              + dim("  (" + results.passed + " of " + results.total + " checks)"));
      System.out.println();
      for (Failure f : results.failures) {
        System.out.println("  " + red("✗ ") + bold(f.name));
        for (String line : f.message.split("\n")) {
          if (!line.isBlank()) {
            System.out.println("      " + line.strip());
          }
        }
        System.out.println();
      }
      System.out.println(
          dim("  Stuck? ")
              + cyan("./tools/frcprog hint " + l.dir)
              + dim("  gives you one hint. Ask again for the next.")); 
      recordProgress(l.id, false);
    }
    System.out.println();
    return exit;
  }

  private static void suggestNext(Lesson current) throws IOException {
    List<Lesson> lessons = loadLessons();
    Map<String, Boolean> done = loadProgress();
    for (int i = 0; i < lessons.size(); i++) {
      if (lessons.get(i).id.equals(current.id)) {
        for (int j = i + 1; j < lessons.size(); j++) {
          Lesson n = lessons.get(j);
          if (n.graded && !Boolean.TRUE.equals(done.get(n.id))) {
            System.out.println();
            System.out.println("  Next: " + bold(n.id + " — " + n.title));
            System.out.println("        " + cyan("./tools/frcprog read " + n.dir));
            return;
          }
        }
      }
    }
  }

  /**
   * The offline stand-in for continuous integration.
   *
   * <p>An online version of this curriculum would run every rubric on a server and post the results
   * to a pull request. Offline, you run it yourself, and it prints the same board.
   */
  private static int checkAll() throws Exception {
    System.out.println();
    System.out.println(bold("  Running every rubric. This takes a minute."));
    System.out.println();

    gradleCaptured("lessonAll");

    List<Lesson> lessons = loadLessons();
    Results results = readResults("lesson-all");
    Map<String, Boolean> byLesson = new LinkedHashMap<>();

    // JUnit XML gives us class names, not tags, so map results back to lessons
    // through each lesson.json's declared test classes.
    for (Lesson l : lessons) {
      if (!l.graded) {
        continue;
      }
      List<Object> classes = asList(readLessonJson(l).get("tests"));
      boolean allPassed = !classes.isEmpty();
      for (Object cls : classes) {
        String name = String.valueOf(cls);
        if (results.failedClasses.contains(name) || !results.seenClasses.contains(name)) {
          allPassed = false;
        }
      }
      byLesson.put(l.id, allPassed);
      recordProgress(l.id, allPassed);
    }

    int passed = 0;
    System.out.println();
    for (Lesson l : lessons) {
      if (!l.graded) {
        continue;
      }
      boolean ok = Boolean.TRUE.equals(byLesson.get(l.id));
      if (ok) {
        passed++;
      }
      System.out.printf(
          "    %s  %-4s %s%n", ok ? green("✓") : red("✗"), l.id, ok ? l.title : bold(l.title));
    }

    int total = byLesson.size();
    System.out.println();
    System.out.println(
        (passed == total ? green(bold("  ✓ " + passed + " / " + total)) : bold("  " + passed + " / " + total))
            + " graded lessons passing");
    System.out.println();
    return passed == total ? 0 : 1;
  }

  private static int cmdSim() throws Exception {
    System.out.println();
    System.out.println(bold("  Starting the robot simulator."));
    System.out.println();
    System.out.println("  A window called " + bold("Robot Simulation") + " will open. In it:");
    System.out.println("    · drag " + cyan("Keyboard 0") + " onto " + cyan("Joystick[0]") + " to drive with WASD");
    System.out.println("    · drag " + cyan("Keyboard 1") + " onto " + cyan("Joystick[1]") + " for the operator");
    System.out.println("    · click " + cyan("Teleoperated") + " to enable the robot");
    System.out.println();
    System.out.println(
        "  For plots, run " + cyan("./tools/frcprog scope") + " in another terminal and connect");
    System.out.println("  to NetworkTables at " + cyan("localhost") + ".");
    System.out.println();
    System.out.println(dim("  Ctrl-C here when you are done."));
    System.out.println();
    return gradle("simulateJava");
  }

  private static int cmdBuild(String[] args) throws Exception {
    boolean online = args.length > 0 && args[0].equals("--online");
    if (online) {
      System.out.println(
          yellow("  Building with the network enabled — only needed when adding a vendordep."));
    }
    return gradle(online ? new String[] {"build", "--refresh-dependencies"} : new String[] {"build"},
        online);
  }

  private static void cmdScope() throws Exception {
    Path scope = findAdvantageScope();
    if (scope == null) {
      throw new UserError(
          "Could not find AdvantageScope. It ships with WPILib — look for an 'advantagescope'\n"
              + "  folder inside your WPILib install and launch it by hand.");
    }
    System.out.println();
    System.out.println("  Launching " + bold("AdvantageScope") + " from " + dim(scope.toString()));
    System.out.println();
    System.out.println("  Then: " + bold("File → Connect to Simulator") + ", or connect to " + cyan("localhost") + ".");
    System.out.println();

    String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
    ProcessBuilder pb;
    if (os.contains("mac")) {
      pb = new ProcessBuilder("open", "-a", scope.toString());
    } else if (os.contains("win")) {
      pb = new ProcessBuilder("cmd", "/c", "start", "", scope.toString());
    } else {
      pb = new ProcessBuilder(scope.toString());
    }
    pb.inheritIO().start();
  }

  private static void cmdSite() throws Exception {
    // The site lives next to the curriculum in the full repository. A student who
    // copied only curriculum/ will not have it, which is fine — say so plainly
    // rather than failing with a stack trace.
    Path site = root.getParent() == null ? null : root.getParent().resolve("site");
    if (site == null || !Files.exists(site.resolve("mkdocs.yml"))) {
      throw new UserError(
          "The lesson site is not next to this project.\n"
              + "  It lives at <repo>/site. If you copied only the curriculum folder, read the\n"
              + "  lessons in your terminal instead: ./tools/frcprog read <lesson>");
    }
    System.out.println();
    System.out.println("  Serving the lesson site at " + cyan("http://localhost:8000"));
    System.out.println(dim("  Ctrl-C to stop."));
    System.out.println();
    new ProcessBuilder("bash", site.resolve("serve.sh").toString())
        .directory(site.toFile())
        .inheritIO()
        .start()
        .waitFor();
  }

  private static void cmdSolution(String[] args) throws Exception {
    Lesson l = requireLesson(args);
    Path exemplar = root.resolve(".meta/exemplar").resolve(l.dir);
    if (!Files.exists(exemplar)) {
      throw new UserError("No reference answer ships for lesson " + l.id + ".");
    }

    System.out.println();
    System.out.println(yellow(bold("  This will overwrite your work for lesson " + l.id + ".")));
    System.out.println();
    System.out.println("  The reference answer is one way to solve it, not the only way. If your");
    System.out.println("  rubric is failing, " + cyan("./tools/frcprog hints " + l.dir) + " gets you there");
    System.out.println("  with your own code, which is the version you will remember.");
    System.out.println();
    if (!confirm("  Overwrite anyway? [y/N] ")) {
      System.out.println("  Nothing changed.");
      return;
    }

    int copied = copyTree(exemplar, root);
    System.out.println();
    System.out.println(green("  ✓ Restored " + copied + " file(s) from the reference answer."));
    System.out.println("  " + dim("Read them. Then " + cyan("./tools/frcprog reset " + l.dir) + " and do it yourself."));
    System.out.println();
  }

  private static void cmdReset(String[] args) throws Exception {
    Lesson l = requireLesson(args);
    Path starter = root.resolve(".meta/starter");
    if (!Files.exists(starter)) {
      throw new UserError(".meta/starter is missing — cannot restore the original files.");
    }

    List<Object> edits = asList(readLessonJson(l).get("edits"));
    if (edits.isEmpty()) {
      throw new UserError("Lesson " + l.id + " does not declare any files to reset.");
    }

    System.out.println();
    System.out.println(yellow("  This discards your changes to:"));
    for (Object e : edits) {
      System.out.println("    " + e);
    }
    System.out.println();
    if (!confirm("  Reset them to the original starter code? [y/N] ")) {
      System.out.println("  Nothing changed.");
      return;
    }

    int n = 0;
    for (Object e : edits) {
      Path rel = Path.of(String.valueOf(e));
      Path from = starter.resolve(rel);
      if (Files.exists(from)) {
        Files.createDirectories(root.resolve(rel).getParent());
        Files.copy(from, root.resolve(rel), StandardCopyOption.REPLACE_EXISTING);
        n++;
      }
    }
    System.out.println();
    System.out.println(green("  ✓ Reset " + n + " file(s)."));
    System.out.println();
  }

  private static void cmdProgress() throws IOException {
    List<Lesson> lessons = loadLessons();
    Map<String, Boolean> done = loadProgress();

    long graded = lessons.stream().filter(l -> l.graded).count();
    long passed = lessons.stream().filter(l -> l.graded && Boolean.TRUE.equals(done.get(l.id))).count();

    int width = 40;
    int filled = graded == 0 ? 0 : (int) Math.round((double) passed / graded * width);

    System.out.println();
    System.out.println(
        "  "
            + green("█".repeat(filled))
            + dim("░".repeat(width - filled))
            + "  "
            + bold(passed + " / " + graded));
    System.out.println();

    Map<String, int[]> byStage = new TreeMap<>();
    for (Lesson l : lessons) {
      if (!l.graded) {
        continue;
      }
      int[] counts = byStage.computeIfAbsent(l.stage, k -> new int[2]);
      counts[1]++;
      if (Boolean.TRUE.equals(done.get(l.id))) {
        counts[0]++;
      }
    }
    for (var e : byStage.entrySet()) {
      String label = "Stage " + e.getKey();
      boolean complete = e.getValue()[0] == e.getValue()[1];
      System.out.printf(
          "    %s %-10s %d / %d%n",
          complete ? green("✓") : " ", label, e.getValue()[0], e.getValue()[1]);
    }
    System.out.println();
  }

  // ═══════════════════════════════════════════════════════════════════════════
  //  doctor — the most valuable command in this file
  // ═══════════════════════════════════════════════════════════════════════════

  /**
   * Checks the environment before anybody blames their code.
   *
   * <p>Nearly every "it doesn't work" on a team's first meeting is one of half a dozen things, and
   * none of them are Java. This command knows them all, checks them in order, and says what to do —
   * because the alternative is a mentor rediscovering the same six problems every September.
   */
  /**
   * Scaffold a new lesson that already satisfies {@code ./gradlew checkLessons}.
   *
   * <p>The linter is strict on purpose — required headings, required manifest fields, every path in
   * {@code edits} and every class in {@code tests} existing on disk. Written by hand that is a
   * twenty-minute fight with an error list. This emits a skeleton that passes on the first run, so
   * the author spends their time on the prose instead.
   *
   * <p>For a graded lesson it insists you name the file the student edits and the JUnit class that
   * grades them. Those are the two decisions that determine whether a lesson teaches anything, and
   * defaulting them would only mean guessing wrong quietly.
   */
  private static void cmdNewLesson(List<String> args) throws IOException {
    String dirName = null, title = null, testFqcn = null, editPath = null, stage = null;
    boolean graded = false;
    int minutes = 30;
    for (int i = 0; i < args.size(); i++) {
      String a = args.get(i);
      switch (a) {
        case "--graded" -> graded = true;
        case "--title" -> title = args.get(++i);
        case "--test" -> testFqcn = args.get(++i);
        case "--edits" -> editPath = args.get(++i);
        case "--stage" -> stage = args.get(++i);
        case "--minutes" -> minutes = Integer.parseInt(args.get(++i));
        default -> {
          if (a.startsWith("-")) throw new UserError("Unknown flag " + a);
          dirName = a;
        }
      }
    }
    if (dirName == null) {
      throw new UserError(
          "Usage: frcprog new-lesson <NN>-<slug> [--graded --test <FQCN> --edits <path>]");
    }
    if (!dirName.matches("[0-9][0-9A-Za-z]-[a-z0-9-]+")) {
      throw new UserError(
          "Lesson directory must look like 31-swerve-odometry (id, dash, lowercase slug); got "
              + dirName);
    }
    String id = dirName.substring(0, 2).toUpperCase(Locale.ROOT);
    if (graded && (testFqcn == null || editPath == null)) {
      throw new UserError(
          "A graded lesson needs --test <FQCN> (the JUnit class that grades it) and --edits <path>"
              + " (the file the student changes). Both are checked by ./gradlew checkLessons.");
    }
    if (title == null) {
      String[] words = dirName.substring(3).split("-");
      StringBuilder sb = new StringBuilder();
      for (String w : words) {
        sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1)).append(' ');
      }
      title = sb.toString().trim();
    }
    if (stage == null) stage = id;

    Path dir = root.resolve("lessons").resolve(dirName);
    if (Files.exists(dir)) throw new UserError("lessons/" + dirName + " already exists.");

    List<Lesson> existing = loadLessons();
    for (Lesson l : existing) {
      if (l.id.equalsIgnoreCase(id)) {
        throw new UserError("Lesson id " + id + " is already taken by " + l.dir + ".");
      }
    }
    String prevDir = existing.isEmpty() ? null : existing.get(existing.size() - 1).dir;

    Path testFile = null;
    if (graded) {
      testFile = root.resolve("src/test/java/" + testFqcn.replace('.', '/') + ".java");
      if (!Files.exists(root.resolve(editPath))) {
        throw new UserError(
            "--edits points at " + editPath + ", which does not exist. Create the starter first.");
      }
    }

    Files.createDirectories(dir);

    String checkIt =
        graded
            ? String.join(
                "\n",
                "",
                "## Check it",
                "",
                "```bash",
                "./tools/frcprog check " + dirName,
                "```",
                "",
                "- TODO: one bullet per assertion, in the words a student would use",
                "",
                "## See it",
                "",
                "```bash",
                "./tools/frcprog sim",
                "```",
                "",
                "TODO: what to open in the Simulation GUI or AdvantageScope, and what a correct",
                "run looks like versus a wrong one.",
                "")
            : "";

    Files.writeString(
        dir.resolve("README.md"),
        String.join(
            "\n",
            "# " + id + " — " + title,
            "",
            "> **Stage " + stage + " · ~" + minutes + " min"
                + (prevDir == null ? "" : " · after [" + prevDir + "](../" + prevDir + ")") + "**",
            "",
            "TODO: one paragraph. What goes wrong on a real robot without this? Lead with the",
            "problem, not the feature — the concept lands when it is the answer to something.",
            "",
            "## Do this",
            "",
            "1. TODO",
            "2. TODO",
            checkIt,
            "## Done",
            "",
            "TODO: how the student knows they are finished, and what comes next.",
            ""));

    Files.writeString(
        dir.resolve("hints.md"),
        String.join(
            "\n",
            "# Hints — " + id + " " + title,
            "",
            "## Hint 1 — Where to start",
            "",
            "TODO: a nudge toward the right question. No code.",
            "",
            "## Hint 2 — The shape of the answer",
            "",
            "TODO: describe the structure. Still no code.",
            "",
            "## Hint 3 — Almost there",
            "",
            "TODO: near-answer scaffolding with the key part left blank.",
            "",
            "## Hint 4 — Reference answer",
            "",
            "<details>",
            "<summary>The answer</summary>",
            "",
            "```java",
            "// TODO",
            "```",
            "",
            "TODO: and a version that looks right but is not, with the reason why.",
            "",
            "</details>",
            ""));

    StringBuilder j = new StringBuilder("{\n");
    j.append("  \"slug\": \"").append(dirName).append("\",\n");
    j.append("  \"title\": \"").append(title).append("\",\n");
    j.append("  \"stage\": \"").append(stage).append("\",\n");
    if (graded) j.append("  \"tag\": \"lesson-").append(id).append("\",\n");
    j.append("  \"prerequisites\": [")
        .append(prevDir == null ? "" : "\n    \"" + prevDir + "\"\n  ")
        .append("],\n");
    if (graded) {
      j.append("  \"edits\": [\n    \"").append(editPath).append("\"\n  ],\n");
      j.append("  \"tests\": [\n    \"").append(testFqcn).append("\"\n  ],\n");
    }
    j.append("  \"estimatedMinutes\": ").append(minutes).append("\n}\n");
    Files.writeString(dir.resolve("lesson.json"), j.toString());

    if (graded && !Files.exists(testFile)) {
      Files.createDirectories(testFile.getParent());
      String pkg = testFqcn.substring(0, testFqcn.lastIndexOf('.'));
      String cls = testFqcn.substring(testFqcn.lastIndexOf('.') + 1);
      Files.writeString(
          testFile,
          String.join(
              "\n",
              "package " + pkg + ";",
              "",
              "import static org.junit.jupiter.api.Assertions.assertEquals;",
              "",
              "import org.junit.jupiter.api.DisplayName;",
              "import org.junit.jupiter.api.Tag;",
              "import org.junit.jupiter.api.Test;",
              "",
              "/**",
              " * Rubric for lesson " + id + " — " + title + ".",
              " *",
              " * <p>Write the assertions before the lesson prose. A rubric is the specification;"
                  + " the prose",
              " * only has to get the student to it. Every @DisplayName is shown to the student when"
                  + " it fails,",
              " * so write them as advice (\"a small reading should collapse to zero\"), not as"
                  + " labels (\"test 1\").",
              " */",
              "@Tag(\"lesson\")",
              "@Tag(\"lesson-" + id + "\")",
              "class " + cls + " {",
              "",
              "  @Test",
              "  @DisplayName(\"1. TODO: say what should be true, in the student's words\")",
              "  void firstAssertion() {",
              "    // This deliberately fails until you write the rubric. A lesson whose rubric",
              "    // passes on the untouched starter grades nothing at all.",
              "    assertEquals(1, 0, \"TODO: replace with a real assertion\");",
              "  }",
              "}",
              ""));
    }

    // Append to the manifest by insertion rather than reserialising, so the
    // existing hand-tuned formatting and comments-in-description survive.
    Path manifest = root.resolve("lessons/manifest.json");
    String text = Files.readString(manifest);
    int close = text.lastIndexOf("\n  ]");
    if (close < 0) throw new UserError("Could not find the end of the lessons array in manifest.json");
    String entry =
        ",\n    {\n"
            + "      \"id\": \"" + id + "\",\n"
            + "      \"dir\": \"" + dirName + "\",\n"
            + "      \"title\": \"" + title + "\",\n"
            + "      \"stage\": \"" + stage + "\",\n"
            + "      \"track\": \"core\",\n"
            + "      \"graded\": " + graded + ",\n"
            + "      \"estimatedMinutes\": " + minutes + "\n"
            + "    }";
    Files.writeString(manifest, text.substring(0, close) + entry + text.substring(close));

    System.out.println();
    System.out.println(green("  ✓ ") + bold("lessons/" + dirName) + " created");
    System.out.println("     " + dim("README.md · hints.md · lesson.json"));
    if (graded) {
      System.out.println(green("  ✓ ") + "src/test/java/" + testFqcn.replace('.', '/') + ".java");
      System.out.println("     " + dim("tagged lesson / lesson-" + id + ", failing on purpose"));
    }
    System.out.println(green("  ✓ ") + "lessons/manifest.json updated");
    System.out.println();
    System.out.println(bold("  Next"));
    System.out.println("     1. Write the rubric assertions first — they are the specification.");
    System.out.println("     2. Fill the TODOs in README.md and hints.md.");
    if (graded) {
      System.out.println("     3. Add the answer to .meta/make-exemplars.py, then:");
      System.out.println("           " + cyan("python3 .meta/make-exemplars.py"));
      System.out.println("           " + cyan("bash .meta/verify-rubrics.sh"));
      System.out.println(
          "        " + dim("which proves it fails on the starter and passes on the answer."));
    }
    System.out.println("     " + (graded ? "4" : "3") + ". " + cyan("./gradlew checkLessons"));
    System.out.println();
  }

  private static int cmdDoctor() throws Exception {
    System.out.println();
    System.out.println(bold("  frcprog doctor"));
    System.out.println();

    List<String> failures = new ArrayList<>();
    List<String> warnings = new ArrayList<>();

    // ── 1. The JDK ───────────────────────────────────────────────────────────
    // The classic failure: a student has some system Java on their PATH, runs
    // `gradle` instead of `./gradlew`, and gets "Unsupported class file major
    // version" — an error that tells a beginner precisely nothing.
    String javaHome = System.getProperty("java.home");
    String javaVersion = System.getProperty("java.version");
    boolean isWpilibJdk = javaHome != null && javaHome.replace('\\', '/').contains("/wpilib/");
    report(
        isWpilibJdk,
        "Running on WPILib's bundled JDK",
        "Running on Java " + javaVersion + " from " + javaHome,
        isWpilibJdk
            ? null
            : "That is not WPILib's JDK. It may work, but the supported setup is WPILib's own.\n"
                + "      Always use ./gradlew (never a system `gradle`), and open the project in\n"
                + "      the VS Code that WPILib installed, not a separate one.",
        failures,
        warnings,
        false);

    report(
        javaVersion != null && javaVersion.startsWith("17"),
        "Java 17",
        "Java " + javaVersion,
        "WPILib 2026 targets Java 17. Other versions usually work for the lessons but are not\n"
            + "      what the season is tested against.",
        failures,
        warnings,
        false);

    // ── 2. The WPILib install and its offline Maven repository ──────────────
    Path wpilib = findWpilibHome();
    report(
        wpilib != null,
        "WPILib 2026 installed" + (wpilib == null ? "" : " at " + wpilib),
        "WPILib 2026 not found",
        "Install it from https://docs.wpilib.org — this curriculum needs the 2026 release.\n"
            + "      Nothing else here will work until it is installed.",
        failures,
        warnings,
        true);

    if (wpilib != null) {
      Path maven = wpilib.resolve("maven");
      boolean hasMaven = Files.isDirectory(maven.resolve("edu/wpi/first"));
      report(
          hasMaven,
          "Offline Maven repository present",
          "Offline Maven repository missing",
          "Your WPILib install has no maven/ folder, so Gradle would have to download\n"
              + "      everything from the internet. Re-run the WPILib installer and choose the\n"
              + "      option that includes the offline artifacts.",
          failures,
          warnings,
          true);
    }

    // ── 3. Where the project lives ──────────────────────────────────────────
    String path = root.toString();
    boolean cloudSynced =
        path.contains("OneDrive")
            || path.contains("Dropbox")
            || path.contains("Google Drive")
            || path.contains("com~apple~CloudDocs");
    report(
        !cloudSynced,
        "Project is on local disk",
        "Project is inside a cloud-synced folder",
        "Gradle keeps file locks that fight cloud sync, and builds fail at random with errors\n"
            + "      that look like corruption. Move the project to somewhere like ~/dev or C:\\dev.",
        failures,
        warnings,
        false);

    boolean asciiPath = path.chars().allMatch(ch -> ch < 128);
    report(
        asciiPath,
        "Project path is plain ASCII",
        "Project path contains non-ASCII characters",
        "Some native toolchain components mishandle these. If builds fail strangely, this is\n"
            + "      the first thing to rule out.",
        failures,
        warnings,
        false);

    // ── 4. The build itself, offline ────────────────────────────────────────
    System.out.println("    " + dim("running a build offline, this may take a moment…"));
    int build = gradleQuiet("compileJava");
    report(
        build == 0,
        "Project compiles offline",
        "Project does not compile offline",
        "Run ./gradlew compileJava to see the errors. If they mention downloading or\n"
            + "      resolving dependencies, the offline Maven repository above is the problem.\n"
            + "      If they are Java errors, they are in your lesson code — that is normal, and\n"
            + "      `frcprog check` will tell you which lesson.",
        failures,
        warnings,
        true);

    // ── 5. Nice-to-haves ────────────────────────────────────────────────────
    Path scope = findAdvantageScope();
    report(
        scope != null,
        "AdvantageScope available",
        "AdvantageScope not found",
        "It normally ships with WPILib. Without it you can still do every lesson; you just\n"
            + "      cannot see the plots, and Stage 1C onward is much less rewarding blind.",
        failures,
        warnings,
        false);

    boolean hasExemplars = Files.isDirectory(root.resolve(".meta/exemplar"));
    report(
        hasExemplars,
        "Reference answers and starter snapshots present",
        "Reference answers missing",
        "`frcprog solution` and `frcprog reset` will not work. Not fatal.",
        failures,
        warnings,
        false);

    // ── verdict ─────────────────────────────────────────────────────────────
    System.out.println();
    if (failures.isEmpty() && warnings.isEmpty()) {
      System.out.println(green(bold("  ✓ Everything checks out. Run ./tools/frcprog next.")));
      System.out.println();
      return 0;
    }
    if (failures.isEmpty()) {
      System.out.println(
          yellow(bold("  " + warnings.size() + " warning(s)"))
              + " — you can work, but read the notes above.");
      System.out.println();
      return 0;
    }
    System.out.println(red(bold("  ✗ " + failures.size() + " problem(s) must be fixed first.")));
    System.out.println();
    return 1;
  }

  private static void report(
      boolean ok,
      String okText,
      String badText,
      String advice,
      List<String> failures,
      List<String> warnings,
      boolean fatal) {
    if (ok) {
      System.out.println("    " + green("✓") + " " + okText);
      return;
    }
    System.out.println("    " + (fatal ? red("✗") : yellow("!")) + " " + badText);
    if (advice != null) {
      for (String line : advice.split("\n")) {
        System.out.println("      " + dim(line.strip().isEmpty() ? "" : line.replaceFirst("^ {6}", "")));
      }
    }
    (fatal ? failures : warnings).add(badText);
  }

  // ═══════════════════════════════════════════════════════════════════════════
  //  Plumbing
  // ═══════════════════════════════════════════════════════════════════════════

  private static int gradle(String task) throws Exception {
    return gradle(new String[] {task}, false);
  }

  private static int gradle(String[] tasks, boolean online) throws Exception {
    List<String> cmd = new ArrayList<>();
    cmd.add(isWindows() ? "cmd" : root.resolve("gradlew").toString());
    if (isWindows()) {
      cmd.add("/c");
      cmd.add(root.resolve("gradlew.bat").toString());
    }
    cmd.addAll(Arrays.asList(tasks));

    ProcessBuilder pb = new ProcessBuilder(cmd).directory(root.toFile()).inheritIO();
    if (online) {
      pb.environment().put("FRCPROG_ONLINE", "1");
    }
    return pb.start().waitFor();
  }

  /** A finished Gradle run: what it printed, and whether it succeeded. */
  private record Captured(String output, int exitCode) {}

  /**
   * Runs a Gradle task without letting it print anything, and hands back what it would have said.
   *
   * <p>Used by {@code check}, which wants to present failures as advice rather than as forty lines
   * of stack trace — but still needs the raw output available for the case where the build broke
   * before any test could run.
   */
  private static Captured gradleCaptured(String task) throws Exception {
    List<String> cmd = new ArrayList<>();
    if (isWindows()) {
      cmd.addAll(List.of("cmd", "/c", root.resolve("gradlew.bat").toString()));
    } else {
      cmd.add(root.resolve("gradlew").toString());
    }
    cmd.addAll(List.of(task, "--console=plain", "-q"));

    Process p =
        new ProcessBuilder(cmd).directory(root.toFile()).redirectErrorStream(true).start();
    StringBuilder sb = new StringBuilder();
    try (BufferedReader in =
        new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
      String line;
      while ((line = in.readLine()) != null) {
        sb.append(line).append('\n');
      }
    }
    return new Captured(sb.toString(), p.waitFor());
  }

  private static int gradleQuiet(String task) throws Exception {
    List<String> cmd = new ArrayList<>();
    if (isWindows()) {
      cmd.addAll(List.of("cmd", "/c", root.resolve("gradlew.bat").toString()));
    } else {
      cmd.add(root.resolve("gradlew").toString());
    }
    cmd.add(task);
    cmd.add("-q");
    Process p =
        new ProcessBuilder(cmd)
            .directory(root.toFile())
            .redirectErrorStream(true)
            .redirectOutput(ProcessBuilder.Redirect.DISCARD)
            .start();
    return p.waitFor();
  }

  private static boolean isWindows() {
    return System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("win");
  }

  private static boolean confirm(String prompt) throws IOException {
    System.out.print(prompt);
    System.out.flush();
    BufferedReader in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
    String answer = in.readLine();
    return answer != null && answer.strip().toLowerCase(Locale.ROOT).startsWith("y");
  }

  private static int copyTree(Path from, Path to) throws IOException {
    int[] count = {0};
    Files.walkFileTree(
        from,
        new SimpleFileVisitor<>() {
          @Override
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Path target = to.resolve(from.relativize(file));
            Files.createDirectories(target.getParent());
            Files.copy(file, target, StandardCopyOption.REPLACE_EXISTING);
            count[0]++;
            return FileVisitResult.CONTINUE;
          }
        });
    return count[0];
  }

  private static Path findProjectRoot() {
    Path here = Path.of("").toAbsolutePath();
    for (Path p = here; p != null; p = p.getParent()) {
      if (Files.exists(p.resolve("lessons/manifest.json")) && Files.exists(p.resolve("build.gradle"))) {
        return p;
      }
    }
    // Fall back to the directory two levels above this source file (tools/).
    return here;
  }

  private static Path findWpilibHome() {
    List<Path> candidates = new ArrayList<>();
    if (isWindows()) {
      String pub = System.getenv("PUBLIC");
      candidates.add(Path.of(pub == null ? "C:\\Users\\Public" : pub, "wpilib", "2026"));
    } else {
      candidates.add(Path.of(System.getProperty("user.home"), "wpilib", "2026"));
    }
    for (Path p : candidates) {
      if (Files.isDirectory(p)) {
        return p;
      }
    }
    return null;
  }

  private static Path findAdvantageScope() {
    Path wpilib = findWpilibHome();
    if (wpilib == null) {
      return null;
    }
    Path dir = wpilib.resolve("advantagescope");
    if (!Files.isDirectory(dir)) {
      return null;
    }
    try (var s = Files.list(dir)) {
      return s.filter(
              p -> {
                String n = p.getFileName().toString().toLowerCase(Locale.ROOT);
                return n.endsWith(".app") || n.endsWith(".exe") || n.startsWith("advantagescope");
              })
          .findFirst()
          .orElse(dir);
    } catch (IOException e) {
      return dir;
    }
  }

  // ─── Lessons ───────────────────────────────────────────────────────────────

  private record Lesson(
      String id, String dir, String title, String stage, String track, boolean graded, int estimatedMinutes) {}

  @SuppressWarnings("unchecked")
  private static List<Lesson> loadLessons() throws IOException {
    Map<String, Object> manifest =
        (Map<String, Object>) Json.parse(Files.readString(root.resolve("lessons/manifest.json")));
    List<Lesson> out = new ArrayList<>();
    for (Object o : asList(manifest.get("lessons"))) {
      Map<String, Object> m = (Map<String, Object>) o;
      out.add(
          new Lesson(
              str(m.get("id")),
              str(m.get("dir")),
              str(m.get("title")),
              str(m.get("stage")),
              m.get("track") == null ? "core" : str(m.get("track")),
              Boolean.TRUE.equals(m.get("graded")),
              m.get("estimatedMinutes") instanceof Number n ? n.intValue() : 0));
    }
    return out;
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> readLessonJson(Lesson l) throws IOException {
    Path p = root.resolve("lessons").resolve(l.dir).resolve("lesson.json");
    if (!Files.exists(p)) {
      return Map.of();
    }
    return (Map<String, Object>) Json.parse(Files.readString(p));
  }

  private static Lesson requireLesson(String[] args) throws IOException {
    if (args.length == 0) {
      throw new UserError("Which lesson? Try `frcprog list` or `frcprog next`.");
    }
    String key = args[0].toLowerCase(Locale.ROOT);
    List<Lesson> lessons = loadLessons();
    for (Lesson l : lessons) {
      if (l.id.equalsIgnoreCase(key) || l.dir.equalsIgnoreCase(key)) {
        return l;
      }
    }
    // Be forgiving: "07" should also match "07-tank-drive", and vice versa.
    for (Lesson l : lessons) {
      if (l.dir.toLowerCase(Locale.ROOT).startsWith(key)
          || l.dir.toLowerCase(Locale.ROOT).contains(key)) {
        return l;
      }
    }
    throw new UserError("No lesson matches '" + args[0] + "'. Try `frcprog list`.");
  }

  // ─── Progress ──────────────────────────────────────────────────────────────

  private static Path progressFile() {
    return root.resolve(".frcprog/progress.json");
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Boolean> loadProgress() {
    Map<String, Boolean> out = new LinkedHashMap<>();
    try {
      if (!Files.exists(progressFile())) {
        return out;
      }
      Map<String, Object> m = (Map<String, Object>) Json.parse(Files.readString(progressFile()));
      Object lessons = m.get("lessons");
      if (lessons instanceof Map<?, ?> lm) {
        lm.forEach((k, v) -> out.put(String.valueOf(k), Boolean.TRUE.equals(v)));
      }
    } catch (Exception ignored) {
      // A corrupt progress file must never block a student. Worst case they see
      // everything as not-yet-done and re-run a check.
    }
    return out;
  }

  private static void recordProgress(String id, boolean passed) {
    try {
      Map<String, Boolean> all = loadProgress();
      all.put(id, passed);
      StringBuilder sb = new StringBuilder();
      sb.append("{\n  \"lessons\": {\n");
      int i = 0;
      for (var e : all.entrySet()) {
        sb.append("    \"").append(e.getKey()).append("\": ").append(e.getValue());
        sb.append(++i < all.size() ? ",\n" : "\n");
      }
      sb.append("  }\n}\n");
      Files.createDirectories(progressFile().getParent());
      Files.writeString(progressFile(), sb.toString());
    } catch (IOException ignored) {
      // Progress tracking is a convenience, not a gate.
    }
  }

  // ─── JUnit XML ─────────────────────────────────────────────────────────────

  private record Failure(String name, String message) {}

  private static final class Results {
    int total;
    int passed;
    final List<Failure> failures = new ArrayList<>();
    final List<String> failedClasses = new ArrayList<>();
    final List<String> seenClasses = new ArrayList<>();
  }

  /**
   * Reads Gradle's JUnit XML so failures can be printed as advice rather than as a stack trace.
   *
   * <p>Hand-rolled rather than using an XML parser, for the same reason as the JSON parser below:
   * the whole tool has to run from a single source file with no dependencies.
   */
  private static Results readResults(String reportDir) {
    Results r = new Results();
    Path dir = root.resolve("build/test-results").resolve(reportDir);
    if (!Files.isDirectory(dir)) {
      return r;
    }
    try (var files = Files.list(dir)) {
      for (Path f : files.filter(p -> p.toString().endsWith(".xml")).toList()) {
        String xml = Files.readString(f);
        String className = attr(xml, "<testsuite ", "name");
        if (className != null) {
          r.seenClasses.add(className);
        }
        for (String caseXml : xml.split("<testcase ")) {
          if (!caseXml.contains("name=")) {
            continue;
          }
          r.total++;
          String name = attrOfFragment(caseXml, "name");
          if (caseXml.contains("<failure") || caseXml.contains("<error")) {
            if (className != null && !r.failedClasses.contains(className)) {
              r.failedClasses.add(className);
            }
            r.failures.add(new Failure(unescape(name), extractFailureMessage(caseXml)));
          } else {
            r.passed++;
          }
        }
      }
    } catch (IOException ignored) {
      // No results is the same as no information; the exit code still governs.
    }
    return r;
  }

  private static String extractFailureMessage(String caseXml) {
    String msg = attrOfFragment(caseXml.substring(Math.max(0, caseXml.indexOf("<failure"))), "message");
    if (msg == null) {
      return "(no message)";
    }
    msg = unescape(msg);

    // Assertion messages in this curriculum are written to be read; the stack
    // trace after them is not, so cut it off.
    int at = msg.indexOf("\n\tat ");
    if (at > 0) {
      msg = msg.substring(0, at);
    }

    // Drop the exception class name. "org.opentest4j.AssertionFailedError:" in
    // front of a carefully-written sentence tells a beginner nothing except that
    // something scary happened.
    int colon = msg.indexOf(": ");
    if (colon > 0 && colon < 60 && msg.substring(0, colon).matches("[a-zA-Z0-9.$]+")) {
      msg = msg.substring(colon + 2);
    }
    return msg.strip();
  }

  private static String attr(String xml, String tag, String name) {
    int i = xml.indexOf(tag);
    return i < 0 ? null : attrOfFragment(xml.substring(i), name);
  }

  private static String attrOfFragment(String fragment, String name) {
    String needle = name + "=\"";
    int i = fragment.indexOf(needle);
    if (i < 0) {
      return null;
    }
    int start = i + needle.length();
    int end = fragment.indexOf('"', start);
    return end < 0 ? null : fragment.substring(start, end);
  }

  private static String unescape(String s) {
    return s == null
        ? ""
        : s.replace("&quot;", "\"")
            .replace("&apos;", "'")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&#10;", "\n")
            .replace("&#13;", "")
            .replace("&#9;", "\t")
            .replace("&amp;", "&");
  }

  // ─── Small helpers ─────────────────────────────────────────────────────────

  private static List<Object> asList(Object o) {
    return o instanceof List<?> l ? new ArrayList<>(l) : List.of();
  }

  private static String str(Object o) {
    return o == null ? "" : String.valueOf(o);
  }

  private static final class UserError extends RuntimeException {
    UserError(String message) {
      super(message);
    }
  }

  // ═══════════════════════════════════════════════════════════════════════════
  //  A minimal JSON reader.
  //
  //  Java has no JSON support in the standard library, and this tool refuses to
  //  take a dependency, so here are ~90 lines that handle the subset our own
  //  manifest files use. Not a general-purpose parser; do not lift it.
  // ═══════════════════════════════════════════════════════════════════════════
  private static final class Json {
    private final String s;
    private int i;

    private Json(String s) {
      this.s = s;
    }

    static Object parse(String text) {
      Json p = new Json(text);
      p.ws();
      Object v = p.value();
      p.ws();
      return v;
    }

    private Object value() {
      char ch = s.charAt(i);
      return switch (ch) {
        case '{' -> object();
        case '[' -> array();
        case '"' -> string();
        case 't' -> literal("true", Boolean.TRUE);
        case 'f' -> literal("false", Boolean.FALSE);
        case 'n' -> literal("null", null);
        default -> number();
      };
    }

    private Map<String, Object> object() {
      Map<String, Object> m = new LinkedHashMap<>();
      i++; // {
      ws();
      if (s.charAt(i) == '}') {
        i++;
        return m;
      }
      while (true) {
        ws();
        String key = string();
        ws();
        i++; // :
        ws();
        m.put(key, value());
        ws();
        if (s.charAt(i) == ',') {
          i++;
        } else {
          i++; // }
          return m;
        }
      }
    }

    private List<Object> array() {
      List<Object> out = new ArrayList<>();
      i++; // [
      ws();
      if (s.charAt(i) == ']') {
        i++;
        return out;
      }
      while (true) {
        ws();
        out.add(value());
        ws();
        if (s.charAt(i) == ',') {
          i++;
        } else {
          i++; // ]
          return out;
        }
      }
    }

    private String string() {
      StringBuilder sb = new StringBuilder();
      i++; // opening quote
      while (s.charAt(i) != '"') {
        char ch = s.charAt(i++);
        if (ch == '\\') {
          char esc = s.charAt(i++);
          switch (esc) {
            case 'n' -> sb.append('\n');
            case 't' -> sb.append('\t');
            case 'r' -> sb.append('\r');
            case 'b' -> sb.append('\b');
            case 'f' -> sb.append('\f');
            case 'u' -> {
              sb.append((char) Integer.parseInt(s.substring(i, i + 4), 16));
              i += 4;
            }
            default -> sb.append(esc);
          }
        } else {
          sb.append(ch);
        }
      }
      i++; // closing quote
      return sb.toString();
    }

    private Object number() {
      int start = i;
      while (i < s.length() && "-+.eE0123456789".indexOf(s.charAt(i)) >= 0) {
        i++;
      }
      String text = s.substring(start, i);
      return text.contains(".") || text.contains("e") || text.contains("E")
          ? Double.parseDouble(text)
          : Long.parseLong(text);
    }

    private Object literal(String word, Object v) {
      i += word.length();
      return v;
    }

    private void ws() {
      while (i < s.length() && Character.isWhitespace(s.charAt(i))) {
        i++;
      }
    }
  }
}
