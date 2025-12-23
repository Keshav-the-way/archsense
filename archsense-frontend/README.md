# ArchSense Frontend

AI-Powered Architecture Analysis Platform - Frontend Application

## Prerequisites

- Node.js 18+ and npm
- Angular CLI 17+

## Installation
```bash
npm install
```

## Development Server
```bash
npm start
```

Navigate to `http://localhost:4200/`. The application will automatically reload if you change any of the source files.

## Build
```bash
npm run build
```

The build artifacts will be stored in the `dist/` directory.

## Project Structure
```
src/
├── app/
│   ├── core/
│   │   ├── guards/          # Route guards
│   │   ├── interceptors/    # HTTP interceptors
│   │   ├── models/          # TypeScript interfaces
│   │   └── services/        # API services
│   ├── features/
│   │   ├── auth/            # Authentication pages
│   │   ├── dashboard/       # Dashboard
│   │   ├── projects/        # Project management
│   │   ├── artifacts/       # Artifact upload
│   │   └── analysis/        # Analysis features
│   └── shared/
│       ├── components/      # Shared components
│       └── pipes/           # Custom pipes
├── assets/                  # Static assets
└── environments/            # Environment configs
```

## Key Features

- JWT-based authentication
- Project management
- File upload with drag-and-drop
- Real-time analysis status updates
- Detailed analysis reports
- Responsive design

## API Configuration

Update `src/environments/environment.ts` to point to your API:
```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api'
};
```

## Authentication Flow

1. User registers or logs in
2. JWT token stored in memory (not localStorage for security)
3. Token automatically included in all API requests via interceptor
4. Auto-redirect to login on 401 errors

## Running with Backend

Ensure the backend API Gateway is running on port 8080, then start the frontend:
```bash
npm start
```