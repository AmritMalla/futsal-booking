import { createTheme, alpha } from '@mui/material';

// VBFutsal-inspired Dark Theme - Premium Futsal Experience
export const colors = {
  // Primary - Dark backgrounds
  primary: {
    main: '#00AEC7',      // Cyan accent
    light: '#33C4D8',
    dark: '#008A9E',
    contrastText: '#FFFFFF',
  },
  // Secondary - Vibrant accent
  secondary: {
    main: '#00AEC7',
    light: '#4DD0E1',
    dark: '#0097A7',
    contrastText: '#FFFFFF',
  },
  // Accent colors
  accent: {
    cyan: '#00AEC7',
    blue: '#2196F3',
    teal: '#009688',
    gold: '#D4AF37',
    yellow: '#FFC107',
    white: '#FFFFFF',
    green: '#4CAF50',
  },
  // Background colors - Dark theme
  background: {
    default: '#0A0A0A',
    paper: '#141414',
    card: '#1A1A1A',
    elevated: '#222222',
    overlay: 'rgba(0, 0, 0, 0.85)',
  },
  // Neutral colors
  neutral: {
    white: '#FFFFFF',
    offWhite: '#F5F5F5',
    lightGray: '#B0B0B0',
    gray: '#6B6B6B',
    darkGray: '#3A3A3A',
    charcoal: '#1A1A1A',
    black: '#0A0A0A',
  },
  // Status colors
  status: {
    success: '#4CAF50',
    warning: '#FFC107',
    error: '#F44336',
    info: '#00AEC7',
  },
  // Text colors
  text: {
    primary: '#FFFFFF',
    secondary: 'rgba(255, 255, 255, 0.7)',
    disabled: 'rgba(255, 255, 255, 0.4)',
    muted: 'rgba(255, 255, 255, 0.5)',
  },
  // Background gradients
  gradients: {
    primary: 'linear-gradient(135deg, #00AEC7 0%, #0097A7 100%)',
    hero: 'linear-gradient(180deg, rgba(10, 10, 10, 0.3) 0%, rgba(10, 10, 10, 0.9) 100%)',
    card: 'linear-gradient(180deg, rgba(26, 26, 26, 0.8) 0%, rgba(20, 20, 20, 1) 100%)',
    overlay: 'linear-gradient(180deg, rgba(0,0,0,0) 0%, rgba(0,0,0,0.9) 100%)',
    dark: 'linear-gradient(180deg, #141414 0%, #0A0A0A 100%)',
    cyanGlow: 'linear-gradient(135deg, rgba(0, 174, 199, 0.2) 0%, rgba(0, 174, 199, 0) 100%)',
  },
};

