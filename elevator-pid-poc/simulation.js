/* ═══════════════════════════════════════════════════════════
   simulation.js  –  Elevator physics + PID engine
   ═══════════════════════════════════════════════════════════ */

class ElevatorSim {
  constructor() {
    // Physical constants
    this.GRAVITY = 9.81;          // m/s²
    this.MASS = 5.0;              // kg  (carriage + game piece)
    this.MAX_HEIGHT = 1.6;        // m
    this.MIN_HEIGHT = 0.0;        // m
    this.MOTOR_MAX_FORCE = 120;   // N   (NEO on ~10:1 reduction)
    this.FRICTION = 0.4;          // viscous friction coeff
    this.DT = 0.02;               // 20 ms loop (50 Hz, like a robot)

    // State
    this.position = 0.0;          // m
    this.velocity = 0.0;          // m/s
    this.setpoint = 1.0;          // m
    this.motorOutput = 0.0;       // -1 … 1

    // PID state (gets overwritten by user code parsing)
    this.kP = 0.0;
    this.kI = 0.0;
    this.kD = 0.0;
    this.kF = 0.0;

    this.integralSum = 0.0;
    this.prevError = 0.0;

    // History for graphing
    this.history = [];
    this.maxHistory = 300;        // ~6 s at 50 Hz
    this.tick = 0;

    // Running flag
    this.running = true;
  }

  /** Called once when user presses Run – re-parse gains */
  setGains(kP, kI, kD, kF) {
    this.kP = kP;
    this.kI = kI;
    this.kD = kD;
    this.kF = kF;
    // Reset PID accumulator on gain change
    this.integralSum = 0.0;
    this.prevError = 0.0;
  }

  setSetpoint(sp) {
    this.setpoint = Math.max(this.MIN_HEIGHT, Math.min(this.MAX_HEIGHT, sp));
  }

  /** One simulation step */
  step() {
    if (!this.running) return;

    const error = this.setpoint - this.position;

    // PID
    this.integralSum += error * this.DT;
    // Anti-windup clamp
    this.integralSum = Math.max(-5, Math.min(5, this.integralSum));
    const derivative = (error - this.prevError) / this.DT;
    this.prevError = error;

    let output = this.kP * error
               + this.kI * this.integralSum
               + this.kD * derivative
               + this.kF;  // gravity feedforward (constant upward bias)

    // Clamp motor output
    output = Math.max(-1, Math.min(1, output));
    this.motorOutput = output;

    // Physics: F = motor - gravity - friction
    const motorForce = output * this.MOTOR_MAX_FORCE;
    const gravityForce = -this.MASS * this.GRAVITY;
    const frictionForce = -this.FRICTION * this.velocity;
    const netForce = motorForce + gravityForce + frictionForce;

    const accel = netForce / this.MASS;
    this.velocity += accel * this.DT;
    this.position += this.velocity * this.DT;

    // Hard limits
    if (this.position <= this.MIN_HEIGHT) {
      this.position = this.MIN_HEIGHT;
      this.velocity = Math.max(0, this.velocity);
    }
    if (this.position >= this.MAX_HEIGHT) {
      this.position = this.MAX_HEIGHT;
      this.velocity = Math.min(0, this.velocity);
    }

    // Record history
    this.tick++;
    this.history.push({
      t: this.tick * this.DT,
      pos: this.position,
      sp: this.setpoint,
      out: this.motorOutput
    });
    if (this.history.length > this.maxHistory) {
      this.history.shift();
    }
  }

  reset() {
    this.position = 0.0;
    this.velocity = 0.0;
    this.integralSum = 0.0;
    this.prevError = 0.0;
    this.motorOutput = 0.0;
    this.history = [];
    this.tick = 0;
  }
}

/* ═══════════════════════════════════════════════════════════
   Elevator Canvas Renderer  –  Detailed "CAD-rendered" look
   ═══════════════════════════════════════════════════════════ */

class ElevatorRenderer {
  constructor(canvas) {
    this.canvas = canvas;
    this.ctx = canvas.getContext('2d');
    this.dpr = window.devicePixelRatio || 1;
    this._resize();
    window.addEventListener('resize', () => this._resize());
  }

