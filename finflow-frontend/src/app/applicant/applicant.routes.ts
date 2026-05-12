import { Routes } from '@angular/router';
import { ApplicantDashboardComponent } from './pages/dashboard/dashboard.component';
import { ApplyWizardComponent } from './pages/apply-wizard/apply-wizard.component';
import { DocumentsComponent } from './pages/documents/documents.component';
import { StatusComponent } from './pages/status/status.component';

export const APPLICANT_ROUTES: Routes = [
  { path: 'dashboard', component: ApplicantDashboardComponent },
  { path: 'apply', component: ApplyWizardComponent },
  { path: 'documents/:applicationId', component: DocumentsComponent },
  { path: 'status/:id', component: StatusComponent }
];
