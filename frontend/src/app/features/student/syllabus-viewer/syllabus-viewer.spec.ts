import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SyllabusViewer } from './syllabus-viewer';

describe('SyllabusViewer', () => {
  let component: SyllabusViewer;
  let fixture: ComponentFixture<SyllabusViewer>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SyllabusViewer]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SyllabusViewer);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