  _resize() {
    const rect = this.canvas.getBoundingClientRect();
    this.canvas.width = rect.width * this.dpr;
    this.canvas.height = rect.height * this.dpr;
    this.ctx.setTransform(this.dpr, 0, 0, this.dpr, 0, 0);
    this.w = rect.width;
    this.h = rect.height;
  }

  draw(sim) {
    const ctx = this.ctx;
    const w = this.w;
    const h = this.h;
    ctx.clearRect(0, 0, w, h);
    if (w < 30 || h < 60) return;

    const dark = document.documentElement.getAttribute('data-theme') !== 'light';

    // ── Geometry ──────────────────────────────────────────
    const pad = { top: 22, bot: 48, left: 44, right: 18 };
    const rTop = pad.top;
    const rBot = h - pad.bot;
    const rH = rBot - rTop;
    const cx = w * 0.44;
    const gap = Math.min(58, w * 0.26);
    const rW = 11;
    const LR = cx - gap / 2;
    const RR = cx + gap / 2;

    const posY = rBot - (sim.position / sim.MAX_HEIGHT) * rH;
    const spY  = rBot - (sim.setpoint  / sim.MAX_HEIGHT) * rH;
    const outAbs = Math.abs(sim.motorOutput);

    // ── Palette ──────────────────────────────────────────
    const c = dark
      ? { bg:'#18181c', bg2:'#1e1e22',
          rail:'#4a4a52', railHi:'#5e5e66', railLo:'#303038',
          metal:'#3e3e46', metalHi:'#58585e', metalLo:'#2a2a30',
          chain:'#56565c', txt:'#606068', dim:'#36363c', grid:'#24242a',
          bolt:'#303036', shadow:'rgba(0,0,0,0.35)' }
      : { bg:'#f4f5f7', bg2:'#eef0f2',
          rail:'#b0b0b8', railHi:'#c8c8d0', railLo:'#8a8a92',
          metal:'#a0a0a8', metalHi:'#c4c4cc', metalLo:'#808088',
          chain:'#9a9aa2', txt:'#808088', dim:'#d0d0d4', grid:'#e2e2e8',
          bolt:'#8a8a92', shadow:'rgba(0,0,0,0.08)' };

    // ── Background ───────────────────────────────────────
    const bgG = ctx.createLinearGradient(0, 0, 0, h);
    bgG.addColorStop(0, c.bg); bgG.addColorStop(1, c.bg2);
    ctx.fillStyle = bgG;
    ctx.fillRect(0, 0, w, h);

    // ── Subtle background grid ───────────────────────────
    ctx.strokeStyle = c.grid;
    ctx.lineWidth = 0.3;
    for (let gx = pad.left; gx < w; gx += 20) {
      ctx.beginPath(); ctx.moveTo(gx, rTop); ctx.lineTo(gx, rBot); ctx.stroke();
    }
    for (let gy = rTop; gy <= rBot; gy += 20) {
      ctx.beginPath(); ctx.moveTo(pad.left, gy); ctx.lineTo(w - pad.right, gy); ctx.stroke();
    }

    // ── Title ────────────────────────────────────────────
    ctx.font = 'bold 10px Roboto';
    ctx.fillStyle = c.txt;
    ctx.textAlign = 'center';
    ctx.fillText('ELEVATOR', w / 2, 14);

    // ── Height ruler (left) ──────────────────────────────
    ctx.textAlign = 'right';
    ctx.font = '9px Roboto Mono';
    for (let m = 0; m <= sim.MAX_HEIGHT + 0.01; m += 0.1) {
      const y = rBot - (m / sim.MAX_HEIGHT) * rH;
      const major = Math.abs(Math.round(m * 10) % 5) < 1;
      ctx.strokeStyle = major ? c.txt : c.dim;
      ctx.lineWidth = major ? 1 : 0.5;
      ctx.beginPath();
      ctx.moveTo(pad.left - (major ? 6 : 3), y);
      ctx.lineTo(pad.left, y);
      ctx.stroke();
      if (major) {
        ctx.fillStyle = c.txt;
        ctx.fillText(m.toFixed(1), pad.left - 8, y + 3);
      }
    }

    // ── Meter unit label ─────────────────────────────────
    ctx.font = '8px Roboto';
    ctx.fillStyle = c.dim;
    ctx.textAlign = 'right';
    ctx.fillText('meters', pad.left - 4, rTop - 6);

    // ── Top & bottom plates ──────────────────────────────
    const plateW = gap + rW + 28;
    const plateH = 6;
    for (const py of [rTop - plateH, rBot]) {
      const pg = ctx.createLinearGradient(0, py, 0, py + plateH);
      pg.addColorStop(0, c.metalHi);
      pg.addColorStop(1, c.metalLo);
      ctx.fillStyle = pg;
      ctx.beginPath();
      ctx.roundRect(cx - plateW / 2, py, plateW, plateH, 2);
      ctx.fill();
      ctx.strokeStyle = c.railHi;
      ctx.lineWidth = 0.5;
      ctx.stroke();
      // Plate bolts
      ctx.fillStyle = c.bolt;
      for (const bx of [-plateW / 2 + 6, -10, 10, plateW / 2 - 6]) {
        ctx.beginPath();
        ctx.arc(cx + bx, py + plateH / 2, 1.3, 0, Math.PI * 2);
        ctx.fill();
      }
    }

    // ── Rails (aluminum extrusion with T-slot) ───────────
    for (const rx of [LR, RR]) {
      // Metallic gradient
      const rg = ctx.createLinearGradient(rx - rW / 2, 0, rx + rW / 2, 0);
      rg.addColorStop(0,    c.railLo);
      rg.addColorStop(0.2,  c.railHi);
      rg.addColorStop(0.5,  c.rail);
      rg.addColorStop(0.8,  c.railHi);
      rg.addColorStop(1,    c.railLo);
      ctx.fillStyle = rg;
      ctx.fillRect(rx - rW / 2, rTop, rW, rH);

      // T-slot center groove
      ctx.strokeStyle = c.railLo;
      ctx.lineWidth = 2;
      ctx.beginPath(); ctx.moveTo(rx, rTop); ctx.lineTo(rx, rBot); ctx.stroke();

      // Inner groove shoulders
      ctx.strokeStyle = dark ? '#3a3a42' : '#9898a0';
      ctx.lineWidth = 0.5;
      ctx.beginPath();
      ctx.moveTo(rx - 2, rTop); ctx.lineTo(rx - 2, rBot);
      ctx.moveTo(rx + 2, rTop); ctx.lineTo(rx + 2, rBot);
      ctx.stroke();

      // Edge highlights
      ctx.strokeStyle = c.railHi;
      ctx.lineWidth = 0.4;
      ctx.beginPath();
      ctx.moveTo(rx - rW / 2, rTop); ctx.lineTo(rx - rW / 2, rBot);
      ctx.moveTo(rx + rW / 2, rTop); ctx.lineTo(rx + rW / 2, rBot);
      ctx.stroke();

      // Bolt holes
      ctx.fillStyle = c.bolt;
      for (let y = rTop + 14; y < rBot; y += 26) {
        ctx.beginPath();
        ctx.arc(rx, y, 1.3, 0, Math.PI * 2);
        ctx.fill();
      }
    }

    // ── Cross-braces (periodic horizontal struts) ────────
    ctx.strokeStyle = dark ? '#3a3a42' : '#a8a8b0';
    ctx.lineWidth = 1.5;
    const braceSpacing = rH / 4;
    for (let i = 1; i < 4; i++) {
      const by = rTop + i * braceSpacing;
      // Only draw if carriage isn't blocking
      if (Math.abs(by - posY) > 20) {
        ctx.beginPath();
        ctx.moveTo(LR + rW / 2, by);
        ctx.lineTo(RR - rW / 2, by);
        ctx.stroke();
        // Small bolts at ends
        ctx.fillStyle = c.bolt;
        ctx.beginPath(); ctx.arc(LR + rW / 2 + 2, by, 1, 0, Math.PI * 2); ctx.fill();
        ctx.beginPath(); ctx.arc(RR - rW / 2 - 2, by, 1, 0, Math.PI * 2); ctx.fill();
      }
    }

    // ── Chain drive ──────────────────────────────────────
    const chX = RR + rW / 2 + 8;
    const chBot = rBot - 8;
    const linkH = 5;
    const linkGap = 2;
    ctx.lineCap = 'round';
    for (let y = chBot; y >= Math.max(posY - 8, rTop + 4); y -= (linkH + linkGap)) {
      // Alternate link shading for realism
      const linkIdx = Math.round((chBot - y) / (linkH + linkGap));
      ctx.strokeStyle = linkIdx % 2 === 0 ? c.chain : c.railHi;
      ctx.lineWidth = 2.5;
      ctx.beginPath();
      ctx.moveTo(chX, y);
      ctx.lineTo(chX, Math.max(y - linkH, rTop + 4));
      ctx.stroke();
      // Pin
      ctx.fillStyle = c.metalHi;
      ctx.beginPath();
      ctx.arc(chX, y, 1.3, 0, Math.PI * 2);
      ctx.fill();
    }
    ctx.lineCap = 'butt';

    // Chain coming back down (return side, slightly offset)
    const chX2 = chX + 6;
    ctx.globalAlpha = 0.35;
    for (let y = rTop + 8; y <= chBot; y += (linkH + linkGap)) {
      ctx.strokeStyle = c.chain;
      ctx.lineWidth = 1.5;
      ctx.beginPath();
      ctx.moveTo(chX2, y);
      ctx.lineTo(chX2, Math.min(y + linkH, chBot));
      ctx.stroke();
    }
    ctx.globalAlpha = 1;

    // ── Sprocket (bottom) ────────────────────────────────
    const skY = chBot + 6;
    const skR = 8;
    // Sprocket body
    ctx.beginPath();
    ctx.arc(chX + 3, skY, skR, 0, Math.PI * 2);
    ctx.fillStyle = c.metal;
    ctx.fill();
    ctx.strokeStyle = c.railHi;
    ctx.lineWidth = 1;
    ctx.stroke();
    // Teeth (rotate with position)
    const rot = sim.position * 14;
    for (let i = 0; i < 10; i++) {
      const a = (i / 10) * Math.PI * 2 + rot;
      const tx = chX + 3 + Math.cos(a) * (skR - 1.5);
      const ty = skY + Math.sin(a) * (skR - 1.5);
      ctx.fillStyle = c.railHi;
      ctx.beginPath();
      ctx.arc(tx, ty, 1.2, 0, Math.PI * 2);
      ctx.fill();
    }
    // Center bore
    ctx.fillStyle = c.railLo;
    ctx.beginPath();
    ctx.arc(chX + 3, skY, 2.5, 0, Math.PI * 2);
    ctx.fill();

    // ── Idler sprocket (top) ─────────────────────────────
    const idlY = rTop + 6;
    ctx.beginPath();
    ctx.arc(chX + 3, idlY, 5, 0, Math.PI * 2);
    ctx.fillStyle = c.metal;
    ctx.fill();
    ctx.strokeStyle = c.railHi;
    ctx.lineWidth = 0.7;
    ctx.stroke();
    ctx.fillStyle = c.railLo;
    ctx.beginPath();
    ctx.arc(chX + 3, idlY, 1.5, 0, Math.PI * 2);
    ctx.fill();

    // ── Motor housing ────────────────────────────────────
    const mW = 34;
    const mH = 18;
    const mX = chX + 3 - mW / 2;
    const mY = skY + skR + 4;

    // Glow based on motor output
    if (outAbs > 0.02) {
      ctx.shadowColor = sim.motorOutput > 0
        ? `rgba(76,175,80,${Math.min(outAbs * 0.5, 0.45)})`
        : `rgba(244,67,54,${Math.min(outAbs * 0.5, 0.45)})`;
      ctx.shadowBlur = 8 + outAbs * 12;
    }
    const mg = ctx.createLinearGradient(mX, mY, mX, mY + mH);
    mg.addColorStop(0, c.metalHi);
    mg.addColorStop(0.5, c.metalLo);
    mg.addColorStop(1, c.metalHi);
    ctx.beginPath();
    ctx.roundRect(mX, mY, mW, mH, 3);
    ctx.fillStyle = mg;
    ctx.fill();
    ctx.strokeStyle = c.railHi;
    ctx.lineWidth = 0.7;
    ctx.stroke();
    ctx.shadowColor = 'transparent';
    ctx.shadowBlur = 0;

    // Motor label
    ctx.font = 'bold 6.5px Roboto Mono';
    ctx.fillStyle = c.txt;
    ctx.textAlign = 'center';
    ctx.fillText('TalonFX', mX + mW / 2, mY + mH / 2 + 2);

    // Shaft from motor to sprocket
    ctx.strokeStyle = c.metalHi;
    ctx.lineWidth = 2;
    ctx.beginPath();
    ctx.moveTo(chX + 3, mY);
    ctx.lineTo(chX + 3, skY + skR);
    ctx.stroke();

    // ── Setpoint indicator ───────────────────────────────
    // Triangle marker
    ctx.fillStyle = '#ff9100';
    ctx.beginPath();
    ctx.moveTo(pad.left + 1, spY);
    ctx.lineTo(pad.left + 8, spY - 5);
    ctx.lineTo(pad.left + 8, spY + 5);
    ctx.closePath();
    ctx.fill();

    // Dashed line across
    ctx.strokeStyle = 'rgba(255,145,0,0.45)';
    ctx.lineWidth = 1.2;
    ctx.setLineDash([5, 3]);
    ctx.beginPath();
    ctx.moveTo(pad.left + 10, spY);
    ctx.lineTo(LR - rW / 2 - 2, spY);
    ctx.moveTo(RR + rW / 2 + 2, spY);
    ctx.lineTo(chX - 4, spY);
    ctx.stroke();
    ctx.setLineDash([]);

    // SP label
    ctx.font = 'bold 8px Roboto Mono';
    ctx.fillStyle = '#ff9100';
    ctx.textAlign = 'left';
    ctx.fillText('SP ' + sim.setpoint.toFixed(1) + 'm', chX + 14, spY + 3);

    // ── Carriage ─────────────────────────────────────────
    const cW = gap + rW + 14;
    const cH = 26;
    const cX = cx - cW / 2;
    const cY = posY - cH / 2;

    // Shadow
    ctx.fillStyle = c.shadow;
    ctx.beginPath();
    ctx.roundRect(cX + 3, cY + 3, cW, cH, 4);
    ctx.fill();

    // Carriage color (shifts with motor output)
    let cc;
    if (outAbs < 0.01) {
      cc = dark ? '#4e4e58' : '#b0b0b8';
    } else if (sim.motorOutput > 0) {
      const t = Math.min(outAbs, 1);
      cc = `rgb(${55 + t * 25 | 0},${115 + t * 105 | 0},${55 + t * 15 | 0})`;
    } else {
      const t = Math.min(outAbs, 1);
      cc = `rgb(${155 + t * 85 | 0},${60 - t * 15 | 0},${60 - t * 15 | 0})`;
    }

    // Carriage body with subtle gradient
    const cg = ctx.createLinearGradient(0, cY, 0, cY + cH);
    cg.addColorStop(0, cc);
    cg.addColorStop(0.5, dark ? '#5a5a64' : '#c0c0c8');
    cg.addColorStop(1, cc);
    ctx.beginPath();
    ctx.roundRect(cX, cY, cW, cH, 4);
    ctx.fillStyle = cg;
    ctx.fill();
    ctx.strokeStyle = dark ? '#6e6e78' : '#8a8a94';
    ctx.lineWidth = 1.5;
    ctx.stroke();

    // Bearing blocks (4 linear bearings riding on rails)
    const bs = 8;
    for (const rx of [LR, RR]) {
      for (const dy of [-cH / 2 + 2, cH / 2 - bs - 2]) {
        // Block body
        const bbg = ctx.createLinearGradient(rx - bs / 2, 0, rx + bs / 2, 0);
        bbg.addColorStop(0, c.metalLo);
        bbg.addColorStop(0.5, c.metalHi);
        bbg.addColorStop(1, c.metalLo);
        ctx.fillStyle = bbg;
        ctx.fillRect(rx - bs / 2, posY + dy, bs, bs);
        ctx.strokeStyle = c.railHi;
        ctx.lineWidth = 0.5;
        ctx.strokeRect(rx - bs / 2, posY + dy, bs, bs);
        // Bore hole
        ctx.fillStyle = c.bolt;
        ctx.beginPath();
        ctx.arc(rx, posY + dy + bs / 2, 1.5, 0, Math.PI * 2);
        ctx.fill();
      }
    }

    // Center bolt pattern
    ctx.fillStyle = dark ? '#62626a' : '#a0a0a8';
    for (const dx of [-8, -3, 3, 8]) {
      ctx.beginPath();
      ctx.arc(cx + dx, posY, 1.1, 0, Math.PI * 2);
      ctx.fill();
    }

    // Chain attachment bracket
    ctx.strokeStyle = c.chain;
    ctx.lineWidth = 1.5;
    ctx.beginPath();
    ctx.moveTo(cX + cW, posY);
    ctx.lineTo(chX, posY);
    ctx.stroke();
    // Bracket plate
    ctx.fillStyle = c.metal;
    ctx.beginPath();
    ctx.roundRect(chX - 3, posY - 4, 6, 8, 1);
    ctx.fill();
    ctx.strokeStyle = c.railHi;
    ctx.lineWidth = 0.5;
    ctx.stroke();

    // ── Position readout on carriage ─────────────────────
    ctx.font = 'bold 9px Roboto Mono';
    ctx.fillStyle = '#fff';
    ctx.textAlign = 'center';
    ctx.globalAlpha = 0.9;
    ctx.fillText(sim.position.toFixed(2) + 'm', cx, posY + 3);
    ctx.globalAlpha = 1;

    // ── Output bar (right edge) ──────────────────────────
    const barX = w - 10;
    const barW = 4;
    const barH = rH * 0.6;
    const barTop = rTop + (rH - barH) / 2;
    // Background
    ctx.fillStyle = c.dim;
    ctx.beginPath();
    ctx.roundRect(barX - barW / 2, barTop, barW, barH, 2);
    ctx.fill();
    // Fill
    const fillH = Math.abs(sim.motorOutput) * barH / 2;
    const fillColor = sim.motorOutput > 0 ? '#4caf50' : '#f44336';
    const fillY = sim.motorOutput > 0
      ? barTop + barH / 2 - fillH
      : barTop + barH / 2;
    ctx.fillStyle = fillColor;
    ctx.globalAlpha = 0.7;
    ctx.beginPath();
    ctx.roundRect(barX - barW / 2, fillY, barW, fillH, 1);
    ctx.fill();
    ctx.globalAlpha = 1;
    // Zero line
    ctx.strokeStyle = c.txt;
    ctx.lineWidth = 0.5;
    ctx.beginPath();
    ctx.moveTo(barX - barW, barTop + barH / 2);
    ctx.lineTo(barX + barW, barTop + barH / 2);
    ctx.stroke();
    // Label
    ctx.font = '7px Roboto Mono';
    ctx.fillStyle = c.txt;
    ctx.textAlign = 'center';
    ctx.fillText('V', barX, barTop - 4);
  }
}

