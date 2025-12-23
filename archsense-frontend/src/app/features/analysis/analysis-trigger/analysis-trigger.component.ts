import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { HeaderComponent } from '../../../shared/components/header/header.component';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { ArtifactService } from '../../../core/services/artifact.service';
import { AnalysisService } from '../../../core/services/analysis.service';
import { Artifact } from '../../../core/models/artifact.model';

@Component({
  selector: 'app-analysis-trigger',
  standalone: true,
  imports: [CommonModule, HeaderComponent, LoadingSpinnerComponent],
  templateUrl: './analysis-trigger.component.html',
  styleUrls: ['./analysis-trigger.component.scss']
})
export class AnalysisTriggerComponent implements OnInit {
  projectId!: string;
  artifacts: Artifact[] = [];
  selectedArtifactIds: Set<string> = new Set();
  loading = true;
  submitting = false;
  errorMessage = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private artifactService: ArtifactService,
    private analysisService: AnalysisService
  ) {}

  ngOnInit(): void {
    this.projectId = this.route.snapshot.paramMap.get('id')!;
    this.loadArtifacts();
  }

  loadArtifacts(): void {
    this.artifactService.listByProject(this.projectId).subscribe({
      next: (artifacts) => {
        this.artifacts = artifacts;
        // Select all artifacts by default
        artifacts.forEach(a => this.selectedArtifactIds.add(a.id));
        this.loading = false;
      },
      error: (error) => {
        this.errorMessage = 'Failed to load artifacts';
        this.loading = false;
      }
    });
  }

  toggleArtifact(artifactId: string): void {
    if (this.selectedArtifactIds.has(artifactId)) {
      this.selectedArtifactIds.delete(artifactId);
    } else {
      this.selectedArtifactIds.add(artifactId);
    }
  }

  isSelected(artifactId: string): boolean {
    return this.selectedArtifactIds.has(artifactId);
  }

  startAnalysis(): void {
    if (this.selectedArtifactIds.size === 0) {
      this.errorMessage = 'Please select at least one artifact';
      return;
    }

    this.submitting = true;
    this.errorMessage = '';

    const request = {
      artifactIds: Array.from(this.selectedArtifactIds)
    };

    this.analysisService.create(this.projectId, request).subscribe({
      next: (analysis) => {
        this.router.navigate(['/analyses', analysis.id]);
      },
      error: (error) => {
        this.submitting = false;
        this.errorMessage = error.error?.message || 'Failed to start analysis';
      }
    });
  }

  cancel(): void {
    this.router.navigate(['/projects', this.projectId]);
  }
}