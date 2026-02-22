import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import './Navbar.css';

function Navbar({ currentUser, onLogout }) {
  const navigate = useNavigate();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  const handleLogout = () => {
    onLogout();
    navigate('/login');
    setMobileMenuOpen(false);
  };

  const closeMobileMenu = () => {
    setMobileMenuOpen(false);
  };

  return (
    <nav className="navbar">
      <div className="navbar-brand">
        <Link to="/dashboard">DormCare Elite</Link>
      </div>
      
      <button 
        className="navbar-toggle" 
        onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
        aria-label="Toggle menu"
      >
        <span></span>
        <span></span>
        <span></span>
      </button>

      <div className={`navbar-links ${mobileMenuOpen ? 'active' : ''}`}>
        {currentUser ? (
          <>
            <Link to="/dashboard" onClick={closeMobileMenu}>Dashboard</Link>
            <Link to="/statistics" onClick={closeMobileMenu}>Statistics</Link>
            <span className="navbar-user">
              {currentUser.username} ({currentUser.role})
            </span>
            <button className="btn-logout" onClick={handleLogout}>Logout</button>
          </>
        ) : (
          <>
            <Link to="/login" onClick={closeMobileMenu}>Login</Link>
            <Link to="/signup" onClick={closeMobileMenu}>Signup</Link>
          </>
        )}
      </div>
    </nav>
  );
}

export default Navbar;
