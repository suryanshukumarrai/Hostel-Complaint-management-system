import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getComplaintById, updateComplaintStatus } from '../services/complaintService';
import './ComplaintDetails.css';

const STATUSES = ['OPEN', 'IN_PROGRESS', 'RESOLVED'];
const IMAGE_BASE_URL = 'http://localhost:8080';

function ComplaintDetails({ currentUser }) {
  const { id } = useParams();
  const navigate = useNavigate();
  const [complaint, setComplaint] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [newStatus, setNewStatus] = useState('');
  const [updating, setUpdating] = useState(false);
  const [showImageModal, setShowImageModal] = useState(false);

  useEffect(() => {
    const fetch = async () => {
      try {
        const data = await getComplaintById(id, currentUser);
        setComplaint(data);
        setNewStatus(data.status);
      } catch (err) {
        setError('Failed to load complaint.');
      } finally {
        setLoading(false);
      }
    };
    fetch();
  }, [id, currentUser]);

  const handleStatusUpdate = async () => {
    setUpdating(true);
    try {
      const updated = await updateComplaintStatus(id, newStatus, currentUser);
      setComplaint(updated);
    } catch (err) {
      setError('Failed to update status: ' + (err.response?.data || err.message));
    } finally {
      setUpdating(false);
    }
  };

  if (loading) return <div className="loading">Loading...</div>;
  if (error) return <div className="error">{error}</div>;
  if (!complaint) return null;

  const isAdmin = currentUser?.role === 'ADMIN';

  return (
    <div className="complaint-details">
      <button className="btn-back" onClick={() => navigate('/dashboard')}>← Back</button>
      <h2>Complaint #{complaint.id}</h2>
      <div className="detail-grid">
        <div className="detail-item"><span>Type:</span> {complaint.messageType}</div>
        <div className="detail-item"><span>Category:</span> {complaint.category}</div>
        <div className="detail-item"><span>Sub Category:</span> {complaint.subCategory || '—'}</div>
        <div className="detail-item"><span>Block:</span> {complaint.block || '—'}</div>
        <div className="detail-item"><span>Contact:</span> {complaint.contactNo || '—'}</div>
        <div className="detail-item"><span>Status:</span>
          <span className={`status-badge status-${complaint.status?.toLowerCase()}`}>{complaint.status}</span>
        </div>
        <div className="detail-item"><span>Raised By:</span> {complaint.raisedBy?.name || '—'}</div>
        <div className="detail-item"><span>Assigned To:</span> {complaint.assignedTo || '—'}</div>
        <div className="detail-item"><span>Created:</span> {new Date(complaint.createdAt).toLocaleString()}</div>
      </div>
      {complaint.imageUrl && (
        <div className="detail-image">
          <img
            src={`${IMAGE_BASE_URL}${complaint.imageUrl}`}
            alt={`Complaint ${complaint.id}`}
            className="detail-image-thumb"
            onClick={() => setShowImageModal(true)}
          />
        </div>
      )}
      <div className="description-box">
        <h4>Description</h4>
        <p>{complaint.description}</p>
      </div>

      {isAdmin && (
        <div className="status-update">
          <h4>Update Status</h4>
          <select value={newStatus} onChange={(e) => setNewStatus(e.target.value)}>
            {STATUSES.map((s) => <option key={s} value={s}>{s}</option>)}
          </select>
          <button className="btn-primary" onClick={handleStatusUpdate} disabled={updating}>
            {updating ? 'Updating...' : 'Update'}
          </button>
        </div>
      )}

      {showImageModal && complaint.imageUrl && (
        <div className="image-modal-backdrop" onClick={() => setShowImageModal(false)}>
          <div className="image-modal" onClick={(e) => e.stopPropagation()}>
            <img
              src={`${IMAGE_BASE_URL}${complaint.imageUrl}`}
              alt={`Complaint ${complaint.id}`}
            />
          </div>
        </div>
      )}
    </div>
  );
}

export default ComplaintDetails;
