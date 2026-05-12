import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class PaymentService {
  private baseUrl = 'http://localhost:8080/gateway/payments';

  constructor(private http: HttpClient) {}

  createOrder(applicationId: number): Observable<any> {
    return this.http.post(`${this.baseUrl}/create-order?applicationId=${applicationId}`, {});
  }

  verifyPayment(paymentData: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/verify`, paymentData);
  }

  getPaymentStatus(applicationId: number): Observable<any> {
    return this.http.get(`${this.baseUrl}/application/${applicationId}`);
  }
}
