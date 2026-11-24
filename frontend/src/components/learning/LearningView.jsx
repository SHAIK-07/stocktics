import React, { useState, useEffect, useMemo, useRef } from 'react';
import {
  BookOpen, CheckCircle2, Circle, ArrowLeft, ArrowRight, Menu,
  Search, Maximize2, ShieldCheck, AlertOctagon, AlertTriangle, HelpCircle,
  TrendingUp, Award, Bookmark, RefreshCw, Layers, Compass, ChevronDown
} from 'lucide-react';
import { CURRICULUM_STAGES, ALL_40_LESSONS, getLessonFormulas, getLessonVisualFlow } from '../../data/curriculumData';
import { learningService } from '../../services/supabase';
import InfographicModal from './InfographicModal';
import CurriculumDrawerModal from './CurriculumDrawerModal';
import LiveLessonSandbox from './LiveLessonSandbox';

function FormattedNotes({ text }) {
  if (!text) return null;
  const paragraphs = text.split('\n\n').map(p => p.trim()).filter(Boolean);

  return (
    <div className="learning-notes-content">
      {paragraphs.map((p, pIdx) => {
        const numberedItems = [];
        const regex = /(?:^|\s)(\d+)\.\s+([^:\n]+?):\s+([\s\S]*?)(?=(?:\s+\d+\.|\Z))/g;
        let match;
        while ((match = regex.exec(p)) !== null) {
          numberedItems.push({
            num: match[1],
            title: match[2].trim(),
            desc: match[3].trim()
          });
        }

        if (numberedItems.length >= 2) {
          const colors = ['#1455FE', '#00D09C', '#F59E0B', '#8B5CF6', '#06B6D4'];
          return (
            <div key={pIdx} className="learning-notes-pillars-grid">
              {numberedItems.map((item, idx) => {
                const accent = colors[idx % colors.length];
                return (
                  <div key={idx} className="learning-pillar-card" style={{ borderTop: `3px solid ${accent}` }}>
                    <div className="pillar-card-top">
                      <span className="pillar-num-badge font-mono" style={{ color: accent, background: `${accent}15`, borderColor: `${accent}40` }}>
                        0{item.num}
                      </span>
                      <h4 className="pillar-title">{item.title}</h4>
                    </div>
                    <p className="pillar-desc">{item.desc}</p>
                  </div>
                );
              })}
            </div>
          );
        }

        const lower = p.toLowerCase();
        if (
          lower.startsWith('the ultimate analytical objective') ||
          lower.startsWith('the golden rule') ||
          lower.startsWith('in summary') ||
          lower.startsWith('congratulations on completing')
        ) {
          return (
            <div key={pIdx} className="learning-takeaway-callout">
              <div className="takeaway-header">
                <TrendingUp size={16} />
                <span>CORE ANALYTICAL OBJECTIVE</span>
              </div>
              <p className="takeaway-text">{p}</p>
            </div>
          );
        }

        return (
          <p key={pIdx} className="learning-notes-lead">
            {p}
          </p>
        );
      })}
    </div>
  );
}

