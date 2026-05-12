import { CommonModule } from '@angular/common';
import { Component, HostListener } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ApplicationService } from '../../../services/application.service';
import { DocumentService } from '../../../services/document.service';
import { DocumentResponse } from '../../../models/document.model';

@Component({
  selector: 'app-documents',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './documents.component.html',
  styleUrl: './documents.component.css'
})
export class DocumentsComponent {
  applicationId = Number(this.route.snapshot.paramMap.get('applicationId'));
  readonly slots = ['AADHAAR', 'PAN_CARD', 'SALARY_SLIP', 'BANK_STATEMENT'];
  uploadedDocs: DocumentResponse[] = [];
  uploading = false;
  submitting = false;
  showSuccessPopup = false;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly documentService: DocumentService,
    private readonly applicationService: ApplicationService
  ) {
    if (this.applicationId) {
      this.loadDocs();
    }
  }

  upload(event: Event, type: string): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file || !this.applicationId) return;
    this.uploading = true;
    this.documentService.upload(file, this.applicationId, type).subscribe({
      next: () => {
        this.uploading = false;
        this.loadDocs();
      },
      error: () => (this.uploading = false)
    });
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

  submitDocuments(): void {
    if (!this.applicationId || this.missingCount > 0 || this.submitting) return;
    this.submitting = true;
    this.applicationService.submitDocs(this.applicationId).subscribe({
      next: () => {
        this.submitting = false;
        this.showSuccessPopup = true;
      },
      error: () => (this.submitting = false)
    });
  }

  continueToDashboard(): void {
    this.router.navigate(['/applicant/dashboard']);
  }
  isUploaded(type: string): boolean {
    return this.uploadedDocs.some((doc) => doc.documentType === type);
  }

  get missingCount(): number {
    return this.slots.filter((slot) => !this.isUploaded(slot)).length;
  }

  get canUploadDocuments(): boolean {
    return !!this.applicationId;
  }

  getDocStatus(type: string): string {
    const doc = this.uploadedDocs.find((d) => d.documentType === type);
    return doc ? doc.status : 'NOT_UPLOADED';
  }

  private loadDocs(): void {
    this.documentService.getByApplication(this.applicationId).subscribe({
      next: (docs) => (this.uploadedDocs = docs)
    });
  }
}
