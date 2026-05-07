# Futsal Arena - Frontend

This is the React frontend for the Futsal Arena.

## Tech Stack

- **React 18** with TypeScript
- **Material-UI (MUI)** for UI components
- **React Router** for routing
- **Context API** for state management
- **Axios** for API calls
- **date-fns** for date manipulation

## Getting Started

### Prerequisites

- Node.js (v16 or higher)
- npm or yarn

### Installation

```bash
cd frontend
npm install
```

### Configuration

Copy `.env.example` to `.env` and update the API URL if needed:

```bash
cp .env.example .env
```

### Running the Development Server

```bash
npm start
```

The application will open at [http://localhost:3000](http://localhost:3000).

### Building for Production

```bash
npm run build
```

## Project Structure

```
src/
├── components/          # Reusable UI components
│   ├── auth/           # Authentication components
│   ├── booking/        # Booking-related components
│   ├── grounds/        # Futsal ground components
│   ├── payments/       # Payment components
│   ├── dashboard/      # Admin/Owner dashboard
│   └── common/         # Common components
├── contexts/           # React Context providers
├── services/           # API service layer
├── types/              # TypeScript type definitions
├── utils/              # Utility functions
├── pages/              # Page components
└── App.tsx             # Main application component
```

## Features

- User Authentication (Login/Register)
- Browse and Search Futsal Grounds
- Book Time Slots
- Payment Processing
- User Profile Management
- Admin Dashboard
- Owner Dashboard with Reports
- Ground Management

## API Integration

The frontend connects to the Spring Boot backend API at `http://localhost:8090/api/v1`.

Make sure the backend server is running before starting the frontend.
