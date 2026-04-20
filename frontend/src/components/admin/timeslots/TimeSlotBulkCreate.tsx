import React, { useEffect, useState } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  TextField,
  MenuItem,
  Box,
  Typography,
  Alert,
  CircularProgress,
  Chip,
} from '@mui/material';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import dayjs, { Dayjs } from 'dayjs';
import { adminService } from '../../../services/adminService';
import { FutsalGround } from '../../../types';

interface TimeSlotBulkCreateProps {
  open: boolean;
  onClose: () => void;
  onSuccess: () => void;
}

interface TimeSlotTemplate {
  startHour: number;
  startMinute: number;
  durationHours: number;
}

interface TimeSlotBulkFormData {
  groundId: string;
  startDate: Dayjs;
  endDate: Dayjs;
  startHour: number;
  endHour: number;
  slotDuration: number;
}

const TimeSlotBulkCreate: React.FC<TimeSlotBulkCreateProps> = ({
  open,
  onClose,
  onSuccess,
}) => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [grounds, setGrounds] = useState<FutsalGround[]>([]);
  const [formData, setFormData] = useState<TimeSlotBulkFormData>({
    groundId: '',
    startDate: dayjs(),
    endDate: dayjs().add(7, 'day'),
    startHour: 6,
    endHour: 22,
    slotDuration: 1,
  });

  useEffect(() => {
    if (open) {
      fetchGrounds();
      setError(null);
      setSuccess(null);
    }
  }, [open]);

  const fetchGrounds = async () => {
    try {
      const data = await adminService.getAllGrounds();
      setGrounds(data);
    } catch (err) {
      console.error('Failed to fetch grounds:', err);
    }
  };

  const generateTimeSlots = (): TimeSlotTemplate[] => {
    const slots: TimeSlotTemplate[] = [];
    for (let hour = formData.startHour; hour < formData.endHour; hour += formData.slotDuration) {
      slots.push({
        startHour: hour,
        startMinute: 0,
        durationHours: formData.slotDuration,
      });
    }
    return slots;
  };

  const handleSubmit = async () => {
    setError(null);
    setSuccess(null);
    setLoading(true);

    try {
      const slots = generateTimeSlots();
      const days = Math.ceil(formData.endDate.diff(formData.startDate, 'day'));

      let created = 0;
      let failed = 0;

      for (let day = 0; day <= days; day++) {
        const date = formData.startDate.add(day, 'day');

        for (const slot of slots) {
          const startTime = date
            .hour(slot.startHour)
            .minute(slot.startMinute)
            .second(0)
            .millisecond(0);
          const endTime = startTime.add(slot.durationHours, 'hour');

          try {
            await adminService.createTimeSlot({
              groundId: formData.groundId,
              startTime: startTime.toISOString(),
              endTime: endTime.toISOString(),
            });
            created++;
          } catch (err) {
            failed++;
            console.error('Failed to create slot:', err);
          }
        }
      }

      setSuccess(`Successfully created ${created} time slots${failed > 0 ? `, ${failed} failed` : ''}`);
      setTimeout(() => {
        onSuccess();
        handleClose();
      }, 2000);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to create time slots');
    } finally {
      setLoading(false);
    }
  };

  const handleClose = () => {
    if (!loading) {
      onClose();
      setFormData({
        groundId: '',
        startDate: dayjs(),
        endDate: dayjs().add(7, 'day'),
        startHour: 6,
        endHour: 22,
        slotDuration: 1,
      });
      setError(null);
      setSuccess(null);
    }
  };

  const estimatedSlots =
    generateTimeSlots().length * (formData.endDate.diff(formData.startDate, 'day') + 1);

  return (
    <Dialog open={open} onClose={handleClose} maxWidth="sm" fullWidth>
      <DialogTitle>Bulk Create Time Slots</DialogTitle>
      <DialogContent>
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

        <Box display="flex" flexDirection="column" gap={2} mt={1}>
          <TextField
            select
            label="Ground"
            value={formData.groundId}
            onChange={(e) => setFormData({ ...formData, groundId: e.target.value })}
            required
            fullWidth
          >
            {grounds.map((ground) => (
              <MenuItem key={ground.id} value={ground.id}>
                {ground.name} - {ground.companyName}
              </MenuItem>
            ))}
          </TextField>

          <LocalizationProvider dateAdapter={AdapterDayjs}>
            <DatePicker
              label="Start Date"
              value={formData.startDate}
              onChange={(newValue) =>
                newValue && setFormData({ ...formData, startDate: newValue })
              }
              slotProps={{ textField: { required: true, fullWidth: true } }}
            />

            <DatePicker
              label="End Date"
              value={formData.endDate}
              onChange={(newValue) =>
                newValue && setFormData({ ...formData, endDate: newValue })
              }
              minDate={formData.startDate}
              slotProps={{ textField: { required: true, fullWidth: true } }}
            />
          </LocalizationProvider>

          <Box display="flex" gap={2}>
            <TextField
              type="number"
              label="Start Hour"
              value={formData.startHour}
              onChange={(e) =>
                setFormData({ ...formData, startHour: parseInt(e.target.value) })
              }
              InputProps={{ inputProps: { min: 0, max: 23 } }}
              required
              fullWidth
            />

            <TextField
              type="number"
              label="End Hour"
              value={formData.endHour}
              onChange={(e) =>
                setFormData({ ...formData, endHour: parseInt(e.target.value) })
              }
              InputProps={{ inputProps: { min: 1, max: 24 } }}
              required
              fullWidth
            />
          </Box>

          <TextField
            select
            label="Slot Duration (hours)"
            value={formData.slotDuration}
            onChange={(e) =>
              setFormData({ ...formData, slotDuration: parseInt(e.target.value) })
            }
            required
            fullWidth
          >
            <MenuItem value={1}>1 hour</MenuItem>
            <MenuItem value={2}>2 hours</MenuItem>
            <MenuItem value={3}>3 hours</MenuItem>
          </TextField>

          <Box
            sx={{
              backgroundColor: 'info.light',
              p: 2,
              borderRadius: 1,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
            }}
          >
            <Typography variant="body2" color="text.secondary">
              Estimated time slots to be created:
            </Typography>
            <Chip label={estimatedSlots} color="primary" />
          </Box>
        </Box>
      </DialogContent>
      <DialogActions>
        <Button onClick={handleClose} disabled={loading}>
          Cancel
        </Button>
        <Button
          onClick={handleSubmit}
          variant="contained"
          disabled={loading || !formData.groundId}
          startIcon={loading ? <CircularProgress size={16} /> : null}
        >
          Create Slots
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default TimeSlotBulkCreate;
