import { Dialog, DialogContent, DialogTitle, DialogTrigger } from "@/components/ui/dialog.tsx";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs.tsx";
import { SocketEvents } from "@/constants/SocketEvents.ts";
import { useSocket } from "@/providers/SocketProvider.tsx";
import useGameStore from "@/stores/gameStore.ts";
import useRoomStore from "@/stores/roomStore.ts";
import AddUserTab from "./AddUserTab.tsx";
import ListUserTab from "./ListUserTab.tsx";
import MezonUserTab from "./MezonUserTab.tsx";

const ModalUser = () => {
  const { gameStatus } = useGameStore();
  const { listMezonUser, setOpenModalShowUser, openModalShowUser, currentRoom, tabs, setTabs } = useRoomStore();
  const socket = useSocket();

  const handleChangeTab = (tab: string) => {
    setTabs(tab);
  };

  const handleChangeOpenModalShowUser = (isOpen: boolean) => {
    if (gameStatus !== "waiting" && !openModalShowUser) {
      return;
    }
    setOpenModalShowUser(isOpen);
  };

  const handleSaveListUser = () => {
    if (!socket) return;
    const listUser = listMezonUser.filter((user) => user.isSelected);
    const listDucks = listUser.map((user) => ({
      ...user,
      score: {
        oldScore: 0,
        newScore: 0,
        totalScore: 0,
      },
      name: user.user?.display_name ?? user.user?.username,
    }));
    socket.emit(SocketEvents.EMIT.UPDATE_LIST_DUCK_OF_ROOM, {
      roomId: currentRoom?.roomId,
      ducks: listDucks,
    });
  };

  return (
    <Dialog open={openModalShowUser} onOpenChange={handleChangeOpenModalShowUser}>
      <DialogTrigger asChild>
        <div className='w-[60px] h-[60px] flex justify-center items-center cursor-pointer absolute top-0 left-[150px] hover:scale-[0.98] transition-all active:scale-[1.0]'>
          <img src='/Buttons/SmallButton.png' />
          <img className='w-[20px] absolute top-[10px] left-[20px]' src='/Icons/ProfileIcon.png' />
        </div>
      </DialogTrigger>
      <DialogContent aria-describedby='modal-description' aria-labelledby='modal-title' role='dialog'>
        <DialogTitle className='text-center text-[20px] hidden'>Tạo phòng</DialogTitle>
        <div className='w-[700px] h-[500px] rounded-lg p-10 bg-[url("/Window/SmallWindow.png")] bg-center bg-cover flex items-center justify-center'>
          <Tabs value={tabs} onValueChange={(e) => setTabs(e)} className='w-full h-[400px]'>
            <TabsList className='grid w-full grid-cols-3 h-[50px]'>
              <TabsTrigger className='font-titan h-[40px]' value='list-user'>
                List Ducks
              </TabsTrigger>
              <TabsTrigger className='font-titan h-[40px]' value='add-user'>
                Add Ducks
              </TabsTrigger>
              <TabsTrigger className='font-titan h-[40px]' value='mezon-user'>
                Mezon Ducks
              </TabsTrigger>
            </TabsList>
            <TabsContent
              className='h-[calc(100%-70px)] outline-none p-2 overflow-y-auto [&::-webkit-scrollbar]:w-[3px] [&::-webkit-scrollbar-thumb]:bg-red-500 [&::-webkit-scrollbar-thumb]:rounded-lg [&::-webkit-scrollbar-track]:bg-gray-200 bg-[#fdfdfd54] rounded-lg select-none'
              value='list-user'
            >
              <ListUserTab handleChangeTab={handleChangeTab} />
            </TabsContent>
            <TabsContent className='h-[calc(100%-70px)] p-2 bg-[#fdfdfd54] rounded-lg select-none' value='add-user'>
              <AddUserTab />
            </TabsContent>
            <TabsContent className='h-[calc(100%-70px)] bg-[#fdfdfd54] rounded-lg select-none' value='mezon-user'>
              <MezonUserTab handleSaveListUser={handleSaveListUser} />
            </TabsContent>
          </Tabs>
        </div>
      </DialogContent>
    </Dialog>
  );
};
export default ModalUser;
