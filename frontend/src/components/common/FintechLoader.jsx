import React, { useState, useEffect } from 'react';
import { Sparkles, Activity, ShieldCheck, Database, Cpu } from 'lucide-react';

const DEFAULT_STEPS = [
  'Executing 33 institutional quantitative pattern detectors...',
  'Auditing multi-horizon backtest win rates & forward returns...',
  'Calculating composite trend, volume & relative strength rankings...',
  'Verifying institutional order blocks and liquidity zones...'
];

export default function FintechLoader({
  title = 'Scanning Quantitative Setup Engine...',
  subtitle = 'Aggregating daily indicators, composite rankings, and backtest evidence',
  steps = DEFAULT_STEPS,
  size = 'large'
}) {
  const [stepIndex, setStepIndex] = useState(0);

  useEffect(() => {
    if (!steps || steps.length <= 1) return;
    const timer = setInterval(() => {
      setStepIndex((prev) => (prev + 1) % steps.length);
    }, 2000);
    return () => clearInterval(timer);
  }, [steps]);

  return (
    <div className={`fintech-loader-container ${size}`}>
      {/* Central Animated Quantum Radar */}
      <div className="fintech-radar-core">
        {/* Outermost Orbit Ring */}
        <div className="radar-ring ring-outer" />
        
        {/* Middle Pulse Ring */}
        <div className="radar-ring ring-middle" />
        
        {/* Inner Sweep Ring with Spinning Beam */}
        <div className="radar-ring ring-inner">
          <div className="radar-sweep-beam" />
        </div>

        {/* Orbiting Quantum Particles */}
        <div className="radar-particle particle-1" />
        <div className="radar-particle particle-2" />
        <div className="radar-particle particle-3" />

        {/* Central Glowing Gem */}
        <div className="radar-center-gem">
          <Cpu className="gem-icon" />
        </div>
      </div>

      {/* Live Status Pill */}
      <div className="fintech-loader-status-pill font-mono">
        <span className="live-pulse-dot" />
        <span>AI QUANT RADAR ACTIVE</span>
      </div>

      {/* Title & Subtitle */}
      <div className="fintech-loader-content">
        <h3 className="fintech-loader-title">{title}</h3>
        <p className="fintech-loader-subtitle">{subtitle}</p>
      </div>

      {/* Dynamic Telemetry Step Banner */}
      {steps && steps.length > 0 && (
        <div className="fintech-loader-step-track font-mono">
          <Activity size={12} className="step-activity-icon" />
          <span className="step-active-text">{steps[stepIndex]}</span>
        </div>
      )}

      {/* Slim Shimmer Progress Line */}
      <div className="fintech-loader-shimmer-bar">
        <div className="shimmer-runner" />
      </div>
    </div>
  );
}
