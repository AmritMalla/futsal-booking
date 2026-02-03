import React, { useEffect, useState } from 'react';
import {
  Container,
  Paper,
  Typography,
  Box,
  CircularProgress,
  Alert,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Chip,
} from '@mui/material';
import { useAuth } from '../../contexts/AuthContext';
import { paymentService } from '../../services/paymentService';
import { Payment, PaymentStatus } from '../../types';

const PaymentHistory: React.FC = () => {
  const [payments, setPayments] = useState<Payment[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const { user } = useAuth();

  useEffect(() => {
    if (user) {
      fetchPayments();
    }
  }, [user]);

  const fetchPayments = async () => {
    if (!user) return;

    try {
      setLoading(true);
      const data = await paymentService.getPaymentsByUser(user.id);
      setPayments(data);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load payment history');
    } finally {
      setLoading(false);
    }
  };

  const getStatusColor = (status: PaymentStatus) => {
    switch (status) {
      case PaymentStatus.SUCCESS:
        return 'success';
      case PaymentStatus.FAILED:
        return 'error';
      case PaymentStatus.PENDING:
        return 'warning';
      case PaymentStatus.REFUNDED:
        return 'info';
      default:
        return 'default';
    }
  };

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="60vh">
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Container sx={{ mt: 4, mb: 4 }}>
      <Typography variant="h4" gutterBottom>
        Payment History
      </Typography>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      {payments.length === 0 ? (
        <Paper sx={{ p: 3 }}>
          <Alert severity="info">You don't have any payment records yet.</Alert>
        </Paper>
      ) : (
        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Transaction ID</TableCell>
                <TableCell>Booking ID</TableCell>
                <TableCell>Amount</TableCell>
                <TableCell>Status</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {payments.map((payment) => (
                <TableRow key={payment.id}>
                  <TableCell>{payment.transactionId}</TableCell>
                  <TableCell>{payment.bookingId.substring(0, 8)}...</TableCell>
                  <TableCell>NPR {payment.amount.toFixed(2)}</TableCell>
                  <TableCell>
                    <Chip
                      label={payment.paymentStatus}
                      color={getStatusColor(payment.paymentStatus)}
                      size="small"
                    />
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      )}
    </Container>
  );
};

export default PaymentHistory;
