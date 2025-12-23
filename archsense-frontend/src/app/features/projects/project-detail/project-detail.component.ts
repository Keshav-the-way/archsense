import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { HeaderComponent } from '../../../shared/components/header/header.component';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { FileSizePipe } from '../../../shared/pipes/file-size.pipe';
import { ProjectService } from '../../../core/services/project.service';
import { ArtifactService } from '../../../core/services/artifact.service';
import { AnalysisService } from '../../../core/services/analysis.service';
import { Project } from '../../../core/models/project.model';
import { Artifact } from '../../../core/models/artifact.model';
import { Analysis, AnalysisStatus } from '../../../core/models/analysis.model';

@Component({
  selector: 'app-project-detail',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    HeaderComponent,
    LoadingSpinnerComponent,
    FileSizePipe
  ],
  templateUrl: './project-detail.component.html',
  styleUrls: ['./project-detail.component.scss']
})
export class ProjectDetailComponent implements OnInit {
  projectId!: string;
  project: Project | null = null;
  artifacts: Artifact[] = [];
  analyses: Analysis[] = [];
  loading = true;
  errorMessage = '';
  activeTab: 'artifacts' | 'analyses' = 'artifacts';

  AnalysisStatus = AnalysisStatus;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private projectService: ProjectService,
    private artifactService: ArtifactService,
    private analysisService: AnalysisService
  ) {}

  ngOnInit(): void {
    this.projectId = this.route.snapshot.paramMap.get('id')!;
    this.loadProject();
    this.loadArtifacts();
    this.loadAnalyses();
  }

  loadProject(): void {
    this.projectService.getById(this.projectId).subscribe({
      next: (project) => {
        this.project = project;
        this.loading = false;
      },
      error: (error) => {
        this.errorMessage = 'Failed to load project';
        this.loading = false;
      }
    });
  }

  loadArtifacts(): void {
    this.artifactService.listByProject(this.projectId).subscribe({
      next: (artifacts) => {
        this.artifacts = artifacts;
      },
      error: (error) => {
        console.error('Failed to load artifacts', error);
      }
    });
  }

  loadAnalyses(): void {
    this.analysisService.listByProject(this.projectId).subscribe({
      next: (analyses) => {
        this.analyses = analyses;
      },
      error: (error) => {
        console.error('Failed to load analyses', error);
      }
    });
  }

  uploadArtifact(): void {
    this.router.navigate(['/projects', this.projectId, 'upload']);
  }

  triggerAnalysis(): void {
    if (this.artifacts.length === 0) {
      alert('Please upload at least one artifact first');
      return;
    }
    this.router.navigate(['/projects', this.projectId, 'analyze']);
  }

  viewAnalysis(analysisId: string): void {
    this.router.navigate(['/analyses', analysisId]);
  }

  deleteProject(): void {
    if (!confirm('Are you sure you want to delete this project? This action cannot be undone.')) {
      return;
    }

    this.projectService.delete(this.projectId).subscribe({
      next: () => {
        this.router.navigate(['/dashboard']);
      },
      error: (error) => {
        alert('Failed to delete project');
      }
    });
  }

  getStatusBadgeClass(status: AnalysisStatus): string {
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