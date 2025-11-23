import { Dialog, DialogContent, DialogTitle, DialogTrigger } from "@/components/ui/dialog.tsx";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover.tsx";
import { SocketEvents } from "@/constants/SocketEvents.ts";
import { DuckPicked } from "@/interface/game/Game.ts";
import { useSocket } from "@/providers/SocketProvider.tsx";
import useGameStore from "@/stores/gameStore.ts";
import useRoomStore from "@/stores/roomStore.ts";
import useUserStore from "@/stores/userStore.ts";
import { useEffect, useRef, useState } from "react";

interface ModalBetProps {
  isPopoverOpen: boolean;
  setIsPopoverOpen: (isOpen: boolean) => void;
}
const ModalBet = ({ isPopoverOpen, setIsPopoverOpen }: ModalBetProps) => {
  const [timeCountDown, setTimeCountDown] = useState<number | null>(30);
  const [isActive, setIsActive] = useState(false);
  const listDuckPickedRef = useRef<DuckPicked[]>([]);

  const { gameStatus, openModalBet, setOpenModalBet, listBettorOfDucks, listDuckPicked, setListDuckPicked, isConfirmedBet } =
    useGameStore();
  const { currentRoom } = useRoomStore();
  const { currentUser } = useUserStore();
  const socket = useSocket();
  const handleOpenModalBet = (isOpen: boolean) => {
    if (gameStatus === "betting") {
      return;
    }
    setOpenModalBet(isOpen);
  };

  const handlePickDuck = (duckId: string, index: number) => {
    if (gameStatus === "completed" || gameStatus === "racing") return;
    const checkIsPicked = listDuckPicked.find((duck) => duck.duckId === duckId);
    const duck = listBettorOfDucks.find((duck) => duck.id === duckId);
    if (!checkIsPicked) {
      setListDuckPicked([
        ...listDuckPicked,
        {
          duckId: duckId,
          duckName: duck?.name ?? "",
          duckOrder: index + 1,
        },
      ]);
    } else {
      setListDuckPicked(listDuckPicked.filter((duck) => duck.duckId !== duckId));
    }
  };
  const handleConfirmBet = () => {
    if (!socket) return;

    socket.emit(SocketEvents.EMIT.BET_FOR_DUCK, {
      ducks: listDuckPickedRef.current?.map((duck) => duck.duckId),
      gameId: currentRoom?.currentGame,
      userId: currentUser.id,
      betAmount: currentRoom?.roomInfo.roomBet ?? 1,
      roomId: currentRoom?.roomId,
    });
  };

  useEffect(() => {
    listDuckPickedRef.current = listDuckPicked;
  }, [listDuckPicked]);
  useEffect(() => {
    if (!socket || !currentRoom?.currentGame) return;
    socket.emit(SocketEvents.EMIT.GET_GAME_BETTORS, currentRoom?.currentGame);
  }, [currentRoom?.currentGame, openModalBet, socket]);

  useEffect(() => {
    if (gameStatus === "betting") {
      setTimeCountDown(30);
      setIsActive(true);
    } else if (gameStatus === "waiting") {
      setIsActive(false);
      setTimeCountDown(30);
    }
  }, [gameStatus, openModalBet]);

  useEffect(() => {
    let interval: NodeJS.Timeout | null = null;

    if (isActive && timeCountDown !== null) {
      interval = setTimeout(() => {
        if (timeCountDown > 0) {
          setTimeCountDown(timeCountDown - 1);
        } else {
          setIsActive(false);
        }
      }, 1000);
    }
    return () => {
      if (interval) clearTimeout(interval);
    };
  }, [isActive, timeCountDown]);
  return (
    <Dialog open={openModalBet} onOpenChange={handleOpenModalBet}>
      <DialogTrigger asChild>
        <div
          onClick={() => setOpenModalBet(true)}
          className='w-[60px] h-[60px] flex justify-center items-center cursor-pointer absolute top-[70px] left-2 hover:scale-[0.98] transition-all active:scale-[1.0]'
        >
          <img src='/Buttons/SmallButton.png' />
          <img className='w-[30px] absolute top-[13px] left-[15px]' src='/Icons/GameIcon.png' />
        </div>
      </DialogTrigger>
      <DialogContent aria-describedby='modal-description' aria-labelledby='modal-title' role='dialog'>
        <DialogTitle className='text-center text-[20px] hidden'>Tạo phòng</DialogTitle>
        <div className='w-[620px] h-[600px] rounded-lg p-10 pt-5 bg-[url("/Window/MediumWindow.png")] bg-center bg-cover font-titan flex  items-center flex-col gap-2'>
          <div className='text-center text-[30px]'>
            {gameStatus === "racing"
              ? "BETTORS OF GAME"
              : gameStatus === "betting"
              ? "THE MATCH WILL START IN 30s"
              : "LIST DUCKS YOU CAN BET"}
          </div>
          {gameStatus === "betting" && (
            <div className='w-[150px] h-[55px] justify-center items-center relative '>
              <img className='w-full h-full' src='/Buttons/Button-hover.png' />
              <div className='flex justify-center items-center gap-1 text-[30px] font-titan text-white absolute top-1 left-1/2 transform -translate-x-1/2 '>
                <span className='flex justify-center items-center w-[50px]'>
                  {timeCountDown !== null && Math.floor(timeCountDown / 60) >= 0
                    ? `0${Math.floor(timeCountDown / 60)}`.slice(-2)
                    : "00"}
                </span>
                <span>:</span>
                <span className='flex justify-center items-center w-[50px]'>
                  {timeCountDown !== null && timeCountDown % 60 >= 0 ? `0${timeCountDown % 60}`.slice(-2) : "00"}
                </span>
              </div>
            </div>
          )}
          <div className='text-center text-[25px]'>Pick your duck if you want to bet</div>
          <div className='w-full h-[380px] bg-[#9f9e9e26] rounded-xl overflow-y-auto [&::-webkit-scrollbar]:w-[3px] [&::-webkit-scrollbar-thumb]:bg-red-500 [&::-webkit-scrollbar-thumb]:rounded-lg [&::-webkit-scrollbar-track]:bg-gray-200'>
            <div className='grid grid-cols-4 gap-2 p-2'>
              {listBettorOfDucks.map((duck, index) => {
                const checkIsPicked = listDuckPicked.find((duckPicked) => duckPicked.duckId === duck.id);
                return (
                  <Popover key={duck.id}>
                    <PopoverTrigger asChild>
                      <div
                        className={`flex p-2 relative justify-center bg-[#9f9e9e26] rounded-lg h-[125px] w-full hover:bg-[#9f9e9e4d] active:bg-[#9f9e9e26] transition-all cursor-pointer ${
                          checkIsPicked ? "border-2 border-[#9f9e9e]" : ""
                        }`}
                      >
                        <div className='w-[90px] h-[80px] rounded-full relative'>
                          <img className='w-full h-full ' src={`/duck-${duck.colorNumber}.png`} />
                          <span className='absolute bottom-[7px] left-[34px] text-gray-500 w-[30px] h-[30px] flex items-center justify-center'>
                            {duck.order}
                          </span>
                        </div>
                        <div
                          className='w-[30px] h-[30px] flex items-center justify-center bg-[#9b9b9b] text-gray-100 absolute bottom-0 right-0'
                          style={{ borderTopLeftRadius: "20px", borderBottomRightRadius: "6px" }}
                        >
                          {duck.bettors?.length}
                        </div>
                      </div>
                    </PopoverTrigger>
                    <PopoverContent className='w-80 h-80 bg-[#bab29bd9] p-2'>
                      <div className='h-full w-full  '>
                        <div className='text-white text-center font-titan border-b-2 pb-1 border-gray-300 h-[55px]'>
                          <span className=''>Duck {duck.order}</span>
                          <p>
                            - <span className='px-2'> {duck.name} </span> -
                          </p>
                        </div>
                        <div className='h-[calc(100%-110px)] overflow-y-auto [&::-webkit-scrollbar]:w-[3px] [&::-webkit-scrollbar-thumb]:bg-red-500 [&::-webkit-scrollbar-thumb]:rounded-lg [&::-webkit-scrollbar-track]:bg-gray-200'>
                          {/* <div className='h-full flex justify-center items-center'>
                            <span className='font-titan text-white'>No one has bet on this duck yet.</span>
                          </div> */}
                          {duck.bettors && duck.bettors.length > 0 ? (
                            <div className='p-1 '>
                              {duck.bettors?.map((bettor) => (
                                <div
                                  key={bettor.userId}
                                  className='flex p-2 items-center bg-[#9f9e9ead] rounded-lg h-[55px] w-full mb-1'
                                >
                                  <img className='w-[40px] h-[40px] rounded-full' src='/Icons/StarIcon.png' />
                                  <div className='flex flex-col gap-1 ml-2'>
                                    <span className='text-white font-titan'>{bettor.user.userName}</span>
                                    <span className='text-white font-titan text-sm'>Quyen Nguyen Ta</span>
                                  </div>
                                </div>
                              ))}
                            </div>
                          ) : (
                            <div className='h-full flex justify-center items-center'>
                              <span className='font-titan text-white'>No one has bet on this duck yet.</span>
                            </div>
                          )}
                        </div>
                        <div className='border-t-2 pt-1 border-gray-300 h-[55px] flex justify-center items-center'>
                          <button
                            disabled={gameStatus === "completed" || gameStatus === "racing" || isConfirmedBet}
                            onClick={() => handlePickDuck(duck.id, index)}
                            className='w-[100px] h-[35px] cursor-pointer justify-center items-center relative filter hover:drop-shadow-[0_0_0.1rem_rgba(124,6,226,0.874)] transition-all active:drop-shadow-[0_0_0.2rem_rgba(124,6,226,0.874)]'
                          >
                            <img
                              className='w-full h-full'
                              src={
                                gameStatus === "completed" || gameStatus === "racing" || isConfirmedBet
                                  ? "/Buttons/Button-disabled.png"
                                  : "/Buttons/Button.png"
                              }
                            />
                            <div className='flex w-full justify-center items-center gap-2 text-[18px] font-titan text-white absolute top-1 left-1/2 transform -translate-x-1/2 '>
                              <span>{checkIsPicked ? "Un Pick" : "Pick"}</span>
                            </div>
                          </button>
                        </div>
                      </div>
                    </PopoverContent>
                  </Popover>
                );
              })}
            </div>
          </div>
          <div className=' pt-1 h-[55px] flex justify-center items-center'>
            <Popover open={isPopoverOpen} onOpenChange={setIsPopoverOpen}>
              <PopoverTrigger asChild>
                <button
                  disabled={gameStatus !== "betting" || listDuckPicked.length === 0 || isConfirmedBet}
                  className='w-[150px] h-[40px] cursor-pointer justify-center items-center relative filter hover:drop-shadow-[0_0_0.1rem_rgba(124,6,226,0.874)] transition-all active:drop-shadow-[0_0_0.2rem_rgba(124,6,226,0.874)]'
                  onClick={() => setIsPopoverOpen(true)}
                >
                  <img
                    className='w-full h-full'
                    src={gameStatus === "betting" && !isConfirmedBet ? "/Buttons/Button.png" : "/Buttons/Button-disabled.png"}
                  />
                  <div className='flex w-full justify-center items-center gap-2 text-[16px] font-titan text-white absolute top-2 left-1/2 transform -translate-x-1/2 '>
                    <span className='inline-block w-full'>Confirm Bet</span>
                  </div>
                </button>
              </PopoverTrigger>
              <PopoverContent className='w-64 h-72 bg-[#7aab83] p-2'>
                <div className='font-titan flex items-center justify-center text-white text-xl h-[40px] border-b-2 '>
                  Confirm Bet
                </div>
                <div className='flex text-white flex-col gap-2 items-center justify-center text-center font-titan h-[160px]'>
                  <span>
                    You bet on ducks <br /> {listDuckPicked.map((duck) => duck.duckOrder).join(", ")}
                  </span>
                  {/* <span>Do you confirm?</span> */}
                  <span>
                    You have 30 second <br /> to confirm in Mezon
                  </span>
                </div>
                <div className='flex justify-center items-center gap-2 h-[80px]'>
                  <div
                    onClick={() => setIsPopoverOpen(false)}
                    className='w-[60px] h-[60px] flex justify-center items-center cursor-pointer relative hover:scale-[0.98] transition-all active:scale-[1.0]'
                  >
                    <img src='/Buttons/SmallButton-pressed.png' />
                    <img className='w-[30px] absolute top-[13px] left-[15px]' src='/Icons/CloseIcon.png' />
                  </div>
                  <button
                    disabled={gameStatus !== "betting" || listDuckPicked.length === 0 || isConfirmedBet}
                    onClick={handleConfirmBet}
                    className='w-[60px] h-[60px] flex justify-center items-center cursor-pointer relative hover:scale-[0.98] transition-all active:scale-[1.0]'
                  >
                    <img
                      src={
                        gameStatus === "betting" && !isConfirmedBet
                          ? "/Buttons/SmallButton.png"
                          : "/Buttons/SmallButton-disabled.png"
                      }
                    />
                    <img className='w-[30px] absolute top-[15px] left-[15px]' src='/Icons/ApproveIcon.png' />
                  </button>
                </div>
              </PopoverContent>
            </Popover>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  );
};

export default ModalBet;
