# Playgrounds

Three small browser sandboxes. They are **not** part of the curriculum and nothing
depends on them — they exist because some ideas land faster when you can drag a
slider than when you can read a paragraph.

They run entirely in your browser, off this same local server. No network.

!!! tip "Serve them, do not open them"

    These pages share a browser-local filesystem, and browsers partition that per
    file when you open HTML directly from disk. Reach them through this site (or any
    HTTP server) and they work; double-click the `.html` and they will behave oddly.

---

## Deadband — the maths from Lesson 01

A noisy joystick trace and a threshold you can drag. Watch the output snap flat
inside the band and track exactly outside it.

Pairs with [Lesson 01 · Methods](../learn/stage1a/01-methods/).

[Open the deadband playground](/examples/functions-poc/index.html){ .md-button target=_blank }

---

## Elevator PID — the tuning from Lesson 05

Three sliders — `kP`, `kI`, `kD` — and a live step response. This is the fastest way
to build intuition for what each gain does before you go and tune the real one in
Java.

Try `kP = 10, kI = 0, kD = 0` first and feel the oscillation. Then add `kD` until it
stops. Then notice it settles slightly short, and add a sliver of `kI`.

Pairs with [Lesson 05 · PID introduction](../learn/stage1b/05-pid-elevator/).

[Open the PID playground](/examples/elevator-pid-poc/index.html){ .md-button target=_blank }

---

## Tank drive — the mixing from Lesson 07

Two axes in, two wheel speeds out. Useful for seeing why `left = fwd + rot` and
`right = fwd - rot` produce a robot that turns the way you expect, and what happens
when the sum saturates.

Pairs with [Lesson 07 · Tank drive wiring](../learn/stage1c/07-tank-drive/).

[Open the drive playground](/examples/tank-drive-poc/index.html){ .md-button target=_blank }

---

## Why these are optional

The curriculum deliberately teaches in the real toolchain: real Java, real WPILib,
real simulation. A browser widget can show you what a gain does; it cannot teach you
Gradle, or the requirement system, or how to read a WPILOG.

Use these to build intuition quickly, then go and do the lesson properly.
