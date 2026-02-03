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
} from '@mui/material';
import { useParams, useNavigate } from 'react-router-dom';
import { FutsalGround } from '../../types';
import { groundService } from '../../services/groundService';
import { useAuth } from '../../contexts/AuthContext';

const GroundDetails: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const [ground, setGround] = useState<FutsalGround | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (id) {
      fetchGround(id);
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

  const handleBookNow = () => {
    if (!isAuthenticated) {
      navigate('/login', { state: { from: `/grounds/${id}` } });
      return;
    }
    navigate(`/booking/new?groundId=${id}`);
  };

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="60vh">
        <CircularProgress />
      </Box>
    );
  }

  if (error || !ground) {
    return (
      <Container sx={{ mt: 4 }}>
        <Alert severity="error">{error || 'Ground not found'}</Alert>
        <Button variant="outlined" sx={{ mt: 2 }} onClick={() => navigate('/grounds')}>
          Back to Grounds
        </Button>
      </Container>
    );
  }

  return (
    <Container sx={{ mt: 4, mb: 4 }}>
      <Paper elevation={3} sx={{ p: 3 }}>
        <Grid container spacing={3}>
          <Grid item xs={12} md={6}>
            <Box
              component="img"
              src={ground.imageUrl || '/placeholder-ground.jpg'}
              alt={ground.name}
              sx={{
                width: '100%',
                height: 'auto',
                borderRadius: 2,
                maxHeight: '400px',
                objectFit: 'cover',
              }}
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <Typography variant="h4" gutterBottom>
              {ground.name}
            </Typography>
            <Typography variant="h6" color="text.secondary" gutterBottom>
              {ground.companyName}
            </Typography>

            <Box sx={{ mt: 3 }}>
              <Typography variant="body1" gutterBottom>
                <strong>Surface Type:</strong>
              </Typography>
              <Chip label={ground.surfaceType} color="primary" sx={{ mb: 2 }} />
            </Box>

            <Box sx={{ mt: 2 }}>
              <Typography variant="body1" gutterBottom>
                <strong>Price:</strong>
              </Typography>
              <Typography variant="h4" color="primary">
                NPR {ground.pricePerHour}
                <Typography variant="body2" component="span" color="text.secondary">
                  {' '}
                  / hour
                </Typography>
              </Typography>
            </Box>

            <Box sx={{ mt: 2 }}>
              <Typography variant="body2" color="text.secondary">
                Added on: {new Date(ground.createdAt).toLocaleDateString()}
              </Typography>
            </Box>

            <Button
              variant="contained"
              size="large"
              fullWidth
              sx={{ mt: 4 }}
              onClick={handleBookNow}
            >
              Book Now
            </Button>
          </Grid>
        </Grid>
      </Paper>
    </Container>
  );
};

export default GroundDetails;
