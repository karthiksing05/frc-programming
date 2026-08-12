# Lesson 0D — Project tour, and saving your work

> **Stage 0 · ~30 minutes · Prerequisites: 0A, 0B, 0C**

Two things before you write code: knowing what is in this folder, and being able to
get back to a version that worked.

## What you'll learn

1. What every top-level folder in this project is for.
2. The five `frcprog` commands you will use constantly.
3. Enough Git to never lose work — and nothing more than that.

## What you'll do

### 1. The tour

```
curriculum/
├── src/main/java/frc/robot/      ← everything you write lives here
│   ├── Main.java                     starts the program; never edit it
│   ├── Robot.java                    the lifecycle: disabled, auto, teleop
│   ├── RobotContainer.java           where subsystems meet controllers
│   ├── Constants.java                every number, with a name attached
│   ├── subsystems/                   one folder per mechanism
│   ├── autos/                        autonomous routines
│   └── util/                         small shared helpers
│
├── src/test/java/frc/robot/      ← the rubrics that grade each lesson
├── lessons/                      ← the lesson text you are reading now
├── tools/                        ← the frcprog command line
├── .meta/                        ← reference answers and pristine starters
└── build.gradle                  ← how it all gets compiled
```

Two things to notice.

**There is one `src` tree, and it grows.** You are not going to start a new project
every lesson. The deadband method you write in lesson 01 is still there in lesson
15, being used by a drivetrain you have not built yet. By the end this folder holds
a robot you could deploy.

**The tests are not hidden from you.** `src/test/java` contains the exact code that
grades each lesson. Reading a rubric before you start is not cheating — it is
reading the specification, which is what professionals do.

### 2. The commands

```bash
./tools/frcprog next            # what should I do now?
./tools/frcprog read 01-methods # the lesson text, in the terminal
./tools/frcprog check 01-methods# grade me
./tools/frcprog hints 01-methods# I'm stuck
./tools/frcprog list            # where am I overall?
```

Try each one now. `next` should point you at lesson 01.

### 3. Saving your work

If your team uses Git, this is where you learn three commands. If not, skip to the
alternative below — it is genuinely fine.

Git's job here is narrow: let you get back to a version that worked. That is all we
are using it for today.

```bash
git init                    # once, ever
git add -A                  # "include everything I've changed"
git commit -m "Lesson 01"   # "save that as a checkpoint, with a note"
```

Do that now — commit the project as it stands, before you have changed anything.
Then, after each lesson passes:

```bash
git add -A && git commit -m "Lesson 02 passing"
```

That is the entire ritual. `git log --oneline` shows your checkpoints.

Branching, merging, rebasing, pull requests: real, useful, and not today. They
solve problems you do not have yet, and learning them now costs you the attention
you need for the actual lesson.

When you do want them — or when something goes wrong and you need to get back —
the handbook's **Git** page covers the whole thing: reading history, undoing at
four different levels, branches, working with a teammate, and what to do when you
are convinced you have destroyed everything. (You almost certainly have not;
`git reflog` remembers.) Open it from the site, or read
`site/docs/handbook/git.md`.

**If you are not using Git:** copy the whole folder somewhere safe after each
lesson passes, with the lesson number in the name. It is clumsy and it works, and
it is much better than the common alternative of having no way back at all.

## Done?

You can find `src/main/java/frc/robot/`, you have run `frcprog next`, and you have
some way to get back to a working version.

```bash
./tools/frcprog next
```

## Why this lesson exists

Two reasons, both learned the hard way by every team.

Students who do not know where files live spend their first three lessons lost in
a folder tree instead of thinking about code.

And students without a way to save their work eventually break something at 11pm,
cannot get back, and lose an evening — or quit. Thirty minutes now buys that back
several times over.
