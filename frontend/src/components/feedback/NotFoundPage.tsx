import React from 'react';
import { Box, Button, Container, Paper, Typography } from '@mui/material';
import { ArrowBack, SearchOff } from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';

const NotFoundPage: React.FC = () => {
  const navigate = useNavigate();

  return (
    <Container maxWidth="sm" sx={{ py: { xs: 8, md: 12 } }}>
      <Paper sx={{ p: { xs: 4, md: 6 }, textAlign: 'center' }}>
        <Box sx={{ mb: 2 }}>
          <SearchOff color="primary" sx={{ fontSize: 64 }} />
        </Box>
        <Typography variant="overline" color="primary" sx={{ letterSpacing: '0.15em' }}>
          Page Missing
        </Typography>
        <Typography variant="h3" sx={{ mt: 1, mb: 2, fontWeight: 700 }}>
          404
        </Typography>
        <Typography variant="h5" sx={{ mb: 2 }}>
          We couldn't find that page
        </Typography>
        <Typography variant="body1" color="text.secondary" sx={{ mb: 4 }}>
          The link may be outdated, or the page may have been moved while the app was being improved.
        </Typography>
        <Box sx={{ display: 'flex', gap: 2, justifyContent: 'center', flexWrap: 'wrap' }}>
          <Button variant="contained" onClick={() => navigate('/')} startIcon={<ArrowBack />}>
            Back to Home
          </Button>
          <Button variant="outlined" onClick={() => navigate('/grounds')}>
            Browse Grounds
          </Button>
        </Box>
      </Paper>
    </Container>
  );
};

export default NotFoundPage;
