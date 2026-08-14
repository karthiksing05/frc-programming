# The robot, part by part

Robot code is mostly instructions to hardware, so the names of the hardware turn up
everywhere — in class names, in port numbers, in error messages. This page introduces
the parts and the vocabulary. You do not need to memorise it; you need to have seen
it once so that `TalonFX` and `DIO 4` are not strangers when they appear.

---

## The chain from battery to motion

Almost everything on a robot is a link in one chain:

<figure class="r3d-fig">
<svg viewBox="0 0 760 260" xmlns="http://www.w3.org/2000/svg" role="img"
     aria-label="Battery feeds the power distribution hub, which powers motor controllers, which drive motors. The roboRIO sends commands over CAN, and sensors report back."
     style="max-width:100%;height:auto">
  <style>
    .bx   { fill: var(--md-code-bg-color); stroke: var(--md-default-fg-color--lighter); stroke-width: 1.5; }
    .bxa  { fill: var(--md-primary-fg-color); opacity: 0.14; stroke: var(--md-primary-fg-color); stroke-width: 1.5; }
    .t    { fill: var(--md-default-fg-color); font: 600 13px/1 var(--md-text-font-family, system-ui); }
    .s    { fill: var(--md-default-fg-color--light); font: 400 11px/1 var(--md-text-font-family, system-ui); }
    .ar   { stroke: var(--md-default-fg-color--light); stroke-width: 1.6; fill: none; }
    .ard  { stroke: var(--md-default-fg-color--light); stroke-width: 1.6; fill: none; stroke-dasharray: 4 3; }
    .el   { fill: var(--md-default-fg-color--light); font: 400 10px/1 var(--md-code-font-family, monospace); }
  </style>
  <defs>
    <marker id="a" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="6" markerHeight="6" orient="auto">
      <path d="M0,0 L10,5 L0,10 z" fill="var(--md-default-fg-color--light)"/>
    </marker>
  </defs>

  <rect class="bx" x="8"   y="96"  width="104" height="52" rx="6"/>
  <text class="t"  x="60"  y="118" text-anchor="middle">Battery</text>
  <text class="s"  x="60"  y="136" text-anchor="middle">12 volts</text>

  <rect class="bx" x="152" y="96"  width="104" height="52" rx="6"/>
  <text class="t"  x="204" y="118" text-anchor="middle">PDH</text>
  <text class="s"  x="204" y="136" text-anchor="middle">fuses + power</text>

  <rect class="bx" x="296" y="96"  width="130" height="52" rx="6"/>
  <text class="t"  x="361" y="118" text-anchor="middle">Motor controller</text>
  <text class="s"  x="361" y="136" text-anchor="middle">SPARK MAX, Talon FX</text>

  <rect class="bx" x="466" y="96"  width="104" height="52" rx="6"/>
  <text class="t"  x="518" y="118" text-anchor="middle">Motor</text>
  <text class="s"  x="518" y="136" text-anchor="middle">NEO, Kraken</text>

  <rect class="bx" x="610" y="96"  width="140" height="52" rx="6"/>
  <text class="t"  x="680" y="118" text-anchor="middle">A mechanism</text>
  <text class="s"  x="680" y="136" text-anchor="middle">wheels, arm, intake</text>

  <rect class="bxa" x="296" y="8"  width="130" height="52" rx="6"/>
  <text class="t"   x="361" y="30" text-anchor="middle">roboRIO</text>
  <text class="s"   x="361" y="48" text-anchor="middle">runs your code</text>

  <rect class="bx" x="466" y="196" width="284" height="52" rx="6"/>
  <text class="t"  x="608" y="218" text-anchor="middle">Sensors</text>
  <text class="s"  x="608" y="236" text-anchor="middle">encoders, gyro, beam break, limit switches</text>

  <path class="ar" d="M112,122 H150" marker-end="url(#a)"/>
  <path class="ar" d="M256,122 H294" marker-end="url(#a)"/>
  <path class="ar" d="M426,122 H464" marker-end="url(#a)"/>
  <path class="ar" d="M570,122 H608" marker-end="url(#a)"/>
  <path class="ar" d="M361,60 V94" marker-end="url(#a)"/>
  <text class="el" x="370" y="82">CAN</text>
  <path class="ard" d="M608,196 V170 H361 V62" marker-end="url(#a)"/>
  <text class="el" x="430" y="164">what actually happened</text>
</svg>
<figcaption>Solid lines carry commands and power outward. The dashed line is the robot telling your code what it actually did — that feedback loop is most of Stage 1B.</figcaption>
</figure>

