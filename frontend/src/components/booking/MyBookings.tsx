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
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
} from '@mui/material';
import { useAuth } from '../../contexts/AuthContext';
import { bookingService } from '../../services/bookingService';
import { Booking, BookingStatus } from '../../types';

const MyBookings: React.FC = () => {
  const [bookings, setBookings] = useState<Booking[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [cancelDialogOpen, setCancelDialogOpen] = useState(false);
  const [selectedBooking, setSelectedBooking] = useState<Booking | null>(null);
  const { user } = useAuth();

  useEffect(() => {
    if (user) {
      fetchBookings();
    }
  }, [user]);

  const fetchBookings = async () => {
    if (!user) return;

    try {
      setLoading(true);
      const data = await bookingService.getBookingsByUser(user.id);
      setBookings(data);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load bookings');
    } finally {
      setLoading(false);
    }
  };

  const handleCancelClick = (booking: Booking) => {
    setSelectedBooking(booking);
    setCancelDialogOpen(true);
  };

  const handleCancelConfirm = async () => {
    if (!selectedBooking) return;

    try {
      await bookingService.cancelBooking(selectedBooking.id);
      setCancelDialogOpen(false);
      fetchBookings(); // Refresh the list
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to cancel booking');
    }
  };

  const getStatusColor = (status: BookingStatus) => {
    switch (status) {
      case BookingStatus.CONFIRMED:
        return 'success';
      case BookingStatus.CANCELLED:
        return 'error';
      case BookingStatus.COMPLETED:
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
        My Bookings
      </Typography>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      {bookings.length === 0 ? (
        <Paper sx={{ p: 3 }}>
          <Alert severity="info">You don't have any bookings yet.</Alert>
        </Paper>
      ) : (
        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Ground Name</TableCell>
                <TableCell>Date & Time</TableCell>
                <TableCell>Booking Date</TableCell>
                <TableCell>Status</TableCell>
                <TableCell>Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {bookings.map((booking) => (
                <TableRow key={booking.id}>
                  <TableCell>{booking.groundName}</TableCell>
                  <TableCell>
                    {new Date(booking.slotStartTime).toLocaleString()} -{' '}
                    {new Date(booking.slotEndTime).toLocaleTimeString()}
                  </TableCell>
                  <TableCell>{new Date(booking.bookingDate).toLocaleDateString()}</TableCell>
                  <TableCell>
                    <Chip label={booking.status} color={getStatusColor(booking.status)} size="small" />
                  </TableCell>
                  <TableCell>
                    {booking.status === BookingStatus.CONFIRMED && (
                      <Button
                        variant="outlined"
                        color="error"
                        size="small"
                        onClick={() => handleCancelClick(booking)}
                      >
                        Cancel
                      </Button>
                    )}
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      <Dialog open={cancelDialogOpen} onClose={() => setCancelDialogOpen(false)}>
        <DialogTitle>Cancel Booking</DialogTitle>
        <DialogContent>
          Are you sure you want to cancel this booking for {selectedBooking?.groundName}?
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setCancelDialogOpen(false)}>No</Button>
          <Button onClick={handleCancelConfirm} color="error" variant="contained">
            Yes, Cancel
          </Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default MyBookings;
