import { Component, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { switchMap } from 'rxjs';
import { ApplicationService } from '../../../services/application.service';
import { ApplicationRequest } from '../../../models/application.model';
import { DocumentService } from '../../../services/document.service';
import { PaymentService } from '../../../services/payment.service';

declare var Razorpay: any;

@Component({
  selector: 'app-apply-wizard',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './apply-wizard.component.html',
  styleUrl: './apply-wizard.component.css'
})
export class ApplyWizardComponent {
  currentStep = 1;
  loading = false;
  submitted = false;
  showSuccessPopup = false;
  errorMessage = '';
  selectedFiles: { [key: string]: File } = {};

  // Dynamic Calculation Properties
  interestRate = 11;
  monthlyEmi = 0;
  submittedApplicationId?: number;
  readonly statusLifecycle = [
    'Draft',
    'Submitted',
    'Docs Pending',
    'Docs Verified',
    'Under Review',
    'Approved/Rejected',
    'Closed'
  ];
  form = this.fb.group({
    firstName: ['', Validators.required],
    lastName: ['', Validators.required],
    phone: ['', Validators.required],
    address: ['', Validators.required],
    employerName: [''],
    employmentType: ['SALARIED', Validators.required],
    monthlyIncome: [0, Validators.min(0)],
    loanAmount: [1000, [Validators.required, Validators.min(1000)]],
    purpose: ['PERSONAL'],
    tenureMonths: [12, Validators.required]
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly applicationService: ApplicationService,
    private readonly docService: DocumentService,
    private readonly paymentService: PaymentService,
    private readonly router: Router
  ) {
    this.setupEmiCalculation();
  }

  private setupEmiCalculation(): void {
    this.form.valueChanges.subscribe(() => {
      this.calculateEmi();
    });
    this.calculateEmi(); // Initial calculation
  }

  private calculateEmi(): void {
    const amount = Number(this.form.value.loanAmount) || 0;
    const months = Number(this.form.value.tenureMonths) || 1;

    // Determine Interest Rate
    if (amount < 1000000) this.interestRate = 11;
    else if (amount < 2000000) this.interestRate = 9;
    else if (amount < 3000000) this.interestRate = 7;
    else this.interestRate = 6;

    // EMI Calculation
    if (amount > 0 && months > 0) {
      const r = this.interestRate / 12 / 100;
      const n = months;
      this.monthlyEmi = (amount * r * Math.pow(1 + r, n)) / (Math.pow(1 + r, n) - 1);
    } else {
      this.monthlyEmi = 0;
    }
  }

  nextStep(): void {
    if (this.currentStep < 5) {
      this.currentStep++;
    }
  }

  prevStep(): void {
    if (this.currentStep > 1) {
      this.currentStep--;
    }
  }

  goToStep(step: number): void {
    this.currentStep = step;
  }

  onFileSelected(event: Event, docType: string): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.selectedFiles[docType] = input.files[0];
    }
  }

  submit(): void {
    if (this.form.invalid || this.loading || this.submitted) return;
    this.loading = true;

    // Create and then submit application, then upload documents
    this.applicationService
      .create(this.toPayload())
      .subscribe({
        next: (app) => {
          this.submittedApplicationId = app.id;
          this.uploadAllDocuments(app.id);
        },
        error: (err) => {
          this.loading = false;
          this.errorMessage = 'Failed to create application. Please check your connection.';
          console.error(err);
        }
      });
  }

  private uploadAllDocuments(appId: number): void {
    const fileKeys = Object.keys(this.selectedFiles);

    // Step 1: Upload documents if any
    if (fileKeys.length === 0) {
      this.initiatePayment(appId);
      return;
    }

    let completed = 0;
    fileKeys.forEach(type => {
      this.docService.upload(this.selectedFiles[type], appId, type).subscribe({
        complete: () => {
          completed++;
          if (completed === fileKeys.length) {
            this.initiatePayment(appId);
          }
        }
      });
    });
  }

  private initiatePayment(appId: number): void {
    this.paymentService.createOrder(appId).subscribe({
      next: (payment) => {
        const options = {
          key: 'rzp_test_Sk0s5FcAaqhzCy', // Actual Key ID
          amount: payment.amount * 100,
          currency: payment.currency,
          name: 'FinFlow',
          description: 'Loan Application Fee',
          order_id: payment.razorpayOrderId,
          handler: (response: any) => {
            this.verifyPayment(response);
          },
          prefill: {
            name: `${this.form.value.firstName} ${this.form.value.lastName}`,
            contact: this.form.value.phone
          },
          theme: {
            color: '#00c6ff'
          },
          modal: {
            ondismiss: () => {
              this.loading = false;
              alert('Payment cancelled. Please try again to submit.');
            }
          }
        };

        const rzp = new Razorpay(options);
        rzp.open();
      },
      error: (err) => {
        console.error('Payment initiation failed', err);
        this.loading = false;
        this.errorMessage = 'Failed to initiate payment. Please try again.';
      }
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
        // FINAL STEP: Mark as submitted ONLY after successful payment
        this.applicationService.submit(this.submittedApplicationId!).subscribe({
          next: () => this.finishSubmission(),
          error: () => this.finishSubmission() // Even if submit fails, payment is done
        });
      },
      error: (err) => {
        console.error('Payment verification failed', err);
        this.errorMessage = 'Payment verification failed. Please contact support.';
        this.loading = false;
      }
    });
  }

  private finishSubmission(): void {
    this.loading = false;
    this.submitted = true;
    this.showSuccessPopup = true;
  }

  // @HostListener('mousemove', ['$event'])
  // onMouseMove(e: MouseEvent) {
  //   const interactives = document.getElementsByClassName('interactive-luxe');
  //   for (const el of Array.from(interactives) as HTMLElement[]) {
  //     const rect = el.getBoundingClientRect();
  //     const x = e.clientX - rect.left;
  //     const y = e.clientY - rect.top;

  //     el.style.setProperty('--m-x', `${x}px`);
  //     el.style.setProperty('--m-y', `${y}px`);
  //   }
  // }

  continueToDashboard(): void {
    this.showSuccessPopup = false;
    this.router.navigate(['/applicant/dashboard']);
  }

  private toPayload(): ApplicationRequest {
    const raw = this.form.getRawValue();
    return {
      loanAmount: Number(raw.loanAmount),
      purpose: raw.purpose || '',
      tenureMonths: Number(raw.tenureMonths),
      firstName: raw.firstName || '',
      lastName: raw.lastName || '',
      phone: raw.phone || '',
      address: raw.address || '',
      employerName: raw.employerName || '',
      employmentType: raw.employmentType || '',
      monthlyIncome: Number(raw.monthlyIncome)
    };
  }
}
