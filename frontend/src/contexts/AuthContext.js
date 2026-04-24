import React, { createContext, useContext, useState, useEffect } from 'react';
import axios from 'axios';

// Configure axios to always use JSON and base URL
axios.defaults.headers.common['Content-Type'] = 'application/json';
axios.defaults.baseURL = import.meta.env.VITE_API_URL;

const AuthContext = createContext();

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (token) {
      axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
      // Verify token validity
      axios.get('/api/usuarios/perfil')
        .then(response => {
          setUser(response.data);
        })
        .catch(() => {
          localStorage.removeItem('token');
          delete axios.defaults.headers.common['Authorization'];
        })
        .finally(() => {
          setLoading(false);
        });
    } else {
      setLoading(false);
    }
  }, []);

  const login = async (credentials) => {
    try {
      // Limpar header de Authorization antes de fazer login
      delete axios.defaults.headers.common['Authorization'];

      const response = await axios.post('/api/auth/login', credentials);
      const { token, id, login: username, nomeUsuario, role } = response.data;

      localStorage.setItem('token', token);
      localStorage.setItem('userName', nomeUsuario);
      localStorage.setItem('userId', id);
      axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;

      const userObj = { id, login: username, nome: nomeUsuario, role };
      setUser(userObj);
      return { success: true, user: userObj };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Erro ao fazer login'
      };
    }
  };

  const register = async (userData) => {
    try {
      const response = await axios.post('/api/auth/registrar', userData);
      const { token, id, login: username, nomeUsuario, role } = response.data;
      
      localStorage.setItem('token', token);
      localStorage.setItem('userName', nomeUsuario);
      localStorage.setItem('userId', id);
      axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
      
      setUser({ id, login: username, nome: nomeUsuario, role });
      return { success: true };
    } catch (error) {
      return { 
        success: false, 
        error: error.response?.data?.message || 'Erro ao registrar' 
      };
    }
  };

  const logout = () => {
    localStorage.removeItem('token');
    delete axios.defaults.headers.common['Authorization'];
    setUser(null);
  };

  const value = {
    user,
    login,
    register,
    logout,
    loading
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};
