import React, { useState, useEffect } from 'react';
import {
  Container,
  Paper,
  Typography,
  Box,
  Button,
  Alert,
  CircularProgress,
  Step,
  Stepper,
  StepLabel,
} from '@mui/material';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import { useToast } from '../../contexts/ToastContext';
import { bookingService } from '../../services/bookingService';
import { groundService } from '../../services/groundService';
import { FutsalGround } from '../../types';

const steps = ['Select Ground', 'Choose Time Slot', 'Confirm Booking', 'Payment'];

const CreateBooking: React.FC = () => {
  const [searchParams] = useSearchParams();
  const groundId = searchParams.get('groundId');
  const [activeStep, setActiveStep] = useState(0);
  const [ground, setGround] = useState<FutsalGround | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const { user } = useAuth();
  const { showToast } = useToast();
  const navigate = useNavigate();

  useEffect(() => {
    if (groundId) {
      fetchGround(groundId);
    }
  }, [groundId]);

  const fetchGround = async (id: string) => {
    try {
      setLoading(true);
      const data = await groundService.getGroundById(id);
      setGround(data);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load ground details');
    } finally {
      setLoading(false);
    }
  };

  const handleNext = () => {
    setActiveStep((prevActiveStep) => prevActiveStep + 1);
  };

  const handleBack = () => {
    setActiveStep((prevActiveStep) => prevActiveStep - 1);
  };

  const handleCreateBooking = async () => {
    if (!user || !groundId) return;

    try {
      setLoading(true);
      setError('');

      // This is a simplified version
      // In a real implementation, you would select a time slot first
      const bookingData = {
        groundId: groundId,
        slotId: 'temp-slot-id', // Should come from time slot selection
      };

      const booking = await bookingService.createBooking(bookingData);
      setSuccess('Booking created successfully!');
      showToast('Booking created. Continue to payment.', 'success');

      // Navigate to payment page
      setTimeout(() => {
        navigate(`/payment?bookingId=${booking.id}`);
      }, 1500);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to create booking');
    } finally {
      setLoading(false);
    }
  };

  if (loading && !ground) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="60vh">
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Container sx={{ mt: 4, mb: 4 }}>
      <Paper elevation={3} sx={{ p: 3 }}>
        <Typography variant="h4" gutterBottom>
          Book Your Futsal Ground
        </Typography>

        <Stepper activeStep={activeStep} sx={{ mt: 3, mb: 4 }}>
          {steps.map((label) => (
            <Step key={label}>
              <StepLabel>{label}</StepLabel>
            </Step>
          ))}
        </Stepper>

        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}

        {success && (
          <Alert severity="success" sx={{ mb: 2 }}>
            {success}
          </Alert>
        )}

        {ground && (
          <Box sx={{ mb: 3 }}>
            <Typography variant="h6" gutterBottom>
              Selected Ground: {ground.name}
            </Typography>
            <Typography variant="body1" color="text.secondary">
              Price: NPR {ground.pricePerHour}/hour
            </Typography>
          </Box>
        )}

        <Box sx={{ display: 'flex', justifyContent: 'space-between', mt: 3 }}>
          <Button disabled={activeStep === 0} onClick={handleBack}>
            Back
          </Button>
          <Box>
            {activeStep === steps.length - 1 ? (
              <Button variant="contained" onClick={handleCreateBooking} disabled={loading}>
                {loading ? 'Processing...' : 'Proceed to Payment'}
              </Button>
            ) : (
              <Button variant="contained" onClick={handleNext}>
                Next
              </Button>
            )}
          </Box>
        </Box>

        <Typography variant="body2" color="text.secondary" sx={{ mt: 3 }}>
          Note: This is a simplified booking flow. In a complete implementation, you would select
          available time slots before proceeding to payment.
        </Typography>
      </Paper>
    </Container>
  );
};

export default CreateBooking;
