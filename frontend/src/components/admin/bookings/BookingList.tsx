import React, { useCallback, useEffect, useState } from 'react';
import {
  Box,
  Container,
  Typography,
  TextField,
  MenuItem,
  Alert,
  IconButton,
  Chip,
} from '@mui/material';
import VisibilityIcon from '@mui/icons-material/Visibility';
import DeleteIcon from '@mui/icons-material/Delete';
import { adminService } from '../../../services/adminService';
import { AdminBooking } from '../../../types/admin';
import { BookingStatus } from '../../../types';
import { DataTable, Column, ConfirmDialog } from '../common';
import BookingStatusChip from './BookingStatusChip';
import BookingDetailModal from './BookingDetailModal';

const BookingList: React.FC = () => {
  const [bookings, setBookings] = useState<AdminBooking[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [statusFilter, setStatusFilter] = useState<string>('');
  const [selectedBooking, setSelectedBooking] = useState<AdminBooking | null>(null);
  const [detailModalOpen, setDetailModalOpen] = useState(false);
  const [deleteDialog, setDeleteDialog] = useState<{ open: boolean; id: string | null }>({
    open: false,
    id: null,
  });

  const fetchBookings = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const filters = {
        status: statusFilter ? (statusFilter as BookingStatus) : undefined,
      };
      const data = await adminService.getAllBookings(filters);
      setBookings(data);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to fetch bookings');
    } finally {
      setLoading(false);
    }
  }, [statusFilter]);

  useEffect(() => {
    fetchBookings();
  }, [fetchBookings]);

  const handleViewDetails = (booking: AdminBooking) => {
    setSelectedBooking(booking);
    setDetailModalOpen(true);
  };

  const handleDetailModalClose = () => {
    setDetailModalOpen(false);
    setSelectedBooking(null);
  };

  const handleDetailModalSuccess = () => {
    setDetailModalOpen(false);
    setSelectedBooking(null);
    setSuccess('Booking updated successfully');
    fetchBookings();
    setTimeout(() => setSuccess(null), 3000);
  };

  const handleDeleteClick = (id: string) => {
    setDeleteDialog({ open: true, id });
  };

  const handleDeleteConfirm = async () => {
    if (!deleteDialog.id) return;

    try {
      await adminService.deleteBooking(deleteDialog.id);
      setSuccess('Booking deleted successfully');
      fetchBookings();
      setTimeout(() => setSuccess(null), 3000);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to delete booking');
    } finally {
      setDeleteDialog({ open: false, id: null });
    }
  };

  const columns: Column<AdminBooking>[] = [
    {
      id: 'userName',
      label: 'Customer',
      minWidth: 150,
      format: (_, row) => (
        <Box>
          <Typography variant="body2">{row.userName}</Typography>
          <Typography variant="caption" color="text.secondary">
            {row.userEmail}
          </Typography>
        </Box>
      ),
    },
    {
      id: 'groundName',
      label: 'Ground',
      minWidth: 150,
      format: (_, row) => (
        <Box>
          <Typography variant="body2">{row.groundName}</Typography>
          <Typography variant="caption" color="text.secondary">
            {row.companyName}
          </Typography>
        </Box>
      ),
    },
    {
      id: 'slotStartTime',
      label: 'Time Slot',
      minWidth: 200,
      format: (_, row) => (
        <Box>
          <Typography variant="body2">
            {new Date(row.slotStartTime).toLocaleString('en-US', {
              dateStyle: 'medium',
              timeStyle: 'short',
            })}
          </Typography>
          <Typography variant="caption" color="text.secondary">
            to {new Date(row.slotEndTime).toLocaleTimeString('en-US', { timeStyle: 'short' })}
          </Typography>
        </Box>
      ),
    },
    {
      id: 'bookingDate',
      label: 'Booking Date',
      minWidth: 150,
      format: (value) => new Date(value).toLocaleDateString(),
    },
    {
      id: 'status',
      label: 'Status',
      minWidth: 100,
      format: (value) => <BookingStatusChip status={value} />,
    },
    {
      id: 'paymentStatus',
      label: 'Payment',
      minWidth: 120,
      format: (value, row) => (
        <Box>
          {value ? (
            <>
              <Chip
                label={value}
                color={value === 'SUCCESS' ? 'success' : value === 'PENDING' ? 'warning' : 'error'}
                size="small"
              />
              {row.paymentAmount && (
                <Typography variant="caption" display="block" color="text.secondary">
                  NPR {row.paymentAmount}
                </Typography>
              )}
            </>
          ) : (
            <Typography variant="caption" color="text.secondary">
              No payment
            </Typography>
          )}
        </Box>
      ),
    },
    {
      id: 'actions',
      label: 'Actions',
      minWidth: 100,
      align: 'center',
      format: (_, row) => (
        <Box display="flex" gap={1} justifyContent="center">
          <IconButton
            size="small"
            color="primary"
            onClick={() => handleViewDetails(row)}
          >
            <VisibilityIcon fontSize="small" />
          </IconButton>
          <IconButton
            size="small"
            color="error"
            onClick={() => handleDeleteClick(row.id)}
          >
            <DeleteIcon fontSize="small" />
          </IconButton>
        </Box>
      ),
    },
  ];

  return (
    <Container maxWidth="xl" sx={{ mt: 4, mb: 4 }}>
      <Box mb={4}>
        <Typography variant="h4" component="h1" gutterBottom fontWeight="bold">
          Bookings
        </Typography>
        <Typography variant="body1" color="text.secondary">
          Manage all bookings across futsal grounds
        </Typography>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 3 }} onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      {success && (
        <Alert severity="success" sx={{ mb: 3 }} onClose={() => setSuccess(null)}>
          {success}
        </Alert>
      )}

      <Box mb={3}>
        <TextField
          select
          label="Filter by Status"
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value)}
          size="small"
          sx={{ minWidth: 200 }}
        >
          <MenuItem value="">All Statuses</MenuItem>
          <MenuItem value={BookingStatus.CONFIRMED}>Confirmed</MenuItem>
          <MenuItem value={BookingStatus.CANCELLED}>Cancelled</MenuItem>
          <MenuItem value={BookingStatus.COMPLETED}>Completed</MenuItem>
        </TextField>
      </Box>

      <DataTable
        columns={columns}
        data={bookings}
        loading={loading}
        emptyMessage="No bookings found"
      />

      {selectedBooking && (
        <BookingDetailModal
          open={detailModalOpen}
          booking={selectedBooking}
          onClose={handleDetailModalClose}
          onSuccess={handleDetailModalSuccess}
        />
      )}

      <ConfirmDialog
        open={deleteDialog.open}
        title="Delete Booking"
        message="Are you sure you want to delete this booking? This action cannot be undone."
        confirmText="Delete"
        confirmColor="error"
        onConfirm={handleDeleteConfirm}
        onCancel={() => setDeleteDialog({ open: false, id: null })}
      />
    </Container>
  );
};

export default BookingList;
