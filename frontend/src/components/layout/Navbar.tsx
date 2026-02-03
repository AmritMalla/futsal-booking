import React, { useState } from 'react';
import {
  AppBar,
  Toolbar,
  Typography,
  Button,
  Box,
  IconButton,
  Menu,
  MenuItem,
  Container,
} from '@mui/material';
import {
  AccountCircle,
  SportsFootball,
  Menu as MenuIcon,
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import { UserRole } from '../../types';

const Navbar: React.FC = () => {
  const { isAuthenticated, user, logout } = useAuth();
  const navigate = useNavigate();
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const [mobileMenuAnchor, setMobileMenuAnchor] = useState<null | HTMLElement>(null);

  const handleMenu = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleMobileMenu = (event: React.MouseEvent<HTMLElement>) => {
    setMobileMenuAnchor(event.currentTarget);
  };

  const handleClose = () => {
    setAnchorEl(null);
    setMobileMenuAnchor(null);
  };

  const handleLogout = () => {
    logout();
    handleClose();
    navigate('/login');
  };

  const handleNavigate = (path: string) => {
    navigate(path);
    handleClose();
  };

  return (
    <AppBar position="sticky">
      <Container maxWidth="xl">
        <Toolbar>
          <SportsFootball sx={{ mr: 2 }} />
          <Typography
            variant="h6"
            component="div"
            sx={{ flexGrow: 1, cursor: 'pointer' }}
            onClick={() => navigate('/')}
          >
            Futsal Booking
          </Typography>

          {/* Desktop Menu */}
          <Box sx={{ display: { xs: 'none', md: 'flex' }, alignItems: 'center', gap: 2 }}>
            <Button color="inherit" onClick={() => navigate('/grounds')}>
              Browse Grounds
            </Button>

            {isAuthenticated ? (
              <>
                <Button color="inherit" onClick={() => navigate('/my-bookings')}>
                  My Bookings
                </Button>

                {user?.role === UserRole.OWNER && (
                  <>
                    <Button color="inherit" onClick={() => navigate('/owner/dashboard')}>
                      Dashboard
                    </Button>
                    <Button color="inherit" onClick={() => navigate('/owner/grounds')}>
                      Manage Grounds
                    </Button>
                  </>
                )}

                {user?.role === UserRole.ADMIN && (
                  <Button color="inherit" onClick={() => navigate('/admin/dashboard')}>
                    Admin Panel
                  </Button>
                )}

                <IconButton color="inherit" onClick={handleMenu}>
                  <AccountCircle />
                </IconButton>
                <Menu
                  anchorEl={anchorEl}
                  open={Boolean(anchorEl)}
                  onClose={handleClose}
                >
                  <MenuItem disabled>
                    <Typography variant="body2">{user?.email}</Typography>
                  </MenuItem>
                  <MenuItem onClick={() => handleNavigate('/payment-history')}>
                    Payment History
                  </MenuItem>
                  <MenuItem onClick={handleLogout}>Logout</MenuItem>
                </Menu>
              </>
            ) : (
              <>
                <Button color="inherit" onClick={() => navigate('/login')}>
                  Login
                </Button>
                <Button variant="outlined" color="inherit" onClick={() => navigate('/register')}>
                  Sign Up
                </Button>
              </>
            )}
          </Box>

          {/* Mobile Menu */}
          <Box sx={{ display: { xs: 'flex', md: 'none' } }}>
            <IconButton color="inherit" onClick={handleMobileMenu}>
              <MenuIcon />
            </IconButton>
            <Menu
              anchorEl={mobileMenuAnchor}
              open={Boolean(mobileMenuAnchor)}
              onClose={handleClose}
            >
              <MenuItem onClick={() => handleNavigate('/grounds')}>Browse Grounds</MenuItem>
              {isAuthenticated ? (
                <>
                  <MenuItem onClick={() => handleNavigate('/my-bookings')}>My Bookings</MenuItem>
                  {user?.role === UserRole.OWNER && (
                    <>
                      <MenuItem onClick={() => handleNavigate('/owner/dashboard')}>
                        Dashboard
                      </MenuItem>
                      <MenuItem onClick={() => handleNavigate('/owner/grounds')}>
                        Manage Grounds
                      </MenuItem>
                    </>
                  )}
                  <MenuItem onClick={() => handleNavigate('/payment-history')}>
                    Payment History
                  </MenuItem>
                  <MenuItem onClick={handleLogout}>Logout</MenuItem>
                </>
              ) : (
                <>
                  <MenuItem onClick={() => handleNavigate('/login')}>Login</MenuItem>
                  <MenuItem onClick={() => handleNavigate('/register')}>Sign Up</MenuItem>
                </>
              )}
            </Menu>
          </Box>
        </Toolbar>
      </Container>
    </AppBar>
  );
};

export default Navbar;
