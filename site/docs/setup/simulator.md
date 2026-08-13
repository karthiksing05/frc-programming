# Running and watching the simulator

You will spend more time in these two windows than in your editor. This page is the
reference for both. Read it once now, come back when something looks wrong.

Two programs, two jobs:

| | What it is | What it does |
|---|---|---|
| **Simulation GUI** | Your robot program, running | Drives the physics. Lets you press buttons and flip sensors. |
| **AdvantageScope** | A viewer | Draws what the robot publishes. Simulates nothing. |

You need both. The GUI makes the robot move; AdvantageScope is how you see why.

---

## 1. Start the robot

```bash
./tools/frcprog sim
```

First run takes 20–30 seconds. A window titled **Robot Simulation** opens.

Leave the terminal alone while it runs. `Ctrl-C` there stops the robot.

!!! tip "The VS Code button does the same thing"

    `Ctrl/Cmd-Shift-P` → **WPILib: Simulate Robot Code**. Same result. Use whichever
    you will remember.

---

## 2. Learn the window

The GUI opens with several floating panels. Drag them anywhere; the layout is saved.

**Robot State** is the one you cannot ignore. It lists Disabled, Autonomous,
Teleoperated, Test. Click a label to switch mode, exactly like a real Driver
Station.

**Your robot starts Disabled, and a disabled robot ignores everything.** If you
press keys and nothing happens, this is why, roughly nine times out of ten.

Other panels you will use:

| Panel | Shows | Where |
|---|---|---|
| **Joysticks** | What the robot code sees | open by default |
| **System Joysticks** | Keyboards and real controllers available to bind | open by default |
| **NetworkTables** | Every value the robot publishes | open by default |
| **PWM Outputs** | What each motor is being told | Hardware menu |
| **DIO** | Digital inputs. Click to flip a sensor. | Hardware menu |
| **Encoders** | Counts and rates | Hardware menu |
| **Analog Inputs** | The gyro, among others | Hardware menu |

If a panel is missing, it is under **Hardware** in the menu bar.

---

## 3. Connect a controller

The robot reads joysticks whether or not any exist, so an unbound joystick reads
zero forever and your drive command does nothing.

**Bind a keyboard:**

1. Find **Keyboard 0** in the **System Joysticks** panel
2. Drag it onto slot **0** in the **Joysticks** panel
3. Drag **Keyboard 1** onto slot **1** for the operator controls

This curriculum uses joystick 0 for the driver and 1 for the operator, matching
`Constants.OperatorInterface`.

**Check what the keys are mapped to.** The defaults vary by WPILib version, so do
not trust anyone's table, including this one:

> Menu bar → **DS** → **Keyboard 0 Settings**

That window lists every axis and button next to the key that drives it, and you can
change any of them. Set them once and the GUI remembers.

A layout that works well for this curriculum:

| Control | Suggested key | Used by |
|---|---|---|
| Joystick 0, Axis 1 (left Y) | `W` / `S` | drive forward and back |
| Joystick 0, Axis 4 (right X) | `A` / `D` | turn |
| Joystick 1, Button 1 (A) | `Z` | flywheels |
| Joystick 1, Button 2 (B) | `X` | intake |
| Joystick 1, Button 3 (X) | `C` | eject |
| Joystick 1, Button 4 (Y) | `V` | elevator to high |

!!! warning "Keyboard axes are on or off"

    A key gives you −1, 0 or +1. There is no half throttle. So a deadband bug will
    not show up here, and neither will smooth acceleration. Plug in a real Xbox
    controller when you want to feel those; it appears in System Joysticks and binds
    the same way.

**Watch the Joysticks panel while you press keys.** If the numbers there do not
move, the robot is not seeing your input and nothing downstream will work.

---

## 4. Enable it

Click **Teleoperated** in Robot State.

The robot is now live. Your `teleopPeriodic` runs, commands are scheduled, motors
respond.

Click **Autonomous** instead to run the routine from the auto chooser. Click
**Disabled** to stop everything.

---

## 5. Poke the hardware

This is what makes simulation better than a real robot for learning: you can create
any situation instantly.

**Flip a sensor.** Open **Hardware → DIO**. Channel 4 is the roller's beam-break.
Click it to toggle. The value shown is the raw line, so `false` means the beam is
broken and a game piece is present.

That single click is how you test "what happens when a piece arrives" without a
piece, a field, or a robot.

