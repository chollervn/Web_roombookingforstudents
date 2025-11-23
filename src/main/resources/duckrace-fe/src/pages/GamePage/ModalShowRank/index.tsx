import {
  Dialog,
  DialogContent,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog.tsx";
import { IDuck } from "@/interface/room/Room.ts";
import useGameStore from "@/stores/gameStore.ts";
import useRoomStore from "@/stores/roomStore.ts";
import { useEffect, useState } from "react";

const ModalShowRank = () => {
  const { gameStatus } = useGameStore();
  const { listDucks, openModalShowRank, setOpenModalShowRank } = useRoomStore();
  const [listPlayerAfterSort, setListPlayerAfterSort] = useState<IDuck[]>([]);
  useEffect(() => {
    const listPlayerNew = [...listDucks];
    listPlayerNew.sort((a, b) => b.score.totalScore - a.score.totalScore);
    setListPlayerAfterSort(listPlayerNew);
  }, [listDucks]);

  const handleOpenModalShowRank = (isOpen: boolean) => {
    if (gameStatus === "waiting" && isOpen) {
      return;
    }
    setOpenModalShowRank(isOpen);
  };
  return (
    <Dialog open={openModalShowRank} onOpenChange={handleOpenModalShowRank}>
      <DialogTrigger asChild>
        <div
          onClick={() => handleOpenModalShowRank(true)}
          className="w-[60px] h-[60px] flex justify-center items-center cursor-pointer absolute top-[70px] left-[80px] hover:scale-[0.98] transition-all active:scale-[1.0]"
        >
          <img src="/Buttons/SmallButton.png" />
          <img
            className="w-[25px] absolute top-[10px] left-[18px]"
            src="/Icons/MedalIcon.png"
          />
        </div>
      </DialogTrigger>
      <DialogContent
        aria-describedby="modal-description"
        aria-labelledby="modal-title"
        role="dialog"
      >
        <DialogTitle className="text-center text-[20px] hidden">
          Tạo phòng
        </DialogTitle>
        <div className='w-[350px] h-[600px] rounded-lg py-10 px-5 bg-[url("/Window/MenuWindow.png")] bg-center bg-cover '>
          <div className="flex justify-center items-center h-[50px] text-[30px] text-gray-600 font-titan">
            Rank
          </div>
          <div className="h-[450px] overflow-y-auto [&::-webkit-scrollbar]:w-[3px] [&::-webkit-scrollbar-thumb]:bg-red-500 [&::-webkit-scrollbar-thumb]:rounded-lg [&::-webkit-scrollbar-track]:bg-gray-200">
            {listPlayerAfterSort.map((player, index) => (
              <div
                className="flex items-center gap-2 py-2 border-b-2 border-[#21107266]"
                key={player.id}
              >
                <div className="bg-[url('/Icons/StarIcon.png')] font-titan flex items-center justify-center bg-center bg-no-repeat bg-cover w-[45px] h-[45px]">
                  <span className="text-gray-500">{index + 1}</span>
                </div>
                <span className="flex-1 font-titan text-gray-500">
                  {player.name ?? player.user?.display_name}
                </span>
                <div
                  className={` flex items-center justify-center bg-center bg-no-repeat bg-cover w-[50px] h-[45px] mr-2 relative`}
                  style={{
                    backgroundImage: `url("/duck-${player.colorNumber}.png")`,
                  }}
                >
                  <span className="text-gray-500 text-[12px] top-[23px] left-[18px] w-[17px] h-[17px] flex items-center line-clamp-1 justify-center absolute">
                    {player.order}
                  </span>
                </div>
              </div>
            ))}
          </div>
        </div>
      </DialogContent>
    </Dialog>
  );
};
export default ModalShowRank;
