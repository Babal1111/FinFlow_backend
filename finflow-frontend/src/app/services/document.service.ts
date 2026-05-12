import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { DocumentResponse } from '../models/document.model';

@Injectable({
  providedIn: 'root'
})
export class DocumentService {
  private readonly baseUrl = `${environment.apiUrl}/documents`;

  constructor(private readonly http: HttpClient) {}

  upload(file: File, applicationId: number, documentType: string): Observable<DocumentResponse> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('applicationId', String(applicationId));
    formData.append('documentType', documentType);
    return this.http.post<DocumentResponse>(`${this.baseUrl}/upload`, formData);
  }

  getByApplication(applicationId: number): Observable<DocumentResponse[]> {
    return this.http.get<DocumentResponse[]>(`${this.baseUrl}/application/${applicationId}`);
  }

  verify(id: number, approved: boolean): Observable<DocumentResponse> {
    const params = new HttpParams().set('approved', approved);
    return this.http.put<DocumentResponse>(`${this.baseUrl}/${id}/verify`, null, { params });
  }

  getViewUrl(id: number): string {
    return `${this.baseUrl}/${id}/view`;
  }

  getDocument(id: number): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/${id}/view`, { responseType: 'blob' });
  }
  
}
