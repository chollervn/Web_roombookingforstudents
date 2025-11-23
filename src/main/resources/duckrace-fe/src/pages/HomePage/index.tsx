import UserInformation from "@/components/UserInformation";
import { ROUTES } from "@/routes/path.ts";
import { useNavigate } from "react-router-dom";

const HomePage = () => {
  const navigate = useNavigate();
  const handleStartGame = () => {
    navigate(ROUTES.ROOM);
  };
  return (
    <div className='h-full w-full bg-gradient-to-b from-teal-100 via-teal-300 to-teal-200 bg-center bg-no-repeat rounded-lg p-2 relative flex justify-center items-center '>
      <div className='fixed top-3 right-3'>
        <UserInformation />
      </div>
      <div className='flex justify-center items-center relative w-[400px] sm:w-[600px] h-[600px]'>
        <img src='/bg-home.webp' alt='duck' className='w-full max-w-[400px] sm:max-w-[600px]' />
        <span
          onClick={handleStartGame}
          className='font-titan absolute text-[50px] sm:text-[65px] top-[145px] sm:top-[75px] left-[85px] sm:left-[130px] rotate-[-4deg] cursor-pointer hover:text-blue-400 select-none transition-all active:scale-[1.02]'
        >
          Start
        </span>
        <span className='font-titan absolute text-[35px] sm:text-[55px] top-[230px] left-[70px] sm:top-[192px] sm:left-[90px] rotate-[-4deg] select-none'>
          Duck{" "}
        </span>
        <span className='font-titan absolute text-[35px] sm:text-[55px] top-[255px] left-[125px] sm:top-[240px] sm:left-[170px] rotate-[-4deg] select-none'>
          Race
        </span>
      </div>
    </div>
  );
};
export default HomePage;