function FormattedCaseStudy({ text, type }) {
  if (!text) return null;
  const isReal = type === 'real';
  const lines = text.split('\n').map(l => l.trim()).filter(Boolean);
  if (lines.length === 0) return null;

  const headerLine = lines[0];
  const contentLines = lines.slice(1);

  const symMatch = headerLine.match(/\((.*?)\)/);
  const companyName = headerLine.replace(/\(.*?\)/, '').replace(/:$/, '').trim();
  const tickerTag = symMatch ? symMatch[1] : '';

  const metrics = [];
  let scorecard = null;
  const narrativeLines = [];

  contentLines.forEach(line => {
    if (line.startsWith('•') || line.startsWith('-')) {
      const clean = line.replace(/^[•\-]\s*/, '');
      if (clean.includes(':')) {
        const parts = clean.split(':');
        metrics.push({
          label: parts[0].trim(),
          val: parts.slice(1).join(':').trim()
        });
      } else {
        metrics.push({ label: 'Metric', val: clean });
      }
    } else if (line.toLowerCase().startsWith('scorecard:')) {
      scorecard = line.substring(line.indexOf(':') + 1).trim();
    } else if (line.toLowerCase().startsWith('result:') || line.toLowerCase().startsWith('outcome:')) {
      scorecard = line.substring(line.indexOf(':') + 1).trim();
    } else {
      narrativeLines.push(line);
    }
  });

  return (
    <div className={`case-study-card ${isReal ? 'real' : 'fictional'}`}>
      <div className="case-study-header-row">
        <div className="case-study-entity-info">
          <div className="case-study-sub-badge font-mono">
            {isReal ? (
              <>
                <ShieldCheck size={12} />
                <span>SEC AUDITED 10-K CASE STUDY</span>
              </>
            ) : (
              <>
                <AlertTriangle size={12} />
                <span>COUNTERFACTUAL RISK CASE STUDY</span>
              </>
            )}
          </div>
          <h4 className="case-study-entity-title">{companyName}</h4>
        </div>
        {tickerTag && (
          <div className="case-study-ticker-pill font-mono">
            {tickerTag}
          </div>
        )}
      </div>

      {metrics.length > 0 && (
        <div className="case-study-metrics-grid">
          {metrics.map((m, idx) => {
            const isProfit = /margin|fcf|profit|cfo|revenue/i.test(m.label);
            const isLoss = /cogs|capex|burn|expense|debt/i.test(m.label);
            return (
              <div key={idx} className="case-metric-tile">
                <span className="case-metric-label">{m.label}</span>
                <span className={`case-metric-val font-mono ${isProfit ? 'gain' : isLoss ? 'loss' : ''}`}>
                  {m.val}
                </span>
              </div>
            );
          })}
        </div>
      )}

      {narrativeLines.length > 0 && (
        <div className="case-study-narrative-list">
          {narrativeLines.map((nLine, idx) => {
            const isWarning = /default|burned|crater|write-off|loss|alert|reveals|warning/i.test(nLine);
            return isWarning ? (
              <div key={idx} className="case-study-alert-box">
                <AlertTriangle size={14} className="alert-box-icon" />
                <span className="alert-box-text">{nLine}</span>
              </div>
            ) : (
              <p key={idx} className="case-study-text-line">{nLine}</p>
            );
          })}
        </div>
      )}

      {scorecard && (
        <div className={`case-study-verdict-box ${isReal ? 'verified' : 'caution'}`}>
          {isReal ? <ShieldCheck size={18} /> : <AlertTriangle size={18} />}
          <div className="verdict-content">
            <span className="verdict-label font-mono">
              {isReal ? 'INSTITUTIONAL AUDIT SCORECARD' : 'RISK POST-MORTEM VERDICT'}
            </span>
            <p className="verdict-text">{scorecard}</p>
          </div>
        </div>
      )}
    </div>
  );
}

function FormattedCompoundingRealityCheck({ text }) {
  if (!text) return null;
  const formulaMatch = text.match(/\((.*?)\)\s*=\s*(.*?%[^\n\.]*)/) || text.match(/\(1\s*\+\s*0\.10\)\^12[^\n\.]*/);
  const formula = formulaMatch ? formulaMatch[0] : null;

  return (
    <div className="learning-reality-check-box">
      <div className="reality-check-header">
        <Award size={16} />
        <span>MATHEMATICAL REALITY CHECK — ANNUAL COMPOUNDING</span>
      </div>

      {formula && (
        <div className="reality-check-formula-pill font-mono">
          <span className="math-label">Formula Proof:</span>
          <span className="math-code">{formula}</span>
        </div>
      )}

      <div className="reality-check-benchmarks">
        <span className="benchmark-pill font-mono">Buffett / Berkshire: ~20% CAGR</span>
        <span className="benchmark-pill font-mono">Simons Medallion: ~39% Net</span>
        <span className="benchmark-pill font-mono">Institutional Target: 12%–25%</span>
      </div>

      <div className="reality-check-text">
        {text}
      </div>
    </div>
  );
}

