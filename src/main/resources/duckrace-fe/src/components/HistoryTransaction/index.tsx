import transactionService from "@/services/transactionServices.ts";
import useUserStore from "@/stores/userStore.ts";
import { formatDateTime } from "@/utils/Functions/formatTimes.ts";
import { memo, useEffect } from "react";
import { toast } from "react-toastify";
import { Dialog, DialogContent, DialogTitle, DialogTrigger } from "../ui/dialog.tsx";

interface HistoryTransactionProps {
  openModalHistory: boolean;
  setOpenModalHistory: (value: boolean) => void;
}
const HistoryTransaction = ({ openModalHistory, setOpenModalHistory }: HistoryTransactionProps) => {
  const { currentUser, changeHistoryTransaction, historyTransaction } = useUserStore();
  useEffect(() => {
    if (!currentUser.id) return;
    const getHistoryTransaction = async () => {
      try {
        const response = await transactionService.getHistoryTransaction();
        if (response.isSuccess) {
          changeHistoryTransaction(response.data ?? []);
        } else {
          toast.error(response.errorMessage);
        }
      } catch (error) {
        console.log("Error in getHistoryTransaction", error);
      }
    };
    getHistoryTransaction();
  }, [changeHistoryTransaction, currentUser.id]);
  return (
    <Dialog open={openModalHistory} onOpenChange={setOpenModalHistory}>
      <DialogTrigger asChild></DialogTrigger>
      <DialogContent aria-describedby='modal-description' aria-labelledby='modal-title' role='dialog'>
        <DialogTitle id='modal-title' className='hidden'></DialogTitle>
        <div className='w-[680px] h-[500px] rounded-lg p-10 pt-5 bg-[url("/Window/SmallWindow.png")] bg-center bg-cover'>
          <div className='text-center font-titan text-gray-600 uppercase'>Lịch sử giao dịch</div>
          <div className='h-[calc(100%-70px)] p-2 mt-3 overflow-y-auto [&::-webkit-scrollbar]:w-[3px] [&::-webkit-scrollbar-thumb]:bg-red-500 [&::-webkit-scrollbar-thumb]:rounded-lg [&::-webkit-scrollbar-track]:bg-gray-200'>
            {historyTransaction.map((item, index) => (
              <div
                key={item.id}
                className='bg-[#fdfdfd54] text-gray-500 min-h-[70px] mb-2 p-2 rounded-sm font-titan flex items-center gap-2'
              >
                <div className='w-[40px] text-center'>{index + 1}</div>
                <div className={`w-[100px] text-center ${item.isIncrease ? "text-green-500" : "text-red-500"}`}>
                  {item.isIncrease ? "+" : "-"} {item.amount}
                </div>
                <div className='flex-1 text-center'>{item.note}</div>
                <div className='w-[100px] text-center'>{formatDateTime(item.createdAt)}</div>
              </div>
            ))}
          </div>
        </div>
      </DialogContent>
    </Dialog>
  );
};

export default memo(HistoryTransaction);
