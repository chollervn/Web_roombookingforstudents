import { Room } from "@/interface/room/Room.ts";

interface RoomItemProps {
  index: number;
  room: Room;
  onJoinRoom: (roomId: string) => void;
}
const RoomItem = ({ index, room, onJoinRoom }: RoomItemProps) => {
  return (
    <div
      className={`w-[320px] h-[390px] bg-center bg-contain bg-no-repeat`}
      style={{
        backgroundImage: `url("/Rooms/room-${(index % 4) + 1}.webp")`,
      }}
    >
      <div className='flex w-full justify-center items-end h-[200px] relative'>
        <div className='w-[80px] h-[35px] cursor-pointer justify-center items-center absolute bottom-[125px] left-1/2 -translate-x-1/2 filter hover:drop-shadow-[0_0_0.1rem_rgba(124,6,226,0.874)] transition-all active:drop-shadow-[0_0_0.2rem_rgba(124,6,226,0.874)]'>
          <img className='w-full h-full' src='/Buttons/Button.png' />
          <div
            onClick={() => onJoinRoom(room.roomId)}
            className='flex  justify-center items-center gap-2 text-[18px] font-titan text-white absolute top-1 left-1/2 transform -translate-x-1/2 '
          >
            <span>Join</span>
          </div>
        </div>
        <img className='w-[180px] h-[130px] ml-2' src='/Rooms/room-5.png' />
        <span className='absolute font-titan bottom-[80px] text-[25px] text-[#439887]'>{room.roomId}</span>
        <div className='absolute h-[50px] gap-1 w-[80px] right-[calc(50%-90px)] bottom-2 flex flex-col justify-center items-center'>
          <img className='w-[15px] h-[20px]' src='/Icons/DollarIcon.png' />
          <span className='w-full h-[20px] inline-block text-center font-titan text-[#439887]'>{room.roomInfo.roomBet}</span>
        </div>
        <div className='absolute h-[50px] gap-1 w-[80px] left-[calc(50%-80px)] bottom-2 flex flex-col justify-center items-center'>
          <img className='w-[20px] h-[20px]' src='/Icons/PeopleIcon.png' />
          <span className='w-full h-[20px] inline-block text-center font-titan text-[#439887]'>{room.members?.length}</span>
        </div>
      </div>
    </div>
  );
};
export default RoomItem;
