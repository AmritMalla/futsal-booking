import apiClient from './api';
import { User, FutsalGround, Review, FutsalGroundRequest, UserRole } from '../types';
import {
  AdminStats,
  RevenueAnalytics,
  BookingAnalytics,
  UserAnalytics,
  FutsalCompany,
  FutsalCompanyRequest,
  AdminTimeSlot,
  AdminBooking,
  AdminPayment,
  CreateUserRequest,
  UpdateUserRequest,
  TimeSlotRequest,
  BookingRequest,
  PaymentRequest,
  ReviewRequest,
  TimeSlotFilters,
  BookingFilters,
  PaymentFilters,
  ReviewFilters,
  AnalyticsDateRange,
} from '../types/admin';

export const adminService = {
  // ==================== Analytics ====================

  async getAdminStats(): Promise<AdminStats> {
    const response = await apiClient.get<AdminStats>('/admin/stats');
    return response.data;
  },

  async getRevenueAnalytics(params?: AnalyticsDateRange): Promise<RevenueAnalytics> {
    const response = await apiClient.get<RevenueAnalytics>('/admin/analytics/revenue', { params });
    return response.data;
  },

  async getBookingAnalytics(params?: AnalyticsDateRange): Promise<BookingAnalytics> {
    const response = await apiClient.get<BookingAnalytics>('/admin/analytics/bookings', { params });
    return response.data;
  },

  async getUserAnalytics(): Promise<UserAnalytics> {
    const response = await apiClient.get<UserAnalytics>('/admin/analytics/users');
    return response.data;
  },

  // ==================== User Management ====================

  async getAllUsers(): Promise<User[]> {
    const response = await apiClient.get<User[]>('/admin/users');
    return response.data;
  },

  async getUserById(userId: string): Promise<User> {
    const response = await apiClient.get<User>(`/admin/users/${userId}`);
    return response.data;
  },

  async createUser(request: CreateUserRequest): Promise<User> {
    const response = await apiClient.post<User>('/admin/users', request);
    return response.data;
  },

  async updateUser(userId: string, request: UpdateUserRequest): Promise<User> {
    const response = await apiClient.put<User>(`/admin/users/${userId}`, request);
    return response.data;
  },

  async updateUserRole(userId: string, role: UserRole): Promise<User> {
    const response = await apiClient.patch<User>(`/admin/users/${userId}/role`, null, {
      params: { role },
    });
    return response.data;
  },

  async deleteUser(userId: string): Promise<void> {
    await apiClient.delete(`/admin/users/${userId}`);
  },

  async getAllOwners(): Promise<User[]> {
    const response = await apiClient.get<User[]>('/admin/owners');
    return response.data;
  },

  async getAllCustomers(): Promise<User[]> {
    const response = await apiClient.get<User[]>('/admin/customers');
    return response.data;
  },

  // ==================== Company Management ====================

  async getAllCompanies(): Promise<FutsalCompany[]> {
    const response = await apiClient.get<FutsalCompany[]>('/admin/companies');
    return response.data;
  },

  async getCompanyById(companyId: string): Promise<FutsalCompany> {
    const response = await apiClient.get<FutsalCompany>(`/admin/companies/${companyId}`);
    return response.data;
  },

  async createCompany(request: FutsalCompanyRequest): Promise<FutsalCompany> {
    const response = await apiClient.post<FutsalCompany>('/admin/companies', request);
    return response.data;
  },

  async updateCompany(companyId: string, request: FutsalCompanyRequest): Promise<FutsalCompany> {
    const response = await apiClient.put<FutsalCompany>(`/admin/companies/${companyId}`, request);
    return response.data;
  },

  async deleteCompany(companyId: string): Promise<void> {
    await apiClient.delete(`/admin/companies/${companyId}`);
  },

  // ==================== Ground Management ====================

  async getAllGrounds(): Promise<FutsalGround[]> {
    const response = await apiClient.get<FutsalGround[]>('/admin/grounds');
    return response.data;
  },

  async getGroundById(groundId: string): Promise<FutsalGround> {
    const response = await apiClient.get<FutsalGround>(`/admin/grounds/${groundId}`);
    return response.data;
  },

  async createGround(request: FutsalGroundRequest): Promise<FutsalGround> {
    const response = await apiClient.post<FutsalGround>('/admin/grounds', request);
    return response.data;
  },

  async updateGround(groundId: string, request: FutsalGroundRequest): Promise<FutsalGround> {
    const response = await apiClient.put<FutsalGround>(`/admin/grounds/${groundId}`, request);
    return response.data;
  },

  async deleteGround(groundId: string): Promise<void> {
    await apiClient.delete(`/admin/grounds/${groundId}`);
  },

  // ==================== TimeSlot Management ====================

  async getAllTimeSlots(filters?: TimeSlotFilters): Promise<AdminTimeSlot[]> {
    const response = await apiClient.get<AdminTimeSlot[]>('/admin/timeslots', { params: filters });
    return response.data;
  },

  async getTimeSlotById(slotId: string): Promise<AdminTimeSlot> {
    const response = await apiClient.get<AdminTimeSlot>(`/admin/timeslots/${slotId}`);
    return response.data;
  },

  async createTimeSlot(request: TimeSlotRequest): Promise<AdminTimeSlot> {
    const response = await apiClient.post<AdminTimeSlot>('/admin/timeslots', request);
    return response.data;
  },

  async updateTimeSlot(slotId: string, request: TimeSlotRequest): Promise<AdminTimeSlot> {
    const response = await apiClient.put<AdminTimeSlot>(`/admin/timeslots/${slotId}`, request);
    return response.data;
  },

  async deleteTimeSlot(slotId: string): Promise<void> {
    await apiClient.delete(`/admin/timeslots/${slotId}`);
  },

  // ==================== Booking Management ====================

  async getAllBookings(filters?: BookingFilters): Promise<AdminBooking[]> {
    const response = await apiClient.get<AdminBooking[]>('/admin/bookings', { params: filters });
    return response.data;
  },

  async getBookingById(bookingId: string): Promise<AdminBooking> {
    const response = await apiClient.get<AdminBooking>(`/admin/bookings/${bookingId}`);
    return response.data;
  },

  async updateBooking(bookingId: string, request: BookingRequest): Promise<AdminBooking> {
    const response = await apiClient.put<AdminBooking>(`/admin/bookings/${bookingId}`, request);
    return response.data;
  },

  async updateBookingStatus(bookingId: string, status: string): Promise<AdminBooking> {
    const response = await apiClient.patch<AdminBooking>(`/admin/bookings/${bookingId}/status`, null, {
      params: { status },
    });
    return response.data;
  },

  async deleteBooking(bookingId: string): Promise<void> {
    await apiClient.delete(`/admin/bookings/${bookingId}`);
  },

  // ==================== Payment Management ====================

  async getAllPayments(filters?: PaymentFilters): Promise<AdminPayment[]> {
    const response = await apiClient.get<AdminPayment[]>('/admin/payments', { params: filters });
    return response.data;
  },

  async getPaymentById(paymentId: string): Promise<AdminPayment> {
    const response = await apiClient.get<AdminPayment>(`/admin/payments/${paymentId}`);
    return response.data;
  },

  async updatePayment(paymentId: string, request: PaymentRequest): Promise<AdminPayment> {
    const response = await apiClient.put<AdminPayment>(`/admin/payments/${paymentId}`, request);
    return response.data;
  },

  async updatePaymentStatus(paymentId: string, status: string): Promise<AdminPayment> {
    const response = await apiClient.patch<AdminPayment>(`/admin/payments/${paymentId}/status`, null, {
      params: { status },
    });
    return response.data;
  },

  async deletePayment(paymentId: string): Promise<void> {
    await apiClient.delete(`/admin/payments/${paymentId}`);
  },

  // ==================== Review Management ====================

  async getAllReviews(filters?: ReviewFilters): Promise<Review[]> {
    const response = await apiClient.get<Review[]>('/admin/reviews', { params: filters });
    return response.data;
  },

  async getReviewById(reviewId: string): Promise<Review> {
    const response = await apiClient.get<Review>(`/admin/reviews/${reviewId}`);
    return response.data;
  },

  async updateReview(reviewId: string, request: ReviewRequest): Promise<Review> {
    const response = await apiClient.put<Review>(`/admin/reviews/${reviewId}`, request);
    return response.data;
  },

  async deleteReview(reviewId: string): Promise<void> {
    await apiClient.delete(`/admin/reviews/${reviewId}`);
  },
};
