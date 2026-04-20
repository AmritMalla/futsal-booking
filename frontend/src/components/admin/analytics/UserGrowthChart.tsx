import React from 'react';
import { Card, CardContent, Typography, Box, Chip } from '@mui/material';
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
} from 'recharts';
import { UserAnalytics } from '../../../types/admin';

interface UserGrowthChartProps {
  data: UserAnalytics;
  loading?: boolean;
}

const COLORS = ['#1976d2', '#dc004e', '#388e3c', '#ff9800'];

const UserGrowthChart: React.FC<UserGrowthChartProps> = ({ data, loading }) => {
  const roleData = Object.entries(data.usersByRole || {}).map(([role, count]) => ({
    name: role,
    value: count,
  }));

  if (loading) {
    return (
      <Card>
        <CardContent>
          <Typography variant="h6" gutterBottom>
            User Analytics
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
            User Analytics
          </Typography>
          <Box display="flex" gap={3} mt={2}>
            <Box>
              <Typography variant="body2" color="text.secondary">
                Total Users
              </Typography>
              <Typography variant="h5" fontWeight="bold" color="primary">
                {data.totalUsers || 0}
              </Typography>
            </Box>
            <Box>
              <Typography variant="body2" color="text.secondary">
                New This Month
              </Typography>
              <Typography variant="h5" fontWeight="bold">
                {data.newUsersThisMonth || 0}
              </Typography>
            </Box>
            <Box>
              <Typography variant="body2" color="text.secondary">
                Active Users
              </Typography>
              <Typography variant="h5" fontWeight="bold" color="success.main">
                {data.activeUsers || 0}
              </Typography>
            </Box>
            <Box>
              <Typography variant="body2" color="text.secondary">
                Growth Rate
              </Typography>
              <Typography
                variant="h5"
                fontWeight="bold"
                color={data.userGrowthRate >= 0 ? 'success.main' : 'error.main'}
              >
                {data.userGrowthRate >= 0 ? '+' : ''}
                {data.userGrowthRate?.toFixed(1) || 0}%
              </Typography>
            </Box>
          </Box>
        </Box>

        <Box display="flex" gap={2} alignItems="center">
          <Box flex={1}>
            <Typography variant="subtitle2" color="text.secondary" gutterBottom>
              Users by Role
            </Typography>
            <ResponsiveContainer width="100%" height={250}>
              <PieChart>
                <Pie
                  data={roleData}
                  cx="50%"
                  cy="50%"
                  labelLine={false}
                  label={({ name, value }) => `${name}: ${value}`}
                  outerRadius={80}
                  fill="#8884d8"
                  dataKey="value"
                >
                  {roleData.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip />
              </PieChart>
            </ResponsiveContainer>
          </Box>

          <Box flex={1}>
            <Typography variant="subtitle2" color="text.secondary" gutterBottom>
              Top Customers
            </Typography>
            <Box display="flex" flexDirection="column" gap={1}>
              {data.topCustomers?.slice(0, 5).map((customer, index) => (
                <Box
                  key={customer.id}
                  sx={{
                    border: 1,
                    borderColor: 'divider',
                    borderRadius: 1,
                    p: 1.5,
                  }}
                >
                  <Box display="flex" justifyContent="space-between" alignItems="center">
                    <Box>
                      <Typography variant="body2" fontWeight="bold">
                        {index + 1}. {customer.name}
                      </Typography>
                      <Typography variant="caption" color="text.secondary">
                        {customer.email}
                      </Typography>
                    </Box>
                    <Box display="flex" gap={1}>
                      <Chip
                        label={`${customer.totalBookings} bookings`}
                        size="small"
                        color="primary"
                        variant="outlined"
                      />
                      <Chip
                        label={`NPR ${customer.totalSpent.toLocaleString()}`}
                        size="small"
                        color="success"
                      />
                    </Box>
                  </Box>
                </Box>
              ))}
            </Box>
          </Box>
        </Box>
      </CardContent>
    </Card>
  );
};

export default UserGrowthChart;
