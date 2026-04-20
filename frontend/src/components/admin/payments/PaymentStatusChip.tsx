import React from 'react';
import { Chip } from '@mui/material';
import { PaymentStatus } from '../../../types';

interface PaymentStatusChipProps {
  status: PaymentStatus;
  size?: 'small' | 'medium';
}

const PaymentStatusChip: React.FC<PaymentStatusChipProps> = ({ status, size = 'small' }) => {
  const getColor = (): 'success' | 'error' | 'warning' | 'default' => {
    switch (status) {
      case PaymentStatus.SUCCESS:
        return 'success';
      case PaymentStatus.FAILED:
        return 'error';
      case PaymentStatus.PENDING:
        return 'warning';
      case PaymentStatus.REFUNDED:
        return 'default';
      default:
        return 'default';
    }
  };

  return <Chip label={status} color={getColor()} size={size} />;
};

export default PaymentStatusChip;
