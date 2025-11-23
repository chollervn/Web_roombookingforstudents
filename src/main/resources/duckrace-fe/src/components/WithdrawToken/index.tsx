import transactionService from "@/services/transactionServices.ts";
import useUserStore from "@/stores/userStore.ts";
import { memo, useEffect, useRef, useState } from "react";
import { toast } from "react-toastify";
import { Dialog, DialogContent, DialogTitle, DialogTrigger } from "../ui/dialog.tsx";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "../ui/tabs.tsx";
import InstructionTab from "./InstructionTab";
import WithdrawTab from "./WithdrawTab";

interface WithdrawTokenProps {
  openModalWithdraw: boolean;
  setOpenModalWithdraw: (value: boolean) => void;
}
const WithdrawToken = ({ openModalWithdraw, setOpenModalWithdraw }: WithdrawTokenProps) => {
  const { changeWallet, currentUser } = useUserStore();
  const depositAmountRef = useRef<number>(0);
  const [depositAmount, setDepositAmount] = useState<number>(0);
  useEffect(() => {
    depositAmountRef.current = depositAmount;
  }, [depositAmount]);
  const handleWithdraw = async () => {
    try {
      const amount = depositAmount;
      if (amount <= 0) {
        toast.error("Số tiền phải lớn hơn 0");
        return;
      }
      if (currentUser.wallet && amount > currentUser?.wallet) {
        toast.error("Số tiền rút không được lớn hơn số dư trong tài khoản");
        return;
      }
      const response = await transactionService.withdrawToken({
        amount,
      });
      console.log("response", response);
      if (response.isSuccess) {
        toast.success("Rút tiền thành công");
        setDepositAmount(0);
        changeWallet(response.data?.currentWallet ?? 0);
        setOpenModalWithdraw(false);
      } else {
        toast.error(response.errorMessage);
      }
    } catch (error) {
      console.log("Error in handleWithdraw", error);
      toast.error((error as { message: string })?.message);
    }
  };
  return (
    <Dialog open={openModalWithdraw} onOpenChange={setOpenModalWithdraw}>
      <DialogTrigger asChild></DialogTrigger>
      <DialogContent aria-describedby='modal-description' aria-labelledby='modal-title' role='dialog'>
        <DialogTitle id='modal-title' className='hidden'>
          Modal Title
        </DialogTitle>
        <div className='w-[550px] h-[400px] rounded-lg p-10 bg-[url("/Window/SmallWindow.png")] bg-center bg-cover flex items-center justify-center'>
          <Tabs className='w-full h-[320px]' defaultChecked defaultValue='deposit'>
            <TabsList className='grid w-full grid-cols-2 h-[50px]'>
              <TabsTrigger className='font-titan uppercase h-[40px]' value='deposit'>
                Rút tiền
              </TabsTrigger>
              <TabsTrigger className='font-titan uppercase h-[40px]' value='instruction'>
                Hướng dẫn
              </TabsTrigger>
            </TabsList>
            <TabsContent
              className='h-[calc(100%-70px)] outline-none p-2 overflow-y-auto [&::-webkit-scrollbar]:w-[3px] [&::-webkit-scrollbar-thumb]:bg-red-500 [&::-webkit-scrollbar-thumb]:rounded-lg [&::-webkit-scrollbar-track]:bg-gray-200 bg-[#fdfdfd54] rounded-lg select-none'
              value='deposit'
            >
              <WithdrawTab onWithdraw={handleWithdraw} depositAmount={depositAmount} setDepositAmount={setDepositAmount} />
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

export default memo(WithdrawToken);
