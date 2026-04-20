import React from 'react';
import { Card, CardContent, Box, Typography, Rating, Avatar } from '@mui/material';
import { Review } from '../../../types';

interface ReviewCardProps {
  review: Review;
}

const ReviewCard: React.FC<ReviewCardProps> = ({ review }) => {
  const getInitials = (name: string) => {
    const parts = name.split(' ');
    return parts.length > 1
      ? `${parts[0][0]}${parts[1][0]}`.toUpperCase()
      : name.substring(0, 2).toUpperCase();
  };

  return (
    <Card>
      <CardContent>
        <Box display="flex" gap={2}>
          <Avatar sx={{ bgcolor: 'primary.main' }}>{getInitials(review.userName)}</Avatar>
          <Box flex={1}>
            <Box display="flex" justifyContent="space-between" alignItems="start" mb={1}>
              <Box>
                <Typography variant="subtitle1" fontWeight="bold">
                  {review.userName}
                </Typography>
                <Typography variant="caption" color="text.secondary">
                  {review.groundName}
                </Typography>
              </Box>
              <Typography variant="caption" color="text.secondary">
                {new Date(review.createdAt).toLocaleDateString()}
              </Typography>
            </Box>

            <Rating value={review.rating} readOnly size="small" sx={{ mb: 1 }} />

            {review.reviewText && (
              <Typography variant="body2" color="text.secondary">
                {review.reviewText}
              </Typography>
            )}
          </Box>
        </Box>
      </CardContent>
    </Card>
  );
};

export default ReviewCard;