// Create MUI dark theme
const theme = createTheme({
  palette: {
    mode: 'dark',
    primary: colors.primary,
    secondary: colors.secondary,
    success: {
      main: colors.status.success,
    },
    warning: {
      main: colors.status.warning,
    },
    error: {
      main: colors.status.error,
    },
    info: {
      main: colors.status.info,
    },
    background: {
      default: colors.background.default,
      paper: colors.background.paper,
    },
    text: {
      primary: colors.text.primary,
      secondary: colors.text.secondary,
    },
  },
  typography: {
    fontFamily: '"Lato", "Roboto", "Helvetica", "Arial", sans-serif',
    h1: {
      fontFamily: '"Oswald", "Roboto", "Helvetica", sans-serif',
      fontWeight: 700,
      fontSize: '4rem',
      lineHeight: 1.1,
      letterSpacing: '0.02em',
      textTransform: 'uppercase',
    },
    h2: {
      fontFamily: '"Oswald", "Roboto", "Helvetica", sans-serif',
      fontWeight: 600,
      fontSize: '2.75rem',
      lineHeight: 1.2,
      letterSpacing: '0.02em',
      textTransform: 'uppercase',
    },
    h3: {
      fontFamily: '"Oswald", "Roboto", "Helvetica", sans-serif',
      fontWeight: 600,
      fontSize: '2rem',
      lineHeight: 1.3,
      textTransform: 'uppercase',
    },
    h4: {
      fontFamily: '"Oswald", "Roboto", "Helvetica", sans-serif',
      fontWeight: 500,
      fontSize: '1.5rem',
      lineHeight: 1.4,
      textTransform: 'uppercase',
    },
    h5: {
      fontFamily: '"Lato", "Roboto", "Helvetica", sans-serif',
      fontWeight: 700,
      fontSize: '1.25rem',
      lineHeight: 1.4,
    },
    h6: {
      fontFamily: '"Lato", "Roboto", "Helvetica", sans-serif',
      fontWeight: 700,
      fontSize: '1rem',
      lineHeight: 1.5,
    },
    subtitle1: {
      fontWeight: 400,
      fontSize: '1.125rem',
      lineHeight: 1.6,
      color: colors.text.secondary,
    },
    body1: {
      fontSize: '1rem',
      lineHeight: 1.7,
      fontWeight: 400,
    },
    body2: {
      fontSize: '0.875rem',
      lineHeight: 1.6,
      fontWeight: 400,
    },
    button: {
      fontFamily: '"Lato", "Roboto", "Helvetica", sans-serif',
      fontWeight: 700,
      textTransform: 'uppercase',
      letterSpacing: '0.1em',
      fontSize: '0.875rem',
    },
    overline: {
      fontWeight: 600,
      letterSpacing: '0.15em',
      textTransform: 'uppercase',
      fontSize: '0.75rem',
    },
  },
  shape: {
    borderRadius: 4,
  },
  shadows: [
    'none',
    '0px 2px 4px rgba(0, 0, 0, 0.3)',
    '0px 4px 8px rgba(0, 0, 0, 0.35)',
    '0px 6px 12px rgba(0, 0, 0, 0.4)',
    '0px 8px 16px rgba(0, 0, 0, 0.45)',
    '0px 10px 20px rgba(0, 0, 0, 0.5)',
    '0px 12px 24px rgba(0, 174, 199, 0.15)',
    '0px 14px 28px rgba(0, 174, 199, 0.2)',
    '0px 16px 32px rgba(0, 0, 0, 0.5)',
    '0px 18px 36px rgba(0, 0, 0, 0.52)',
    '0px 20px 40px rgba(0, 0, 0, 0.54)',
    '0px 22px 44px rgba(0, 0, 0, 0.56)',
    '0px 24px 48px rgba(0, 0, 0, 0.58)',
    '0px 26px 52px rgba(0, 0, 0, 0.6)',
    '0px 28px 56px rgba(0, 0, 0, 0.62)',
    '0px 30px 60px rgba(0, 0, 0, 0.64)',
    '0px 32px 64px rgba(0, 0, 0, 0.66)',
    '0px 34px 68px rgba(0, 0, 0, 0.68)',
    '0px 36px 72px rgba(0, 0, 0, 0.7)',
    '0px 38px 76px rgba(0, 0, 0, 0.72)',
    '0px 40px 80px rgba(0, 0, 0, 0.74)',
    '0px 42px 84px rgba(0, 0, 0, 0.76)',
    '0px 44px 88px rgba(0, 0, 0, 0.78)',
    '0px 46px 92px rgba(0, 0, 0, 0.8)',
    '0px 48px 96px rgba(0, 0, 0, 0.82)',
  ],
  components: {
    MuiCssBaseline: {
      styleOverrides: {
        body: {
          backgroundColor: colors.background.default,
          color: colors.text.primary,
        },
      },
    },
    MuiButton: {
      styleOverrides: {
        root: {
          borderRadius: 50,  // Pill shape
          padding: '12px 32px',
          fontSize: '0.875rem',
          fontWeight: 700,
          letterSpacing: '0.1em',
          boxShadow: 'none',
          transition: 'all 0.3s ease',
          '&:hover': {
            boxShadow: '0px 4px 20px rgba(0, 174, 199, 0.4)',
            transform: 'translateY(-2px)',
          },
        },
        contained: {
          '&:hover': {
            transform: 'translateY(-2px)',
          },
        },
        containedPrimary: {
          background: colors.primary.main,
          color: '#FFFFFF',
          '&:hover': {
            background: colors.primary.light,
          },
        },
        containedSecondary: {
          background: 'transparent',
          border: `2px solid ${colors.primary.main}`,
          color: colors.primary.main,
          '&:hover': {
            background: alpha(colors.primary.main, 0.1),
            boxShadow: '0px 4px 20px rgba(0, 174, 199, 0.3)',
          },
        },
        outlined: {
          borderWidth: 2,
          borderColor: colors.primary.main,
          color: colors.primary.main,
          '&:hover': {
            borderWidth: 2,
            background: alpha(colors.primary.main, 0.1),
          },
        },
        outlinedPrimary: {
          borderColor: colors.primary.main,
          '&:hover': {
            borderColor: colors.primary.light,
            background: alpha(colors.primary.main, 0.1),
          },
        },
        text: {
          color: colors.text.primary,
          '&:hover': {
            background: alpha(colors.neutral.white, 0.05),
          },
        },
        sizeLarge: {
          padding: '16px 40px',
          fontSize: '1rem',
        },
        sizeSmall: {
          padding: '8px 20px',
          fontSize: '0.75rem',
        },
      },
    },
    MuiCard: {
      styleOverrides: {
        root: {
          borderRadius: 8,
          backgroundColor: colors.background.card,
          boxShadow: '0px 4px 20px rgba(0, 0, 0, 0.4)',
          border: `1px solid ${alpha(colors.neutral.white, 0.05)}`,
          transition: 'all 0.3s ease',
          '&:hover': {
            transform: 'translateY(-8px)',
            boxShadow: `0px 12px 40px rgba(0, 0, 0, 0.6), 0px 0px 30px ${alpha(colors.primary.main, 0.15)}`,
            borderColor: alpha(colors.primary.main, 0.3),
          },
        },
      },
    },
    MuiPaper: {
      styleOverrides: {
        root: {
          borderRadius: 8,
          backgroundColor: colors.background.paper,
          backgroundImage: 'none',
        },
        elevation1: {
          boxShadow: '0px 2px 8px rgba(0, 0, 0, 0.4)',
        },
        elevation2: {
          boxShadow: '0px 4px 16px rgba(0, 0, 0, 0.45)',
        },
        elevation3: {
          boxShadow: '0px 6px 24px rgba(0, 0, 0, 0.5)',
        },
      },
    },
    MuiAppBar: {
      styleOverrides: {
        root: {
          backgroundColor: alpha(colors.background.default, 0.95),
          backdropFilter: 'blur(10px)',
          boxShadow: 'none',
          borderBottom: `1px solid ${alpha(colors.neutral.white, 0.05)}`,
        },
      },
    },
    MuiChip: {
      styleOverrides: {
        root: {
          borderRadius: 50,
          fontWeight: 600,
          letterSpacing: '0.05em',
        },
        filled: {
          backgroundColor: alpha(colors.primary.main, 0.15),
          color: colors.primary.main,
          '&:hover': {
            backgroundColor: alpha(colors.primary.main, 0.25),
          },
        },
        outlined: {
          borderColor: colors.primary.main,
          color: colors.primary.main,
        },
      },
    },
    MuiTextField: {
      styleOverrides: {
        root: {
          '& .MuiOutlinedInput-root': {
            borderRadius: 8,
            backgroundColor: alpha(colors.neutral.white, 0.03),
            '& fieldset': {
              borderColor: alpha(colors.neutral.white, 0.1),
            },
            '&:hover fieldset': {
              borderColor: alpha(colors.primary.main, 0.5),
            },
            '&.Mui-focused fieldset': {
              borderColor: colors.primary.main,
              borderWidth: 2,
            },
          },
          '& .MuiInputLabel-root': {
            color: colors.text.secondary,
          },
          '& .MuiInputBase-input': {
            color: colors.text.primary,
          },
        },
      },
    },
    MuiSelect: {
      styleOverrides: {
        root: {
          '& .MuiOutlinedInput-notchedOutline': {
            borderColor: alpha(colors.neutral.white, 0.1),
          },
          '&:hover .MuiOutlinedInput-notchedOutline': {
            borderColor: alpha(colors.primary.main, 0.5),
          },
          '&.Mui-focused .MuiOutlinedInput-notchedOutline': {
            borderColor: colors.primary.main,
          },
        },
      },
    },
    MuiAlert: {
      styleOverrides: {
        root: {
          borderRadius: 8,
          border: '1px solid',
        },
        standardSuccess: {
          backgroundColor: alpha(colors.status.success, 0.1),
          borderColor: alpha(colors.status.success, 0.3),
          color: colors.status.success,
        },
        standardError: {
          backgroundColor: alpha(colors.status.error, 0.1),
          borderColor: alpha(colors.status.error, 0.3),
          color: colors.status.error,
        },
        standardWarning: {
          backgroundColor: alpha(colors.status.warning, 0.1),
          borderColor: alpha(colors.status.warning, 0.3),
          color: colors.status.warning,
        },
        standardInfo: {
          backgroundColor: alpha(colors.status.info, 0.1),
          borderColor: alpha(colors.status.info, 0.3),
          color: colors.status.info,
        },
      },
    },
    MuiTableHead: {
      styleOverrides: {
        root: {
          '& .MuiTableCell-head': {
            backgroundColor: colors.background.elevated,
            fontWeight: 700,
            color: colors.text.primary,
            textTransform: 'uppercase',
            letterSpacing: '0.05em',
            fontSize: '0.75rem',
            borderBottom: `2px solid ${colors.primary.main}`,
          },
        },
      },
    },
    MuiTableCell: {
      styleOverrides: {
        root: {
          borderBottom: `1px solid ${alpha(colors.neutral.white, 0.05)}`,
          color: colors.text.primary,
        },
      },
    },
    MuiTableRow: {
      styleOverrides: {
        root: {
          '&:hover': {
            backgroundColor: alpha(colors.primary.main, 0.05),
          },
        },
      },
    },
    MuiTabs: {
      styleOverrides: {
        root: {
          borderBottom: `1px solid ${alpha(colors.neutral.white, 0.1)}`,
        },
        indicator: {
          height: 3,
          borderRadius: 2,
          backgroundColor: colors.primary.main,
        },
      },
    },
    MuiTab: {
      styleOverrides: {
        root: {
          fontWeight: 700,
          textTransform: 'uppercase',
          letterSpacing: '0.1em',
          minWidth: 120,
          color: colors.text.secondary,
          '&.Mui-selected': {
            color: colors.primary.main,
          },
        },
      },
    },
    MuiDialog: {
      styleOverrides: {
        paper: {
          borderRadius: 12,
          backgroundColor: colors.background.card,
          border: `1px solid ${alpha(colors.neutral.white, 0.1)}`,
        },
      },
    },
    MuiDialogTitle: {
      styleOverrides: {
        root: {
          fontWeight: 700,
          fontSize: '1.25rem',
          borderBottom: `1px solid ${alpha(colors.neutral.white, 0.1)}`,
        },
      },
    },
    MuiDivider: {
      styleOverrides: {
        root: {
          borderColor: alpha(colors.neutral.white, 0.1),
        },
      },
    },
    MuiRating: {
      styleOverrides: {
        iconFilled: {
          color: colors.accent.gold,
        },
        iconEmpty: {
          color: alpha(colors.neutral.white, 0.2),
        },
      },
    },
    MuiIconButton: {
      styleOverrides: {
        root: {
          color: colors.text.secondary,
          '&:hover': {
            backgroundColor: alpha(colors.primary.main, 0.1),
            color: colors.primary.main,
          },
        },
      },
    },
    MuiTooltip: {
      styleOverrides: {
        tooltip: {
          backgroundColor: colors.background.elevated,
          color: colors.text.primary,
          border: `1px solid ${alpha(colors.neutral.white, 0.1)}`,
          borderRadius: 4,
          fontSize: '0.75rem',
        },
      },
    },
    MuiMenu: {
      styleOverrides: {
        paper: {
          backgroundColor: colors.background.card,
          border: `1px solid ${alpha(colors.neutral.white, 0.1)}`,
        },
      },
    },
    MuiMenuItem: {
      styleOverrides: {
        root: {
          '&:hover': {
            backgroundColor: alpha(colors.primary.main, 0.1),
          },
          '&.Mui-selected': {
            backgroundColor: alpha(colors.primary.main, 0.15),
            '&:hover': {
              backgroundColor: alpha(colors.primary.main, 0.2),
            },
          },
        },
      },
    },
    MuiSlider: {
      styleOverrides: {
        root: {
          color: colors.primary.main,
        },
        thumb: {
          '&:hover, &.Mui-focusVisible': {
            boxShadow: `0px 0px 0px 8px ${alpha(colors.primary.main, 0.16)}`,
          },
        },
      },
    },
    MuiCircularProgress: {
      styleOverrides: {
        root: {
          color: colors.primary.main,
        },
      },
    },
    MuiLinearProgress: {
      styleOverrides: {
        root: {
          backgroundColor: alpha(colors.primary.main, 0.2),
        },
        bar: {
          backgroundColor: colors.primary.main,
        },
      },
    },
  },
});

export default theme;
