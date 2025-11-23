import useUserStore from "@/stores/userStore.ts";
import { MezonWebViewEvent } from "@/types/webview";
import { memo } from "react";
import { toast } from "react-toastify";

interface DepositTabProps {
  depositAmount: number;
  setDepositAmount: (value: number) => void;
}
const DepositTab = ({ depositAmount, setDepositAmount }: DepositTabProps) => {
  const { currentUser } = useUserStore();
  const handleDeposit = async () => {
    try {
      const amount = depositAmount;
      if (amount <= 0) {
        toast.error("Số tiền nạp phải lớn hơn 0");
        return;
      }
      const dataEmit = {
        receiver_id: import.meta.env.VITE_MEZON_APP_ID,
        amount,
        note: `Bạn đã nạp ${amount} token từ Mezon vào game Duck Race`,
        sender_id: currentUser.id,
        sender_name: currentUser.userName,
      };
      window.Mezon.WebView?.postEvent("SEND_TOKEN" as MezonWebViewEvent, dataEmit, () => {});
    } catch (error) {
      console.log("Error in handleDeposit", error);
      toast.error((error as { message: string })?.message);
    }
  };

  const handleChangeAmount = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value.replace(/\D/g, ""); // Loại bỏ tất cả ký tự không phải số
    setDepositAmount(value ? Number(value) : 0);
  };
  return (
    <div className=''>
      <div className='text-center font-titan text-gray-600 uppercase'>Nạp tiền từ mezon</div>
      <div className='flex justify-center items-center mt-5 flex-col gap-2'>
        <div className='relative'>
          <img src='/Icons/DollarIcon.png' className='w-5 h-7 absolute left-2 top-1/2 -translate-y-1/2 ' alt='' />
          <div className='w-[1px] h-[30px] bg-gray-600 absolute left-[35px] top-1/2 -translate-y-1/2'></div>
          <input
            maxLength={8}
            onChange={(e) => handleChangeAmount(e)}
            value={depositAmount}
            className='h-[50px] w-[200px] pr-3 pl-[45px] rounded-lg bg-[#d9d9d9b8] font-titan uppercase text-center text-gray-600 text-xl outline-none border-2 border-transparent focus:border-white transition-all'
          />
        </div>
        <div
          onClick={handleDeposit}
          className='w-[180px] h-[70px] mx-auto mt-3 relative cursor-pointer hover:scale-[0.99] transition-all active:scale-[1.0]'
        >
          <img className='w-full h-full' src='/Buttons/Button.png' />
          <span className='absolute top-3 left-0 flex items-center justify-center w-full text-[25px] font-titan'>Xác nhận</span>
        </div>
      </div>
    </div>
  );
};

export default memo(DepositTab);
