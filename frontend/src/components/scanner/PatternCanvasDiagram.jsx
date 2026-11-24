import React, { useRef, useEffect } from 'react';

/**
 * Institutional Technical Analysis Canvas Engine
 * Renders 27+ bespoke, mathematically accurate technical pattern diagrams
 * modeled after institutional fintech charting standards (Groww / INDmoney / CMT).
 */

const C = {
  green: '#00D09C',
  red: '#F04438',
  cyan: '#27d5ff',
  yellow: '#ffc83d',
  purple: '#a875ff',
  blue: '#1455FE',
  white: '#F8FAFC',
  muted: '#94A3B8',
  grid: 'rgba(34, 42, 59, 0.22)',
  dark: '#0B0E14'
};

function setupCanvas(canvas) {
  const rect = canvas.getBoundingClientRect();
  const dpr = window.devicePixelRatio || 1;
  const w = rect.width || 240;
  const h = rect.height || 105;
  canvas.width = Math.round(w * dpr);
  canvas.height = Math.round(h * dpr);
  const ctx = canvas.getContext('2d');
  ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
  return [ctx, w, h];
}

function drawGrid(ctx, w, h) {
  ctx.strokeStyle = C.grid;
  ctx.lineWidth = 1;
  for (let i = 0; i < w; i += 26) {
    ctx.beginPath();
    ctx.moveTo(i, 0);
    ctx.lineTo(i, h);
    ctx.stroke();
  }
  for (let i = 0; i < h; i += 22) {
    ctx.beginPath();
    ctx.moveTo(0, i);
    ctx.lineTo(w, i);
    ctx.stroke();
  }
}

function drawText(ctx, text, x, y, col = C.white, size = 9.5, bold = false) {
  ctx.fillStyle = col;
  ctx.font = `${bold ? '800' : '600'} ${size}px Inter, Segoe UI, system-ui, sans-serif`;
  ctx.fillText(text, x, y);
}

function drawLine(ctx, pts, col = C.green, lw = 2, dash = []) {
  ctx.strokeStyle = col;
  ctx.lineWidth = lw;
  ctx.setLineDash(dash);
  ctx.beginPath();
  pts.forEach((q, i) => (i ? ctx.lineTo(q[0], q[1]) : ctx.moveTo(q[0], q[1])));
  ctx.stroke();
  ctx.setLineDash([]);
}

function drawHorizLine(ctx, y, label, col = C.red, w = 240) {
  drawLine(ctx, [[18, y], [w - 18, y]], col, 1.5, [6, 4]);
  drawText(ctx, label, 22, y - 4, col, 9, true);
}

function drawCandle(ctx, c, bull = true, wide = 9) {
  const col = bull ? C.green : C.red;
  ctx.strokeStyle = col;
  ctx.fillStyle = col;
  ctx.lineWidth = 1.4;
  ctx.beginPath();
  ctx.moveTo(c.x, c.h);
  ctx.lineTo(c.x, c.l);
  ctx.stroke();
  const top = Math.min(c.o, c.c);
  const height = Math.max(3, Math.abs(c.c - c.o));
  ctx.fillRect(c.x - wide / 2, top, wide, height);
}

function drawArrow(ctx, a, b, col = C.green) {
  drawLine(ctx, [a, b], col, 2);
  const ang = Math.atan2(b[1] - a[1], b[0] - a[0]);
  ctx.fillStyle = col;
  ctx.beginPath();
  ctx.moveTo(b[0], b[1]);
  ctx.lineTo(b[0] - 7 * Math.cos(ang - 0.5), b[1] - 7 * Math.sin(ang - 0.5));
  ctx.lineTo(b[0] - 7 * Math.cos(ang + 0.5), b[1] - 7 * Math.sin(ang + 0.5));
  ctx.fill();
}

/**
 * Core Canvas Drawing Dispatcher
 */
