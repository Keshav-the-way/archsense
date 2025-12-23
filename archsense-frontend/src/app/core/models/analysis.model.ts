// analysis.model.ts - UPDATED with Phase 1 features

export interface Analysis {
  id: string;
  projectId: string;
  userId: string;
  artifactIds: string[];
  status: AnalysisStatus;
  createdAt: string;
  updatedAt?: string;
  completedAt?: string;
  reportUrl?: string;
  error?: string;
  
  // NEW Phase 1 fields
  version?: number;
  previousAnalysisId?: string;
  evolutionMetrics?: EvolutionMetrics;
}

export enum AnalysisStatus {
  PENDING = 'PENDING',
  IN_PROGRESS = 'IN_PROGRESS',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED'
}

export interface CreateAnalysisRequest {
  artifactIds: string[];
}

export interface AnalysisReport {
  analysisId: string;
  projectId?: string;
  summary: string;
  architecturePattern: string;
  components: string[];
  connections: string[];
  issues: ReportIssue[];
  recommendations: ReportRecommendation[];
  generatedAt: string;
  
  // NEW Phase 1 fields
  version?: number;
  evolutionAnalysis?: EvolutionAnalysis;
  costEstimation?: CostEstimation;
}

export interface ReportIssue {
  severity: string;
  category: string;
  description: string;
  location: string;
  confidenceScore?: number;  // NEW: 0.0 - 1.0
}

export interface ReportRecommendation {
  priority: string;
  title: string;
  description: string;
  benefit: string;
}

// NEW Phase 1 interfaces

export interface EvolutionMetrics {
  issuesResolved: number;
  newIssues: number;
  issuesRegressed: number;
  improvementScore: number;  // 0-100
}

export interface EvolutionAnalysis {
  resolvedIssues: string[];
  newIssues: string[];
  regressedIssues: string[];
  overallTrend: 'IMPROVING' | 'STABLE' | 'DEGRADING' | 'UNKNOWN';
}

export interface CostEstimation {
  monthlyEstimateUsd: number;
  costTier: 'LOW' | 'MEDIUM' | 'HIGH';
  componentCosts: ComponentCost[];
  costOptimizations: string[];
}

export interface ComponentCost {
  componentName: string;
  serviceType: 'compute' | 'database' | 'storage' | 'network' | 'other';
  estimatedMonthlyCost: number;
}