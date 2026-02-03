# Futsal Booking System - Frontend Setup Guide

## Prerequisites

Before you begin, ensure you have the following installed:
- **Node.js** (v16 or higher) - [Download](https://nodejs.org/)
- **npm** (comes with Node.js) or **yarn**
- **Backend API** running on `http://localhost:8090`

## Installation Steps

### 1. Navigate to Frontend Directory

```bash
cd frontend
```

### 2. Install Dependencies

```bash
npm install
```

This will install all required dependencies including:
- React 18
- Material-UI (MUI)
- React Router
- Axios
- TypeScript
- date-fns

### 3. Environment Configuration

The `.env` file is already created with default values:

```env
REACT_APP_API_BASE_URL=http://localhost:8090/api/v1
```

If your backend runs on a different URL, update this file accordingly.

### 4. Start the Development Server

```bash
npm start
```

The application will open automatically at [http://localhost:3000](http://localhost:3000).

## Available Scripts

### `npm start`
Runs the app in development mode.
Open [http://localhost:3000](http://localhost:3000) to view it in your browser.

### `npm build`
Builds the app for production to the `build` folder.

### `npm test`
Launches the test runner in interactive watch mode.

## Project Structure

```
frontend/
├── public/                 # Static files
│   ├── index.html         # HTML template
│   └── manifest.json      # PWA manifest
├── src/
│   ├── components/        # React components
│   │   ├── auth/         # Authentication components
│   │   │   ├── Login.tsx
│   │   │   ├── Register.tsx
│   │   │   └── PrivateRoute.tsx
│   │   ├── booking/      # Booking components
│   │   │   ├── CreateBooking.tsx
│   │   │   └── MyBookings.tsx
│   │   ├── dashboard/    # Dashboard components
│   │   │   ├── OwnerDashboard.tsx
│   │   │   └── ManageGrounds.tsx
│   │   ├── grounds/      # Ground components
│   │   │   ├── GroundList.tsx
│   │   │   └── GroundDetails.tsx
│   │   ├── layout/       # Layout components
│   │   │   ├── Navbar.tsx
│   │   │   ├── Footer.tsx
│   │   │   └── Layout.tsx
│   │   └── payment/      # Payment components
│   │       ├── PaymentForm.tsx
│   │       └── PaymentHistory.tsx
│   ├── contexts/         # React contexts
│   │   └── AuthContext.tsx
│   ├── pages/            # Page components
│   │   └── Home.tsx
│   ├── services/         # API services
│   │   ├── api.ts
│   │   ├── authService.ts
│   │   ├── bookingService.ts
│   │   ├── fileService.ts
│   │   ├── groundService.ts
│   │   ├── paymentService.ts
│   │   └── reportService.ts
│   ├── types/            # TypeScript types
│   │   └── index.ts
│   ├── App.tsx           # Main app component
│   ├── index.tsx         # Entry point
│   └── index.css         # Global styles
├── package.json          # Dependencies
├── tsconfig.json         # TypeScript config
└── .env                  # Environment variables
```

## Features Included

### User Features
- ✅ User Registration & Login
- ✅ Browse Futsal Grounds
- ✅ Search & Filter Grounds
- ✅ View Ground Details
- ✅ Create Bookings
- ✅ View My Bookings
- ✅ Cancel Bookings
- ✅ Make Payments
- ✅ View Payment History

### Owner Features
- ✅ Owner Dashboard with Analytics
- ✅ Revenue Reports
- ✅ Booking Reports
- ✅ Customer Reports
- ✅ Manage Grounds (CRUD)
- ✅ Upload Ground Images

### Technical Features
- ✅ TypeScript for Type Safety
- ✅ Material-UI Components
- ✅ Responsive Design
- ✅ JWT Authentication
- ✅ Protected Routes
- ✅ Role-Based Access Control
- ✅ Context API for State Management
- ✅ Axios Interceptors
- ✅ Error Handling

## User Roles

The application supports three user roles:

1. **USER** - Regular users who can browse and book grounds
2. **OWNER** - Ground owners who can manage their grounds and view reports
3. **ADMIN** - Administrators with full access (to be implemented)

## API Integration

The frontend communicates with the Spring Boot backend API at:
```
http://localhost:8090/api/v1
```

Make sure the backend server is running before starting the frontend.

## Authentication Flow

1. User registers or logs in
2. Backend returns JWT token
3. Token is stored in localStorage
4. Axios interceptor adds token to all requests
5. Protected routes check authentication status

## Common Issues & Solutions

### Issue: "Network Error" or "Cannot connect to API"
**Solution:** Ensure the backend server is running on port 8090.

### Issue: "CORS Error"
**Solution:** The backend should have CORS enabled. Check `SecurityConfig.java` in the backend.

### Issue: Module not found errors
**Solution:** Run `npm install` again to ensure all dependencies are installed.

### Issue: TypeScript errors
**Solution:** Run `npm install --save-dev @types/react @types/react-dom @types/node`

## Next Steps

After setting up the frontend:

1. Start the backend server
2. Run `npm start` in the frontend directory
3. Open [http://localhost:3000](http://localhost:3000)
4. Register a new account or login
5. Start exploring the application!

## Development Tips

- Use **React DevTools** browser extension for debugging
- Use **Redux DevTools** if you add Redux later
- Check browser console for errors
- API responses are logged in network tab

## Production Build

To create a production build:

```bash
npm run build
```

This creates an optimized build in the `build` folder ready for deployment.

### Deployment Options

- **Netlify** - Drag and drop the build folder
- **Vercel** - Connect GitHub repo
- **AWS S3 + CloudFront** - Host static files
- **Docker** - Create Docker image with nginx

## Support

For issues or questions:
- Check the backend API documentation
- Review the code comments
- Check browser console for errors
- Ensure backend is running properly

## License

This project is part of the Futsal Booking System.
