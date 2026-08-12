/* ═══════════════════════════════════════════════════════════
   simulation.js — top-down tank-drive physics.

   The student's periodic() is called every tick. It writes to
   mock motor objects via `m_leftMotor.set(...)`. We then turn
   left/right motor demand into a body velocity and integrate.
   ═══════════════════════════════════════════════════════════ */

class TankDriveSim {
  constructor() {
    // Field is 8m × 8m for the visual; physical units in meters/sec.
    this.FIELD_W = 8.0;
    this.FIELD_H = 8.0;
    this.TRACK = 0.55;             // m between left & right wheel centers
    this.MAX_SPEED = 3.0;          // m/s when motor.set(1.0)
    this.DT = 0.02;
    this.FRICTION = 0.05;          // velocity decays each tick if not commanded

    this.reset();
  }

  reset() {
    this.x = this.FIELD_W / 2;
    this.y = this.FIELD_H / 2;
    this.theta = 0;                 // radians, 0 = facing +X
    this.leftDemand = 0;
    this.rightDemand = 0;
    this.trail = [];
  }

  step() {
    // Differential drive kinematics
    const vL = this.leftDemand  * this.MAX_SPEED;
    const vR = this.rightDemand * this.MAX_SPEED;
    const v       = (vL + vR) / 2;
    const omega   = (vR - vL) / this.TRACK;

    this.theta += omega * this.DT;
    this.x     += v * Math.cos(this.theta) * this.DT;
    this.y     += v * Math.sin(this.theta) * this.DT;

    // Clamp to field; bounce velocity off walls a bit
    if (this.x < 0.25)              { this.x = 0.25; }
    if (this.x > this.FIELD_W-0.25) { this.x = this.FIELD_W-0.25; }
    if (this.y < 0.25)              { this.y = 0.25; }
    if (this.y > this.FIELD_H-0.25) { this.y = this.FIELD_H-0.25; }

    // Trail
    if (this.trail.length === 0 ||
        Math.hypot(this.x - this.trail[this.trail.length-1].x,
                   this.y - this.trail[this.trail.length-1].y) > 0.04) {
      this.trail.push({ x: this.x, y: this.y });
      if (this.trail.length > 400) this.trail.shift();
    }
  }
}

/* ═══════════════════════════════════════════════════════════
   Top-down renderer
   ═══════════════════════════════════════════════════════════ */

class RobotRenderer {
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

  draw(sim) {
    const ctx = this.ctx;
    const w = this.w, h = this.h;
    if (w < 50 || h < 50) return;

    // Background
    ctx.fillStyle = '#131313';
    ctx.fillRect(0, 0, w, h);

    // Field metric: world meters -> pixels
    const pad = 10;
    const scale = Math.min((w - 2*pad) / sim.FIELD_W,
                           (h - 2*pad) / sim.FIELD_H);
    const W = sim.FIELD_W * scale;
    const H = sim.FIELD_H * scale;
    const ox = (w - W) / 2;
    const oy = (h - H) / 2;

    // Grid
    ctx.strokeStyle = '#252525';
    ctx.lineWidth = 1;
    for (let i = 0; i <= sim.FIELD_W; i++) {
      const x = ox + i * scale;
      ctx.beginPath(); ctx.moveTo(x, oy); ctx.lineTo(x, oy + H); ctx.stroke();
    }
    for (let i = 0; i <= sim.FIELD_H; i++) {
      const y = oy + i * scale;
      ctx.beginPath(); ctx.moveTo(ox, y); ctx.lineTo(ox + W, y); ctx.stroke();
    }

    // Border
    ctx.strokeStyle = '#3a3a3a';
    ctx.lineWidth = 2;
    ctx.strokeRect(ox, oy, W, H);

    // World coords helper (Y flipped so +Y is up in our model)
    const toX = mx => ox + mx * scale;
    const toY = my => oy + H - my * scale;

    // Trail
    if (sim.trail.length > 1) {
      ctx.strokeStyle = 'rgba(76,175,80,0.4)';
      ctx.lineWidth = 1.5;
      ctx.beginPath();
      ctx.moveTo(toX(sim.trail[0].x), toY(sim.trail[0].y));
      for (let i = 1; i < sim.trail.length; i++) {
        ctx.lineTo(toX(sim.trail[i].x), toY(sim.trail[i].y));
      }
      ctx.stroke();
    }

    // Robot — rectangle, oriented by theta
    const cx = toX(sim.x);
    const cy = toY(sim.y);
    const robotLen   = 0.7 * scale;
    const robotWidth = 0.55 * scale;

    ctx.save();
    ctx.translate(cx, cy);
    ctx.rotate(-sim.theta); // canvas Y is down, our theta uses up

    // chassis
    ctx.fillStyle = '#3d6f3f';
    ctx.strokeStyle = '#4caf50';
    ctx.lineWidth = 2;
    ctx.beginPath();
    ctx.rect(-robotLen/2, -robotWidth/2, robotLen, robotWidth);
    ctx.fill(); ctx.stroke();

    // front indicator
    ctx.fillStyle = '#69f0ae';
    ctx.beginPath();
    ctx.moveTo(robotLen/2, 0);
    ctx.lineTo(robotLen/2 - 8, -8);
    ctx.lineTo(robotLen/2 - 8,  8);
    ctx.closePath();
    ctx.fill();

    // wheels (left=top in this orientation since +Y is "up"; left side
    // is -y in body frame which after Y-flip ends up on top of canvas)
    ctx.fillStyle = '#222';
    const wheelLen = robotLen * 0.6;
    const wheelW   = 6;
    ctx.fillRect(-wheelLen/2, -robotWidth/2 - wheelW, wheelLen, wheelW);
    ctx.fillRect(-wheelLen/2,  robotWidth/2,          wheelLen, wheelW);

    ctx.restore();

    // HUD
    ctx.fillStyle = '#a0a0a0';
    ctx.font = '11px Roboto Mono, monospace';
    ctx.fillText(`x=${sim.x.toFixed(2)}m  y=${sim.y.toFixed(2)}m  θ=${(sim.theta*180/Math.PI).toFixed(0)}°`,
                 ox + 6, oy + 14);
  }
}
