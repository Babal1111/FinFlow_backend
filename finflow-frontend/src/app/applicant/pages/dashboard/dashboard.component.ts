import { CommonModule } from '@angular/common';
import { Component, HostListener } from '@angular/core';
import { RouterLink, Router } from '@angular/router';
import { ApplicationResponse, ApplicationStatus } from '../../../models/application.model';
import { ApplicationService } from '../../../services/application.service';
import { PaymentService } from '../../../services/payment.service';
import { AuthService } from '../../../services/auth.service';

declare var Razorpay: any;

@Component({
  selector: 'app-applicant-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class ApplicantDashboardComponent {
  loading = true;
  applications: ApplicationResponse[] = [];
  firstName = 'User';

  constructor(
    private readonly applicationService: ApplicationService,
    private readonly paymentService: PaymentService,
    private readonly router: Router,
    private readonly authService: AuthService
  ) {
    this.loadApplications();
  }

  initiatePayment(app: any): void {
    this.paymentService.createOrder(app.id).subscribe({
      next: (payment) => {
        const options = {
          key: 'rzp_test_Sk0s5FcAaqhzCy',
          amount: payment.amount * 100,
          currency: payment.currency,
          name: 'FinFlow',
          description: 'Loan Application Fee',
          order_id: payment.razorpayOrderId,
          handler: (response: any) => {
            this.verifyPayment(response, app.id);
          },
          prefill: {
            name: this.firstName,
            contact: ''
          },
          theme: {
            color: '#00c6ff'
          }
        };
        const rzp = new Razorpay(options);
        rzp.open();
      },
      error: (err) => alert('Failed to initiate payment: ' + err.message)
    });
  }

  private verifyPayment(razorpayResponse: any, appId: number): void {
    const verificationData = {
      razorpay_order_id: razorpayResponse.razorpay_order_id,
      razorpay_payment_id: razorpayResponse.razorpay_payment_id,
      razorpay_signature: razorpayResponse.razorpay_signature
    };

    this.paymentService.verifyPayment(verificationData).subscribe({
      next: () => {
        this.applicationService.submit(appId).subscribe({
          next: () => {
            alert('Payment & Submission Successful!');
            this.loadApplications();
          }
        });
      },
      error: (err) => alert('Payment verification failed!')
    });
  }

  private loadApplications(): void {
    this.applicationService.getMyApplications().subscribe({
      next: (data) => {
        this.applications = data || [];
        this.loading = false;
        if (this.applications.length > 0 && this.applications[0].firstName) {
          this.firstName = this.applications[0].firstName;
        }
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  get userEmail(): string {
    return this.authService.getEmailFromToken() || '';
  }

  get totalAmount(): number {
    return this.applications.reduce((acc, app) => acc + (app.loanAmount || 0), 0);
  }

  get approvedCount(): number {
    return this.applications.filter(a => a.status === 'APPROVED' || a.status === 'CLOSED').length;
  }

  get pendingCount(): number {
    return this.applications.filter(a =>
      a.status !== 'APPROVED' && a.status !== 'REJECTED' && a.status !== 'CLOSED'
    ).length;
  }

  get rejectedCount(): number {
    return this.applications.filter(a => a.status === 'REJECTED').length;
  }
}
