import React, { useEffect, useState } from 'react';
import {
  Grid,
  Card,
  CardContent,
  CardMedia,
  Typography,
  Button,
  Box,
  CircularProgress,
  Alert,
  TextField,
  Container,
  InputAdornment,
  Chip,
  Rating,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Paper,
  alpha,
  Slider,
} from '@mui/material';
import {
  Search as SearchIcon,
  FilterList as FilterIcon,
  LocationOn,
  Clear as ClearIcon,
  PlayArrow,
  TuneOutlined,
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { FutsalGround } from '../../types';
import { groundService } from '../../services/groundService';
import { colors } from '../../theme/theme';

const surfaceTypes = ['Turf', 'Grass', 'Indoor', 'Artificial'];

const GroundList: React.FC = () => {
  const [grounds, setGrounds] = useState<FutsalGround[]>([]);
  const [filteredGrounds, setFilteredGrounds] = useState<FutsalGround[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [searchTerm, setSearchTerm] = useState('');
  const [surfaceFilter, setSurfaceFilter] = useState('');
  const [priceRange, setPriceRange] = useState<number[]>([0, 5000]);
  const [showFilters, setShowFilters] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    fetchGrounds();
  }, []);

  useEffect(() => {
    filterGrounds();
  }, [searchTerm, surfaceFilter, priceRange, grounds]);

  const fetchGrounds = async () => {
    try {
      setLoading(true);
      const data = await groundService.getAllGrounds();
      setGrounds(data);
      setFilteredGrounds(data);

      if (data.length > 0) {
        const maxPrice = Math.max(...data.map(g => g.pricePerHour));
        setPriceRange([0, maxPrice]);
      }
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load grounds');
    } finally {
      setLoading(false);
    }
  };

  const filterGrounds = () => {
    let filtered = [...grounds];

    if (searchTerm.trim()) {
      const term = searchTerm.toLowerCase();
      filtered = filtered.filter(
        (ground) =>
          ground.name.toLowerCase().includes(term) ||
          ground.surfaceType.toLowerCase().includes(term) ||
          ground.companyName.toLowerCase().includes(term)
      );
    }

    if (surfaceFilter) {
      filtered = filtered.filter(
        (ground) => ground.surfaceType.toLowerCase() === surfaceFilter.toLowerCase()
      );
    }

    filtered = filtered.filter(
      (ground) =>
        ground.pricePerHour >= priceRange[0] && ground.pricePerHour <= priceRange[1]
    );

    setFilteredGrounds(filtered);
  };

  const clearFilters = () => {
    setSearchTerm('');
    setSurfaceFilter('');
    if (grounds.length > 0) {
      const maxPrice = Math.max(...grounds.map(g => g.pricePerHour));
      setPriceRange([0, maxPrice]);
    }
  };

  const handleViewDetails = (groundId: string) => {
    navigate(`/grounds/${groundId}`);
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

  return (
    <Box sx={{ bgcolor: colors.background.default, minHeight: '100vh', pb: 8, pt: 10 }}>
      {/* Hero Section */}
      <Box
        sx={{
          py: { xs: 6, md: 10 },
          position: 'relative',
          overflow: 'hidden',
          bgcolor: colors.background.paper,
        }}
      >
        <Container maxWidth="lg">
          <Box sx={{ textAlign: 'center', mb: 4 }}>
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
              Explore Venues
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
              Find Your Perfect Pitch
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
                maxWidth: 500,
                mx: 'auto',
              }}
            >
              Discover and book the best futsal grounds near you with instant confirmation
            </Typography>
          </Box>

          {/* Search Bar */}
          <Paper
            sx={{
              p: 1.5,
              display: 'flex',
              alignItems: 'center',
              maxWidth: 700,
              mx: 'auto',
              bgcolor: colors.background.card,
              border: `1px solid ${alpha(colors.neutral.white, 0.1)}`,
              gap: 1,
            }}
          >
            <TextField
              fullWidth
              placeholder="Search by name, surface type, or location..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <SearchIcon sx={{ color: colors.text.muted }} />
                  </InputAdornment>
                ),
                disableUnderline: true,
                sx: {
                  '& input': { color: colors.text.primary },
                  '& input::placeholder': { color: colors.text.muted },
                },
              }}
              variant="standard"
              sx={{ px: 1 }}
            />
            <Button
              variant="contained"
              onClick={() => setShowFilters(!showFilters)}
              startIcon={<TuneOutlined />}
              sx={{
                bgcolor: colors.primary.main,
                color: '#FFFFFF',
                whiteSpace: 'nowrap',
                px: 3,
                '&:hover': { bgcolor: colors.primary.light },
              }}
            >
              Filters
            </Button>
          </Paper>
        </Container>
      </Box>

      <Container maxWidth="lg" sx={{ mt: 4 }}>
        {/* Filters Panel */}
        {showFilters && (
          <Paper
            sx={{
              p: 4,
              mb: 4,
              bgcolor: colors.background.card,
              border: `1px solid ${alpha(colors.neutral.white, 0.1)}`,
            }}
          >
            <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
              <Typography
                variant="h6"
                sx={{
                  fontWeight: 700,
                  color: colors.text.primary,
                  fontFamily: '"Oswald", sans-serif',
                  textTransform: 'uppercase',
                  letterSpacing: '0.05em',
                }}
              >
                Filters
              </Typography>
              <Button
                size="small"
                startIcon={<ClearIcon />}
                onClick={clearFilters}
                sx={{ color: colors.primary.main }}
              >
                Clear All
              </Button>
            </Box>
            <Grid container spacing={4}>
              <Grid item xs={12} sm={6} md={4}>
                <FormControl fullWidth>
                  <InputLabel sx={{ color: colors.text.secondary }}>Surface Type</InputLabel>
                  <Select
                    value={surfaceFilter}
                    label="Surface Type"
                    onChange={(e) => setSurfaceFilter(e.target.value)}
                    sx={{
                      bgcolor: alpha(colors.neutral.white, 0.03),
                      '& .MuiOutlinedInput-notchedOutline': {
                        borderColor: alpha(colors.neutral.white, 0.1),
                      },
                    }}
                  >
                    <MenuItem value="">All Types</MenuItem>
                    {surfaceTypes.map((type) => (
                      <MenuItem key={type} value={type}>
                        {type}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
              </Grid>
              <Grid item xs={12} sm={6} md={4}>
                <Typography
                  gutterBottom
                  sx={{
                    color: colors.text.secondary,
                    fontSize: '0.875rem',
                    mb: 2,
                  }}
                >
                  Price Range: <span style={{ color: colors.primary.main, fontWeight: 700 }}>NPR {priceRange[0]} - {priceRange[1]}</span>
                </Typography>
                <Slider
                  value={priceRange}
                  onChange={(_, newValue) => setPriceRange(newValue as number[])}
                  valueLabelDisplay="auto"
                  min={0}
                  max={grounds.length > 0 ? Math.max(...grounds.map(g => g.pricePerHour)) : 5000}
                  step={100}
                  sx={{
                    color: colors.primary.main,
                    '& .MuiSlider-thumb': {
                      '&:hover, &.Mui-focusVisible': {
                        boxShadow: `0 0 0 8px ${alpha(colors.primary.main, 0.2)}`,
                      },
                    },
                  }}
                />
              </Grid>
            </Grid>
          </Paper>
        )}

        {/* Results Summary */}
        <Box display="flex" justifyContent="space-between" alignItems="center" mb={4}>
          <Typography
            variant="h6"
            sx={{
              fontWeight: 700,
              color: colors.text.primary,
              fontFamily: '"Oswald", sans-serif',
            }}
          >
            {filteredGrounds.length} Ground{filteredGrounds.length !== 1 ? 's' : ''} Found
          </Typography>
          {(searchTerm || surfaceFilter) && (
            <Box display="flex" gap={1} flexWrap="wrap">
              {searchTerm && (
                <Chip
                  label={`Search: ${searchTerm}`}
                  onDelete={() => setSearchTerm('')}
                  size="small"
                  sx={{
                    bgcolor: alpha(colors.primary.main, 0.1),
                    color: colors.primary.main,
                    fontWeight: 600,
                    '& .MuiChip-deleteIcon': { color: colors.primary.main },
                  }}
                />
              )}
              {surfaceFilter && (
                <Chip
                  label={`Surface: ${surfaceFilter}`}
                  onDelete={() => setSurfaceFilter('')}
                  size="small"
                  sx={{
                    bgcolor: alpha(colors.primary.main, 0.1),
                    color: colors.primary.main,
                    fontWeight: 600,
                    '& .MuiChip-deleteIcon': { color: colors.primary.main },
                  }}
                />
              )}
            </Box>
          )}
        </Box>

        {error && (
          <Alert
            severity="error"
            sx={{
              mb: 3,
              bgcolor: alpha(colors.status.error, 0.1),
              borderColor: alpha(colors.status.error, 0.3),
              color: colors.status.error,
            }}
          >
            {error}
          </Alert>
        )}

        {filteredGrounds.length === 0 ? (
          <Paper
            sx={{
              p: 8,
              textAlign: 'center',
              bgcolor: colors.background.card,
              border: `1px solid ${alpha(colors.neutral.white, 0.1)}`,
            }}
          >
            <Typography
              variant="h6"
              sx={{ color: colors.text.secondary, mb: 2 }}
            >
              No grounds found
            </Typography>
            <Typography variant="body2" sx={{ color: colors.text.muted, mb: 3 }}>
              Try adjusting your search or filters
            </Typography>
            <Button
              variant="outlined"
              onClick={clearFilters}
              sx={{
                borderColor: colors.primary.main,
                color: colors.primary.main,
                '&:hover': { bgcolor: alpha(colors.primary.main, 0.1) },
              }}
            >
              Clear Filters
            </Button>
          </Paper>
        ) : (
          <Grid container spacing={3}>
            {filteredGrounds.map((ground, index) => (
              <Grid item xs={12} sm={6} md={4} key={ground.id}>
                <Card
                  sx={{
                    height: '100%',
                    cursor: 'pointer',
                    position: 'relative',
                    overflow: 'hidden',
                    display: 'flex',
                    flexDirection: 'column',
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
                  onClick={() => handleViewDetails(ground.id)}
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
                      height="200"
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
                        View Details
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
                  <CardContent sx={{ flexGrow: 1, p: 3 }}>
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
                  <Box sx={{ p: 3, pt: 0 }}>
                    <Button
                      variant="contained"
                      fullWidth
                      onClick={(e) => {
                        e.stopPropagation();
                        handleViewDetails(ground.id);
                      }}
                      sx={{
                        bgcolor: colors.primary.main,
                        color: '#FFFFFF',
                        fontWeight: 700,
                        letterSpacing: '0.1em',
                        '&:hover': {
                          bgcolor: colors.primary.light,
                        },
                      }}
                    >
                      View Details & Book
                    </Button>
                  </Box>
                </Card>
              </Grid>
            ))}
          </Grid>
        )}
      </Container>
    </Box>
  );
};

export default GroundList;
