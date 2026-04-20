import React from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  CircularProgress,
  IconButton,
  Box,
} from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';

interface FormDialogProps {
  open: boolean;
  title: string;
  loading?: boolean;
  maxWidth?: 'xs' | 'sm' | 'md' | 'lg' | 'xl';
  children: React.ReactNode;
  onSubmit: () => void;
  onCancel: () => void;
  submitText?: string;
  cancelText?: string;
  disableSubmit?: boolean;
}

const FormDialog: React.FC<FormDialogProps> = ({
  open,
  title,
  loading = false,
  maxWidth = 'sm',
  children,
  onSubmit,
  onCancel,
  submitText = 'Save',
  cancelText = 'Cancel',
  disableSubmit = false,
}) => {
  const handleSubmit = (event: React.FormEvent) => {
    event.preventDefault();
    onSubmit();
  };

  return (
    <Dialog
      open={open}
      onClose={onCancel}
      maxWidth={maxWidth}
      fullWidth
      aria-labelledby="form-dialog-title"
    >
      <form onSubmit={handleSubmit}>
        <DialogTitle id="form-dialog-title">
          <Box display="flex" justifyContent="space-between" alignItems="center">
            {title}
            <IconButton
              aria-label="close"
              onClick={onCancel}
              disabled={loading}
              size="small"
            >
              <CloseIcon />
            </IconButton>
          </Box>
        </DialogTitle>
        <DialogContent dividers>{children}</DialogContent>
        <DialogActions>
          <Button onClick={onCancel} disabled={loading}>
            {cancelText}
          </Button>
          <Button
            type="submit"
            variant="contained"
            color="primary"
            disabled={loading || disableSubmit}
            startIcon={loading ? <CircularProgress size={16} /> : null}
          >
            {submitText}
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  );
};

export default FormDialog;
