import apiClient from './api';
import { Payment, PaymentRequest, PaymentStatus } from '../types';

export const paymentService = {
  async createPayment(data: PaymentRequest): Promise<Payment> {
    const response = await apiClient.post<Payment>('/payments', data);
    return response.data;
  },

  async getPaymentById(id: string): Promise<Payment> {
    const response = await apiClient.get<Payment>(`/payments/${id}`);
    return response.data;
  },

  async getPaymentByTransaction(transactionId: string): Promise<Payment> {
    const response = await apiClient.get<Payment>(`/payments/transaction/${transactionId}`);
    return response.data;
  },

  async getPaymentsByBooking(bookingId: string): Promise<Payment[]> {
    const response = await apiClient.get<Payment[]>(`/payments/booking/${bookingId}`);
    return response.data;
  },

  async getPaymentsByUser(userId: string): Promise<Payment[]> {
    const response = await apiClient.get<Payment[]>(`/payments/user/${userId}`);
    return response.data;
  },

  async getPaymentsByStatus(status: PaymentStatus): Promise<Payment[]> {
    const response = await apiClient.get<Payment[]>(`/payments/status/${status}`);
    return response.data;
  },

  async updatePaymentStatus(id: string, status: PaymentStatus): Promise<Payment> {
    const response = await apiClient.put<Payment>(`/payments/${id}/status`, null, {
      params: { status },
    });
    return response.data;
  },

  async refundPayment(id: string): Promise<Payment> {
    const response = await apiClient.post<Payment>(`/payments/${id}/refund`);
    return response.data;
  },
};
