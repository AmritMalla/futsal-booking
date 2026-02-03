import React, { useState, useEffect } from 'react';
import {
  Container,
  Paper,
  Typography,
  Box,
  Grid,
  Button,
  IconButton,
  Chip,
  CircularProgress,
  Alert,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Tooltip,
  alpha,
} from '@mui/material';
import {
  ArrowBack as ArrowBackIcon,
  Add as AddIcon,
  Delete as DeleteIcon,
  Schedule as ScheduleIcon,
  CheckCircle as CheckCircleIcon,
  Cancel as CancelIcon,
  Refresh as RefreshIcon,
} from '@mui/icons-material';
import { useParams, useNavigate } from 'react-router-dom';
import { TimeSlot } from '../../types';
import { slotService } from '../../services/slotService';
import { groundService } from '../../services/groundService';
import { colors } from '../../theme/theme';
import { format, addDays, startOfDay, isSameDay } from 'date-fns';

const ManageTimeSlots: React.FC = () => {
  const { groundId } = useParams<{ groundId: string }>();
  const navigate = useNavigate();

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [groundName, setGroundName] = useState('');
  const [slots, setSlots] = useState<TimeSlot[]>([]);
  const [selectedDate, setSelectedDate] = useState(startOfDay(new Date()));

  // Create slots dialog
  const [createDialog, setCreateDialog] = useState(false);
  const [createDate, setCreateDate] = useState(format(new Date(), 'yyyy-MM-dd'));
  const [startHour, setStartHour] = useState(6);
  const [endHour, setEndHour] = useState(22);
  const [creating, setCreating] = useState(false);

  // Available dates (next 7 days)
  const availableDates = Array.from({ length: 7 }, (_, i) => addDays(new Date(), i));

  useEffect(() => {
    if (groundId) {
      fetchGroundDetails();
      fetchSlots();
    }
  }, [groundId]);

  useEffect(() => {
    if (groundId) {
      fetchSlots();
    }
  }, [selectedDate]);

  const fetchGroundDetails = async () => {
    try {
      const ground = await groundService.getGroundById(groundId!);
      setGroundName(ground.name);
    } catch (err: any) {
      console.error('Failed to fetch ground:', err);
    }
  };

  const fetchSlots = async () => {
    try {
      setLoading(true);
      const allSlots = await slotService.getTimeSlotsByGround(groundId!);
      // Filter slots for selected date
      const filteredSlots = allSlots.filter(slot =>
        isSameDay(new Date(slot.startTime), selectedDate)
      );
      // Sort by start time
      filteredSlots.sort((a, b) => new Date(a.startTime).getTime() - new Date(b.startTime).getTime());
      setSlots(filteredSlots);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load time slots');
    } finally {
      setLoading(false);
    }
  };

  const handleCreateSlots = async () => {
    try {
      setCreating(true);
      setError('');

      const date = new Date(createDate);
      await slotService.createDailySlots(groundId!, date, startHour, endHour);

      setSuccess(`Created ${endHour - startHour} time slots for ${format(date, 'MMM d, yyyy')}`);
      setCreateDialog(false);

      // If created slots are for selected date, refresh
      if (isSameDay(date, selectedDate)) {
        fetchSlots();
      }
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to create time slots');
    } finally {
      setCreating(false);
    }
  };

  const handleDeleteSlot = async (slotId: string) => {
    try {
      await slotService.deleteTimeSlot(slotId);
      setSlots(slots.filter(s => s.id !== slotId));
      setSuccess('Time slot deleted');
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to delete slot');
    }
  };

  const getSlotStatus = (slot: TimeSlot) => {
    if (slot.isBooked) return 'booked';
    const now = new Date();
    const slotStart = new Date(slot.startTime);
    if (slotStart < now) return 'past';
    return 'available';
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'available':
        return colors.status.success;
      case 'booked':
        return colors.status.error;
      case 'past':
        return colors.neutral.gray;
      default:
        return colors.neutral.gray;
    }
  };

  const hours = Array.from({ length: 24 }, (_, i) => i);

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      {/* Header */}
      <Box sx={{ mb: 4 }}>
        <Button
          startIcon={<ArrowBackIcon />}
          onClick={() => navigate('/owner/grounds')}
          sx={{ mb: 2 }}
        >
          Back to Grounds
        </Button>
        <Box display="flex" justifyContent="space-between" alignItems="center" flexWrap="wrap" gap={2}>
          <Box>
            <Typography variant="h4" fontWeight={700} gutterBottom>
              Manage Time Slots
            </Typography>
            <Typography variant="body1" color="text.secondary">
              {groundName}
            </Typography>
          </Box>
          <Box display="flex" gap={2}>
            <Button
              variant="outlined"
              startIcon={<RefreshIcon />}
              onClick={fetchSlots}
            >
              Refresh
            </Button>
            <Button
              variant="contained"
              startIcon={<AddIcon />}
              onClick={() => setCreateDialog(true)}
            >
              Create Slots
            </Button>
          </Box>
        </Box>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 3 }} onClose={() => setError('')}>
          {error}
        </Alert>
      )}
      {success && (
        <Alert severity="success" sx={{ mb: 3 }} onClose={() => setSuccess('')}>
          {success}
        </Alert>
      )}

      {/* Date Selector */}
      <Paper sx={{ p: 2, mb: 3 }}>
        <Typography variant="subtitle2" color="text.secondary" gutterBottom>
          Select Date
        </Typography>
        <Box display="flex" gap={1} flexWrap="wrap">
          {availableDates.map((date) => (
            <Button
              key={date.toISOString()}
              variant={isSameDay(date, selectedDate) ? 'contained' : 'outlined'}
              onClick={() => setSelectedDate(startOfDay(date))}
              sx={{ minWidth: 100 }}
            >
              <Box textAlign="center">
                <Typography variant="caption" display="block">
                  {format(date, 'EEE')}
                </Typography>
                <Typography variant="body2" fontWeight={600}>
                  {format(date, 'MMM d')}
                </Typography>
              </Box>
            </Button>
          ))}
        </Box>
      </Paper>

      {/* Slots Grid */}
      <Paper sx={{ p: 3 }}>
        <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
          <Typography variant="h6">
            Time Slots for {format(selectedDate, 'EEEE, MMMM d, yyyy')}
          </Typography>
          <Box display="flex" gap={2}>
            <Chip
              icon={<CheckCircleIcon />}
              label="Available"
              sx={{ bgcolor: alpha(colors.status.success, 0.1), color: colors.status.success }}
            />
            <Chip
              icon={<CancelIcon />}
              label="Booked"
              sx={{ bgcolor: alpha(colors.status.error, 0.1), color: colors.status.error }}
            />
          </Box>
        </Box>

        {loading ? (
          <Box display="flex" justifyContent="center" py={4}>
            <CircularProgress />
          </Box>
        ) : slots.length === 0 ? (
          <Box textAlign="center" py={4}>
            <ScheduleIcon sx={{ fontSize: 60, color: colors.neutral.gray, mb: 2 }} />
            <Typography color="text.secondary" gutterBottom>
              No time slots created for this date
            </Typography>
            <Button
              variant="contained"
              startIcon={<AddIcon />}
              onClick={() => {
                setCreateDate(format(selectedDate, 'yyyy-MM-dd'));
                setCreateDialog(true);
              }}
              sx={{ mt: 2 }}
            >
              Create Slots for This Day
            </Button>
          </Box>
        ) : (
          <Grid container spacing={2}>
            {slots.map((slot) => {
              const status = getSlotStatus(slot);
              const statusColor = getStatusColor(status);

              return (
                <Grid item xs={6} sm={4} md={3} lg={2} key={slot.id}>
                  <Paper
                    elevation={0}
                    sx={{
                      p: 2,
                      textAlign: 'center',
                      border: `2px solid ${statusColor}`,
                      bgcolor: alpha(statusColor, 0.05),
                      position: 'relative',
                    }}
                  >
                    <Typography variant="h6" fontWeight={600} sx={{ color: statusColor }}>
                      {format(new Date(slot.startTime), 'HH:mm')}
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      to {format(new Date(slot.endTime), 'HH:mm')}
                    </Typography>
                    <Chip
                      label={status.charAt(0).toUpperCase() + status.slice(1)}
                      size="small"
                      sx={{
                        mt: 1,
                        bgcolor: alpha(statusColor, 0.1),
                        color: statusColor,
                        fontWeight: 600,
                      }}
                    />
                    {status === 'available' && (
                      <Tooltip title="Delete Slot">
                        <IconButton
                          size="small"
                          sx={{
                            position: 'absolute',
                            top: 4,
                            right: 4,
                          }}
                          onClick={() => handleDeleteSlot(slot.id)}
                        >
                          <DeleteIcon fontSize="small" />
                        </IconButton>
                      </Tooltip>
                    )}
                  </Paper>
                </Grid>
              );
            })}
          </Grid>
        )}
      </Paper>

      {/* Create Slots Dialog */}
      <Dialog open={createDialog} onClose={() => setCreateDialog(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Create Time Slots</DialogTitle>
        <DialogContent>
          <Box sx={{ pt: 1 }}>
            <TextField
              fullWidth
              label="Date"
              type="date"
              value={createDate}
              onChange={(e) => setCreateDate(e.target.value)}
              sx={{ mb: 3 }}
              InputLabelProps={{ shrink: true }}
            />
            <Grid container spacing={2}>
              <Grid item xs={6}>
                <FormControl fullWidth>
                  <InputLabel>Start Hour</InputLabel>
                  <Select
                    value={startHour}
                    label="Start Hour"
                    onChange={(e) => setStartHour(Number(e.target.value))}
                  >
                    {hours.map((hour) => (
                      <MenuItem key={hour} value={hour}>
                        {hour.toString().padStart(2, '0')}:00
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
              </Grid>
              <Grid item xs={6}>
                <FormControl fullWidth>
                  <InputLabel>End Hour</InputLabel>
                  <Select
                    value={endHour}
                    label="End Hour"
                    onChange={(e) => setEndHour(Number(e.target.value))}
                  >
                    {hours.map((hour) => (
                      <MenuItem key={hour} value={hour} disabled={hour <= startHour}>
                        {hour.toString().padStart(2, '0')}:00
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
              </Grid>
            </Grid>
            <Typography variant="body2" color="text.secondary" sx={{ mt: 2 }}>
              This will create {endHour - startHour} hourly slots from {startHour.toString().padStart(2, '0')}:00 to {endHour.toString().padStart(2, '0')}:00
            </Typography>
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setCreateDialog(false)}>Cancel</Button>
          <Button
            variant="contained"
            onClick={handleCreateSlots}
            disabled={creating || endHour <= startHour}
          >
            {creating ? 'Creating...' : 'Create Slots'}
          </Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default ManageTimeSlots;
