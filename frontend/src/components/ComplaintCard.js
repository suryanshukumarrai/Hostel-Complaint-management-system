import React from 'react';
import './ComplaintCard.css';

const IMAGE_BASE_URL = 'http://localhost:8080';

function ComplaintCard({ complaint, onClick }) {
  return (
    <div className="complaint-card" onClick={onClick}>
      {complaint.imageUrl && (
        <div className="card-image-wrapper">
          <img
            src={`${IMAGE_BASE_URL}${complaint.imageUrl}`}
            alt={`Complaint ${complaint.id}`}
            className="complaint-image-thumb"
          />
        </div>
      )}
      <div className="card-content">
        <div className="card-header">
          <span className="card-id">#{complaint.id}</span>
          <span className={`status-badge status-${complaint.status?.toLowerCase()}`}>
            {complaint.status}
          </span>
        </div>
        <div className="card-category">{complaint.category}</div>
        {complaint.messageType && (
          <div className="card-message-type">{complaint.messageType}</div>
        )}
        <div className="card-description">
          {complaint.description || 'No description provided'}
        </div>
      </div>
      <div className="card-footer">
        <span className="footer-user">{complaint.raisedBy?.name || 'Unknown'}</span>
        <span className="footer-date">{complaint.createdAt ? new Date(complaint.createdAt).toLocaleDateString() : ''}</span>
      </div>
    </div>
  );
}

export default ComplaintCard;
