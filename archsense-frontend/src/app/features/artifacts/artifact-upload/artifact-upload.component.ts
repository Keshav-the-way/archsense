import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { HeaderComponent } from '../../../shared/components/header/header.component';
import { ArtifactService } from '../../../core/services/artifact.service';

@Component({
  selector: 'app-artifact-upload',
  standalone: true,
  imports: [CommonModule, HeaderComponent],
  templateUrl: './artifact-upload.component.html',
  styleUrls: ['./artifact-upload.component.scss']
})
export class ArtifactUploadComponent implements OnInit {
  projectId!: string;
  selectedFile: File | null = null;
  uploading = false;
  errorMessage = '';
  dragOver = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private artifactService: ArtifactService
  ) {}

  ngOnInit(): void {
    this.projectId = this.route.snapshot.paramMap.get('id')!;
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.selectedFile = input.files[0];
      this.errorMessage = '';
    }
  }

  onDragOver(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.dragOver = true;
  }

  onDragLeave(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.dragOver = false;
  }

  onDrop(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.dragOver = false;

    if (event.dataTransfer?.files && event.dataTransfer.files.length > 0) {
      this.selectedFile = event.dataTransfer.files[0];
      this.errorMessage = '';
    }
  }

  upload(): void {
    if (!this.selectedFile) {
      this.errorMessage = 'Please select a file';
      return;
    }

    const maxSize = 50 * 1024 * 1024; // 50MB
    if (this.selectedFile.size > maxSize) {
      this.errorMessage = 'File size exceeds 50MB limit';
      return;
    }

    this.uploading = true;
    this.errorMessage = '';

    this.artifactService.upload(this.projectId, this.selectedFile).subscribe({
      next: () => {
        this.router.navigate(['/projects', this.projectId]);
      },
      error: (error) => {
        this.uploading = false;
        this.errorMessage = error.error?.message || 'Failed to upload file';
      }
    });
  }

  cancel(): void {
    this.router.navigate(['/projects', this.projectId]);
  }

  formatFileSize(bytes: number): string {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i];
  }
}