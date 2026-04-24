import React, { useEffect, useState } from 'react';
import {
  Alert,
  Container,
  Typography,
  Button,
  Box,
  Grid,
  Card,
  CardContent,
  CardMedia,
  Chip,
  Rating,
  Skeleton,
  Stack,
  alpha,
} from '@mui/material';
import {
  SportsSoccer,
  Schedule,
  Security,
  Speed,
  ArrowForward,
  LocationOn,
  Star,
  PlayArrow,
  EmojiEvents,
  Groups,
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { FutsalGround, OpenMatch } from '../types';
import { groundService } from '../services/groundService';
import { openMatchService } from '../services/openMatchService';
import OpenMatchCard from '../components/matches/OpenMatchCard';
import { colors } from '../theme/theme';

// Stats data
const stats = [
  { value: '500+', label: 'Bookings', icon: <Schedule /> },
  { value: '50+', label: 'Grounds', icon: <SportsSoccer /> },
  { value: '1000+', label: 'Players', icon: <Groups /> },
  { value: '5★', label: 'Rating', icon: <Star /> },
];

// Features data
const features = [
  {
    icon: <SportsSoccer sx={{ fontSize: 36 }} />,
    title: 'Premium Grounds',
    description: 'Access top-quality futsal grounds with professional-grade surfaces and facilities',
  },
  {
    icon: <Schedule sx={{ fontSize: 36 }} />,
    title: 'Instant Booking',
    description: 'Book your preferred time slot in seconds with real-time availability updates',
  },
  {
    icon: <Security sx={{ fontSize: 36 }} />,
    title: 'Secure Payments',
    description: 'Your transactions are protected with industry-standard encryption',
  },
  {
    icon: <Speed sx={{ fontSize: 36 }} />,
    title: 'Quick & Easy',
    description: 'Streamlined booking process designed for the best user experience',
  },
];

const Home: React.FC = () => {
  const navigate = useNavigate();
  const { isAuthenticated, user } = useAuth();
  const [popularGrounds, setPopularGrounds] = useState<FutsalGround[]>([]);
  const [featuredMatches, setFeaturedMatches] = useState<OpenMatch[]>([]);
  const [loading, setLoading] = useState(true);
  const [matchesLoading, setMatchesLoading] = useState(true);

  useEffect(() => {
    fetchPopularGrounds();
    fetchFeaturedMatches();
  }, []);

  const fetchPopularGrounds = async () => {
    try {
      setLoading(true);
      const grounds = await groundService.getAllGrounds();
      setPopularGrounds(grounds.slice(0, 6));
    } catch (error) {
      console.error('Failed to fetch grounds:', error);
    } finally {
      setLoading(false);
    }
  };

  const fetchFeaturedMatches = async () => {
    try {
      setMatchesLoading(true);
      const matches = await openMatchService.getOpenMatches();
      setFeaturedMatches(matches.slice(0, 3));
    } catch (error) {
      console.error('Failed to fetch open matches:', error);
    } finally {
      setMatchesLoading(false);
    }
  };

  const handleJoinFeaturedMatch = async (matchId: string) => {
    try {
      const updatedMatch = await openMatchService.joinOpenMatch(matchId);
      setFeaturedMatches((currentMatches) =>
        currentMatches.map((match) => (match.id === matchId ? updatedMatch : match))
      );
    } catch (error) {
      console.error('Failed to join featured match:', error);
      navigate('/matches');
    }
  };

  const handleLeaveFeaturedMatch = async (matchId: string) => {
    try {
      const updatedMatch = await openMatchService.leaveOpenMatch(matchId);
      setFeaturedMatches((currentMatches) =>
        currentMatches.map((match) => (match.id === matchId ? updatedMatch : match))
      );
    } catch (error) {
      console.error('Failed to leave featured match:', error);
      navigate('/matches');
    }
  };

  return (
    <Box sx={{ bgcolor: colors.background.default }}>
      {/* Hero Section */}
      <Box
        sx={{
          position: 'relative',
          minHeight: '100vh',
          display: 'flex',
          alignItems: 'center',
          overflow: 'hidden',
          pt: { xs: 10, md: 0 },
        }}
      >
        {/* Video/Image Background */}
        <Box
          sx={{
            position: 'absolute',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            backgroundImage: 'url(https://images.unsplash.com/photo-1574629810360-7efbbe195018?ixlib=rb-4.0.3&auto=format&fit=crop&w=2070&q=80)',
            backgroundSize: 'cover',
            backgroundPosition: 'center',
            '&::after': {
              content: '""',
              position: 'absolute',
              top: 0,
              left: 0,
              right: 0,
              bottom: 0,
              background: `linear-gradient(180deg,
                rgba(10, 10, 10, 0.7) 0%,
                rgba(10, 10, 10, 0.8) 50%,
                rgba(10, 10, 10, 0.95) 100%)`,
            },
          }}
        />

        {/* Animated Grid Pattern */}
        <Box
          className="grid-pattern"
          sx={{
            position: 'absolute',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            opacity: 0.3,
            zIndex: 1,
          }}
        />

        <Container maxWidth="lg" sx={{ position: 'relative', zIndex: 2 }}>
          <Grid container spacing={4} alignItems="center" justifyContent="center">
            <Grid item xs={12} md={10} lg={8}>
              <Box sx={{ textAlign: 'center' }} className="animate-fadeInUp">
                {/* Badge */}
                <Chip
                  icon={<EmojiEvents sx={{ color: `${colors.accent.gold} !important` }} />}
                  label="NEPAL'S #1 FUTSAL BOOKING PLATFORM"
                  sx={{
                    bgcolor: alpha(colors.primary.main, 0.15),
                    color: colors.primary.main,
                    fontWeight: 700,
                    fontSize: '0.7rem',
                    letterSpacing: '0.15em',
                    mb: 4,
                    py: 2.5,
                    px: 1,
                    border: `1px solid ${alpha(colors.primary.main, 0.3)}`,
                  }}
                />

                {/* Main Headline */}
                <Typography
                  variant="h1"
                  sx={{
                    color: colors.text.primary,
                    fontSize: { xs: '2.5rem', sm: '3.5rem', md: '4.5rem', lg: '5rem' },
                    fontWeight: 700,
                    lineHeight: 1,
                    mb: 3,
                    fontFamily: '"Oswald", sans-serif',
                    textTransform: 'uppercase',
                    letterSpacing: '0.02em',
                  }}
                >
                  Book Your
                  <br />
                  <Box
                    component="span"
                    sx={{
                      color: colors.primary.main,
                      textShadow: `0 0 40px ${alpha(colors.primary.main, 0.5)}`,
                    }}
                  >
                    Perfect Pitch
                  </Box>
                </Typography>

                {/* Subheadline */}
                <Typography
                  variant="h6"
                  sx={{
                    color: colors.text.secondary,
                    mb: 5,
                    fontWeight: 400,
                    maxWidth: '600px',
                    mx: 'auto',
                    lineHeight: 1.8,
                  }}
                >
                  Find and book premium futsal grounds near you in seconds.
                  Real-time availability, instant confirmation.
                </Typography>

                {/* CTA Buttons */}
                <Box sx={{ display: 'flex', gap: 2, justifyContent: 'center', flexWrap: 'wrap', mb: 6 }}>
                  <Button
                    variant="contained"
                    size="large"
                    onClick={() => navigate('/grounds')}
                    sx={{
                      bgcolor: colors.primary.main,
                      color: '#FFFFFF',
                      px: 5,
                      py: 2,
                      fontSize: '0.9rem',
                      fontWeight: 700,
                      letterSpacing: '0.1em',
                      '&:hover': {
                        bgcolor: colors.primary.light,
                        transform: 'translateY(-3px)',
                        boxShadow: `0 10px 40px ${alpha(colors.primary.main, 0.5)}`,
                      },
                    }}
                    endIcon={<ArrowForward />}
                  >
                    Browse Grounds
                  </Button>
                  {!isAuthenticated && (
                    <Button
                      variant="outlined"
                      size="large"
                      onClick={() => navigate('/register')}
                      sx={{
                        borderColor: colors.text.primary,
                        color: colors.text.primary,
                        borderWidth: 2,
                        px: 5,
                        py: 2,
                        fontSize: '0.9rem',
                        fontWeight: 700,
                        letterSpacing: '0.1em',
                        '&:hover': {
                          borderColor: colors.primary.main,
                          color: colors.primary.main,
                          bgcolor: alpha(colors.primary.main, 0.05),
                          borderWidth: 2,
                        },
                      }}
                    >
                      Join Free
                    </Button>
                  )}
                </Box>

                {/* Stats Row */}
                <Grid container spacing={3} sx={{ maxWidth: 800, mx: 'auto' }}>
                  {stats.map((stat, index) => (
                    <Grid item xs={6} sm={3} key={index}>
                      <Box
                        sx={{
                          textAlign: 'center',
                          p: 2,
                          borderRadius: 2,
                          bgcolor: alpha(colors.neutral.white, 0.03),
                          border: `1px solid ${alpha(colors.neutral.white, 0.05)}`,
                          transition: 'all 0.3s ease',
                          '&:hover': {
                            borderColor: alpha(colors.primary.main, 0.3),
                            bgcolor: alpha(colors.primary.main, 0.05),
                          },
                        }}
                      >
                        <Box sx={{ color: colors.primary.main, mb: 1 }}>
                          {stat.icon}
                        </Box>
                        <Typography
                          variant="h4"
                          sx={{
                            color: colors.text.primary,
                            fontWeight: 700,
                            fontFamily: '"Oswald", sans-serif',
                          }}
                        >
                          {stat.value}
                        </Typography>
                        <Typography
                          variant="caption"
                          sx={{
                            color: colors.text.muted,
                            textTransform: 'uppercase',
                            letterSpacing: '0.1em',
                            fontSize: '0.7rem',
                          }}
                        >
                          {stat.label}
                        </Typography>
                      </Box>
                    </Grid>
                  ))}
                </Grid>
              </Box>
            </Grid>
          </Grid>

          {/* Scroll Indicator */}
          <Box
            sx={{
              position: 'absolute',
              bottom: 40,
              left: '50%',
              transform: 'translateX(-50%)',
              textAlign: 'center',
            }}
            className="animate-bounce"
          >
            <Typography
              variant="caption"
              sx={{
                color: colors.text.muted,
                textTransform: 'uppercase',
                letterSpacing: '0.2em',
                fontSize: '0.65rem',
                display: 'block',
                mb: 1,
              }}
            >
              Scroll to Explore
            </Typography>
            <Box
              sx={{
                width: 24,
                height: 40,
                border: `2px solid ${alpha(colors.neutral.white, 0.2)}`,
                borderRadius: 12,
                mx: 'auto',
                position: 'relative',
                '&::after': {
                  content: '""',
                  position: 'absolute',
                  top: 8,
                  left: '50%',
                  transform: 'translateX(-50%)',
                  width: 4,
                  height: 8,
                  bgcolor: colors.primary.main,
                  borderRadius: 2,
                  animation: 'scrollIndicator 1.5s ease-in-out infinite',
                },
                '@keyframes scrollIndicator': {
                  '0%, 100%': { top: 8, opacity: 1 },
                  '50%': { top: 20, opacity: 0.5 },
                },
              }}
            />
          </Box>
        </Container>
      </Box>

      <Box sx={{ py: 12, bgcolor: colors.background.default }}>
        <Container maxWidth="lg">
          <Grid container spacing={5} alignItems="center">
            <Grid item xs={12} md={4}>
              <Typography
                variant="overline"
                sx={{
                  color: colors.primary.main,
                  fontWeight: 600,
                  letterSpacing: '0.2em',
                  mb: 2,
                  display: 'block',
                }}
              >
                Community Play
              </Typography>
              <Typography
                variant="h2"
                sx={{
                  fontWeight: 700,
                  color: colors.text.primary,
                  mb: 2,
                  fontFamily: '"Oswald", sans-serif',
                }}
              >
                Open Matches
              </Typography>
              <Typography
                variant="body1"
                sx={{
                  color: colors.text.secondary,
                  lineHeight: 1.8,
                  mb: 4,
                }}
              >
                Turn unused booking slots into pickup games. Hosts can publish a match, set the level,
                and fill the squad faster. Players can discover games without organizing a full team first.
              </Typography>
              <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
                <Button
                  variant="contained"
                  onClick={() => navigate('/matches')}
                  endIcon={<ArrowForward />}
                  sx={{
                    bgcolor: colors.primary.main,
                    '&:hover': {
                      bgcolor: colors.primary.light,
                    },
                  }}
                >
                  Browse Open Matches
                </Button>
                {isAuthenticated && (
                  <Button
                    variant="outlined"
                    onClick={() => navigate('/my-bookings')}
                    sx={{
                      borderColor: colors.primary.main,
                      color: colors.primary.main,
                    }}
                  >
                    Publish a Booking
                  </Button>
                )}
              </Stack>
            </Grid>

            <Grid item xs={12} md={8}>
              {matchesLoading ? (
                <Grid container spacing={3}>
                  {Array.from(new Array(3)).map((_, index) => (
                    <Grid item xs={12} key={index}>
                      <Card sx={{ bgcolor: colors.background.card }}>
                        <CardContent>
                          <Skeleton variant="text" height={36} sx={{ bgcolor: alpha(colors.neutral.white, 0.05) }} />
                          <Skeleton variant="text" width="50%" sx={{ bgcolor: alpha(colors.neutral.white, 0.05) }} />
                          <Skeleton
                            variant="rounded"
                            height={80}
                            sx={{ mt: 2, bgcolor: alpha(colors.neutral.white, 0.05) }}
                          />
                        </CardContent>
                      </Card>
                    </Grid>
                  ))}
                </Grid>
              ) : featuredMatches.length === 0 ? (
                <Alert severity="info" sx={{ bgcolor: colors.background.card }}>
                  Open matches will appear here once a player publishes a booking as a pickup game.
                </Alert>
              ) : (
                <Stack spacing={3}>
                  {featuredMatches.map((match) => (
                    <OpenMatchCard
                      key={match.id}
                      match={match}
                      currentUserId={user?.id}
                      isAuthenticated={isAuthenticated}
                      onJoin={handleJoinFeaturedMatch}
                      onLeave={handleLeaveFeaturedMatch}
                    />
                  ))}
                </Stack>
              )}
            </Grid>
          </Grid>
        </Container>
      </Box>

      {/* Popular Grounds Section */}
      <Box sx={{ py: 12, bgcolor: colors.background.paper }}>
        <Container maxWidth="lg">
          <Box sx={{ textAlign: 'center', mb: 8 }}>
            <Typography
              variant="overline"
              sx={{
                color: colors.primary.main,
                fontWeight: 600,
                letterSpacing: '0.2em',
                mb: 2,
                display: 'block',
              }}
            >
              Featured Venues
            </Typography>
            <Typography
              variant="h2"
              sx={{
                fontWeight: 700,
                color: colors.text.primary,
                mb: 2,
                fontFamily: '"Oswald", sans-serif',
              }}
            >
              Popular Grounds
            </Typography>
            <Box
              sx={{
                width: 60,
                height: 3,
                bgcolor: colors.primary.main,
                mx: 'auto',
                mb: 3,
              }}
            />
            <Typography
              variant="body1"
              sx={{
                color: colors.text.secondary,
                maxWidth: '600px',
                mx: 'auto',
              }}
            >
              Discover the most booked futsal venues in your area
            </Typography>
          </Box>

          <Grid container spacing={3}>
            {loading
              ? Array.from(new Array(6)).map((_, index) => (
                  <Grid item xs={12} sm={6} md={4} key={index}>
                    <Card sx={{ height: '100%', bgcolor: colors.background.card }}>
                      <Skeleton
                        variant="rectangular"
                        height={220}
                        sx={{ bgcolor: alpha(colors.neutral.white, 0.05) }}
                      />
                      <CardContent>
                        <Skeleton variant="text" height={32} sx={{ bgcolor: alpha(colors.neutral.white, 0.05) }} />
                        <Skeleton variant="text" width="60%" sx={{ bgcolor: alpha(colors.neutral.white, 0.05) }} />
                        <Skeleton variant="text" width="40%" sx={{ bgcolor: alpha(colors.neutral.white, 0.05) }} />
                      </CardContent>
                    </Card>
                  </Grid>
                ))
              : popularGrounds.map((ground, index) => (
                  <Grid item xs={12} sm={6} md={4} key={ground.id}>
                    <Card
                      sx={{
                        height: '100%',
                        cursor: 'pointer',
                        position: 'relative',
                        overflow: 'hidden',
                        bgcolor: colors.background.card,
                        border: `1px solid ${alpha(colors.neutral.white, 0.05)}`,
                        '&:hover': {
                          borderColor: alpha(colors.primary.main, 0.3),
                          '& .ground-image': {
                            transform: 'scale(1.1)',
                          },
                          '& .book-overlay': {
                            opacity: 1,
                          },
                          '& .card-glow': {
                            opacity: 1,
                          },
                        },
                      }}
                      onClick={() => navigate(`/grounds/${ground.id}`)}
                    >
                      {/* Glow effect on hover */}
                      <Box
                        className="card-glow"
                        sx={{
                          position: 'absolute',
                          top: 0,
                          left: 0,
                          right: 0,
                          height: 4,
                          background: colors.gradients.primary,
                          opacity: 0,
                          transition: 'opacity 0.3s ease',
                          zIndex: 1,
                        }}
                      />

                      <Box sx={{ position: 'relative', overflow: 'hidden' }}>
                        <CardMedia
                          component="img"
                          height="220"
                          image={ground.imageUrl || `https://images.unsplash.com/photo-1575361204480-aadea25e6e68?w=400&h=300&fit=crop&sig=${index}`}
                          alt={ground.name}
                          className="ground-image"
                          sx={{
                            transition: 'transform 0.5s ease',
                          }}
                        />
                        <Box
                          className="book-overlay"
                          sx={{
                            position: 'absolute',
                            top: 0,
                            left: 0,
                            right: 0,
                            bottom: 0,
                            bgcolor: alpha(colors.background.default, 0.85),
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            opacity: 0,
                            transition: 'opacity 0.3s ease',
                          }}
                        >
                          <Button
                            variant="contained"
                            startIcon={<PlayArrow />}
                            sx={{
                              bgcolor: colors.primary.main,
                              '&:hover': { bgcolor: colors.primary.light },
                            }}
                          >
                            Book Now
                          </Button>
                        </Box>
                        <Chip
                          label={ground.surfaceType}
                          size="small"
                          sx={{
                            position: 'absolute',
                            top: 16,
                            left: 16,
                            bgcolor: alpha(colors.background.default, 0.9),
                            color: colors.primary.main,
                            fontWeight: 700,
                            fontSize: '0.7rem',
                            letterSpacing: '0.05em',
                            border: `1px solid ${alpha(colors.primary.main, 0.3)}`,
                          }}
                        />
                        <Box
                          sx={{
                            position: 'absolute',
                            bottom: 16,
                            right: 16,
                            bgcolor: colors.primary.main,
                            color: 'white',
                            px: 2,
                            py: 0.75,
                            borderRadius: 50,
                            fontWeight: 700,
                            fontSize: '0.85rem',
                          }}
                        >
                          NPR {ground.pricePerHour}/hr
                        </Box>
                      </Box>
                      <CardContent sx={{ p: 3 }}>
                        <Typography
                          variant="h6"
                          gutterBottom
                          sx={{
                            fontWeight: 700,
                            color: colors.text.primary,
                            fontFamily: '"Oswald", sans-serif',
                            textTransform: 'uppercase',
                            letterSpacing: '0.02em',
                          }}
                        >
                          {ground.name}
                        </Typography>
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5, mb: 1.5 }}>
                          <LocationOn sx={{ fontSize: 16, color: colors.primary.main }} />
                          <Typography variant="body2" sx={{ color: colors.text.secondary }}>
                            {ground.companyName}
                          </Typography>
                        </Box>
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                          <Rating
                            value={4.5}
                            precision={0.5}
                            size="small"
                            readOnly
                            sx={{
                              '& .MuiRating-iconFilled': { color: colors.accent.gold },
                              '& .MuiRating-iconEmpty': { color: alpha(colors.neutral.white, 0.2) },
                            }}
                          />
                          <Typography variant="body2" sx={{ color: colors.text.muted }}>
                            (4.5)
                          </Typography>
                        </Box>
                      </CardContent>
                    </Card>
                  </Grid>
                ))}
          </Grid>

          <Box sx={{ textAlign: 'center', mt: 6 }}>
            <Button
              variant="outlined"
              size="large"
              onClick={() => navigate('/grounds')}
              endIcon={<ArrowForward />}
              sx={{
                borderWidth: 2,
                borderColor: colors.primary.main,
                color: colors.primary.main,
                px: 5,
                py: 1.5,
                fontWeight: 700,
                letterSpacing: '0.1em',
                '&:hover': {
                  borderWidth: 2,
                  bgcolor: alpha(colors.primary.main, 0.1),
                },
              }}
            >
              View All Grounds
            </Button>
          </Box>
        </Container>
      </Box>

      {/* Features Section */}
      <Box sx={{ py: 12, bgcolor: colors.background.default }}>
        <Container maxWidth="lg">
          <Box sx={{ textAlign: 'center', mb: 8 }}>
            <Typography
              variant="overline"
              sx={{
                color: colors.primary.main,
                fontWeight: 600,
                letterSpacing: '0.2em',
                mb: 2,
                display: 'block',
              }}
            >
              Why Us
            </Typography>
            <Typography
              variant="h2"
              sx={{
                fontWeight: 700,
                color: colors.text.primary,
                mb: 2,
                fontFamily: '"Oswald", sans-serif',
              }}
            >
              Why Choose FutsalBook?
            </Typography>
            <Box
              sx={{
                width: 60,
                height: 3,
                bgcolor: colors.primary.main,
                mx: 'auto',
              }}
            />
          </Box>

          <Grid container spacing={4}>
            {features.map((feature, index) => (
              <Grid item xs={12} sm={6} md={3} key={index}>
                <Box
                  sx={{
                    height: '100%',
                    textAlign: 'center',
                    p: 4,
                    borderRadius: 2,
                    bgcolor: colors.background.card,
                    border: `1px solid ${alpha(colors.neutral.white, 0.05)}`,
                    transition: 'all 0.3s ease',
                    '&:hover': {
                      borderColor: alpha(colors.primary.main, 0.3),
                      transform: 'translateY(-8px)',
                      '& .feature-icon': {
                        bgcolor: colors.primary.main,
                        color: '#FFFFFF',
                        boxShadow: `0 0 30px ${alpha(colors.primary.main, 0.5)}`,
                      },
                    },
                  }}
                >
                  <Box
                    className="feature-icon"
                    sx={{
                      width: 80,
                      height: 80,
                      borderRadius: 2,
                      bgcolor: alpha(colors.primary.main, 0.1),
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      mx: 'auto',
                      mb: 3,
                      color: colors.primary.main,
                      transition: 'all 0.3s ease',
                    }}
                  >
                    {feature.icon}
                  </Box>
                  <Typography
                    variant="h6"
                    gutterBottom
                    sx={{
                      fontWeight: 700,
                      color: colors.text.primary,
                      fontFamily: '"Oswald", sans-serif',
                      textTransform: 'uppercase',
                      letterSpacing: '0.05em',
                    }}
                  >
                    {feature.title}
                  </Typography>
                  <Typography variant="body2" sx={{ color: colors.text.secondary, lineHeight: 1.7 }}>
                    {feature.description}
                  </Typography>
                </Box>
              </Grid>
            ))}
          </Grid>
        </Container>
      </Box>

      {/* CTA Section */}
      <Box
        sx={{
          py: 12,
          position: 'relative',
          overflow: 'hidden',
          bgcolor: colors.background.card,
        }}
      >
        {/* Background Pattern */}
        <Box
          sx={{
            position: 'absolute',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            backgroundImage: 'url(https://images.unsplash.com/photo-1552667466-07770ae110d0?w=1920&q=80)',
            backgroundSize: 'cover',
            backgroundPosition: 'center',
            opacity: 0.1,
          }}
        />

        <Container maxWidth="md" sx={{ position: 'relative', zIndex: 1 }}>
          <Box sx={{ textAlign: 'center' }}>
            <Typography
              variant="h2"
              sx={{
                color: colors.text.primary,
                fontWeight: 700,
                mb: 3,
                fontFamily: '"Oswald", sans-serif',
                textTransform: 'uppercase',
              }}
            >
              Ready to <span style={{ color: colors.primary.main }}>Play</span>?
            </Typography>
            <Typography
              variant="h6"
              sx={{
                color: colors.text.secondary,
                mb: 5,
                fontWeight: 400,
                maxWidth: 500,
                mx: 'auto',
              }}
            >
              Join thousands of players who book their futsal games with us every day
            </Typography>
            <Button
              variant="contained"
              size="large"
              onClick={() => navigate(isAuthenticated ? '/grounds' : '/register')}
              sx={{
                bgcolor: colors.primary.main,
                color: '#FFFFFF',
                px: 6,
                py: 2,
                fontSize: '1rem',
                fontWeight: 700,
                letterSpacing: '0.1em',
                '&:hover': {
                  bgcolor: colors.primary.light,
                  transform: 'translateY(-3px)',
                  boxShadow: `0 10px 40px ${alpha(colors.primary.main, 0.5)}`,
                },
              }}
              endIcon={<ArrowForward />}
            >
              {isAuthenticated ? 'Find a Ground' : 'Get Started Free'}
            </Button>
          </Box>
        </Container>
      </Box>
    </Box>
  );
};

export default Home;
