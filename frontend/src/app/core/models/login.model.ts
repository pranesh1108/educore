export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  userId: number;
  email: string;
  userName: string;
  role: string;
  phone: number;
  token: string;
}
