import { Outlet } from "react-router-dom";

const MainLayout: React.FC = () => {
  return (
    <div className='max-w-[950px]  sm:max-h-[550px] shadow-indigo-500 shadow-2xl  rounded-lg w-full h-full bg-[url("/bg-main.png")] bg-cover bg-center bg-no-repeat '>
      <div className='h-full w-full bg-[#21107266] rounded-lg relative'>
        {/* <Header /> */}
        <div className='overflow-y-auto pt-[55px] sm:pt-0 h-[calc(100%-0px)] [&::-webkit-scrollbar]:hidden [-ms-overflow-style:none] [scrollbar-width:none] relative'>
          <Outlet />
        </div>
        {/* <Footer /> */}
      </div>
    </div>
  );
};
export default MainLayout;
