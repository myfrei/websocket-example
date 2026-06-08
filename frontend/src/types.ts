export type Role = 'USER' | 'MANAGER';

export interface AuthUser {
  username: string;
  fullName: string;
  phone: string;
  role: Role;
}

export interface AuthResponse extends AuthUser {
  token: string;
}

export interface RegisterForm {
  username: string;
  password: string;
  fullName: string;
  phone: string;
}

export interface RegisterResponse {
  username: string;
  status: string;
  message: string;
}

export interface Account {
  username: string;
  fullName: string;
  phone: string;
  accountNumber: string;
  currency: string;
  balance: number;
}

export type TxType = 'CREDIT' | 'DEBIT';
export type TxStatus = 'COMPLETED' | 'FAILED';

export interface Transaction {
  id: string;
  type: TxType;
  amount: number;
  currency: string;
  counterparty: string;
  description?: string | null;
  status: TxStatus;
  createdAt: string;
}

export interface TransferAccepted {
  reference: string;
  status: string;
}

export interface ChatMessage {
  id?: string;
  chatId?: string;
  chatUser?: string;
  fromUsername: string;
  fromRole: string;
  text: string;
  createdAt: string;
}

export interface ChatSummary {
  chatId: string;
  user: string;
  status: string;
  createdAt: string;
  updatedAt: string;
}

export interface PendingUser {
  username: string;
  fullName: string;
  phone: string;
  role: string;
  status: string;
}
