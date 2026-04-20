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
  Chip,
} from '@mui/material';
import { AdminBooking } from '../../../types/admin';
import { BookingStatus } from '../../../types';
import { adminService } from '../../../services/adminService';
import BookingStatusChip from './BookingStatusChip';

interface BookingDetailModalProps {
  open: boolean;
  booking: AdminBooking;
  onClose: () => void;
  onSuccess: () => void;
}

const BookingDetailModal: React.FC<BookingDetailModalProps> = ({
  open,
  booking,
  onClose,
  onSuccess,
}) => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [newStatus, setNewStatus] = useState<BookingStatus>(booking.status);

  const handleUpdateStatus = async () => {
    setError(null);
    setLoading(true);

    try {
      await adminService.updateBookingStatus(booking.id, newStatus);
      onSuccess();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to update booking status');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>
        <Box display="flex" justifyContent="space-between" alignItems="center">
          <Typography variant="h6">Booking Details</Typography>
          <BookingStatusChip status={booking.status} size="medium" />
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
              Booking Information
            </Typography>
            <Box display="flex" flexDirection="column" gap={1}>
              <Box display="flex" justifyContent="space-between">
                <Typography variant="body2" fontWeight="bold">
                  Booking ID:
                </Typography>
                <Typography variant="body2">{booking.id}</Typography>
              </Box>
              <Box display="flex" justifyContent="space-between">
                <Typography variant="body2" fontWeight="bold">
                  Booking Date:
                </Typography>
                <Typography variant="body2">
                  {new Date(booking.bookingDate).toLocaleString()}
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
                <Typography variant="body2">{booking.userName}</Typography>
              </Box>
              <Box>
                <Typography variant="body2" fontWeight="bold">
                  Email:
                </Typography>
                <Typography variant="body2">{booking.userEmail}</Typography>
              </Box>
            </Box>
          </Grid>

          <Grid item xs={12} md={6}>
            <Typography variant="subtitle2" color="text.secondary" gutterBottom>
              Ground Details
            </Typography>
            <Box display="flex" flexDirection="column" gap={1}>
              <Box>
                <Typography variant="body2" fontWeight="bold">
                  Ground:
                </Typography>
                <Typography variant="body2">{booking.groundName}</Typography>
              </Box>
              <Box>
                <Typography variant="body2" fontWeight="bold">
                  Company:
                </Typography>
                <Typography variant="body2">{booking.companyName}</Typography>
              </Box>
            </Box>
          </Grid>

          <Grid item xs={12}>
            <Divider />
          </Grid>

          <Grid item xs={12}>
            <Typography variant="subtitle2" color="text.secondary" gutterBottom>
              Time Slot
            </Typography>
            <Box display="flex" flexDirection="column" gap={1}>
              <Box display="flex" justifyContent="space-between">
                <Typography variant="body2" fontWeight="bold">
                  Start Time:
                </Typography>
                <Typography variant="body2">
                  {new Date(booking.slotStartTime).toLocaleString()}
                </Typography>
              </Box>
              <Box display="flex" justifyContent="space-between">
                <Typography variant="body2" fontWeight="bold">
                  End Time:
                </Typography>
                <Typography variant="body2">
                  {new Date(booking.slotEndTime).toLocaleString()}
                </Typography>
              </Box>
            </Box>
          </Grid>

          {booking.paymentStatus && (
            <>
              <Grid item xs={12}>
                <Divider />
              </Grid>

              <Grid item xs={12}>
                <Typography variant="subtitle2" color="text.secondary" gutterBottom>
                  Payment Information
                </Typography>
                <Box display="flex" flexDirection="column" gap={1}>
                  <Box display="flex" justifyContent="space-between" alignItems="center">
                    <Typography variant="body2" fontWeight="bold">
                      Payment Status:
                    </Typography>
                    <Chip
                      label={booking.paymentStatus}
                      color={
                        booking.paymentStatus === 'SUCCESS'
                          ? 'success'
                          : booking.paymentStatus === 'PENDING'
                          ? 'warning'
                          : 'error'
                      }
                      size="small"
                    />
                  </Box>
                  {booking.paymentAmount && (
                    <Box display="flex" justifyContent="space-between">
                      <Typography variant="body2" fontWeight="bold">
                        Amount:
                      </Typography>
                      <Typography variant="body2" color="primary" fontWeight="bold">
                        NPR {booking.paymentAmount.toLocaleString()}
                      </Typography>
                    </Box>
                  )}
                  {booking.transactionId && (
                    <Box display="flex" justifyContent="space-between">
                      <Typography variant="body2" fontWeight="bold">
                        Transaction ID:
                      </Typography>
                      <Typography variant="body2" fontFamily="monospace">
                        {booking.transactionId}
                      </Typography>
                    </Box>
                  )}
                </Box>
              </Grid>
            </>
          )}

          <Grid item xs={12}>
            <Divider />
          </Grid>

          <Grid item xs={12}>
            <Typography variant="subtitle2" color="text.secondary" gutterBottom>
              Update Status
            </Typography>
            <TextField
              select
              label="Booking Status"
              value={newStatus}
              onChange={(e) => setNewStatus(e.target.value as BookingStatus)}
              fullWidth
              size="small"
            >
              <MenuItem value={BookingStatus.CONFIRMED}>Confirmed</MenuItem>
              <MenuItem value={BookingStatus.CANCELLED}>Cancelled</MenuItem>
              <MenuItem value={BookingStatus.COMPLETED}>Completed</MenuItem>
            </TextField>
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
          disabled={loading || newStatus === booking.status}
          startIcon={loading ? <CircularProgress size={16} /> : null}
        >
          Update Status
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default BookingDetailModal;