/* ═══════════════════════════════════════════════════════════
   Graph Renderer  –  Position vs Setpoint over time
   ═══════════════════════════════════════════════════════════ */

class GraphRenderer {
  constructor(canvas) {
    this.canvas = canvas;
    this.ctx = canvas.getContext('2d');
    this.dpr = window.devicePixelRatio || 1;
    this._resize();
    window.addEventListener('resize', () => this._resize());
  }

  _resize() {
    const rect = this.canvas.getBoundingClientRect();
    this.canvas.width = rect.width * this.dpr;
    this.canvas.height = rect.height * this.dpr;
    this.ctx.setTransform(this.dpr, 0, 0, this.dpr, 0, 0);
    this.w = rect.width;
    this.h = rect.height;
  }

  draw(history, maxHeight) {
    const ctx = this.ctx;
    const w = this.w;
    const h = this.h;
    ctx.clearRect(0, 0, w, h);

    const isDark = document.documentElement.getAttribute('data-theme') !== 'light';

    const pad = { top: 18, right: 12, bottom: 22, left: 40 };
    const gw = w - pad.left - pad.right;
    const gh = h - pad.top - pad.bottom;

    // Title
    ctx.font = '10px Roboto';
    ctx.fillStyle = isDark ? '#888' : '#999';
    ctx.textAlign = 'left';
    ctx.fillText('Position vs Setpoint', pad.left, 12);

    // Axes
    ctx.strokeStyle = isDark ? '#444' : '#ddd';
    ctx.lineWidth = 1;
    ctx.beginPath();
    ctx.moveTo(pad.left, pad.top);
    ctx.lineTo(pad.left, pad.top + gh);
    ctx.lineTo(pad.left + gw, pad.top + gh);
    ctx.stroke();

    // Y-axis labels
    ctx.font = '9px Roboto Mono';
    ctx.fillStyle = isDark ? '#666' : '#aaa';
    ctx.textAlign = 'right';
    for (let v = 0; v <= maxHeight; v += 0.5) {
      const y = pad.top + gh - (v / maxHeight) * gh;
      ctx.fillText(v.toFixed(1), pad.left - 6, y + 3);
      // grid line
      ctx.strokeStyle = isDark ? '#333' : '#eee';
      ctx.beginPath();
      ctx.moveTo(pad.left, y);
      ctx.lineTo(pad.left + gw, y);
      ctx.stroke();
    }

    if (history.length < 2) return;

    // Map data to pixels
    const tMin = history[0].t;
    const tMax = history[history.length - 1].t;
    const tRange = Math.max(tMax - tMin, 0.1);

    const toX = (t) => pad.left + ((t - tMin) / tRange) * gw;
    const toY = (v) => pad.top + gh - (v / maxHeight) * gh;

    // Draw setpoint line
    ctx.strokeStyle = '#ff9100';
    ctx.lineWidth = 2;
    ctx.setLineDash([4, 4]);
    ctx.beginPath();
    for (let i = 0; i < history.length; i++) {
      const x = toX(history[i].t);
      const y = toY(history[i].sp);
      i === 0 ? ctx.moveTo(x, y) : ctx.lineTo(x, y);
    }
    ctx.stroke();
    ctx.setLineDash([]);

    // Draw position line
    ctx.strokeStyle = '#4caf50';
    ctx.lineWidth = 2;
    ctx.beginPath();
    for (let i = 0; i < history.length; i++) {
      const x = toX(history[i].t);
      const y = toY(history[i].pos);
      i === 0 ? ctx.moveTo(x, y) : ctx.lineTo(x, y);
    }
    ctx.stroke();

    // Draw motor output as a filled area (subtle)
    ctx.fillStyle = isDark ? 'rgba(76,175,80,0.08)' : 'rgba(76,175,80,0.05)';
    ctx.beginPath();
    ctx.moveTo(toX(history[0].t), pad.top + gh);
    for (let i = 0; i < history.length; i++) {
      const x = toX(history[i].t);
      const y = pad.top + gh - (Math.abs(history[i].out) * 0.3) * gh;
      ctx.lineTo(x, y);
    }
    ctx.lineTo(toX(history[history.length - 1].t), pad.top + gh);
    ctx.closePath();
    ctx.fill();

    // Legend
    const legX = pad.left + gw - 140;
    const legY = pad.top + 6;
    ctx.font = '9px Roboto';
    // Position
    ctx.fillStyle = '#4caf50';
    ctx.fillRect(legX, legY, 12, 3);
    ctx.fillStyle = isDark ? '#aaa' : '#666';
    ctx.textAlign = 'left';
    ctx.fillText('Position', legX + 16, legY + 5);
    // Setpoint
    ctx.fillStyle = '#ff9100';
    ctx.fillRect(legX + 70, legY, 12, 3);
    ctx.fillStyle = isDark ? '#aaa' : '#666';
    ctx.fillText('Setpoint', legX + 86, legY + 5);
  }
}