**Watch a motor.** Open **Hardware → PWM Outputs**. Each row is one motor:

| Channel | Motor |
|---|---|
| 0, 1 | drive left, right |
| 5 | roller |
| 6 | elevator |
| 7 | shoulder |
| 8 | flywheels |

The number is what your code commanded, between −1 and 1. If it is stuck at 0 while
you hold a button, your problem is upstream of the motor.

**Read an encoder.** **Hardware → Encoders** shows counts and rate for each one.

---

## 6. Start AdvantageScope

In a second terminal, with the simulator still running:

```bash
./tools/frcprog scope
```

Then in AdvantageScope: **File → Connect to Simulator**.

That connects to NetworkTables on `localhost`. The sidebar fills with a tree of
everything the robot is publishing. If it stays empty, the robot is not running or
the connection failed. See troubleshooting below.

---

## 7. Make your first plot

1. Find `NT` in the left sidebar and expand it
2. Expand a subsystem, say `Flywheels`
3. **Drag** `TargetRPM` onto the graph area
4. Drag `ActualRPM` onto the same graph

Two traces, same axes. That is the whole trick: a setpoint and its measurement on
one chart, so the gap between them is visible.

**Drag onto the left axis for one unit and the right axis for another.** Volts and
metres on the same left axis makes both unreadable.

### Tabs worth knowing

| Tab | Use it for |
|---|---|
| **Line Graph** | Anything numeric. Your default. |
| **Table** | Exact values at one instant, when the plot is ambiguous |
| **2D Field** | Drop a `Pose2d` on it and watch the robot drive (lesson 13) |
| **Mechanism** | Articulated arms and elevators (lesson 17) |
| **Swerve** | Four module direction arrows (lesson 21) |
| **Console** | Anything printed, plus warnings |

Add a tab with the **+** at the top.

### Save the layout

**File → Save Layout**, once you have a set of plots you like. Loading it back is
one action instead of ten drags.

Between matches you get about four minutes. A saved layout is the difference
between diagnosing something and running out of time.

---

## 8. Read the plot

Almost every mechanism plot is a **step response**: you asked for a new value, and
the measurement chases it. Four things to look at.

| What you see | Name | Means |
|---|---|---|
| Time until it first gets near | rise time | how quick |
| Time until it stops moving | settling time | how long to trust it |
| How far past it goes | overshoot | too aggressive |
| Gap left when it settles | steady-state error | never quite arrives |

Shapes you will actually meet:

**Rises and stops cleanly.** Done. Move on.

**Rises past, comes back, repeats.** Too much `kP`, or not enough `kD`. The
controller is over-reacting and the mechanism carries past.

**Creeps up and stalls short.** Not enough `kP`. Or friction is eating the last bit
and you need a little `kI`.

**Fuzzy, jittering constantly.** Too much `kD`. Sensor noise looks like fast change,
and `kD` reacts to fast change.

**Flat at zero while the setpoint is not zero.** Nothing is reaching the motor.
Check PWM Outputs. This is a wiring problem, not a tuning problem.

That last distinction saves the most time. Before touching a gain, confirm voltage
is arriving.

---

## 9. Troubleshooting

**Nothing happens when I press keys.**
Robot State says Disabled. Click Teleoperated.

**Still nothing.**
Is Keyboard 0 dragged onto Joysticks slot 0? Do the numbers in the Joysticks panel
move when you press keys? If not, the binding is missing or the key mapping differs
from what you assumed. Check DS → Keyboard 0 Settings.

**AdvantageScope sidebar is empty.**
The robot has to be running first. Start `frcprog sim`, wait for the window, then
connect. If it still fails on Windows, the first-run firewall prompt was probably
dismissed: allow Java through the firewall, or NetworkTables cannot bind.

**A signal I just added is not in the sidebar.**
Publishers are created once at construction. Restart the simulator after adding one.

**The robot moves but the plot is flat.**
You are watching a signal nothing sets. Check the exact name in the NetworkTables
panel of the GUI, which shows the live tree.

**Everything is very slow.**
Close the 3D Field tab if you have one open. It is the expensive one.

**Simulation runs but numbers are nonsense.**
Check units. A gain tuned for centimetres behaves wildly when the sensor reports
metres, and nothing in the tooling will warn you.

---

## The habit worth building

When you add a control loop, publish its setpoint and its measurement in the same
commit. Not later, when it breaks.

Five minutes then is the difference between debugging with data and debugging by
argument.
