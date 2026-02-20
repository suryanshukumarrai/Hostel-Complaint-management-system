import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Navbar from './components/Navbar';
import Dashboard from './pages/Dashboard';
import CreateComplaint from './pages/CreateComplaint';
import ComplaintDetails from './pages/ComplaintDetails';
import Login from './pages/Login';
import Signup from './pages/Signup';
import { authService } from './services/authService';

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(authService.isAuthenticated());
  const [username, setUsername] = useState(authService.getCurrentUsername());

  useEffect(() => {
    setIsAuthenticated(authService.isAuthenticated());
    setUsername(authService.getCurrentUsername());
  }, []);

  const handleLogout = () => {
    authService.logout();
    setIsAuthenticated(false);
    setUsername(null);
  };

  const currentUser = isAuthenticated
    ? {
        username,
        credentials: authService.getStoredCredentials(),
        ...( authService.getUserInfo() || {} )
      }
    : null;

  return (
    <Router future={{ v7_startTransition: true, v7_relativeSplatPath: true }}>
      <Navbar currentUser={currentUser} onLogout={handleLogout} />
      <div className="app-container">
        <Routes>
          <Route path="/login" element={
            isAuthenticated ? <Navigate to="/dashboard" replace /> : <Login />
          } />
          <Route path="/signup" element={
            isAuthenticated ? <Navigate to="/dashboard" replace /> : <Signup />
          } />
          <Route path="/dashboard" element={
            isAuthenticated ? <Dashboard currentUser={currentUser} /> : <Navigate to="/login" replace />
          } />
          <Route path="/complaint/new" element={
            isAuthenticated ? <CreateComplaint currentUser={currentUser} /> : <Navigate to="/login" replace />
          } />
          <Route path="/complaint/:id" element={
            isAuthenticated ? <ComplaintDetails currentUser={currentUser} /> : <Navigate to="/login" replace />
          } />
          <Route path="/" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;
