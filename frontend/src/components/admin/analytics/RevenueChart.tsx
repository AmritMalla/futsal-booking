import React from 'react';
import { Card, CardContent, Typography, Box } from '@mui/material';
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from 'recharts';
import { RevenueAnalytics } from '../../../types/admin';

interface RevenueChartProps {
  data: RevenueAnalytics;
  loading?: boolean;
}

const RevenueChart: React.FC<RevenueChartProps> = ({ data, loading }) => {
  const chartData = Object.entries(data.revenueByCompany || {}).map(([company, revenue]) => ({
    name: company,
    revenue: revenue,
  }));

  if (loading) {
    return (
      <Card>
        <CardContent>
          <Typography variant="h6" gutterBottom>
            Revenue Analytics
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
            Revenue Analytics
          </Typography>
          <Box display="flex" gap={3} mt={2}>
            <Box>
              <Typography variant="body2" color="text.secondary">
                Total Revenue
              </Typography>
              <Typography variant="h5" fontWeight="bold" color="primary">
                NPR {data.totalRevenue?.toLocaleString() || 0}
              </Typography>
            </Box>
            <Box>
              <Typography variant="body2" color="text.secondary">
                Monthly Revenue
              </Typography>
              <Typography variant="h5" fontWeight="bold">
                NPR {data.monthlyRevenue?.toLocaleString() || 0}
              </Typography>
            </Box>
            <Box>
              <Typography variant="body2" color="text.secondary">
                Growth
              </Typography>
              <Typography
                variant="h5"
                fontWeight="bold"
                color={data.revenueGrowth >= 0 ? 'success.main' : 'error.main'}
              >
                {data.revenueGrowth >= 0 ? '+' : ''}
                {data.revenueGrowth?.toFixed(1) || 0}%
              </Typography>
            </Box>
          </Box>
        </Box>

        <ResponsiveContainer width="100%" height={300}>
          <LineChart data={chartData}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="name" />
            <YAxis />
            <Tooltip
              formatter={(value: number | undefined) =>
                `NPR ${value != null ? value.toLocaleString() : '0'}`
              }
            />
            <Legend />
            <Line
              type="monotone"
              dataKey="revenue"
              stroke="#1976d2"
              strokeWidth={2}
              dot={{ r: 4 }}
              activeDot={{ r: 6 }}
            />
          </LineChart>
        </ResponsiveContainer>
      </CardContent>
    </Card>
  );
};

export default RevenueChart;
