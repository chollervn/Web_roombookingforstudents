import useUserStore from "@/stores/userStore.ts";
import { useState } from "react";
import DepositToken from "../DepositToken";
import HistoryTransaction from "../HistoryTransaction";
import { Popover, PopoverContent, PopoverTrigger } from "../ui/popover.tsx";
import WithdrawToken from "../WithdrawToken";

const UserInformation = () => {
  const [openModalDeposit, setOpenModalDeposit] = useState(false);
  const [openModalWithdraw, setOpenModalWithdraw] = useState(false);
  const [openModalHistory, setOpenModalHistory] = useState(false);
  const { currentUser } = useUserStore();
  return (
    <div className='w-[75px] h-[75px] bg-gray-300 rounded-md relative'>
      <div
        className='w-full h-full flex justify-center items-center bg-[url(/avatar-default.jpg)] bg-center bg-cover bg-no-repeat rounded-md border-2 border-[#23ae89d1]'
        style={{ backgroundImage: `url('${currentUser?.avatar ?? "/avatar-default.jpg"}')` }}
      ></div>
      <Popover>
        <PopoverTrigger asChild>
          <div
            className='bg-gray-400 w-[35px] h-[35px] bg-opacity-75 absolute bottom-0 right-0 flex justify-center items-center cursor-pointer hover:bg-opacity-100 transition-all active:scale-[0.98]'
            style={{ borderBottomRightRadius: "8px", borderTopLeftRadius: "20px" }}
          >
            <img src='/Icons/RewardIcon.png' alt='edit' className='w-[20px] h-[20px]' />
          </div>
        </PopoverTrigger>
        <PopoverContent className='w-64 min-h-72 bg-[#23ae89d1] p-2 m-2'>
          <div className='flex flex-col items-center'>
            <div
              className='w-[75px] h-[75px] flex justify-center items-center bg-center bg-cover rounded-md bg-no-repeat'
              style={{ backgroundImage: `url('${currentUser?.avatar ?? "/avatar-default.jpg"}')` }}
            ></div>
            <p className='font-titan text-white mt-2'>{currentUser?.userName ?? currentUser.playerName ?? "ten.nguoichoi"}</p>
            <div className='flex items-center justify-center text-white font-titan gap-2 text-lg mt-1'>
              <img src='/Icons/DollarIcon.png' className='w-5 h-7' alt='' />
              <span>{currentUser.wallet ?? 0}</span>
            </div>
          </div>
          <div className='mt-3 flex flex-col gap-1'>
            <button
              onClick={() => setOpenModalDeposit(true)}
              className='w-full font-titan text-gray-600 bg-[#d9d9d9b8] active:bg-[#d9d9d9b8] hover:bg-[#d9d9d9] transition-all p-2 rounded-md flex items-center gap-2 outline-none'
            >
              <img className='w-[30px] h-[30px]' src='/deposit.png' />
              <span>Nạp tiền</span>
            </button>
            <button
              onClick={() => setOpenModalWithdraw(true)}
              className='w-full font-titan text-gray-600 bg-[#d9d9d9b8] active:bg-[#d9d9d9b8] hover:bg-[#d9d9d9] transition-all p-2 rounded-md flex items-center gap-2 outline-none'
            >
              <img className='w-[30px] h-[30px]' src='/withdraw.png' />
              <span>Rút tiền</span>
            </button>
            <button
              onClick={() => setOpenModalHistory(true)}
              className='w-full font-titan text-gray-600 bg-[#d9d9d9b8] active:bg-[#d9d9d9b8] hover:bg-[#d9d9d9] transition-all p-2 rounded-md flex items-center gap-2 outline-none'
            >
              <img className='w-[30px] h-[30px]' src='/history.png' />
              <span>Lịch sử giao dịch</span>
            </button>
          </div>
        </PopoverContent>
      </Popover>
      <div>
        <DepositToken openModalDeposit={openModalDeposit} setOpenModalDeposit={setOpenModalDeposit} />
        <WithdrawToken openModalWithdraw={openModalWithdraw} setOpenModalWithdraw={setOpenModalWithdraw} />
        <HistoryTransaction openModalHistory={openModalHistory} setOpenModalHistory={setOpenModalHistory} />
      </div>
    </div>
  );
};

export default UserInformation;
