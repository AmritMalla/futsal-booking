import React from 'react';
import { Box, Button, Container, Paper, Typography } from '@mui/material';
import { LockOutlined, HomeOutlined } from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';

const AccessDeniedPage: React.FC = () => {
  const navigate = useNavigate();

  return (
    <Container maxWidth="sm" sx={{ py: { xs: 8, md: 12 } }}>
      <Paper sx={{ p: { xs: 4, md: 6 }, textAlign: 'center' }}>
        <Box sx={{ mb: 2 }}>
          <LockOutlined color="warning" sx={{ fontSize: 64 }} />
        </Box>
        <Typography variant="overline" color="warning.main" sx={{ letterSpacing: '0.15em' }}>
          Access Restricted
        </Typography>
        <Typography variant="h4" sx={{ mt: 1, mb: 2, fontWeight: 700 }}>
          You don't have permission to view this page
        </Typography>
        <Typography variant="body1" color="text.secondary" sx={{ mb: 4 }}>
          This area is available only to the required role. You can head back to the public site or
          continue with the features available to your account.
        </Typography>
        <Box sx={{ display: 'flex', gap: 2, justifyContent: 'center', flexWrap: 'wrap' }}>
          <Button variant="contained" onClick={() => navigate('/')} startIcon={<HomeOutlined />}>
            Go Home
          </Button>
          <Button variant="outlined" onClick={() => navigate('/my-bookings')}>
            My Bookings
          </Button>
        </Box>
      </Paper>
    </Container>
  );
};

export default AccessDeniedPage;
