import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Navbar from './components/Navbar';
import Dashboard from './pages/Dashboard';
import CreateComplaint from './pages/CreateComplaint';
import ComplaintDetails from './pages/ComplaintDetails';
import Login from './pages/Login';
import Signup from './pages/Signup';
import AiAssistant from './pages/AiAssistant';
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

  // currentUser object passed down to pages
  const currentUser = isAuthenticated
    ? {
        username,
        credentials: authService.getStoredCredentials(),
        ...( authService.getUserInfo() || {} )
      }
    : null;

  return (
    <Router>
      <Navbar currentUser={currentUser} onLogout={handleLogout} />
      <div className="app-container">
        <Routes>
          <Route path="/login" element={
            isAuthenticated ? <Navigate to="/dashboard" /> : <Login />
          } />
          <Route path="/signup" element={
            isAuthenticated ? <Navigate to="/dashboard" /> : <Signup />
          } />
          <Route path="/dashboard" element={
            isAuthenticated ? <Dashboard currentUser={currentUser} /> : <Navigate to="/login" />
          } />
          <Route path="/ai-assistant" element={
            isAuthenticated ? <AiAssistant currentUser={currentUser} /> : <Navigate to="/login" />
          } />
          <Route path="/complaint/new" element={
            isAuthenticated ? <CreateComplaint currentUser={currentUser} /> : <Navigate to="/login" />
          } />
          <Route path="/complaint/:id" element={
            isAuthenticated ? <ComplaintDetails currentUser={currentUser} /> : <Navigate to="/login" />
          } />
          <Route path="/" element={<Navigate to="/dashboard" />} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;
