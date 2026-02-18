import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import ComplaintCard from '../components/ComplaintCard';
import { getAllComplaints } from '../services/complaintService';
import { dashboardService } from '../services/dashboardService';
import './Dashboard.css';

function Dashboard({ currentUser }) {
  const [complaints, setComplaints] = useState([]);
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [statsLoading, setStatsLoading] = useState(false);
  const [error, setError] = useState('');
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
  );
}

export default Dashboard;
