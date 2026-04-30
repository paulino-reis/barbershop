import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './contexts/AuthContext';
import { ThemeProvider } from './contexts/ThemeContext';
import LoginScreen from './pages/LoginScreen';
import Register from './pages/Register';
import ForgotPassword from './pages/ForgotPassword';
import ResetPassword from './pages/ResetPassword';
import Agendamento from './pages/Agendamento';
import AppointmentBooking from './pages/AppointmentBooking';
import Perfil from './pages/Perfil';
import Administrativo from './pages/Administrativo';
import AgendamentosHoje from './pages/AgendamentosHoje';
import UsuarioManagement from './pages/UsuarioManagement';
import ProtectedRoute from './components/ProtectedRoute';

function App() {
  return (
    <ThemeProvider>
      <AuthProvider>
        <Router>
          <div className="App">
            <Routes>
              <Route path="/login" element={<LoginScreen />} />
              <Route path="/register" element={<Register />} />
              <Route path="/forgot-password" element={<ForgotPassword />} />
              <Route path="/reset-password" element={<ResetPassword />} />
              <Route 
                path="/agendamento" 
                element={
                  <ProtectedRoute>
                    <Agendamento />
                  </ProtectedRoute>
                } 
              />
              <Route 
                path="/booking" 
                element={
                  <ProtectedRoute>
                    <AppointmentBooking />
                  </ProtectedRoute>
                } 
              />
              <Route 
                path="/perfil" 
                element={
                  <ProtectedRoute>
                    <Perfil />
                  </ProtectedRoute>
                } 
              />
              <Route 
                path="/administrativo" 
                element={
                  <ProtectedRoute>
                    <Administrativo />
                  </ProtectedRoute>
                } 
              />
              <Route 
                path="/agendamentos-hoje" 
                element={
                  <ProtectedRoute>
                    <AgendamentosHoje />
                  </ProtectedRoute>
                } 
              />
              <Route 
                path="/usuarios" 
                element={
                  <ProtectedRoute>
                    <UsuarioManagement />
                  </ProtectedRoute>
                } 
              />
              <Route path="/" element={<Navigate to="/agendamento" />} />
            </Routes>
          </div>
        </Router>
      </AuthProvider>
    </ThemeProvider>
  );
}

export default App;
