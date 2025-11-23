export interface AppResponse<T> {
  statusCode: number;
  isSuccess: boolean;
  errorMessage?: string;
  message?: string;
  data?: T;
}
