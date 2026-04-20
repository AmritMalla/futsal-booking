import React, { useState } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Box,
  Typography,
  Divider,
  TextField,
  Alert,
  CircularProgress,
  Grid,
  Rating,
  Avatar,
} from '@mui/material';
import { Review } from '../../../types';
import { adminService } from '../../../services/adminService';

interface ReviewDetailModalProps {
  open: boolean;
  review: Review;
  onClose: () => void;
  onSuccess: () => void;
}

const ReviewDetailModal: React.FC<ReviewDetailModalProps> = ({
  open,
  review,
  onClose,
  onSuccess,
}) => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [formData, setFormData] = useState({
    rating: review.rating,
    reviewText: review.reviewText || '',
  });

  const handleSubmit = async () => {
    setError(null);
    setLoading(true);

    try {
      await adminService.updateReview(review.id, {
        userId: review.userId,
        groundId: review.groundId,
        rating: formData.rating,
        reviewText: formData.reviewText,
      });
      onSuccess();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to update review');
    } finally {
      setLoading(false);
    }
  };

  const getInitials = (name: string) => {
    const parts = name.split(' ');
    return parts.length > 1
      ? `${parts[0][0]}${parts[1][0]}`.toUpperCase()
      : name.substring(0, 2).toUpperCase();
  };

  const hasChanges =
    formData.rating !== review.rating || formData.reviewText !== (review.reviewText || '');

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>
        <Typography variant="h6">Review Details</Typography>
      </DialogTitle>

      <DialogContent dividers>
        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}

        <Grid container spacing={3}>
          <Grid item xs={12}>
            <Box display="flex" gap={2} alignItems="center">
              <Avatar sx={{ bgcolor: 'primary.main', width: 56, height: 56 }}>
                {getInitials(review.userName)}
              </Avatar>
              <Box>
                <Typography variant="h6">{review.userName}</Typography>
                <Typography variant="body2" color="text.secondary">
                  {review.groundName}
                </Typography>
                <Typography variant="caption" color="text.secondary">
                  Posted on {new Date(review.createdAt).toLocaleDateString()}
                </Typography>
              </Box>
            </Box>
          </Grid>

          <Grid item xs={12}>
            <Divider />
          </Grid>

          <Grid item xs={12}>
            <Typography variant="subtitle2" color="text.secondary" gutterBottom>
              Review ID
            </Typography>
            <Typography variant="body2">{review.id}</Typography>
          </Grid>

          <Grid item xs={12}>
            <Divider />
          </Grid>

          <Grid item xs={12}>
            <Typography variant="subtitle2" color="text.secondary" gutterBottom>
              Edit Rating
            </Typography>
            <Rating
              value={formData.rating}
              onChange={(_, newValue) => {
                if (newValue !== null) {
                  setFormData({ ...formData, rating: newValue });
                }
              }}
              size="large"
            />
          </Grid>

          <Grid item xs={12}>
            <Typography variant="subtitle2" color="text.secondary" gutterBottom>
              Edit Review Text
            </Typography>
            <TextField
              fullWidth
              multiline
              rows={4}
              value={formData.reviewText}
              onChange={(e) => setFormData({ ...formData, reviewText: e.target.value })}
              placeholder="Enter review text..."
            />
          </Grid>

          {!hasChanges && (
            <Grid item xs={12}>
              <Alert severity="info">
                Make changes to the rating or review text to update.
              </Alert>
            </Grid>
          )}
        </Grid>
      </DialogContent>

      <DialogActions>
        <Button onClick={onClose} disabled={loading}>
          Close
        </Button>
        <Button
          onClick={handleSubmit}
          variant="contained"
          disabled={loading || !hasChanges}
          startIcon={loading ? <CircularProgress size={16} /> : null}
        >
          Update Review
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default ReviewDetailModal;
