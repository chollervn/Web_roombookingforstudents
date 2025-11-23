import { useEffect, useState, useImperativeHandle, forwardRef, memo } from "react";

interface ICountdownTimeProps {
  initTime?: number;
}

const CountdownTime = forwardRef(({ initTime = 0 }: ICountdownTimeProps, ref) => {
  const [timeCountDown, setTimeCountDown] = useState<number | null>(initTime);
  const [isActive, setIsActive] = useState(false);

  // Forward ref method
  useImperativeHandle(ref, () => ({
    startCountdown: () => {
      setTimeCountDown(initTime);
      setIsActive(true);
    },
    resetCountdown: () => {
      setTimeCountDown(initTime);
      setIsActive(false);
    },
  }));

  useEffect(() => {
    setTimeCountDown(initTime);
  }, [initTime]);

  useEffect(() => {
    let interval: NodeJS.Timeout | null = null;

    if (isActive && timeCountDown !== null) {
      interval = setTimeout(() => {
        if (timeCountDown > 0) {
          setTimeCountDown(timeCountDown - 1);
        } else {
          setIsActive(false);
        }
      }, 1000);
    }

    return () => {
      if (interval) clearTimeout(interval);
    };
  }, [isActive, timeCountDown]);

  return (
    <div className='flex justify-center items-center gap-1 text-[30px] font-titan text-white absolute top-1 left-1/2 transform -translate-x-1/2 '>
      <span className='flex justify-center items-center w-[50px]'>
        {timeCountDown !== null && Math.floor(timeCountDown / 60) >= 0 ? `0${Math.floor(timeCountDown / 60)}`.slice(-2) : "00"}
      </span>
      <span>:</span>
      <span className='flex justify-center items-center w-[50px]'>
        {timeCountDown !== null && timeCountDown % 60 >= 0 ? `0${timeCountDown % 60}`.slice(-2) : "00"}
      </span>
    </div>
  );
});

export default memo(CountdownTime);
