import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import ComplaintCard from '../components/ComplaintCard';
import { getAllComplaints, searchComplaints } from '../services/complaintService';
import { dashboardService } from '../services/dashboardService';
import { askClientQuestion, askAdminQuestion } from '../services/qaService';
import './Dashboard.css';

function Dashboard({ currentUser }) {
  const [complaints, setComplaints] = useState([]);
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [statsLoading, setStatsLoading] = useState(false);
  const [error, setError] = useState('');
  const [search, setSearch] = useState({
    q: '',
    category: '',
    fromDate: '',
    toDate: '',
    agent: '',
  });
  const [qaQuestion, setQaQuestion] = useState('');
  const [qaAnswer, setQaAnswer] = useState('');
  const [qaLoading, setQaLoading] = useState(false);
  const [showSearch, setShowSearch] = useState(false);
  const navigate = useNavigate();

  const isAdmin = currentUser?.role === 'ADMIN';

  useEffect(() => {
    const fetchData = async () => {
      try {
        const data = await getAllComplaints(currentUser);
        setComplaints(data);
        
        // Fetch stats if admin
        if (isAdmin) {
          setStatsLoading(true);
          try {
            const statsData = await dashboardService.getAdminStats(currentUser.credentials);
            setStats(statsData);
          } catch (statsErr) {
            console.error('Failed to load stats:', statsErr);
          } finally {
            setStatsLoading(false);
          }
        }
      } catch (err) {
        setError('Failed to load complaints. ' + (err.response?.data || err.message));
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, [currentUser, isAdmin]);

  const handleSearchChange = (e) => {
    const { name, value } = e.target;
    setSearch((prev) => ({ ...prev, [name]: value }));
  };

  const handleSearchSubmit = async (e) => {
    e.preventDefault();
    setError('');
    try {
      const params = {};
      if (search.q) params.q = search.q;
      if (search.category) params.category = search.category;
      if (search.fromDate) params.fromDate = search.fromDate;
      if (search.toDate) params.toDate = search.toDate;
      if (search.agent) params.agent = search.agent;

      const data = await searchComplaints(params, currentUser);
      setComplaints(data);
    } catch (err) {
      setError('Failed to search complaints. ' + (err.response?.data || err.message));
    }
  };

  const handleSearchReset = async () => {
    setSearch({ q: '', category: '', fromDate: '', toDate: '', agent: '' });
    setError('');
    try {
      const data = await getAllComplaints(currentUser);
      setComplaints(data);
    } catch (err) {
      setError('Failed to load complaints. ' + (err.response?.data || err.message));
    }
  };

  const handleAskQuestion = async () => {
    if (!qaQuestion.trim()) return;
    setQaLoading(true);
    setQaAnswer('');
    setError('');
    try {
      const resp = await askClientQuestion(qaQuestion, currentUser.userId, currentUser);
      setQaAnswer(resp.answer);
    } catch (err) {
      setError('Failed to get answer. ' + (err.response?.data || err.message));
    } finally {
      setQaLoading(false);
    }
  };

  // admin AI is now on a separate page

  if (loading) return <div className="loading">Loading complaints...</div>;
  if (error) return <div className="error">{error}</div>;

  return (
    <div className="dashboard">
      <div className="dashboard-header">
        <h2>{isAdmin ? 'Admin Dashboard' : 'My Complaints'}</h2>
        <button className="btn-primary" onClick={() => navigate('/complaint/new')}>
          + New Complaint
        </button>
      </div>

      <div className="dashboard-toggles">
        <button
          type="button"
          className={`btn-toggle ${showSearch ? 'active' : ''}`}
          onClick={() => setShowSearch((v) => !v)}
        >
          {showSearch ? 'Hide Search' : 'Show Search'}
        </button>
        <button
          type="button"
          className="btn-toggle"
          onClick={() => navigate('/ai-assistant')}
        >
          Show AI Assistant
        </button>
      </div>

      {/* Search & Filters */}
      {showSearch && (
        <form className="search-filters" onSubmit={handleSearchSubmit}>
          <input
            type="text"
            name="q"
            value={search.q}
            onChange={handleSearchChange}
            placeholder="Search description..."
          />
          <select
            name="category"
            value={search.category}
            onChange={handleSearchChange}
          >
            <option value="">All Categories</option>
            <option value="CARPENTRY">CARPENTRY</option>
            <option value="ELECTRICAL">ELECTRICAL</option>
            <option value="PLUMBING">PLUMBING</option>
            <option value="RAGGING">RAGGING</option>
          </select>
          <input
            type="date"
            name="fromDate"
            value={search.fromDate}
            onChange={handleSearchChange}
          />
          <input
            type="date"
            name="toDate"
            value={search.toDate}
            onChange={handleSearchChange}
          />
          {isAdmin && (
            <input
              type="text"
              name="agent"
              value={search.agent}
              onChange={handleSearchChange}
              placeholder="Assigned to (agent)"
            />
          )}
          <button type="submit" className="btn-primary btn-small">Search</button>
          <button type="button" className="btn-secondary btn-small" onClick={handleSearchReset}>Reset</button>
        </form>
      )}
      <div className="dashboard-layout">
        <div className="dashboard-main">
          {/* Admin Stats Section */}
          {isAdmin && (
            <div className="admin-stats-section">
              {statsLoading ? (
                <div className="stats-loading">Loading statistics...</div>
              ) : stats ? (
                <>
                  {/* Status Counter Cards */}
                  <div className="stats-cards">
                    <div className="stat-card total">
                      <div className="stat-value">{stats.total}</div>
                      <div className="stat-label">Total Complaints</div>
                    </div>
                    <div className="stat-card open">
                      <div className="stat-value">{stats.open}</div>
                      <div className="stat-label">Open</div>
                    </div>
                    <div className="stat-card in-progress">
                      <div className="stat-value">{stats.inProgress}</div>
                      <div className="stat-label">In Progress</div>
                    </div>
                    <div className="stat-card resolved">
                      <div className="stat-value">{stats.resolved}</div>
                      <div className="stat-label">Resolved</div>
                    </div>
                  </div>

                  {/* Category Breakdown */}
                  <div className="category-breakdown">
                    <h3>Category Breakdown</h3>
                    <div className="category-list">
                      {Object.entries(stats.categoryCounts).map(([category, count]) => (
                        <div key={category} className="category-item">
                          <span className="category-name">{category}</span>
                          <span className="category-count">{count}</span>
                        </div>
                      ))}
                    </div>
                  </div>
                </>
              ) : null}
            </div>
          )}

          {/* Complaints List */}
          {complaints.length === 0 ? (
            <p className="no-complaints">No complaints found. {!isAdmin && 'Create your first one!'}</p>
          ) : (
            <div className="complaint-grid">
              {complaints.map((c) => (
                <ComplaintCard
                  key={c.id}
                  complaint={c}
                  onClick={() => navigate(`/complaint/${c.id}`)}
                />
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default Dashboard;
