import React, { useState, useEffect } from 'react';
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
} from '@mui/icons-material';
import { User, FutsalGround, UserRole } from '../../types';
import { adminService, FutsalCompany, AdminStats } from '../../services/adminService';
import { colors } from '../../theme/theme';

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
  const [tabValue, setTabValue] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // Data states
  const [stats, setStats] = useState<AdminStats | null>(null);
  const [users, setUsers] = useState<User[]>([]);
  const [companies, setCompanies] = useState<FutsalCompany[]>([]);
  const [grounds, setGrounds] = useState<FutsalGround[]>([]);

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

      const [usersData, companiesData, groundsData] = await Promise.all([
        adminService.getAllUsers(),
        adminService.getAllCompanies(),
        adminService.getAllGrounds(),
      ]);

      setUsers(usersData);
      setCompanies(companiesData);
      setGrounds(groundsData);

      // Calculate stats
      setStats({
        totalUsers: usersData.length,
        totalOwners: usersData.filter(u => u.role === UserRole.OWNER).length,
        totalCustomers: usersData.filter(u => u.role === UserRole.USER).length,
        totalCompanies: companiesData.length,
        totalGrounds: groundsData.length,
        totalBookings: 0,
      });
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

      {/* Stats Cards */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid item xs={12} sm={6} md={3}>
          <Card sx={{ bgcolor: alpha(colors.primary.main, 0.1) }}>
            <CardContent>
              <Box display="flex" alignItems="center" justifyContent="space-between">
                <Box>
                  <Typography color="text.secondary" variant="body2" gutterBottom>
                    Total Users
                  </Typography>
                  <Typography variant="h4" fontWeight={700} color="primary">
                    {stats?.totalUsers || 0}
                  </Typography>
                </Box>
                <Avatar sx={{ bgcolor: colors.primary.main, width: 56, height: 56 }}>
                  <PeopleIcon />
                </Avatar>
              </Box>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card sx={{ bgcolor: alpha(colors.secondary.main, 0.1) }}>
            <CardContent>
              <Box display="flex" alignItems="center" justifyContent="space-between">
                <Box>
                  <Typography color="text.secondary" variant="body2" gutterBottom>
                    Ground Owners
                  </Typography>
                  <Typography variant="h4" fontWeight={700} sx={{ color: colors.secondary.main }}>
                    {stats?.totalOwners || 0}
                  </Typography>
                </Box>
                <Avatar sx={{ bgcolor: colors.secondary.main, width: 56, height: 56 }}>
                  <StoreIcon />
                </Avatar>
              </Box>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card sx={{ bgcolor: alpha(colors.accent.blue, 0.1) }}>
            <CardContent>
              <Box display="flex" alignItems="center" justifyContent="space-between">
                <Box>
                  <Typography color="text.secondary" variant="body2" gutterBottom>
                    Companies
                  </Typography>
                  <Typography variant="h4" fontWeight={700} sx={{ color: colors.accent.blue }}>
                    {stats?.totalCompanies || 0}
                  </Typography>
                </Box>
                <Avatar sx={{ bgcolor: colors.accent.blue, width: 56, height: 56 }}>
                  <BusinessIcon />
                </Avatar>
              </Box>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card sx={{ bgcolor: alpha(colors.accent.teal, 0.1) }}>
            <CardContent>
              <Box display="flex" alignItems="center" justifyContent="space-between">
                <Box>
                  <Typography color="text.secondary" variant="body2" gutterBottom>
                    Grounds
                  </Typography>
                  <Typography variant="h4" fontWeight={700} sx={{ color: colors.accent.teal }}>
                    {stats?.totalGrounds || 0}
                  </Typography>
                </Box>
                <Avatar sx={{ bgcolor: colors.accent.teal, width: 56, height: 56 }}>
                  <StadiumIcon />
                </Avatar>
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Tabs */}
      <Paper sx={{ mb: 3 }}>
        <Tabs
          value={tabValue}
          onChange={(_, newValue) => setTabValue(newValue)}
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
