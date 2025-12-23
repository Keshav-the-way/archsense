import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    redirectTo: '/dashboard',
    pathMatch: 'full'
  },
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'register',
    loadComponent: () => import('./features/auth/register/register.component').then(m => m.RegisterComponent)
  },
  {
    path: 'dashboard',
    loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent),
    canActivate: [authGuard]
  },
  {
    path: 'projects/new',
    loadComponent: () => import('./features/projects/project-form/project-form.component').then(m => m.ProjectFormComponent),
    canActivate: [authGuard]
  },
  {
    path: 'projects/:id',
    loadComponent: () => import('./features/projects/project-detail/project-detail.component').then(m => m.ProjectDetailComponent),
    canActivate: [authGuard]
  },
  {
    path: 'projects/:id/upload',
    loadComponent: () => import('./features/artifacts/artifact-upload/artifact-upload.component').then(m => m.ArtifactUploadComponent),
    canActivate: [authGuard]
  },
  {
    path: 'projects/:id/analyze',
    loadComponent: () => import('./features/analysis/analysis-trigger/analysis-trigger.component').then(m => m.AnalysisTriggerComponent),
    canActivate: [authGuard]
  },
  {
    path: 'analyses/:id',
    loadComponent: () => import('./features/analysis/analysis-report/analysis-report.component').then(m => m.AnalysisReportComponent),
    canActivate: [authGuard]
  },
  {
    path: '**',
    redirectTo: '/dashboard'
  }
];