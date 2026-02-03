// Enums
export enum UserRole {
  ADMIN = 'ADMIN',
  OWNER = 'OWNER',
  USER = 'USER'
}

export enum BookingStatus {
  CONFIRMED = 'CONFIRMED',
  CANCELLED = 'CANCELLED',
  COMPLETED = 'COMPLETED'
}

export enum PaymentStatus {
  PENDING = 'PENDING',
  SUCCESS = 'SUCCESS',
  FAILED = 'FAILED',
  REFUNDED = 'REFUNDED'
}

export enum ReportType {
  REVENUE = 'REVENUE',
  BOOKINGS = 'BOOKINGS',
  CUSTOMERS = 'CUSTOMERS'
}

// User Types
export interface User {
  id: string;
  name: string;
  email: string;
  phoneNumber?: string;
  role: UserRole;
  createdAt: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
  phoneNumber?: string;
  role: UserRole;
}

export interface LoginResponse {
  token: string;
  type: string;
  email: string;
  role: UserRole;
}

// Futsal Ground Types
export interface FutsalGround {
  id: string;
  companyId: string;
  companyName: string;
  name: string;
  surfaceType: string;
  pricePerHour: number;
  imageUrl?: string;
  createdAt: string;
}

export interface FutsalGroundRequest {
  companyId: string;
  name: string;
  surfaceType: string;
  pricePerHour: number;
  imageUrl?: string;
}

export interface GroundSearchParams {
  location?: string;
  surfaceType?: string;
  minPrice?: number;
  maxPrice?: number;
}

// TimeSlot Types
export interface TimeSlot {
  id: string;
  groundId: string;
  startTime: string;
  endTime: string;
  isBooked: boolean;
}

export interface TimeSlotRequest {
  groundId: string;
  startTime: string;
  endTime: string;
}

// Booking Types
export interface Booking {
  id: string;
  userId: string;
  userName: string;
  groundId: string;
  groundName: string;
  slotId: string;
  slotStartTime: string;
  slotEndTime: string;
  bookingDate: string;
  status: BookingStatus;
}

export interface BookingRequest {
  userId: string;
  groundId: string;
  slotId: string;
}

// Payment Types
export interface Payment {
  id: string;
  bookingId: string;
  userId: string;
  userName: string;
  amount: number;
  paymentStatus: PaymentStatus;
  transactionId: string;
}

export interface PaymentRequest {
  bookingId: string;
  userId: string;
  amount: number;
  transactionId: string;
}

// Review Types
export interface Review {
  id: string;
  userId: string;
  userName: string;
  groundId: string;
  groundName: string;
  rating: number;
  reviewText?: string;
  createdAt: string;
}

export interface ReviewRequest {
  userId: string;
  groundId: string;
  rating: number;
  reviewText?: string;
}

// Report Types
export interface MonthlyBreakdown {
  month: string;
  revenue: number;
  bookingCount: number;
}

export interface RevenueReportData {
  totalRevenue: number;
  totalBookings: number;
  averageBookingValue: number;
  revenueByGround: Record<string, number>;
  monthlyBreakdown: MonthlyBreakdown[];
}

export interface DailyBreakdown {
  date: string;
  bookingCount: number;
}

export interface BookingsReportData {
  totalBookings: number;
  confirmedBookings: number;
  cancelledBookings: number;
  completedBookings: number;
  bookingsByGround: Record<string, number>;
  dailyBreakdown: DailyBreakdown[];
}

export interface TopCustomer {
  customerId: string;
  customerName: string;
  totalBookings: number;
  totalSpent: number;
}

export interface CustomersReportData {
  totalCustomers: number;
  newCustomers: number;
  returningCustomers: number;
  topCustomers: TopCustomer[];
}

export interface Report {
  id: string;
  ownerId: string;
  ownerName: string;
  reportType: ReportType;
  reportData: RevenueReportData | BookingsReportData | CustomersReportData;
  startDate?: string;
  endDate?: string;
  generatedAt: string;
}

// File Upload Types
export interface FileUploadResponse {
  fileName: string;
  fileUrl: string;
  fileType: string;
  size: string;
}

// Error Types
export interface ApiError {
  status: number;
  message: string;
  path?: string;
  timestamp?: string;
  errors?: Record<string, string>;
}

// Auth Context Types
export interface AuthContextType {
  user: User | null;
  token: string | null;
  login: (email: string, password: string) => Promise<void>;
  register: (data: RegisterRequest) => Promise<void>;
  logout: () => void;
  isAuthenticated: boolean;
  isLoading: boolean;
}
