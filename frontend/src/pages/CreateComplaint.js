import React, { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { createComplaint } from '../services/complaintService';
import './CreateComplaint.css';

const MESSAGE_TYPES = ['GRIEVANCE', 'ASSISTANCE', 'ENQUIRY', 'FEEDBACK', 'POSITIVE_FEEDBACK'];
const CATEGORIES = ['CARPENTRY', 'ELECTRICAL', 'PLUMBING', 'RAGGING'];

const SUB_CATEGORIES = {
  CARPENTRY: ['Door Repair', 'Window Repair', 'Furniture Repair', 'Cabinet Repair', 'Other'],
  ELECTRICAL: ['Light Not Working', 'Fan Issue', 'AC Issue', 'Switch Problem', 'Socket Problem', 'Wiring Issue', 'Other'],
  PLUMBING: ['Leakage', 'Blockage', 'Tap Issue', 'Bathroom Issue', 'Other'],
  RAGGING: ['Verbal Abuse', 'Physical Abuse', 'Mental Harassment', 'Other']
};

const TIME_SLOTS = [
  '8:00 AM - 10:00 AM',
  '10:00 AM - 12:00 PM',
  '12:00 PM - 2:00 PM',
  '2:00 PM - 4:00 PM',
  '4:00 PM - 6:00 PM',
  '6:00 PM - 8:00 PM'
];

function CreateComplaint({ currentUser }) {
  const navigate = useNavigate();
  const location = useLocation();
  const fromAiState = location.state || {};

  const [form, setForm] = useState({
    messageType: 'GRIEVANCE',
    category: fromAiState.category || 'PLUMBING',
    subCategory: '',
    specificCategory: '',
    block: '',
    subBlock: '',
    roomType: '',
    roomNo: '',
    contactNo: '',
    availabilityDate: '',
    timeSlot: '',
    description: fromAiState.description || '',
  });
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [image, setImage] = useState(null);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm({
      ...form,
      [name]: value,
      // Reset sub-category when category changes
      ...(name === 'category' && { subCategory: '', specificCategory: '' })
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    if (!form.description.trim()) {
      setError('Description is required.');
      return;
    }
    setSubmitting(true);
    try {
      const payload = {
        ...form,
        userId: currentUser.userId,
      };
      await createComplaint(payload, currentUser, image);
      navigate('/dashboard');
    } catch (err) {
      setError('Failed to submit complaint: ' + (err.response?.data || err.message));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="create-complaint">
      <h2>Raise a New Complaint</h2>
      {fromAiState.fromAi && (
        <div className="info-banner">
          We have pre-filled the description (and suggested category) from your AI assistant message. Please review and complete the remaining details.
        </div>
      )}
      {error && <div className="error">{error}</div>}
      <form onSubmit={handleSubmit}>
        <div className="form-row">
          <label>Message Type *
            <select name="messageType" value={form.messageType} onChange={handleChange} required>
              {MESSAGE_TYPES.map((t) => (
                <option key={t} value={t}>{t.replace(/_/g, ' ')}</option>
              ))}
            </select>
          </label>

          <label>Category *
            <select name="category" value={form.category} onChange={handleChange} required>
              {CATEGORIES.map((c) => (
                <option key={c} value={c}>{c}</option>
              ))}
            </select>
          </label>
        </div>

        <div className="form-row">
          <label>Sub Category
            <select name="subCategory" value={form.subCategory} onChange={handleChange}>
              <option value="">-- Select Sub Category --</option>
              {SUB_CATEGORIES[form.category]?.map((sub) => (
                <option key={sub} value={sub}>{sub}</option>
              ))}
            </select>
          </label>

          <label>Specific Issue
            <input 
              name="specificCategory" 
              value={form.specificCategory} 
              onChange={handleChange} 
              placeholder="e.g., Bathroom Shower" 
            />
          </label>
        </div>

        <div className="form-row">
          <label>Block
            <input 
              name="block" 
              value={form.block} 
              onChange={handleChange} 
              placeholder="e.g., A, B, C" 
            />
          </label>

          <label>Sub Block
            <input 
              name="subBlock" 
              value={form.subBlock} 
              onChange={handleChange} 
              placeholder="e.g., A1, A2" 
            />
          </label>
        </div>

        <div className="form-row">
          <label>Room Type
            <select name="roomType" value={form.roomType} onChange={handleChange}>
              <option value="">-- Select Room Type --</option>
              <option value="Single">Single</option>
              <option value="Double">Double</option>
              <option value="Triple">Triple</option>
              <option value="Four Seater">Four Seater</option>
            </select>
          </label>

          <label>Room Number
            <input 
              name="roomNo" 
              value={form.roomNo} 
              onChange={handleChange} 
              placeholder="e.g., 101, 202" 
            />
          </label>
        </div>

        <div className="form-row">
          <label>Contact Number
            <input 
              type="tel"
              name="contactNo" 
              value={form.contactNo} 
              onChange={handleChange} 
              placeholder="Your contact number" 
            />
          </label>

          <label>Availability Date
            <input 
              type="date"
              name="availabilityDate" 
              value={form.availabilityDate} 
              onChange={handleChange}
              min={new Date().toISOString().split('T')[0]}
            />
          </label>
        </div>

        <label>Preferred Time Slot
          <select name="timeSlot" value={form.timeSlot} onChange={handleChange}>
            <option value="">-- Select Time Slot --</option>
            {TIME_SLOTS.map((slot) => (
              <option key={slot} value={slot}>{slot}</option>
            ))}
          </select>
        </label>

        <label>Description *
          <textarea
            name="description"
            value={form.description}
            onChange={handleChange}
            rows={5}
            placeholder="Describe the issue in detail..."
            required
          />
        </label>

        <label>Attach Image (optional)
          <input
            type="file"
            accept="image/*"
            onChange={(e) => setImage(e.target.files[0] || null)}
          />
        </label>

        <div className="form-actions">
          <button type="button" className="btn-secondary" onClick={() => navigate('/dashboard')}>Cancel</button>
          <button type="submit" className="btn-primary" disabled={submitting}>
            {submitting ? 'Submitting...' : 'Submit Complaint'}
          </button>
        </div>
      </form>
    </div>
  );
}

export default CreateComplaint;

