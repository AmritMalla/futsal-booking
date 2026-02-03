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
} from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { FutsalGround } from '../../types';
import { groundService } from '../../services/groundService';

const GroundList: React.FC = () => {
  const [grounds, setGrounds] = useState<FutsalGround[]>([]);
  const [filteredGrounds, setFilteredGrounds] = useState<FutsalGround[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [searchTerm, setSearchTerm] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    fetchGrounds();
  }, []);

  useEffect(() => {
    if (searchTerm.trim() === '') {
      setFilteredGrounds(grounds);
    } else {
      const filtered = grounds.filter(
        (ground) =>
          ground.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
          ground.surfaceType.toLowerCase().includes(searchTerm.toLowerCase()) ||
          ground.companyName.toLowerCase().includes(searchTerm.toLowerCase())
      );
      setFilteredGrounds(filtered);
    }
  }, [searchTerm, grounds]);

  const fetchGrounds = async () => {
    try {
      setLoading(true);
      const data = await groundService.getAllGrounds();
      setGrounds(data);
      setFilteredGrounds(data);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load grounds');
    } finally {
      setLoading(false);
    }
  };

  const handleViewDetails = (groundId: string) => {
    navigate(`/grounds/${groundId}`);
  };

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="60vh">
        <CircularProgress />
      </Box>
    );
  }

  if (error) {
    return (
      <Container sx={{ mt: 4 }}>
        <Alert severity="error">{error}</Alert>
      </Container>
    );
  }

  return (
    <Container sx={{ mt: 4, mb: 4 }}>
      <Typography variant="h4" gutterBottom>
        Browse Futsal Grounds
      </Typography>

      <TextField
        fullWidth
        label="Search by name, surface type, or company"
        variant="outlined"
        value={searchTerm}
        onChange={(e) => setSearchTerm(e.target.value)}
        sx={{ mb: 4 }}
      />

      {filteredGrounds.length === 0 ? (
        <Alert severity="info">No grounds found matching your search.</Alert>
      ) : (
        <Grid container spacing={3}>
          {filteredGrounds.map((ground) => (
            <Grid item xs={12} sm={6} md={4} key={ground.id}>
              <Card>
                <CardMedia
                  component="img"
                  height="200"
                  image={ground.imageUrl || '/placeholder-ground.jpg'}
                  alt={ground.name}
                  sx={{ objectFit: 'cover' }}
                />
                <CardContent>
                  <Typography variant="h6" gutterBottom>
                    {ground.name}
                  </Typography>
                  <Typography variant="body2" color="text.secondary" gutterBottom>
                    {ground.companyName}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    Surface: {ground.surfaceType}
                  </Typography>
                  <Typography variant="h6" color="primary" sx={{ mt: 1 }}>
                    NPR {ground.pricePerHour}/hour
                  </Typography>
                  <Button
                    variant="contained"
                    fullWidth
                    sx={{ mt: 2 }}
                    onClick={() => handleViewDetails(ground.id)}
                  >
                    View Details & Book
                  </Button>
                </CardContent>
              </Card>
            </Grid>
          ))}
        </Grid>
      )}
    </Container>
  );
};

export default GroundList;
