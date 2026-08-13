# Lesson 0D — Project tour

**Stage 0 · 30 min · Needs: 0A, 0B, 0C**

Learn where things live and how to not lose your work.

## Do this

1. **Look at the folder layout** below.
2. **Try the five commands** below.
3. **Set up a way to save your work** (Git, or copies).

## The layout

```
curriculum/
├── src/main/java/frc/robot/      everything you write
│   ├── Robot.java                    disabled / auto / teleop
│   ├── RobotContainer.java           subsystems meet controllers
│   ├── Constants.java                every number, named
│   ├── subsystems/                   one folder per mechanism
│   ├── autos/                        autonomous routines
│   └── util/                         small helpers
├── src/test/java/frc/robot/      the rubrics that grade you
├── lessons/                      the lesson text
├── tools/                        the frcprog command
└── .meta/                        answers and pristine starters
```

Two things matter here.

**One `src` tree, and it grows.** The method you write in lesson 01 is still there
in lesson 15, used by a drivetrain you have not built yet.

**The tests are not hidden.** `src/test/java` is the exact code that grades you.
Reading it before you start is reading the spec, not cheating.

## The five commands

```bash
./tools/frcprog next             # what do I do now?
./tools/frcprog read 01-methods  # the lesson
./tools/frcprog check 01-methods # grade me
./tools/frcprog hints 01-methods # I am stuck
./tools/frcprog list             # where am I?
```

Run each one now.

## Saving your work

If your team uses Git, three commands:

```bash
git init                    # once, ever
git add -A                  # include everything
git commit -m "Lesson 01"   # save a checkpoint
```

Do it now, before you change anything. Then after each lesson passes:

```bash
git add -A && git commit -m "Lesson 02 passing"
```

That is the whole ritual for now. Branching and merging solve problems you do not
have yet. The handbook's **Git** page has the rest when you want it, including how
to get back when something breaks.

**Not using Git?** Copy the whole folder somewhere safe after each lesson, with the
number in the name. Clumsy, works, much better than nothing.

## Done

You can find `src/main/java/frc/robot/`, you ran `frcprog next`, and you have some
way back to a working version.

```bash
./tools/frcprog next
```
