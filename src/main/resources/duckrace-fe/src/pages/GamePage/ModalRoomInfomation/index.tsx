import { Dialog, DialogContent, DialogTitle, DialogTrigger } from "@/components/ui/dialog.tsx";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs.tsx";
import { useState } from "react";
import RoomInformationTab from "./RoomInfomationTab.tsx";
import RoomMemberTab from "./RoomMemberTab.tsx";
const ModalRoomInfomation = () => {
  const [tabs, setTabs] = useState("room-infomation");
  return (
    <Dialog>
      <DialogTrigger asChild>
        <div className='w-[60px] h-[60px] flex justify-center items-center cursor-pointer absolute top-0 left-[80px] hover:scale-[0.98] transition-all active:scale-[1.0]'>
          <img src='/Buttons/SmallButton-hover.png' />
          <img className='w-[30px] absolute top-[12px] left-[15px]' src='/Icons/InfoIcon.png' />
        </div>
      </DialogTrigger>
      <DialogContent aria-describedby='modal-description' aria-labelledby='modal-title' role='dialog'>
        <DialogTitle className='text-center text-[20px] hidden'></DialogTitle>
        <div className='w-[700px] h-[500px] rounded-lg p-10 bg-[url("/Window/SmallWindow.png")] bg-center bg-cover flex items-center justify-center'>
          <Tabs value={tabs} onValueChange={(e) => setTabs(e)} className='w-full h-[400px]'>
            <TabsList className='grid w-full grid-cols-2 h-[50px]'>
              <TabsTrigger className='font-titan h-[40px] uppercase' value='room-infomation'>
                Room Infomation
              </TabsTrigger>
              <TabsTrigger className='font-titan h-[40px] uppercase' value='room-member'>
                Room Members
              </TabsTrigger>
            </TabsList>
            <TabsContent
              className='h-[calc(100%-70px)] outline-none p-2 overflow-y-auto [&::-webkit-scrollbar]:w-[3px] [&::-webkit-scrollbar-thumb]:bg-red-500 [&::-webkit-scrollbar-thumb]:rounded-lg [&::-webkit-scrollbar-track]:bg-gray-200 bg-[#fdfdfd54] rounded-lg select-none'
              value='room-infomation'
            >
              <RoomInformationTab />
            </TabsContent>
            <TabsContent
              className='h-[calc(100%-70px)] overflow-y-auto [&::-webkit-scrollbar]:w-[3px] [&::-webkit-scrollbar-thumb]:bg-red-500 [&::-webkit-scrollbar-thumb]:rounded-lg [&::-webkit-scrollbar-track]:bg-gray-200 p-2 bg-[#fdfdfd54] rounded-lg select-none'
              value='room-member'
            >
              <RoomMemberTab />
            </TabsContent>
          </Tabs>
        </div>
      </DialogContent>
    </Dialog>
  );
};
export default ModalRoomInfomation;
