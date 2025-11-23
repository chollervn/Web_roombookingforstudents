import useGameStore from "@/stores/gameStore.ts";
import { useEffect, useRef, useState } from "react";
import { Outlet } from "react-router-dom";

const SoundLayout = () => {
  const { gameStatus, isCompletedAll } = useGameStore();
  const audioBgRef = useRef<HTMLAudioElement>(null);
  const audioStartBet = useRef<HTMLAudioElement>(null);
  const audioStartGame = useRef<HTMLAudioElement>(null);
  const audioDuckRef = useRef<HTMLAudioElement>(null);
  const audioVictoryRef = useRef<HTMLAudioElement>(null);

  const [isPlayBgSound, setIsPlayBgSound] = useState(false);
  useEffect(() => {
    setTimeout(() => {
      setIsPlayBgSound(true);
    }, 1000);
  }, []);

  useEffect(() => {
    if (!audioBgRef.current) return;
    if (!isPlayBgSound) return;
    const playAudio = async () => {
      try {
        if (audioBgRef.current) audioBgRef.current.volume = 0.05;
        await audioBgRef.current?.play();
      } catch (err) {
        console.error("Autoplay failed", err);
      }
    };
    playAudio();
  }, [isPlayBgSound]);

  useEffect(() => {
    if (!audioStartBet.current) return;
    const playAudio = async () => {
      try {
        if (gameStatus === "betting") {
          if (audioStartBet.current) {
            audioStartBet.current.volume = 0.1;
            audioStartBet.current.currentTime = 0;
          }
          await audioStartBet.current?.play();

          await audioBgRef.current?.pause();

          setTimeout(() => {
            audioStartBet.current?.pause();
            if (audioStartBet.current) audioStartBet.current.currentTime = 0;
          }, 32000);
        }
      } catch (err) {
        console.error("Autoplay failed:", err);
      }
    };
    playAudio();
  }, [gameStatus]);

  useEffect(() => {
    if (!audioStartGame.current) return;

    let duckInterval: NodeJS.Timeout;
    const playAudio = async () => {
      try {
        if (gameStatus === "racing") {
          if (audioStartBet.current) audioStartBet.current.volume = 0.03;
          if (audioStartGame.current) audioStartGame.current.volume = 0.05;

          await audioStartGame.current?.play();

          // Lặp lại mỗi 6 giây
          duckInterval = setInterval(() => {
            if (audioDuckRef.current) {
              audioDuckRef.current.volume = 0.2;
              audioDuckRef.current.currentTime = 0;
              audioDuckRef.current.play();
            }

            // Phát lại sau 1 giây
            setTimeout(() => {
              if (audioDuckRef.current) {
                audioDuckRef.current.currentTime = 0;
                audioDuckRef.current.play();
              }
            }, 1000);
          }, 6000);
        }
      } catch (err) {
        console.error("Autoplay failed:", err);
      }
    };

    playAudio();

    return () => {
      // Cleanup interval nếu gameStatus thay đổi
      clearInterval(duckInterval);
    };
  }, [gameStatus]);

  useEffect(() => {
    if (!audioVictoryRef.current) return;
    const playAudio = async () => {
      try {
        if (isCompletedAll) {
          // if (audioVictoryRef.current) audioVictoryRef.current.volume = 0.05;
          await audioVictoryRef.current?.play();
          if (audioBgRef.current) audioBgRef.current.play();
          if (audioStartBet.current) {
            audioStartBet.current.currentTime = 0;
            audioStartBet.current.pause();
          }
          if (audioStartGame.current) {
            audioStartGame.current.currentTime = 0;
            audioStartGame.current.pause();
          }
          if (audioDuckRef.current) {
            audioDuckRef.current.currentTime = 0;
            audioDuckRef.current.pause();
          }
          setIsPlayBgSound(true);

          setTimeout(() => {
            if (audioVictoryRef.current) audioVictoryRef.current.currentTime = 0;
          }, 6000);
        }
      } catch (err) {
        console.error("Autoplay failed:", err);
      }
    };
    playAudio();
  }, [isCompletedAll]);

  return (
    <>
      <Outlet />
      <audio ref={audioBgRef} loop>
        <source src='/Sounds/background-sound.mp3' type='audio/mp3' />
      </audio>
      <audio ref={audioStartGame} loop>
        <source src='/Sounds/start-racing-game.mp3' type='audio/mp3' />
      </audio>
      <audio ref={audioDuckRef}>
        <source src='/Sounds/quacking-duck.mp3' type='audio/mp3' />
      </audio>
      <audio ref={audioStartBet}>
        <source src='/Sounds/start-bet.mp3' type='audio/mp3' />
      </audio>
      <audio ref={audioVictoryRef}>
        <source src='/Sounds/victory-game.mp3' type='audio/mp3' />
      </audio>
    </>
  );
};
export default SoundLayout;
