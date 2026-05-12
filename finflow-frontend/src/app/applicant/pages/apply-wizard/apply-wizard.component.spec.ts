import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ApplyWizardComponent } from './apply-wizard.component';

describe('ApplyWizardComponent', () => {
  let component: ApplyWizardComponent;
  let fixture: ComponentFixture<ApplyWizardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ApplyWizardComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ApplyWizardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
