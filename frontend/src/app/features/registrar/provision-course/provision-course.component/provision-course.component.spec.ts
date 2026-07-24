import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProvisionCourseComponent } from './provision-course.component';

describe('ProvisionCourseComponent', () => {
  let component: ProvisionCourseComponent;
  let fixture: ComponentFixture<ProvisionCourseComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProvisionCourseComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ProvisionCourseComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
