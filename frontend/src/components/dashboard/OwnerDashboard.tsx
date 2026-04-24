import React, { useCallback, useEffect, useState } from 'react';
import {
  Container,
  Grid,
  Paper,
  Typography,
  Box,
  CircularProgress,
  Alert,
  Card,
  CardContent,
  Button,
} from '@mui/material';
import {
  TrendingUp as TrendingUpIcon,
  Event as EventIcon,
  People as PeopleIcon,
  Stadium as StadiumIcon,
} from '@mui/icons-material';
import { useAuth } from '../../contexts/AuthContext';
import { companyService } from '../../services/companyService';
import { reportService } from '../../services/reportService';
import { groundService } from '../../services/groundService';
import { RevenueReportData } from '../../types';

const OwnerDashboard: React.FC = () => {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [revenueData, setRevenueData] = useState<RevenueReportData | null>(null);
  const [totalGrounds, setTotalGrounds] = useState(0);
  const { user } = useAuth();

  const fetchDashboardData = useCallback(async () => {
    if (!user) return;

    try {
      setLoading(true);

      // Fetch revenue report
      const report = await reportService.generateRevenueReport();
      setRevenueData(report.reportData as RevenueReportData);

      const companies = await companyService.getMyCompanies();
      const groundsByCompany = await Promise.all(
        companies.map((company) => groundService.getGroundsByCompany(company.id))
      );
      setTotalGrounds(groundsByCompany.flat().length);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load dashboard data');
    } finally {
      setLoading(false);
    }
  }, [user]);

  useEffect(() => {
    if (user) {
      fetchDashboardData();
    }
  }, [user, fetchDashboardData]);

  const handleGenerateReport = async (type: 'revenue' | 'bookings' | 'customers') => {
    if (!user) return;

    try {
      setLoading(true);
      if (type === 'revenue') {
        await reportService.generateRevenueReport();
      } else if (type === 'bookings') {
        await reportService.generateBookingsReport();
      } else {
        await reportService.generateCustomersReport();
      }
      alert(`${type.charAt(0).toUpperCase() + type.slice(1)} report generated successfully!`);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to generate report');
    } finally {
      setLoading(false);
    }
  };

  if (loading && !revenueData) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="60vh">
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Container sx={{ mt: 4, mb: 4 }}>
      <Typography variant="h4" gutterBottom>
        Owner Dashboard
      </Typography>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Box display="flex" alignItems="center" justifyContent="space-between">
                <Box>
                  <Typography color="textSecondary" gutterBottom>
                    Total Revenue
                  </Typography>
                  <Typography variant="h5">
                    NPR {revenueData?.totalRevenue.toFixed(2) || '0.00'}
                  </Typography>
                </Box>
                <TrendingUpIcon color="primary" sx={{ fontSize: 40 }} />
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Box display="flex" alignItems="center" justifyContent="space-between">
                <Box>
                  <Typography color="textSecondary" gutterBottom>
                    Total Bookings
                  </Typography>
                  <Typography variant="h5">{revenueData?.totalBookings || 0}</Typography>
                </Box>
                <EventIcon color="secondary" sx={{ fontSize: 40 }} />
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Box display="flex" alignItems="center" justifyContent="space-between">
                <Box>
                  <Typography color="textSecondary" gutterBottom>
                    Avg Booking Value
                  </Typography>
                  <Typography variant="h5">
                    NPR {revenueData?.averageBookingValue.toFixed(2) || '0.00'}
                  </Typography>
                </Box>
                <PeopleIcon color="success" sx={{ fontSize: 40 }} />
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Box display="flex" alignItems="center" justifyContent="space-between">
                <Box>
                  <Typography color="textSecondary" gutterBottom>
                    Total Grounds
                  </Typography>
                  <Typography variant="h5">{totalGrounds}</Typography>
                </Box>
                <StadiumIcon color="info" sx={{ fontSize: 40 }} />
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      <Paper sx={{ p: 3, mb: 3 }}>
        <Typography variant="h6" gutterBottom>
          Generate Reports
        </Typography>
        <Box display="flex" gap={2} flexWrap="wrap">
          <Button
            variant="contained"
            onClick={() => handleGenerateReport('revenue')}
            disabled={loading}
          >
            Revenue Report
          </Button>
          <Button
            variant="contained"
            color="secondary"
            onClick={() => handleGenerateReport('bookings')}
            disabled={loading}
          >
            Bookings Report
          </Button>
          <Button
            variant="contained"
            color="success"
            onClick={() => handleGenerateReport('customers')}
            disabled={loading}
          >
            Customers Report
          </Button>
        </Box>
      </Paper>

      {revenueData?.revenueByGround && (
        <Paper sx={{ p: 3 }}>
          <Typography variant="h6" gutterBottom>
            Revenue by Ground
          </Typography>
          <Grid container spacing={2}>
            {Object.entries(revenueData.revenueByGround).map(([ground, revenue]) => (
              <Grid item xs={12} sm={6} md={4} key={ground}>
                <Box sx={{ p: 2, border: '1px solid #e0e0e0', borderRadius: 1 }}>
                  <Typography variant="subtitle1">{ground}</Typography>
                  <Typography variant="h6" color="primary">
                    NPR {revenue.toFixed(2)}
                  </Typography>
                </Box>
              </Grid>
            ))}
          </Grid>
        </Paper>
      )}
    </Container>
  );
};

export default OwnerDashboard;
