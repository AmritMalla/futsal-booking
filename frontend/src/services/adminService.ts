import apiClient from './api';
import { User, FutsalGround } from '../types';

export interface FutsalCompany {
  id: string;
  name: string;
  location: string;
  ownerId: string;
  ownerName: string;
  createdAt: string;
}

export interface AdminStats {
  totalUsers: number;
  totalOwners: number;
  totalCustomers: number;
  totalCompanies: number;
  totalGrounds: number;
  totalBookings: number;
}

export const adminService = {
  // User Management
  async getAllUsers(): Promise<User[]> {
    const response = await apiClient.get<User[]>('/admin/users');
    return response.data;
  },

  async getAllOwners(): Promise<User[]> {
    const response = await apiClient.get<User[]>('/admin/owners');
    return response.data;
  },

  async getAllCustomers(): Promise<User[]> {
    const response = await apiClient.get<User[]>('/admin/customers');
    return response.data;
  },

  async deleteUser(userId: string): Promise<void> {
    await apiClient.delete(`/admin/users/${userId}`);
  },

  // Company Management
  async getAllCompanies(): Promise<FutsalCompany[]> {
    const response = await apiClient.get<FutsalCompany[]>('/admin/companies');
    return response.data;
  },

  async deleteCompany(companyId: string): Promise<void> {
    await apiClient.delete(`/admin/companies/${companyId}`);
  },

  // Ground Management
  async getAllGrounds(): Promise<FutsalGround[]> {
    const response = await apiClient.get<FutsalGround[]>('/admin/grounds');
    return response.data;
  },

  async deleteGround(groundId: string): Promise<void> {
    await apiClient.delete(`/admin/grounds/${groundId}`);
  },

  // Stats - computed from other endpoints
  async getStats(): Promise<AdminStats> {
    const [users, owners, customers, companies, grounds] = await Promise.all([
      this.getAllUsers(),
      this.getAllOwners(),
      this.getAllCustomers(),
      this.getAllCompanies(),
      this.getAllGrounds(),
    ]);

    return {
      totalUsers: users.length,
      totalOwners: owners.length,
      totalCustomers: customers.length,
      totalCompanies: companies.length,
      totalGrounds: grounds.length,
      totalBookings: 0, // Would need a separate endpoint
    };
  },
};
