import React, { useState, useEffect } from 'react';
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
  Avatar,
  Divider,
  ListItemIcon,
  alpha,
} from '@mui/material';
import {
  SportsSoccer,
  Menu as MenuIcon,
  Dashboard as DashboardIcon,
  Stadium as StadiumIcon,
  Event as EventIcon,
  Group as GroupIcon,
  Payment as PaymentIcon,
  Logout as LogoutIcon,
  AdminPanelSettings as AdminIcon,
  Close as CloseIcon,
} from '@mui/icons-material';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import { UserRole } from '../../types';
import { colors } from '../../theme/theme';

const Navbar: React.FC = () => {
  const { isAuthenticated, user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const [mobileMenuAnchor, setMobileMenuAnchor] = useState<null | HTMLElement>(null);
  const [scrolled, setScrolled] = useState(false);

  useEffect(() => {
    const handleScroll = () => {
      setScrolled(window.scrollY > 50);
    };
    window.addEventListener('scroll', handleScroll);
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

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

  const isActive = (path: string) => location.pathname === path;

  const navLinkStyle = (path: string) => ({
    color: colors.text.primary,
    fontWeight: 700,
    fontSize: '0.8rem',
    letterSpacing: '0.1em',
    textTransform: 'uppercase' as const,
    position: 'relative' as const,
    px: 2,
    py: 1,
    borderRadius: 0,
    '&::after': {
      content: '""',
      position: 'absolute' as const,
      bottom: 0,
      left: '50%',
      transform: 'translateX(-50%)',
      width: isActive(path) ? '100%' : '0%',
      height: 2,
      bgcolor: colors.primary.main,
      transition: 'width 0.3s ease',
    },
    '&:hover': {
      bgcolor: 'transparent',
      '&::after': {
        width: '100%',
      },
    },
  });

  return (
    <AppBar
      position="fixed"
      sx={{
        bgcolor: scrolled ? alpha(colors.background.default, 0.98) : 'transparent',
        backdropFilter: scrolled ? 'blur(20px)' : 'none',
        boxShadow: scrolled ? '0 2px 20px rgba(0, 0, 0, 0.3)' : 'none',
        borderBottom: scrolled ? `1px solid ${alpha(colors.neutral.white, 0.05)}` : 'none',
        transition: 'all 0.3s ease',
      }}
    >
      <Container maxWidth="xl">
        <Toolbar sx={{ py: 1.5, justifyContent: 'space-between' }}>
          {/* Logo */}
          <Box
            sx={{
              display: 'flex',
              alignItems: 'center',
              cursor: 'pointer',
            }}
            onClick={() => navigate('/')}
          >
            <SportsSoccer
              sx={{
                fontSize: 40,
                mr: 1.5,
                color: colors.primary.main,
                filter: 'drop-shadow(0 0 10px rgba(0, 174, 199, 0.5))',
              }}
            />
            <Box>
              <Typography
                sx={{
                  fontFamily: '"Oswald", sans-serif',
                  fontWeight: 700,
                  fontSize: '1.5rem',
                  letterSpacing: '0.05em',
                  lineHeight: 1,
                  color: colors.text.primary,
                  textTransform: 'uppercase',
                }}
              >
                Futsal<span style={{ color: colors.primary.main }}>Book</span>
              </Typography>
              <Typography
                variant="caption"
                sx={{
                  color: colors.text.muted,
                  fontSize: '0.6rem',
                  letterSpacing: '0.2em',
                  textTransform: 'uppercase',
                }}
              >
                Book Your Game
              </Typography>
            </Box>
          </Box>

          {/* Desktop Menu */}
          <Box sx={{ display: { xs: 'none', md: 'flex' }, alignItems: 'center', gap: 1 }}>
            <Button sx={navLinkStyle('/')} onClick={() => navigate('/')}>
              Home
            </Button>
            <Button sx={navLinkStyle('/grounds')} onClick={() => navigate('/grounds')}>
              Grounds
            </Button>
            {isAuthenticated && (
              <Button sx={navLinkStyle('/matches')} onClick={() => navigate('/matches')}>
                Find Games
              </Button>
            )}

            {isAuthenticated && (
              <Button sx={navLinkStyle('/my-bookings')} onClick={() => navigate('/my-bookings')}>
                My Bookings
              </Button>
            )}

            {user?.role === UserRole.OWNER && (
              <>
                <Button sx={navLinkStyle('/owner/dashboard')} onClick={() => navigate('/owner/dashboard')}>
                  Dashboard
                </Button>
                <Button sx={navLinkStyle('/owner/grounds')} onClick={() => navigate('/owner/grounds')}>
                  My Grounds
                </Button>
              </>
            )}

            {user?.role === UserRole.ADMIN && (
              <Button
                sx={{
                  ...navLinkStyle('/admin/dashboard'),
                  color: colors.primary.main,
                }}
                onClick={() => navigate('/admin/dashboard')}
                startIcon={<AdminIcon />}
              >
                Admin
              </Button>
            )}
          </Box>

          {/* Auth Buttons / User Menu - Desktop */}
          <Box sx={{ display: { xs: 'none', md: 'flex' }, alignItems: 'center', gap: 2 }}>
            {isAuthenticated ? (
              <>
                <Box
                  onClick={handleMenu}
                  sx={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: 1.5,
                    cursor: 'pointer',
                    p: 1,
                    borderRadius: 2,
                    border: `1px solid ${alpha(colors.neutral.white, 0.1)}`,
                    transition: 'all 0.3s ease',
                    '&:hover': {
                      borderColor: colors.primary.main,
                      bgcolor: alpha(colors.primary.main, 0.05),
                    },
                  }}
                >
                  <Avatar
                    sx={{
                      width: 36,
                      height: 36,
                      bgcolor: colors.primary.main,
                      fontSize: '0.9rem',
                      fontWeight: 700,
                    }}
                  >
                    {user?.name.charAt(0).toUpperCase()}
                  </Avatar>
                  <Box sx={{ display: { xs: 'none', lg: 'block' } }}>
                    <Typography
                      variant="body2"
                      sx={{
                        color: colors.text.primary,
                        fontWeight: 700,
                        lineHeight: 1.2,
                        fontSize: '0.85rem',
                      }}
                    >
                      {user?.name}
                    </Typography>
                    <Typography
                      variant="caption"
                      sx={{
                        color: colors.primary.main,
                        textTransform: 'uppercase',
                        fontSize: '0.65rem',
                        letterSpacing: '0.1em',
                      }}
                    >
                      {user?.role}
                    </Typography>
                  </Box>
                </Box>
                <Menu
                  anchorEl={anchorEl}
                  open={Boolean(anchorEl)}
                  onClose={handleClose}
                  PaperProps={{
                    sx: {
                      minWidth: 220,
                      mt: 1,
                      bgcolor: colors.background.card,
                      border: `1px solid ${alpha(colors.neutral.white, 0.1)}`,
                    },
                  }}
                >
                  <Box sx={{ px: 2, py: 1.5 }}>
                    <Typography variant="body2" fontWeight={700} color={colors.text.primary}>
                      {user?.name}
                    </Typography>
                    <Typography variant="caption" sx={{ color: colors.text.secondary }}>
                      {user?.email}
                    </Typography>
                  </Box>
                  <Divider sx={{ borderColor: alpha(colors.neutral.white, 0.1) }} />
                  <MenuItem
                    onClick={() => handleNavigate('/my-bookings')}
                    sx={{ py: 1.5, '&:hover': { bgcolor: alpha(colors.primary.main, 0.1) } }}
                  >
                    <ListItemIcon>
                      <EventIcon fontSize="small" sx={{ color: colors.primary.main }} />
                    </ListItemIcon>
                    <Typography variant="body2">My Bookings</Typography>
                  </MenuItem>
                  <MenuItem
                    onClick={() => handleNavigate('/payment-history')}
                    sx={{ py: 1.5, '&:hover': { bgcolor: alpha(colors.primary.main, 0.1) } }}
                  >
                    <ListItemIcon>
                      <PaymentIcon fontSize="small" sx={{ color: colors.primary.main }} />
                    </ListItemIcon>
                    <Typography variant="body2">Payment History</Typography>
                  </MenuItem>
                  <Divider sx={{ borderColor: alpha(colors.neutral.white, 0.1) }} />
                  <MenuItem
                    onClick={handleLogout}
                    sx={{
                      py: 1.5,
                      color: colors.status.error,
                      '&:hover': { bgcolor: alpha(colors.status.error, 0.1) }
                    }}
                  >
                    <ListItemIcon>
                      <LogoutIcon fontSize="small" sx={{ color: colors.status.error }} />
                    </ListItemIcon>
                    <Typography variant="body2">Logout</Typography>
                  </MenuItem>
                </Menu>
              </>
            ) : (
              <>
                <Button
                  sx={{
                    color: colors.text.primary,
                    fontWeight: 700,
                    fontSize: '0.8rem',
                    letterSpacing: '0.1em',
                    '&:hover': {
                      bgcolor: alpha(colors.neutral.white, 0.05),
                    },
                  }}
                  onClick={() => navigate('/login')}
                >
                  Login
                </Button>
                <Button
                  variant="contained"
                  sx={{
                    bgcolor: colors.primary.main,
                    color: '#FFFFFF',
                    fontWeight: 700,
                    fontSize: '0.8rem',
                    letterSpacing: '0.1em',
                    px: 3,
                    '&:hover': {
                      bgcolor: colors.primary.light,
                      boxShadow: '0 0 20px rgba(0, 174, 199, 0.5)',
                    },
                  }}
                  onClick={() => navigate('/register')}
                >
                  Sign Up
                </Button>
              </>
            )}
          </Box>

          {/* Mobile Menu Button */}
          <Box sx={{ display: { xs: 'flex', md: 'none' } }}>
            <IconButton
              onClick={handleMobileMenu}
              sx={{
                color: colors.text.primary,
                border: `1px solid ${alpha(colors.neutral.white, 0.1)}`,
                borderRadius: 1,
              }}
            >
              {mobileMenuAnchor ? <CloseIcon /> : <MenuIcon />}
            </IconButton>
            <Menu
              anchorEl={mobileMenuAnchor}
              open={Boolean(mobileMenuAnchor)}
              onClose={handleClose}
              PaperProps={{
                sx: {
                  minWidth: 280,
                  bgcolor: colors.background.card,
                  border: `1px solid ${alpha(colors.neutral.white, 0.1)}`,
                },
              }}
            >
              {isAuthenticated && (
                <Box sx={{
                  px: 2,
                  py: 2,
                  bgcolor: alpha(colors.primary.main, 0.05),
                  borderBottom: `1px solid ${alpha(colors.neutral.white, 0.1)}`,
                }}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
                    <Avatar
                      sx={{
                        width: 40,
                        height: 40,
                        bgcolor: colors.primary.main,
                        fontWeight: 700,
                      }}
                    >
                      {user?.name.charAt(0).toUpperCase()}
                    </Avatar>
                    <Box>
                      <Typography variant="body2" fontWeight={700} color={colors.text.primary}>
                        {user?.name}
                      </Typography>
                      <Typography variant="caption" sx={{ color: colors.text.secondary }}>
                        {user?.email}
                      </Typography>
                    </Box>
                  </Box>
                </Box>
              )}

              <MenuItem
                onClick={() => handleNavigate('/')}
                sx={{ py: 1.5, '&:hover': { bgcolor: alpha(colors.primary.main, 0.1) } }}
              >
                <Typography
                  variant="body2"
                  sx={{
                    fontWeight: 600,
                    letterSpacing: '0.05em',
                    textTransform: 'uppercase',
                  }}
                >
                  Home
                </Typography>
              </MenuItem>

              <MenuItem
                onClick={() => handleNavigate('/grounds')}
                sx={{ py: 1.5, '&:hover': { bgcolor: alpha(colors.primary.main, 0.1) } }}
              >
                <ListItemIcon>
                  <StadiumIcon fontSize="small" sx={{ color: colors.primary.main }} />
                </ListItemIcon>
                <Typography variant="body2" fontWeight={600}>Browse Grounds</Typography>
              </MenuItem>

              {isAuthenticated && (
                <MenuItem
                  onClick={() => handleNavigate('/matches')}
                  sx={{ py: 1.5, '&:hover': { bgcolor: alpha(colors.primary.main, 0.1) } }}
                >
                  <ListItemIcon>
                    <GroupIcon fontSize="small" sx={{ color: colors.primary.main }} />
                  </ListItemIcon>
                  <Typography variant="body2" fontWeight={600}>Find Games</Typography>
                </MenuItem>
              )}

              {isAuthenticated ? (
                <>
                  <MenuItem
                    onClick={() => handleNavigate('/my-bookings')}
                    sx={{ py: 1.5, '&:hover': { bgcolor: alpha(colors.primary.main, 0.1) } }}
                  >
                    <ListItemIcon>
                      <EventIcon fontSize="small" sx={{ color: colors.primary.main }} />
                    </ListItemIcon>
                    <Typography variant="body2" fontWeight={600}>My Bookings</Typography>
                  </MenuItem>

                  {user?.role === UserRole.OWNER && (
                    <>
                      <MenuItem
                        onClick={() => handleNavigate('/owner/dashboard')}
                        sx={{ py: 1.5, '&:hover': { bgcolor: alpha(colors.primary.main, 0.1) } }}
                      >
                        <ListItemIcon>
                          <DashboardIcon fontSize="small" sx={{ color: colors.primary.main }} />
                        </ListItemIcon>
                        <Typography variant="body2" fontWeight={600}>Dashboard</Typography>
                      </MenuItem>
                      <MenuItem
                        onClick={() => handleNavigate('/owner/grounds')}
                        sx={{ py: 1.5, '&:hover': { bgcolor: alpha(colors.primary.main, 0.1) } }}
                      >
                        <ListItemIcon>
                          <StadiumIcon fontSize="small" sx={{ color: colors.primary.main }} />
                        </ListItemIcon>
                        <Typography variant="body2" fontWeight={600}>Manage Grounds</Typography>
                      </MenuItem>
                    </>
                  )}

                  {user?.role === UserRole.ADMIN && (
                    <MenuItem
                      onClick={() => handleNavigate('/admin/dashboard')}
                      sx={{ py: 1.5, '&:hover': { bgcolor: alpha(colors.primary.main, 0.1) } }}
                    >
                      <ListItemIcon>
                        <AdminIcon fontSize="small" sx={{ color: colors.primary.main }} />
                      </ListItemIcon>
                      <Typography variant="body2" fontWeight={600}>Admin Panel</Typography>
                    </MenuItem>
                  )}

                  <MenuItem
                    onClick={() => handleNavigate('/payment-history')}
                    sx={{ py: 1.5, '&:hover': { bgcolor: alpha(colors.primary.main, 0.1) } }}
                  >
                    <ListItemIcon>
                      <PaymentIcon fontSize="small" sx={{ color: colors.primary.main }} />
                    </ListItemIcon>
                    <Typography variant="body2" fontWeight={600}>Payment History</Typography>
                  </MenuItem>

                  <Divider sx={{ borderColor: alpha(colors.neutral.white, 0.1), my: 1 }} />
                  <MenuItem
                    onClick={handleLogout}
                    sx={{
                      py: 1.5,
                      color: colors.status.error,
                      '&:hover': { bgcolor: alpha(colors.status.error, 0.1) }
                    }}
                  >
                    <ListItemIcon>
                      <LogoutIcon fontSize="small" sx={{ color: colors.status.error }} />
                    </ListItemIcon>
                    <Typography variant="body2" fontWeight={600}>Logout</Typography>
                  </MenuItem>
                </>
              ) : (
                <>
                  <Divider sx={{ borderColor: alpha(colors.neutral.white, 0.1), my: 1 }} />
                  <Box sx={{ p: 2, display: 'flex', flexDirection: 'column', gap: 1 }}>
                    <Button
                      fullWidth
                      variant="outlined"
                      onClick={() => handleNavigate('/login')}
                      sx={{
                        borderColor: colors.primary.main,
                        color: colors.primary.main,
                        fontWeight: 700,
                        '&:hover': {
                          bgcolor: alpha(colors.primary.main, 0.1),
                        },
                      }}
                    >
                      Login
                    </Button>
                    <Button
                      fullWidth
                      variant="contained"
                      onClick={() => handleNavigate('/register')}
                      sx={{
                        bgcolor: colors.primary.main,
                        fontWeight: 700,
                        '&:hover': {
                          bgcolor: colors.primary.light,
                        },
                      }}
                    >
                      Sign Up
                    </Button>
                  </Box>
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
