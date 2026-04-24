import React, { useCallback, useEffect, useMemo, useState } from 'react';
import {
  Alert,
  Box,
  Chip,
  Container,
  Grid,
  MenuItem,
  Stack,
  TextField,
  Typography,
} from '@mui/material';
import dayjs from 'dayjs';
import { useAuth } from '../contexts/AuthContext';
import { useToast } from '../contexts/ToastContext';
import OpenMatchCard from '../components/matches/OpenMatchCard';
import { openMatchService } from '../services/openMatchService';
import { MatchSkillLevel, OpenMatch } from '../types';

const OpenMatchesPage: React.FC = () => {
  const { isAuthenticated, user } = useAuth();
  const { showToast } = useToast();
  const [matches, setMatches] = useState<OpenMatch[]>([]);
  const [error, setError] = useState('');
  const [skillFilter, setSkillFilter] = useState('');
  const [searchTerm, setSearchTerm] = useState('');
  const [availabilityFilter, setAvailabilityFilter] = useState('');
  const [actionLoadingId, setActionLoadingId] = useState<string | null>(null);

  const fetchMatches = useCallback(async () => {
    try {
      const data = await openMatchService.getOpenMatches();
      setMatches(data);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load open matches');
    }
  }, []);

  useEffect(() => {
    fetchMatches();
  }, [fetchMatches]);

  const filteredMatches = useMemo(() => {
    return matches.filter((match) => {
      const matchesSkill = !skillFilter || match.skillLevel === skillFilter;
      const matchesAvailability =
        !availabilityFilter ||
        (availabilityFilter === 'last-spots' ? match.openSpots <= 2 : match.openSpots >= 3);
      const isUpcoming = dayjs(match.slotStartTime).isAfter(dayjs().subtract(1, 'hour'));
      const normalizedSearch = searchTerm.trim().toLowerCase();
      const matchesSearch =
        !normalizedSearch ||
        match.title.toLowerCase().includes(normalizedSearch) ||
        match.groundName.toLowerCase().includes(normalizedSearch) ||
        match.hostName.toLowerCase().includes(normalizedSearch);
      return matchesSkill && matchesAvailability && matchesSearch && isUpcoming;
    });
  }, [availabilityFilter, matches, searchTerm, skillFilter]);

  const handleJoin = async (matchId: string) => {
    try {
      setActionLoadingId(matchId);
      const updated = await openMatchService.joinOpenMatch(matchId);
      setMatches((current) => current.map((match) => (match.id === matchId ? updated : match)));
      showToast('Joined match successfully.', 'success');
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to join match');
    } finally {
      setActionLoadingId(null);
    }
  };

  const handleLeave = async (matchId: string) => {
    try {
      setActionLoadingId(matchId);
      const updated = await openMatchService.leaveOpenMatch(matchId);
      setMatches((current) => current.map((match) => (match.id === matchId ? updated : match)));
      showToast('Left match successfully.', 'info');
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to leave match');
    } finally {
      setActionLoadingId(null);
    }
  };

  const handleCancel = async (matchId: string) => {
    try {
      setActionLoadingId(matchId);
      await openMatchService.cancelOpenMatch(matchId);
      setMatches((current) => current.filter((match) => match.id !== matchId));
      showToast('Match cancelled.', 'success');
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to cancel match');
    } finally {
      setActionLoadingId(null);
    }
  };

  return (
    <Container maxWidth="lg" sx={{ py: 6 }}>
      <Stack spacing={3}>
        <Box>
          <Typography variant="h3" fontWeight={700} gutterBottom>
            Find Open Matches
          </Typography>
          <Typography variant="body1" color="text.secondary">
            Join pickup games, meet other players, and turn a normal booking app into a social futsal hub.
          </Typography>
          <Stack direction="row" spacing={1} sx={{ mt: 2, flexWrap: 'wrap' }} useFlexGap>
            <Chip label={`${matches.length} listed`} color="primary" variant="outlined" />
            <Chip label={`${filteredMatches.length} matching filters`} variant="outlined" />
          </Stack>
        </Box>

        {error && (
          <Alert severity="error" onClose={() => setError('')}>
            {error}
          </Alert>
        )}

        <Stack direction={{ xs: 'column', md: 'row' }} spacing={2}>
          <TextField
            fullWidth
            label="Search matches"
            value={searchTerm}
            onChange={(event) => setSearchTerm(event.target.value)}
            placeholder="Search by title, ground, or host"
          />
          <TextField
            select
            label="Skill level"
            value={skillFilter}
            onChange={(event) => setSkillFilter(event.target.value)}
            sx={{ minWidth: { md: 220 } }}
          >
            <MenuItem value="">All levels</MenuItem>
            <MenuItem value={MatchSkillLevel.ANY}>Any level</MenuItem>
            <MenuItem value={MatchSkillLevel.CASUAL}>Casual</MenuItem>
            <MenuItem value={MatchSkillLevel.INTERMEDIATE}>Intermediate</MenuItem>
            <MenuItem value={MatchSkillLevel.COMPETITIVE}>Competitive</MenuItem>
          </TextField>
          <TextField
            select
            label="Spots"
            value={availabilityFilter}
            onChange={(event) => setAvailabilityFilter(event.target.value)}
            sx={{ minWidth: { md: 220 } }}
          >
            <MenuItem value="">Any availability</MenuItem>
            <MenuItem value="last-spots">Last 2 spots</MenuItem>
            <MenuItem value="roomy">3+ spots</MenuItem>
          </TextField>
        </Stack>

        {filteredMatches.length === 0 ? (
          <Alert severity="info">
            No open matches found right now. Publish one from your confirmed bookings to get the community started.
          </Alert>
        ) : (
          <Grid container spacing={3}>
            {filteredMatches.map((match) => (
              <Grid item xs={12} md={6} key={match.id}>
                <OpenMatchCard
                  match={match}
                  currentUserId={user?.id}
                  isAuthenticated={isAuthenticated}
                  onJoin={handleJoin}
                  onLeave={handleLeave}
                  onCancel={handleCancel}
                  actionLoading={actionLoadingId === match.id}
                />
              </Grid>
            ))}
          </Grid>
        )}
      </Stack>
    </Container>
  );
};

export default OpenMatchesPage;
