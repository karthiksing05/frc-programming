# Lesson 0A — First-run install

> **Stage 0 · ~60 minutes (40 working, 20 waiting on downloads) · No prerequisites**

This is the lesson that decides whether you learn FRC programming at all.

Not because it is hard — it is the easiest lesson here — but because it is the one
people quit on. A download stalls, an installer asks a question nobody warned them
about, an error message mentions a "class file major version", and a perfectly
capable person concludes that programming is not for them. It is not a fair test
and you should not let it be one.

So: one thing at a time, in order, and a tool at the end that tells you whether it
worked.

## What you'll learn

1. What WPILib actually installs, and why it insists on bringing its own Java and
   its own VS Code rather than using yours.
2. How to get a copy of this curriculum onto your machine.
3. How to run `frcprog doctor` and read what it tells you.

## What you'll do

### 1. Install WPILib 2026

Download the installer for your operating system from
[docs.wpilib.org](https://docs.wpilib.org/en/stable/docs/zero-to-robot/step-2/wpilib-setup.html)
and run it. Choose **Everything** and **Install for this User** when asked.

It is a 2–3 GB download. Do this at home, on a network you control. Fifty students
installing simultaneously on school Wi-Fi at the first meeting of the year is a
tradition, and it is a bad one.

What lands on your disk:

| Piece | Why it exists |
|---|---|
| A Java 17 JDK | So your code compiles the same way on every machine on your team. |
| Its own copy of VS Code | Pre-configured, with the WPILib extension already installed. |
| An offline Maven repository | Every library this project needs, already downloaded. **This is what makes the rest of the curriculum work without a network.** |
| AdvantageScope | The plotting and visualisation tool you will live in from Stage 1C onward. |
| Simulation tools | The robot simulator, driver station, and log tools. |

**Use WPILib's VS Code, not yours.** If you already had VS Code installed, you now
have two. The WPILib one knows where its Java is; a normal one does not, and the
resulting errors are cryptic in a way that wastes entire afternoons. On Windows and
Linux the installer offers to make a separate shortcut — use it. On macOS it is in
your Applications folder as *WPILib VS Code 2026*.

### 2. Get the curriculum onto your machine

Copy the `curriculum` folder from wherever your mentor gave it to you — a USB
stick, a shared drive, a zip file — to somewhere on your own disk.

**Where you put it matters.** Put it somewhere short and local:

- macOS / Linux: `~/dev/frc-curriculum`
- Windows: `C:\dev\frc-curriculum`

**Not** inside OneDrive, iCloud Drive, Dropbox, or Google Drive. Those services
sync files while the build system is holding them open, and the result is builds
that fail at random with errors that look like file corruption. You will not guess
that the cloud sync is the cause. Nobody ever does.

### 3. Open it and check yourself

Open WPILib VS Code, then **File → Open Folder**, and choose the folder you just
copied. Open a terminal inside VS Code (**Terminal → New Terminal**) and run:

```bash
./tools/frcprog doctor
```

On Windows, `tools\frcprog.cmd doctor`.

This checks the half-dozen things that actually go wrong, and for each one it
tells you what to do. Work down the list until everything is green.

Then:

```bash
./gradlew build
```

The first run takes a couple of minutes: Gradle is unpacking itself and reading the
libraries out of the offline repository your WPILib install provided. Later builds
take a few seconds.

You are looking for `BUILD SUCCESSFUL`.

## Done?

`./tools/frcprog doctor` is green and `./gradlew build` says `BUILD SUCCESSFUL`.

That is a genuine milestone, and it is worth being clear about what it proves: your
JDK works, your build system works, the libraries are present, and the simulation
layer loaded. Every problem from here on is a problem with code you wrote — which
is a much better kind of problem to have.

```bash
./tools/frcprog next
```

## When it goes wrong

Ranked by how often it actually happens.

**"Unsupported class file major version"** — Gradle is using some other Java, not
WPILib's. You almost certainly typed `gradle` instead of `./gradlew`. The leading
`./` and the `w` both matter: `gradlew` is a small script in this folder that finds
the right Java for you.

**The build hangs trying to download something** — something is reaching the
network when it should not. Check that `settings.gradle` still contains the
`frcHome` maven block; that is what points Gradle at your local WPILib install.

**Windows Firewall popup on first run** — click **Allow**. It is the JVM asking to
open a local network port so the simulator and AdvantageScope can talk to each
other. Clicking Cancel out of reflex means your plots never connect, and the
symptom shows up two lessons later looking like a completely different problem.

**Builds are extremely slow on Windows** — antivirus is scanning every file Gradle
touches. Ask whoever administers the machine to exclude your project folder and
your `.gradle` folder. This routinely turns a twenty-minute build into a
two-minute one.

**macOS says the app is from an unidentified developer** — right-click the app and
choose **Open**, rather than double-clicking it.

Still stuck? Write down the exact error text — not a paraphrase — and ask. "It
doesn't work" is unanswerable; the actual message is usually enough for someone to
recognise the problem immediately.
