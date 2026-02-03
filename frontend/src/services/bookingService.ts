import apiClient from './api';
import { Booking, BookingRequest, BookingStatus } from '../types';

export const bookingService = {
  async createBooking(data: BookingRequest): Promise<Booking> {
    const response = await apiClient.post<Booking>('/bookings', data);
    return response.data;
  },

  async getBookingById(id: string): Promise<Booking> {
    const response = await apiClient.get<Booking>(`/bookings/${id}`);
    return response.data;
  },

  async getBookingsByUser(userId: string): Promise<Booking[]> {
    const response = await apiClient.get<Booking[]>(`/bookings/user/${userId}`);
    return response.data;
  },

  async getBookingsByGround(groundId: string): Promise<Booking[]> {
    const response = await apiClient.get<Booking[]>(`/bookings/ground/${groundId}`);
    return response.data;
  },

  async getBookingsByStatus(status: BookingStatus): Promise<Booking[]> {
    const response = await apiClient.get<Booking[]>(`/bookings/status/${status}`);
    return response.data;
  },

  async getBookingsByDateRange(startDate: string, endDate: string): Promise<Booking[]> {
    const response = await apiClient.get<Booking[]>('/bookings/date-range', {
      params: { startDate, endDate },
    });
    return response.data;
  },

  async updateBookingStatus(id: string, status: BookingStatus): Promise<Booking> {
    const response = await apiClient.put<Booking>(`/bookings/${id}/status`, null, {
      params: { status },
    });
    return response.data;
  },

  async cancelBooking(id: string): Promise<Booking> {
    const response = await apiClient.post<Booking>(`/bookings/${id}/cancel`);
    return response.data;
  },

  async deleteBooking(id: string): Promise<void> {
    await apiClient.delete(`/bookings/${id}`);
  },
};
