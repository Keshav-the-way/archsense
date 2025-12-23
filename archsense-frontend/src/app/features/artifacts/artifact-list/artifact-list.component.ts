import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Artifact } from '../../../core/models/artifact.model';
import { FileSizePipe } from '../../../shared/pipes/file-size.pipe';

@Component({
  selector: 'app-artifact-list',
  standalone: true,
  imports: [CommonModule, FileSizePipe],
  templateUrl: './artifact-list.component.html',
  styleUrls: ['./artifact-list.component.scss']
})
export class ArtifactListComponent {
  @Input() artifacts: Artifact[] = [];
}