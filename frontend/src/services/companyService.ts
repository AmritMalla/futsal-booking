import apiClient from './api';
import { FutsalCompany } from '../types';

export const companyService = {
  async getMyCompanies(): Promise<FutsalCompany[]> {
    const response = await apiClient.get<FutsalCompany[]>('/companies/me');
    return response.data;
  },

  async getCompaniesByOwner(ownerId: string): Promise<FutsalCompany[]> {
    const response = await apiClient.get<FutsalCompany[]>(`/companies/owner/${ownerId}`);
    return response.data;
  },
};
