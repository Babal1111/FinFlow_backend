import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { DecisionRequest, DecisionResponse, ReportsResponse } from '../models/admin.model';
import { ApplicationResponse } from '../models/application.model';
import { DocumentResponse } from '../models/document.model';

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private readonly baseUrl = `${environment.apiUrl}/admin`;

  constructor(private readonly http: HttpClient) { }

  // getApplications(): Observable<ApplicationResponse[]> {
  //   return this.http.get<ApplicationResponse[]>(`${this.baseUrl}/applications`);
  // }

  // page aur size as parameters pass karo
getApplications(page: number, size: number): Observable<any> {
  return this.http.get(`${this.baseUrl}/applications/all?page=${page}&size=${size}`);
}


  makeDecision(id: number, payload: DecisionRequest): Observable<DecisionResponse> {
    return this.http.post<DecisionResponse>(`${this.baseUrl}/applications/${id}/decision`, payload);
  }

  verifyDocument(id: number, approved: boolean): Observable<DocumentResponse> {
    const params = new HttpParams().set('approved', approved);
    return this.http.put<DocumentResponse>(`${this.baseUrl}/documents/${id}/verify`, null, { params });
  }

  getReports(): Observable<ReportsResponse> {
    return this.http.get<ReportsResponse>(`${this.baseUrl}/reports`);
  }

  getUsers(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/users`);
  }
}
