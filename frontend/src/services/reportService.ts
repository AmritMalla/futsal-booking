import apiClient from './api';
import { Report, ReportType } from '../types';

export const reportService = {
  async getReportById(id: string): Promise<Report> {
    const response = await apiClient.get<Report>(`/reports/${id}`);
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
    ownerId: string,
    startDate?: string,
    endDate?: string
  ): Promise<Report> {
    const response = await apiClient.post<Report>(`/reports/generate/revenue/${ownerId}`, null, {
      params: { startDate, endDate },
    });
    return response.data;
  },

  async generateBookingsReport(
    ownerId: string,
    startDate?: string,
    endDate?: string
  ): Promise<Report> {
    const response = await apiClient.post<Report>(`/reports/generate/bookings/${ownerId}`, null, {
      params: { startDate, endDate },
    });
    return response.data;
  },

  async generateCustomersReport(
    ownerId: string,
    startDate?: string,
    endDate?: string
  ): Promise<Report> {
    const response = await apiClient.post<Report>(`/reports/generate/customers/${ownerId}`, null, {
      params: { startDate, endDate },
    });
    return response.data;
  },

  async deleteReport(id: string): Promise<void> {
    await apiClient.delete(`/reports/${id}`);
  },
};
