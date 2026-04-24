import { UserRole, BookingStatus, PaymentStatus } from './index';

// ==================== Admin Statistics ====================

export interface AdminStats {
  totalUsers: number;
  totalOwners: number;
  totalCustomers: number;
  totalAdmins: number;
  totalCompanies: number;
  totalGrounds: number;
  totalTimeSlots: number;
  totalBookings: number;
  confirmedBookings: number;
  cancelledBookings: number;
  completedBookings: number;
  totalRevenue: number;
  pendingRevenue: number;
  successRevenue: number;
  totalReviews: number;
  averageRating: number;
  recentActivityCount: number;
}

// ==================== Analytics Responses ====================

export interface RevenueAnalytics {
  totalRevenue: number;
  monthlyRevenue: number;
  dailyRevenue: number;
  revenueByGround: Record<string, number>;
  revenueByCompany: Record<string, number>;
  revenueByStatus: Record<string, number>;
  revenueGrowth: number;
}

export interface PeakHour {
  hour: string;
  bookingCount: number;
}

export interface BookingTrend {
  date: string;
  bookingCount: number;
}

export interface BookingAnalytics {
  totalBookings: number;
  bookingsByStatus: Record<string, number>;
  bookingsByGround: Record<string, number>;
  bookingsByDay: Record<string, number>;
  peakHours: PeakHour[];
  averageBookingValue: number;
  bookingTrends: BookingTrend[];
}

export interface TopCustomer {
  id: string;
  name: string;
  email: string;
  totalBookings: number;
  totalSpent: number;
}

export interface UserAnalytics {
  totalUsers: number;
  newUsersThisMonth: number;
  activeUsers: number;
  usersByRole: Record<string, number>;
  userGrowthRate: number;
  topCustomers: TopCustomer[];
}

// ==================== Extended Entity Types ====================

export interface FutsalCompany {
  id: string;
  name: string;
  location: string;
  ownerId: string;
  ownerName: string;
  createdAt: string;
}

export interface FutsalCompanyRequest {
  name: string;
  location: string;
  ownerId: string;
}

export interface AdminTimeSlot {
  id: string;
  groundId: string;
  groundName: string;
  companyName: string;
  startTime: string;
  endTime: string;
  isBooked: boolean;
  version: number;
}

export interface AdminBooking {
  id: string;
  userId: string;
  userName: string;
  userEmail: string;
  groundId: string;
  groundName: string;
  companyName: string;
  slotId: string;
  slotStartTime: string;
  slotEndTime: string;
  bookingDate: string;
  status: BookingStatus;
  paymentStatus?: PaymentStatus;
  paymentAmount?: number;
  transactionId?: string;
}

export interface AdminPayment {
  id: string;
  bookingId: string;
  userId: string;
  userName: string;
  userEmail: string;
  groundId: string;
  groundName: string;
  slotStartTime: string;
  slotEndTime: string;
  amount: number;
  paymentStatus: PaymentStatus;
  transactionId: string;
  createdAt: string;
}

// ==================== Request Types ====================

export interface CreateUserRequest {
  name: string;
  email: string;
  password: string;
  phoneNumber?: string;
  role: UserRole;
}

export interface UpdateUserRequest {
  name?: string;
  email?: string;
  phoneNumber?: string;
}

export interface TimeSlotRequest {
  groundId: string;
  startTime: string;
  endTime: string;
}

export interface BookingRequest {
  groundId: string;
  slotId: string;
}

export interface PaymentRequest {
  bookingId: string;
  amount: number;
  transactionId?: string;
}

export interface ReviewRequest {
  userId: string;
  groundId: string;
  rating: number;
  reviewText?: string;
}

// ==================== Filter Types ====================

export interface TimeSlotFilters {
  groundId?: string;
  isBooked?: boolean;
}

export interface BookingFilters {
  status?: BookingStatus;
  userId?: string;
  groundId?: string;
}

export interface PaymentFilters {
  status?: PaymentStatus;
  userId?: string;
}

export interface ReviewFilters {
  groundId?: string;
  minRating?: number;
}

export interface AnalyticsDateRange {
  startDate?: string;
  endDate?: string;
}
