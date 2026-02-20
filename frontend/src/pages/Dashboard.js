import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import ComplaintCard from '../components/ComplaintCard';
import { getAllComplaints } from '../services/complaintService';
import { dashboardService } from '../services/dashboardService';
import { aiComplaintService } from '../services/aiComplaintService';
import PrimaryButton from '../components/PrimaryButton';
import './Dashboard.css';

function Dashboard({ currentUser }) {
  const [complaints, setComplaints] = useState([]);
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [statsLoading, setStatsLoading] = useState(false);
  const [error, setError] = useState('');
  const [showAiModal, setShowAiModal] = useState(false);
  const [aiDescription, setAiDescription] = useState('');
  const [aiLoading, setAiLoading] = useState(false);
  const [aiError, setAiError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');
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

  const handleAiGenerateClick = () => {
    setShowAiModal(true);
    setAiError('');
    setAiDescription('');
  };

  const handleAiSubmit = async (e) => {
    e.preventDefault();
    
    if (!aiDescription.trim()) {
      setAiError('Please describe your issue');
      return;
    }

    if (aiDescription.length > 10000) {
      setAiError('Description cannot exceed 10000 characters');
      return;
    }

    setAiLoading(true);
    setAiError('');

    try {
      const response = await aiComplaintService.generateComplaint(
        aiDescription
      );
      
      // Add new complaint to the list
      setComplaints([response, ...complaints]);
      
      // Show success message
      setSuccessMessage('Complaint generated successfully!');
      setTimeout(() => setSuccessMessage(''), 3000);
      
      // Close modal and reset form
      setShowAiModal(false);
      setAiDescription('');
      
    } catch (err) {
      setAiError(
        err.response?.data?.message ||
        err.message ||
        'Failed to generate complaint. Please try again.'
      );
    } finally {
      setAiLoading(false);
    }
  };

  const handleCloseModal = () => {
    setShowAiModal(false);
    setAiDescription('');
    setAiError('');
  };

  if (loading) return <div className="loading">Loading complaints...</div>;
  if (error) return <div className="error">{error}</div>;

  return (
    <div className="dashboard">
      <div className="dashboard-header">
        <h2>{isAdmin ? 'Admin Dashboard' : 'My Complaints'}</h2>
        <div className="header-buttons">
          <PrimaryButton onClick={handleAiGenerateClick}>
            🤖 Auto Generate Complaint
          </PrimaryButton>
          <PrimaryButton onClick={() => navigate('/complaint/new')}>
            + New Complaint
          </PrimaryButton>
        </div>
      </div>

      {/* Success Message */}
      {successMessage && (
        <div className="success-message">
          ✓ {successMessage}
        </div>
      )}

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

      {/* AI Generate Modal */}
      {showAiModal && (
        <div className="modal-overlay" onClick={handleCloseModal}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3>🤖 AI-Powered Complaint Generator</h3>
              <button className="modal-close" onClick={handleCloseModal}>&times;</button>
            </div>
            
            <form onSubmit={handleAiSubmit}>
              <div className="form-group">
                <label htmlFor="ai-description">Describe your issue in detail:</label>
                <textarea
                  id="ai-description"
                  className="ai-textarea"
                  placeholder="Example: My water tap in room A401 is completely broken and leaking water. It happened this morning and I haven't been able to turn it off..."
                  value={aiDescription}
                  onChange={(e) => setAiDescription(e.target.value)}
                  rows={8}
                  disabled={aiLoading}
                />
                <div className="char-count">
                  {aiDescription.length} / 10000 characters
                </div>
              </div>

              {aiError && (
                <div className="error-message">{aiError}</div>
              )}

              <div className="modal-buttons">
                <button
                  type="button"
                  className="btn-secondary"
                  onClick={handleCloseModal}
                  disabled={aiLoading}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="btn-primary"
                  disabled={aiLoading || !aiDescription.trim()}
                >
                  {aiLoading ? 'Generating...' : 'Generate Ticket'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

export default Dashboard;
