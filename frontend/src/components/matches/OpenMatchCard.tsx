import React from 'react';
import {
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  Stack,
  Typography,
} from '@mui/material';
import {
  Group as GroupIcon,
  Schedule as ScheduleIcon,
  SportsSoccer as SportsSoccerIcon,
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import dayjs from 'dayjs';
import { MatchSkillLevel, OpenMatch, OpenMatchStatus } from '../../types';

interface OpenMatchCardProps {
  match: OpenMatch;
  currentUserId?: string;
  isAuthenticated: boolean;
  onJoin?: (matchId: string) => Promise<void> | void;
  onLeave?: (matchId: string) => Promise<void> | void;
  onCancel?: (matchId: string) => Promise<void> | void;
  actionLoading?: boolean;
  showGroundLink?: boolean;
}

const skillLevelLabels: Record<MatchSkillLevel, string> = {
  ANY: 'Any level',
  CASUAL: 'Casual',
  INTERMEDIATE: 'Intermediate',
  COMPETITIVE: 'Competitive',
};

const statusColorMap: Record<OpenMatchStatus, 'success' | 'warning' | 'default'> = {
  OPEN: 'success',
  FULL: 'warning',
  CANCELLED: 'default',
};

const OpenMatchCard: React.FC<OpenMatchCardProps> = ({
  match,
  currentUserId,
  isAuthenticated,
  onJoin,
  onLeave,
  onCancel,
  actionLoading = false,
  showGroundLink = true,
}) => {
  const navigate = useNavigate();
  const isHost = currentUserId === match.hostUserId;
  const isParticipant = !!currentUserId && match.participantUserIds.includes(currentUserId);
  const canJoin = isAuthenticated && !isHost && !isParticipant && match.status === OpenMatchStatus.OPEN;
  const canLeave = isAuthenticated && isParticipant && match.status !== OpenMatchStatus.CANCELLED;
  const canCancel = isAuthenticated && isHost && match.status !== OpenMatchStatus.CANCELLED;

  return (
    <Card sx={{ height: '100%' }}>
      <CardContent>
        <Stack spacing={2}>
          <Box display="flex" justifyContent="space-between" alignItems="flex-start" gap={2}>
            <Box>
              <Typography variant="h6" fontWeight={700}>
                {match.title}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Hosted by {match.hostName}
              </Typography>
            </Box>
            <Chip label={match.status} color={statusColorMap[match.status]} size="small" />
          </Box>

          <Stack direction="row" spacing={1} flexWrap="wrap" useFlexGap>
            <Chip icon={<SportsSoccerIcon />} label={skillLevelLabels[match.skillLevel]} size="small" />
            <Chip
              icon={<GroupIcon />}
              label={`${match.currentPlayerCount}/${match.desiredPlayerCount} players`}
              size="small"
            />
            <Chip icon={<ScheduleIcon />} label={`${match.openSpots} spots left`} size="small" />
          </Stack>

          <Typography variant="body2" color="text.secondary">
            {dayjs(match.slotStartTime).format('ddd, MMM D • h:mm A')} to {dayjs(match.slotEndTime).format('h:mm A')}
          </Typography>

          {showGroundLink && (
            <Button
              variant="text"
              sx={{ alignSelf: 'flex-start', px: 0 }}
              onClick={() => navigate(`/grounds/${match.groundId}`)}
            >
              {match.groundName}
            </Button>
          )}

          {match.notes && (
            <Typography variant="body2">
              {match.notes}
            </Typography>
          )}

          {match.participantNames.length > 0 && (
            <Typography variant="body2" color="text.secondary">
              Joined: {match.participantNames.join(', ')}
            </Typography>
          )}

          <Box display="flex" gap={1} flexWrap="wrap">
            {canJoin && onJoin && (
              <Button
                variant="contained"
                onClick={() => onJoin(match.id)}
                disabled={actionLoading}
              >
                Join Match
              </Button>
            )}

            {canLeave && onLeave && (
              <Button
                variant="outlined"
                color="warning"
                onClick={() => onLeave(match.id)}
                disabled={actionLoading}
              >
                Leave Match
              </Button>
            )}

            {canCancel && onCancel && (
              <Button
                variant="outlined"
                color="error"
                onClick={() => onCancel(match.id)}
                disabled={actionLoading}
              >
                Cancel Match
              </Button>
            )}

            {!isAuthenticated && (
              <Button variant="outlined" onClick={() => navigate('/login')}>
                Sign in to join
              </Button>
            )}
          </Box>
        </Stack>
      </CardContent>
    </Card>
  );
};

export default OpenMatchCard;
