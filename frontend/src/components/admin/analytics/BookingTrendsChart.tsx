import React from 'react';
import { Card, CardContent, Typography, Box } from '@mui/material';
import {
  AreaChart,
  Area,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from 'recharts';
import { BookingAnalytics } from '../../../types/admin';

interface BookingTrendsChartProps {
  data: BookingAnalytics;
  loading?: boolean;
}

const BookingTrendsChart: React.FC<BookingTrendsChartProps> = ({ data, loading }) => {
  const chartData = data.bookingTrends || [];

  if (loading) {
    return (
      <Card>
        <CardContent>
          <Typography variant="h6" gutterBottom>
            Booking Trends
          </Typography>
          <Box display="flex" justifyContent="center" alignItems="center" height={300}>
            <Typography color="text.secondary">Loading...</Typography>
          </Box>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card>
      <CardContent>
        <Box mb={2}>
          <Typography variant="h6" gutterBottom>
            Booking Trends (Last 30 Days)
          </Typography>
          <Box display="flex" gap={3} mt={2}>
            <Box>
              <Typography variant="body2" color="text.secondary">
                Total Bookings
              </Typography>
              <Typography variant="h5" fontWeight="bold" color="primary">
                {data.totalBookings || 0}
              </Typography>
            </Box>
            <Box>
              <Typography variant="body2" color="text.secondary">
                Average Booking Value
              </Typography>
              <Typography variant="h5" fontWeight="bold">
                NPR {data.averageBookingValue?.toLocaleString() || 0}
              </Typography>
            </Box>
          </Box>
        </Box>

        <ResponsiveContainer width="100%" height={300}>
          <AreaChart data={chartData}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="date" />
            <YAxis />
            <Tooltip />
            <Legend />
            <Area
              type="monotone"
              dataKey="bookingCount"
              stroke="#1976d2"
              fill="#1976d2"
              fillOpacity={0.6}
            />
          </AreaChart>
        </ResponsiveContainer>

        {data.peakHours && data.peakHours.length > 0 && (
          <Box mt={3}>
            <Typography variant="subtitle2" color="text.secondary" gutterBottom>
              Peak Hours
            </Typography>
            <Box display="flex" gap={2} flexWrap="wrap">
              {data.peakHours.map((peak, index) => (
                <Box
                  key={index}
                  sx={{
                    backgroundColor: 'primary.light',
                    color: 'primary.contrastText',
                    borderRadius: 1,
                    px: 2,
                    py: 1,
                  }}
                >
                  <Typography variant="body2" fontWeight="bold">
                    {peak.hour}
                  </Typography>
                  <Typography variant="caption">
                    {peak.bookingCount} bookings
                  </Typography>
                </Box>
              ))}
            </Box>
          </Box>
        )}
      </CardContent>
    </Card>
  );
};

export default BookingTrendsChart;
