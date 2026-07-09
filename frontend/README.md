# Educore Frontend (Angular 20)

Role-based Angular frontend for the Educore backend, structured for
**Student**, **Instructor**, **ExamCoordinator**, and **Registrar** roles.

## Setup

```bash
npm install
ng serve
```

Backend API base URL is set in:
- `src/environments/environment.development.ts` → `http://localhost:8080/api`
- `src/environments/environment.production.ts`

## Styling

Bootstrap 5 is included via `angular.json` (`node_modules/bootstrap/dist/css/bootstrap.min.css`
and `bootstrap.bundle.min.js`) and listed in `package.json`. Run `npm install`
to pull it in — no manual import needed in `styles.scss`.

## Structure

- `core/` — singleton services, guards (`auth.guard.ts`, `role.guard.ts`),
  interceptors (JWT attach + error handling), and app layout (navbar/sidebar).
- `shared/` — reusable components, pipes, directives, validators.
- `features/auth/` — login/register, talks to `UserController` / `RoleIdentityController`.
- `features/student/` — courses, assignments, exams, results (`StudentController`, `ResultController`).
- `features/instructor/` — courses, assignments, grading (`InstructorController`).
- `features/exam-coordinator/` — exams, exam rooms, results (`ExamCoordinatorController`, `ResultController`).
- `features/registrar/` — courses, enrollments, academics, user management (`RegistrarController`, `CourseController`).

Folders containing only a `.gitkeep` file are placeholders — add your
components/services there as you build out each feature.

## Adding a component

```bash
ng generate component features/student/dashboard --standalone
ng generate service features/student/services/student-api
```

## Implemented so far

- **Login** (`features/auth/login`) and **Registration** (`features/auth/register`) —
  fully wired to the backend:
  - `POST /api/v1/user/login` → `AuthApiService.login()`
  - `POST /api/v1/user/register` → `AuthApiService.register()`
- JWT is stored via `TokenStorageService` (localStorage) and auto-attached to every
  outgoing request by `auth.interceptor.ts`.
- `error.interceptor.ts` normalizes the backend's different error shapes (plain string,
  field-validation map, or structured 401/403 body) into one message shown in the UI.
- `authGuard` protects `/dashboard`, a placeholder landing page (`main-layout` +
  `navbar`) that shows the logged-in user's name/role and a logout button.
- Form validation on Register mirrors the backend's exact rules (name pattern,
  password complexity, 10-digit phone, role enum: `STUDENT`, `INSTRUCTOR`,
  `REGISTRAR`, `EXAM_COORDINATOR`).
- Backend runs on `http://localhost:9098/api/v1` (see `application.properties`) —
  already set in `environment.development.ts`.

## Not yet built

Role-specific dashboards (Student/Instructor/ExamCoordinator/Registrar features) are
still empty placeholders — `student.routes.ts`, `instructor.routes.ts`, etc. all
currently export `[]`.
