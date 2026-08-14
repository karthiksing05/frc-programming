/* ---------------------------------------------------------------------------
 * robot3d.js — an interactive 3D mechanism viewer, with no dependencies.
 *
 * Renders the two reference robots as jointed solids you can drag around and
 * drive with sliders. The joint limits and preset positions are the SAME
 * NUMBERS as Constants.java in the curriculum project, so the thing you drag
 * here is the thing you command in lessons 05, 06 and 14.
 *
 * Deliberately dependency-free: no three.js, no CDN, no WebGL. Canvas 2D, a
 * painter's algorithm and Newell normals are plenty for a few hundred faces,
 * and it keeps the site working offline and inside a single HTML file.
 * ------------------------------------------------------------------------- */
(function () {
  "use strict";

  /* ---- minimal 4x4 matrix maths (column-major, like OpenGL) ------------- */
  function mul(a, b) {
    const o = new Array(16);
    for (let c = 0; c < 4; c++)
      for (let r = 0; r < 4; r++)
        o[c*4+r] = a[r]*b[c*4] + a[4+r]*b[c*4+1] + a[8+r]*b[c*4+2] + a[12+r]*b[c*4+3];
    return o;
  }
  const T = (x,y,z) => [1,0,0,0, 0,1,0,0, 0,0,1,0, x,y,z,1];
  const Rx = t => { const c=Math.cos(t), s=Math.sin(t);
    return [1,0,0,0, 0,c,s,0, 0,-s,c,0, 0,0,0,1]; };
  const Ry = t => { const c=Math.cos(t), s=Math.sin(t);
    return [c,0,-s,0, 0,1,0,0, s,0,c,0, 0,0,0,1]; };
  const Rz = t => { const c=Math.cos(t), s=Math.sin(t);
    return [c,s,0,0, -s,c,0,0, 0,0,1,0, 0,0,0,1]; };
  const xf = (m, p) => ({
    x: m[0]*p.x + m[4]*p.y + m[8]*p.z  + m[12],
    y: m[1]*p.x + m[5]*p.y + m[9]*p.z  + m[13],
    z: m[2]*p.x + m[6]*p.y + m[10]*p.z + m[14],
  });

  /* ---- primitives -------------------------------------------------------
   * Face winding is counter-clockwise seen from OUTSIDE, so Newell normals
   * come out pointing outward and back-face culling keeps the right half.
   * Getting this backwards renders the inside of every solid, which looks
   * exactly as wrong as it sounds.
   * --------------------------------------------------------------------- */
  const CUBE_V = [[0,0,0],[1,0,0],[1,1,0],[0,1,0],[0,0,1],[1,0,1],[1,1,1],[0,1,1]];
  const CUBE_F = [[3,2,1,0],[6,7,4,5],[7,3,0,4],[2,6,5,1],[7,6,2,3],[0,1,5,4]];

  function box(w, h, d, anchor, color) {
    let ox = -w/2, oy = -h/2, oz = -d/2;
    if (anchor === "base") oy = 0;
    if (anchor === "end")  { ox = 0; oy = -h/2; }
    return {
      verts: CUBE_V.map(([i,j,k]) => ({ x: ox+i*w, y: oy+j*h, z: oz+k*d })),
      faces: CUBE_F,
      color: color,
    };
  }

  // A cylinder centred on the origin, running along one axis. Used for wheels
  // and flywheels, which read as round even at this polygon count.
  function cyl(radius, length, axis, color, seg) {
    seg = seg || 14;
    const verts = [], faces = [];
    for (let end = 0; end < 2; end++) {
      const t = (end ? 0.5 : -0.5) * length;
      for (let i = 0; i < seg; i++) {
        const a = (i / seg) * Math.PI * 2;
        const u = Math.cos(a) * radius, v = Math.sin(a) * radius;
        verts.push(axis === "x" ? { x:t, y:u, z:v }
                 : axis === "y" ? { x:u, y:t, z:v }
                 :                { x:u, y:v, z:t });
      }
    }
    for (let i = 0; i < seg; i++) {
      const j = (i + 1) % seg;
      faces.push([i, j, seg + j, seg + i]);          // side
    }
    const capA = [], capB = [];
    for (let i = 0; i < seg; i++) { capA.push(seg - 1 - i); capB.push(seg + i); }
    faces.push(capA, capB);
    return { verts, faces, color };
  }

  /* ---- the two robots ---------------------------------------------------
   * Built from the teams' own published CAD renders, so the silhouettes read
   * as the real machines rather than as generic boxes:
   *   Presto  6328's robot-image.png — indigo bumpers on a plywood frame, a
   *           row of cream rollers across the front, blue pocketed plate
   *           structure, black motors and timing-belt pulleys.
   *   Kelpie  8033's published elevator render — a tall rectangular aluminium
   *           gantry with black triangular corner gussets and a cable chain up
   *           one side, on a square swerve chassis.
   * Joint limits and presets still come from Constants.java.
   * --------------------------------------------------------------------- */
  const ALU   = "#aeb5bd",  // bare aluminium tube
        STEEL = "#7e858e",
        BLACK = "#2b2f36",  // gussets, motors, pulleys
        TYRE  = "#33383f",
        INDIGO= "#33357e",  // 6328's bumper blue
        BLUEAL= "#3a5fa8",  // their anodised blue plate
        CREAM = "#e4dcc9",  // the rollers, which are the thing you notice
        PURPLE= "#7d4f9e",  // 8033's accent
        WHITE = "#eceff2";

  // Four swerve modules at the corners of a square frame. The module body is
  // the tall black box; the wheel hangs below it.
  function swerveCorners(add, half, yTop) {
    for (const sx of [-half, half])
      for (const sz of [-half, half]) {
        add(T(sx, yTop - 0.02, sz), box(0.11, 0.13, 0.10, "center", BLACK));  // module
        add(T(sx, 0.06, sz), cyl(0.06, 0.05, "x", TYRE, 14));                 // wheel
        add(T(sx, 0.06, sz), cyl(0.022, 0.056, "x", STEEL, 8));               // hub
      }
  }

  const ROBOTS = {
    kelpie: {
      camera: { yaw: -0.85, pitch: 0.17, dist: 1.95, target: [0.04, 0.95, 0] },
      caption: "Kelpie — elevator, shoulder and wrist",
      note: "Three joints that all have to agree before a Coral goes where you meant.",
      controls: [
        { id:"height", label:"Elevator height", unit:"m",
          min:0, max:1.60, step:0.01, value:0.90,
          presets:[["Stow",0.05],["Low",0.45],["Mid",0.90],["High",1.45]] },
        { id:"shoulder", label:"Shoulder angle", unit:"°",
          min:-90, max:90, step:1, value:25,
          presets:[["Down",-45],["Level",0],["Up",60]] },
        { id:"wrist", label:"Wrist angle", unit:"°",
          min:-90, max:90, step:1, value:0, presets:[["Flat",0],["Turned",90]] },
      ],
      build(s) {
        const parts = [], add = (m, b) => parts.push({ m, b });

        // ---- square swerve chassis, 29.5 inch frame ----
        add(T(0, 0.165, 0), box(0.60, 0.045, 0.60, "center", ALU));     // top deck
        for (const sz of [-0.36, 0.36])
          add(T(0, 0.165, sz), box(0.76, 0.075, 0.045, "center", ALU)); // side rails
        for (const sx of [-0.36, 0.36])
          add(T(sx, 0.165, 0), box(0.045, 0.075, 0.76, "center", ALU));
        add(T(0, 0.215, 0.10), box(0.28, 0.09, 0.20, "center", BLACK)); // battery
        swerveCorners(add, 0.30, 0.185);

        // ---- elevator: a closed rectangular gantry, not two loose posts ----
        const GY = 0.16, GH = 0.92;                    // fixed stage
        for (const dx of [-0.17, 0.17])
          add(T(dx, GY, -0.24), box(0.045, GH, 0.075, "base", ALU));
        add(T(0, GY + GH, -0.24), box(0.42, 0.075, 0.09, "center", ALU));   // top rail
        add(T(0, GY + 0.02, -0.24), box(0.42, 0.06, 0.09, "center", ALU));  // bottom rail
        // black triangular corner gussets, the thing you notice on the real one
        for (const dx of [-0.17, 0.17])
          for (const dy of [GY + 0.05, GY + GH - 0.09])
            add(T(dx, dy, -0.20), box(0.055, 0.09, 0.02, "center", BLACK));

        // Second stage cascades up at half the carriage rate.
        const lift = s.height;
        for (const dx of [-0.115, 0.115])
          add(T(dx, GY + lift * 0.5, -0.24), box(0.035, GH, 0.05, "base", STEEL));
        // cable chain running up one side
        for (let i = 0; i < 9; i++)
          add(T(0.215, GY + 0.08 + i * 0.105 + lift * 0.25, -0.24),
              box(0.03, 0.07, 0.045, "center", BLACK));
        add(T(0, GY + GH + 0.04, -0.24), box(0.10, 0.03, 0.06, "center", PURPLE));

        // ---- carriage ----
        const carriage = T(0, 0.22 + lift, -0.24);
        add(carriage, box(0.30, 0.14, 0.10, "center", STEEL));
        add(mul(carriage, T(0, 0, 0.07)), box(0.13, 0.10, 0.05, "center", STEEL)); // gearbox

        // ---- shoulder: arm pitches in the XY plane about a Z axle ----
        const sh = mul(mul(carriage, T(0, 0, 0.09)), Rz(s.shoulder * Math.PI/180));
        add(sh, cyl(0.05, 0.13, "z", BLACK, 12));
        add(mul(sh, T(0, 0, 0)), box(0.56, 0.07, 0.11, "end", ALU));
        add(mul(sh, T(0.16, 0, 0)), box(0.10, 0.085, 0.115, "center", BLACK)); // motor

        // ---- wrist rolls the end effector about the arm's own axis ----
        const wr = mul(mul(sh, T(0.56, 0, 0)), Rx(s.wrist * Math.PI/180));
        add(wr, cyl(0.042, 0.11, "z", BLACK, 12));
        add(mul(wr, T(0.055, 0, 0)), box(0.11, 0.09, 0.15, "center", ALU));
        for (const dz of [-0.075, 0.075])                      // gripper jaws
          add(mul(wr, T(0.11, 0, dz)), box(0.13, 0.05, 0.03, "end", BLACK));
        for (const dz of [-0.045, 0.045])                      // intake rollers
          add(mul(wr, T(0.15, 0, dz)), cyl(0.028, 0.05, "x", CREAM, 10));
        // a Coral, held across the jaws
        add(mul(wr, T(0.19, 0, 0)), cyl(0.028, 0.32, "z", WHITE, 12));   // a Coral, held across the jaws
        return parts;
      },
      readout(s) {
        const rad = s.shoulder * Math.PI/180;
        return [
          ["Gripper height", (0.22 + s.height + 0.56*Math.sin(rad)).toFixed(2) + " m"],
          ["Reach forward",  (0.56 * Math.cos(rad)).toFixed(2) + " m"],
        ];
      },
    },

    presto: {
      camera: { yaw: -0.9, pitch: 0.15, dist: 1.95, target: [0.06, 0.50, 0] },
      caption: "Presto — pivoting shooter and flywheels",
      note: "One angle and one speed decide where a Note lands.",
      controls: [
        { id:"arm", label:"Shooter angle", unit:"°",
          min:0, max:75, step:1, value:35, presets:[["Stow",0],["Podium",35],["Amp",60]] },
        { id:"rpm", label:"Flywheel speed", unit:"RPM",
          min:0, max:5800, step:50, value:3000, presets:[["Idle",0],["Shoot",3000],["Max",5800]] },
      ],
      build(s) {
        const parts = [], add = (m, b) => parts.push({ m, b });

        // ---- chassis: indigo bumper over a plywood frame ----
        add(T(0, 0.14, 0), box(0.68, 0.05, 0.68, "center", "#b9a06a"));  // ply frame
        add(T(0, 0.105, 0), box(0.80, 0.115, 0.80, "center", INDIGO));   // bumper
        add(T(0, 0.175, 0), box(0.62, 0.02, 0.62, "center", BLACK));     // belly pan
        swerveCorners(add, 0.30, 0.16);

        // ---- blue side plates carrying the shooter ----
        for (const dz of [-0.20, 0.20])
          add(T(-0.04, 0.29, dz), box(0.38, 0.37, 0.022, "center", BLUEAL));
        for (const dx of [-0.20, 0.10])
          add(T(dx, 0.29, 0), box(0.03, 0.35, 0.40, "center", BLUEAL));

        // ---- the row of cream rollers across the front. On the real robot
        //      these are the first thing you see, so they matter visually. ----
        for (const dx of [0.20, 0.29, 0.375])
          add(T(dx, 0.185, 0), cyl(0.048, 0.44, "z", CREAM, 12));
        for (const dz of [-0.23, 0.23])
          add(T(0.29, 0.185, dz), box(0.26, 0.16, 0.02, "center", BLACK));

        // ---- shooter pivots about a Z axle and grows along +x ----
        const pivot = mul(T(-0.06, 0.46, 0), Rz(s.arm * Math.PI/180));
        add(pivot, cyl(0.05, 0.40, "z", BLACK, 12));
        for (const dz of [-0.185, 0.185])                       // blue side plates
          add(mul(pivot, T(0, 0, dz)), box(0.46, 0.15, 0.022, "end", BLUEAL));
        add(mul(pivot, T(0, 0, 0)), box(0.44, 0.06, 0.34, "end", BLUEAL));
        add(mul(pivot, T(0.10, 0.055, 0)), box(0.13, 0.10, 0.14, "center", BLACK)); // motor

        // ---- the flywheels, spinning when commanded ----
        const spin = s._t * (s.rpm / 5800) * 9;
        for (const dz of [-0.115, 0.115]) {
          const w = mul(mul(pivot, T(0.46, 0, dz)), Rz(spin));
          add(w, cyl(0.075, 0.09, "z", CREAM, 14));
          add(w, box(0.02, 0.14, 0.095, "center", s.rpm > 0 ? PURPLE : BLACK));
          add(w, cyl(0.028, 0.10, "z", BLACK, 10));            // pulley hub
        }
        // feed roller just behind the flywheels
        add(mul(pivot, T(0.30, 0, 0)), cyl(0.045, 0.30, "z", CREAM, 12));
        return parts;
      },
      readout(s) {
        const mps = (s.rpm / 60) * Math.PI * 0.1016;   // 4-inch wheel
        return [
          ["Wheel surface speed", mps.toFixed(1) + " m/s"],
          ["Fraction of free speed", Math.round(s.rpm / 5800 * 100) + " %"],
        ];
      },
    },
  };

  /* ---- viewer ----------------------------------------------------------- */
  function Viewer(root) {
    const def = ROBOTS[root.getAttribute("data-robot")];
    if (!def) return;

    const state = { _t: 0 };
    def.controls.forEach(c => state[c.id] = c.value);

    const canvas = document.createElement("canvas");
    canvas.className = "r3d-canvas";
    canvas.setAttribute("role", "img");
    canvas.setAttribute("aria-label", def.caption + ". " + def.note);
    const panel = document.createElement("div");
    panel.className = "r3d-panel";
    root.appendChild(canvas);
    root.appendChild(panel);

    const readoutEl = document.createElement("div");
    readoutEl.className = "r3d-readout";

    def.controls.forEach(c => {
      const wrap = document.createElement("div");
      wrap.className = "r3d-control";
      const lab = document.createElement("label");
      lab.className = "r3d-label";
      const val = document.createElement("span");
      val.className = "r3d-value";
      lab.textContent = c.label;
      lab.appendChild(val);

      const slider = document.createElement("input");
      slider.type = "range";
      slider.min = c.min; slider.max = c.max; slider.step = c.step;
      slider.value = c.value;
      slider.className = "r3d-slider";
      slider.setAttribute("aria-label", c.label);
      const show = () => {
        val.textContent = (+slider.value).toFixed(c.step < 1 ? 2 : 0) + " " + c.unit;
      };
      slider.addEventListener("input", () => {
        state[c.id] = +slider.value; show(); draw(); kick();
      });
      show();

      const presets = document.createElement("div");
      presets.className = "r3d-presets";
      (c.presets || []).forEach(([name, v]) => {
        const b = document.createElement("button");
        b.type = "button";
        b.className = "r3d-preset";
        b.textContent = name;
        b.addEventListener("click", () => {
          slider.value = v; state[c.id] = v; show(); draw(); kick();
        });
        presets.appendChild(b);
      });

      wrap.appendChild(lab); wrap.appendChild(slider); wrap.appendChild(presets);
      panel.appendChild(wrap);
    });
    panel.appendChild(readoutEl);

    const hint = document.createElement("p");
    hint.className = "r3d-hint";
    hint.textContent = "Drag the picture to orbit. " + def.note;
    panel.appendChild(hint);

    /* ---- camera ---- */
    const cam = def.camera;
    let yaw = cam.yaw, pitch = cam.pitch;
    const dist = cam.dist;
    let dragging = false, lastX = 0, lastY = 0;

    const onDown = e => {
      dragging = true;
      const p = e.touches ? e.touches[0] : e;
      lastX = p.clientX; lastY = p.clientY;
    };
    const onMove = e => {
      if (!dragging) return;
      const p = e.touches ? e.touches[0] : e;
      yaw   += (p.clientX - lastX) * 0.01;
      pitch += (p.clientY - lastY) * 0.01;
      pitch = Math.max(-0.15, Math.min(1.15, pitch));
      lastX = p.clientX; lastY = p.clientY;
      if (e.cancelable) e.preventDefault();
      draw();
    };
    const onUp = () => { dragging = false; };

    canvas.addEventListener("mousedown", onDown);
    window.addEventListener("mousemove", onMove);
    window.addEventListener("mouseup", onUp);
    canvas.addEventListener("touchstart", onDown, { passive: true });
    canvas.addEventListener("touchmove", onMove, { passive: false });
    canvas.addEventListener("touchend", onUp);

    /* ---- draw ---- */
    function draw() {
      const cssW = Math.max(240, canvas.clientWidth || root.clientWidth || 560);
      const cssH = Math.max(300, Math.round(cssW * 0.72));
      const dpr = window.devicePixelRatio || 1;
      canvas.width = cssW * dpr;
      canvas.height = cssH * dpr;
      canvas.style.height = cssH + "px";

      const ctx = canvas.getContext("2d");
      ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
      ctx.clearRect(0, 0, cssW, cssH);

      const view = mul(
        mul(mul(T(0, 0, -dist), Rx(pitch)), Ry(yaw)),
        T(-cam.target[0], -cam.target[1], -cam.target[2]));
      const focal = cssH * 1.05;
      const project = p => {
        const z = Math.max(0.05, -p.z);
        return { X: cssW/2 + p.x * focal / z, Y: cssH/2 - p.y * focal / z };
      };

      // Floor grid, so the height slider reads as height and not as zoom.
      ctx.lineWidth = 1;
      const grid = getComputedStyle(root).getPropertyValue("--r3d-grid").trim();
      ctx.strokeStyle = grid || "rgba(128,138,155,0.35)";
      for (let i = -3; i <= 3; i++) {
        for (const [a, b] of [
          [{x:i*0.3,y:0,z:-0.9}, {x:i*0.3,y:0,z:0.9}],
          [{x:-0.9,y:0,z:i*0.3}, {x:0.9,y:0,z:i*0.3}],
        ]) {
          const p1 = project(xf(view, a)), p2 = project(xf(view, b));
          ctx.beginPath(); ctx.moveTo(p1.X, p1.Y); ctx.lineTo(p2.X, p2.Y); ctx.stroke();
        }
      }

      // Build, transform, cull, depth-sort, paint.
      const polys = [];
      for (const { m, b } of def.build(state)) {
        const mv = mul(view, m);
        const pts3 = b.verts.map(v => xf(mv, v));
        for (const f of b.faces) {
          const pts = f.map(i => pts3[i]);
          // Newell's method — correct for any polygon, not just quads.
          let nx = 0, ny = 0, nz = 0;
          for (let i = 0; i < pts.length; i++) {
            const a = pts[i], c = pts[(i + 1) % pts.length];
            nx += (a.y - c.y) * (a.z + c.z);
            ny += (a.z - c.z) * (a.x + c.x);
            nz += (a.x - c.x) * (a.y + c.y);
          }
          const len = Math.hypot(nx, ny, nz) || 1;
          if (nz / len <= 0) continue;                     // facing away — skip
          const light = 0.42 + 0.58 * Math.max(0,
            (nx*0.35 + ny*0.78 + nz*0.52) / len);
          polys.push({
            pts, light, color: b.color,
            depth: pts.reduce((s, p) => s + p.z, 0) / pts.length,
          });
        }
      }
      polys.sort((a, b) => a.depth - b.depth);            // farthest first

      for (const q of polys) {
        const p = q.pts.map(project);
        ctx.beginPath();
        ctx.moveTo(p[0].X, p[0].Y);
        for (let i = 1; i < p.length; i++) ctx.lineTo(p[i].X, p[i].Y);
        ctx.closePath();
        ctx.fillStyle = shade(q.color, q.light);
        ctx.fill();
        ctx.strokeStyle = "rgba(0,0,0,0.22)";
        ctx.lineWidth = 0.5;
        ctx.stroke();
      }

      readoutEl.innerHTML = "";
      for (const [k, v] of def.readout(state)) {
        const row = document.createElement("div");
        row.className = "r3d-row";
        const a = document.createElement("span"); a.textContent = k;
        const b2 = document.createElement("b");   b2.textContent = v;
        row.appendChild(a); row.appendChild(b2);
        readoutEl.appendChild(row);
      }
    }

    function shade(hex, k) {
      const n = parseInt(hex.slice(1), 16);
      const c = v => Math.max(0, Math.min(255, Math.round(v * k)));
      return "rgb(" + c((n>>16)&255) + "," + c((n>>8)&255) + "," + c(n&255) + ")";
    }

    // Only animate while something is actually spinning.
    let raf = null;
    function tick() {
      if (state.rpm > 0) { state._t += 0.06; draw(); raf = requestAnimationFrame(tick); }
      else { raf = null; }
    }
    function kick() { if (state.rpm > 0 && !raf) raf = requestAnimationFrame(tick); }

    window.addEventListener("resize", draw);
    draw();
    kick();
  }

  function init() {
    document.querySelectorAll(".robot3d").forEach(el => {
      if (el.dataset.r3dReady) return;
      el.dataset.r3dReady = "1";
      Viewer(el);
    });
  }

  if (document.readyState !== "loading") init();
  else document.addEventListener("DOMContentLoaded", init);

  // Material for MkDocs swaps page content without a reload, and the
  // single-file artifact build has its own router. Rather than knowing about
  // either, watch for new nodes and wire up anything that appears.
  if (window.document$ && window.document$.subscribe) window.document$.subscribe(init);
  if (window.MutationObserver) {
    let queued = false;
    new MutationObserver(() => {
      if (queued) return;
      queued = true;
      requestAnimationFrame(() => { queued = false; init(); });
    }).observe(document.documentElement, { childList: true, subtree: true });
  }
})();