export default function LearningView({
  initialLessonNumber = null,
  user = null,
  onSelectStock = () => {},
  onNavigateTab = () => {}
}) {
  const [activeLessonNumber, setActiveLessonNumber] = useState(initialLessonNumber);
  const [completedLessons, setCompletedLessons] = useState([]);
  const [isDrawerOpen, setIsDrawerOpen] = useState(false);
  const [isInfographicModalOpen, setIsInfographicModalOpen] = useState(false);

  // Catalog filtering state
  const [catalogStageFilter, setCatalogStageFilter] = useState(0);
  const [catalogSearchQuery, setCatalogSearchQuery] = useState('');

  const contentTopRef = useRef(null);

  // Load completed lessons on mount or user change
  useEffect(() => {
    const list = learningService.getCompletedLessons(user?.userId || 'default_user');
    setCompletedLessons(list);
  }, [user?.userId]);

  // Sync initialLessonNumber prop if changed externally
  useEffect(() => {
    if (initialLessonNumber !== undefined) {
      setActiveLessonNumber(initialLessonNumber);
    }
  }, [initialLessonNumber]);

  // Scroll to top when active lesson changes
  useEffect(() => {
    window.scrollTo({ top: 0, behavior: 'smooth' });
    if (contentTopRef.current) {
      contentTopRef.current.scrollIntoView({ behavior: 'smooth' });
    }
  }, [activeLessonNumber]);

  const handleToggleComplete = (lessonNum) => {
    const next = learningService.toggleLessonCompletion(user?.userId || 'default_user', lessonNum);
    setCompletedLessons(next);
  };

  const handleSelectLesson = (lessonNum) => {
    setActiveLessonNumber(lessonNum);
    // update URL without full page reload
    if (window.history.pushState) {
      window.history.pushState({ tab: 'learning', lesson: lessonNum }, '', `/learning/${lessonNum}`);
    }
  };

  const handleBackToCatalog = () => {
    setActiveLessonNumber(null);
    if (window.history.pushState) {
      window.history.pushState({ tab: 'learning' }, '', '/learning');
    }
  };

  // Find current active lesson object
  const currentLesson = useMemo(() => {
    if (!activeLessonNumber) return null;
    return ALL_40_LESSONS.find((l) => l.lessonNumber === activeLessonNumber) || ALL_40_LESSONS[0];
  }, [activeLessonNumber]);

  // Calculate next lesson to learn
  const nextLessonToLearn = useMemo(() => {
    for (let i = 1; i <= 40; i++) {
      if (!completedLessons.includes(i)) return i;
    }
    return 1;
  }, [completedLessons]);

  // Filtered lessons for catalog view
  const filteredCatalogLessons = useMemo(() => {
    return ALL_40_LESSONS.filter((lesson) => {
      const stageMatch = catalogStageFilter === 0 || lesson.stageNumber === catalogStageFilter;
      const q = catalogSearchQuery.trim().toLowerCase();
      if (!q) return stageMatch;
      const queryMatch =
        lesson.title.toLowerCase().includes(q) ||
        lesson.coreQuestion.toLowerCase().includes(q) ||
        lesson.stageTitle.toLowerCase().includes(q) ||
        lesson.primaryFormula.toLowerCase().includes(q) ||
        `lesson ${lesson.lessonNumber}`.includes(q);
      return stageMatch && queryMatch;
    });
  }, [catalogStageFilter, catalogSearchQuery]);

  const isCurrentLessonDone = activeLessonNumber ? completedLessons.includes(activeLessonNumber) : false;
  const currentFormulas = activeLessonNumber ? getLessonFormulas(activeLessonNumber, currentLesson?.primaryFormula || '') : [];
  const currentVisualFlow = activeLessonNumber ? getLessonVisualFlow(activeLessonNumber) : null;

  const totalCompleted = completedLessons.length;
  const masteredPct = Math.round((totalCompleted / 40) * 100);

  // --------------------------------------------------------------------------
  // VIEW 1: CATALOG / LIBRARY OVERVIEW (When no lesson is active)
  // --------------------------------------------------------------------------
  if (!activeLessonNumber) {
    return (
      <div className="learning-catalog-container" ref={contentTopRef}>
        {/* Hero Section */}
        <div className="learning-hero-card">
          <div className="learning-hero-content">
            <div className="learning-hero-badge">
              <BookOpen size={14} />
              <span>STOCKTICS QUANTITATIVE ACADEMY</span>
            </div>
            <h1 className="learning-hero-title">
              Institutional Stock Market & Quantitative Curriculum
            </h1>
            <p className="learning-hero-desc">
              Master 40 data-driven fundamental and quantitative lessons across 8 progressive stages.
              From SEC Form 10-K forensic analysis and cash flow waterfalls to factor modeling, backtesting, and institutional risk management.
            </p>

            <div className="learning-hero-cta-row">
              <button
                className="learning-hero-start-btn"
                onClick={() => handleSelectLesson(nextLessonToLearn)}
              >
                <span>{totalCompleted === 0 ? 'Start Curriculum — Lesson 1' : `Resume Lesson ${nextLessonToLearn} →`}</span>
              </button>
              <div className="learning-hero-stat-pill font-mono">
                {totalCompleted} of 40 Lessons Mastered ({masteredPct}%)
              </div>
            </div>
          </div>

          {/* Progress Tracker Banner */}
          <div className="learning-progress-banner">
            <div className="learning-progress-info">
              <span className="learning-progress-label">Curriculum Completion</span>
              <span className="learning-progress-val font-mono">{masteredPct}%</span>
            </div>
            <div className="learning-progress-bar-track">
              <div
                className="learning-progress-bar-fill"
                style={{ width: `${Math.max(2, masteredPct)}%` }}
              />
            </div>
          </div>
        </div>

        {/* Filter & Search Bar */}
        <div className="learning-filter-wrapper">
          <div className="learning-search-box">
            <Search size={16} className="learning-search-icon" />
            <input
              type="text"
              className="learning-search-input"
              placeholder="Search 40 lessons by title, formula, concept, or keyword..."
              value={catalogSearchQuery}
              onChange={(e) => setCatalogSearchQuery(e.target.value)}
            />
            {catalogSearchQuery && (
              <button
                className="learning-search-clear"
                onClick={() => setCatalogSearchQuery('')}
              >
                ✕
              </button>
            )}
          </div>

          <div className="learning-stage-tabs">
            <button
              className={`learning-stage-tab ${catalogStageFilter === 0 ? 'active' : ''}`}
              onClick={() => setCatalogStageFilter(0)}
            >
              All Stages
            </button>
            {CURRICULUM_STAGES.map((stg) => (
              <button
                key={stg.stageNumber}
                className={`learning-stage-tab ${catalogStageFilter === stg.stageNumber ? 'active' : ''}`}
                onClick={() => setCatalogStageFilter(stg.stageNumber)}
              >
                Stage {stg.stageNumber}
              </button>
            ))}
          </div>
        </div>

        {/* 40 Lessons Grid */}
        <div className="learning-lessons-grid">
          {filteredCatalogLessons.length === 0 ? (
            <div className="learning-empty-state">
              <HelpCircle size={40} className="learning-empty-icon" />
              <h3>No lessons found</h3>
              <p>No lessons matched your search "{catalogSearchQuery}". Try clearing filters.</p>
              <button
                className="learning-btn-secondary"
                onClick={() => {
                  setCatalogSearchQuery('');
                  setCatalogStageFilter(0);
                }}
              >
                Reset Filters
              </button>
            </div>
          ) : (
            filteredCatalogLessons.map((lesson) => {
              const isDone = completedLessons.includes(lesson.lessonNumber);

              return (
                <div
                  key={lesson.lessonNumber}
                  className={`learning-lesson-card ${isDone ? 'completed' : ''}`}
                  onClick={() => handleSelectLesson(lesson.lessonNumber)}
                >
                  <div className="lesson-card-top">
                    <div className="lesson-card-badges">
                      <span className="lesson-card-num-badge font-mono">
                        LESSON {lesson.lessonNumber}
                      </span>
                      <span className="lesson-card-stage-badge">
                        Stage {lesson.stageNumber}
                      </span>
                    </div>

                    <button
                      className="lesson-card-check-btn"
                      onClick={(e) => {
                        e.stopPropagation();
                        handleToggleComplete(lesson.lessonNumber);
                      }}
                      title={isDone ? 'Mark as Incomplete' : 'Mark as Completed'}
                    >
                      {isDone ? (
                        <CheckCircle2 size={20} className="check-icon done" />
                      ) : (
                        <Circle size={20} className="check-icon todo" />
                      )}
                    </button>
                  </div>

                  <h3 className="lesson-card-title">{lesson.title}</h3>
                  <p className="lesson-card-question">{lesson.coreQuestion}</p>

                  {lesson.primaryFormula && (
                    <div className="lesson-card-formula-box font-mono">
                      {lesson.primaryFormula}
                    </div>
                  )}

                  <div className="lesson-card-footer">
                    <span className="lesson-card-meta">
                      {lesson.keyCheckpoints?.length || 0} Core Checkpoints • 10 min read
                    </span>
                    <div className="lesson-card-action">
                      <span>{isDone ? 'Review Lesson' : 'Start Lesson'}</span>
                      <ArrowRight size={14} />
                    </div>
                  </div>
                </div>
              );
            })
          )}
        </div>
      </div>
    );
  }

  // --------------------------------------------------------------------------
  // VIEW 2: ACTIVE LESSON DETAIL VIEW
  // --------------------------------------------------------------------------
  return (
    <div className="learning-detail-container" ref={contentTopRef}>
      {/* Sticky Header Nav Bar */}
      <div className="learning-detail-sticky-bar">
        <div className="learning-detail-bar-left">
          <button
            className="learning-back-btn"
            onClick={handleBackToCatalog}
            title="Back to All 40 Lessons"
          >
            <ArrowLeft size={16} />
            <span>All Lessons</span>
          </button>

          <div className="learning-bar-title-wrap">
            <span className="learning-bar-stage-badge font-mono">
              STAGE {currentLesson.stageNumber} • LESSON {currentLesson.lessonNumber}
            </span>
            <span className="learning-bar-lesson-title">{currentLesson.title}</span>
          </div>
        </div>

        <div className="learning-detail-bar-right">
          <button
            className={`learning-complete-toggle-btn ${isCurrentLessonDone ? 'done' : ''}`}
            onClick={() => handleToggleComplete(currentLesson.lessonNumber)}
          >
            {isCurrentLessonDone ? (
              <>
                <CheckCircle2 size={16} />
                <span>Completed ✓</span>
              </>
            ) : (
              <>
                <Circle size={16} />
                <span>Mark Complete</span>
              </>
            )}
          </button>

          <button
            className="learning-menu-drawer-btn"
            onClick={() => setIsDrawerOpen(true)}
            title="Curriculum Directory (All 40 Lessons)"
          >
            <Menu size={18} />
            <span className="drawer-btn-label">Directory</span>
          </button>
        </div>
      </div>

      {/* Main Lesson Body */}
      <div className="learning-detail-content">
        {/* 1. Hero Title & Core Thesis Question */}
        <div className="learning-card learning-detail-hero">
          <div className="learning-stage-pill font-mono">
            STAGE {currentLesson.stageNumber}: {currentLesson.stageTitle}
          </div>
          <h1 className="learning-detail-main-title">{currentLesson.title}</h1>
          <div className="learning-core-question-box">
            <span className="learning-core-q-label">CORE QUANTITATIVE QUESTION:</span>
            <p className="learning-core-q-text">{currentLesson.coreQuestion}</p>
          </div>
        </div>

        {/* 2. Key Checkpoints Overview */}
        {currentLesson.keyCheckpoints && currentLesson.keyCheckpoints.length > 0 && (
          <div className="learning-card">
            <div className="learning-section-header">
              <Bookmark size={16} className="section-header-icon" />
              <h3>Core Checkpoints & Key Takeaways</h3>
            </div>
            <div className="learning-checkpoints-list">
              {currentLesson.keyCheckpoints.map((cp, idx) => (
                <div key={idx} className="learning-checkpoint-item">
                  <div className="checkpoint-num font-mono">{idx + 1}</div>
                  <div className="checkpoint-text">{cp}</div>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* 3. Primary Mathematical Formulas */}
        {currentFormulas.length > 0 && (
          <div className="learning-card">
            <div className="learning-section-header">
              <Layers size={16} className="section-header-icon" />
              <h3>Mathematical Foundations & Formula Breakdown</h3>
            </div>
            <div className="learning-formulas-grid">
              {currentFormulas.map((item, idx) => (
                <div key={idx} className="learning-formula-card">
                  <div className="formula-card-header">
                    <span className="formula-card-title">{item.title}</span>
                    <span className="formula-card-badge">Formula</span>
                  </div>
                  <div className="formula-card-eq font-mono">{item.formula}</div>
                  <div className="formula-card-example">{item.example}</div>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* 4. Visual Process Flow / Waterfall (If applicable) */}
        {currentVisualFlow && (
          <div className="learning-card">
            <div className="learning-section-header between">
              <div className="header-left">
                <Compass size={16} className="section-header-icon" />
                <div>
                  <h3>{currentVisualFlow.title}</h3>
                  <span className="section-subtitle">{currentVisualFlow.subtitle}</span>
                </div>
              </div>
              <span className="learning-flow-badge">{currentVisualFlow.flowBadge}</span>
            </div>

            <div className="visual-flow-steps-container">
              {currentVisualFlow.steps.map((step, idx) => {
                const isLast = idx === currentVisualFlow.steps.length - 1;
                return (
                  <React.Fragment key={idx}>
                    <div className={`visual-flow-step-card ${isLast ? 'final-result' : ''}`}>
                      <div className="step-card-left">
                        <div className="step-number-circle font-mono">{step.stepNumber}</div>
                        <div className="step-info">
                          <h4 className="step-title">{step.title}</h4>
                          <p className="step-desc">{step.description}</p>
                        </div>
                      </div>
                      {step.valueBadge && (
                        <div className="step-value-badge font-mono">{step.valueBadge}</div>
                      )}
                    </div>

                    {!isLast && (
                      <div className="visual-flow-connector">
                        <ChevronDown size={18} className="connector-arrow" />
                        {step.transitionText && (
                          <span className="connector-text">{step.transitionText}</span>
                        )}
                      </div>
                    )}
                  </React.Fragment>
                );
              })}
            </div>
          </div>
        )}

        {/* 5. Live Interactive Sandbox / Quantitative Laboratory */}
        <LiveLessonSandbox
          lessonNumber={currentLesson.lessonNumber}
          onSelectStock={onSelectStock}
        />

        {/* 6. Comprehensive Notes & Concept Deep Dive */}
        {currentLesson.fullNotesOverview && (
          <div className="learning-card">
            <div className="learning-section-header">
              <BookOpen size={16} className="section-header-icon" />
              <h3>Curriculum Notes & Forensic Analysis</h3>
            </div>
            
            <FormattedNotes text={currentLesson.fullNotesOverview} />

            {currentLesson.conceptsSummary && Object.keys(currentLesson.conceptsSummary).length > 0 && (
              <div className="learning-concepts-grid">
                {Object.entries(currentLesson.conceptsSummary).map(([k, v]) => (
                  <div key={k} className="concept-tile">
                    <div className="concept-tile-key">{k}</div>
                    <div className="concept-tile-val">{v}</div>
                  </div>
                ))}
              </div>
            )}

            {currentLesson.compoundingRealityCheck && (
              <FormattedCompoundingRealityCheck text={currentLesson.compoundingRealityCheck} />
            )}
          </div>
        )}

        {/* 7. Real-World & Counterfactual Case Studies */}
        {(currentLesson.fictionalExample || currentLesson.realStockExample) && (
          <div className="learning-card">
            <div className="learning-section-header">
              <Layers size={16} className="section-header-icon" />
              <h3>Case Studies & Audited Market Precedents</h3>
            </div>

            <div className="case-studies-container">
              {currentLesson.fictionalExample && (
                <FormattedCaseStudy text={currentLesson.fictionalExample} type="fictional" />
              )}

              {currentLesson.realStockExample && (
                <FormattedCaseStudy text={currentLesson.realStockExample} type="real" />
              )}
            </div>
          </div>
        )}

        {/* 8. Think Like a Researcher */}
        <div className="learning-card research-defense-card">
          <div className="learning-section-header">
            <ShieldCheck size={18} className="research-defense-icon" />
            <h3 className="research-defense-title">THINK LIKE A QUANTITATIVE RESEARCHER</h3>
          </div>

          <div className="researcher-rule-block">
            <span className="researcher-subhead">The Researcher Rule:</span>
            <p className="researcher-text">{currentLesson.researcherRule}</p>
          </div>

          <div className="what-not-proved-block">
            <span className="what-not-proved-subhead">What This Data Does NOT Prove:</span>
            <p className="what-not-proved-text">{currentLesson.whatDataDoesNotProve}</p>
          </div>
        </div>

        {/* 9. Common Mistakes & Golden Rules */}
        <div className="learning-card">
          <div className="mistakes-rules-split">
            {/* Common Mistakes */}
            <div className="mistakes-column">
              <div className="column-header loss">
                <AlertOctagon size={16} />
                <span>COMMON MISTAKES TO AVOID</span>
              </div>
              <div className="mistakes-list">
                {currentLesson.commonMistakes?.map((item, idx) => (
                  <div key={idx} className="mistake-item">
                    <div className="mistake-title">✕ {item.title}</div>
                    <div className="mistake-why">{item.explanation}</div>
                  </div>
                ))}
              </div>
            </div>

            {/* Golden Rules */}
            <div className="rules-column">
              <div className="column-header gain">
                <CheckCircle2 size={16} />
                <span>GOLDEN RULES TO FOLLOW</span>
              </div>
              <div className="rules-list">
                {currentLesson.goldenRules?.map((rule, idx) => (
                  <div key={idx} className="rule-item">
                    <CheckCircle2 size={14} className="rule-check" />
                    <div className="rule-text">{rule}</div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>

        {/* 10. Lesson Infographic Cheat Sheet */}
        {currentLesson.assetInfographicPath && (
          <div className="learning-card infographic-preview-card">
            <div className="learning-section-header between">
              <div className="header-left">
                <Award size={16} className="section-header-icon" />
                <h3>Lesson Infographic Cheat Sheet</h3>
              </div>
              <button
                className="expand-infographic-btn"
                onClick={() => setIsInfographicModalOpen(true)}
                title="View Full Screen"
              >
                <Maximize2 size={14} />
                <span>Tap to Expand</span>
              </button>
            </div>

            <div
              className="infographic-preview-frame"
              onClick={() => setIsInfographicModalOpen(true)}
            >
              <img
                src={currentLesson.assetInfographicPath}
                alt={`Lesson ${currentLesson.lessonNumber} Infographic`}
                className="infographic-preview-img"
              />
              <div className="infographic-overlay-badge">
                <Maximize2 size={14} />
                <span>Tap to Expand High-Resolution Poster</span>
              </div>
            </div>
          </div>
        )}

        {/* 11. Bottom Lesson Navigation Footer */}
        <div className="learning-bottom-nav">
          <button
            className="learning-nav-btn prev"
            disabled={currentLesson.lessonNumber <= 1}
            onClick={() => handleSelectLesson(currentLesson.lessonNumber - 1)}
          >
            <ArrowLeft size={16} />
            <span>Previous Lesson</span>
          </button>

          <button
            className={`learning-nav-complete-btn ${isCurrentLessonDone ? 'done' : ''}`}
            onClick={() => handleToggleComplete(currentLesson.lessonNumber)}
          >
            {isCurrentLessonDone ? 'Completed ✓' : 'Mark as Completed'}
          </button>

          <button
            className="learning-nav-btn next"
            disabled={currentLesson.lessonNumber >= 40}
            onClick={() => handleSelectLesson(currentLesson.lessonNumber + 1)}
          >
            <span>Next Lesson</span>
            <ArrowRight size={16} />
          </button>
        </div>
      </div>

      {/* Curriculum Directory Modal */}
      <CurriculumDrawerModal
        isOpen={isDrawerOpen}
        onClose={() => setIsDrawerOpen(false)}
        activeLessonNumber={currentLesson.lessonNumber}
        completedLessons={completedLessons}
        onSelectLesson={handleSelectLesson}
      />

      {/* High-Resolution Infographic Lightbox Modal */}
      <InfographicModal
        isOpen={isInfographicModalOpen}
        onClose={() => setIsInfographicModalOpen(false)}
        imageSrc={currentLesson.assetInfographicPath}
        title={currentLesson.title}
        lessonNumber={currentLesson.lessonNumber}
      />
    </div>
  );
}
