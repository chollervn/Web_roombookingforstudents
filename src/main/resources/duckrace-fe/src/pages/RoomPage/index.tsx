import { SocketEvents } from "@/constants/SocketEvents.ts";
import { AppResponse } from "@/interface/app/AppResponse.ts";
import { Room } from "@/interface/room/Room.ts";
import { useSocket } from "@/providers/SocketProvider.tsx";
import { ROUTES } from "@/routes/path.ts";
import useRoomStore from "@/stores/roomStore.ts";
import useUserStore from "@/stores/userStore.ts";
import { MezonWebViewEvent } from "@/types/webview";
import { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { toast } from "react-toastify";
import ModalCreateRoom from "./ModalCreateRoom";
import RoomItem from "./RoomItem";

const RoomPage = () => {
  const socket = useSocket();
  const navigate = useNavigate();
  const { setListRoom, listRoom, setOpenModalCreateRoom } = useRoomStore();
  const { currentUser } = useUserStore();
  const handleJoinRoom = (roomId: string) => {
    if (!socket) return;
    socket.emit(SocketEvents.EMIT.JOIN_ROOM, { roomId, userId: currentUser.id });
  };
  const handleGoToHomePage = () => {
    navigate(ROUTES.HOME);
  };
  useEffect(() => {
    if (!socket || !currentUser.id) return;
    socket.emit(SocketEvents.EMIT.GET_LIST_ROOMS, {});
    socket.on(SocketEvents.ON.GET_LIST_ROOMS_SUCCESS, (data: AppResponse<Room[]>) => {
      setListRoom(data.data as Room[]);
    });
    socket.on(SocketEvents.ON.GET_LIST_ROOMS_FAILED, () => {});
    socket.on(SocketEvents.ON.JOIN_ROOM_SUCCESS, (data: AppResponse<Room>) => {
      window.Mezon.WebView?.postEvent(
        "JOIN_ROOM" as MezonWebViewEvent,
        {
          roomId: (data.data as Room).roomId,
        },
        () => {}
      );
      navigate(`${ROUTES.ROOM_DETAIL.replace(":roomId", (data.data as Room).roomId)}?gameId=${(data.data as Room).currentGame}`);
    });
    socket.on(SocketEvents.ON.JOIN_ROOM_FAILED, (data: AppResponse<Room>) => {
      toast.warning(data.errorMessage);
    });
    return () => {
      socket.off(SocketEvents.ON.GET_LIST_ROOMS_SUCCESS);
      socket.off(SocketEvents.ON.GET_LIST_ROOMS_FAILED);
      socket.off(SocketEvents.ON.JOIN_ROOM_SUCCESS);
      socket.off(SocketEvents.ON.JOIN_ROOM_FAILED);
    };
  }, [setListRoom, socket, currentUser.id, navigate]);

  return (
    <div className='h-full w-full bg-gradient-to-b from-teal-100 via-teal-300 to-teal-200 bg-center bg-no-repeat  p-2 relative'>
      <div className='h-[100px] flex justify-center relative'>
        <div
          onClick={handleGoToHomePage}
          className='w-[60px] h-[60px] flex justify-center items-center cursor-pointer absolute top-0 left-2 hover:scale-[0.98] transition-all active:scale-[1.0]'
        >
          <img src='/Buttons/SmallButton-pressed.png' />
          <img className='w-[30px] absolute top-[12px] left-[12px]' src='/Icons/ExitIcon.png' />
        </div>
        <div className='w-[200px] h-[80px] rounded-lg flex justify-center items-center bg-[url("/Rooms/room-top.png")] bg-no-repeat bg-center bg-cover '>
          <span className='font-titan text-2xl select-none'>Rooms</span>
        </div>
        <ModalCreateRoom />
        {/* <ModalSearchRoom /> */}
      </div>
      <div className='h-[calc(100%-100px)] w-full overflow-y-auto [&::-webkit-scrollbar]:w-[0px] [&::-webkit-scrollbar-thumb]:hidden [&::-webkit-scrollbar-thumb]:rounded-lg [&::-webkit-scrollbar-track]:bg-gray-200'>
        {listRoom.length > 0 ? (
          <div className='grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 w-full'>
            {listRoom?.map((room, index) => (
              <div className='w-full flex justify-center items-center' key={index}>
                <RoomItem onJoinRoom={handleJoinRoom} room={room} index={index} />
              </div>
            ))}
          </div>
        ) : (
          <div className='flex justify-center items-center h-full'>
            <div className='flex flex-col items-center gap-2 font-titan text-gray-600'>
              <span className='text-2xl '>Wellcome to duckrace game!</span>
              <span>No rooms have been created yet.</span>
              <div
                onClick={() => setOpenModalCreateRoom(true)}
                className='w-[220px] h-[60px] relative mt-5 cursor-pointer filter hover:drop-shadow-[0_0_0.1rem_rgba(124,6,226,0.874)] transition-all active:drop-shadow-[0_0_0.2rem_rgba(124,6,226,0.874)]'
              >
                <img className='w-full h-full' src='/Buttons/Button.png' />
                <span className='absolute flex h-full w-full justify-center items-center top-0 left-0 text-[18px] '>
                  Create Room Now
                </span>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
export default RoomPage;
