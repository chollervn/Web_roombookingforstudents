import { memo } from "react";

interface WithdrawTabProps {
  depositAmount: number;
  setDepositAmount: (value: number) => void;
  onWithdraw: () => void;
}
const WithdrawTab = ({ depositAmount, setDepositAmount, onWithdraw }: WithdrawTabProps) => {
  const handleChangeAmount = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value.replace(/\D/g, ""); // Loại bỏ tất cả ký tự không phải số
    setDepositAmount(value ? Number(value) : 0);
  };
  return (
    <div className=''>
      <div className='text-center font-titan text-gray-600 uppercase'>Rút tiền về Mezon</div>
      <span></span>
      <div className='flex justify-center items-center mt-5 flex-col gap-2'>
        <span className='font-titan text-sm text-gray-600'>(Nhập số tiền bạn muốn rút)</span>
        <div className='relative'>
          <img src='/Icons/DollarIcon.png' className='w-5 h-7 absolute left-2 top-1/2 -translate-y-1/2 ' alt='' />
          <div className='w-[1px] h-[30px] bg-gray-600 absolute left-[35px] top-1/2 -translate-y-1/2'></div>
          <input
            maxLength={8}
            onChange={(e) => handleChangeAmount(e)}
            value={depositAmount}
            className='h-[50px] w-[200px] pr-4 pl-[45px] rounded-lg bg-[#d9d9d9b8] font-titan uppercase text-center text-gray-600 text-xl outline-none border-2 border-transparent focus:border-white transition-all'
          />
        </div>
        <div
          onClick={onWithdraw}
          className='w-[180px] h-[70px] mx-auto mt-3 relative cursor-pointer hover:scale-[0.99] transition-all active:scale-[1.0]'
        >
          <img className='w-full h-full' src='/Buttons/Button.png' />
          <span className='absolute top-3 left-0 flex items-center justify-center w-full text-[25px] font-titan'>Xác nhận</span>
        </div>
      </div>
    </div>
  );
};

export default memo(WithdrawTab);
