import useRoomStore from "@/stores/roomStore.ts";

const RoomMemberTab = () => {
  const { roomMembers } = useRoomStore();
  return (
    <div>
      <div>
        {roomMembers.map((member) => (
          <div key={member.id} className='mb-1 p-2 border-b-[1px] border-[#21107266] flex items-center justify-between'>
            <div className=' flex items-center font-titan text-gray-700 text-[15px] gap-2 '>
              <img
                className='w-[35px] h-[35px] rounded-full'
                src={member.avatar ?? "https://mezon.ai/assets/images/mezon-logo-black.svg"}
              />
              <div className='ml-2 flex flex-col justify-center items-start flex-1 h-full w-full'>
                <span className=''>{member.userName}</span>
                <span className='text-[12px] font-light text-gray-500'>{member.email}</span>
              </div>
            </div>
            <button className='w-[35px] h-[35px] flex items-center justify-center p-3 rounded-full bg-[#f47e8441] hover:bg-[#4e080c41] transition-all active:bg-[#f47e8441] shadow-inner'>
              <img className='w-full h-full' src='/Icons/CloseIcon.png' />
            </button>
          </div>
        ))}
      </div>
    </div>
  );
};
export default RoomMemberTab;
