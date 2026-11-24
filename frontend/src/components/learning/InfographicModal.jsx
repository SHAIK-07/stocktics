import React, { useEffect, useState } from 'react';
import { X, ZoomIn, ZoomOut, Maximize2, Download } from 'lucide-react';

export default function InfographicModal({ isOpen, onClose, imageSrc, title, lessonNumber }) {
  const [scale, setScale] = useState(1);

  useEffect(() => {
    const handleKeyDown = (e) => {
      if (e.key === 'Escape') onClose();
    };
    if (isOpen) {
      window.addEventListener('keydown', handleKeyDown);
      document.body.style.overflow = 'hidden';
    }
    return () => {
      window.removeEventListener('keydown', handleKeyDown);
      document.body.style.overflow = '';
    };
  }, [isOpen, onClose]);

  if (!isOpen || !imageSrc) return null;

  return (
    <div className="infographic-modal-backdrop" onClick={onClose}>
      <div className="infographic-modal-dialog" onClick={(e) => e.stopPropagation()}>
        {/* Top Control Bar */}
        <div className="infographic-modal-header">
          <div className="infographic-modal-titles">
            <span className="infographic-modal-badge">LESSON {lessonNumber} CHEAT SHEET</span>
            <h3 className="infographic-modal-title">{title || 'Quantitative Process Infographic'}</h3>
          </div>
          <div className="infographic-modal-actions">
            <button
              className="infographic-modal-btn"
              onClick={() => setScale((s) => Math.max(0.6, s - 0.2))}
              title="Zoom Out"
            >
              <ZoomOut size={16} />
            </button>
            <span className="infographic-modal-zoom-val">{Math.round(scale * 100)}%</span>
            <button
              className="infographic-modal-btn"
              onClick={() => setScale((s) => Math.min(2.5, s + 0.2))}
              title="Zoom In"
            >
              <ZoomIn size={16} />
            </button>
            <button
              className="infographic-modal-btn"
              onClick={() => setScale(1)}
              title="Reset Zoom"
            >
              <Maximize2 size={16} />
            </button>
            <a
              href={imageSrc}
              download={`stocktics_lesson_${lessonNumber}_infographic.jpg`}
              className="infographic-modal-btn"
              title="Download Infographic"
            >
              <Download size={16} />
            </a>
            <button
              className="infographic-modal-close-btn"
              onClick={onClose}
              title="Close (Esc)"
            >
              <X size={18} />
            </button>
          </div>
        </div>

        {/* Image Display Area */}
        <div className="infographic-modal-canvas">
          <img
            src={imageSrc}
            alt={`Lesson ${lessonNumber} Infographic`}
            style={{
              transform: `scale(${scale})`,
              transformOrigin: 'top center',
              transition: 'transform 0.15s ease-out'
            }}
            className="infographic-modal-image"
          />
        </div>
      </div>
    </div>
  );
}
