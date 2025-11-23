export interface User {
  id?: string;
  playerName?: string;
  userName: string;
  avatar?: string;
  email?: string;
  wallet?: number;
  isConnected?: boolean;
}

export interface IUserHashInfo {
  hashData: string;
}

export interface IGetAccessToken {
  userData: User;
  hashData: string;
}

export interface IAccessToken {
  token: string;
  expiresAt: Date;
  expiresAtUtc: string;
}
export interface ILoginResponse {
  userInfor: User;
  accessToken: IAccessToken;
}
