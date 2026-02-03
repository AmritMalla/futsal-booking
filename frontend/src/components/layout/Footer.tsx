import React from 'react';
import { Box, Container, Typography, Link, Grid, IconButton, alpha, Button } from '@mui/material';
import {
  SportsSoccer,
  Facebook,
  Instagram,
  Twitter,
  YouTube,
  Email,
  Phone,
  LocationOn,
  ArrowForward,
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { colors } from '../../theme/theme';

const Footer: React.FC = () => {
  const navigate = useNavigate();

  return (
    <Box
      component="footer"
      sx={{
        bgcolor: colors.background.default,
        color: colors.text.primary,
        pt: 8,
        pb: 4,
        borderTop: `1px solid ${alpha(colors.neutral.white, 0.05)}`,
      }}
    >
      <Container maxWidth="lg">
        <Grid container spacing={6}>
          {/* Brand Section */}
          <Grid item xs={12} md={4}>
            <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
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
            <Typography
              variant="body2"
              sx={{
                color: colors.text.secondary,
                mb: 3,
                lineHeight: 1.8,
                maxWidth: 300,
              }}
            >
              Nepal's premier futsal ground booking platform. Find, book, and play at the best
              futsal venues near you with instant confirmation.
            </Typography>
            <Box sx={{ display: 'flex', gap: 1.5 }}>
              {[
                { icon: <Facebook fontSize="small" />, label: 'Facebook' },
                { icon: <Instagram fontSize="small" />, label: 'Instagram' },
                { icon: <Twitter fontSize="small" />, label: 'Twitter' },
                { icon: <YouTube fontSize="small" />, label: 'YouTube' },
              ].map((social, index) => (
                <IconButton
                  key={index}
                  size="small"
                  aria-label={social.label}
                  sx={{
                    color: colors.text.secondary,
                    border: `1px solid ${alpha(colors.neutral.white, 0.1)}`,
                    borderRadius: 1,
                    transition: 'all 0.3s ease',
                    '&:hover': {
                      bgcolor: colors.primary.main,
                      borderColor: colors.primary.main,
                      color: '#FFFFFF',
                      transform: 'translateY(-3px)',
                    },
                  }}
                >
                  {social.icon}
                </IconButton>
              ))}
            </Box>
          </Grid>

          {/* Quick Links */}
          <Grid item xs={6} sm={4} md={2}>
            <Typography
              variant="overline"
              sx={{
                color: colors.primary.main,
                fontWeight: 700,
                letterSpacing: '0.15em',
                mb: 3,
                display: 'block',
              }}
            >
              Quick Links
            </Typography>
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
              {[
                { label: 'Home', path: '/' },
                { label: 'Browse Grounds', path: '/grounds' },
                { label: 'Register', path: '/register' },
                { label: 'Login', path: '/login' },
              ].map((link, index) => (
                <Link
                  key={index}
                  onClick={() => navigate(link.path)}
                  sx={{
                    color: colors.text.secondary,
                    cursor: 'pointer',
                    textDecoration: 'none',
                    fontSize: '0.9rem',
                    transition: 'all 0.2s ease',
                    '&:hover': {
                      color: colors.primary.main,
                      paddingLeft: 1,
                    },
                  }}
                >
                  {link.label}
                </Link>
              ))}
            </Box>
          </Grid>

          {/* For Owners */}
          <Grid item xs={6} sm={4} md={2}>
            <Typography
              variant="overline"
              sx={{
                color: colors.primary.main,
                fontWeight: 700,
                letterSpacing: '0.15em',
                mb: 3,
                display: 'block',
              }}
            >
              For Owners
            </Typography>
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
              {[
                { label: 'List Your Ground', path: '/register' },
                { label: 'Owner Dashboard', path: '/owner/dashboard' },
                { label: 'Manage Bookings', path: '/owner/dashboard' },
                { label: 'Support', path: '/' },
              ].map((link, index) => (
                <Link
                  key={index}
                  onClick={() => navigate(link.path)}
                  sx={{
                    color: colors.text.secondary,
                    cursor: 'pointer',
                    textDecoration: 'none',
                    fontSize: '0.9rem',
                    transition: 'all 0.2s ease',
                    '&:hover': {
                      color: colors.primary.main,
                      paddingLeft: 1,
                    },
                  }}
                >
                  {link.label}
                </Link>
              ))}
            </Box>
          </Grid>

          {/* Contact */}
          <Grid item xs={12} sm={4} md={4}>
            <Typography
              variant="overline"
              sx={{
                color: colors.primary.main,
                fontWeight: 700,
                letterSpacing: '0.15em',
                mb: 3,
                display: 'block',
              }}
            >
              Get In Touch
            </Typography>
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2.5 }}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
                <Box
                  sx={{
                    width: 36,
                    height: 36,
                    borderRadius: 1,
                    bgcolor: alpha(colors.primary.main, 0.1),
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                  }}
                >
                  <LocationOn sx={{ fontSize: 18, color: colors.primary.main }} />
                </Box>
                <Typography variant="body2" sx={{ color: colors.text.secondary }}>
                  Kathmandu, Nepal
                </Typography>
              </Box>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
                <Box
                  sx={{
                    width: 36,
                    height: 36,
                    borderRadius: 1,
                    bgcolor: alpha(colors.primary.main, 0.1),
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                  }}
                >
                  <Email sx={{ fontSize: 18, color: colors.primary.main }} />
                </Box>
                <Typography variant="body2" sx={{ color: colors.text.secondary }}>
                  support@futsalbook.com
                </Typography>
              </Box>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
                <Box
                  sx={{
                    width: 36,
                    height: 36,
                    borderRadius: 1,
                    bgcolor: alpha(colors.primary.main, 0.1),
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                  }}
                >
                  <Phone sx={{ fontSize: 18, color: colors.primary.main }} />
                </Box>
                <Typography variant="body2" sx={{ color: colors.text.secondary }}>
                  +977-1-1234567
                </Typography>
              </Box>
            </Box>

            {/* Newsletter */}
            <Box sx={{ mt: 4 }}>
              <Typography
                variant="body2"
                sx={{
                  color: colors.text.secondary,
                  mb: 2,
                }}
              >
                Subscribe for updates and offers
              </Typography>
              <Button
                variant="outlined"
                endIcon={<ArrowForward />}
                sx={{
                  borderColor: colors.primary.main,
                  color: colors.primary.main,
                  fontWeight: 700,
                  letterSpacing: '0.05em',
                  '&:hover': {
                    bgcolor: alpha(colors.primary.main, 0.1),
                  },
                }}
              >
                Subscribe
              </Button>
            </Box>
          </Grid>
        </Grid>

        {/* Bottom Section */}
        <Box
          sx={{
            mt: 6,
            pt: 4,
            borderTop: `1px solid ${alpha(colors.neutral.white, 0.05)}`,
            display: 'flex',
            flexDirection: { xs: 'column', md: 'row' },
            justifyContent: 'space-between',
            alignItems: 'center',
            gap: 2,
          }}
        >
          <Typography variant="body2" sx={{ color: colors.text.muted }}>
            © {new Date().getFullYear()} FutsalBook. All rights reserved.
          </Typography>
          <Box sx={{ display: 'flex', gap: 3 }}>
            {['Privacy Policy', 'Terms of Service', 'Cookie Policy'].map((item, index) => (
              <Link
                key={index}
                sx={{
                  color: colors.text.muted,
                  cursor: 'pointer',
                  textDecoration: 'none',
                  fontSize: '0.85rem',
                  transition: 'color 0.2s ease',
                  '&:hover': { color: colors.primary.main },
                }}
              >
                {item}
              </Link>
            ))}
          </Box>
        </Box>
      </Container>
    </Box>
  );
};

export default Footer;
