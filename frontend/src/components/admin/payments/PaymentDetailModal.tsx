import React, { useState } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Box,
  Typography,
  Divider,
  TextField,
  MenuItem,
  Alert,
  CircularProgress,
  Grid,
} from '@mui/material';
import { AdminPayment } from '../../../types/admin';
import { PaymentStatus } from '../../../types';
import { adminService } from '../../../services/adminService';
import PaymentStatusChip from './PaymentStatusChip';

interface PaymentDetailModalProps {
  open: boolean;
  payment: AdminPayment;
  onClose: () => void;
  onSuccess: () => void;
}

const PaymentDetailModal: React.FC<PaymentDetailModalProps> = ({
  open,
  payment,
  onClose,
  onSuccess,
}) => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [newStatus, setNewStatus] = useState<PaymentStatus>(payment.paymentStatus);

  const handleUpdateStatus = async () => {
    setError(null);
    setLoading(true);

    try {
      await adminService.updatePaymentStatus(payment.id, newStatus);
      onSuccess();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to update payment status');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>
        <Box display="flex" justifyContent="space-between" alignItems="center">
          <Typography variant="h6">Payment Details</Typography>
          <PaymentStatusChip status={payment.paymentStatus} size="medium" />
        </Box>
      </DialogTitle>

      <DialogContent dividers>
        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}

        <Grid container spacing={3}>
          <Grid item xs={12}>
            <Typography variant="subtitle2" color="text.secondary" gutterBottom>
              Payment Information
            </Typography>
            <Box display="flex" flexDirection="column" gap={1}>
              <Box display="flex" justifyContent="space-between">
                <Typography variant="body2" fontWeight="bold">
                  Payment ID:
                </Typography>
                <Typography variant="body2">{payment.id}</Typography>
              </Box>
              <Box display="flex" justifyContent="space-between">
                <Typography variant="body2" fontWeight="bold">
                  Transaction ID:
                </Typography>
                <Typography variant="body2" fontFamily="monospace">
                  {payment.transactionId}
                </Typography>
              </Box>
              <Box display="flex" justifyContent="space-between">
                <Typography variant="body2" fontWeight="bold">
                  Amount:
                </Typography>
                <Typography variant="h6" color="primary" fontWeight="bold">
                  NPR {payment.amount.toLocaleString()}
                </Typography>
              </Box>
              <Box display="flex" justifyContent="space-between">
                <Typography variant="body2" fontWeight="bold">
                  Payment Date:
                </Typography>
                <Typography variant="body2">
                  {new Date(payment.createdAt).toLocaleString()}
                </Typography>
              </Box>
            </Box>
          </Grid>

          <Grid item xs={12}>
            <Divider />
          </Grid>

          <Grid item xs={12} md={6}>
            <Typography variant="subtitle2" color="text.secondary" gutterBottom>
              Customer Details
            </Typography>
            <Box display="flex" flexDirection="column" gap={1}>
              <Box>
                <Typography variant="body2" fontWeight="bold">
                  Name:
                </Typography>
                <Typography variant="body2">{payment.userName}</Typography>
              </Box>
              <Box>
                <Typography variant="body2" fontWeight="bold">
                  Email:
                </Typography>
                <Typography variant="body2">{payment.userEmail}</Typography>
              </Box>
            </Box>
          </Grid>

          <Grid item xs={12} md={6}>
            <Typography variant="subtitle2" color="text.secondary" gutterBottom>
              Booking Details
            </Typography>
            <Box display="flex" flexDirection="column" gap={1}>
              <Box>
                <Typography variant="body2" fontWeight="bold">
                  Booking ID:
                </Typography>
                <Typography variant="body2">{payment.bookingId}</Typography>
              </Box>
              <Box>
                <Typography variant="body2" fontWeight="bold">
                  Ground:
                </Typography>
                <Typography variant="body2">{payment.groundName}</Typography>
              </Box>
            </Box>
          </Grid>

          <Grid item xs={12}>
            <Divider />
          </Grid>

          <Grid item xs={12}>
            <Typography variant="subtitle2" color="text.secondary" gutterBottom>
              Booking Time Slot
            </Typography>
            <Box display="flex" flexDirection="column" gap={1}>
              <Box display="flex" justifyContent="space-between">
                <Typography variant="body2" fontWeight="bold">
                  Start Time:
                </Typography>
                <Typography variant="body2">
                  {new Date(payment.slotStartTime).toLocaleString()}
                </Typography>
              </Box>
              <Box display="flex" justifyContent="space-between">
                <Typography variant="body2" fontWeight="bold">
                  End Time:
                </Typography>
                <Typography variant="body2">
                  {new Date(payment.slotEndTime).toLocaleString()}
                </Typography>
              </Box>
            </Box>
          </Grid>

          <Grid item xs={12}>
            <Divider />
          </Grid>

          <Grid item xs={12}>
            <Typography variant="subtitle2" color="text.secondary" gutterBottom>
              Update Status
            </Typography>
            <TextField
              select
              label="Payment Status"
              value={newStatus}
              onChange={(e) => setNewStatus(e.target.value as PaymentStatus)}
              fullWidth
              size="small"
            >
              <MenuItem value={PaymentStatus.SUCCESS}>Success</MenuItem>
              <MenuItem value={PaymentStatus.PENDING}>Pending</MenuItem>
              <MenuItem value={PaymentStatus.FAILED}>Failed</MenuItem>
              <MenuItem value={PaymentStatus.REFUNDED}>Refunded</MenuItem>
            </TextField>
            {newStatus === PaymentStatus.REFUNDED && (
              <Alert severity="warning" sx={{ mt: 2 }}>
                Changing status to REFUNDED will also cancel the associated booking and free up the
                time slot.
              </Alert>
            )}
          </Grid>
        </Grid>
      </DialogContent>

      <DialogActions>
        <Button onClick={onClose} disabled={loading}>
          Close
        </Button>
        <Button
          onClick={handleUpdateStatus}
          variant="contained"
          disabled={loading || newStatus === payment.paymentStatus}
          startIcon={loading ? <CircularProgress size={16} /> : null}
        >
          Update Status
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default PaymentDetailModal;
