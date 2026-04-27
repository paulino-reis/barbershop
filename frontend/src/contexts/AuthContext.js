import React, { createContext, useContext, useState, useEffect } from 'react';
import axios from 'axios';

// Configure axios to always use JSON and base URL
axios.defaults.headers.common['Content-Type'] = 'application/json';

// Dynamic API URL based on current domain
const getApiBaseUrl = () => {
  const hostname = window.location.hostname;
  // If accessing via subdomain, use the same subdomain for API
  if (hostname !== 'localhost' && hostname !== '127.0.0.1') {
    return `http://${hostname}:8090`;
  }
  // Default to localhost for local development
  return import.meta.env.VITE_API_URL || 'http://localhost:8090';
};

axios.defaults.baseURL = getApiBaseUrl();

// Add interceptor to include token in all requests
axios.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Add interceptor to handle 401/403 errors
axios.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401 || error.response?.status === 403) {
      localStorage.removeItem('token');
      localStorage.removeItem('userName');
      localStorage.removeItem('userId');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

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
  const [currentHostname, setCurrentHostname] = useState(window.location.hostname);

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (token) {
      // Verify token validity
      axios.get('/api/usuarios/perfil')
        .then(response => {
          setUser(response.data);
        })
        .catch(() => {
          localStorage.removeItem('token');
          setUser(null);
        })
        .finally(() => {
          setLoading(false);
        });
    } else {
      setLoading(false);
    }
  }, []);

  // Monitor hostname changes to detect tenant slug changes
  useEffect(() => {
    const handleHostnameChange = () => {
      const newHostname = window.location.hostname;
      if (newHostname !== currentHostname) {
        // Slug changed, logout and redirect to login
        console.log('Hostname changed from', currentHostname, 'to', newHostname);
        localStorage.removeItem('token');
        localStorage.removeItem('userName');
        localStorage.removeItem('userId');
        setUser(null);
        setCurrentHostname(newHostname);
        window.location.href = '/login';
      }
    };

    // Check on visibility change (when user switches tabs/windows)
    document.addEventListener('visibilitychange', handleHostnameChange);

    return () => {
      document.removeEventListener('visibilitychange', handleHostnameChange);
    };
  }, [currentHostname]);

  const login = async (credentials) => {
    try {
      const response = await axios.post('/api/auth/login', credentials);
      const { token, id, login: username, nomeUsuario, role } = response.data;

      localStorage.setItem('token', token);
      localStorage.setItem('userName', nomeUsuario);
      localStorage.setItem('userId', id);

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
    localStorage.removeItem('userName');
    localStorage.removeItem('userId');
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
