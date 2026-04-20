import React, { useEffect, useState } from 'react';
import {
  TextField,
  MenuItem,
  Alert,
  Box,
} from '@mui/material';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs';
import { DateTimePicker } from '@mui/x-date-pickers/DateTimePicker';
import dayjs from 'dayjs';
import { adminService } from '../../../services/adminService';
import { AdminTimeSlot } from '../../../types/admin';
import { FutsalGround } from '../../../types';
import { FormDialog } from '../common';

interface TimeSlotFormProps {
  open: boolean;
  slot: AdminTimeSlot | null;
  onClose: () => void;
  onSuccess: () => void;
}

const TimeSlotForm: React.FC<TimeSlotFormProps> = ({ open, slot, onClose, onSuccess }) => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [grounds, setGrounds] = useState<FutsalGround[]>([]);
  const [formData, setFormData] = useState({
    groundId: '',
    startTime: dayjs(),
    endTime: dayjs().add(1, 'hour'),
  });

  useEffect(() => {
    fetchGrounds();
  }, []);

  useEffect(() => {
    if (slot) {
      setFormData({
        groundId: slot.groundId,
        startTime: dayjs(slot.startTime),
        endTime: dayjs(slot.endTime),
      });
    } else {
      setFormData({
        groundId: '',
        startTime: dayjs(),
        endTime: dayjs().add(1, 'hour'),
      });
    }
    setError(null);
  }, [slot, open]);

  const fetchGrounds = async () => {
    try {
      const data = await adminService.getAllGrounds();
      setGrounds(data);
    } catch (err) {
      console.error('Failed to fetch grounds:', err);
    }
  };

  const handleSubmit = async () => {
    setError(null);
    setLoading(true);

    try {
      const request = {
        groundId: formData.groundId,
        startTime: formData.startTime.toISOString(),
        endTime: formData.endTime.toISOString(),
      };

      if (slot) {
        await adminService.updateTimeSlot(slot.id, request);
      } else {
        await adminService.createTimeSlot(request);
      }

      onSuccess();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to save time slot');
    } finally {
      setLoading(false);
    }
  };

  return (
    <FormDialog
      open={open}
      title={slot ? 'Edit Time Slot' : 'Create Time Slot'}
      loading={loading}
      onSubmit={handleSubmit}
      onCancel={onClose}
      disableSubmit={
        !formData.groundId || !formData.startTime.isBefore(formData.endTime)
      }
    >
      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      <Box display="flex" flexDirection="column" gap={2}>
        <TextField
          select
          label="Ground"
          value={formData.groundId}
          onChange={(e) => setFormData({ ...formData, groundId: e.target.value })}
          required
          fullWidth
          disabled={!!slot}
        >
          {grounds.map((ground) => (
            <MenuItem key={ground.id} value={ground.id}>
              {ground.name} - {ground.companyName}
            </MenuItem>
          ))}
        </TextField>

        <LocalizationProvider dateAdapter={AdapterDayjs}>
          <DateTimePicker
            label="Start Time"
            value={formData.startTime}
            onChange={(newValue) =>
              newValue && setFormData({ ...formData, startTime: newValue })
            }
            slotProps={{ textField: { required: true, fullWidth: true } }}
          />

          <DateTimePicker
            label="End Time"
            value={formData.endTime}
            onChange={(newValue) =>
              newValue && setFormData({ ...formData, endTime: newValue })
            }
            minDateTime={formData.startTime}
            slotProps={{ textField: { required: true, fullWidth: true } }}
          />
        </LocalizationProvider>

        {!formData.startTime.isBefore(formData.endTime) && (
          <Alert severity="warning">
            End time must be after start time
          </Alert>
        )}
      </Box>
    </FormDialog>
  );
};

export default TimeSlotForm;
