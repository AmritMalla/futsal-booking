import React, { useState, useEffect } from 'react';
import {
  Container,
  Paper,
  Typography,
  Box,
  Button,
  Alert,
  CircularProgress,
  TextField,
  Divider,
} from '@mui/material';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import { paymentService } from '../../services/paymentService';
import { bookingService } from '../../services/bookingService';
import { Booking } from '../../types';

const PaymentForm: React.FC = () => {
  const [searchParams] = useSearchParams();
  const bookingId = searchParams.get('bookingId');
  const [booking, setBooking] = useState<Booking | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [transactionId, setTransactionId] = useState('');
  const { user } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (bookingId) {
      fetchBooking(bookingId);
    }
  }, [bookingId]);

  const fetchBooking = async (id: string) => {
    try {
      setLoading(true);
      const data = await bookingService.getBookingById(id);
      setBooking(data);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load booking details');
    } finally {
      setLoading(false);
    }
  };

  const generateTransactionId = () => {
    return `TXN${Date.now()}${Math.floor(Math.random() * 1000)}`;
  };

  const handlePayment = async () => {
    if (!user || !booking) return;

    try {
      setLoading(true);
      setError('');

      const txnId = transactionId || generateTransactionId();

      const paymentData = {
        bookingId: booking.id,
        amount: 500, // This should come from ground price calculation
        transactionId: txnId,
      };

      await paymentService.createPayment(paymentData);
      setSuccess('Payment processed successfully!');

      setTimeout(() => {
        navigate('/my-bookings');
      }, 2000);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Payment failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  if (loading && !booking) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="60vh">
        <CircularProgress />
      </Box>
    );
  }

  if (!booking) {
    return (
      <Container sx={{ mt: 4 }}>
        <Alert severity="error">Booking not found</Alert>
      </Container>
    );
  }

  return (
    <Container maxWidth="sm" sx={{ mt: 4, mb: 4 }}>
      <Paper elevation={3} sx={{ p: 3 }}>
        <Typography variant="h4" gutterBottom>
          Complete Payment
        </Typography>

        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}

        {success && (
          <Alert severity="success" sx={{ mb: 2 }}>
            {success}
          </Alert>
        )}

        <Box sx={{ mb: 3 }}>
          <Typography variant="h6" gutterBottom>
            Booking Details
          </Typography>
          <Typography variant="body1">Ground: {booking.groundName}</Typography>
          <Typography variant="body1">
            Date & Time: {new Date(booking.slotStartTime).toLocaleString()}
          </Typography>
          <Typography variant="body1">
            Duration: {new Date(booking.slotStartTime).toLocaleTimeString()} -{' '}
            {new Date(booking.slotEndTime).toLocaleTimeString()}
          </Typography>
        </Box>

        <Divider sx={{ my: 2 }} />

        <Box sx={{ mb: 3 }}>
          <Typography variant="h6" gutterBottom>
            Payment Information
          </Typography>
          <Typography variant="h4" color="primary">
            NPR 500.00
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Total Amount
          </Typography>
        </Box>

        <TextField
          fullWidth
          label="Transaction ID (Optional)"
          variant="outlined"
          value={transactionId}
          onChange={(e) => setTransactionId(e.target.value)}
          helperText="Leave blank to auto-generate"
          sx={{ mb: 3 }}
        />

        <Alert severity="info" sx={{ mb: 3 }}>
          This is a demo payment form. In a production environment, integrate with payment gateways
          like Khalti, eSewa, or IME Pay for secure transactions.
        </Alert>

        <Button
          variant="contained"
          fullWidth
          size="large"
          onClick={handlePayment}
          disabled={loading || !!success}
        >
          {loading ? 'Processing...' : 'Pay Now'}
        </Button>

        <Button
          variant="outlined"
          fullWidth
          sx={{ mt: 2 }}
          onClick={() => navigate('/my-bookings')}
        >
          Cancel
        </Button>
      </Paper>
    </Container>
  );
};

export default PaymentForm;
