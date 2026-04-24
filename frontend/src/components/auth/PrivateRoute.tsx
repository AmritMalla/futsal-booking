import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import { UserRole } from '../../types';
import { Box, CircularProgress } from '@mui/material';
import AccessDeniedPage from '../feedback/AccessDeniedPage';

interface PrivateRouteProps {
  children: React.ReactElement;
  requiredRole?: UserRole;
}

const PrivateRoute: React.FC<PrivateRouteProps> = ({ children, requiredRole }) => {
  const { isAuthenticated, user, isLoading } = useAuth();
  const location = useLocation();

  if (isLoading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="100vh">
        <CircularProgress />
      </Box>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location.pathname, reason: 'auth' }} />;
  }

  if (requiredRole && user?.role !== requiredRole && user?.role !== UserRole.ADMIN) {
    return <AccessDeniedPage />;
  }

  return children;
};

export default PrivateRoute;
