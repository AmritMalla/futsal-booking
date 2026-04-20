import React from 'react';
import { Chip } from '@mui/material';
import { BookingStatus } from '../../../types';

interface BookingStatusChipProps {
  status: BookingStatus;
  size?: 'small' | 'medium';
}

const BookingStatusChip: React.FC<BookingStatusChipProps> = ({ status, size = 'small' }) => {
  const getColor = (): 'success' | 'error' | 'default' => {
    switch (status) {
      case BookingStatus.CONFIRMED:
        return 'success';
      case BookingStatus.CANCELLED:
        return 'error';
      case BookingStatus.COMPLETED:
        return 'default';
      default:
        return 'default';
    }
  };

  return <Chip label={status} color={getColor()} size={size} />;
};

export default BookingStatusChip;
