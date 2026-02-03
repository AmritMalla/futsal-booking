import React, { useState, useEffect } from 'react';
import {
  Box,
  Typography,
  Rating,
  TextField,
  Button,
  Paper,
  Avatar,
  Divider,
  CircularProgress,
  Alert,
  IconButton,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  DialogActions,
  Chip,
  alpha,
} from '@mui/material';
import {
  Star as StarIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  Person as PersonIcon,
} from '@mui/icons-material';
import { Review, ReviewRequest } from '../../types';
import { reviewService } from '../../services/reviewService';
import { useAuth } from '../../contexts/AuthContext';
import { colors } from '../../theme/theme';
import { format } from 'date-fns';

interface ReviewSectionProps {
  groundId: string;
  groundName: string;
}

const ReviewSection: React.FC<ReviewSectionProps> = ({ groundId, groundName }) => {
  const { user, isAuthenticated } = useAuth();
  const [reviews, setReviews] = useState<Review[]>([]);
  const [averageRating, setAverageRating] = useState<number>(0);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Form state
  const [rating, setRating] = useState<number | null>(0);
  const [reviewText, setReviewText] = useState('');
  const [editingReview, setEditingReview] = useState<Review | null>(null);

  // Delete dialog
  const [deleteDialog, setDeleteDialog] = useState<{ open: boolean; reviewId: string }>({
    open: false,
    reviewId: '',
  });

  useEffect(() => {
    fetchReviews();
  }, [groundId]);

  const fetchReviews = async () => {
    try {
      setLoading(true);
      const [reviewsData, avgRating] = await Promise.all([
        reviewService.getReviewsByGround(groundId),
        reviewService.getAverageRating(groundId),
      ]);
      setReviews(reviewsData);
      setAverageRating(avgRating);
    } catch (err: any) {
      console.error('Failed to fetch reviews:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async () => {
    if (!user || !rating || rating === 0) {
      setError('Please provide a rating');
      return;
    }

    try {
      setSubmitting(true);
      setError('');

      if (editingReview) {
        // Update existing review
        const updatedReview: Review = {
          ...editingReview,
          rating,
          reviewText: reviewText || undefined,
        };
        await reviewService.updateReview(editingReview.id, updatedReview);
        setSuccess('Review updated successfully!');
      } else {
        // Create new review
        const reviewData: ReviewRequest = {
          userId: user.id,
          groundId,
          rating,
          reviewText: reviewText || undefined,
        };
        await reviewService.createReview(reviewData);
        setSuccess('Review submitted successfully!');
      }

      // Reset form
      setRating(0);
      setReviewText('');
      setEditingReview(null);
      fetchReviews();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to submit review');
    } finally {
      setSubmitting(false);
    }
  };

  const handleEdit = (review: Review) => {
    setEditingReview(review);
    setRating(review.rating);
    setReviewText(review.reviewText || '');
  };

  const handleCancelEdit = () => {
    setEditingReview(null);
    setRating(0);
    setReviewText('');
  };

  const handleDelete = async () => {
    try {
      await reviewService.deleteReview(deleteDialog.reviewId);
      setDeleteDialog({ open: false, reviewId: '' });
      setSuccess('Review deleted successfully!');
      fetchReviews();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to delete review');
    }
  };

  const userHasReviewed = reviews.some(r => r.userId === user?.id);

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" py={4}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box>
      {/* Rating Summary */}
      <Paper sx={{ p: 3, mb: 3 }}>
        <Box display="flex" alignItems="center" gap={3} flexWrap="wrap">
          <Box textAlign="center">
            <Typography variant="h2" fontWeight={700} color="primary">
              {averageRating.toFixed(1)}
            </Typography>
            <Rating value={averageRating} precision={0.5} readOnly size="large" />
            <Typography variant="body2" color="text.secondary">
              {reviews.length} review{reviews.length !== 1 ? 's' : ''}
            </Typography>
          </Box>
          <Divider orientation="vertical" flexItem sx={{ display: { xs: 'none', sm: 'block' } }} />
          <Box flex={1}>
            <Typography variant="h6" gutterBottom>
              What players say about {groundName}
            </Typography>
            <Box display="flex" gap={1} flexWrap="wrap">
              {[5, 4, 3, 2, 1].map((star) => {
                const count = reviews.filter(r => r.rating === star).length;
                return (
                  <Chip
                    key={star}
                    icon={<StarIcon sx={{ color: colors.accent.yellow }} />}
                    label={`${star} (${count})`}
                    variant="outlined"
                    size="small"
                  />
                );
              })}
            </Box>
          </Box>
        </Box>
      </Paper>

      {/* Write Review Form */}
      {isAuthenticated && (!userHasReviewed || editingReview) && (
        <Paper sx={{ p: 3, mb: 3 }}>
          <Typography variant="h6" gutterBottom>
            {editingReview ? 'Edit Your Review' : 'Write a Review'}
          </Typography>

          {error && (
            <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError('')}>
              {error}
            </Alert>
          )}
          {success && (
            <Alert severity="success" sx={{ mb: 2 }} onClose={() => setSuccess('')}>
              {success}
            </Alert>
          )}

          <Box mb={2}>
            <Typography component="legend" gutterBottom>
              Your Rating *
            </Typography>
            <Rating
              value={rating}
              onChange={(_, newValue) => setRating(newValue)}
              size="large"
              sx={{
                '& .MuiRating-iconFilled': {
                  color: colors.accent.yellow,
                },
              }}
            />
          </Box>

          <TextField
            fullWidth
            multiline
            rows={3}
            label="Your Review (optional)"
            placeholder="Share your experience at this ground..."
            value={reviewText}
            onChange={(e) => setReviewText(e.target.value)}
            sx={{ mb: 2 }}
          />

          <Box display="flex" gap={2}>
            <Button
              variant="contained"
              onClick={handleSubmit}
              disabled={submitting || !rating}
            >
              {submitting ? 'Submitting...' : editingReview ? 'Update Review' : 'Submit Review'}
            </Button>
            {editingReview && (
              <Button variant="outlined" onClick={handleCancelEdit}>
                Cancel
              </Button>
            )}
          </Box>
        </Paper>
      )}

      {!isAuthenticated && (
        <Alert severity="info" sx={{ mb: 3 }}>
          Please log in to write a review
        </Alert>
      )}

      {/* Reviews List */}
      <Typography variant="h6" gutterBottom>
        All Reviews
      </Typography>

      {reviews.length === 0 ? (
        <Paper sx={{ p: 3, textAlign: 'center' }}>
          <Typography color="text.secondary">
            No reviews yet. Be the first to review this ground!
          </Typography>
        </Paper>
      ) : (
        <Box>
          {reviews.map((review, index) => (
            <Paper
              key={review.id}
              sx={{
                p: 3,
                mb: 2,
                bgcolor: review.userId === user?.id ? alpha(colors.primary.main, 0.05) : 'white',
              }}
            >
              <Box display="flex" justifyContent="space-between" alignItems="flex-start">
                <Box display="flex" gap={2}>
                  <Avatar sx={{ bgcolor: colors.primary.main }}>
                    {review.userName?.charAt(0).toUpperCase() || <PersonIcon />}
                  </Avatar>
                  <Box>
                    <Box display="flex" alignItems="center" gap={1}>
                      <Typography fontWeight={600}>
                        {review.userName || 'Anonymous'}
                      </Typography>
                      {review.userId === user?.id && (
                        <Chip label="You" size="small" color="primary" />
                      )}
                    </Box>
                    <Rating value={review.rating} readOnly size="small" />
                    <Typography variant="body2" color="text.secondary">
                      {format(new Date(review.createdAt), 'MMM d, yyyy')}
                    </Typography>
                  </Box>
                </Box>

                {review.userId === user?.id && !editingReview && (
                  <Box>
                    <IconButton size="small" onClick={() => handleEdit(review)}>
                      <EditIcon fontSize="small" />
                    </IconButton>
                    <IconButton
                      size="small"
                      color="error"
                      onClick={() => setDeleteDialog({ open: true, reviewId: review.id })}
                    >
                      <DeleteIcon fontSize="small" />
                    </IconButton>
                  </Box>
                )}
              </Box>

              {review.reviewText && (
                <Typography sx={{ mt: 2, pl: 7 }}>
                  {review.reviewText}
                </Typography>
              )}
            </Paper>
          ))}
        </Box>
      )}

      {/* Delete Confirmation Dialog */}
      <Dialog
        open={deleteDialog.open}
        onClose={() => setDeleteDialog({ open: false, reviewId: '' })}
      >
        <DialogTitle>Delete Review</DialogTitle>
        <DialogContent>
          <DialogContentText>
            Are you sure you want to delete your review? This action cannot be undone.
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleteDialog({ open: false, reviewId: '' })}>
            Cancel
          </Button>
          <Button onClick={handleDelete} color="error" variant="contained">
            Delete
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default ReviewSection;
