import { ApplicationResponse, ApplicationStatus } from '../../../models/application.model';
import { CommonModule } from '@angular/common';
import { Component, HostListener } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AdminService } from '../../../services/admin.service';
import { ApplicationService } from '../../../services/application.service';
import { DocumentService } from '../../../services/document.service';
import { DocumentResponse, DocumentStatus } from '../../../models/document.model';

@Component({
  selector: 'app-review',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './review.component.html',
  styleUrl: './review.component.css'
})
export class ReviewComponent {
  readonly applicationId: number;
  documents: DocumentResponse[] = [];
  loadingDocs = true;
  loadingApp = true;
  submitting = false;
  application: ApplicationResponse | null = null;

  form = this.fb.group({
    decision: ['APPROVED', Validators.required],
    remarks: ['']
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly adminService: AdminService,
    private readonly docService: DocumentService,
    private readonly appService: ApplicationService,
    private readonly router: Router,
    route: ActivatedRoute
  ) {
    this.applicationId = Number(route.snapshot.paramMap.get('id'));
    this.loadApplication();
    this.loadDocuments();
  }

  submit(): void {
    if (this.form.invalid || this.submitting) return;
    this.submitting = true;
    this.adminService.makeDecision(this.applicationId, this.form.getRawValue() as { decision: string; remarks: string }).subscribe({
      next: () => this.router.navigate(['/admin/dashboard']),
      error: () => {
        this.submitting = false;
        // Even if there's a 400 error (like double submit), if it's already approved, we should probably go back
        this.router.navigate(['/admin/dashboard']);
      }
    });
  }

  private loadApplication(): void {
    this.loadingApp = true;
    this.appService.getById(this.applicationId).subscribe({
      next: (app) => {
        this.application = app;
        this.loadingApp = false;
      },
      error: () => (this.loadingApp = false)
    });
  }

  verifyDocument(id: number, approved: boolean): void {
    this.adminService.verifyDocument(id, approved).subscribe({
      next: () => this.loadDocuments()
    });
  }

  startReview(): void {
    if (!this.application) return;
    this.recursiveTransition(this.application.status);
  }

  private recursiveTransition(currentStatus: string): void {
    const targetStatus = 'UNDER_REVIEW';
    if (currentStatus === targetStatus) {
      this.loadApplication();
      return;
    }

    // Define the next step based on the backend state machine
    let nextStep = '';
    if (currentStatus === 'SUBMITTED') nextStep = 'DOCS_PENDING';
    else if (currentStatus === 'DOCS_PENDING') nextStep = 'DOCS_VERIFIED';
    else if (currentStatus === 'DOCS_VERIFIED') nextStep = 'UNDER_REVIEW';

    if (nextStep) {
      this.appService.updateStatus(this.applicationId, nextStep).subscribe({
        next: () => {
          this.recursiveTransition(nextStep);
        },
        error: (err) => {
          // SELF-HEALING: If we get a 400 error (Invalid transition), 
          // it means the backend might have already moved forward.
          // We refresh the application and try again from the NEW status.
          console.warn(`Transition to ${nextStep} failed, refreshing state...`, err);
          this.appService.getById(this.applicationId).subscribe(app => {
            this.application = app;
            if (this.application.status !== currentStatus) {
              this.recursiveTransition(this.application.status);
            } else {
              // If it's truly stuck, just refresh the full UI
              this.loadApplication();
            }
          });
        }
      });
    }
  }

  viewDocument(id: number): void {
    this.docService.getDocument(id).subscribe({
      next: (blob: Blob) => {
        const objectUrl = URL.createObjectURL(blob);
        const newTab = window.open(objectUrl, '_blank', 'noopener');

        // Revoke the object URL after the tab has had time to load,
        // so memory is freed without breaking the opened tab
        if (newTab) {
          newTab.addEventListener('load', () => URL.revokeObjectURL(objectUrl));
        } else {
          // Fallback if popup was blocked — create a temporary anchor link
          const anchor = document.createElement('a');
          anchor.href = objectUrl;
          anchor.target = '_blank';
          anchor.click();
          setTimeout(() => URL.revokeObjectURL(objectUrl), 5000);
        }
      },
      error: (err: unknown) => {
        console.error('Failed to load document', err);
        alert('Could not open document. Please try again.');
      }
    });
  }

  private loadDocuments(): void {
    this.loadingDocs = true;
    this.docService.getByApplication(this.applicationId).subscribe({
      next: (docs: DocumentResponse[]) => {
        this.documents = docs;
        this.loadingDocs = false;
      },
      error: () => (this.loadingDocs = false)
    });
  }

  getStatusBadgeClass(status: ApplicationStatus | undefined): string {
    if (!status) return 'badge-draft';
    const map: Record<string, string> = {
      DRAFT: 'badge-draft', SUBMITTED: 'badge-submitted', DOCS_PENDING: 'badge-docs-pending',
      DOCS_VERIFIED: 'badge-docs-verified', UNDER_REVIEW: 'badge-under-review',
      APPROVED: 'badge-approved', REJECTED: 'badge-rejected', CLOSED: 'badge-closed'
    };
    return map[status] || 'badge-draft';
  }

  @HostListener('mousemove', ['$event'])
  onMouseMove(e: MouseEvent) {
    const interactives = document.getElementsByClassName('interactive-luxe');
    for (const el of Array.from(interactives) as HTMLElement[]) {
      const rect = el.getBoundingClientRect();
      const x = e.clientX - rect.left;
      const y = e.clientY - rect.top;

      el.style.setProperty('--m-x', `${x}px`);
      el.style.setProperty('--m-y', `${y}px`);
    }
  }
}
