import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Analysis, AnalysisStatus } from '../../../core/models/analysis.model';

@Component({
  selector: 'app-analysis-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './analysis-list.component.html',
  styleUrls: ['./analysis-list.component.scss']
})
export class AnalysisListComponent {
  @Input() analyses: Analysis[] = [];
  @Input() projectId!: string;

  AnalysisStatus = AnalysisStatus;

  constructor(private router: Router) {}

  viewAnalysis(analysisId: string): void {
    this.router.navigate(['/analyses', analysisId]);
  }

  getStatusClass(status: AnalysisStatus): string {
    switch (status) {
      case AnalysisStatus.COMPLETED:
        return 'badge-success';
      case AnalysisStatus.IN_PROGRESS:
        return 'badge-info';
      case AnalysisStatus.PENDING:
        return 'badge-warning';
      case AnalysisStatus.FAILED:
        return 'badge-danger';
      default:
        return 'badge-secondary';
    }
  }
}