import React, { useEffect, useState } from 'react';
import {
  Container,
  Paper,
  Typography,
  Box,
  CircularProgress,
  Alert,
  Button,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  IconButton,
  Tooltip,
  Chip,
  Card,
  CardContent,
  CardMedia,
  Grid,
  alpha,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
} from '@mui/material';
import {
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  Schedule as ScheduleIcon,
  Visibility as ViewIcon,
  Stadium as StadiumIcon,
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import { groundService } from '../../services/groundService';
import { FutsalGround, FutsalGroundRequest } from '../../types';
import { colors } from '../../theme/theme';

const surfaceTypes = ['Turf', 'Grass', 'Indoor', 'Artificial'];

const ManageGrounds: React.FC = () => {
  const [grounds, setGrounds] = useState<FutsalGround[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editingGround, setEditingGround] = useState<FutsalGround | null>(null);
  const [formData, setFormData] = useState<FutsalGroundRequest>({
    companyId: '',
    name: '',
    surfaceType: '',
    pricePerHour: 0,
    imageUrl: '',
  });
  const { user } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (user) {
      fetchGrounds();
    }
  }, [user]);

  const fetchGrounds = async () => {
    if (!user) return;

    try {
      setLoading(true);
      const data = await groundService.getGroundsByCompany(user.id);
      setGrounds(data);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load grounds');
    } finally {
      setLoading(false);
    }
  };

  const handleOpenDialog = (ground?: FutsalGround) => {
    if (ground) {
      setEditingGround(ground);
      setFormData({
        companyId: ground.companyId,
        name: ground.name,
        surfaceType: ground.surfaceType,
        pricePerHour: ground.pricePerHour,
        imageUrl: ground.imageUrl,
      });
    } else {
      setEditingGround(null);
      setFormData({
        companyId: user?.id || '',
        name: '',
        surfaceType: '',
        pricePerHour: 0,
        imageUrl: '',
      });
    }
    setDialogOpen(true);
  };

  const handleCloseDialog = () => {
    setDialogOpen(false);
    setEditingGround(null);
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | { name?: string; value: unknown }>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name as string]: name === 'pricePerHour' ? parseFloat(value as string) || 0 : value,
    }));
  };

  const handleSubmit = async () => {
    try {
      setError('');
      if (editingGround) {
        await groundService.updateGround(editingGround.id, formData);
        setSuccess('Ground updated successfully!');
      } else {
        await groundService.createGround(formData);
        setSuccess('Ground created successfully!');
      }
      handleCloseDialog();
      fetchGrounds();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to save ground');
    }
  };

  const handleDelete = async (id: string, name: string) => {
    if (window.confirm(`Are you sure you want to delete "${name}"? This action cannot be undone.`)) {
      try {
        await groundService.deleteGround(id);
        setSuccess('Ground deleted successfully');
        fetchGrounds();
      } catch (err: any) {
        setError(err.response?.data?.message || 'Failed to delete ground');
      }
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
    <Container maxWidth="lg" sx={{ py: 4 }}>
      {/* Header */}
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={4}>
        <Box>
          <Typography variant="h4" fontWeight={700} gutterBottom>
            My Grounds
          </Typography>
          <Typography variant="body1" color="text.secondary">
            Manage your futsal grounds and time slots
          </Typography>
        </Box>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => handleOpenDialog()}
          sx={{
            bgcolor: colors.secondary.main,
            '&:hover': { bgcolor: colors.secondary.dark },
          }}
        >
          Add New Ground
        </Button>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 3 }} onClose={() => setError('')}>
          {error}
        </Alert>
      )}
      {success && (
        <Alert severity="success" sx={{ mb: 3 }} onClose={() => setSuccess('')}>
          {success}
        </Alert>
      )}

      {grounds.length === 0 ? (
        <Paper sx={{ p: 6, textAlign: 'center' }}>
          <StadiumIcon sx={{ fontSize: 60, color: colors.neutral.gray, mb: 2 }} />
          <Typography variant="h6" color="text.secondary" gutterBottom>
            No grounds added yet
          </Typography>
          <Typography variant="body2" color="text.secondary" mb={3}>
            Start by adding your first futsal ground
          </Typography>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => handleOpenDialog()}
          >
            Add Your First Ground
          </Button>
        </Paper>
      ) : (
        <Grid container spacing={3}>
          {grounds.map((ground, index) => (
            <Grid item xs={12} sm={6} md={4} key={ground.id}>
              <Card sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
                <CardMedia
                  component="img"
                  height="160"
                  image={ground.imageUrl || `https://source.unsplash.com/400x300/?futsal,soccer,field&sig=${index}`}
                  alt={ground.name}
                />
                <CardContent sx={{ flexGrow: 1 }}>
                  <Typography variant="h6" fontWeight={600} gutterBottom>
                    {ground.name}
                  </Typography>
                  <Box display="flex" gap={1} mb={2} flexWrap="wrap">
                    <Chip
                      label={ground.surfaceType}
                      size="small"
                      color="primary"
                    />
                    <Chip
                      label={`NPR ${ground.pricePerHour}/hr`}
                      size="small"
                      sx={{
                        bgcolor: alpha(colors.secondary.main, 0.1),
                        color: colors.secondary.main,
                        fontWeight: 600,
                      }}
                    />
                  </Box>
                </CardContent>
                <Box
                  sx={{
                    p: 2,
                    pt: 0,
                    display: 'flex',
                    gap: 1,
                    flexWrap: 'wrap',
                  }}
                >
                  <Tooltip title="Manage Time Slots">
                    <Button
                      variant="contained"
                      size="small"
                      startIcon={<ScheduleIcon />}
                      onClick={() => navigate(`/owner/grounds/${ground.id}/slots`)}
                      sx={{ flex: 1 }}
                    >
                      Time Slots
                    </Button>
                  </Tooltip>
                  <Tooltip title="View Public Page">
                    <IconButton
                      size="small"
                      onClick={() => navigate(`/grounds/${ground.id}`)}
                      sx={{
                        border: `1px solid ${colors.neutral.gray}`,
                      }}
                    >
                      <ViewIcon fontSize="small" />
                    </IconButton>
                  </Tooltip>
                  <Tooltip title="Edit Ground">
                    <IconButton
                      size="small"
                      onClick={() => handleOpenDialog(ground)}
                      sx={{
                        border: `1px solid ${colors.primary.main}`,
                        color: colors.primary.main,
                      }}
                    >
                      <EditIcon fontSize="small" />
                    </IconButton>
                  </Tooltip>
                  <Tooltip title="Delete Ground">
                    <IconButton
                      size="small"
                      onClick={() => handleDelete(ground.id, ground.name)}
                      sx={{
                        border: `1px solid ${colors.status.error}`,
                        color: colors.status.error,
                      }}
                    >
                      <DeleteIcon fontSize="small" />
                    </IconButton>
                  </Tooltip>
                </Box>
              </Card>
            </Grid>
          ))}
        </Grid>
      )}

      {/* Add/Edit Dialog */}
      <Dialog open={dialogOpen} onClose={handleCloseDialog} maxWidth="sm" fullWidth>
        <DialogTitle>
          {editingGround ? 'Edit Ground' : 'Add New Ground'}
        </DialogTitle>
        <DialogContent>
          <TextField
            fullWidth
            margin="normal"
            label="Ground Name"
            name="name"
            value={formData.name}
            onChange={handleChange}
            required
          />
          <FormControl fullWidth margin="normal">
            <InputLabel>Surface Type</InputLabel>
            <Select
              name="surfaceType"
              value={formData.surfaceType}
              label="Surface Type"
              onChange={handleChange as any}
            >
              {surfaceTypes.map((type) => (
                <MenuItem key={type} value={type}>
                  {type}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
          <TextField
            fullWidth
            margin="normal"
            label="Price Per Hour (NPR)"
            name="pricePerHour"
            type="number"
            value={formData.pricePerHour}
            onChange={handleChange}
            required
          />
          <TextField
            fullWidth
            margin="normal"
            label="Image URL (optional)"
            name="imageUrl"
            value={formData.imageUrl || ''}
            onChange={handleChange}
            placeholder="https://example.com/image.jpg"
          />
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
          <Button onClick={handleCloseDialog}>Cancel</Button>
          <Button
            variant="contained"
            onClick={handleSubmit}
            disabled={!formData.name || !formData.surfaceType || !formData.pricePerHour}
          >
            {editingGround ? 'Update' : 'Create'}
          </Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default ManageGrounds;
