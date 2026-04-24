import React, { useEffect, useState } from 'react';
import {
  Container,
  Paper,
  Typography,
  Box,
  CircularProgress,
  Alert,
  Grid,
  Chip,
  Button,
  Rating,
  Divider,
  alpha,
} from '@mui/material';
import {
  LocationOn,
  CalendarMonth,
  ArrowBack,
  SportsSoccer,
  LocalParking,
  Wc,
  LocalDrink,
  MedicalServices,
} from '@mui/icons-material';
import { useParams, useNavigate } from 'react-router-dom';
import { FutsalGround } from '../../types';
import { groundService } from '../../services/groundService';
import { reviewService } from '../../services/reviewService';
import { useAuth } from '../../contexts/AuthContext';
import { colors } from '../../theme/theme';
import ReviewSection from '../reviews/ReviewSection';
import dayjs from 'dayjs';

const facilities = [
  { icon: <Wc />, name: 'Changing Rooms' },
  { icon: <LocalParking />, name: 'Parking' },
  { icon: <LocalDrink />, name: 'Water Cooler' },
  { icon: <MedicalServices />, name: 'First Aid' },
];

const GroundDetails: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const [ground, setGround] = useState<FutsalGround | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [averageRating, setAverageRating] = useState(0);
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (id) {
      fetchGround(id);
      fetchRating(id);
    }
  }, [id]);

  const fetchGround = async (groundId: string) => {
    try {
      setLoading(true);
      const data = await groundService.getGroundById(groundId);
      setGround(data);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load ground details');
    } finally {
      setLoading(false);
    }
  };

  const fetchRating = async (groundId: string) => {
    try {
      const rating = await reviewService.getAverageRating(groundId);
      setAverageRating(rating);
    } catch (err) {
      console.error('Failed to fetch rating:', err);
    }
  };

  const handleBookNow = () => {
    if (!isAuthenticated) {
      navigate('/login', { state: { from: `/grounds/${id}` } });
      return;
    }
    navigate(`/booking/new?groundId=${id}`);
  };

  if (loading) {
    return (
      <Box
        display="flex"
        justifyContent="center"
        alignItems="center"
        minHeight="60vh"
        sx={{ bgcolor: colors.background.default }}
      >
        <CircularProgress sx={{ color: colors.primary.main }} />
      </Box>
    );
  }

  if (error || !ground) {
    return (
      <Box sx={{ bgcolor: colors.background.default, minHeight: '100vh', pt: 12 }}>
        <Container sx={{ mt: 4 }}>
          <Alert
            severity="error"
            sx={{
              bgcolor: alpha(colors.status.error, 0.1),
              color: colors.status.error,
            }}
          >
            {error || 'Ground not found'}
          </Alert>
          <Button
            variant="outlined"
            sx={{
              mt: 2,
              borderColor: colors.primary.main,
              color: colors.primary.main,
            }}
            startIcon={<ArrowBack />}
            onClick={() => navigate('/grounds')}
          >
            Back to Grounds
          </Button>
        </Container>
      </Box>
    );
  }

  return (
    <Box sx={{ bgcolor: colors.background.default, minHeight: '100vh', pb: 8 }}>
      {/* Hero Image Section */}
      <Box
        sx={{
          position: 'relative',
          height: { xs: 300, md: 450 },
          overflow: 'hidden',
        }}
      >
        <Box
          component="img"
          src={ground.imageUrl || 'https://images.unsplash.com/photo-1574629810360-7efbbe195018?w=1920&q=80'}
          alt={ground.name}
          sx={{
            width: '100%',
            height: '100%',
            objectFit: 'cover',
          }}
        />
        <Box
          sx={{
            position: 'absolute',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            background: `linear-gradient(180deg,
              rgba(10, 10, 10, 0.3) 0%,
              rgba(10, 10, 10, 0.5) 50%,
              rgba(10, 10, 10, 0.95) 100%)`,
          }}
        />
        <Container
          sx={{
            position: 'absolute',
            bottom: 0,
            left: 0,
            right: 0,
            pb: 4,
            pt: 12,
          }}
        >
          <Button
            startIcon={<ArrowBack />}
            onClick={() => navigate('/grounds')}
            sx={{
              color: colors.text.primary,
              mb: 3,
              '&:hover': { bgcolor: alpha(colors.neutral.white, 0.1) },
            }}
          >
            Back to Grounds
          </Button>
          <Typography
            variant="h2"
            sx={{
              fontWeight: 700,
              color: colors.text.primary,
              fontFamily: '"Oswald", sans-serif',
              textTransform: 'uppercase',
              letterSpacing: '0.02em',
              mb: 2,
            }}
          >
            {ground.name}
          </Typography>
          <Box display="flex" alignItems="center" gap={3} flexWrap="wrap">
            <Box display="flex" alignItems="center" gap={0.5}>
              <LocationOn sx={{ color: colors.primary.main }} />
              <Typography sx={{ color: colors.text.secondary }}>{ground.companyName}</Typography>
            </Box>
            <Box display="flex" alignItems="center" gap={0.5}>
              <Rating
                value={averageRating}
                precision={0.5}
                readOnly
                size="small"
                sx={{
                  '& .MuiRating-iconFilled': { color: colors.accent.gold },
                  '& .MuiRating-iconEmpty': { color: alpha(colors.neutral.white, 0.2) },
                }}
              />
              <Typography sx={{ color: colors.text.secondary }}>({averageRating.toFixed(1)})</Typography>
            </Box>
          </Box>
        </Container>
      </Box>

      <Container maxWidth="lg" sx={{ mt: -6, position: 'relative', zIndex: 1 }}>
        <Grid container spacing={4}>
          {/* Main Content */}
          <Grid item xs={12} md={8}>
            {/* Ground Info Card */}
            <Paper
              sx={{
                p: 4,
                mb: 4,
                bgcolor: colors.background.card,
                border: `1px solid ${alpha(colors.neutral.white, 0.05)}`,
              }}
            >
              <Typography
                variant="h5"
                sx={{
                  fontWeight: 700,
                  color: colors.text.primary,
                  fontFamily: '"Oswald", sans-serif',
                  textTransform: 'uppercase',
                  letterSpacing: '0.05em',
                  mb: 3,
                }}
              >
                About This Ground
              </Typography>
              <Box display="flex" gap={2} flexWrap="wrap" mb={4}>
                <Chip
                  icon={<SportsSoccer sx={{ color: `${colors.primary.main} !important` }} />}
                  label={ground.surfaceType}
                  sx={{
                    bgcolor: alpha(colors.primary.main, 0.1),
                    color: colors.primary.main,
                    fontWeight: 700,
                    border: `1px solid ${alpha(colors.primary.main, 0.3)}`,
                  }}
                />
                <Chip
                  icon={<CalendarMonth sx={{ color: `${colors.text.secondary} !important` }} />}
                  label={`Added ${dayjs(ground.createdAt).format('MMM YYYY')}`}
                  variant="outlined"
                  sx={{
                    borderColor: alpha(colors.neutral.white, 0.1),
                    color: colors.text.secondary,
                  }}
                />
              </Box>
              <Typography
                variant="body1"
                sx={{
                  color: colors.text.secondary,
                  lineHeight: 1.8,
                }}
              >
                Experience top-quality futsal at {ground.name}. This {ground.surfaceType.toLowerCase()}
                surface ground offers an excellent playing experience for both casual games and
                competitive matches. Located at {ground.companyName}, the facility provides
                well-maintained playing surfaces and convenient booking options.
              </Typography>

              <Divider sx={{ my: 4, borderColor: alpha(colors.neutral.white, 0.1) }} />

              {/* Facilities */}
              <Typography
                variant="h6"
                sx={{
                  fontWeight: 700,
                  color: colors.text.primary,
                  fontFamily: '"Oswald", sans-serif',
                  textTransform: 'uppercase',
                  letterSpacing: '0.05em',
                  mb: 3,
                }}
              >
                Facilities
              </Typography>
              <Grid container spacing={2}>
                {facilities.map((facility, index) => (
                  <Grid item xs={6} sm={3} key={index}>
                    <Paper
                      elevation={0}
                      sx={{
                        p: 3,
                        textAlign: 'center',
                        bgcolor: alpha(colors.primary.main, 0.05),
                        borderRadius: 2,
                        border: `1px solid ${alpha(colors.primary.main, 0.1)}`,
                        transition: 'all 0.3s ease',
                        '&:hover': {
                          borderColor: alpha(colors.primary.main, 0.3),
                          '& .facility-icon': {
                            color: colors.primary.main,
                          },
                        },
                      }}
                    >
                      <Box
                        className="facility-icon"
                        sx={{
                          color: colors.text.secondary,
                          mb: 1,
                          transition: 'color 0.3s ease',
                        }}
                      >
                        {facility.icon}
                      </Box>
                      <Typography
                        variant="body2"
                        sx={{
                          fontWeight: 600,
                          color: colors.text.primary,
                        }}
                      >
                        {facility.name}
                      </Typography>
                    </Paper>
                  </Grid>
                ))}
              </Grid>
            </Paper>

            {/* Reviews Section */}
            <Paper
              sx={{
                p: 4,
                bgcolor: colors.background.card,
                border: `1px solid ${alpha(colors.neutral.white, 0.05)}`,
              }}
            >
              <Typography
                variant="h5"
                sx={{
                  fontWeight: 700,
                  color: colors.text.primary,
                  fontFamily: '"Oswald", sans-serif',
                  textTransform: 'uppercase',
                  letterSpacing: '0.05em',
                  mb: 3,
                }}
              >
                Reviews & Ratings
              </Typography>
              <ReviewSection groundId={ground.id} groundName={ground.name} />
            </Paper>
          </Grid>

          {/* Sidebar - Booking Card */}
          <Grid item xs={12} md={4}>
            <Paper
              sx={{
                p: 4,
                position: { md: 'sticky' },
                top: 100,
                bgcolor: colors.background.card,
                border: `1px solid ${alpha(colors.neutral.white, 0.05)}`,
              }}
            >
              <Box
                sx={{
                  bgcolor: alpha(colors.primary.main, 0.1),
                  p: 3,
                  borderRadius: 2,
                  mb: 4,
                  textAlign: 'center',
                  border: `1px solid ${alpha(colors.primary.main, 0.2)}`,
                }}
              >
                <Typography
                  variant="overline"
                  sx={{
                    color: colors.text.muted,
                    letterSpacing: '0.15em',
                  }}
                >
                  Price per Hour
                </Typography>
                <Typography
                  variant="h3"
                  sx={{
                    fontWeight: 700,
                    color: colors.primary.main,
                    fontFamily: '"Oswald", sans-serif',
                    textShadow: `0 0 20px ${alpha(colors.primary.main, 0.3)}`,
                  }}
                >
                  NPR {ground.pricePerHour}
                </Typography>
              </Box>

              <Box mb={4}>
                <Box
                  display="flex"
                  justifyContent="space-between"
                  mb={2}
                  pb={2}
                  sx={{ borderBottom: `1px solid ${alpha(colors.neutral.white, 0.05)}` }}
                >
                  <Typography variant="body2" sx={{ color: colors.text.muted }}>
                    Surface Type
                  </Typography>
                  <Typography variant="body2" sx={{ fontWeight: 700, color: colors.text.primary }}>
                    {ground.surfaceType}
                  </Typography>
                </Box>
                <Box
                  display="flex"
                  justifyContent="space-between"
                  mb={2}
                  pb={2}
                  sx={{ borderBottom: `1px solid ${alpha(colors.neutral.white, 0.05)}` }}
                >
                  <Typography variant="body2" sx={{ color: colors.text.muted }}>
                    Company
                  </Typography>
                  <Typography variant="body2" sx={{ fontWeight: 700, color: colors.text.primary }}>
                    {ground.companyName}
                  </Typography>
                </Box>
                <Box display="flex" justifyContent="space-between" alignItems="center">
                  <Typography variant="body2" sx={{ color: colors.text.muted }}>
                    Rating
                  </Typography>
                  <Box display="flex" alignItems="center" gap={0.5}>
                    <Rating
                      value={averageRating}
                      precision={0.5}
                      size="small"
                      readOnly
                      sx={{
                        '& .MuiRating-iconFilled': { color: colors.accent.gold },
                        '& .MuiRating-iconEmpty': { color: alpha(colors.neutral.white, 0.2) },
                      }}
                    />
                    <Typography variant="body2" sx={{ fontWeight: 700, color: colors.text.primary }}>
                      {averageRating.toFixed(1)}
                    </Typography>
                  </Box>
                </Box>
              </Box>

              <Button
                variant="contained"
                size="large"
                fullWidth
                onClick={handleBookNow}
                sx={{
                  py: 2,
                  fontSize: '1rem',
                  fontWeight: 700,
                  letterSpacing: '0.1em',
                  bgcolor: colors.primary.main,
                  '&:hover': {
                    bgcolor: colors.primary.light,
                    boxShadow: `0 0 30px ${alpha(colors.primary.main, 0.5)}`,
                  },
                }}
              >
                Book Now
              </Button>

              {!isAuthenticated && (
                <Typography
                  variant="caption"
                  sx={{
                    display: 'block',
                    textAlign: 'center',
                    mt: 2,
                    color: colors.text.muted,
                  }}
                >
                  You'll need to login to make a booking
                </Typography>
              )}
            </Paper>
          </Grid>
        </Grid>
      </Container>
    </Box>
  );
};

export default GroundDetails;
