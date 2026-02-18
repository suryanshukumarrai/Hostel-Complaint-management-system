import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import ComplaintCard from '../components/ComplaintCard';
import { getAllComplaints } from '../services/complaintService';
import './Dashboard.css';

function Dashboard({ currentUser }) {
  const [complaints, setComplaints] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    const fetchComplaints = async () => {
      try {
        const data = await getAllComplaints(currentUser);
        setComplaints(data);
      } catch (err) {
        setError('Failed to load complaints. ' + (err.response?.data || err.message));
      } finally {
        setLoading(false);
      }
    };
    fetchComplaints();
  }, [currentUser]);

  if (loading) return <div className="loading">Loading complaints...</div>;
  if (error) return <div className="error">{error}</div>;

  return (
    <div className="dashboard">
      <div className="dashboard-header">
        <h2>Complaints Dashboard</h2>
        <button className="btn-primary" onClick={() => navigate('/complaint/new')}>
          + New Complaint
        </button>
      </div>
      {complaints.length === 0 ? (
        <p className="no-complaints">No complaints found. Create your first one!</p>
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
