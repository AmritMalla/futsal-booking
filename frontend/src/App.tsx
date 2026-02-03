import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { ThemeProvider, createTheme, CssBaseline } from '@mui/material';
import { AuthProvider } from './contexts/AuthContext';
import Layout from './components/layout/Layout';
import PrivateRoute from './components/auth/PrivateRoute';
import Home from './pages/Home';
import Login from './components/auth/Login';
import Register from './components/auth/Register';
import GroundList from './components/grounds/GroundList';
import GroundDetails from './components/grounds/GroundDetails';
import CreateBooking from './components/booking/CreateBooking';
import MyBookings from './components/booking/MyBookings';
import PaymentForm from './components/payment/PaymentForm';
import PaymentHistory from './components/payment/PaymentHistory';
import OwnerDashboard from './components/dashboard/OwnerDashboard';
import ManageGrounds from './components/dashboard/ManageGrounds';
import { UserRole } from './types';

const theme = createTheme({
  palette: {
    primary: {
      main: '#667eea',
    },
    secondary: {
      main: '#764ba2',
    },
  },
  typography: {
    fontFamily: 'Roboto, Arial, sans-serif',
  },
});

function App() {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <AuthProvider>
        <Router>
          <Layout>
            <Routes>
              {/* Public Routes */}
              <Route path="/" element={<Home />} />
              <Route path="/login" element={<Login />} />
              <Route path="/register" element={<Register />} />
              <Route path="/grounds" element={<GroundList />} />
              <Route path="/grounds/:id" element={<GroundDetails />} />

              {/* Protected Routes - User */}
              <Route
                path="/my-bookings"
                element={
                  <PrivateRoute>
                    <MyBookings />
                  </PrivateRoute>
                }
              />
              <Route
                path="/booking/new"
                element={
                  <PrivateRoute>
                    <CreateBooking />
                  </PrivateRoute>
                }
              />
              <Route
                path="/payment"
                element={
                  <PrivateRoute>
                    <PaymentForm />
                  </PrivateRoute>
                }
              />
              <Route
                path="/payment-history"
                element={
                  <PrivateRoute>
                    <PaymentHistory />
                  </PrivateRoute>
                }
              />

              {/* Protected Routes - Owner */}
              <Route
                path="/owner/dashboard"
                element={
                  <PrivateRoute requiredRole={UserRole.OWNER}>
                    <OwnerDashboard />
                  </PrivateRoute>
                }
              />
              <Route
                path="/owner/grounds"
                element={
                  <PrivateRoute requiredRole={UserRole.OWNER}>
                    <ManageGrounds />
                  </PrivateRoute>
                }
              />

              {/* 404 Not Found */}
              <Route
                path="*"
                element={
                  <div style={{ padding: '2rem', textAlign: 'center' }}>
                    <h1>404 - Page Not Found</h1>
                    <p>The page you are looking for does not exist.</p>
                  </div>
                }
              />
            </Routes>
          </Layout>
        </Router>
      </AuthProvider>
    </ThemeProvider>
  );
}

export default App;
