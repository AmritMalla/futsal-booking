import React, { useCallback, useEffect, useState } from 'react';
import {
  Box,
  Container,
  Typography,
  Grid,
  Button,
  Alert,
} from '@mui/material';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs';
import dayjs, { Dayjs } from 'dayjs';
import { adminService } from '../../../services/adminService';
import {
  RevenueAnalytics,
  BookingAnalytics,
  UserAnalytics,
} from '../../../types/admin';
import RevenueChart from './RevenueChart';
import BookingTrendsChart from './BookingTrendsChart';
import UserGrowthChart from './UserGrowthChart';

const AnalyticsOverview: React.FC = () => {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [revenueData, setRevenueData] = useState<RevenueAnalytics | null>(null);
  const [bookingData, setBookingData] = useState<BookingAnalytics | null>(null);
  const [userData, setUserData] = useState<UserAnalytics | null>(null);
  const [startDate, setStartDate] = useState<Dayjs | null>(dayjs().subtract(1, 'month'));
  const [endDate, setEndDate] = useState<Dayjs | null>(dayjs());

  const fetchAnalytics = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const params = {
        startDate: startDate?.toISOString(),
        endDate: endDate?.toISOString(),
      };

      const [revenue, bookings, users] = await Promise.all([
        adminService.getRevenueAnalytics(params),
        adminService.getBookingAnalytics(params),
        adminService.getUserAnalytics(),
      ]);

      setRevenueData(revenue);
      setBookingData(bookings);
      setUserData(users);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to fetch analytics data');
    } finally {
      setLoading(false);
    }
  }, [endDate, startDate]);

  useEffect(() => {
    fetchAnalytics();
  }, [fetchAnalytics]);

  const handleApplyFilters = () => {
    fetchAnalytics();
  };

  return (
    <Container maxWidth="xl" sx={{ mt: 4, mb: 4 }}>
      <Box mb={4}>
        <Typography variant="h4" component="h1" gutterBottom fontWeight="bold">
          Analytics Overview
        </Typography>
        <Typography variant="body1" color="text.secondary">
          Comprehensive analytics and insights for your futsal business
        </Typography>
      </Box>

      <LocalizationProvider dateAdapter={AdapterDayjs}>
        <Box mb={4} display="flex" gap={2} alignItems="center">
          <DatePicker
            label="Start Date"
            value={startDate}
            onChange={(newValue) => setStartDate(newValue)}
            slotProps={{ textField: { size: 'small' } }}
          />
          <DatePicker
            label="End Date"
            value={endDate}
            onChange={(newValue) => setEndDate(newValue)}
            slotProps={{ textField: { size: 'small' } }}
          />
          <Button variant="contained" onClick={handleApplyFilters}>
            Apply Filters
          </Button>
        </Box>
      </LocalizationProvider>

      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}

      <Grid container spacing={3}>
        <Grid item xs={12}>
          {revenueData && <RevenueChart data={revenueData} loading={loading} />}
        </Grid>

        <Grid item xs={12}>
          {bookingData && <BookingTrendsChart data={bookingData} loading={loading} />}
        </Grid>

        <Grid item xs={12}>
          {userData && <UserGrowthChart data={userData} loading={loading} />}
        </Grid>
      </Grid>
    </Container>
  );
};

export default AnalyticsOverview;
