import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Analysis, CreateAnalysisRequest, AnalysisReport } from '../models/analysis.model';

@Injectable({
  providedIn: 'root'
})
export class AnalysisService {
  private readonly baseUrl = `${environment.apiUrl}`;

  constructor(private http: HttpClient) {}

  create(projectId: string, request: CreateAnalysisRequest): Observable<Analysis> {
    return this.http.post<Analysis>(`${this.baseUrl}/projects/${projectId}/analyses`, request);
  }

  listByProject(projectId: string): Observable<Analysis[]> {
    return this.http.get<Analysis[]>(`${this.baseUrl}/projects/${projectId}/analyses`);
  }

  getById(id: string): Observable<Analysis> {
    return this.http.get<Analysis>(`${this.baseUrl}/analyses/${id}`);
  }

  getReport(id: string): Observable<AnalysisReport> {
    return this.http.get<AnalysisReport>(`${this.baseUrl}/analyses/${id}/report`);
  }
}