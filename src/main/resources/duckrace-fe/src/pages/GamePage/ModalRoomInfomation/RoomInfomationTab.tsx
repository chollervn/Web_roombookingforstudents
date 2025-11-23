import useRoomStore from "@/stores/roomStore.ts";

const RoomInformationTab = () => {
  const { currentRoom, roomMembers } = useRoomStore();
  return (
    <div className='text-gray-700 font-titan text-[18px] flex flex-col gap-3'>
      <div className='flex justify-between items-center'>
        <span>Room Code</span>
        <span>{currentRoom?.roomInfo?.roomId}</span>
      </div>
      <div className='flex justify-between items-center'>
        <span>Room Name</span>
        <span>{currentRoom?.roomInfo?.roomName}</span>
      </div>
      <div className='flex justify-between items-center'>
        <span>Password</span>
        <span>No Password</span>
      </div>
      <div className='flex justify-between items-center'>
        <span>Owner</span>
        <span>{currentRoom?.owner?.userName}</span>
      </div>
      <div className='flex justify-between items-center'>
        <span>Room Bet</span>
        <span>{currentRoom?.roomInfo?.roomBet} VND</span>
      </div>
      <div className='flex justify-between items-center'>
        <span>Expired Time</span>
        <span>{currentRoom?.expiredTime} second</span>
      </div>
      <div className='flex justify-between items-center'>
        <span>Member Count</span>
        <span>{roomMembers?.length}</span>
      </div>
    </div>
  );
};
export default RoomInformationTab;
