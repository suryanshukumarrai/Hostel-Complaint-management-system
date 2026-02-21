import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { askClientQuestion, askAdminQuestion, getQaHistory, getUserAiAnalytics, getGlobalAiAnalytics, getUserAiDailyCounts, getGlobalAiDailyCounts } from '../services/qaService';
import './Dashboard.css';

function AiAssistant({ currentUser }) {
  const [question, setQuestion] = useState('');
  const [answer, setAnswer] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [history, setHistory] = useState([]);
  const [openHistoryId, setOpenHistoryId] = useState(null);
  const [analytics, setAnalytics] = useState(null);
  const [dailyCounts, setDailyCounts] = useState([]);
  const navigate = useNavigate();

  const isAdmin = currentUser?.role === 'ADMIN';

  useEffect(() => {
    const loadHistory = async () => {
      try {
        const data = await getQaHistory(currentUser);
        setHistory(data || []);
        setOpenHistoryId(null);
      } catch (e) {
        // history is non-critical; fail silently
        // console.error('Failed to load QA history', e);
      }
    };
    const loadAnalytics = async () => {
      if (!currentUser || currentUser.role !== 'ADMIN') {
        setAnalytics(null);
        return;
      }
      try {
        const data = await getGlobalAiAnalytics(currentUser);
        setAnalytics(data || null);
      } catch (e) {
        // ignore analytics errors
      }
    };
    const loadDailyCounts = async () => {
      if (!currentUser || currentUser.role !== 'ADMIN') {
        setDailyCounts([]);
        return;
      }
      try {
        const data = await getGlobalAiDailyCounts(currentUser, 7);
        setDailyCounts(data || []);
      } catch (e) {
        // ignore daily analytics errors
      }
    };
    if (currentUser) {
      loadHistory();
      loadAnalytics();
      loadDailyCounts();
    }
  }, [currentUser]);

  const handleAsk = async () => {
    if (!question.trim()) return;
    setLoading(true);
    setAnswer('');
    setError('');
    try {
      let resp;
      if (isAdmin) {
        resp = await askAdminQuestion(question, currentUser);
      } else {
        resp = await askClientQuestion(question, currentUser.userId, currentUser);
      }
      setAnswer(resp.answer);
      // refresh history after a successful question
      try {
        const data = await getQaHistory(currentUser);
        setHistory(data || []);
        setOpenHistoryId(null);
      } catch (e) {
        // ignore history refresh errors
      }
    } catch (err) {
      setError('Failed to get answer. ' + (err.response?.data || err.message));
    } finally {
      setLoading(false);
    }
  };

  const inferCategoryFromQuestion = (text) => {
    const q = (text || '').toLowerCase();
    if (q.includes('plumb') || q.includes('leak') || q.includes('water')) return 'PLUMBING';
    if (q.includes('electric') || q.includes('light') || q.includes('fan') || q.includes('ac')) return 'ELECTRICAL';
    if (q.includes('door') || q.includes('window') || q.includes('furniture') || q.includes('bed')) return 'CARPENTRY';
    if (q.includes('ragging') || q.includes('bully') || q.includes('harass')) return 'RAGGING';
    return 'PLUMBING';
  };

  const handleCreateTicket = async () => {
    if (isAdmin || !question.trim() || !currentUser) return;

    // Navigate to the full complaint form with AI-provided details pre-filled
    navigate('/complaint/new', {
      state: {
        fromAi: true,
        description: question,
        category: inferCategoryFromQuestion(question),
      },
    });
  };

  if (!currentUser) {
    return <div className="loading">Please log in to use the AI assistant.</div>;
  }

  const hasHistory = history && history.length > 0;
  const totalQuestions = analytics?.totalQuestions || 0;
  const totalAdminQuestions = analytics?.totalAdminQuestions || 0;
  const totalUserQuestions = analytics?.totalUserQuestions || 0;
  const successCount = analytics?.successCount || 0;
  const errorCount = analytics?.errorCount || 0;
  const successRate = totalQuestions > 0 ? Math.round((successCount / totalQuestions) * 100) : 0;
  const firstAskedAt = analytics?.firstQuestionDate || null;
  const lastAskedAt = analytics?.lastQuestionDate || null;

  const hasDaily = dailyCounts && dailyCounts.length > 0;
  const maxDailyTotal = hasDaily ? Math.max(...dailyCounts.map((d) => d.total)) : 0;

  return (
    <div className="dashboard">
      <div className="dashboard-header">
        <h2>AI Assistant</h2>
      </div>

      {isAdmin && analytics && (
        <div className="qa-section ai-analytics">
          <div className="qa-header">
            <div>
              <div className="qa-label">AI Analytics</div>
              <h3>Your assistant usage</h3>
              <p className="qa-subtitle">
                {isAdmin
                  ? 'Overall usage of the AI assistant in the system.'
                  : 'Your personal usage of the AI assistant.'}
              </p>
            </div>
          </div>
          <div className="ai-analytics-cards">
            <div className="ai-analytics-card">
              <div className="ai-analytics-label">Total questions</div>
              <div className="ai-analytics-value">{totalQuestions}</div>
              {firstAskedAt && lastAskedAt && (
                <div className="ai-analytics-subtext">
                  From {new Date(firstAskedAt).toLocaleDateString()} to {new Date(lastAskedAt).toLocaleDateString()}
                </div>
              )}
            </div>
            <div className="ai-analytics-card">
              <div className="ai-analytics-label">Admin questions</div>
              <div className="ai-analytics-value">{totalAdminQuestions}</div>
            </div>
            <div className="ai-analytics-card">
              <div className="ai-analytics-label">User questions</div>
              <div className="ai-analytics-value">{totalUserQuestions}</div>
            </div>
            <div className="ai-analytics-card">
              <div className="ai-analytics-label">Success rate</div>
              <div className="ai-analytics-value">{successRate}%</div>
              <div className="ai-analytics-subtext">{successCount} successful answers</div>
            </div>
            <div className="ai-analytics-card">
              <div className="ai-analytics-label">Errors</div>
              <div className="ai-analytics-value">{errorCount}</div>
              <div className="ai-analytics-subtext">LLM/API failures detected</div>
            </div>
          </div>

          {hasDaily && maxDailyTotal > 0 && (
            <div className="ai-analytics-chart">
              <div className="ai-analytics-label">Last 7 days</div>
              <div className="ai-analytics-chart-bars">
                {dailyCounts.map((d) => {
                  const adminPercent = maxDailyTotal > 0 ? (d.admin / maxDailyTotal) * 100 : 0;
                  const userPercent = maxDailyTotal > 0 ? (d.user / maxDailyTotal) * 100 : 0;
                  const adminHeight = d.admin === 0 ? 0 : Math.max(8, adminPercent);
                  const userHeight = d.user === 0 ? 0 : Math.max(8, userPercent);
                  const dateLabel = new Date(d.date).toLocaleDateString(undefined, { month: 'short', day: 'numeric' });
                  return (
                    <div key={d.date} className="ai-analytics-chart-day">
                      <div className="ai-analytics-chart-day-bars">
                        <div
                          className="ai-analytics-chart-bar admin"
                          style={{ height: `${adminHeight || 0}%` }}
                          title={`${d.admin} admin questions on ${dateLabel}`}
                        />
                        <div
                          className="ai-analytics-chart-bar user"
                          style={{ height: `${userHeight || 0}%` }}
                          title={`${d.user} user questions on ${dateLabel}`}
                        />
                      </div>
                      <div className="ai-analytics-chart-day-count">
                        {d.total}
                        <span className="ai-analytics-chart-day-count-sub">
                          {`  (${d.admin} admin / ${d.user} user)`}
                        </span>
                      </div>
                      <div className="ai-analytics-chart-day-label">{dateLabel}</div>
                    </div>
                  );
                })}
              </div>
              <div className="ai-analytics-chart-legend">
                <div className="ai-analytics-chart-legend-item">
                  <span className="ai-analytics-chart-legend-swatch admin" /> Admin
                </div>
                <div className="ai-analytics-chart-legend-item">
                  <span className="ai-analytics-chart-legend-swatch user" /> User
                </div>
              </div>
            </div>
          )}
        </div>
      )}

      <div className="qa-section">
        <div className="qa-header">
          <div>
            <div className="qa-label">AI Assistant</div>
            <h3>{isAdmin ? 'Ask about all complaints' : 'Ask about my complaints'}</h3>
            <p className="qa-subtitle">
              {isAdmin
                ? 'Get summaries and insights across all tickets in the system.'
                : 'Get quick answers based on your own complaint history.'}
            </p>
          </div>
        </div>
        <div className="qa-input-row">
          <input
            type="text"
            value={question}
            onChange={(e) => setQuestion(e.target.value)}
            placeholder={
              isAdmin
                ? 'e.g., How many plumbing complaints are open and who raised them?'
                : 'e.g., How many open plumbing complaints do I have?'
            }
          />
          <button
            type="button"
            className="btn-primary btn-small qa-ask-button"
            onClick={handleAsk}
            disabled={loading}
          >
            {loading ? 'Thinking...' : 'Ask'}
          </button>
        </div>
        {!isAdmin && (
          <div className="qa-history-follow-up-row" style={{ marginTop: 8 }}>
            <button
              type="button"
              className="btn-secondary btn-small qa-history-follow-up-btn"
              onClick={handleCreateTicket}
              disabled={!question.trim()}
            >
              Raise complaint from this description
            </button>
          </div>
        )}
        {error && <div className="error">{error}</div>}
        {answer && <div className="qa-answer">{answer}</div>}
      </div>

      {hasHistory && (
        <div className="qa-section" style={{ marginTop: 16 }}>
          <div className="qa-header">
            <div>
              <div className="qa-label">History</div>
              <h3>Recent questions</h3>
              <p className="qa-subtitle">Last {history.length} interactions with the assistant.</p>
            </div>
          </div>
          <div className="qa-history-list">
            {history.map((item) => (
              <div
                key={item.id}
                className={`qa-history-item ${openHistoryId === item.id ? 'open' : ''}`}
                onClick={() => setOpenHistoryId(openHistoryId === item.id ? null : item.id)}
              >
                <div className="qa-history-question">
                  Q: {item.question}
                </div>
                {openHistoryId === item.id && (
                  <>
                    <div className="qa-history-answer">A: {item.answer}</div>
                    {item.askedAt && (
                      <div className="qa-history-meta">
                        {new Date(item.askedAt).toLocaleString()}{item.admin ? ' • Admin' : ''}
                      </div>
                    )}
                    <div className="qa-history-follow-up-row">
                      <button
                        type="button"
                        className="btn-primary btn-small qa-history-follow-up-btn"
                        onClick={(e) => {
                          e.stopPropagation();
                          setQuestion(`Follow up on: "${item.question}" - `);
                        }}
                      >
                        Ask follow-up
                      </button>
                    </div>
                  </>
                )}
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}

export default AiAssistant;
