import apiClient from './api';
import { FileUploadResponse } from '../types';

export const fileService = {
  async uploadFile(file: File): Promise<FileUploadResponse> {
    const formData = new FormData();
    formData.append('file', file);
    const response = await apiClient.post<FileUploadResponse>('/files/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  },

  getFileUrl(fileName: string): string {
    const base = process.env.REACT_APP_API_BASE_URL || '';
    const absoluteBase = base.startsWith('http') ? base : `${window.location.origin}${base}`;
    return `${absoluteBase}/files/${fileName}`;
  },

  async deleteFile(fileName: string): Promise<void> {
    await apiClient.delete(`/files/${fileName}`);
  },
};
