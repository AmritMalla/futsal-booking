import React from 'react';
import { Card, CardContent, Typography, Box } from '@mui/material';
import TrendingUpIcon from '@mui/icons-material/TrendingUp';
import TrendingDownIcon from '@mui/icons-material/TrendingDown';

interface StatCardProps {
  title: string;
  value: React.ReactNode;
  icon?: React.ReactNode;
  trend?: number;
  trendLabel?: string;
  color?: 'primary' | 'secondary' | 'success' | 'error' | 'warning' | 'info';
}

const StatCard: React.FC<StatCardProps> = ({
  title,
  value,
  icon,
  trend,
  trendLabel,
  color = 'primary',
}) => {
  const getTrendColor = () => {
    if (!trend) return 'text.secondary';
    return trend > 0 ? 'success.main' : 'error.main';
  };

  const getTrendIcon = () => {
    if (!trend) return null;
    return trend > 0 ? <TrendingUpIcon fontSize="small" /> : <TrendingDownIcon fontSize="small" />;
  };

  return (
    <Card sx={{ height: '100%' }}>
      <CardContent>
        <Box display="flex" justifyContent="space-between" alignItems="flex-start" mb={2}>
          <Typography color="text.secondary" variant="subtitle2" gutterBottom>
            {title}
          </Typography>
          {icon && (
            <Box
              sx={{
                backgroundColor: `${color}.light`,
                color: `${color}.main`,
                borderRadius: 1,
                p: 0.5,
                display: 'flex',
                alignItems: 'center',
              }}
            >
              {icon}
            </Box>
          )}
        </Box>

        <Typography variant="h4" component="div" fontWeight="bold" mb={1}>
          {value}
        </Typography>

        {trend !== undefined && (
          <Box display="flex" alignItems="center" gap={0.5}>
            <Box
              sx={{
                color: getTrendColor(),
                display: 'flex',
                alignItems: 'center',
                gap: 0.5,
              }}
            >
              {getTrendIcon()}
              <Typography variant="body2" fontWeight="medium">
                {trend > 0 ? '+' : ''}
                {trend}%
              </Typography>
            </Box>
            {trendLabel && (
              <Typography variant="body2" color="text.secondary">
                {trendLabel}
              </Typography>
            )}
          </Box>
        )}
      </CardContent>
    </Card>
  );
};

export default StatCard;
