import React, { createContext, ReactNode, useContext, useMemo, useState } from 'react';
import { Alert, AlertColor, Snackbar } from '@mui/material';

interface ToastContextType {
  showToast: (message: string, severity?: AlertColor) => void;
}

interface ToastState {
  open: boolean;
  message: string;
  severity: AlertColor;
}

const ToastContext = createContext<ToastContextType | undefined>(undefined);

export const useToast = () => {
  const context = useContext(ToastContext);
  if (!context) {
    throw new Error('useToast must be used within a ToastProvider');
  }
  return context;
};

interface ToastProviderProps {
  children: ReactNode;
}

export const ToastProvider: React.FC<ToastProviderProps> = ({ children }) => {
  const [toast, setToast] = useState<ToastState>({
    open: false,
    message: '',
    severity: 'success',
  });

  const value = useMemo(
    () => ({
      showToast: (message: string, severity: AlertColor = 'success') => {
        setToast({
          open: true,
          message,
          severity,
        });
      },
    }),
    []
  );

  return (
    <ToastContext.Provider value={value}>
      {children}
      <Snackbar
        open={toast.open}
        autoHideDuration={3500}
        onClose={() => setToast((current) => ({ ...current, open: false }))}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      >
        <Alert
          onClose={() => setToast((current) => ({ ...current, open: false }))}
          severity={toast.severity}
          variant="filled"
          sx={{ width: '100%' }}
        >
          {toast.message}
        </Alert>
      </Snackbar>
    </ToastContext.Provider>
  );
};
