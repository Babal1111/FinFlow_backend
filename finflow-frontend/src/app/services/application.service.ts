import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  ApplicationRequest,
  ApplicationResponse,
  StatusResponse,
  SubmitResponse
} from '../models/application.model';

@Injectable({
  providedIn: 'root'
})
export class ApplicationService {
  private readonly baseUrl = `${environment.apiUrl}/applications`;

  constructor(private readonly http: HttpClient) {}

  create(payload: ApplicationRequest): Observable<ApplicationResponse> {
    return this.http.post<ApplicationResponse>(this.baseUrl, payload);
  }

  update(id: number, payload: ApplicationRequest): Observable<ApplicationResponse> {
    return this.http.put<ApplicationResponse>(`${this.baseUrl}/${id}`, payload);
  }

  submit(id: number): Observable<SubmitResponse> {
    return this.http.post<SubmitResponse>(`${this.baseUrl}/${id}/submit`, {});
  }

  submitDocs(id: number): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/${id}/submit-docs`, {});
  }

  getMyApplications(): Observable<ApplicationResponse[]> {
    return this.http.get<ApplicationResponse[]>(`${this.baseUrl}/my`);
  }

  getAllApplications(): Observable<ApplicationResponse[]> {
    return this.http.get<ApplicationResponse[]>(`${this.baseUrl}/all`);
  }

  getStatus(id: number): Observable<StatusResponse> {
    return this.http.get<StatusResponse>(`${this.baseUrl}/${id}/status`);
  }

  updateStatus(id: number, status: string): Observable<void> {
    const params = new HttpParams().set('status', status);
    return this.http.put<void>(`${this.baseUrl}/${id}/status`, null, { params });
  }

  getById(id: number): Observable<ApplicationResponse> {
    return this.http.get<ApplicationResponse>(`${this.baseUrl}/${id}`);
  }
}
