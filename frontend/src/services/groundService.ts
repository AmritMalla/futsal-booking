import apiClient from './api';
import { FutsalGround, FutsalGroundRequest, GroundSearchParams } from '../types';

export const groundService = {
  async getAllGrounds(): Promise<FutsalGround[]> {
    const response = await apiClient.get<FutsalGround[]>('/grounds');
    return response.data;
  },

  async getGroundById(id: string): Promise<FutsalGround> {
    const response = await apiClient.get<FutsalGround>(`/grounds/${id}`);
    return response.data;
  },

  async getGroundByName(name: string): Promise<FutsalGround> {
    const response = await apiClient.get<FutsalGround>(`/grounds/name/${name}`);
    return response.data;
  },

  async getGroundsByCompany(companyId: string): Promise<FutsalGround[]> {
    const response = await apiClient.get<FutsalGround[]>(`/grounds/company/${companyId}`);
    return response.data;
  },

  async getGroundsBySurfaceType(surfaceType: string): Promise<FutsalGround[]> {
    const response = await apiClient.get<FutsalGround[]>(`/grounds/surface/${surfaceType}`);
    return response.data;
  },

  async searchGrounds(params: GroundSearchParams): Promise<FutsalGround[]> {
    const response = await apiClient.get<FutsalGround[]>('/grounds/search', { params });
    return response.data;
  },

  async createGround(data: FutsalGroundRequest): Promise<FutsalGround> {
    const response = await apiClient.post<FutsalGround>('/grounds', data);
    return response.data;
  },

  async updateGround(id: string, data: FutsalGroundRequest): Promise<FutsalGround> {
    const response = await apiClient.put<FutsalGround>(`/grounds/${id}`, data);
    return response.data;
  },

  async uploadGroundImage(id: string, file: File): Promise<FutsalGround> {
    const formData = new FormData();
    formData.append('file', file);
    const response = await apiClient.post<FutsalGround>(`/grounds/${id}/image`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  },

  async deleteGround(id: string): Promise<void> {
    await apiClient.delete(`/grounds/${id}`);
  },
};
