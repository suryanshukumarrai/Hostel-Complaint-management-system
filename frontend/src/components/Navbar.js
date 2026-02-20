import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import './Navbar.css';

function Navbar({ currentUser, onLogout }) {
  const navigate = useNavigate();

  const handleLogout = () => {
    onLogout();
    navigate('/login');
  };

  return (
    <nav className="navbar">
      <div className="navbar-brand">
        <Link to="/dashboard">🏢 DormCare Elite</Link>
      </div>
      <div className="navbar-links">
        {currentUser ? (
          <>
            <Link to="/dashboard">Dashboard</Link>
            <Link to="/complaint/new">New Complaint</Link>
            <span className="navbar-user">
              {currentUser.username} ({currentUser.role})
            </span>
            <button className="btn-logout" onClick={handleLogout}>Logout</button>
          </>
        ) : (
          <>
            <Link to="/login">Login</Link>
            <Link to="/signup">Signup</Link>
          </>
        )}
      </div>
    </nav>
  );
}

export default Navbar;
