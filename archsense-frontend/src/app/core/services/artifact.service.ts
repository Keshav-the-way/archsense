import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Artifact } from '../models/artifact.model';

@Injectable({
  providedIn: 'root'
})
export class ArtifactService {
  private readonly baseUrl = `${environment.apiUrl}`;

  constructor(private http: HttpClient) {}

  upload(projectId: string, file: File): Observable<Artifact> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<Artifact>(`${this.baseUrl}/projects/${projectId}/artifacts`, formData);
  }

  listByProject(projectId: string): Observable<Artifact[]> {
    return this.http.get<Artifact[]>(`${this.baseUrl}/projects/${projectId}/artifacts`);
  }

  getById(id: string): Observable<Artifact> {
    return this.http.get<Artifact>(`${this.baseUrl}/artifacts/${id}`);
  }

  download(id: string): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/artifacts/${id}/download`, { responseType: 'blob' });
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/artifacts/${id}`);
  }
}