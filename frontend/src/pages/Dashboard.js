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

  // FAQ state
  const [faqOpen, setFaqOpen] = useState(false);
  const [activeFaq, setActiveFaq] = useState(null);

  const navigate = useNavigate();
  const isAdmin = currentUser?.role === 'ADMIN';

  // ---------------- FAQ DATA ----------------
  const faqs = [
    {
      id: 1,
      question: "How much time does it take to solve a plumbing problem?",
      answer: "Minor plumbing issues are usually resolved within 24–48 hours. Major pipe or leakage problems may take up to 3 days."
    },
    {
      id: 2,
      question: "Do we need to pay for repair or replacement?",
      answer: "If damage is due to normal wear and tear, it is free. If caused intentionally or by negligence, charges may apply."
    },
    {
      id: 3,
      question: "How long does electrical complaint resolution take?",
      answer: "Electrical issues are prioritized and usually resolved within 24 hours."
    },
    {
      id: 4,
      question: "Can I track my complaint status?",
      answer: "Yes, go to Dashboard → My Complaints to track real-time status."
    },
    {
      id: 5,
      question: "Can I cancel a complaint after submission?",
      answer: "You cannot cancel it directly, but you can contact the warden or mark it resolved if fixed."
    },
    {
      id: 6,
      question: "What if my complaint is not resolved on time?",
      answer: "You can escalate it by contacting hostel administration."
    },
    {
      id: 7,
      question: "Will I be informed before maintenance staff visit?",
      answer: "Yes, staff usually coordinate with you before entering your room."
    },
    {
      id: 8,
      question: "What are common carpentry repair timelines?",
      answer: "Door or cupboard repairs are typically resolved within 48–72 hours."
    },
    {
      id: 9,
      question: "Can I reopen a resolved complaint?",
      answer: "Yes, if the issue persists, you may submit a new complaint referencing the previous one."
    },
    {
      id: 10,
      question: "Are emergency complaints handled faster?",
      answer: "Yes. Water leakage, power failure, and safety issues are treated as high priority."
    }
  ];
  // ------------------------------------------------

  useEffect(() => {
    const fetchData = async () => {
      try {
        const data = await getAllComplaints(currentUser);
        setComplaints(data);

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

      {/* ---------------- HEADER ---------------- */}
      <div className="dashboard-header">
        <h2>{isAdmin ? 'Admin Dashboard' : 'My Complaints'}</h2>
        <button className="btn-primary" onClick={() => navigate('/complaint/new')}>
          + New Complaint
        </button>
      </div>

      {/* ---------------- ADMIN STATS ---------------- */}
      {isAdmin && (
        <div className="admin-stats-section">
          {statsLoading ? (
            <div className="stats-loading">Loading statistics...</div>
          ) : stats ? (
            <>
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

      {/* ---------------- COMPLAINT LIST ---------------- */}
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

      {/* ---------------- FAQ FLOATING BUTTON ---------------- */}
      <div className="faq-button" onClick={() => setFaqOpen(!faqOpen)}>
        ?
      </div>

      {faqOpen && (
        <div className="faq-popup">
          <div className="faq-header">
            <h4>Frequently Asked Questions</h4>
            <span onClick={() => setFaqOpen(false)}>✖</span>
          </div>

          <div className="faq-content">
            {faqs.map((faq) => (
              <div key={faq.id} className="faq-item">
                <div
                  className="faq-question"
                  onClick={() =>
                    setActiveFaq(activeFaq === faq.id ? null : faq.id)
                  }
                >
                  {faq.question}
                </div>

                {activeFaq === faq.id && (
                  <div className="faq-answer">
                    {faq.answer}
                  </div>
                )}
              </div>
            ))}
          </div>

          <div className="faq-footer">
            <button onClick={() => navigate('/complaint/new')}>
              Still Need Help?
            </button>
          </div>
        </div>
      )}
    </div>
  );
}

export default Dashboard;