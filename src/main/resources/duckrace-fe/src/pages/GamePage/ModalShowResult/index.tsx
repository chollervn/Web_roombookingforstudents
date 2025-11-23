import { Dialog, DialogContent, DialogTitle } from "@/components/ui/dialog.tsx";
import { IDuck } from "@/interface/room/Room.ts";
import useGameStore from "@/stores/gameStore.ts";
import useRoomStore from "@/stores/roomStore.ts";
import useUserStore from "@/stores/userStore.ts";
import { useEffect, useState } from "react";

interface IModalShowResultProps {
  onResetGame: () => void;
}
const ModalShowResult = ({ onResetGame }: IModalShowResultProps) => {
  const { isCompletedAll, gameStatus, setOpenModalBet, gameResult } =
    useGameStore();
  const {
    listDucks,
    currentRoom,
    openModalShowResult,
    setOpenModalShowResult,
    setOpenModalShowRank,
  } = useRoomStore();
  const { currentUser } = useUserStore();
  const [listPlayerAfterSort, setListPlayerAfterSort] = useState<IDuck[]>([]);
  useEffect(() => {
    const listPlayerNew = [...listDucks];
    listPlayerNew.sort((a, b) => b.score.totalScore - a.score.totalScore);
    setListPlayerAfterSort(listPlayerNew);
  }, [listDucks]);

  useEffect(() => {
    if (isCompletedAll) {
      setOpenModalShowResult(true);
      setOpenModalBet(false);
    }
  }, [isCompletedAll, setOpenModalBet, setOpenModalShowResult]);

  const handleOpenModalShowRank = () => {
    setOpenModalShowRank(true);
  };
  return (
    <Dialog open={openModalShowResult}>
      <DialogContent
        aria-describedby="modal-description"
        aria-labelledby="modal-title"
        role="dialog"
      >
        <DialogTitle className="text-center text-[20px] hidden">
          Tạo phòng
        </DialogTitle>
        <div className='w-[350px] h-[600px] rounded-lg p-5 bg-[url("/Window/MenuWindow.png")] bg-center bg-cover '>
          <div className="flex justify-center items-center h-[45px] text-[40px] text-gray-600 font-titan">
            Result
          </div>
          <div className="h-[230px] flex items-end justify-center relative">
            <img className="w-[300px]" src="/rank.png" />
            <div
              className={` flex items-center justify-center bg-center bg-no-repeat bg-cover w-[70px] h-[58px] mr-2 absolute left-[50px] bottom-[105px]`}
              style={{
                backgroundImage: `url("/duck-${listPlayerAfterSort[1]?.colorNumber}.png")`,
              }}
            >
              <span className="text-gray-500 text-[12px] top-[30px] left-[27px] w-[22px] h-[22px] flex items-center line-clamp-1 justify-center absolute ">
                {listPlayerAfterSort[1]?.order}
              </span>
            </div>
            <div
              className={` flex items-center justify-center bg-center bg-no-repeat bg-cover w-[70px] h-[58px] mr-2 absolute left-[125px] bottom-[155px]`}
              style={{
                backgroundImage: `url("/duck-${listPlayerAfterSort[0]?.colorNumber}.png")`,
              }}
            >
              <span className="text-gray-500 text-[12px] top-[30px] left-[27px] w-[22px] h-[22px] flex items-center line-clamp-1 justify-center absolute">
                {listPlayerAfterSort[0]?.order}
              </span>
            </div>
            <div
              className={` flex items-center justify-center bg-center bg-no-repeat bg-cover w-[70px] h-[58px] mr-2 absolute bottom-[130px] right-[30px]`}
              style={{
                backgroundImage: `url("/duck-${listPlayerAfterSort[2]?.colorNumber}.png")`,
              }}
            >
              <span className="text-gray-500 text-[12px] top-[30px] left-[27px] w-[22px] h-[22px] flex items-center line-clamp-1 justify-center absolute">
                {listPlayerAfterSort[2]?.order}
              </span>
            </div>
          </div>
          <div className="h-[160px] flex items-center justify-center">
            <div className="flex items-center">
              <img className="w-[20px]" src="/Icons/PlusIcon.png" />
              <span className="text-[50px] font-titan inline-block ml-1 mr-2 text-gray-500">
                {gameResult?.bettors &&
                gameResult.bettors.includes(currentUser.id ?? "")
                  ? gameResult?.winners && gameResult.winners.length > 0
                    ? gameResult.winners.includes(currentUser.id ?? "")
                      ? gameResult.winBet
                      : 0
                    : gameResult?.winBet
                  : 0}
              </span>
              <img className="w-[30px]" src="/Icons/DollarIcon.png" />
            </div>
          </div>
          <div className="flex justify-center items-center gap-2 mt-5">
            {isCompletedAll ? (
              <button
                disabled={
                  gameStatus !== "completed" ||
                  currentRoom?.ownerId !== currentUser.id
                }
                onClick={onResetGame}
                className="w-[150px] h-[55px] cursor-pointer justify-center items-center relative filter hover:drop-shadow-[0_0_0.1rem_rgba(124,6,226,0.874)] transition-all active:drop-shadow-[0_0_0.2rem_rgba(124,6,226,0.874)]"
              >
                <img
                  className="w-full h-full"
                  src={
                    gameStatus !== "completed" ||
                    currentRoom?.ownerId !== currentUser.id
                      ? "/Buttons/Button-disabled.png"
                      : "/Buttons/Button.png"
                  }
                />
                <div className="flex  justify-center items-center gap-2 text-[30px] font-titan text-white absolute top-1 left-1/2 transform -translate-x-1/2 ">
                  <img className="w-[20px] " src="/Icons/RefreshIcon.png" />
                  <span>Reset</span>
                </div>
              </button>
            ) : (
              <div
                onClick={() => setOpenModalShowResult(false)}
                className="w-[150px] h-[55px] cursor-pointer justify-center items-center relative filter hover:drop-shadow-[0_0_0.1rem_rgba(124,6,226,0.874)] transition-all active:drop-shadow-[0_0_0.2rem_rgba(124,6,226,0.874)]"
              >
                <img
                  className="w-full h-full"
                  src="/Buttons/Button-pressed.png"
                />
                <div className="flex  justify-center items-center gap-2 text-[30px] font-titan text-white absolute top-1 left-1/2 transform -translate-x-1/2 ">
                  <img className="w-[20px] " src="/Icons/RefreshIcon.png" />
                  <span>Close</span>
                </div>
              </div>
            )}
            <button
              onClick={handleOpenModalShowRank}
              className="w-[150px] h-[55px] cursor-pointer justify-center items-center relative filter hover:drop-shadow-[0_0_0.1rem_rgba(124,6,226,0.874)] transition-all active:drop-shadow-[0_0_0.2rem_rgba(124,6,226,0.874)]"
            >
              <img className="w-full h-full" src="/Buttons/Button-hover.png" />
              <div className="flex  justify-center items-center gap-2 text-[30px] font-titan text-white absolute top-1 left-1/2 transform -translate-x-1/2 ">
                <img className="w-[20px] " src="/Icons/RewardIcon.png" />
                <span>Rank</span>
              </div>
            </button>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  );
};
export default ModalShowResult;
