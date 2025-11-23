import transactionService from "@/services/transactionServices.ts";
import useUserStore from "@/stores/userStore.ts";
import { MezonAppEvent } from "@/types/webview";
import { useEffect, useRef, useState } from "react";
import { toast } from "react-toastify";
import { Dialog, DialogContent, DialogTitle, DialogTrigger } from "../ui/dialog.tsx";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "../ui/tabs.tsx";
import DepositTab from "./DepositTab";
import InstructionTab from "./InstructionTab";

interface DepositTokenProps {
  openModalDeposit: boolean;
  setOpenModalDeposit: (value: boolean) => void;
}
const DepositToken = ({ openModalDeposit, setOpenModalDeposit }: DepositTokenProps) => {
  const { changeWallet } = useUserStore();
  const depositAmountRef = useRef<number>(0);
  const [depositAmount, setDepositAmount] = useState<number>(0);
  useEffect(() => {
    depositAmountRef.current = depositAmount;
  }, [depositAmount]);
  const handleDepositToken = async () => {
    try {
      const depositReponse = await transactionService.depositToken({
        amount: depositAmountRef.current,
      });
      if (depositReponse.isSuccess) {
        toast.success("Nạp tiền thành công");
        setOpenModalDeposit(false);
        setOpenModalDeposit(false);
        changeWallet(depositReponse.data?.currentWallet ?? 0);
      } else {
        toast.error(depositReponse.errorMessage);
      }
    } catch (error) {
      console.log("Error in handleDepositToken", error);
    }
  };
  useEffect(() => {
    window.Mezon.WebView?.onEvent("SEND_TOKEN_RESPONSE_SUCCESS" as MezonAppEvent, () => {
      handleDepositToken();
    });
    window.Mezon.WebView?.onEvent("SEND_TOKEN_RESPONSE_FAILED" as MezonAppEvent, () => {});
    return () => {
      window.Mezon.WebView?.offEvent("SEND_TOKEN_RESPONSE_SUCCESS" as MezonAppEvent, () => {});
      window.Mezon.WebView?.offEvent("SEND_TOKEN_RESPONSE_FAILED" as MezonAppEvent, () => {});
    };
  }, []);
  return (
    <Dialog open={openModalDeposit} onOpenChange={setOpenModalDeposit}>
      <DialogTrigger asChild></DialogTrigger>
      <DialogContent aria-describedby='modal-description' aria-labelledby='modal-title' role='dialog'>
        <DialogTitle id='modal-title' className='hidden'>
          Modal Title
        </DialogTitle>
        <div className='w-[550px] h-[400px] rounded-lg p-10 bg-[url("/Window/SmallWindow.png")] bg-center bg-cover flex items-center justify-center'>
          <Tabs className='w-full h-[320px]' defaultChecked defaultValue='deposit'>
            <TabsList className='grid w-full grid-cols-2 h-[50px]'>
              <TabsTrigger className='font-titan uppercase h-[40px]' value='deposit'>
                Nạp tiền
              </TabsTrigger>
              <TabsTrigger className='font-titan uppercase h-[40px]' value='instruction'>
                Hướng dẫn
              </TabsTrigger>
            </TabsList>
            <TabsContent
              className='h-[calc(100%-70px)] outline-none p-2 overflow-y-auto [&::-webkit-scrollbar]:w-[3px] [&::-webkit-scrollbar-thumb]:bg-red-500 [&::-webkit-scrollbar-thumb]:rounded-lg [&::-webkit-scrollbar-track]:bg-gray-200 bg-[#fdfdfd54] rounded-lg select-none'
              value='deposit'
            >
              <DepositTab depositAmount={depositAmount} setDepositAmount={setDepositAmount} />
            </TabsContent>
            <TabsContent
              className='h-[calc(100%-70px)] outline-none p-2 overflow-y-auto [&::-webkit-scrollbar]:w-[3px] [&::-webkit-scrollbar-thumb]:bg-red-500 [&::-webkit-scrollbar-thumb]:rounded-lg [&::-webkit-scrollbar-track]:bg-gray-200 bg-[#fdfdfd54] rounded-lg select-none'
              value='instruction'
            >
              <InstructionTab />
            </TabsContent>
          </Tabs>
        </div>
      </DialogContent>
    </Dialog>
  );
};

export default DepositToken;
