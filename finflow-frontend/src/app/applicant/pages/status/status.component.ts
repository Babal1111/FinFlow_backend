import { CommonModule } from '@angular/common';
import { Component, HostListener } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ApplicationService } from '../../../services/application.service';
import { PaymentService } from '../../../services/payment.service';
import { AuthService } from '../../../services/auth.service';

declare var Razorpay: any;
import { ApplicationResponse, ApplicationStatus, StatusResponse } from '../../../models/application.model';
import { DocumentService } from '../../../services/document.service';
import { DocumentResponse } from '../../../models/document.model';

@Component({
  selector: 'app-status',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './status.component.html',
  styleUrl: './status.component.css'
})
export class StatusComponent {
  status?: StatusResponse;
  application?: ApplicationResponse;
  documents: DocumentResponse[] = [];
  loading = true;
  readonly applicationId: number;

  readonly allStatuses: ApplicationStatus[] = [
    'DRAFT', 'SUBMITTED', 'DOCS_PENDING', 'DOCS_VERIFIED', 'UNDER_REVIEW', 'APPROVED'
  ];

  constructor(
    private readonly route: ActivatedRoute,
    private readonly applicationService: ApplicationService,
    private readonly paymentService: PaymentService,
    private readonly docService: DocumentService,
    private readonly authService: AuthService
  ) {
    this.applicationId = Number(route.snapshot.paramMap.get('id'));
    this.applicationService.getStatus(this.applicationId).subscribe({
      next: (res) => {
        this.status = res;
        this.loading = false;
      },
      error: () => (this.loading = false)
    });
    this.docService.getByApplication(this.applicationId).subscribe({
      next: (docs) => {
        // Keeping only the latest document for each type to avoid duplicates
        const uniqueDocs = new Map();
        docs.forEach(doc => uniqueDocs.set(doc.documentType, doc));
        this.documents = Array.from(uniqueDocs.values());
      }
    });
    this.loadApplicationData();
  }

  get userEmail(): string {
    return this.authService.getEmailFromToken() || '';
  }

  getStatusIndex(status: ApplicationStatus): number {
    if (status === 'REJECTED') return -1;
    if (status === 'CLOSED') return this.allStatuses.length;
    return this.allStatuses.indexOf(status);
  }

  isCompleted(step: ApplicationStatus): boolean {
    if (!this.status) return false;
    return this.getStatusIndex(this.status.currentStatus) > this.allStatuses.indexOf(step);
  }

  initiatePayment(): void {
    this.paymentService.createOrder(this.applicationId!).subscribe({
      next: (payment) => {
        const options = {
          key: 'rzp_test_Sk0s5FcAaqhzCy',
          amount: payment.amount * 100,
          currency: payment.currency,
          name: 'FinFlow',
          description: 'Loan Application Fee',
          order_id: payment.razorpayOrderId,
          handler: (response: any) => {
            this.verifyPayment(response);
          },
          prefill: {
            name: this.application?.firstName + ' ' + this.application?.lastName,
            contact: this.application?.phone
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

  private verifyPayment(razorpayResponse: any): void {
    const verificationData = {
      razorpay_order_id: razorpayResponse.razorpay_order_id,
      razorpay_payment_id: razorpayResponse.razorpay_payment_id,
      razorpay_signature: razorpayResponse.razorpay_signature
    };

    this.paymentService.verifyPayment(verificationData).subscribe({
      next: () => {
        this.applicationService.submit(this.applicationId!).subscribe({
          next: () => {
            alert('Payment & Submission Successful!');
            this.loadApplicationData();
          }
        });
      },
      error: (err) => alert('Payment verification failed!')
    });
  }

  private loadApplicationData(): void {
    this.applicationService.getById(this.applicationId).subscribe(app => this.application = app);
  }

  isCurrent(step: ApplicationStatus): boolean {
    if (!this.status) return false;
    return this.status.currentStatus === step;
  }

  getStatusBadgeClass(status: string): string {
    const map: Record<string, string> = {
      DRAFT: 'badge-draft', SUBMITTED: 'badge-submitted', DOCS_PENDING: 'badge-docs-pending',
      DOCS_VERIFIED: 'badge-docs-verified', UNDER_REVIEW: 'badge-under-review',
      APPROVED: 'badge-approved', REJECTED: 'badge-rejected', CLOSED: 'badge-closed',
      PENDING: 'badge-pending', VERIFIED: 'badge-verified'
    };
    return map[status] || 'badge-draft';
  }
}
