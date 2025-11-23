const Loading = () => {
  return (
    <div className='fixed top-0 left-0 w-full h-full bg-[#131b1beb] z-[1000] flex justify-center items-center'>
      <span className='inline-block w-[70px] h-[70px] border-[10px] border-white rounded-full border-b-[#ff3d00] animate-spin'></span>
    </div>
  );
};

export default Loading;
