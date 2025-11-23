import { SocketEvents } from "@/constants/SocketEvents.ts";
import { IGameResult } from "@/interface/game/Game.ts";
import { useSocket } from "@/providers/SocketProvider.tsx";
import useGameStore from "@/stores/gameStore.ts"; // Giả sử bạn có store để quản lý state
import useRoomStore from "@/stores/roomStore.ts";
import useUserStore from "@/stores/userStore.ts";
import { useEffect, useRef } from "react";

const ListPlayer = () => {
  const socket = useSocket();
  const { currentUser, changeWallet } = useUserStore();
  const canvasRef = useRef<HTMLCanvasElement | null>(null);
  const duckImagesRef = useRef<HTMLImageElement[]>([]);
  const backgroundImageRef1 = useRef<HTMLImageElement | null>(null); // Lưu hình ảnh nền
  const backgroundImageRef2 = useRef<HTMLImageElement | null>(null); // Lưu hình ảnh nền
  const endGameLineRef = useRef<HTMLImageElement | null>(null); // Lưu hình ảnh nền
  const startGameLineRef = useRef<HTMLImageElement | null>(null); // Lưu hình ảnh nền
  const background1Position = useRef<number>(0); // Lưu vị trí của background 1
  const background2Position = useRef<number>(0); // Lưu vị trí của background 2
  const endGameLinePosition = useRef<number | null>(null); // Lưu vị trí của background 2
  const startGameLinePosition = useRef<number | null>(null); // Lưu vị trí của background 2
  const players = useRoomStore((state) => state.listDucks); // Lấy danh sách player từ store
  const isResetGame = useGameStore((state) => state.isResetGame); // Lấy trạng thái reset vị trí từ store
  const setIsCompletedAll = useGameStore((state) => state.setIsCompletedAll); // Lấy danh sách player từ store
  const setGameStatus = useGameStore((state) => state.setGameStatus); // Lấy danh sách player từ store
  const isResetRef = useRef<boolean>(false); // Lưu trạng thái reset vị trí
  const isEndGameRef = useRef<boolean>(false); // Lưu trạng thái kết thúc game
  const isRacingRef = useRef<boolean>(false); // Lưu trạng thái đua của game
  const resetGameRef = useRef<boolean>(false); // Lưu trạng thái reset game
  const gameStatus = useGameStore((state) => state.gameStatus); // Lấy trạng thái game từ store

  const { isRacing, setGameResult } = useGameStore(); // Lấy trạng thái đua của game từ store

  // Dùng ref để lưu vị trí của các vịt
  const duckPositionsRef = useRef<Map<number, { x: number; y: number; v: number; isReset: boolean; duckIcon: number }>>(
    new Map()
  );
  const totalPlayerRef = useRef<number>(0); // Lưu số lượng player

  useEffect(() => {
    if (!socket) return;
    socket.on(SocketEvents.ON.END_GAME_SUCCESS, (data: IGameResult) => {
      isEndGameRef.current = true;
      setGameStatus("completed");
      setGameResult(data);
      if (currentUser?.wallet === undefined) return;
      if (data.winners?.length > 0 && data.winners?.includes(currentUser?.id ?? "")) {
        changeWallet(currentUser.wallet + data.winBet);
        return;
      }
      if (data.bettors?.length > 0 && data.winners.length === 0 && data.bettors?.includes(currentUser?.id ?? "")) {
        changeWallet(currentUser.wallet + data.winBet);
        return;
      }
    });
    return () => {
      socket.off(SocketEvents.ON.END_GAME_SUCCESS);
    };
  }, [changeWallet, currentUser?.id, currentUser.wallet, setGameResult, setGameStatus, socket]);

  useEffect(() => {
    resetGameRef.current = isResetGame;
  }, [isResetGame]);
  // Tải hình ảnh vịt và hình ảnh nền
  useEffect(() => {
    const maxIcons = 10;
    const duckImages: HTMLImageElement[] = [];
    for (let i = 1; i <= maxIcons; i++) {
      const img = new Image();
      img.src = `/duck-${i}.png`;
      duckImages.push(img);
    }
    duckImagesRef.current = duckImages;

    const backgroundImage1 = new Image();
    backgroundImage1.src = "/bg-4.png"; // Đường dẫn tới hình ảnh nền
    backgroundImageRef1.current = backgroundImage1;

    const backgroundImage2 = new Image();
    backgroundImage2.src = "/bg-5.png"; // Đường dẫn tới hình ảnh nền
    backgroundImageRef2.current = backgroundImage2;

    const endGameLineImage = new Image();
    endGameLineImage.src = "/vach-dich.jpg"; // Đường dẫn tới hình ảnh nền
    endGameLineRef.current = endGameLineImage;

    const startGameLineImage = new Image();
    startGameLineImage.src = "/vach-dich.jpg"; // Đường dẫn tới hình ảnh nền
    startGameLineRef.current = startGameLineImage;
  }, []); // Chỉ tải hình ảnh một lần khi component được mount

  // Cập nhật vị trí và tốc độ của vịt mỗi khi players thay đổi
  useEffect(() => {
    players.forEach((player, index) => {
      const duckPosition = duckPositionsRef.current.get(index) || {
        x: 0,
        y: index,
        v: 0, // Khởi tạo tốc độ là 0 khi không có sự thay đổi\
        isReset: false,
        duckIcon: player.colorNumber,
      };

      // Tính toán tốc độ dựa trên sự thay đổi của điểm số (newScore - oldScore)
      const speed = player.score.newScore - player.score.oldScore; // Tính tốc độ\
      duckPosition.v = speed; // Cập nhật lại tốc độ của
      // duckPosition.v = player.score.newScore >= 110 ? 0 : speed; // Cập nhật lại tốc độ của vịt

      duckPositionsRef.current.set(index, duckPosition); // Lưu lại vị trí và tốc độ
    });
    totalPlayerRef.current = players.length; // Lưu lại số lượng player
  }, [players]); // Khi players thay đổi, sẽ cập nhật lại vị trí và tốc độ của vịt

  useEffect(() => {
    if (isRacingRef) {
      isRacingRef.current = isRacing ?? false;
    }
    if (isRacing) {
      setIsCompletedAll(false);
    }
  }, [isRacing, setIsCompletedAll]);
  useEffect(() => {
    if (gameStatus !== "completed") {
      isEndGameRef.current = false;
    }
  }, [gameStatus]);
  useEffect(() => {
    const canvas = canvasRef.current;
    const context = canvas?.getContext("2d");
    if (!context || !backgroundImageRef1.current || !canvas) return;
    const ratio = 1;
    const resizeWindow = () => {
      canvas.width = window.innerWidth * ratio;
      canvas.height = window.innerHeight * ratio;
      canvas.style.width = `${window.innerWidth}px`;
      canvas.style.height = `${window.innerHeight}px`;
    };
    resizeWindow(); // Gọi hàm resizeWindow khi component được mount
    window.addEventListener("resize", resizeWindow); // Gọi hàm resizeWindow khi kích thước cửa sổ thay đổi

    if (context) {
      context.scale(ratio, ratio);
    }
    let lastTime = 0; // Lưu lại thời gian của lần vẽ trước

    const animateDucks = (timestamp: number) => {
      const deltaTime = timestamp - lastTime; // Thời gian trôi qua từ lần vẽ trước
      if (deltaTime < 16) {
        // Giới hạn tốc độ tối đa của vịt
        requestAnimationFrame(animateDucks);
        return;
      }
      lastTime = timestamp;

      context.clearRect(0, 0, canvas?.width ?? 0, canvas?.height ?? 0); // Xóa canvas

      // Vẽ background di chuyển
      const backgroundSpeed = 0.5; // Tốc độ chuyển động của background
      let background1X = background1Position.current - backgroundSpeed - (isResetRef.current ? 1 : 0);
      let background2X = background2Position.current - backgroundSpeed - (isResetRef.current ? 1 : 0);

      if (backgroundImageRef1.current && backgroundImageRef2.current) {
        // Vẽ ảnh nền từ bên trái
        context.drawImage(backgroundImageRef1.current, background1X, 0, canvas.width + 10, canvas.height); // Background 1
        context.drawImage(backgroundImageRef2.current, background2X + canvas.width, 0, canvas.width + 10, canvas.height); // Background 2
      }

      // Kiểm tra và reset lại vị trí của background khi nó ra ngoài canvas
      if (background1X <= -canvas.width) {
        background1X = canvas.width; // Reset background1 về vị trí bên phải
      }
      if (background2X <= -2 * canvas.width) {
        background2X = 0; // Reset background2 về vị trí bên phải
      }

      // Cập nhật vị trí của background
      background1Position.current = background1X;
      background2Position.current = background2X;

      // Vẽ vạch đích
      const endGameLineSpeed = 0.5;
      if (endGameLineRef.current && isEndGameRef.current) {
        let endGameLineX = (endGameLinePosition.current ?? canvas.width) - endGameLineSpeed - (isResetRef.current ? 1 : 0);
        if (endGameLineRef.current) {
          context.drawImage(endGameLineRef.current, endGameLineX, canvas.height / 5, 30, canvas.height);
        }
        if (endGameLineX <= canvas.width - canvas.height / 5) {
          endGameLineX = canvas.width - canvas.height / 5;
        }
        endGameLinePosition.current = endGameLineX;
      }

      // Vẽ vạch bắt đầu
      const startGameLineSpeed = 0.5;
      if (startGameLineRef.current) {
        let startGameLineX = (startGameLinePosition.current ?? 60) - startGameLineSpeed;
        if (startGameLineRef.current) {
          context.drawImage(startGameLineRef.current, startGameLineX, canvas.height / 5, 30, canvas.height);
        }
        if (startGameLineX <= 60 && !isRacingRef.current) {
          startGameLineX = 60;
        }
        startGameLinePosition.current = startGameLineX;
      }

      let greaterThan0 = 0;
      duckPositionsRef.current.forEach((duckPosition) => {
        if (duckPosition.x > canvas.width / 2) {
          isResetRef.current = true;
        }
        if (duckPosition.x > 0) {
          greaterThan0++;
        }
      });
      if (greaterThan0 <= 5) {
        isResetRef.current = false;
      }
      let completedGameCount = 0;
      // Lặp qua danh sách player để vẽ và cập nhật vị trí của mỗi vịt
      duckPositionsRef.current.forEach((duckPosition, playerId) => {
        // Tính tốc độ dựa trên điểm số
        const speed = duckPosition.v * 0.2; // Tốc độ được lấy từ v
        const olePosition = duckPosition.x;
        duckPosition.x =
          duckPosition.x + (isEndGameRef.current ? 2 : speed - 4 * 0.05 - (isResetRef.current && speed !== 0 ? 1 : 0)); // Cập nhật vị trí theo tốc độ
        duckPosition.y = canvas.height / 5 + playerId * ((canvas.height - 180) / totalPlayerRef.current); // Vị trí dọc của vịt

        // Nếu vịt vượt quá chiều rộng của canvas, reset lại vị trí
        if (duckPosition.x > canvas.width - 100) {
          completedGameCount++;
          duckPosition.x = canvas.width - 100; // Đặt lại vị trí x của vịt (cộng thêm một chút để bắt đầu từ ngoài màn hình)
        }

        if (!isRacingRef.current && duckPosition.x <= 5) {
          duckPosition.x = 5;
        }
        if (!isRacingRef.current) {
          duckPosition.x = olePosition;
        }
        if (resetGameRef.current) {
          duckPosition.x = 5;
        }
        // Cập nhật lại vị trí vịt trong ref
        duckPositionsRef.current.set(playerId, duckPosition);

        const duckImage = duckImagesRef.current[duckPosition.duckIcon - 1];
        // Vẽ vịt lên canvas
        if (duckImage) {
          context.drawImage(duckImage, duckPosition.x, duckPosition.y, 60, 60); // Vịt có kích thước 50x50px
        }
        context.font = "12px Arial";
        context.fillStyle = "black";
        context.textAlign = "center";
        context.fillText(`${playerId + 1}`, duckPosition.x + 31, duckPosition.y + 47);
      });
      if (completedGameCount === totalPlayerRef.current && completedGameCount !== 0) {
        setIsCompletedAll(true);
      }

      requestAnimationFrame(animateDucks); // Tiếp tục vẽ lại vịt
    };

    requestAnimationFrame(animateDucks); // Bắt đầu vẽ vịt

    return () => {
      cancelAnimationFrame(lastTime);
      window.removeEventListener("resize", resizeWindow); // Hủy bỏ sự kiện khi component bị unmount
    };
  }, []);

  return (
    <div className='relative bg-[#21107266] rounded-lg '>
      <canvas ref={canvasRef} className='border border-gray-500 rounded w-[100vw] h-[100vh] m-0 p-0 block' />
    </div>
  );
};

export default ListPlayer;
