import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-loading-spinner',
  standalone: true,
  imports: [CommonModule],
  template: `<div class="loading-spinner"></div>`,
  styles: []
})
export class LoadingSpinnerComponent {}