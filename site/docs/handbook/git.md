# Git

Lesson 0D gives you three commands and tells you not to worry about the rest. That
is the right call on day one — Git anxiety stops more people from writing code than
Git problems ever do.

This page is the rest, for when you want it. Read the first two sections and stop;
come back for the others when you hit the problem they solve.

!!! tip "You do not need a GitHub account for any of this"

    Git runs entirely on your machine. A remote — GitHub, GitLab, a hard drive — is
    optional and only matters once more than one person is involved. Everything down
    to [Working with other people](#working-with-other-people) works offline.

---

## What Git is actually for

Two things, and the second is the one that matters at 11pm.

**Getting back.** You had it working. You changed six files trying to make the arm
faster. Now nothing works and you cannot remember what you touched. With commits,
that is a thirty-second problem. Without them, it is your evening.

**Working in parallel.** Three people editing one robot project without overwriting
each other's work.

Almost everything confusing about Git comes from the second job. If you are working
alone, you can ignore most of it for a long time.

### The mental model: snapshots, not diffs

A commit is a **complete photograph of every tracked file**, plus a message and a
pointer to the commit before it. Git shows you differences because differences are
what you want to read, but it stores snapshots.

That single fact explains most of Git's behaviour. "Go back to yesterday" is cheap
because yesterday is still there, whole. Nothing is reconstructed; it is just
another photograph you can walk to.

---

## The loop

Ninety percent of your Git usage, for years.

```bash
git status                       # what have I changed?
git add -A                       # include all of it in the next snapshot
git commit -m "Lesson 05 passing" # take the snapshot
```

`git status` before every commit. It takes two seconds and it is how you notice you
are about to commit a 40 MB log file or forget the one file you actually meant to
save.

### When to commit

**Whenever a rubric goes green.** That is the natural checkpoint this curriculum
gives you: a moment when the project provably works.

```bash
./tools/frcprog check 05-pid-elevator   # green
git add -A && git commit -m "Lesson 05: elevator PID tuned"
```

Also commit before you start something risky. A commit you did not need costs
nothing; a commit you needed and did not make costs an evening.

### Writing a message

Say what changed and, if it is not obvious, why.

```
Lesson 06: arm gravity feedforward, kG = 0.12

Measured rather than calculated — the gearbox friction makes the
computed value about 30% low.
```

Six months later you will not remember why `kG` is 0.12. That second paragraph is
the whole point of commit messages, and almost nobody writes it.

`git commit` with no `-m` opens an editor for exactly this. If it drops you into
Vim and you are stuck: `Esc`, then `:wq`, then Enter.

---

## Seeing what you did

```bash
git log --oneline               # your commits, newest first
git log --oneline --stat        # ...with which files each touched
git diff                        # changes you have NOT staged yet
git diff --staged               # changes that WILL go in the next commit
git show HEAD                   # everything in the most recent commit
git show HEAD:src/main/java/frc/robot/Constants.java   # a file AS OF that commit
```

That last one is quietly excellent. "What did `Constants.java` look like when lesson
05 was passing?" is one command, and it does not disturb your working files at all.

```bash
git log --oneline -- src/main/java/frc/robot/Constants.java
```

shows only the commits that touched one file — the fastest way to find when a
constant changed.

---

## Undoing things

The section worth actually learning. These are four different operations that
beginners reach for interchangeably, and picking the wrong one is how people lose
work.

### Throw away uncommitted changes to a file

```bash
git restore src/main/java/frc/robot/Constants.java
```

Back to the last commit. **The changes are gone permanently** — they were never
committed, so Git has no copy.

!!! warning "This is the one command here that can actually lose work"

    Everything else in this section is recoverable. `git restore` on uncommitted
    changes is not, because there is nothing to recover from. If you are unsure,
    commit first — you can always delete the commit later.

    This curriculum also gives you `./tools/frcprog reset <lesson>`, which restores
    just that lesson's files from the pristine starters and asks before it does.

### Unstage something you `git add`ed by mistake

```bash
git restore --staged bigfile.log
```

The file keeps your changes; it just will not be in the next commit.

### Undo a commit but keep the work

```bash
git reset --soft HEAD~1
```

The commit disappears; every change it contained is sitting in your working tree,
staged. Use this when you committed too early or wrote a bad message.

### Undo a commit that other people already have

```bash
git revert <commit>
```

Makes a **new** commit that reverses the old one. Slightly awkward-looking history,
and the only safe option once a commit has been shared — because it adds rather than
rewrites.

The rule: **rewrite history that only you have; revert history other people have.**

---

## Branches

A branch is a sticky note on a commit. That is genuinely all it is, which is why
creating one is instant.

```bash
git switch -c vision-experiment   # new branch, and switch to it
git switch main                   # back
git branch                        # which do I have? * marks the current one
```

Use one when you want to try something that might not work, without disturbing the
version that does.

```bash
git switch -c try-higher-kp
# ...experiment, commit freely...
git switch main                   # your working version, untouched
git branch -D try-higher-kp       # it did not work out; bin it
```

### Merging

```bash
git switch main
git merge vision-experiment
```

If the two branches changed different things, this just works. If they changed the
same lines, you get a conflict:

```
<<<<<<< HEAD
    public static final double kP = 40.0;
=======
    public static final double kP = 55.0;
>>>>>>> vision-experiment
```

Git is not broken and it is not asking you to do anything clever. It is saying *two
people changed this line and I will not guess.* Edit the file so it says what you
want, delete all three marker lines, then:

```bash
git add -A && git commit
```

Nothing bad happens if you take your time. `git merge --abort` puts everything back
exactly as it was.

---

## Working with other people

This is the only part that needs a network.

```bash
git clone https://github.com/yourteam/robot-2026.git   # once
git pull                                                # get their work
git push                                                # share yours
```

The realistic team loop:

```bash
git pull                    # start from what everyone else has
# ...work, commit...
git pull                    # catch anything landed while you worked
git push
```

**Pull before you push.** Most "Git is fighting me" moments are a push rejected
because someone else pushed first, and a pull fixes it.

### What not to commit

Generated files. They are large, they change constantly, and two people will always
conflict on them.

This project's `.gitignore` already handles it:

```
.gradle/          # Gradle's internal state
build/            # compiled output — regenerated every build
.frcprog/         # your personal lesson progress
*.wpilog          # simulation logs, often huge
```

Rule of thumb: **if a build can regenerate it, do not commit it.** Commit source,
commit configuration, commit the `gradlew` wrapper. Never commit `build/`.

---

## "I think I broke it"

You almost certainly did not. Git's default posture is to keep things.

### Step 1: what actually happened

```bash
git reflog
```

**The most valuable command on this page.** Every commit, checkout, merge, reset and
rename you have made, in order, even ones no branch points at any more:

```
93110e3 HEAD@{0}: Branch: renamed refs/heads/repo-tutorial to refs/heads/main
93110e3 HEAD@{2}: commit: Add a single-file artifact build of the whole site
3be8602 HEAD@{4}: checkout: moving from main to repo-tutorial
```

That is a real reflog from this repository, and it answers "what did I just do to
myself" precisely.

### Step 2: go back to any of it

```bash
git switch -c rescue 93110e3
```

A branch at that exact state. Your work was never gone; nothing was pointing at it.

### Step 3: check nothing was actually lost

If you merged or renamed and want to be sure an old branch's work survived:

```bash
git merge-base --is-ancestor <old-commit> HEAD && echo "safe" || echo "NOT in this history"
```

"safe" means that commit is somewhere in your current history and nothing it
contained was dropped.

### Common panics

**"I committed to the wrong branch."**
```bash
git switch correct-branch
git cherry-pick <commit>      # copy it here
git switch wrong-branch
git reset --hard HEAD~1       # and remove it there
```

**"I deleted a branch I needed."** It is in the reflog. `git switch -c name <sha>`.

**"`git status` shows hundreds of files I did not touch."** Something generated
output that is not ignored — usually `build/`. Check `.gitignore`.

**"It says `detached HEAD`."** You are looking at a commit rather than standing on a
branch. Harmless. `git switch main` returns; `git switch -c name` keeps whatever you
did there.

---

## What this page skips

Deliberately, because you do not need them yet and they cost attention you need for
the robot: rebasing, stashing, submodules, tags, `bisect`, hooks, and the
staging-area-as-a-concept.

They are all real and all useful eventually. When you hit the problem one of them
solves, you will know, and that is the right moment to learn it.

If you want the full picture: the [Pro Git book](https://git-scm.com/book/en/v2) is
free, well written, and the first three chapters cover everything above properly.

---

## Cheat sheet

```bash
# the loop
git status                      # what changed?
git add -A                      # stage everything
git commit -m "message"         # snapshot it

# looking
git log --oneline               # history
git diff                        # unstaged changes
git show HEAD:path/to/File.java # a file as of a commit

# undoing
git restore <file>              # discard uncommitted changes (NOT recoverable)
git restore --staged <file>     # unstage, keep the changes
git reset --soft HEAD~1         # undo last commit, keep the work
git revert <commit>             # safely undo something already shared

# branches
git switch -c <name>            # create and switch
git switch main                 # switch back
git merge <name>                # bring it in
git merge --abort               # changed my mind

# other people
git pull                        # get theirs
git push                        # share yours

# help
git reflog                      # everything you have done — start here when lost
```
