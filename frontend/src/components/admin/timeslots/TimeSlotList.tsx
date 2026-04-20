import React, { useEffect, useState } from 'react';
import {
  Box,
  Container,
  Typography,
  Button,
  TextField,
  MenuItem,
  Alert,
  Chip,
  IconButton,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import { adminService } from '../../../services/adminService';
import { AdminTimeSlot } from '../../../types/admin';
import { FutsalGround } from '../../../types';
import { DataTable, Column, ConfirmDialog } from '../common';
import TimeSlotForm from './TimeSlotForm';

const TimeSlotList: React.FC = () => {
  const [timeSlots, setTimeSlots] = useState<AdminTimeSlot[]>([]);
  const [grounds, setGrounds] = useState<FutsalGround[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [groundFilter, setGroundFilter] = useState<string>('');
  const [bookedFilter, setBookedFilter] = useState<string>('');
  const [formOpen, setFormOpen] = useState(false);
  const [selectedSlot, setSelectedSlot] = useState<AdminTimeSlot | null>(null);
  const [deleteDialog, setDeleteDialog] = useState<{ open: boolean; id: string | null }>({
    open: false,
    id: null,
  });

  const fetchTimeSlots = async () => {
    setLoading(true);
    setError(null);
    try {
      const filters = {
        groundId: groundFilter || undefined,
        isBooked: bookedFilter ? bookedFilter === 'true' : undefined,
      };
      const data = await adminService.getAllTimeSlots(filters);
      setTimeSlots(data);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to fetch time slots');
    } finally {
      setLoading(false);
    }
  };

  const fetchGrounds = async () => {
    try {
      const data = await adminService.getAllGrounds();
      setGrounds(data);
    } catch (err: any) {
      console.error('Failed to fetch grounds:', err);
    }
  };

  useEffect(() => {
    fetchTimeSlots();
    fetchGrounds();
  }, [groundFilter, bookedFilter]);

  const handleCreate = () => {
    setSelectedSlot(null);
    setFormOpen(true);
  };

  const handleEdit = (slot: AdminTimeSlot) => {
    setSelectedSlot(slot);
    setFormOpen(true);
  };

  const handleFormClose = () => {
    setFormOpen(false);
    setSelectedSlot(null);
  };

  const handleFormSuccess = () => {
    setFormOpen(false);
    setSelectedSlot(null);
    setSuccess(selectedSlot ? 'Time slot updated successfully' : 'Time slot created successfully');
    fetchTimeSlots();
    setTimeout(() => setSuccess(null), 3000);
  };

  const handleDeleteClick = (id: string) => {
    setDeleteDialog({ open: true, id });
  };

  const handleDeleteConfirm = async () => {
    if (!deleteDialog.id) return;

    try {
      await adminService.deleteTimeSlot(deleteDialog.id);
      setSuccess('Time slot deleted successfully');
      fetchTimeSlots();
      setTimeout(() => setSuccess(null), 3000);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to delete time slot');
    } finally {
      setDeleteDialog({ open: false, id: null });
    }
  };

  const columns: Column<AdminTimeSlot>[] = [
    {
      id: 'groundName',
      label: 'Ground',
      minWidth: 150,
    },
    {
      id: 'companyName',
      label: 'Company',
      minWidth: 150,
    },
    {
      id: 'startTime',
      label: 'Start Time',
      minWidth: 150,
      format: (value) => new Date(value).toLocaleString(),
    },
    {
      id: 'endTime',
      label: 'End Time',
      minWidth: 150,
      format: (value) => new Date(value).toLocaleString(),
    },
    {
      id: 'isBooked',
      label: 'Status',
      minWidth: 100,
      format: (value) => (
        <Chip
          label={value ? 'Booked' : 'Available'}
          color={value ? 'error' : 'success'}
          size="small"
        />
      ),
    },
    {
      id: 'actions',
      label: 'Actions',
      minWidth: 120,
      align: 'center',
      format: (_, row) => (
        <Box display="flex" gap={1} justifyContent="center">
          <IconButton size="small" color="primary" onClick={() => handleEdit(row)}>
            <EditIcon fontSize="small" />
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
      <Box mb={4} display="flex" justifyContent="space-between" alignItems="center">
        <Box>
          <Typography variant="h4" component="h1" gutterBottom fontWeight="bold">
            Time Slots
          </Typography>
          <Typography variant="body1" color="text.secondary">
            Manage time slots for all futsal grounds
          </Typography>
        </Box>
        <Button variant="contained" startIcon={<AddIcon />} onClick={handleCreate}>
          Create Time Slot
        </Button>
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

      <Box mb={3} display="flex" gap={2}>
        <TextField
          select
          label="Filter by Ground"
          value={groundFilter}
          onChange={(e) => setGroundFilter(e.target.value)}
          size="small"
          sx={{ minWidth: 200 }}
        >
          <MenuItem value="">All Grounds</MenuItem>
          {grounds.map((ground) => (
            <MenuItem key={ground.id} value={ground.id}>
              {ground.name}
            </MenuItem>
          ))}
        </TextField>

        <TextField
          select
          label="Filter by Status"
          value={bookedFilter}
          onChange={(e) => setBookedFilter(e.target.value)}
          size="small"
          sx={{ minWidth: 150 }}
        >
          <MenuItem value="">All</MenuItem>
          <MenuItem value="false">Available</MenuItem>
          <MenuItem value="true">Booked</MenuItem>
        </TextField>
      </Box>

      <DataTable
        columns={columns}
        data={timeSlots}
        loading={loading}
        emptyMessage="No time slots found"
      />

      <TimeSlotForm
        open={formOpen}
        slot={selectedSlot}
        onClose={handleFormClose}
        onSuccess={handleFormSuccess}
      />

      <ConfirmDialog
        open={deleteDialog.open}
        title="Delete Time Slot"
        message="Are you sure you want to delete this time slot? This action cannot be undone."
        confirmText="Delete"
        confirmColor="error"
        onConfirm={handleDeleteConfirm}
        onCancel={() => setDeleteDialog({ open: false, id: null })}
      />
    </Container>
  );
};

export default TimeSlotList;
