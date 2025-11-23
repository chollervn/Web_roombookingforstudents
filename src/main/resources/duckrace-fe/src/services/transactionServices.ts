import { AppResponse } from "@/interface/app/AppResponse.ts";
import {
  IDepositTokenData,
  IDepositTokenResponse,
  ITransactionData,
  IWithdrawTokenData,
} from "@/interface/transaction/Transaction.ts";
import axiosConfig from "@/utils/axios";

const depositToken = async (data: IDepositTokenData): Promise<AppResponse<IDepositTokenResponse>> => {
  try {
    const response = await axiosConfig.post<AppResponse<IDepositTokenResponse>>("/api/transactions/deposit", data);
    return response.data; // Extract the `data` field to match `AppResponse<ILoginResponse>`
  } catch (error) {
    console.log("Error in transactionService -> login", error);
    throw error;
  }
};

const withdrawToken = async (data: IWithdrawTokenData): Promise<AppResponse<IDepositTokenResponse>> => {
  try {
    const response = await axiosConfig.post<AppResponse<IDepositTokenResponse>>("/api/transactions/withdraw", data);
    return response.data; // Extract the `data` field to match `AppResponse
  } catch (error) {
    console.log("Error in transactionService -> login", error);
    throw error;
  }
};

const getHistoryTransaction = async (): Promise<AppResponse<ITransactionData[]>> => {
  try {
    const response = await axiosConfig.get<AppResponse<ITransactionData[]>>("/api/transactions/history");
    return response.data; // Extract the `data` field to match `AppResponse<ILoginResponse>`
  } catch (error) {
    console.log("Error in transactionService -> login", error);
    throw error;
  }
};

const transactionService = {
  depositToken,
  withdrawToken,
  getHistoryTransaction,
};
export default transactionService;
