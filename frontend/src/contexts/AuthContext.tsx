import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { User, AuthContextType, RegisterRequest } from '../types';
import { authService } from '../services/authService';

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    // Check if user is already logged in
    const storedToken = localStorage.getItem('token');
    const storedUser = localStorage.getItem('user');

    if (storedToken && storedUser) {
      setToken(storedToken);
      setUser(JSON.parse(storedUser));
    }
    setIsLoading(false);
  }, []);

  const login = async (email: string, password: string) => {
    try {
      const response = await authService.login({ email, password });
      setToken(response.token);

      // Create user object from login response
      const userData: User = {
        id: '', // Will be populated from backend if needed
        name: email.split('@')[0], // Temporary name
        email: response.email,
        role: response.role,
        createdAt: new Date().toISOString(),
      };

      setUser(userData);
      localStorage.setItem('token', response.token);
      localStorage.setItem('user', JSON.stringify(userData));
    } catch (error) {
      console.error('Login failed:', error);
      throw error;
    }
  };

  const register = async (data: RegisterRequest) => {
    try {
      const response = await authService.register(data);
      setToken(response.token);

      // Create user object from register response
      const userData: User = {
        id: '', // Will be populated from backend if needed
        name: data.name,
        email: response.email,
        phoneNumber: data.phoneNumber,
        role: response.role,
        createdAt: new Date().toISOString(),
      };

      setUser(userData);
      localStorage.setItem('token', response.token);
      localStorage.setItem('user', JSON.stringify(userData));
    } catch (error) {
      console.error('Registration failed:', error);
      throw error;
    }
  };

  const logout = () => {
    authService.logout();
    setUser(null);
    setToken(null);
  };

  const value: AuthContextType = {
    user,
    token,
    login,
    register,
    logout,
    isAuthenticated: !!token,
    isLoading,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};
