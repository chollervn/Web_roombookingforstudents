import { Checkbox } from "@/components/ui/checkbox.tsx";
import { Select, SelectContent, SelectGroup, SelectItem, SelectLabel, SelectTrigger, SelectValue } from "@/components/ui/select.tsx";
import useRoomStore from "@/stores/roomStore.ts";
import { useEffect, useState } from "react";

interface IMezonUserTabProps {
  handleSaveListUser: () => void;
}
const MezonUserTab = ({ handleSaveListUser }: IMezonUserTabProps) => {
  const {
    mezonClanRoles,
    selectedClanRole,
    setSelectedClanRole,
    mezonClanUsers,
    setMezonClanUsers,
    setListMezonUser,
    listMezonUser,
  } = useRoomStore();
  const handleChangeSelectedClanRole = (e: React.ChangeEvent<HTMLSelectElement>) => {
    setSelectedClanRole(e.target.value);
  };
  const [isCheckAll, setisCheckAll] = useState(false);
  useEffect(() => {
    if (selectedClanRole === mezonClanRoles[0]?.id) {
      setListMezonUser(mezonClanUsers);
    } else {
      const listMezonUser = mezonClanUsers?.filter((user) => user.role_id?.includes(selectedClanRole ?? ""));
      setListMezonUser(listMezonUser);
    }
  }, [mezonClanRoles, mezonClanUsers, selectedClanRole, setListMezonUser]);

  useEffect(() => {
    if (listMezonUser.length === 0) {
      setisCheckAll(false);
    }
    setisCheckAll(listMezonUser?.every((user) => user.isSelected));
  }, [listMezonUser]);

  const handleCheckUser = (id: string) => {
    const newList = mezonClanUsers?.map((user) => {
      if (user.id === id) {
        return { ...user, isSelected: !user.isSelected };
      }
      return user;
    });
    setMezonClanUsers(newList);
  };

  const handleToogleCheckAll = () => {
    const newList = mezonClanUsers?.map((user) => {
      if (listMezonUser?.some((u) => u.id === user.id)) {
        return { ...user, isSelected: !isCheckAll };
      }
      return user;
    });
    setMezonClanUsers(newList);
    setisCheckAll(!isCheckAll);
  };
  console.log("listMezonUser", listMezonUser);
  return (
    <div className='w-full h-full'>
      <div className='h-[50px] p-2 flex items-center justify-between '>
        <div className='h-full flex items-center'>
          <span className='ml-2'>
            <Checkbox checked={isCheckAll} onClick={handleToogleCheckAll} />
          </span>
          <div className='ml-8'>
            <Select
              value={selectedClanRole ?? ""}
              onValueChange={(value) =>
                handleChangeSelectedClanRole({ target: { value } } as React.ChangeEvent<HTMLSelectElement>)
              }
            >
              <SelectTrigger className='w-[200px] bg-white text-gray-700 outline-none focus:outline-none'>
                <SelectValue className='text-gray-700 outline-none' placeholder='Select roles' />
              </SelectTrigger>
              <SelectContent className='bg-white w-[200px]'>
                <SelectGroup>
                  <SelectLabel>Roles</SelectLabel>
                  {mezonClanRoles?.map((role) => (
                    <SelectItem key={role.id} value={role.id}>
                      {role.title}
                    </SelectItem>
                  ))}
                </SelectGroup>
              </SelectContent>
            </Select>
          </div>
        </div>
        <div
          onClick={handleSaveListUser}
          className='w-[100px] h-[40px] relative cursor-pointer hover:scale-[0.99] transition-all active:scale-[1.0]'
        >
          <img className='w-full h-[40px]' src='/Buttons/Button.png' />
          <span className='absolute top-1 left-0 flex items-center justify-center w-full text-[20px] font-titan'>Save</span>
        </div>
      </div>
      <div className='flex-1 w-full h-[calc(100%-50px)] outline-none p-2 overflow-y-auto [&::-webkit-scrollbar]:w-[3px] [&::-webkit-scrollbar-thumb]:bg-red-500 [&::-webkit-scrollbar-thumb]:rounded-lg [&::-webkit-scrollbar-track]:bg-gray-200 bg-[#fdfdfd54] rounded-lg select-none'>
        {listMezonUser?.length > 0 &&
          listMezonUser?.map((player) => (
            <div
              key={player.id}
              className='flex items-center justify-between h-[50px] border-b border-gray-500 text-gray-800 font-titan'
            >
              <div className='flex items-center'>
                <span className='ml-2'>
                  <Checkbox checked={player.isSelected ?? false} onClick={() => handleCheckUser(player.id)} />
                </span>
                <div className='w-[270px] flex items-center ml-8'>
                  <img
                    className='w-[35px] h-[35px] rounded-full'
                    src={player.user.avatar_url ?? "https://mezon.ai/assets/images/mezon-logo-black.svg"}
                  />
                  <div className='ml-2 flex flex-col justify-center items-start flex-1 h-full w-full'>
                    <span className=''>{player.user?.display_name ?? player.user?.username}</span>
                    <span className='text-[12px] font-light text-gray-500'>
                      {mezonClanRoles
                        .filter((role) => player.role_id.includes(role.id))
                        ?.map((role) => role.title)
                        .join(", ")}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          ))}
      </div>
    </div>
  );
};
export default MezonUserTab;
