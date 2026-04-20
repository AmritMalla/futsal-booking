import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Container,
  Grid,
  Paper,
  Typography,
  Box,
  Tabs,
  Tab,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  IconButton,
  Button,
  Chip,
  CircularProgress,
  Alert,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  DialogActions,
  TextField,
  InputAdornment,
  Card,
  CardContent,
  alpha,
  Avatar,
  Tooltip,
  Rating,
} from '@mui/material';
import {
  Delete as DeleteIcon,
  Search as SearchIcon,
  People as PeopleIcon,
  Business as BusinessIcon,
  Stadium as StadiumIcon,
  Event as EventIcon,
  PersonAdd as PersonAddIcon,
  Store as StoreIcon,
  Refresh as RefreshIcon,
  Warning as WarningIcon,
  Schedule as ScheduleIcon,
  Payment as PaymentIcon,
  RateReview as ReviewIcon,
  TrendingUp as TrendingUpIcon,
} from '@mui/icons-material';
import { User, FutsalGround, UserRole } from '../../types';
import { adminService } from '../../services/adminService';
import { AdminStats, FutsalCompany } from '../../types/admin';
import { colors } from '../../theme/theme';
import { StatCard } from './common';
import { RevenueChart, BookingTrendsChart } from './analytics';

interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
}

function TabPanel(props: TabPanelProps) {
  const { children, value, index, ...other } = props;
  return (
    <div
      role="tabpanel"
      hidden={value !== index}
      id={`admin-tabpanel-${index}`}
      aria-labelledby={`admin-tab-${index}`}
      {...other}
    >
      {value === index && <Box sx={{ py: 3 }}>{children}</Box>}
    </div>
  );
}

