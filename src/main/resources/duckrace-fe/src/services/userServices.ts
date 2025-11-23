import { AppResponse } from "@/interface/app/AppResponse.ts";
import { IGetAccessToken, ILoginResponse } from "@/interface/user/User.ts";
import axiosConfig from "@/utils/axios";

const login = async (data: IGetAccessToken): Promise<AppResponse<ILoginResponse>> => {
  try {
    const response = await axiosConfig.post<AppResponse<ILoginResponse>>("/api/users/login", data);
    return response.data; // Extract the `data` field to match `AppResponse<ILoginResponse>`
  } catch (error) {
    console.log("Error in userServices -> login", error);
    throw error;
  }
};

const userServices = {
  login,
};
export default userServices;
