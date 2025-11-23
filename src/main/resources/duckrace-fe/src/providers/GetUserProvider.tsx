/* eslint-disable @typescript-eslint/no-explicit-any */
import { IUserHashInfo, User } from "@/interface/user/User.ts";
import userServices from "@/services/userServices.ts";
import useUserStore from "@/stores/userStore.ts";
import { MezonAppEvent, MezonWebViewEvent } from "@/types/webview";
import { setToLocalStorage } from "@/utils/localStorage";
import { Base64 } from "js-base64";
import { useEffect, useState } from "react";
import { toast } from "react-toastify";

interface GetUserProviderProps {
  children: React.ReactNode;
}
const GetUserProvider = ({ children }: GetUserProviderProps) => {
  const [mezonUser, setMezonUser] = useState<User>();
  const [hashData, setHashData] = useState<IUserHashInfo>();
  const { setCurrentUser, setUserHashInfo, setAccessToken } = useUserStore();
  useEffect(() => {
    window.Mezon.WebView?.postEvent("PING" as MezonWebViewEvent, { message: "PING" }, () => {
      console.log("PING");
    });
    window.Mezon.WebView?.onEvent("CURRENT_USER_INFO" as MezonAppEvent, (_, userData: any) => {
      if (!userData || !userData.user) {
        return;
      }
      const user = {
        mezonUserId: userData.user?.id,
        id: userData.user?.id,
        playerName: userData.user?.display_name ?? userData.user?.username,
        userName: userData.user?.username,
        avatar: userData.user?.avatar_url,
        email: userData?.email,
      };
      if (user) setMezonUser(user);
    });
    window.Mezon.WebView?.postEvent(
      "SEND_BOT_ID" as MezonWebViewEvent,
      { appId: import.meta.env.VITE_MEZON_APP_ID ?? "" },
      () => {}
    );
    window.Mezon.WebView?.onEvent("USER_HASH_INFO" as MezonAppEvent, async (_, data: any) => {
      const mezonEventData: string | null | undefined = data?.message?.web_app_data;
      if (!mezonEventData) return;
      setHashData({ hashData: Base64.encode(mezonEventData) });
    });
  }, [setHashData, setMezonUser]);
  useEffect(() => {
    if (!mezonUser || !hashData) return;
    const login = async () => {
      try {
        const response = await userServices.login({ userData: mezonUser, hashData: hashData.hashData });

        if (!response.isSuccess) {
          toast.error(response.errorMessage);
          return;
        }
        setCurrentUser(response.data?.userInfor as User);
        setUserHashInfo(hashData);
        setAccessToken(response.data?.accessToken);
        setToLocalStorage("accessToken", response.data?.accessToken?.token ?? "");
      } catch (error) {
        console.log("Error in GetUserProvider -> login", error);
        // toast.error(error?.message);
      }
    };
    login();
  }, [mezonUser, hashData, setCurrentUser, setUserHashInfo, setAccessToken]);
  return <div>{children}</div>;
};
export default GetUserProvider;
