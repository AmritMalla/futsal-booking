import React, { useEffect, useState } from 'react';
import {
  Box,
  Container,
  Typography,
  TextField,
  MenuItem,
  Alert,
  IconButton,
} from '@mui/material';
import VisibilityIcon from '@mui/icons-material/Visibility';
import DeleteIcon from '@mui/icons-material/Delete';
import { adminService } from '../../../services/adminService';
import { AdminPayment } from '../../../types/admin';
import { PaymentStatus } from '../../../types';
import { DataTable, Column, ConfirmDialog } from '../common';
import PaymentStatusChip from './PaymentStatusChip';
import PaymentDetailModal from './PaymentDetailModal';

const PaymentList: React.FC = () => {
  const [payments, setPayments] = useState<AdminPayment[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [statusFilter, setStatusFilter] = useState<string>('');
  const [selectedPayment, setSelectedPayment] = useState<AdminPayment | null>(null);
  const [detailModalOpen, setDetailModalOpen] = useState(false);
  const [deleteDialog, setDeleteDialog] = useState<{ open: boolean; id: string | null }>({
    open: false,
    id: null,
  });

  const fetchPayments = async () => {
    setLoading(true);
    setError(null);
    try {
      const filters = {
        status: statusFilter ? (statusFilter as PaymentStatus) : undefined,
      };
      const data = await adminService.getAllPayments(filters);
      setPayments(data);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to fetch payments');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPayments();
  }, [statusFilter]);

  const handleViewDetails = (payment: AdminPayment) => {
    setSelectedPayment(payment);
    setDetailModalOpen(true);
  };

  const handleDetailModalClose = () => {
    setDetailModalOpen(false);
    setSelectedPayment(null);
  };

  const handleDetailModalSuccess = () => {
    setDetailModalOpen(false);
    setSelectedPayment(null);
    setSuccess('Payment updated successfully');
    fetchPayments();
    setTimeout(() => setSuccess(null), 3000);
  };

  const handleDeleteClick = (id: string) => {
    setDeleteDialog({ open: true, id });
  };

  const handleDeleteConfirm = async () => {
    if (!deleteDialog.id) return;

    try {
      await adminService.deletePayment(deleteDialog.id);
      setSuccess('Payment deleted successfully');
      fetchPayments();
      setTimeout(() => setSuccess(null), 3000);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to delete payment');
    } finally {
      setDeleteDialog({ open: false, id: null });
    }
  };

  const columns: Column<AdminPayment>[] = [
    {
      id: 'transactionId',
      label: 'Transaction ID',
      minWidth: 120,
      format: (value) => (
        <Typography variant="body2" fontFamily="monospace">
          {value}
        </Typography>
      ),
    },
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
    },
    {
      id: 'slotStartTime',
      label: 'Booking Time',
      minWidth: 180,
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
      id: 'amount',
      label: 'Amount',
      minWidth: 100,
      align: 'right',
      format: (value) => (
        <Typography variant="body2" fontWeight="bold" color="primary">
          NPR {value.toLocaleString()}
        </Typography>
      ),
    },
    {
      id: 'paymentStatus',
      label: 'Status',
      minWidth: 100,
      format: (value) => <PaymentStatusChip status={value} />,
    },
    {
      id: 'createdAt',
      label: 'Date',
      minWidth: 120,
      format: (value) => new Date(value).toLocaleDateString(),
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

  const totalRevenue = payments
    .filter((p) => p.paymentStatus === PaymentStatus.SUCCESS)
    .reduce((sum, p) => sum + p.amount, 0);

  return (
    <Container maxWidth="xl" sx={{ mt: 4, mb: 4 }}>
      <Box mb={4} display="flex" justifyContent="space-between" alignItems="center">
        <Box>
          <Typography variant="h4" component="h1" gutterBottom fontWeight="bold">
            Payments
          </Typography>
          <Typography variant="body1" color="text.secondary">
            Manage all payment transactions
          </Typography>
        </Box>
        <Box
          sx={{
            backgroundColor: 'success.light',
            color: 'success.contrastText',
            px: 3,
            py: 1.5,
            borderRadius: 2,
          }}
        >
          <Typography variant="caption">Total Revenue</Typography>
          <Typography variant="h5" fontWeight="bold">
            NPR {totalRevenue.toLocaleString()}
          </Typography>
        </Box>
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
          <MenuItem value={PaymentStatus.SUCCESS}>Success</MenuItem>
          <MenuItem value={PaymentStatus.PENDING}>Pending</MenuItem>
          <MenuItem value={PaymentStatus.FAILED}>Failed</MenuItem>
          <MenuItem value={PaymentStatus.REFUNDED}>Refunded</MenuItem>
        </TextField>
      </Box>

      <DataTable
        columns={columns}
        data={payments}
        loading={loading}
        emptyMessage="No payments found"
      />

      {selectedPayment && (
        <PaymentDetailModal
          open={detailModalOpen}
          payment={selectedPayment}
          onClose={handleDetailModalClose}
          onSuccess={handleDetailModalSuccess}
        />
      )}

      <ConfirmDialog
        open={deleteDialog.open}
        title="Delete Payment"
        message="Are you sure you want to delete this payment record? This action cannot be undone."
        confirmText="Delete"
        confirmColor="error"
        onConfirm={handleDeleteConfirm}
        onCancel={() => setDeleteDialog({ open: false, id: null })}
      />
    </Container>
  );
};

export default PaymentList;
