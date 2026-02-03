import React from 'react';
import { Container, Typography, Button, Box, Grid, Card, CardContent } from '@mui/material';
import {
  SportsFootball,
  Event,
  Payment,
  Dashboard,
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

const Home: React.FC = () => {
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();

  const features = [
    {
      icon: <SportsFootball sx={{ fontSize: 60 }} color="primary" />,
      title: 'Browse Grounds',
      description: 'Find the perfect futsal ground for your game',
    },
    {
      icon: <Event sx={{ fontSize: 60 }} color="secondary" />,
      title: 'Easy Booking',
      description: 'Book your preferred time slot with just a few clicks',
    },
    {
      icon: <Payment sx={{ fontSize: 60 }} color="success" />,
      title: 'Secure Payment',
      description: 'Safe and secure payment processing',
    },
    {
      icon: <Dashboard sx={{ fontSize: 60 }} color="info" />,
      title: 'Manage Business',
      description: 'Ground owners can manage their business efficiently',
    },
  ];

  return (
    <Box>
      {/* Hero Section */}
      <Box
        sx={{
          background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
          color: 'white',
          py: 10,
          textAlign: 'center',
        }}
      >
        <Container>
          <Typography variant="h2" gutterBottom fontWeight="bold">
            Welcome to Futsal Booking System
          </Typography>
          <Typography variant="h5" sx={{ mb: 4 }}>
            Book your futsal ground in seconds
          </Typography>
          <Box sx={{ display: 'flex', gap: 2, justifyContent: 'center' }}>
            <Button
              variant="contained"
              size="large"
              sx={{
                bgcolor: 'white',
                color: 'primary.main',
                '&:hover': { bgcolor: 'grey.100' },
              }}
              onClick={() => navigate('/grounds')}
            >
              Browse Grounds
            </Button>
            {!isAuthenticated && (
              <Button
                variant="outlined"
                size="large"
                sx={{
                  borderColor: 'white',
                  color: 'white',
                  '&:hover': { borderColor: 'white', bgcolor: 'rgba(255,255,255,0.1)' },
                }}
                onClick={() => navigate('/register')}
              >
                Sign Up
              </Button>
            )}
          </Box>
        </Container>
      </Box>

      {/* Features Section */}
      <Container sx={{ py: 8 }}>
        <Typography variant="h4" align="center" gutterBottom fontWeight="bold">
          Why Choose Us?
        </Typography>
        <Typography variant="body1" align="center" color="text.secondary" sx={{ mb: 6 }}>
          Everything you need to book and manage futsal grounds
        </Typography>

        <Grid container spacing={4}>
          {features.map((feature, index) => (
            <Grid item xs={12} sm={6} md={3} key={index}>
              <Card
                sx={{
                  height: '100%',
                  display: 'flex',
                  flexDirection: 'column',
                  alignItems: 'center',
                  textAlign: 'center',
                  p: 2,
                }}
              >
                <CardContent>
                  {feature.icon}
                  <Typography variant="h6" gutterBottom sx={{ mt: 2 }}>
                    {feature.title}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    {feature.description}
                  </Typography>
                </CardContent>
              </Card>
            </Grid>
          ))}
        </Grid>
      </Container>

      {/* CTA Section */}
      <Box sx={{ bgcolor: 'grey.100', py: 8 }}>
        <Container>
          <Box sx={{ textAlign: 'center' }}>
            <Typography variant="h4" gutterBottom fontWeight="bold">
              Ready to Get Started?
            </Typography>
            <Typography variant="body1" color="text.secondary" sx={{ mb: 4 }}>
              Join hundreds of players and ground owners on our platform
            </Typography>
            <Button
              variant="contained"
              size="large"
              onClick={() => navigate(isAuthenticated ? '/grounds' : '/register')}
            >
              {isAuthenticated ? 'Browse Grounds' : 'Create Account'}
            </Button>
          </Box>
        </Container>
      </Box>
    </Box>
  );
};

export default Home;
