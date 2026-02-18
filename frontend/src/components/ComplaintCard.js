import React from 'react';
import './ComplaintCard.css';

function ComplaintCard({ complaint, onClick }) {
  return (
    <div className="complaint-card" onClick={onClick}>
      <div className="card-header">
        <span className="card-id">#{complaint.id}</span>
        <span className={`status-badge status-${complaint.status?.toLowerCase()}`}>
          {complaint.status}
        </span>
      </div>
      <div className="card-category">{complaint.category} — {complaint.messageType}</div>
      <div className="card-description">
        {complaint.description?.length > 100
          ? complaint.description.substring(0, 100) + '...'
          : complaint.description}
      </div>
      <div className="card-footer">
        <span>{complaint.raisedBy?.name || 'Unknown'}</span>
        <span>{complaint.createdAt ? new Date(complaint.createdAt).toLocaleDateString() : ''}</span>
      </div>
    </div>
  );
}

export default ComplaintCard;
