# The `frcprog` command

One command line for the whole curriculum. It runs on the JDK that WPILib installed,
so there is nothing extra to install and nothing to build.

=== "macOS / Linux"

    ```bash
    ./tools/frcprog <command>
    ```

=== "Windows"

    ```bat
    tools\frcprog.cmd <command>
    ```

`<lesson>` below accepts an id (`07`) or a slug (`07-tank-drive`).

---

## Working through a lesson

`frcprog next`
:   What to do now, which file to open, and the two commands you will need. Run this
    when you do not know what to do.

`frcprog read <lesson>`
:   The full lesson text, in your terminal. The same text is on this site, formatted
    more nicely; use whichever you will actually read.

`frcprog check <lesson>`
:   Runs the rubric and grades you. Failures are printed as advice, not as stack
    traces — the assertion messages are written to be read.

`frcprog hints <lesson>`
:   Four hints, escalating. The answer is only in the last one.

---

## Seeing it move

`frcprog sim`
:   Starts the robot simulator. Drag **Keyboard 0** onto **Joystick[0]** to drive
    with WASD, then click **Teleoperated**.

`frcprog scope`
:   Launches AdvantageScope. Then **File → Connect to Simulator**, or connect to
    `localhost`.

`frcprog site`
:   Serves this site from your own machine at `localhost:8000`.

---

## Where am I

`frcprog list`
:   Every lesson, its stage, and its status.

    ```
    ✓ passed   ○ not yet   ◇ nothing to grade   ⬇ needs a download
    ```

`frcprog progress`
:   A bar, and a per-stage breakdown.

`frcprog check --all`
:   Every rubric at once. This is the offline stand-in for continuous integration —
    the online version of this curriculum would run exactly this on a server and post
    the results to a pull request. It takes about a minute.

    Run it after every refactor. A green board is what makes restructuring code safe
    rather than frightening.

---

## Getting unstuck

`frcprog reset <lesson>`
:   Restores the pristine starter code for that lesson's files. Asks first.

    It restores from `.meta/starter/` rather than from Git, deliberately — a student
    handed this project on a USB stick may not have a repository, and "you cannot
    undo because you did not set up version control" is a terrible thing to say to
    somebody who has just broken their lesson.

`frcprog solution <lesson>`
:   Overwrites your work with the reference answer. Asks first, and tries to talk
    you out of it.

    It is here because it is better than being stuck. Read the diff rather than the
    result — the useful part is seeing where your choices differed — and then
    `reset` and do it yourself. That version is the one you will remember.

`frcprog doctor`
:   Checks your environment before you start blaming your code. JDK, WPILib install,
    offline Maven repository, project location, and a real offline compile.

    When something inexplicable is happening, run this first.

`frcprog build [--online]`
:   A plain build. `--online` enables the network for exactly one build, which you
    need only when adding a vendor library.

---

## Progress, and where it lives

`.frcprog/progress.json`, in the project folder. Written when you run `check`.

It goes nowhere else. There is no account, no telemetry, and nothing to sign into.
Delete the file and you have reset your progress; nothing else notices.
