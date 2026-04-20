import React, { useEffect, useState } from 'react';
import {
  Box,
  Container,
  Typography,
  TextField,
  MenuItem,
  Alert,
  IconButton,
  Rating,
} from '@mui/material';
import VisibilityIcon from '@mui/icons-material/Visibility';
import DeleteIcon from '@mui/icons-material/Delete';
import { adminService } from '../../../services/adminService';
import { Review } from '../../../types';
import { FutsalGround } from '../../../types';
import { DataTable, Column, ConfirmDialog } from '../common';
import ReviewDetailModal from './ReviewDetailModal';

const ReviewList: React.FC = () => {
  const [reviews, setReviews] = useState<Review[]>([]);
  const [grounds, setGrounds] = useState<FutsalGround[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [groundFilter, setGroundFilter] = useState<string>('');
  const [ratingFilter, setRatingFilter] = useState<string>('');
  const [selectedReview, setSelectedReview] = useState<Review | null>(null);
  const [detailModalOpen, setDetailModalOpen] = useState(false);
  const [deleteDialog, setDeleteDialog] = useState<{ open: boolean; id: string | null }>({
    open: false,
    id: null,
  });

  const fetchReviews = async () => {
    setLoading(true);
    setError(null);
    try {
      const filters = {
        groundId: groundFilter || undefined,
        minRating: ratingFilter ? parseInt(ratingFilter) : undefined,
      };
      const data = await adminService.getAllReviews(filters);
      setReviews(data);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to fetch reviews');
    } finally {
      setLoading(false);
    }
  };

  const fetchGrounds = async () => {
    try {
      const data = await adminService.getAllGrounds();
      setGrounds(data);
    } catch (err: any) {
      console.error('Failed to fetch grounds:', err);
    }
  };

  useEffect(() => {
    fetchReviews();
    fetchGrounds();
  }, [groundFilter, ratingFilter]);

  const handleViewDetails = (review: Review) => {
    setSelectedReview(review);
    setDetailModalOpen(true);
  };

  const handleDetailModalClose = () => {
    setDetailModalOpen(false);
    setSelectedReview(null);
  };

  const handleDetailModalSuccess = () => {
    setDetailModalOpen(false);
    setSelectedReview(null);
    setSuccess('Review updated successfully');
    fetchReviews();
    setTimeout(() => setSuccess(null), 3000);
  };

  const handleDeleteClick = (id: string) => {
    setDeleteDialog({ open: true, id });
  };

  const handleDeleteConfirm = async () => {
    if (!deleteDialog.id) return;

    try {
      await adminService.deleteReview(deleteDialog.id);
      setSuccess('Review deleted successfully');
      fetchReviews();
      setTimeout(() => setSuccess(null), 3000);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to delete review');
    } finally {
      setDeleteDialog({ open: false, id: null });
    }
  };

  const columns: Column<Review>[] = [
    {
      id: 'userName',
      label: 'Customer',
      minWidth: 150,
    },
    {
      id: 'groundName',
      label: 'Ground',
      minWidth: 150,
    },
    {
      id: 'rating',
      label: 'Rating',
      minWidth: 150,
      format: (value) => <Rating value={value} readOnly size="small" />,
    },
    {
      id: 'reviewText',
      label: 'Review',
      minWidth: 250,
      format: (value) => (
        <Typography variant="body2" noWrap sx={{ maxWidth: 250 }}>
          {value || 'No comment'}
        </Typography>
      ),
    },
    {
      id: 'createdAt',
      label: 'Date',
      minWidth: 120,
      format: (value) => new Date(value).toLocaleDateString(),
    },
    {
      id: 'actions',
      label: 'Actions',
      minWidth: 100,
      align: 'center',
      format: (_, row) => (
        <Box display="flex" gap={1} justifyContent="center">
          <IconButton
            size="small"
            color="primary"
            onClick={() => handleViewDetails(row)}
          >
            <VisibilityIcon fontSize="small" />
          </IconButton>
          <IconButton
            size="small"
            color="error"
            onClick={() => handleDeleteClick(row.id)}
          >
            <DeleteIcon fontSize="small" />
          </IconButton>
        </Box>
      ),
    },
  ];

  const averageRating =
    reviews.length > 0
      ? (reviews.reduce((sum, r) => sum + r.rating, 0) / reviews.length).toFixed(1)
      : '0.0';

  return (
    <Container maxWidth="xl" sx={{ mt: 4, mb: 4 }}>
      <Box mb={4} display="flex" justifyContent="space-between" alignItems="center">
        <Box>
          <Typography variant="h4" component="h1" gutterBottom fontWeight="bold">
            Reviews
          </Typography>
          <Typography variant="body1" color="text.secondary">
            Manage customer reviews and ratings
          </Typography>
        </Box>
        <Box
          sx={{
            backgroundColor: 'primary.light',
            color: 'primary.contrastText',
            px: 3,
            py: 1.5,
            borderRadius: 2,
          }}
        >
          <Typography variant="caption">Average Rating</Typography>
          <Box display="flex" alignItems="center" gap={1}>
            <Typography variant="h5" fontWeight="bold">
              {averageRating}
            </Typography>
            <Rating value={parseFloat(averageRating)} readOnly size="small" />
          </Box>
        </Box>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 3 }} onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      {success && (
        <Alert severity="success" sx={{ mb: 3 }} onClose={() => setSuccess(null)}>
          {success}
        </Alert>
      )}

      <Box mb={3} display="flex" gap={2}>
        <TextField
          select
          label="Filter by Ground"
          value={groundFilter}
          onChange={(e) => setGroundFilter(e.target.value)}
          size="small"
          sx={{ minWidth: 200 }}
        >
          <MenuItem value="">All Grounds</MenuItem>
          {grounds.map((ground) => (
            <MenuItem key={ground.id} value={ground.id}>
              {ground.name}
            </MenuItem>
          ))}
        </TextField>

        <TextField
          select
          label="Minimum Rating"
          value={ratingFilter}
          onChange={(e) => setRatingFilter(e.target.value)}
          size="small"
          sx={{ minWidth: 150 }}
        >
          <MenuItem value="">All Ratings</MenuItem>
          <MenuItem value="5">5 Stars</MenuItem>
          <MenuItem value="4">4+ Stars</MenuItem>
          <MenuItem value="3">3+ Stars</MenuItem>
          <MenuItem value="2">2+ Stars</MenuItem>
          <MenuItem value="1">1+ Star</MenuItem>
        </TextField>
      </Box>

      <DataTable
        columns={columns}
        data={reviews}
        loading={loading}
        emptyMessage="No reviews found"
      />

      {selectedReview && (
        <ReviewDetailModal
          open={detailModalOpen}
          review={selectedReview}
          onClose={handleDetailModalClose}
          onSuccess={handleDetailModalSuccess}
        />
      )}

      <ConfirmDialog
        open={deleteDialog.open}
        title="Delete Review"
        message="Are you sure you want to delete this review? This action cannot be undone."
        confirmText="Delete"
        confirmColor="error"
        onConfirm={handleDeleteConfirm}
        onCancel={() => setDeleteDialog({ open: false, id: null })}
      />
    </Container>
  );
};

export default ReviewList;
