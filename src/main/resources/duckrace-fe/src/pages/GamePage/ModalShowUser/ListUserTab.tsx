import { SocketEvents } from "@/constants/SocketEvents.ts";
import { useSocket } from "@/providers/SocketProvider.tsx";
import useRoomStore from "@/stores/roomStore.ts";

interface IListUserTabProps {
  handleChangeTab: (tab: string) => void;
}
const ListUserTab = ({ handleChangeTab }: IListUserTabProps) => {
  const { listDucks, currentRoom } = useRoomStore();
  const socket = useSocket();
  const handleRemoveUser = (id: string) => {
    if (!socket) return;
    socket.emit(SocketEvents.EMIT.REMOVE_USER_FROM_GAME, {
      roomId: currentRoom?.roomId,
      ducks: [id],
    });
  };
  return (
    <div className='select-none w-full h-full outline-none'>
      {listDucks?.length > 0 ? (
        listDucks?.map((player, index) => (
          <div
            key={player.id}
            className='flex items-center justify-between h-[50px] border-b border-gray-500 text-gray-800 font-titan'
          >
            <span className='ml-2'>{index + 1}</span>
            <span className=''>{player.name ?? player.user?.display_name}</span>
            <span
              onClick={() => handleRemoveUser(player.id)}
              className='w-[30px] h-[30px] cursor-pointer hover:text-red-700 hover:shadow-inner rounded-full flex items-center justify-center mr-2'
            >
              X
            </span>
          </div>
        ))
      ) : (
        <div className='text-center flex flex-col items-center h-[200px] justify-center'>
          <span className='font-titan text-lg text-red-500'>You have not added any players yet!</span>
          <div
            onClick={() => handleChangeTab("add-user")}
            className='w-[150px] h-[70px] mx-auto mt-3 relative cursor-pointer hover:scale-[0.99] transition-all active:scale-[1.0]'
          >
            <img className='w-full' src='/Buttons/Button.png' />
            <span className='absolute top-3 left-0 flex items-center justify-center w-full text-[25px] font-titan'>Thêm</span>
          </div>
        </div>
      )}
    </div>
  );
};
export default ListUserTab;
