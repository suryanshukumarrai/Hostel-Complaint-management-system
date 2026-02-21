import React, { useState, useEffect } from 'react';
import { getAllComplaints } from '../services/complaintService';
import './Statistics.css';

function Statistics({ currentUser }) {
  const [complaints, setComplaints] = useState([]);
  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState({});

  useEffect(() => {
    const fetchComplaints = async () => {
      try {
        const data = await getAllComplaints(currentUser);
        setComplaints(data);
        calculateStats(data);
      } catch (error) {
        console.error('Failed to fetch complaints:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchComplaints();
  }, [currentUser]);

  const calculateStats = (data) => {
    const total = data.length;
    
    // Status breakdown
    const statusCount = {
      OPEN: data.filter(c => c.status === 'OPEN').length,
      IN_PROGRESS: data.filter(c => c.status === 'IN_PROGRESS').length,
      RESOLVED: data.filter(c => c.status === 'RESOLVED').length,
    };

    // Category breakdown
    const categoryCount = {};
    data.forEach(c => {
      categoryCount[c.category] = (categoryCount[c.category] || 0) + 1;
    });

    // Block breakdown
    const blockCount = {};
    data.forEach(c => {
      if (c.block) {
        blockCount[c.block] = (blockCount[c.block] || 0) + 1;
      }
    });

    // Message type breakdown
    const messageTypeCount = {};
    data.forEach(c => {
      messageTypeCount[c.messageType] = (messageTypeCount[c.messageType] || 0) + 1;
    });

    // Recent complaints (last 7 days)
    const sevenDaysAgo = new Date();
    sevenDaysAgo.setDate(sevenDaysAgo.getDate() - 7);
    const recentComplaints = data.filter(c => new Date(c.createdAt) > sevenDaysAgo).length;

    // Average age of open complaints (in days)
    const openComplaints = data.filter(c => c.status !== 'RESOLVED');
    const avgAge = openComplaints.length > 0
      ? openComplaints.reduce((sum, c) => {
          const age = Math.floor((Date.now() - new Date(c.createdAt)) / (1000 * 60 * 60 * 24));
          return sum + age;
        }, 0) / openComplaints.length
      : 0;

    setStats({
      total,
      statusCount,
      categoryCount,
      blockCount,
      messageTypeCount,
      recentComplaints,
      avgAge: Math.round(avgAge),
      resolutionRate: total > 0 ? ((statusCount.RESOLVED / total) * 100).toFixed(1) : 0,
    });
  };

  const getPercentage = (value, total) => {
    return total > 0 ? ((value / total) * 100).toFixed(1) : 0;
  };

  if (loading) {
    return <div className="statistics-loading">Loading statistics...</div>;
  }

  return (
    <div className="statistics-container">
      <h1 className="statistics-title">📊 Complaint Statistics</h1>

      {/* Overview Cards */}
      <div className="stats-overview">
        <div className="stat-card stat-card-primary">
          <div className="stat-icon">📋</div>
          <div className="stat-content">
            <h3>Total Complaints</h3>
            <p className="stat-number">{stats.total}</p>
          </div>
        </div>

        <div className="stat-card stat-card-success">
          <div className="stat-icon">✅</div>
          <div className="stat-content">
            <h3>Resolved</h3>
            <p className="stat-number">{stats.statusCount?.RESOLVED || 0}</p>
            <span className="stat-subtitle">{stats.resolutionRate}% Resolution Rate</span>
          </div>
        </div>

        <div className="stat-card stat-card-warning">
          <div className="stat-icon">⏳</div>
          <div className="stat-content">
            <h3>In Progress</h3>
            <p className="stat-number">{stats.statusCount?.IN_PROGRESS || 0}</p>
          </div>
        </div>

        <div className="stat-card stat-card-danger">
          <div className="stat-icon">🔴</div>
          <div className="stat-content">
            <h3>Open</h3>
            <p className="stat-number">{stats.statusCount?.OPEN || 0}</p>
          </div>
        </div>
      </div>

      {/* Additional Metrics */}
      <div className="stats-metrics">
        <div className="metric-card">
          <div className="metric-icon">🕒</div>
          <div className="metric-info">
            <h4>Avg. Age of Open Complaints</h4>
            <p className="metric-value">{stats.avgAge} days</p>
          </div>
        </div>

        <div className="metric-card">
          <div className="metric-icon">📅</div>
          <div className="metric-info">
            <h4>Recent (Last 7 Days)</h4>
            <p className="metric-value">{stats.recentComplaints} complaints</p>
          </div>
        </div>
      </div>

      {/* Status Breakdown */}
      <div className="stats-section">
        <h2 className="section-title">Status Breakdown</h2>
        <div className="progress-bars">
          <div className="progress-item">
            <div className="progress-label">
              <span>Open</span>
              <span>{stats.statusCount?.OPEN || 0} ({getPercentage(stats.statusCount?.OPEN, stats.total)}%)</span>
            </div>
            <div className="progress-bar">
              <div 
                className="progress-fill progress-open" 
                style={{ width: `${getPercentage(stats.statusCount?.OPEN, stats.total)}%` }}
              ></div>
            </div>
          </div>

          <div className="progress-item">
            <div className="progress-label">
              <span>In Progress</span>
              <span>{stats.statusCount?.IN_PROGRESS || 0} ({getPercentage(stats.statusCount?.IN_PROGRESS, stats.total)}%)</span>
            </div>
            <div className="progress-bar">
              <div 
                className="progress-fill progress-in-progress" 
                style={{ width: `${getPercentage(stats.statusCount?.IN_PROGRESS, stats.total)}%` }}
              ></div>
            </div>
          </div>

          <div className="progress-item">
            <div className="progress-label">
              <span>Resolved</span>
              <span>{stats.statusCount?.RESOLVED || 0} ({getPercentage(stats.statusCount?.RESOLVED, stats.total)}%)</span>
            </div>
            <div className="progress-bar">
              <div 
                className="progress-fill progress-resolved" 
                style={{ width: `${getPercentage(stats.statusCount?.RESOLVED, stats.total)}%` }}
              ></div>
            </div>
          </div>
        </div>
      </div>

      {/* Category Breakdown */}
      <div className="stats-section">
        <h2 className="section-title">Complaints by Category</h2>
        <div className="category-grid">
          {Object.entries(stats.categoryCount || {}).map(([category, count]) => (
            <div key={category} className="category-item">
              <div className="category-header">
                <span className="category-name">{category}</span>
                <span className="category-count">{count}</span>
              </div>
              <div className="category-bar">
                <div 
                  className="category-bar-fill" 
                  style={{ width: `${getPercentage(count, stats.total)}%` }}
                ></div>
              </div>
              <span className="category-percentage">{getPercentage(count, stats.total)}%</span>
            </div>
          ))}
        </div>
      </div>

      {/* Block Breakdown */}
      {Object.keys(stats.blockCount || {}).length > 0 && (
        <div className="stats-section">
          <h2 className="section-title">Complaints by Block</h2>
          <div className="block-grid">
            {Object.entries(stats.blockCount || {})
              .sort((a, b) => b[1] - a[1])
              .map(([block, count]) => (
                <div key={block} className="block-card">
                  <div className="block-name">Block {block}</div>
                  <div className="block-count">{count}</div>
                  <div className="block-percentage">{getPercentage(count, stats.total)}%</div>
                </div>
              ))}
          </div>
        </div>
      )}

      {/* Message Type Breakdown */}
      <div className="stats-section">
        <h2 className="section-title">Message Type Distribution</h2>
        <div className="message-type-grid">
          {Object.entries(stats.messageTypeCount || {}).map(([type, count]) => (
            <div key={type} className="message-type-card">
              <div className="message-type-icon">
                {type === 'COMPLAINT' ? '⚠️' : type === 'SUGGESTION' ? '💡' : '❓'}
              </div>
              <div className="message-type-info">
                <h4>{type}</h4>
                <p className="message-type-count">{count} items</p>
                <span className="message-type-percent">{getPercentage(count, stats.total)}%</span>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Summary Footer */}
      <div className="stats-footer">
        <p>📈 Total Statistics based on {stats.total} complaints</p>
        <p className="footer-note">Last updated: {new Date().toLocaleString()}</p>
      </div>
    </div>
  );
}

export default Statistics;
