/* ═══════════════════════════════════════════════════════════
   simulation.js — Joystick + motor model for the Functions
   (deadband) PoC.

   Model:
   - The "raw" joystick has a true X/Y position the user controls
     with the mouse or WASD, plus a small random noise vector
     applied each tick to mimic sensor jitter.
   - The student's applyDeadband(value, threshold) is applied
     independently to the X and Y channels.
   - The "motor output" is just whatever applyDeadband returned;
     when it's non-zero the motor visually buzzes.
   ═══════════════════════════════════════════════════════════ */

class JoystickSim {
  constructor() {
    this.trueX = 0;
    this.trueY = 0;
    this.noisyX = 0;
    this.noisyY = 0;
    this.cleanX = 0;
    this.cleanY = 0;

    this.noiseEnabled = true;
    this.NOISE_AMPLITUDE = 0.05;
    this.THRESHOLD = 0.10;     // the threshold passed to applyDeadband

    // student function: (value, threshold) -> double
    // default identity (no deadband applied) until they Run their code
    this.deadbandFn = (v, _t) => v;

    this.DT = 0.02;
    this.history = [];
    this.maxHistory = 250;
    this.tick = 0;
  }

  setTrue(x, y) {
    this.trueX = Math.max(-1, Math.min(1, x));
    this.trueY = Math.max(-1, Math.min(1, y));
  }

  setDeadbandFn(fn) {
    this.deadbandFn = fn;
  }

  step() {
    // Add noise
    const nx = this.noiseEnabled
      ? (Math.random() - 0.5) * 2 * this.NOISE_AMPLITUDE : 0;
    const ny = this.noiseEnabled
      ? (Math.random() - 0.5) * 2 * this.NOISE_AMPLITUDE : 0;

    this.noisyX = Math.max(-1, Math.min(1, this.trueX + nx));
    this.noisyY = Math.max(-1, Math.min(1, this.trueY + ny));

    // Apply the student's deadband function
    let cx = 0, cy = 0;
    try {
      cx = Number(this.deadbandFn(this.noisyX, this.THRESHOLD)) || 0;
      cy = Number(this.deadbandFn(this.noisyY, this.THRESHOLD)) || 0;
    } catch (e) {
      cx = this.noisyX;
      cy = this.noisyY;
    }
    this.cleanX = Math.max(-1, Math.min(1, cx));
    this.cleanY = Math.max(-1, Math.min(1, cy));

    this.tick++;
    this.history.push({
      t:     this.tick * this.DT,
      rawX:  this.noisyX,
      cleanX: this.cleanX
    });
    if (this.history.length > this.maxHistory) this.history.shift();
  }
}

/* ═══════════════════════════════════════════════════════════
   Graph renderer — raw vs. clean joystick X over time.
   ═══════════════════════════════════════════════════════════ */

class JoystickGraph {
  constructor(canvas) {
    this.canvas = canvas;
    this.ctx = canvas.getContext('2d');
    this.dpr = window.devicePixelRatio || 1;
    this._resize();
    window.addEventListener('resize', () => this._resize());
  }

  _resize() {
    const r = this.canvas.getBoundingClientRect();
    this.canvas.width  = r.width  * this.dpr;
    this.canvas.height = r.height * this.dpr;
    this.ctx.setTransform(this.dpr, 0, 0, this.dpr, 0, 0);
    this.w = r.width;
    this.h = r.height;
  }

  draw(history, threshold) {
    const ctx = this.ctx;
    const w = this.w, h = this.h;
    ctx.clearRect(0, 0, w, h);
    if (w < 30 || h < 40) return;

    // Background grid
    ctx.fillStyle = '#1a1a1a';
    ctx.fillRect(0, 0, w, h);

    const midY = h / 2;
    // zero line
    ctx.strokeStyle = '#3a3a3a';
    ctx.lineWidth = 1;
    ctx.beginPath();
    ctx.moveTo(0, midY); ctx.lineTo(w, midY); ctx.stroke();

    // deadband band
    const bandH = (h / 2) * threshold;
    ctx.fillStyle = 'rgba(255,145,0,0.08)';
    ctx.fillRect(0, midY - bandH, w, bandH * 2);
    ctx.strokeStyle = 'rgba(255,145,0,0.4)';
    ctx.setLineDash([4, 4]);
    ctx.beginPath();
    ctx.moveTo(0, midY - bandH); ctx.lineTo(w, midY - bandH);
    ctx.moveTo(0, midY + bandH); ctx.lineTo(w, midY + bandH);
    ctx.stroke();
    ctx.setLineDash([]);

    if (!history.length) return;

    const N = history.length;
    const stepX = w / Math.max(N - 1, 1);

    const valY = v => midY - v * (h / 2 - 4);

    // raw trace (red)
    ctx.strokeStyle = '#ff5252';
    ctx.lineWidth = 1.5;
    ctx.beginPath();
    history.forEach((p, i) => {
      const x = i * stepX;
      const y = valY(p.rawX);
      if (i === 0) ctx.moveTo(x, y); else ctx.lineTo(x, y);
    });
    ctx.stroke();

    // clean trace (green)
    ctx.strokeStyle = '#4caf50';
    ctx.lineWidth = 2;
    ctx.beginPath();
    history.forEach((p, i) => {
      const x = i * stepX;
      const y = valY(p.cleanX);
      if (i === 0) ctx.moveTo(x, y); else ctx.lineTo(x, y);
    });
    ctx.stroke();

    // labels
    ctx.fillStyle = '#a0a0a0';
    ctx.font = '11px Roboto Mono, monospace';
    ctx.fillText('raw joystick X (red) vs after applyDeadband (green)', 8, 14);
    ctx.fillText('±threshold band shown in orange', 8, h - 6);
  }
}
