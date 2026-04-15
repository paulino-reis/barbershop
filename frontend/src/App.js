import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './contexts/AuthContext';
import LoginScreen from './pages/LoginScreen';
import Register from './pages/Register';
import Agendamento from './pages/Agendamento';
import ServicesSelection from './pages/ServicesSelection';
import AppointmentBooking from './pages/AppointmentBooking';
import Perfil from './pages/Perfil';
import Administrativo from './pages/Administrativo';
import AgendamentosHoje from './pages/AgendamentosHoje';
import UsuarioManagement from './pages/UsuarioManagement';
import ProtectedRoute from './components/ProtectedRoute';

function App() {
  return (
    <AuthProvider>
      <Router>
        <div className="App">
          <Routes>
            <Route path="/login" element={<LoginScreen />} />
            <Route path="/register" element={<Register />} />
            <Route 
              path="/servicos" 
              element={
                <ProtectedRoute>
                  <ServicesSelection />
                </ProtectedRoute>
              } 
            />
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
            <Route path="/" element={<Navigate to="/servicos" />} />
          </Routes>
        </div>
      </Router>
    </AuthProvider>
  );
}

export default App;