export function drawPatternDiagram(diagramId, canvas) {
  if (!canvas) return;
  const [ctx, w, h] = setupCanvas(canvas);
  drawGrid(ctx, w, h);

  const Y = (v) => (v * h) / 260;
  const X = (v) => (v * w) / 520;
  const id = (diagramId || 'breakout').toLowerCase();

  switch (id) {
    case 'fib':
      drawLine(ctx, [[X(55), Y(215)], [X(215), Y(45)]], C.green, 3.5);
      drawLine(ctx, [[X(215), Y(45)], [X(430), Y(175)]], C.blue, 2.5);
      [0.236, 0.382, 0.5, 0.618, 0.786].forEach((r, i) => {
        const y = Y(45 + (215 - 45) * r);
        const col = [C.muted, C.cyan, C.yellow, C.green, C.red][i];
        drawLine(ctx, [[X(35), y], [X(485), y]], col, 1.2, [5, 4]);
        drawText(ctx, `${(r * 100).toFixed(1)}%`, X(438), y - 3, col, 8.5, true);
      });
      drawText(ctx, 'SWING HIGH', X(210), Y(34), C.green, 9, true);
      drawText(ctx, 'SWING LOW', X(45), Y(235), C.blue, 9, true);
      break;

    case 'breakout':
    case 'pivot_breakout':
      drawHorizLine(ctx, Y(82), 'Resistance R(t)', C.red, w);
      drawLine(
        ctx,
        [
          [X(40), Y(205)],
          [X(105), Y(145)],
          [X(165), Y(175)],
          [X(225), Y(112)],
          [X(290), Y(145)],
          [X(350), Y(90)],
          [X(430), Y(42)]
        ],
        C.green,
        3.5
      );
      drawArrow(ctx, [X(430), Y(55)], [X(430), Y(20)], C.green);
      drawText(ctx, 'BREAKOUT ↗', X(350), Y(34), C.green, 11, true);
      [25, 38, 20, 48, 65, 110].forEach((v, i) => {
        ctx.fillStyle = i > 3 ? C.green : '#4d7f91';
        ctx.fillRect(X(280 + i * 32), Y(238 - v / 2), X(16), Y(v / 2));
      });
      drawText(ctx, '+ Vol Spike', X(405), Y(225), C.cyan, 8.5, true);
      break;

    case 'failed_breakout':
      drawHorizLine(ctx, Y(110), 'Breakout Pivot (Bull Trap)', C.red, w);
      drawLine(
        ctx,
        [
          [X(50), Y(200)],
          [X(140), Y(120)],
          [X(220), Y(160)],
          [X(320), Y(75)],
          [X(380), Y(65)],
          [X(440), Y(210)]
        ],
        C.red,
        3
      );
      drawText(ctx, 'FAKE OUT ❌', X(300), Y(52), C.yellow, 10, true);
      drawArrow(ctx, [X(410), Y(140)], [X(445), Y(220)], C.red);
      break;

    case 'reversal':
    case 'double_bottom':
      drawLine(
        ctx,
        [
          [X(35), Y(62)],
          [X(105), Y(205)],
          [X(175), Y(105)],
          [X(250), Y(205)],
          [X(325), Y(105)],
          [X(405), Y(55)]
        ],
        C.green,
        3.5
      );
      drawHorizLine(ctx, Y(105), 'Neckline Pivot', C.cyan, w);
      drawText(ctx, '1st Low (L1)', X(85), Y(224), C.red, 9, true);
      drawText(ctx, '2nd Low (L2)', X(230), Y(224), C.green, 9, true);
      drawArrow(ctx, [X(405), Y(65)], [X(455), Y(30)], C.green);
      drawText(ctx, 'W-Base Breakout ↗', X(330), Y(38), C.green, 9.5, true);
      break;

    case 'double_top':
      drawHorizLine(ctx, Y(68), 'Resistance Ceiling', C.red, w);
      drawLine(
        ctx,
        [
          [X(40), Y(210)],
          [X(120), Y(68)],
          [X(200), Y(155)],
          [X(280), Y(72)],
          [X(360), Y(155)],
          [X(430), Y(225)]
        ],
        C.red,
        3.5
      );
      drawHorizLine(ctx, Y(155), 'Neckline Support', C.yellow, w);
      drawText(ctx, 'Top 1', X(105), Y(55), C.red, 9, true);
      drawText(ctx, 'Top 2', X(265), Y(58), C.red, 9, true);
      drawArrow(ctx, [X(390), Y(180)], [X(435), Y(235)], C.red);
      drawText(ctx, 'Breakdown ↘', X(340), Y(245), C.red, 9.5, true);
      break;

    case 'cup_handle':
      drawHorizLine(ctx, Y(65), 'Pivot Resistance', C.red, w);
      ctx.strokeStyle = C.green;
      ctx.lineWidth = 3;
      ctx.beginPath();
      // Cup U-shape
      ctx.moveTo(X(50), Y(68));
      ctx.bezierCurveTo(X(80), Y(225), X(240), Y(225), X(270), Y(68));
      // Handle
      ctx.bezierCurveTo(X(305), Y(115), X(340), Y(110), X(370), Y(68));
      ctx.stroke();
      drawArrow(ctx, [X(370), Y(68)], [X(435), Y(32)], C.green);
      drawText(ctx, 'U-Base Cup', X(135), Y(185), C.muted, 9, true);
      drawText(ctx, 'Handle', X(310), Y(125), C.yellow, 9, true);
      drawText(ctx, 'Buy Point ↗', X(380), Y(40), C.green, 9.5, true);
      break;

    case 'vcp':
      drawHorizLine(ctx, Y(60), 'Resistance Ceiling', C.red, w);
      drawLine(
        ctx,
        [
          [X(45), Y(62)],
          [X(110), Y(215)],
          [X(175), Y(62)],
          [X(230), Y(165)],
          [X(285), Y(62)],
          [X(330), Y(120)],
          [X(375), Y(62)],
          [X(435), Y(30)]
        ],
        C.green,
        3
      );
      drawArrow(ctx, [X(375), Y(62)], [X(435), Y(28)], C.green);
      drawText(ctx, '-25%', X(95), Y(230), C.muted, 8.5, true);
      drawText(ctx, '-12%', X(215), Y(180), C.muted, 8.5, true);
      drawText(ctx, '-4%', X(320), Y(135), C.muted, 8.5, true);
      drawText(ctx, 'Apex ↗', X(395), Y(24), C.green, 10, true);
      break;

    case 'flat_base':
    case 'structure':
    case 'multi_year':
    case 'ipo_base':
      drawHorizLine(ctx, Y(70), 'Upper Consolidation Ceiling', C.red, w);
      drawHorizLine(ctx, Y(185), 'Support Base Floor', C.blue, w);
      drawLine(
        ctx,
        [
          [X(45), Y(180)],
          [X(100), Y(75)],
          [X(155), Y(175)],
          [X(210), Y(80)],
          [X(265), Y(170)],
          [X(320), Y(75)],
          [X(375), Y(140)],
          [X(430), Y(50)]
        ],
        C.green,
        3
      );
      drawArrow(ctx, [X(375), Y(140)], [X(435), Y(42)], C.green);
      drawText(ctx, 'Decisive Base Expansion ↗', X(260), Y(38), C.green, 9.5, true);
      break;

    case 'elliott': {
      const ep = [
        [45, 215],
        [125, 135],
        [170, 175],
        [270, 48],
        [330, 125],
        [435, 28]
      ].map((p) => [X(p[0]), Y(p[1])]);
      drawLine(ctx, ep, C.cyan, 3);
      ep.forEach((p, i) => drawText(ctx, i ? `(${i})` : '0', p[0] - 5, p[1] - 8, C.yellow, 11, true));
      drawText(ctx, 'Impulse Wave 3 ↗', X(200), Y(75), C.green, 9, true);
      break;
    }

    case 'fvg':
      drawCandle(ctx, { x: X(105), o: Y(190), c: Y(145), h: Y(125), l: Y(205) }, true, 14);
      drawCandle(ctx, { x: X(220), o: Y(145), c: Y(62), h: Y(48), l: Y(160) }, true, 16);
      drawCandle(ctx, { x: X(335), o: Y(64), c: Y(45), h: Y(30), l: Y(88) }, true, 14);
      ctx.fillStyle = 'rgba(39, 213, 255, 0.22)';
      ctx.fillRect(X(114), Y(88), X(260), Y(37));
      ctx.strokeStyle = C.cyan;
      ctx.setLineDash([5, 4]);
      ctx.strokeRect(X(114), Y(88), X(260), Y(37));
      ctx.setLineDash([]);
      drawText(ctx, 'FVG / IMBALANCE VOID', X(260), Y(80), C.cyan, 9.5, true);
      break;

    case 'bullish_engulfing':
      drawCandle(ctx, { x: X(160), o: Y(100), c: Y(160), h: Y(85), l: Y(175) }, false, 14);
      drawCandle(ctx, { x: X(260), o: Y(170), c: Y(75), h: Y(55), l: Y(185) }, true, 22);
      drawArrow(ctx, [X(320), Y(120)], [X(380), Y(65)], C.green);
      drawText(ctx, 'Prior Bear', X(135), Y(195), C.red, 8.5, true);
      drawText(ctx, 'Bull Engulfing', X(230), Y(45), C.green, 9.5, true);
      break;

    case 'bearish_engulfing':
      drawCandle(ctx, { x: X(160), o: Y(160), c: Y(100), h: Y(85), l: Y(175) }, true, 14);
      drawCandle(ctx, { x: X(260), o: Y(85), c: Y(180), h: Y(70), l: Y(200) }, false, 22);
      drawArrow(ctx, [X(320), Y(130)], [X(380), Y(190)], C.red);
      drawText(ctx, 'Prior Bull', X(135), Y(75), C.green, 8.5, true);
      drawText(ctx, 'Bear Engulfing', X(230), Y(215), C.red, 9.5, true);
      break;

    case 'hammer':
    case 'candles':
      drawHorizLine(ctx, Y(200), 'Intraday Support Level', C.blue, w);
      drawCandle(ctx, { x: X(220), o: Y(105), c: Y(85), h: Y(80), l: Y(205) }, true, 18);
      drawArrow(ctx, [X(270), Y(110)], [X(340), Y(50)], C.green);
      drawText(ctx, 'Hammer', X(195), Y(70), C.green, 10, true);
      drawText(ctx, '2x Long Lower Shadow (Rejection)', X(120), Y(230), C.cyan, 8.5, true);
      break;

    case 'shooting_star':
      drawHorizLine(ctx, Y(70), 'Overhead Resistance Shelf', C.red, w);
      drawCandle(ctx, { x: X(220), o: Y(160), c: Y(180), h: Y(65), l: Y(185) }, false, 18);
      drawArrow(ctx, [X(270), Y(160)], [X(340), Y(220)], C.red);
      drawText(ctx, 'Shooting Star', X(180), Y(50), C.red, 10, true);
      drawText(ctx, '2x Upper Wick Supply Exhaustion', X(120), Y(230), C.yellow, 8.5, true);
      break;

    case 'morning_star':
      drawCandle(ctx, { x: X(130), o: Y(80), c: Y(160), h: Y(65), l: Y(175) }, false, 14);
      drawCandle(ctx, { x: X(220), o: Y(190), c: Y(192), h: Y(178), l: Y(208) }, true, 10);
      drawCandle(ctx, { x: X(310), o: Y(175), c: Y(95), h: Y(80), l: Y(190) }, true, 14);
      drawArrow(ctx, [X(350), Y(110)], [X(410), Y(55)], C.green);
      drawText(ctx, 'Morning Star 3-Candle Reversal ↗', X(130), Y(40), C.green, 9.5, true);
      break;

    case 'evening_star':
      drawCandle(ctx, { x: X(130), o: Y(170), c: Y(90), h: Y(75), l: Y(185) }, true, 14);
      drawCandle(ctx, { x: X(220), o: Y(60), c: Y(62), h: Y(48), l: Y(78) }, false, 10);
      drawCandle(ctx, { x: X(310), o: Y(75), c: Y(155), h: Y(60), l: Y(170) }, false, 14);
      drawArrow(ctx, [X(350), Y(130)], [X(410), Y(195)], C.red);
      drawText(ctx, 'Evening Star 3-Candle Top ↘', X(140), Y(235), C.red, 9.5, true);
      break;

    case 'doji':
      drawCandle(ctx, { x: X(220), o: Y(130), c: Y(130), h: Y(70), l: Y(190) }, true, 16);
      drawText(ctx, 'Doji Equilibrium', X(175), Y(55), C.white, 10, true);
      drawLine(ctx, [[X(120), Y(130)], [X(320), Y(130)]], C.yellow, 1, [4, 4]);
      drawText(ctx, 'Open = Close (Inflection)', X(150), Y(220), C.muted, 8.5, true);
      break;

    case 'heikin':
    case 'heikin_ashi':
      for (let i = 0; i < 9; i++) {
        const xx = 45 + i * 48;
        drawCandle(
          ctx,
          {
            x: X(xx),
            o: Y(190 - i * 13),
            c: Y(162 - i * 14),
            h: Y(145 - i * 14),
            l: Y(204 - i * 12)
          },
          true,
          15
        );
      }
      drawText(ctx, 'FLAT-BOTTOM SMOOTHED TREND', X(45), Y(40), C.cyan, 10, true);
      break;

    case 'moon':
      [65, 160, 260, 360, 455].forEach((xx, i) => {
        ctx.fillStyle = i === 2 ? '#F8FAFC' : '#7892a5';
        ctx.beginPath();
        ctx.arc(X(xx), Y(125), Y(25), 0, Math.PI * 2);
        ctx.fill();
        if (i !== 2) {
          ctx.fillStyle = '#071827';
          ctx.beginPath();
          ctx.arc(X(xx + (i < 2 ? 10 : -10)), Y(116), Y(25), 0, Math.PI * 2);
          ctx.fill();
        }
      });
      drawText(ctx, 'NEW MOON (TROUGH)', X(45), Y(180), C.muted, 8.5, true);
      drawText(ctx, 'FULL MOON (CREST)', X(225), Y(180), C.white, 8.5, true);
      break;

    case 'renko': {
      const ry = [0, 0, 0, 0, 0, 1, 1, 1, 1];
      ry.forEach((_, i) => {
        const up = i < 5;
        const xx = 35 + i * 48;
        const yy = 200 - (i < 5 ? i * 24 : (5 + (i - 5)) * -1 * 24);
        ctx.fillStyle = up ? C.green : C.red;
        ctx.fillRect(X(xx), Y(yy), X(38), Y(24));
        ctx.strokeStyle = '#071827';
        ctx.strokeRect(X(xx), Y(yy), X(38), Y(24));
      });
      drawText(ctx, 'NOISELESS PRICE BRICKS', X(45), Y(40), C.cyan, 10, true);
      break;
    }

    case 'harmonic': {
      const hp = [
        [45, 70],
        [150, 215],
        [245, 105],
        [335, 185],
        [445, 45]
      ].map((p) => [X(p[0]), Y(p[1])]);
      drawLine(ctx, hp, C.blue, 3);
      ['X', 'A', 'B', 'C', 'D'].forEach((s, i) =>
        drawText(ctx, s, hp[i][0] - 5, hp[i][1] - 8, C.yellow, 11, true)
      );
      drawText(ctx, 'PRZ Reversal Point D ↗', X(330), Y(35), C.green, 9, true);
      break;
    }

    case 'sr':
    case 'sr_touch':
      drawHorizLine(ctx, Y(75), 'Resistance Ceiling', C.red, w);
      drawHorizLine(ctx, Y(205), 'Support Baseline', C.blue, w);
      drawLine(
        ctx,
        [
          [X(35), Y(155)],
          [X(85), Y(115)],
          [X(135), Y(170)],
          [X(190), Y(95)],
          [X(245), Y(155)],
          [X(300), Y(185)],
          [X(350), Y(125)],
          [X(405), Y(155)],
          [X(475), Y(90)]
        ],
        C.green,
        3
      );
      break;

    case 'dynamic':
      drawLine(
        ctx,
        [
          [X(35), Y(205)],
          [X(95), Y(175)],
          [X(155), Y(185)],
          [X(215), Y(140)],
          [X(275), Y(150)],
          [X(335), Y(100)],
          [X(395), Y(110)],
          [X(470), Y(55)]
        ],
        C.green,
        3
      );
      drawLine(
        ctx,
        [
          [X(35), Y(215)],
          [X(95), Y(195)],
          [X(155), Y(175)],
          [X(215), Y(155)],
          [X(275), Y(135)],
          [X(335), Y(115)],
          [X(395), Y(95)],
          [X(470), Y(75)]
        ],
        C.yellow,
        2.5
      );
      drawText(ctx, 'DYNAMIC EMA / VWAP SUPPORT', X(260), Y(95), C.yellow, 9, true);
      break;

    case 'trend':
    case 'trendline':
      drawLine(
        ctx,
        [
          [X(35), Y(205)],
          [X(105), Y(165)],
          [X(175), Y(180)],
          [X(245), Y(130)],
          [X(320), Y(145)],
          [X(405), Y(75)],
          [X(470), Y(95)]
        ],
        C.green,
        3
      );
      drawLine(ctx, [[X(35), Y(220)], [X(470), Y(90)]], C.cyan, 2, [5, 4]);
      drawText(ctx, 'ASCENDING TRENDLINE', X(310), Y(82), C.cyan, 9.5, true);
      break;

    case 'gann':
      [[1, 1], [2, 1], [1, 2], [4, 1], [1, 4]].forEach((v, i) => {
        const ex = 90 + i * 75;
        const ey = 205 - (v[0] / v[1]) * 25;
        drawLine(
          ctx,
          [[X(55), Y(205)], [X(ex), Y(ey)]],
          [C.green, C.blue, C.yellow, C.purple, C.cyan][i],
          2
        );
        drawText(ctx, `${v[0]}x${v[1]}`, X(ex - 15), Y(ey - 8), C.white, 8.5, true);
      });
      break;

    case 'momentum':
      drawLine(
        ctx,
        [
          [X(35), Y(155)],
          [X(85), Y(75)],
          [X(135), Y(135)],
          [X(185), Y(55)],
          [X(235), Y(115)],
          [X(285), Y(92)],
          [X(335), Y(190)],
          [X(385), Y(110)],
          [X(435), Y(145)],
          [X(485), Y(70)]
        ],
        C.blue,
        3
      );
      drawLine(ctx, [[X(25), Y(145)], [X(495), Y(145)]], C.muted, 1, [5, 5]);
      drawText(ctx, 'RATE OF CHANGE (ROC) MOMENTUM', X(45), Y(40), C.cyan, 10, true);
      break;

    case 'ma':
    case 'ma_cross':
      drawLine(
        ctx,
        [
          [X(35), Y(200)],
          [X(90), Y(180)],
          [X(145), Y(188)],
          [X(200), Y(145)],
          [X(255), Y(155)],
          [X(310), Y(108)],
          [X(365), Y(120)],
          [X(430), Y(65)]
        ],
        C.green,
        2.5
      );
      drawLine(
        ctx,
        [
          [X(35), Y(215)],
          [X(90), Y(195)],
          [X(145), Y(182)],
          [X(200), Y(165)],
          [X(255), Y(150)],
          [X(310), Y(128)],
          [X(365), Y(108)],
          [X(430), Y(85)]
        ],
        C.cyan,
        3
      );
      drawText(ctx, 'Fast MA (20 EMA)', X(350), Y(55), C.green, 9, true);
      drawText(ctx, 'Slow MA (50 SMA)', X(350), Y(95), C.cyan, 9, true);
      break;

    case 'supertrend':
      drawLine(
        ctx,
        [
          [X(35), Y(205)],
          [X(90), Y(160)],
          [X(145), Y(175)],
          [X(200), Y(120)],
          [X(255), Y(135)],
          [X(310), Y(80)],
          [X(365), Y(105)],
          [X(430), Y(55)]
        ],
        C.green,
        3
      );
      drawLine(ctx, [[X(250), Y(145)], [X(480), Y(145)]], C.red, 2.5);
      drawText(ctx, 'BUY FLIP ↗', X(300), Y(72), C.green, 10, true);
      drawText(ctx, 'ATR STOP', X(410), Y(135), C.red, 9, true);
      break;

    case 'rsi':
      drawLine(
        ctx,
        [
          [X(35), Y(125)],
          [X(75), Y(85)],
          [X(115), Y(145)],
          [X(155), Y(105)],
          [X(195), Y(175)],
          [X(235), Y(120)],
          [X(275), Y(78)],
          [X(315), Y(135)],
          [X(355), Y(98)],
          [X(395), Y(160)],
          [X(475), Y(120)]
        ],
        C.purple,
        3
      );
      drawHorizLine(ctx, Y(62), '70 Overbought Threshold', C.red, w);
      drawHorizLine(ctx, Y(205), '30 Oversold Rebound Shelf', C.green, w);
      drawText(ctx, 'RSI (14) MOMENTUM', X(45), Y(40), C.purple, 10, true);
      break;

    case 'stoch':
      drawLine(
        ctx,
        [
          [X(35), Y(175)],
          [X(85), Y(85)],
          [X(135), Y(180)],
          [X(185), Y(80)],
          [X(235), Y(165)],
          [X(285), Y(105)],
          [X(335), Y(180)],
          [X(385), Y(95)],
          [X(435), Y(160)],
          [X(485), Y(115)]
        ],
        C.yellow,
        2.5
      );
      drawLine(
        ctx,
        [
          [X(35), Y(190)],
          [X(85), Y(115)],
          [X(135), Y(165)],
          [X(185), Y(110)],
          [X(235), Y(150)],
          [X(285), Y(120)],
          [X(335), Y(160)],
          [X(385), Y(115)],
          [X(435), Y(145)],
          [X(485), Y(125)]
        ],
        C.blue,
        2
      );
      drawHorizLine(ctx, Y(62), '80 %K Upper Threshold', C.red, w);
      drawHorizLine(ctx, Y(205), '20 %D Oversold Bull Cross', C.green, w);
      break;

    case 'divergence':
      drawLine(
        ctx,
        [
          [X(35), Y(75)],
          [X(110), Y(140)],
          [X(180), Y(95)],
          [X(260), Y(165)]
        ],
        C.red,
        3
      );
      drawLine(
        ctx,
        [
          [X(35), Y(205)],
          [X(110), Y(225)],
          [X(180), Y(202)],
          [X(260), Y(215)]
        ],
        C.green,
        3
      );
      drawText(ctx, 'PRICE: LOWER LOW ↘', X(45), Y(55), C.red, 9.5, true);
      drawText(ctx, 'RSI: HIGHER LOW ↗ (BULL DIV)', X(45), Y(245), C.green, 9.5, true);
      break;

    case 'volume':
      [45, 72, 38, 90, 58, 112, 160, 65, 95, 45, 125].forEach((v, i) => {
        ctx.fillStyle = i === 6 ? C.green : i % 3 === 0 ? C.blue : '#4b8a9e';
        ctx.fillRect(X(30 + i * 43), Y(235 - v / 2), X(26), Y(v / 2));
      });
      drawText(ctx, 'INSTITUTIONAL VOLUME SPIKE (>2.5x)', X(150), Y(50), C.green, 10, true);
      break;

    case 'vwap':
      drawLine(
        ctx,
        [
          [X(35), Y(205)],
          [X(90), Y(180)],
          [X(145), Y(155)],
          [X(200), Y(170)],
          [X(255), Y(130)],
          [X(310), Y(120)],
          [X(365), Y(95)],
          [X(430), Y(80)]
        ],
        C.green,
        3
      );
      drawLine(
        ctx,
        [
          [X(35), Y(190)],
          [X(90), Y(185)],
          [X(145), Y(170)],
          [X(200), Y(160)],
          [X(255), Y(145)],
          [X(310), Y(125)],
          [X(365), Y(110)],
          [X(430), Y(90)]
        ],
        C.cyan,
        3
      );
      drawText(ctx, 'INTRADAY VWAP BENCHMARK', X(260), Y(95), C.cyan, 9.5, true);
      break;

    case 'profile':
      [35, 55, 90, 120, 85, 55, 30].forEach((v, i) => {
        ctx.fillStyle = i === 3 ? C.red : i < 4 ? C.green : '#347a91';
        ctx.fillRect(X(28), Y(48 + i * 28), X(v), Y(16));
      });
      drawLine(
        ctx,
        [
          [X(205), Y(220)],
          [X(260), Y(175)],
          [X(320), Y(185)],
          [X(380), Y(120)],
          [X(465), Y(145)]
        ],
        C.green,
        3
      );
      drawText(ctx, 'POINT OF CONTROL (POC)', X(30), Y(40), C.red, 9.5, true);
      break;

    case 'supply':
    case 'supply_demand':
      ctx.fillStyle = 'rgba(255, 77, 90, 0.22)';
      ctx.fillRect(X(255), Y(42), X(235), Y(48));
      ctx.fillStyle = 'rgba(32, 212, 123, 0.22)';
      ctx.fillRect(X(255), Y(188), X(235), Y(48));
      drawText(ctx, 'SUPPLY (SELL LIQUIDITY)', X(275), Y(72), C.red, 9.5, true);
      drawText(ctx, 'DEMAND (BUY LIQUIDITY)', X(275), Y(218), C.green, 9.5, true);
      drawLine(
        ctx,
        [
          [X(35), Y(92)],
          [X(90), Y(150)],
          [X(145), Y(110)],
          [X(200), Y(205)],
          [X(275), Y(175)],
          [X(335), Y(205)],
          [X(400), Y(185)],
          [X(475), Y(215)]
        ],
        C.green,
        3
      );
      break;

    case 'bos':
      drawLine(
        ctx,
        [
          [X(35), Y(205)],
          [X(100), Y(150)],
          [X(160), Y(180)],
          [X(220), Y(120)],
          [X(280), Y(145)],
          [X(340), Y(75)],
          [X(405), Y(100)],
          [X(475), Y(35)]
        ],
        C.green,
        3
      );
      drawHorizLine(ctx, Y(75), 'BOS (Break of Structure)', C.green, w);
      drawArrow(ctx, [X(475), Y(40)], [X(475), Y(18)], C.green);
      break;

    case 'choch':
      drawLine(
        ctx,
        [
          [X(35), Y(70)],
          [X(105), Y(115)],
          [X(170), Y(80)],
          [X(235), Y(135)],
          [X(295), Y(105)],
          [X(355), Y(175)],
          [X(415), Y(145)],
          [X(480), Y(220)]
        ],
        C.red,
        3
      );
      drawHorizLine(ctx, Y(135), 'CHoCH (Change of Character)', C.red, w);
      drawText(ctx, 'TREND REGIME SHIFT ↘', X(280), Y(112), C.red, 9.5, true);
      break;

    default:
      drawHorizLine(ctx, Y(80), 'Pivot Resistance', C.red, w);
      drawLine(
        ctx,
        [
          [X(40), Y(205)],
          [X(120), Y(120)],
          [X(200), Y(160)],
          [X(300), Y(80)],
          [X(420), Y(40)]
        ],
        C.green,
        3.5
      );
      drawArrow(ctx, [X(400), Y(50)], [X(445), Y(28)], C.green);
      drawText(ctx, 'Algorithmic Setup Trigger ↗', X(240), Y(35), C.green, 9.5, true);
      break;
  }
}

/**
 * High-performance React Canvas wrapper with automatic device pixel scaling
 * and resize observation.
 */
export default function PatternCanvasDiagram({ diagramId }) {
  const canvasRef = useRef(null);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;

    let animFrame = null;
    const render = () => {
      drawPatternDiagram(diagramId, canvas);
    };

    render();

    const ro = new ResizeObserver(() => {
      cancelAnimationFrame(animFrame);
      animFrame = requestAnimationFrame(render);
    });
    ro.observe(canvas);

    return () => {
      ro.disconnect();
      cancelAnimationFrame(animFrame);
    };
  }, [diagramId]);

  return (
    <canvas
      ref={canvasRef}
      className="theory-canvas"
      style={{
        width: '100%',
        height: '100%',
        display: 'block'
      }}
    />
  );
}