const AdminDashboard: React.FC = () => {
  const navigate = useNavigate();
  const [tabValue, setTabValue] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // Data states
  const [stats, setStats] = useState<AdminStats | null>(null);
  const [users, setUsers] = useState<User[]>([]);
  const [companies, setCompanies] = useState<FutsalCompany[]>([]);
  const [grounds, setGrounds] = useState<FutsalGround[]>([]);
  const [revenueAnalytics, setRevenueAnalytics] = useState<any>(null);
  const [bookingAnalytics, setBookingAnalytics] = useState<any>(null);

  // Search states
  const [userSearch, setUserSearch] = useState('');
  const [companySearch, setCompanySearch] = useState('');
  const [groundSearch, setGroundSearch] = useState('');

  // Delete dialog
  const [deleteDialog, setDeleteDialog] = useState<{
    open: boolean;
    type: 'user' | 'company' | 'ground';
    id: string;
    name: string;
  }>({ open: false, type: 'user', id: '', name: '' });

  useEffect(() => {
    fetchAllData();
  }, []);

  const fetchAllData = async () => {
    try {
      setLoading(true);
      setError('');

      const [statsData, usersData, companiesData, groundsData, revenueData, bookingData] = await Promise.all([
        adminService.getAdminStats(),
        adminService.getAllUsers(),
        adminService.getAllCompanies(),
        adminService.getAllGrounds(),
        adminService.getRevenueAnalytics().catch(() => null),
        adminService.getBookingAnalytics().catch(() => null),
      ]);

      setStats(statsData);
      setUsers(usersData);
      setCompanies(companiesData);
      setGrounds(groundsData);
      setRevenueAnalytics(revenueData);
      setBookingAnalytics(bookingData);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load data');
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async () => {
    try {
      const { type, id } = deleteDialog;

      if (type === 'user') {
        await adminService.deleteUser(id);
        setUsers(users.filter(u => u.id !== id));
      } else if (type === 'company') {
        await adminService.deleteCompany(id);
        setCompanies(companies.filter(c => c.id !== id));
      } else if (type === 'ground') {
        await adminService.deleteGround(id);
        setGrounds(grounds.filter(g => g.id !== id));
      }

      setDeleteDialog({ ...deleteDialog, open: false });
      fetchAllData(); // Refresh stats
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to delete');
    }
  };

  const openDeleteDialog = (type: 'user' | 'company' | 'ground', id: string, name: string) => {
    setDeleteDialog({ open: true, type, id, name });
  };

  const filteredUsers = users.filter(user =>
    user.name.toLowerCase().includes(userSearch.toLowerCase()) ||
    user.email.toLowerCase().includes(userSearch.toLowerCase())
  );

  const filteredCompanies = companies.filter(company =>
    company.name.toLowerCase().includes(companySearch.toLowerCase()) ||
    company.location?.toLowerCase().includes(companySearch.toLowerCase())
  );

  const filteredGrounds = grounds.filter(ground =>
    ground.name.toLowerCase().includes(groundSearch.toLowerCase()) ||
    ground.companyName.toLowerCase().includes(groundSearch.toLowerCase())
  );

  const getRoleColor = (role: UserRole) => {
    switch (role) {
      case UserRole.ADMIN:
        return 'error';
      case UserRole.OWNER:
        return 'primary';
      case UserRole.USER:
        return 'default';
      default:
        return 'default';
    }
  };

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="60vh">
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Container maxWidth="xl" sx={{ py: 4 }}>
      {/* Header */}
      <Box sx={{ mb: 4, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Box>
          <Typography variant="h4" fontWeight={700} gutterBottom>
            Admin Dashboard
          </Typography>
          <Typography variant="body1" color="text.secondary">
            Manage users, companies, and grounds
          </Typography>
        </Box>
        <Button
          variant="outlined"
          startIcon={<RefreshIcon />}
          onClick={fetchAllData}
        >
          Refresh
        </Button>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 3 }} onClose={() => setError('')}>
          {error}
        </Alert>
      )}

      {/* Analytics Charts */}
      {(revenueAnalytics || bookingAnalytics) && (
        <Grid container spacing={3} sx={{ mb: 4 }}>
          {revenueAnalytics && (
            <Grid item xs={12} md={6}>
              <RevenueChart data={revenueAnalytics} />
            </Grid>
          )}
          {bookingAnalytics && (
            <Grid item xs={12} md={6}>
              <BookingTrendsChart data={bookingAnalytics} />
            </Grid>
          )}
        </Grid>
      )}

      {/* Stats Cards */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Total Users"
            value={stats?.totalUsers || 0}
            icon={<PeopleIcon />}
            color="primary"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Total Bookings"
            value={stats?.totalBookings || 0}
            icon={<EventIcon />}
            color="success"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Total Revenue"
            value={`NPR ${stats?.totalRevenue?.toLocaleString() || 0}`}
            icon={<TrendingUpIcon />}
            color="info"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Average Rating"
            value={
              <Box display="flex" alignItems="center" gap={0.5}>
                <Typography variant="h4" component="span" fontWeight="bold">
                  {stats?.averageRating?.toFixed(1) || '0.0'}
                </Typography>
                <Rating value={stats?.averageRating || 0} readOnly size="small" />
              </Box>
            }
            icon={<ReviewIcon />}
            color="warning"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Companies"
            value={stats?.totalCompanies || 0}
            icon={<BusinessIcon />}
            color="secondary"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Futsal Grounds"
            value={stats?.totalGrounds || 0}
            icon={<StadiumIcon />}
            color="info"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Time Slots"
            value={stats?.totalTimeSlots || 0}
            icon={<ScheduleIcon />}
            color="primary"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Total Reviews"
            value={stats?.totalReviews || 0}
            icon={<ReviewIcon />}
            color="warning"
          />
        </Grid>
      </Grid>

      {/* Tabs */}
      <Paper sx={{ mb: 3 }}>
        <Tabs
          value={tabValue}
          onChange={(_, newValue) => setTabValue(newValue)}
          variant="scrollable"
          scrollButtons="auto"
          sx={{ borderBottom: 1, borderColor: 'divider', px: 2 }}
        >
          <Tab
            icon={<PeopleIcon />}
            iconPosition="start"
            label="Users"
          />
          <Tab
            icon={<BusinessIcon />}
            iconPosition="start"
            label="Companies"
          />
          <Tab
            icon={<StadiumIcon />}
            iconPosition="start"
            label="Grounds"
          />
          <Tab
            icon={<ScheduleIcon />}
            iconPosition="start"
            label="Time Slots"
          />
          <Tab
            icon={<EventIcon />}
            iconPosition="start"
            label="Bookings"
          />
          <Tab
            icon={<PaymentIcon />}
            iconPosition="start"
            label="Payments"
          />
          <Tab
            icon={<ReviewIcon />}
            iconPosition="start"
            label="Reviews"
          />
        </Tabs>

        {/* Users Tab */}
        <TabPanel value={tabValue} index={0}>
          <Box sx={{ px: 3 }}>
            <TextField
              fullWidth
              placeholder="Search users by name or email..."
              value={userSearch}
              onChange={(e) => setUserSearch(e.target.value)}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <SearchIcon />
                  </InputAdornment>
                ),
              }}
              sx={{ mb: 3 }}
            />
            <TableContainer>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Name</TableCell>
                    <TableCell>Email</TableCell>
                    <TableCell>Phone</TableCell>
                    <TableCell>Role</TableCell>
                    <TableCell>Joined</TableCell>
                    <TableCell align="right">Actions</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {filteredUsers.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={6} align="center">
                        <Typography color="text.secondary" sx={{ py: 3 }}>
                          No users found
                        </Typography>
                      </TableCell>
                    </TableRow>
                  ) : (
                    filteredUsers.map((user) => (
                      <TableRow key={user.id} hover>
                        <TableCell>
                          <Box display="flex" alignItems="center" gap={1}>
                            <Avatar sx={{ width: 32, height: 32, bgcolor: colors.primary.main }}>
                              {user.name.charAt(0).toUpperCase()}
                            </Avatar>
                            {user.name}
                          </Box>
                        </TableCell>
                        <TableCell>{user.email}</TableCell>
                        <TableCell>{user.phoneNumber || '-'}</TableCell>
                        <TableCell>
                          <Chip
                            label={user.role}
                            size="small"
                            color={getRoleColor(user.role)}
                          />
                        </TableCell>
                        <TableCell>
                          {new Date(user.createdAt).toLocaleDateString()}
                        </TableCell>
                        <TableCell align="right">
                          {user.role !== UserRole.ADMIN && (
                            <Tooltip title="Delete User">
                              <IconButton
                                color="error"
                                onClick={() => openDeleteDialog('user', user.id, user.name)}
                              >
                                <DeleteIcon />
                              </IconButton>
                            </Tooltip>
                          )}
                        </TableCell>
                      </TableRow>
                    ))
                  )}
                </TableBody>
              </Table>
            </TableContainer>
          </Box>
        </TabPanel>

        {/* Companies Tab */}
        <TabPanel value={tabValue} index={1}>
          <Box sx={{ px: 3 }}>
            <TextField
              fullWidth
              placeholder="Search companies by name or location..."
              value={companySearch}
              onChange={(e) => setCompanySearch(e.target.value)}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <SearchIcon />
                  </InputAdornment>
                ),
              }}
              sx={{ mb: 3 }}
            />
            <TableContainer>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Company Name</TableCell>
                    <TableCell>Location</TableCell>
                    <TableCell>Owner</TableCell>
                    <TableCell>Created</TableCell>
                    <TableCell align="right">Actions</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {filteredCompanies.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={5} align="center">
                        <Typography color="text.secondary" sx={{ py: 3 }}>
                          No companies found
                        </Typography>
                      </TableCell>
                    </TableRow>
                  ) : (
                    filteredCompanies.map((company) => (
                      <TableRow key={company.id} hover>
                        <TableCell>
                          <Box display="flex" alignItems="center" gap={1}>
                            <Avatar sx={{ width: 32, height: 32, bgcolor: colors.accent.blue }}>
                              <BusinessIcon sx={{ fontSize: 18 }} />
                            </Avatar>
                            {company.name}
                          </Box>
                        </TableCell>
                        <TableCell>{company.location || '-'}</TableCell>
                        <TableCell>{company.ownerName || '-'}</TableCell>
                        <TableCell>
                          {new Date(company.createdAt).toLocaleDateString()}
                        </TableCell>
                        <TableCell align="right">
                          <Tooltip title="Delete Company">
                            <IconButton
                              color="error"
                              onClick={() => openDeleteDialog('company', company.id, company.name)}
                            >
                              <DeleteIcon />
                            </IconButton>
                          </Tooltip>
                        </TableCell>
                      </TableRow>
                    ))
                  )}
                </TableBody>
              </Table>
            </TableContainer>
          </Box>
        </TabPanel>

        {/* Grounds Tab */}
        <TabPanel value={tabValue} index={2}>
          <Box sx={{ px: 3 }}>
            <TextField
              fullWidth
              placeholder="Search grounds by name or company..."
              value={groundSearch}
              onChange={(e) => setGroundSearch(e.target.value)}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <SearchIcon />
                  </InputAdornment>
                ),
              }}
              sx={{ mb: 3 }}
            />
            <TableContainer>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Ground Name</TableCell>
                    <TableCell>Company</TableCell>
                    <TableCell>Surface Type</TableCell>
                    <TableCell>Price/Hour</TableCell>
                    <TableCell>Created</TableCell>
                    <TableCell align="right">Actions</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {filteredGrounds.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={6} align="center">
                        <Typography color="text.secondary" sx={{ py: 3 }}>
                          No grounds found
                        </Typography>
                      </TableCell>
                    </TableRow>
                  ) : (
                    filteredGrounds.map((ground) => (
                      <TableRow key={ground.id} hover>
                        <TableCell>
                          <Box display="flex" alignItems="center" gap={1}>
                            <Avatar sx={{ width: 32, height: 32, bgcolor: colors.accent.teal }}>
                              <StadiumIcon sx={{ fontSize: 18 }} />
                            </Avatar>
                            {ground.name}
                          </Box>
                        </TableCell>
                        <TableCell>{ground.companyName}</TableCell>
                        <TableCell>
                          <Chip label={ground.surfaceType} size="small" />
                        </TableCell>
                        <TableCell>NPR {ground.pricePerHour}</TableCell>
                        <TableCell>
                          {new Date(ground.createdAt).toLocaleDateString()}
                        </TableCell>
                        <TableCell align="right">
                          <Tooltip title="Delete Ground">
                            <IconButton
                              color="error"
                              onClick={() => openDeleteDialog('ground', ground.id, ground.name)}
                            >
                              <DeleteIcon />
                            </IconButton>
                          </Tooltip>
                        </TableCell>
                      </TableRow>
                    ))
                  )}
                </TableBody>
              </Table>
            </TableContainer>
          </Box>
        </TabPanel>

        {/* Time Slots Tab */}
        <TabPanel value={tabValue} index={3}>
          <Box sx={{ px: 3 }}>
            <Grid container spacing={2} sx={{ mb: 3 }}>
              <Grid item xs={12} sm={4}>
                <Card>
                  <CardContent>
                    <Typography variant="body2" color="text.secondary">
                      Total Time Slots
                    </Typography>
                    <Typography variant="h4" fontWeight="bold" color="primary">
                      {stats?.totalTimeSlots || 0}
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>
              <Grid item xs={12} sm={4}>
                <Card>
                  <CardContent>
                    <Typography variant="body2" color="text.secondary">
                      Available Slots
                    </Typography>
                    <Typography variant="h4" fontWeight="bold" color="success.main">
                      {/* Calculate from actual data if needed */}
                      -
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>
              <Grid item xs={12} sm={4}>
                <Card>
                  <CardContent>
                    <Typography variant="body2" color="text.secondary">
                      Booked Slots
                    </Typography>
                    <Typography variant="h4" fontWeight="bold" color="error.main">
                      {/* Calculate from actual data if needed */}
                      -
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>
            </Grid>
            <Alert severity="info" sx={{ mb: 2 }}>
              Manage all time slots across futsal grounds. Use the dedicated Time Slots page for full CRUD operations.
            </Alert>
            <Box display="flex" justifyContent="center" py={3}>
              <Button
                variant="contained"
                size="large"
                onClick={() => navigate('/admin/timeslots')}
              >
                Go to Time Slots Management
              </Button>
            </Box>
          </Box>
        </TabPanel>

        {/* Bookings Tab */}
        <TabPanel value={tabValue} index={4}>
          <Box sx={{ px: 3 }}>
            <Grid container spacing={2} sx={{ mb: 3 }}>
              <Grid item xs={12} sm={3}>
                <Card>
                  <CardContent>
                    <Typography variant="body2" color="text.secondary">
                      Total Bookings
                    </Typography>
                    <Typography variant="h4" fontWeight="bold">
                      {stats?.totalBookings || 0}
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>
              <Grid item xs={12} sm={3}>
                <Card>
                  <CardContent>
                    <Typography variant="body2" color="text.secondary">
                      Confirmed
                    </Typography>
                    <Typography variant="h4" fontWeight="bold" color="success.main">
                      {stats?.confirmedBookings || 0}
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>
              <Grid item xs={12} sm={3}>
                <Card>
                  <CardContent>
                    <Typography variant="body2" color="text.secondary">
                      Completed
                    </Typography>
                    <Typography variant="h4" fontWeight="bold" color="primary">
                      {stats?.completedBookings || 0}
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>
              <Grid item xs={12} sm={3}>
                <Card>
                  <CardContent>
                    <Typography variant="body2" color="text.secondary">
                      Cancelled
                    </Typography>
                    <Typography variant="h4" fontWeight="bold" color="error.main">
                      {stats?.cancelledBookings || 0}
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>
            </Grid>
            <Alert severity="info" sx={{ mb: 2 }}>
              View and manage all bookings. Use the dedicated Bookings page for detailed management and status updates.
            </Alert>
            <Box display="flex" justifyContent="center" py={3}>
              <Button
                variant="contained"
                size="large"
                onClick={() => navigate('/admin/bookings')}
              >
                Go to Bookings Management
              </Button>
            </Box>
          </Box>
        </TabPanel>

        {/* Payments Tab */}
        <TabPanel value={tabValue} index={5}>
          <Box sx={{ px: 3 }}>
            <Grid container spacing={2} sx={{ mb: 3 }}>
              <Grid item xs={12} sm={4}>
                <Card>
                  <CardContent>
                    <Typography variant="body2" color="text.secondary">
                      Total Revenue
                    </Typography>
                    <Typography variant="h4" fontWeight="bold" color="primary">
                      NPR {stats?.totalRevenue?.toLocaleString() || 0}
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>
              <Grid item xs={12} sm={4}>
                <Card>
                  <CardContent>
                    <Typography variant="body2" color="text.secondary">
                      Success Revenue
                    </Typography>
                    <Typography variant="h4" fontWeight="bold" color="success.main">
                      NPR {stats?.successRevenue?.toLocaleString() || 0}
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>
              <Grid item xs={12} sm={4}>
                <Card>
                  <CardContent>
                    <Typography variant="body2" color="text.secondary">
                      Pending Revenue
                    </Typography>
                    <Typography variant="h4" fontWeight="bold" color="warning.main">
                      NPR {stats?.pendingRevenue?.toLocaleString() || 0}
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>
            </Grid>
            <Alert severity="info" sx={{ mb: 2 }}>
              Monitor all payment transactions and revenue. Use the dedicated Payments page for detailed transaction management.
            </Alert>
            <Box display="flex" justifyContent="center" py={3}>
              <Button
                variant="contained"
                size="large"
                onClick={() => navigate('/admin/payments')}
              >
                Go to Payments Management
              </Button>
            </Box>
          </Box>
        </TabPanel>

        {/* Reviews Tab */}
        <TabPanel value={tabValue} index={6}>
          <Box sx={{ px: 3 }}>
            <Grid container spacing={2} sx={{ mb: 3 }}>
              <Grid item xs={12} sm={6}>
                <Card>
                  <CardContent>
                    <Typography variant="body2" color="text.secondary">
                      Total Reviews
                    </Typography>
                    <Typography variant="h4" fontWeight="bold" color="primary">
                      {stats?.totalReviews || 0}
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>
              <Grid item xs={12} sm={6}>
                <Card>
                  <CardContent>
                    <Typography variant="body2" color="text.secondary">
                      Average Rating
                    </Typography>
                    <Box display="flex" alignItems="center" gap={1}>
                      <Typography variant="h4" fontWeight="bold" color="warning.main">
                        {stats?.averageRating?.toFixed(1) || '0.0'}
                      </Typography>
                      <Rating value={stats?.averageRating || 0} readOnly />
                    </Box>
                  </CardContent>
                </Card>
              </Grid>
            </Grid>
            <Alert severity="info" sx={{ mb: 2 }}>
              Manage customer reviews and ratings. Use the dedicated Reviews page for detailed review management.
            </Alert>
            <Box display="flex" justifyContent="center" py={3}>
              <Button
                variant="contained"
                size="large"
                onClick={() => navigate('/admin/reviews')}
              >
                Go to Reviews Management
              </Button>
            </Box>
          </Box>
        </TabPanel>
      </Paper>

      {/* Delete Confirmation Dialog */}
      <Dialog
        open={deleteDialog.open}
        onClose={() => setDeleteDialog({ ...deleteDialog, open: false })}
      >
        <DialogTitle sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <WarningIcon color="error" />
          Confirm Delete
        </DialogTitle>
        <DialogContent>
          <DialogContentText>
            Are you sure you want to delete <strong>{deleteDialog.name}</strong>?
            This action cannot be undone.
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleteDialog({ ...deleteDialog, open: false })}>
            Cancel
          </Button>
          <Button onClick={handleDelete} color="error" variant="contained">
            Delete
          </Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default AdminDashboard;
