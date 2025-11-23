import { SocketEvents } from "@/constants/SocketEvents.ts";
import { useSocket } from "@/providers/SocketProvider.tsx";
import useRoomStore from "@/stores/roomStore.ts";
import { useParams } from "react-router-dom";

const AddUserTab = () => {
  const { addDuckText, setAddDuckText } = useRoomStore();
  const socket = useSocket();
  const { roomId } = useParams<{ roomId: string }>();
  const handleAddUser = () => {
    if (!socket) return;
    if (addDuckText.trim() === "" || !addDuckText) return;
    const users = addDuckText.split("\n").join(",").split(",");
    socket.emit(SocketEvents.EMIT.ADD_DUCK_TO_ROOM, { roomId, ducks: users });
  };
  return (
    <div className='h-full'>
      <textarea
        value={addDuckText}
        onChange={(e) => setAddDuckText(e.target.value)}
        className='w-full bg-[#fbfbfb8a] text-gray-950 p-2 min-h-[200px] max-h-[300px] outline-none'
      />
      <div
        onClick={handleAddUser}
        className='w-[150px] h-[70px] mx-auto mt-3 relative cursor-pointer hover:scale-[0.99] transition-all active:scale-[1.0]'
      >
        <img className='w-full' src='/Buttons/Button.png' />
        <span className='absolute top-3 left-0 flex items-center justify-center w-full text-[25px] font-titan'>Thêm</span>
      </div>
    </div>
  );
};
export default AddUserTab;
