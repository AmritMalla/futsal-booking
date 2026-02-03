# Futsal Booking System - Frontend Complete Guide

## Overview

This React + TypeScript frontend provides a complete booking system interface for the Futsal Booking backend API.

## Quick Start

```bash
cd frontend
npm install
npm start
```

The app will open at [http://localhost:3000](http://localhost:3000)

## Technology Stack

| Technology | Version | Purpose |
|-----------|---------|---------|
| React | 18.2.0 | UI Framework |
| TypeScript | 4.9.5 | Type Safety |
| Material-UI | 5.15.15 | UI Components |
| React Router | 6.22.3 | Routing |
| Axios | 1.6.8 | HTTP Client |
| date-fns | 3.6.0 | Date Utilities |
| Context API | Built-in | State Management |

## Key Features Implemented

### 1. Authentication System
- **Login** (`/login`) - Email/password authentication
- **Register** (`/register`) - User registration with role selection
- **JWT Token Management** - Automatic token refresh and storage
- **Protected Routes** - Role-based access control

### 2. Ground Management
- **Browse Grounds** (`/grounds`) - List all available grounds
- **Search & Filter** - Search by name, surface type, or company
- **Ground Details** (`/grounds/:id`) - Detailed ground information
- **Owner Ground Management** (`/owner/grounds`) - CRUD operations

### 3. Booking System
- **Create Booking** (`/booking/new`) - Multi-step booking process
- **My Bookings** (`/my-bookings`) - View all user bookings
- **Cancel Booking** - Cancel confirmed bookings
- **Booking Status** - Track booking lifecycle

### 4. Payment Integration
- **Payment Form** (`/payment`) - Process booking payments
- **Payment History** (`/payment-history`) - View all transactions
- **Transaction Tracking** - Track payment status

### 5. Owner Dashboard
- **Analytics Dashboard** (`/owner/dashboard`) - Revenue and booking metrics
- **Generate Reports** - Revenue, bookings, and customer reports
- **Manage Grounds** - Add, edit, delete grounds
- **Revenue Tracking** - View revenue by ground

## Component Architecture

### Authentication Components

**AuthContext** (`src/contexts/AuthContext.tsx`)
```typescript
// Provides authentication state and methods
const { user, token, login, register, logout, isAuthenticated } = useAuth();
```

**Login** (`src/components/auth/Login.tsx`)
- Email/password form
- Error handling
- Redirect after login

**Register** (`src/components/auth/Register.tsx`)
- User registration form
- Role selection (USER/OWNER)
- Password confirmation
- Form validation

**PrivateRoute** (`src/components/auth/PrivateRoute.tsx`)
- Route protection wrapper
- Role-based access control
- Automatic redirects

### Ground Components

**GroundList** (`src/components/grounds/GroundList.tsx`)
- Display all grounds in grid layout
- Search and filter functionality
- Card-based design with images

**GroundDetails** (`src/components/grounds/GroundDetails.tsx`)
- Detailed ground information
- Price display
- Book now button

### Booking Components

**CreateBooking** (`src/components/booking/CreateBooking.tsx`)
- Multi-step booking wizard
- Ground selection
- Time slot selection (to be enhanced)
- Payment integration

**MyBookings** (`src/components/booking/MyBookings.tsx`)
- Table view of all bookings
- Status badges
- Cancel booking option
- Booking details

### Payment Components

**PaymentForm** (`src/components/payment/PaymentForm.tsx`)
- Payment processing interface
- Booking details display
- Transaction ID generation
- Success/error handling

**PaymentHistory** (`src/components/payment/PaymentHistory.tsx`)
- Payment transaction list
- Status tracking
- Filter by status

### Dashboard Components

**OwnerDashboard** (`src/components/dashboard/OwnerDashboard.tsx`)
- Key metrics cards (Revenue, Bookings, etc.)
- Report generation buttons
- Revenue by ground breakdown
- Monthly analytics

**ManageGrounds** (`src/components/dashboard/ManageGrounds.tsx`)
- Ground CRUD operations
- Table view with actions
- Add/Edit dialog
- Delete confirmation

## Service Layer

All API calls are centralized in the `services/` directory:

### API Client (`services/api.ts`)
```typescript
// Axios instance with interceptors
// - Adds JWT token to requests
// - Handles 401 errors
// - Automatic token refresh
```

### Auth Service (`services/authService.ts`)
```typescript
authService.login(credentials)
authService.register(data)
authService.logout()
authService.getToken()
```

### Ground Service (`services/groundService.ts`)
```typescript
groundService.getAllGrounds()
groundService.getGroundById(id)
groundService.searchGrounds(params)
groundService.createGround(data)
groundService.updateGround(id, data)
groundService.uploadGroundImage(id, file)
groundService.deleteGround(id)
```

### Booking Service (`services/bookingService.ts`)
```typescript
bookingService.createBooking(data)
bookingService.getBookingsByUser(userId)
bookingService.cancelBooking(id)
bookingService.updateBookingStatus(id, status)
```

### Payment Service (`services/paymentService.ts`)
```typescript
paymentService.createPayment(data)
paymentService.getPaymentsByUser(userId)
paymentService.getPaymentsByBooking(bookingId)
paymentService.refundPayment(id)
```

### Report Service (`services/reportService.ts`)
```typescript
reportService.generateRevenueReport(ownerId, startDate?, endDate?)
reportService.generateBookingsReport(ownerId, startDate?, endDate?)
reportService.generateCustomersReport(ownerId, startDate?, endDate?)
```

## Type System

All types are defined in `src/types/index.ts`:

```typescript
// Enums
UserRole, BookingStatus, PaymentStatus, ReportType

// Entities
User, FutsalGround, Booking, Payment, Report, Review

// Request Types
LoginRequest, RegisterRequest, BookingRequest, PaymentRequest

// Response Types
LoginResponse, FutsalGroundResponse, BookingResponse
```

## Routing Structure

```
/ (Home)
├── /login (Public)
├── /register (Public)
├── /grounds (Public)
│   └── /grounds/:id (Public)
├── /my-bookings (Protected - All Users)
├── /booking/new (Protected - All Users)
├── /payment (Protected - All Users)
├── /payment-history (Protected - All Users)
├── /owner/dashboard (Protected - Owner Only)
└── /owner/grounds (Protected - Owner Only)
```

## State Management

Using **React Context API** for global state:

### AuthContext
- User authentication state
- JWT token management
- Login/logout methods
- User profile data

## Styling Approach

### Material-UI Theme
```typescript
const theme = createTheme({
  palette: {
    primary: { main: '#667eea' },
    secondary: { main: '#764ba2' },
  },
});
```

### Responsive Design
- Mobile-first approach
- Grid system for layouts
- Responsive navigation
- Adaptive components

## Best Practices Implemented

1. **Type Safety** - Full TypeScript coverage
2. **Code Splitting** - Route-based code splitting
3. **Error Handling** - Centralized error handling
4. **Loading States** - Consistent loading indicators
5. **Form Validation** - Client-side validation
6. **Security** - Protected routes, JWT management
7. **Responsive** - Mobile and desktop support
8. **Accessibility** - Semantic HTML, ARIA labels

## Environment Variables

```env
REACT_APP_API_BASE_URL=http://localhost:8090/api/v1
```

## Development Workflow

1. **Start Backend** - Ensure API is running on port 8090
2. **Start Frontend** - Run `npm start`
3. **Hot Reload** - Changes auto-reload
4. **Check Console** - Monitor for errors
5. **Test API Calls** - Use browser DevTools Network tab

## Future Enhancements

### Suggested Features to Add:

1. **Time Slot Management**
   - Visual calendar for slot selection
   - Real-time availability checking
   - Recurring bookings

2. **Advanced Search**
   - Location-based search
   - Price range filters
   - Rating filters
   - Availability filters

3. **Reviews & Ratings**
   - User reviews for grounds
   - Star ratings
   - Review moderation

4. **Notifications**
   - Booking confirmations
   - Payment receipts
   - Cancellation notifications

5. **File Upload**
   - Ground image gallery
   - User profile pictures
   - Document uploads

6. **Admin Panel**
   - User management
   - System-wide reports
   - Content moderation

7. **Payment Gateway Integration**
   - Khalti integration
   - eSewa integration
   - IME Pay integration

8. **Real-time Features**
   - Live booking updates
   - Chat support
   - Notifications

## Troubleshooting

### Common Issues

**Issue: Blank page after login**
```
Solution: Check browser console for errors
Ensure user data is properly stored in localStorage
```

**Issue: API calls failing**
```
Solution: Verify backend is running on port 8090
Check CORS configuration in backend
Verify JWT token is valid
```

**Issue: TypeScript errors**
```
Solution: Run npm install to ensure all types are installed
Check tsconfig.json configuration
```

**Issue: Components not rendering**
```
Solution: Check React DevTools for component tree
Verify imports are correct
Check for console errors
```

## Testing the Application

### Manual Testing Steps:

1. **Authentication Flow**
   - Register new user
   - Login with credentials
   - Verify token storage
   - Test logout

2. **Ground Browsing**
   - View all grounds
   - Search functionality
   - View ground details
   - Test responsive layout

3. **Booking Flow**
   - Create new booking
   - View bookings list
   - Cancel booking
   - Verify status updates

4. **Payment Flow**
   - Process payment
   - View payment history
   - Check transaction tracking

5. **Owner Dashboard**
   - View analytics
   - Generate reports
   - Manage grounds
   - CRUD operations

## Deployment Guide

### Build for Production

```bash
npm run build
```

### Deploy to Netlify

```bash
# Install Netlify CLI
npm install -g netlify-cli

# Deploy
netlify deploy --prod --dir=build
```

### Deploy to Vercel

```bash
# Install Vercel CLI
npm install -g vercel

# Deploy
vercel --prod
```

### Environment Variables for Production

Update `.env` for production:
```env
REACT_APP_API_BASE_URL=https://your-api-domain.com/api/v1
```

## Performance Optimization

1. **Code Splitting** - Already implemented via React.lazy
2. **Image Optimization** - Use WebP format for images
3. **Caching** - Implement service workers
4. **Lazy Loading** - Load components on demand
5. **Memoization** - Use React.memo for expensive components

## Contributing

When adding new features:

1. Follow existing code structure
2. Add TypeScript types for new entities
3. Create corresponding services for API calls
4. Add proper error handling
5. Update this documentation

## Support Resources

- **React Docs**: https://react.dev
- **Material-UI Docs**: https://mui.com
- **TypeScript Docs**: https://www.typescriptlang.org/docs
- **React Router Docs**: https://reactrouter.com

## Summary

This frontend application provides a complete, production-ready interface for the Futsal Booking System with:
- ✅ Full TypeScript support
- ✅ Material-UI components
- ✅ Role-based authentication
- ✅ Comprehensive API integration
- ✅ Responsive design
- ✅ Owner dashboard with analytics
- ✅ Complete booking workflow
- ✅ Payment processing

The application is ready to use and can be extended with additional features as needed!
