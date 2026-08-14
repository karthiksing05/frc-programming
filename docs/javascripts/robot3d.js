/* ---------------------------------------------------------------------------
 * robot3d.js — an interactive 3D mechanism viewer, with no dependencies.
 *
 * Renders the two reference robots' mechanisms as jointed solids you can drag
 * around and drive with sliders. The joint limits and preset positions are the
 * SAME NUMBERS as Constants.java in the curriculum project, so the thing you
 * drag here is the thing you command in lesson 05 and lesson 06.
 *
 * Deliberately dependency-free: no three.js, no CDN, no WebGL. A few hundred
 * lines of canvas 2D and a painter's algorithm is plenty for a dozen boxes,
 * and it keeps the whole site working offline and inside a single HTML file.
 * ------------------------------------------------------------------------- */
(function () {
  "use strict";

  /* ---- minimal 4x4 matrix maths (column-major, like OpenGL) ------------- */
  const I = () => [1,0,0,0, 0,1,0,0, 0,0,1,0, 0,0,0,1];

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
  const xf = (m, p) => ({
    x: m[0]*p.x + m[4]*p.y + m[8]*p.z  + m[12],
    y: m[1]*p.x + m[5]*p.y + m[9]*p.z  + m[13],
    z: m[2]*p.x + m[6]*p.y + m[10]*p.z + m[14],
  });

  /* ---- a box, given size and the corner it grows from ------------------- */
  // anchor: which point of the box sits at the local origin.
  //   "center" | "base" (bottom-centre) | "end" (grows along +x from origin)
  function box(w, h, d, anchor, color) {
    let ox = -w/2, oy = -h/2, oz = -d/2;
    if (anchor === "base") oy = 0;
    if (anchor === "end")  { ox = 0; oy = -h/2; }
    const v = [];
    for (const [i,j,k] of [[0,0,0],[1,0,0],[1,1,0],[0,1,0],[0,0,1],[1,0,1],[1,1,1],[0,1,1]])
      v.push({ x: ox + i*w, y: oy + j*h, z: oz + k*d });
    return {
      verts: v,
      faces: [[0,1,2,3],[5,4,7,6],[4,0,3,7],[1,5,6,2],[3,2,6,7],[4,5,1,0]],
      color: color,
    };
  }

  /* ---- the two robots --------------------------------------------------- */
  // Every limit and preset below is lifted from Constants.java.
  const ROBOTS = {
    kelpie: {
      caption: "Kelpie — elevator, shoulder and wrist",
      note: "Three joints that all have to agree before a Coral goes where you meant.",
      controls: [
        { id:"height", label:"Elevator height", unit:"m",
          min:0, max:1.60, step:0.01, value:0.90,
          presets:[["Stow",0.05],["Low",0.45],["Mid",0.90],["High",1.45]] },
        { id:"shoulder", label:"Shoulder angle", unit:"°",
          min:-90, max:90, step:1, value:20,
          presets:[["Down",-45],["Level",0],["Up",60]] },
        { id:"wrist", label:"Wrist angle", unit:"°",
          min:-90, max:90, step:1, value:0, presets:[["Flat",0],["Turned",90]] },
      ],
      build(s) {
        const parts = [];
        const add = (m, b) => parts.push({ m, b });

        // Drivetrain, sitting on the floor.
        add(T(0,0.05,0), box(0.72, 0.10, 0.72, "center", "#5a6472"));
        // Elevator tower, fixed height, so you can see the carriage travel it.
        add(T(0,0.10,-0.16), box(0.10, 1.75, 0.10, "base", "#3f4753"));

        // Carriage rides the tower.
        const carriage = T(0, 0.10 + s.height, -0.16);
        add(carriage, box(0.26, 0.16, 0.20, "center", "#c9963f"));

        // Shoulder pivots at the carriage; the arm grows along +x from it.
        const shoulder = mul(carriage, Rx(-s.shoulder * Math.PI/180));
        add(mul(shoulder, T(0,0,0.12)), box(0.08, 0.08, 0.08, "center", "#8b93a1"));
        add(mul(shoulder, T(0,0,0.12)), box(0.60, 0.07, 0.07, "end", "#4a90d9"));

        // Wrist at the far end of the arm, then the gripper.
        const wrist = mul(mul(shoulder, T(0.60,0,0.12)), Ry(s.wrist * Math.PI/180));
        add(wrist, box(0.07, 0.07, 0.07, "center", "#8b93a1"));
        add(mul(wrist, T(0.10,0,0)), box(0.16, 0.05, 0.22, "center", "#d9534f"));
        return parts;
      },
      readout(s) {
        const reach = 0.60 * Math.cos(s.shoulder * Math.PI/180);
        const tip   = 0.10 + s.height + 0.60 * Math.sin(s.shoulder * Math.PI/180);
        return [
          ["Gripper height", tip.toFixed(2) + " m"],
          ["Reach forward",  reach.toFixed(2) + " m"],
        ];
      },
    },

    presto: {
      camera: { yaw: -0.95, pitch: 0.12, dist: 1.9, target: [0.10, 0.55, 0] },
      caption: "Presto — pivoting shooter and flywheels",
      note: "One angle and one speed decide where a Note lands.",
      controls: [
        { id:"arm", label:"Shooter angle", unit:"°",
          min:0, max:75, step:1, value:20, presets:[["Stow",0],["Podium",35],["Amp",60]] },
        { id:"rpm", label:"Flywheel speed", unit:"RPM",
          min:0, max:5800, step:50, value:3000, presets:[["Idle",0],["Shoot",3000],["Max",5800]] },
      ],
      build(s) {
        const parts = [];
        const add = (m, b) => parts.push({ m, b });

        add(T(0,0.05,0), box(0.72, 0.10, 0.72, "center", "#5a6472"));
        add(T(0,0.10,-0.10), box(0.12, 0.30, 0.12, "base", "#3f4753"));

        // The whole shooter assembly pivots about one axis.
        const pivot = mul(T(0, 0.40, -0.10), Rx(-s.arm * Math.PI/180));
        add(pivot, box(0.10, 0.10, 0.10, "center", "#8b93a1"));
        add(mul(pivot, T(0,0,0)), box(0.46, 0.06, 0.30, "end", "#4a90d9"));

        // Two flywheels at the muzzle. They visibly spin with the slider.
        const spin = s._t * s.rpm / 5800 * 8;
        for (const dz of [-0.11, 0.11]) {
          const w = mul(mul(pivot, T(0.46, 0, dz)), Rx(spin));
          add(w, box(0.16, 0.16, 0.05, "center", s.rpm > 0 ? "#e8b04b" : "#7a8290"));
          add(w, box(0.03, 0.22, 0.055, "center", "#2f353f")); // spoke, shows rotation
        }
        return parts;
      },
      readout(s) {
        // Surface speed of a 4-inch wheel, which is what actually throws the ring.
        const mps = (s.rpm / 60) * Math.PI * 0.1016;
        return [
          ["Wheel surface speed", mps.toFixed(1) + " m/s"],
          ["Fraction of free speed", Math.round(s.rpm / 5800 * 100) + " %"],
        ];
      },
    },
  };

  /* ---- viewer ----------------------------------------------------------- */
  function Viewer(root) {
    const key = root.getAttribute("data-robot");
    const def = ROBOTS[key];
    if (!def) return;

    const state = { _t: 0 };
    def.controls.forEach(c => state[c.id] = c.value);

    // --- DOM ---
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
      slider.addEventListener("input", () => { state[c.id] = +slider.value; show(); draw(); });
      show();

      const presets = document.createElement("div");
      presets.className = "r3d-presets";
      (c.presets || []).forEach(([name, v]) => {
        const b = document.createElement("button");
        b.type = "button";
        b.className = "r3d-preset";
        b.textContent = name;
        b.addEventListener("click", () => {
          slider.value = v; state[c.id] = v; show(); draw();
        });
        presets.appendChild(b);
      });

      wrap.appendChild(lab);
      wrap.appendChild(slider);
      wrap.appendChild(presets);
      panel.appendChild(wrap);
    });
    panel.appendChild(readoutEl);

    const hint = document.createElement("p");
    hint.className = "r3d-hint";
    hint.textContent = "Drag the picture to orbit. " + def.note;
    panel.appendChild(hint);

    // --- camera ---
    // Framed so the mechanism fills the canvas at its mid travel rather than
    // sitting in a corner. `target` is the point the camera orbits around.
    const cam = def.camera || { yaw: -0.95, pitch: 0.16, dist: 3.1, target: [0.02, 0.98, 0] };
    let yaw = cam.yaw, pitch = cam.pitch, dist = cam.dist;
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
      pitch = Math.max(-0.2, Math.min(1.2, pitch));
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
      const cssW = root.clientWidth || 640;
      const cssH = Math.max(260, Math.round(cssW * 0.52));
      const dpr = window.devicePixelRatio || 1;
      canvas.width = cssW * dpr;
      canvas.height = cssH * dpr;
      canvas.style.width = cssW + "px";
      canvas.style.height = cssH + "px";

      const ctx = canvas.getContext("2d");
      ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
      ctx.clearRect(0, 0, cssW, cssH);

      const view = mul(
        mul(mul(T(0, 0, -dist), Rx(pitch)), Ry(yaw)),
        T(-cam.target[0], -cam.target[1], -cam.target[2]));
      const focal = cssH * 1.25;
      const project = p => {
        const z = Math.max(0.05, -p.z);
        return { X: cssW/2 + p.x * focal / z, Y: cssH/2 - p.y * focal / z, z: z };
      };

      // Floor grid, so the height slider reads as height rather than scale.
      ctx.lineWidth = 1;
      ctx.strokeStyle = getComputedStyle(root).getPropertyValue("--r3d-grid").trim() || "#8884";
      for (let i = -2; i <= 2; i++) {
        for (const [a, b] of [
          [{x:i*0.4,y:0,z:-0.8}, {x:i*0.4,y:0,z:0.8}],
          [{x:-0.8,y:0,z:i*0.4}, {x:0.8,y:0,z:i*0.4}],
        ]) {
          const p1 = project(xf(view, a)), p2 = project(xf(view, b));
          ctx.beginPath(); ctx.moveTo(p1.X, p1.Y); ctx.lineTo(p2.X, p2.Y); ctx.stroke();
        }
      }

      // Build, transform, depth-sort, paint.
      const quads = [];
      for (const { m, b } of def.build(state)) {
        const world = b.verts.map(v => xf(mul(view, m), v));
        for (const f of b.faces) {
          const pts = f.map(i => world[i]);
          const u = { x: pts[1].x-pts[0].x, y: pts[1].y-pts[0].y, z: pts[1].z-pts[0].z };
          const w = { x: pts[3].x-pts[0].x, y: pts[3].y-pts[0].y, z: pts[3].z-pts[0].z };
          const n = { x: u.y*w.z - u.z*w.y, y: u.z*w.x - u.x*w.z, z: u.x*w.y - u.y*w.x };
          const len = Math.hypot(n.x, n.y, n.z) || 1;
          if (n.z / len < 0) continue;                       // back-face cull
          const light = 0.45 + 0.55 * Math.max(0, (n.x*0.4 + n.y*0.8 + n.z*0.45) / len);
          quads.push({
            pts, light, color: b.color,
            depth: pts.reduce((s, p) => s + p.z, 0) / 4,
          });
        }
      }
      quads.sort((a, b) => a.depth - b.depth);              // far first

      for (const q of quads) {
        const p = q.pts.map(project);
        ctx.beginPath();
        ctx.moveTo(p[0].X, p[0].Y);
        for (let i = 1; i < p.length; i++) ctx.lineTo(p[i].X, p[i].Y);
        ctx.closePath();
        ctx.fillStyle = shade(q.color, q.light);
        ctx.fill();
        ctx.strokeStyle = "rgba(0,0,0,0.28)";
        ctx.lineWidth = 0.6;
        ctx.stroke();
      }

      readoutEl.innerHTML = "";
      for (const [k, v] of def.readout(state)) {
        const row = document.createElement("div");
        row.className = "r3d-row";
        row.innerHTML = '<span>' + k + '</span><b>' + v + '</b>';
        readoutEl.appendChild(row);
      }
    }

    function shade(hex, k) {
      const n = parseInt(hex.slice(1), 16);
      const c = v => Math.max(0, Math.min(255, Math.round(v * k)));
      return "rgb(" + c((n>>16)&255) + "," + c((n>>8)&255) + "," + c(n&255) + ")";
    }

    // Flywheels need to actually turn, so animate only while they are spinning.
    let raf = null;
    function tick() {
      if (state.rpm > 0) { state._t += 0.05; draw(); raf = requestAnimationFrame(tick); }
      else { raf = null; }
    }
    const spinWatcher = new MutationObserver(() => {});
    void spinWatcher;
    root.addEventListener("input", () => { if (state.rpm > 0 && !raf) raf = requestAnimationFrame(tick); });
    root.addEventListener("click", () => { if (state.rpm > 0 && !raf) raf = requestAnimationFrame(tick); });

    window.addEventListener("resize", draw);
    draw();
    if (state.rpm > 0) raf = requestAnimationFrame(tick);
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
  // either, just watch for new nodes and wire up anything that appears.
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
