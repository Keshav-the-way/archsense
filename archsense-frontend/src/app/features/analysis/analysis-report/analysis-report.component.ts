// analysis-report.component.ts - UPDATED with Phase 1 features

import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { interval, Subscription } from 'rxjs';
import { HeaderComponent } from '../../../shared/components/header/header.component';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { AnalysisService } from '../../../core/services/analysis.service';
import { 
  Analysis, 
  AnalysisStatus, 
  AnalysisReport,
  ComponentCost 
} from '../../../core/models/analysis.model';

@Component({
  selector: 'app-analysis-report',
  standalone: true,
  imports: [CommonModule, HeaderComponent, LoadingSpinnerComponent, RouterModule],
  templateUrl: './analysis-report.component.html',
  styleUrls: ['./analysis-report.component.scss']
})
export class AnalysisReportComponent implements OnInit {
  analysisId!: string;
  analysis: Analysis | null = null;
  report: AnalysisReport | null = null;
  loading = true;
  errorMessage = '';
  pollSubscription?: Subscription;

  AnalysisStatus = AnalysisStatus;

  // NEW: Track if we should show evolution section
  showEvolution = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private analysisService: AnalysisService
  ) {}

  ngOnInit(): void {
    this.analysisId = this.route.snapshot.paramMap.get('id')!;
    this.loadAnalysis();
  }

  ngOnDestroy(): void {
    this.stopPolling();
  }

  loadAnalysis(): void {
    this.analysisService.getById(this.analysisId).subscribe({
      next: (analysis) => {
        this.analysis = analysis;
        this.loading = false;

        if (analysis.status === AnalysisStatus.COMPLETED) {
          this.loadReport();
        } else if (analysis.status === AnalysisStatus.PENDING || analysis.status === AnalysisStatus.IN_PROGRESS) {
          this.startPolling();
        }
      },
      error: (error) => {
        this.errorMessage = 'Failed to load analysis';
        this.loading = false;
      }
    });
  }

  loadReport(): void {
    this.analysisService.getReport(this.analysisId).subscribe({
      next: (report) => {
        this.report = report;
        // NEW: Check if we should show evolution
        this.showEvolution = !!(report.version && report.version > 1 && report.evolutionAnalysis);
      },
      error: (error) => {
        this.errorMessage = 'Failed to load report';
      }
    });
  }

  startPolling(): void {
    this.pollSubscription = interval(5000).subscribe(() => {
      this.analysisService.getById(this.analysisId).subscribe({
        next: (analysis) => {
          this.analysis = analysis;

          if (analysis.status === AnalysisStatus.COMPLETED) {
            this.stopPolling();
            this.loadReport();
          } else if (analysis.status === AnalysisStatus.FAILED) {
            this.stopPolling();
          }
        }
      });
    });
  }

  stopPolling(): void {
    if (this.pollSubscription) {
      this.pollSubscription.unsubscribe();
    }
  }

  backToProject(): void {
    if (this.analysis) {
      this.router.navigate(['/projects', this.analysis.projectId]);
    }
  }

  getSeverityClass(severity: string): string {
    switch (severity.toUpperCase()) {
      case 'HIGH':
        return 'severity-high';
      case 'MEDIUM':
        return 'severity-medium';
      case 'LOW':
        return 'severity-low';
      default:
        return '';
    }
  }

  getPriorityClass(priority: string): string {
    switch (priority.toUpperCase()) {
      case 'HIGH':
        return 'priority-high';
      case 'MEDIUM':
        return 'priority-medium';
      case 'LOW':
        return 'priority-low';
      default:
        return '';
    }
  }

  // NEW: Helper methods for Phase 1 features

  getTrendClass(trend: string): string {
    switch (trend.toUpperCase()) {
      case 'IMPROVING':
        return 'trend-improving';
      case 'DEGRADING':
        return 'trend-degrading';
      case 'STABLE':
        return 'trend-stable';
      default:
        return 'trend-unknown';
    }
  }

  getTrendIcon(trend: string): string {
    switch (trend.toUpperCase()) {
      case 'IMPROVING':
        return '📈';
      case 'DEGRADING':
        return '📉';
      case 'STABLE':
        return '➡️';
      default:
        return '❓';
    }
  }

  getConfidenceLabel(score: number): string {
    if (score >= 0.9) return 'Very High';
    if (score >= 0.8) return 'High';
    if (score >= 0.7) return 'Medium';
    if (score >= 0.6) return 'Low';
    return 'Very Low';
  }

  getConfidenceClass(score: number): string {
    if (score >= 0.8) return 'confidence-high';
    if (score >= 0.6) return 'confidence-medium';
    return 'confidence-low';
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0
    }).format(amount);
  }

  getCostTierClass(tier: string): string {
    switch (tier.toUpperCase()) {
      case 'LOW':
        return 'cost-low';
      case 'MEDIUM':
        return 'cost-medium';
      case 'HIGH':
        return 'cost-high';
      default:
        return '';
    }
  }

  groupCostsByType(costs: ComponentCost[]): Map<string, ComponentCost[]> {
    const grouped = new Map<string, ComponentCost[]>();
    costs.forEach(cost => {
      const type = cost.serviceType;
      if (!grouped.has(type)) {
        grouped.set(type, []);
      }
      grouped.get(type)!.push(cost);
    });
    return grouped;
  }

  getTotalCostByType(costs: ComponentCost[], type: string): number {
    return costs
      .filter(c => c.serviceType === type)
      .reduce((sum, c) => sum + c.estimatedMonthlyCost, 0);
  }

  getImprovementScoreClass(score: number): string {
    if (score >= 75) return 'score-excellent';
    if (score >= 50) return 'score-good';
    if (score >= 25) return 'score-fair';
    return 'score-poor';
  }
}