import React, { useCallback, useEffect, useMemo, useState } from 'react';
import {
  AlertTitle,
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
  DialogContentText,
  MenuItem,
  Stack,
  TextField,
  Divider,
} from '@mui/material';
import { useAuth } from '../../contexts/AuthContext';
import { bookingService } from '../../services/bookingService';
import { openMatchService } from '../../services/openMatchService';
import { Booking, BookingStatus, MatchSkillLevel, OpenMatch } from '../../types';
import OpenMatchCard from '../matches/OpenMatchCard';

const MyBookings: React.FC = () => {
  const [bookings, setBookings] = useState<Booking[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [cancelDialogOpen, setCancelDialogOpen] = useState(false);
  const [selectedBooking, setSelectedBooking] = useState<Booking | null>(null);
  const [myMatches, setMyMatches] = useState<OpenMatch[]>([]);
  const [matchDialogOpen, setMatchDialogOpen] = useState(false);
  const [matchActionLoadingId, setMatchActionLoadingId] = useState<string | null>(null);
  const [matchForm, setMatchForm] = useState({
    title: '',
    skillLevel: MatchSkillLevel.ANY,
    desiredPlayerCount: 10,
    notes: '',
  });
  const { user } = useAuth();

  const fetchBookings = useCallback(async () => {
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
  }, [user]);

  const fetchMyMatches = useCallback(async () => {
    if (!user) return;

    try {
      const data = await openMatchService.getMyOpenMatches();
      setMyMatches(data);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load open matches');
    }
  }, [user]);

  useEffect(() => {
    if (user) {
      fetchBookings();
      fetchMyMatches();
    }
  }, [fetchBookings, fetchMyMatches, user]);

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
      fetchMyMatches();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to cancel booking');
    }
  };

  const matchesByBookingId = useMemo(() => {
    return new Map(myMatches.map((match) => [match.bookingId, match]));
  }, [myMatches]);

  const handleOpenGameDialog = (booking: Booking) => {
    setSelectedBooking(booking);
    setMatchForm({
      title: `${booking.groundName} Pickup Game`,
      skillLevel: MatchSkillLevel.ANY,
      desiredPlayerCount: 10,
      notes: '',
    });
    setMatchDialogOpen(true);
  };

  const handleCreateOpenMatch = async () => {
    if (!selectedBooking) return;

    try {
      await openMatchService.createOpenMatch({
        bookingId: selectedBooking.id,
        title: matchForm.title,
        skillLevel: matchForm.skillLevel,
        desiredPlayerCount: matchForm.desiredPlayerCount,
        notes: matchForm.notes || undefined,
      });
      setMatchDialogOpen(false);
      setSelectedBooking(null);
      fetchMyMatches();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to publish open match');
    }
  };

  const handleCancelOpenMatch = async (matchId: string) => {
    try {
      setMatchActionLoadingId(matchId);
      await openMatchService.cancelOpenMatch(matchId);
      setMyMatches((currentMatches) => currentMatches.filter((match) => match.id !== matchId));
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to cancel open match');
    } finally {
      setMatchActionLoadingId(null);
    }
  };

  const handleLeaveOpenMatch = async (matchId: string) => {
    try {
      setMatchActionLoadingId(matchId);
      const updatedMatch = await openMatchService.leaveOpenMatch(matchId);
      setMyMatches((currentMatches) =>
        currentMatches.map((match) => (match.id === matchId ? updatedMatch : match))
      );
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to leave open match');
    } finally {
      setMatchActionLoadingId(null);
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
        <>
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
                      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1}>
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

                        {booking.status === BookingStatus.CONFIRMED && !matchesByBookingId.has(booking.id) && (
                          <Button
                            variant="contained"
                            size="small"
                            onClick={() => handleOpenGameDialog(booking)}
                          >
                            Publish Open Game
                          </Button>
                        )}

                        {matchesByBookingId.has(booking.id) && (
                          <Button
                            variant="outlined"
                            size="small"
                            color="warning"
                            onClick={() => handleCancelOpenMatch(matchesByBookingId.get(booking.id)!.id)}
                          >
                            Close Open Game
                          </Button>
                        )}
                      </Stack>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>

          <Paper sx={{ p: 3, mt: 3 }}>
            <Typography variant="h5" gutterBottom>
              My Open Matches
            </Typography>
            <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
              Use open matches to fill your bookings faster and let other players join your game.
            </Typography>
            <Divider sx={{ mb: 3 }} />

            {myMatches.length === 0 ? (
              <Alert severity="info">
                <AlertTitle>No open matches published yet</AlertTitle>
                Publish one of your confirmed bookings to start finding players.
              </Alert>
            ) : (
              <Stack spacing={2}>
                {myMatches.map((match) => (
                  <OpenMatchCard
                    key={match.id}
                    match={match}
                    currentUserId={user?.id}
                    isAuthenticated={!!user}
                    onLeave={handleLeaveOpenMatch}
                    onCancel={handleCancelOpenMatch}
                    actionLoading={matchActionLoadingId === match.id}
                  />
                ))}
              </Stack>
            )}
          </Paper>
        </>
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

      <Dialog open={matchDialogOpen} onClose={() => setMatchDialogOpen(false)} fullWidth maxWidth="sm">
        <DialogTitle>Publish Open Match</DialogTitle>
        <DialogContent>
          <DialogContentText sx={{ mb: 2 }}>
            Turn this confirmed booking into a public pickup game that other players can discover and join.
          </DialogContentText>
          <Stack spacing={2}>
            <TextField
              label="Match title"
              value={matchForm.title}
              onChange={(event) => setMatchForm((current) => ({ ...current, title: event.target.value }))}
              fullWidth
            />
            <TextField
              select
              label="Skill level"
              value={matchForm.skillLevel}
              onChange={(event) =>
                setMatchForm((current) => ({
                  ...current,
                  skillLevel: event.target.value as MatchSkillLevel,
                }))
              }
              fullWidth
            >
              <MenuItem value={MatchSkillLevel.ANY}>Any level</MenuItem>
              <MenuItem value={MatchSkillLevel.CASUAL}>Casual</MenuItem>
              <MenuItem value={MatchSkillLevel.INTERMEDIATE}>Intermediate</MenuItem>
              <MenuItem value={MatchSkillLevel.COMPETITIVE}>Competitive</MenuItem>
            </TextField>
            <TextField
              type="number"
              label="Total players needed"
              value={matchForm.desiredPlayerCount}
              onChange={(event) =>
                setMatchForm((current) => ({
                  ...current,
                  desiredPlayerCount: Number(event.target.value),
                }))
              }
              inputProps={{ min: 2, max: 14 }}
              fullWidth
            />
            <TextField
              label="Notes"
              value={matchForm.notes}
              onChange={(event) => setMatchForm((current) => ({ ...current, notes: event.target.value }))}
              multiline
              rows={3}
              fullWidth
            />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setMatchDialogOpen(false)}>Cancel</Button>
          <Button variant="contained" onClick={handleCreateOpenMatch}>
            Publish Match
          </Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default MyBookings;