Read it left to right. A **battery** feeds the **power distribution hub**, which is a
board full of fuses that splits 12 volts out to everything. Each **motor controller**
takes that power and, on command, sends some fraction of it to one **motor**, which
spins a gearbox and moves a mechanism.

Your code never touches a motor directly. It talks to the motor controller and says,
in effect, *"give the motor 40% power forward"*. The controller does the electrical
work.

---

## The parts, with their real names

### roboRIO

The robot's computer. It is a small metal box bolted to the frame, and it is the thing
your Java program actually runs on. Every season your code gets compiled on your laptop
and **deployed** — copied and started — on the roboRIO.

It runs your code in a loop, fifty times a second, for the entire match. That loop is
the single most important idea in robot programming, and Lesson 0D takes it apart
properly.

### Motors

You will hear these by brand name constantly.

A **NEO** and a **Kraken X60** are the two motors most teams reach for now. Both are
*brushless*, which means they are efficient, powerful, and — the part that matters to
you — they have a sensor built in that reports how far they have turned. A **CIM** is
the older *brushed* motor you will still see on veteran robots; it has no built-in
sensor, so if you need to know its position you bolt one on separately.

Motors are almost never connected straight to a wheel. They spin far too fast and with
far too little force, so they drive a **gearbox** that trades speed for torque. The
**gear ratio** is how many times the motor spins for one turn of the output. A ratio of
8.45:1 means the motor turns 8.45 times per wheel turn. That number lives in your code,
and if mechanical changes the gearbox without telling you, every distance your robot
thinks it drove is wrong by exactly that factor.

### Motor controllers

The box between the roboRIO and the motor. A **SPARK MAX** or **SPARK Flex** (made by
REV) drives a NEO; a **Talon FX** is built into the Kraken and Falcon motors. Older
robots use a **Victor SPX** or **Talon SRX**.

Two ways they receive commands:

**CAN** is a shared wire that daisy-chains through every controller on the robot. Each
device has an ID number, and the roboRIO addresses them by ID. Almost all modern
robots use CAN, because a CAN controller can also send data *back* — motor current,
temperature, encoder position.

**PWM** is the simple alternative: one signal wire per controller into a numbered port
on the roboRIO. It only goes one direction, so a PWM controller can never tell you
anything. This curriculum uses PWM in simulation because it keeps the first lessons
about one idea instead of two.

### Encoders

An encoder measures rotation, and it is how a robot knows anything about where its own
parts are. Without one, "raise the elevator to 90 centimetres" is not a thing you can
ask for — you can only say "run the motor and hope".

Brushless motors have one built in. You can also mount a separate **Through Bore
Encoder** or **CANCoder** on the mechanism itself, which is more accurate because it
measures the thing you care about rather than the motor several gears upstream.

Encoders count in **ticks** — arbitrary units — so your code multiplies by a conversion
factor to get metres or degrees. Getting that conversion wrong is one of the most
common bugs in all of FRC, and it is silent: everything runs, the numbers are just
wrong.

### Gyro

Also called an **IMU**. A **navX** or **Pigeon 2** reports which way the robot is
facing. Encoders tell you how far you drove; the gyro tells you what direction you
drove in. You need both to know where you are on the field.

### Digital sensors

A **limit switch** is a button that a moving part presses at the end of its travel — a
cheap, reliable way to know the arm is all the way down. A **beam break** shines an
infrared beam across a gap and tells you when something interrupts it, which is how a
robot knows it is holding a game piece.

Both plug into **DIO** ports — Digital Input/Output — numbered 0 upward on the roboRIO.
When you see `DIO 4` in this curriculum, that is which physical port a sensor is in.

### Driver Station

The laptop at the field, running FIRST's Driver Station program. It connects to the
robot over Wi-Fi, sends controller input, and — importantly — is the only thing that
can **enable** the robot. A disabled robot ignores every command your code sends, which
is a safety feature and also the explanation for roughly half of all "my code isn't
working" reports.

---

## The vocabulary, in one place

| Word | What it means |
|---|---|
| Deploy | Copy your compiled code onto the roboRIO and start it |
| Subsystem | One mechanism, and the code that owns it — drivetrain, intake, elevator |
| Setpoint | The position or speed you are asking a mechanism to reach |
| Teleop | The part of the match where drivers control the robot |
| Auto | The first fifteen seconds, robot code only |
| Game piece | The object the game has you pick up and score |
| Tick | One count from an encoder |
| Ratio | Motor turns per output turn, set by the gearbox |
| Brownout | The battery sags under load and the roboRIO cuts power to protect itself |

---

Next: the software. What WPILib, Gradle, and the rest of the toolchain actually are.

[The software, explained :material-arrow-right:](software.md){ .md-button .md-button--primary }
