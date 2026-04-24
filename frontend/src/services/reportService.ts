import apiClient from './api';
import { Report, ReportType } from '../types';

export const reportService = {
  async getReportById(id: string): Promise<Report> {
    const response = await apiClient.get<Report>(`/reports/${id}`);
    return response.data;
  },

  async getMyReports(): Promise<Report[]> {
    const response = await apiClient.get<Report[]>('/reports/me');
    return response.data;
  },

  async getReportsByOwner(ownerId: string): Promise<Report[]> {
    const response = await apiClient.get<Report[]>(`/reports/owner/${ownerId}`);
    return response.data;
  },

  async getReportsByType(reportType: ReportType): Promise<Report[]> {
    const response = await apiClient.get<Report[]>(`/reports/type/${reportType}`);
    return response.data;
  },

  async getReportsByDateRange(startDate: string, endDate: string): Promise<Report[]> {
    const response = await apiClient.get<Report[]>('/reports/date-range', {
      params: { start: startDate, end: endDate },
    });
    return response.data;
  },

  async generateRevenueReport(
    startDate?: string,
    endDate?: string,
    ownerId?: string
  ): Promise<Report> {
    const endpoint = ownerId ? `/reports/generate/revenue/${ownerId}` : '/reports/generate/revenue';
    const response = await apiClient.post<Report>(endpoint, null, {
      params: { startDate, endDate },
    });
    return response.data;
  },

  async generateBookingsReport(
    startDate?: string,
    endDate?: string,
    ownerId?: string
  ): Promise<Report> {
    const endpoint = ownerId ? `/reports/generate/bookings/${ownerId}` : '/reports/generate/bookings';
    const response = await apiClient.post<Report>(endpoint, null, {
      params: { startDate, endDate },
    });
    return response.data;
  },

  async generateCustomersReport(
    startDate?: string,
    endDate?: string,
    ownerId?: string
  ): Promise<Report> {
    const endpoint = ownerId ? `/reports/generate/customers/${ownerId}` : '/reports/generate/customers';
    const response = await apiClient.post<Report>(endpoint, null, {
      params: { startDate, endDate },
    });
    return response.data;
  },

  async deleteReport(id: string): Promise<void> {
    await apiClient.delete(`/reports/${id}`);
  },
};
