export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
  role: string;
  phone: number;
}

export interface RegisterResponse {
  userId: number;
  email: string;
  name: string;
  role: string;
  phone: number;
  status: string;
  createdAt: string;
}
