import apiClient from './api';
import { OpenMatch, OpenMatchRequest, UpdateOpenMatchRequest } from '../types';

export const openMatchService = {
  async getOpenMatches(): Promise<OpenMatch[]> {
    const response = await apiClient.get<OpenMatch[]>('/open-matches');
    return response.data;
  },

  async getOpenMatchesByGround(groundId: string): Promise<OpenMatch[]> {
    const response = await apiClient.get<OpenMatch[]>(`/open-matches/ground/${groundId}`);
    return response.data;
  },

  async getMyOpenMatches(): Promise<OpenMatch[]> {
    const response = await apiClient.get<OpenMatch[]>('/open-matches/me');
    return response.data;
  },

  async createOpenMatch(data: OpenMatchRequest): Promise<OpenMatch> {
    const response = await apiClient.post<OpenMatch>('/open-matches', data);
    return response.data;
  },

  async updateOpenMatch(id: string, data: UpdateOpenMatchRequest): Promise<OpenMatch> {
    const response = await apiClient.put<OpenMatch>(`/open-matches/${id}`, data);
    return response.data;
  },

  async joinOpenMatch(id: string): Promise<OpenMatch> {
    const response = await apiClient.post<OpenMatch>(`/open-matches/${id}/join`);
    return response.data;
  },

  async leaveOpenMatch(id: string): Promise<OpenMatch> {
    const response = await apiClient.post<OpenMatch>(`/open-matches/${id}/leave`);
    return response.data;
  },

  async cancelOpenMatch(id: string): Promise<void> {
    await apiClient.delete(`/open-matches/${id}`);
  },
};
