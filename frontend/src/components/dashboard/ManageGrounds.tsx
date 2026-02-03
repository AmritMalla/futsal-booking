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
} from '@mui/material';
import { useAuth } from '../../contexts/AuthContext';
import { groundService } from '../../services/groundService';
import { FutsalGround, FutsalGroundRequest } from '../../types';

const ManageGrounds: React.FC = () => {
  const [grounds, setGrounds] = useState<FutsalGround[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
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

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: name === 'pricePerHour' ? parseFloat(value) || 0 : value,
    }));
  };

  const handleSubmit = async () => {
    try {
      if (editingGround) {
        await groundService.updateGround(editingGround.id, formData);
      } else {
        await groundService.createGround(formData);
      }
      handleCloseDialog();
      fetchGrounds();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to save ground');
    }
  };

  const handleDelete = async (id: string) => {
    if (window.confirm('Are you sure you want to delete this ground?')) {
      try {
        await groundService.deleteGround(id);
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
    <Container sx={{ mt: 4, mb: 4 }}>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h4">Manage Grounds</Typography>
        <Button variant="contained" onClick={() => handleOpenDialog()}>
          Add New Ground
        </Button>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      {grounds.length === 0 ? (
        <Paper sx={{ p: 3 }}>
          <Alert severity="info">You haven't added any grounds yet.</Alert>
        </Paper>
      ) : (
        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Name</TableCell>
                <TableCell>Surface Type</TableCell>
                <TableCell>Price/Hour</TableCell>
                <TableCell>Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {grounds.map((ground) => (
                <TableRow key={ground.id}>
                  <TableCell>{ground.name}</TableCell>
                  <TableCell>{ground.surfaceType}</TableCell>
                  <TableCell>NPR {ground.pricePerHour}</TableCell>
                  <TableCell>
                    <Button
                      size="small"
                      onClick={() => handleOpenDialog(ground)}
                      sx={{ mr: 1 }}
                    >
                      Edit
                    </Button>
                    <Button
                      size="small"
                      color="error"
                      onClick={() => handleDelete(ground.id)}
                    >
                      Delete
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      <Dialog open={dialogOpen} onClose={handleCloseDialog} maxWidth="sm" fullWidth>
        <DialogTitle>{editingGround ? 'Edit Ground' : 'Add New Ground'}</DialogTitle>
        <DialogContent>
          <TextField
            fullWidth
            margin="normal"
            label="Ground Name"
            name="name"
            value={formData.name}
            onChange={handleChange}
          />
          <TextField
            fullWidth
            margin="normal"
            label="Surface Type"
            name="surfaceType"
            value={formData.surfaceType}
            onChange={handleChange}
          />
          <TextField
            fullWidth
            margin="normal"
            label="Price Per Hour"
            name="pricePerHour"
            type="number"
            value={formData.pricePerHour}
            onChange={handleChange}
          />
          <TextField
            fullWidth
            margin="normal"
            label="Image URL"
            name="imageUrl"
            value={formData.imageUrl}
            onChange={handleChange}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDialog}>Cancel</Button>
          <Button variant="contained" onClick={handleSubmit}>
            {editingGround ? 'Update' : 'Create'}
          </Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default ManageGrounds;
