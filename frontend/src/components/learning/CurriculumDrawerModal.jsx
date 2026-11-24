import React, { useState, useMemo, useEffect } from 'react';
import { X, Search, CheckCircle2, Circle, ArrowRight, BookOpen } from 'lucide-react';
import { CURRICULUM_STAGES, ALL_40_LESSONS } from '../../data/curriculumData';

export default function CurriculumDrawerModal({
  isOpen,
  onClose,
  activeLessonNumber,
  completedLessons = [],
  onSelectLesson
}) {
  const [selectedStage, setSelectedStage] = useState(0); // 0 = all
  const [searchQuery, setSearchQuery] = useState('');

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

  const filteredLessons = useMemo(() => {
    return ALL_40_LESSONS.filter((lesson) => {
      const stageMatch = selectedStage === 0 || lesson.stageNumber === selectedStage;
      const q = searchQuery.trim().toLowerCase();
      if (!q) return stageMatch;
      const queryMatch =
        lesson.title.toLowerCase().includes(q) ||
        lesson.coreQuestion.toLowerCase().includes(q) ||
        lesson.stageTitle.toLowerCase().includes(q) ||
        lesson.primaryFormula.toLowerCase().includes(q) ||
        `lesson ${lesson.lessonNumber}`.includes(q);
      return stageMatch && queryMatch;
    });
  }, [selectedStage, searchQuery]);

  if (!isOpen) return null;

  const doneCount = completedLessons.length;
  const pct = Math.round((doneCount / 40) * 100);

  return (
    <div className="curriculum-drawer-backdrop" onClick={onClose}>
      <div className="curriculum-drawer-dialog" onClick={(e) => e.stopPropagation()}>
        {/* Header */}
        <div className="curriculum-drawer-header">
          <div>
            <div className="curriculum-drawer-sub">CURRICULUM DIRECTORY</div>
            <h2 className="curriculum-drawer-title">40 Lessons Across 8 Stages</h2>
          </div>
          <button className="curriculum-drawer-close" onClick={onClose} title="Close (Esc)">
            <X size={20} />
          </button>
        </div>

        {/* Progress Bar & Summary */}
        <div className="curriculum-drawer-progress-box">
          <div className="curriculum-drawer-progress-header">
            <span className="curriculum-drawer-progress-text">
              Progress: <strong>{doneCount}</strong> of 40 completed
            </span>
            <span className={`curriculum-drawer-badge ${doneCount > 0 ? 'gain' : 'muted'}`}>
              {pct}% Mastered
            </span>
          </div>
          <div className="curriculum-drawer-progress-track">
            <div
              className="curriculum-drawer-progress-fill"
              style={{ width: `${Math.max(2, pct)}%` }}
            />
          </div>
        </div>

        {/* Search Input */}
        <div className="curriculum-drawer-search">
          <Search size={16} className="curriculum-drawer-search-icon" />
          <input
            type="text"
            className="curriculum-drawer-search-input"
            placeholder="Search lessons by topic, concept, formula..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
          />
          {searchQuery && (
            <button
              className="curriculum-drawer-clear-btn"
              onClick={() => setSearchQuery('')}
            >
              ✕
            </button>
          )}
        </div>

        {/* Stage Filter Pills */}
        <div className="curriculum-drawer-stage-pills">
          <button
            className={`curriculum-stage-pill ${selectedStage === 0 ? 'active' : ''}`}
            onClick={() => setSelectedStage(0)}
          >
            All Stages
          </button>
          {CURRICULUM_STAGES.map((stg) => (
            <button
              key={stg.stageNumber}
              className={`curriculum-stage-pill ${selectedStage === stg.stageNumber ? 'active' : ''}`}
              onClick={() => setSelectedStage(stg.stageNumber)}
            >
              Stage {stg.stageNumber}
            </button>
          ))}
        </div>

        {/* Lessons List */}
        <div className="curriculum-drawer-list">
          {filteredLessons.length === 0 ? (
            <div className="curriculum-drawer-empty">
              <BookOpen size={32} style={{ color: 'var(--text-tertiary)', marginBottom: 8 }} />
              <div>No lessons matching "{searchQuery}"</div>
            </div>
          ) : (
            filteredLessons.map((lesson) => {
              const isCurrent = lesson.lessonNumber === activeLessonNumber;
              const isDone = completedLessons.includes(lesson.lessonNumber);

              return (
                <div
                  key={lesson.lessonNumber}
                  className={`curriculum-drawer-item ${isCurrent ? 'current' : ''}`}
                  onClick={() => {
                    onSelectLesson(lesson.lessonNumber);
                    onClose();
                  }}
                >
                  <div className="curriculum-drawer-item-left">
                    {isDone ? (
                      <CheckCircle2 size={18} className="curriculum-check-done" />
                    ) : (
                      <Circle size={18} className="curriculum-check-todo" />
                    )}
                    <div className="curriculum-drawer-item-text">
                      <div className="curriculum-drawer-item-meta">
                        <span className="curriculum-item-lesson-num">Lesson {lesson.lessonNumber}</span>
                        <span className="curriculum-item-dot">•</span>
                        <span className="curriculum-item-stage">Stage {lesson.stageNumber}</span>
                      </div>
                      <div className="curriculum-drawer-item-title">{lesson.title}</div>
                    </div>
                  </div>
                  <ArrowRight
                    size={16}
                    className={`curriculum-drawer-item-arrow ${isCurrent ? 'active' : ''}`}
                  />
                </div>
              );
            })
          )}
        </div>
      </div>
    </div>
  );
}
