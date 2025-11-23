export interface IDepositTokenResponse {
  transaction: unknown;
  currentWallet: number;
}

export interface IWithdrawTokenResponse {
  transaction: unknown;
  currentWallet: number;
}

export interface IDepositTokenData {
  amount: number;
}

export interface IWithdrawTokenData {
  amount: number;
}

export interface ITransactionData {
  id: string;
  userId: string;
  amount: number;
  type: string;
  isIncrease: boolean;
  note: string;
  createdAt: string;
}
