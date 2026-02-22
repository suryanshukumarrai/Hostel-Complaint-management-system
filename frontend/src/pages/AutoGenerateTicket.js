import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { generateAiComplaint } from '../services/complaintService';
import './AutoGenerateTicket.css';

function AutoGenerateTicket({ currentUser }) {
  const [description, setDescription] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    if (!description.trim()) {
      setError('Please paste a description.');
      return;
    }

    setSubmitting(true);
    try {
      const resp = await generateAiComplaint(description, currentUser);
      if (resp?.id) {
        navigate(`/complaint/${resp.id}`);
      } else {
        navigate('/dashboard');
      }
    } catch (err) {
      setError('Failed to auto-generate ticket: ' + (err.response?.data || err.message));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="auto-generate">
      <div className="auto-generate-card">
        <h2>Auto Generate Ticket</h2>
        <p className="helper-text">
          Paste a full description of the issue. We will generate and file the complaint for you.
        </p>
        {error && <div className="error">{error}</div>}
        <form onSubmit={handleSubmit}>
          <label>Description Passage *
            <textarea
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              rows={8}
              placeholder="Describe the problem in detail..."
              required
            />
          </label>
          <div className="form-actions">
            <button type="button" className="btn-secondary" onClick={() => navigate('/dashboard')}>Cancel</button>
            <button type="submit" className="btn-primary" disabled={submitting}>
              {submitting ? 'Generating...' : 'Auto Generate'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default AutoGenerateTicket;
