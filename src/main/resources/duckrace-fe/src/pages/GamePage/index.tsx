/* eslint-disable @typescript-eslint/no-explicit-any */
import Loading from "@/components/Loading";
import UserInformation from "@/components/UserInformation";
import { SocketEvents } from "@/constants/SocketEvents.ts";
import { AppResponse } from "@/interface/app/AppResponse.ts";
import {
  BetForDuckResponse,
  BettorOfDucks,
  DuckPicked,
} from "@/interface/game/Game.ts";
import { IDuck, IMezonClan, IMezonUser, Room } from "@/interface/room/Room.ts";
import { User } from "@/interface/user/User.ts";
import { useSocket } from "@/providers/SocketProvider.tsx";
import { ROUTES } from "@/routes/path.ts";
import useGameStore from "@/stores/gameStore.ts";
import useRoomStore from "@/stores/roomStore.ts";
import useUserStore from "@/stores/userStore.ts";
import { MezonAppEvent, MezonWebViewEvent } from "@/types/webview";
import { useEffect, useRef, useState } from "react";
import { useLocation, useNavigate, useParams } from "react-router-dom";
import { toast } from "react-toastify";
import CountdownTime from "./components/CountdownTime.tsx";
import ListPlayer from "./ListPlayer.tsx";
import ModalBet from "./ModalBet";
import ModalRoomInfomation from "./ModalRoomInfomation";
import ModalSetup from "./ModalSetup";
import ModalShowRank from "./ModalShowRank";
import ModalShowResult from "./ModalShowResult";
import ModalUser from "./ModalShowUser";

const GamePage = () => {
  const socket = useSocket();
  const location = useLocation();
  const searchParams = new URLSearchParams(location.search);
  const gameId = searchParams.get("gameId");
  const { changeWallet, currentUser } = useUserStore();
  const { roomId } = useParams();
  const navigate = useNavigate();
  const listDuckPickedRef = useRef<DuckPicked[]>([]);
  const {
    setCurrentGame,
    setOpenModalBet,
    setIsRacing,
    setIsResetGame,
    setGameStatus,
    setIsCompletedAll,
    setListBettorOfDucks,
    isRacing,
    gameStatus,
    setListDuckPicked,
    setIsConfirmedBet,
    listDuckPicked,
  } = useGameStore();
  const {
    setCurrentRoom,
    setAddDuckText,
    currentRoom,
    setOpenModalShowUser,
    listDucks,
    setMezonClanRoles,
    setMezonClanUsers,
    setSelectedClanRole,
    setListDucks,
    setTabs,
    setTotalDucks,
    setOpenModalShowRank,
    setOpenModalShowResult,
    setRoomMembers,
  } = useRoomStore();
  const [listDuckToShow, setListDuckToShow] = useState<IDuck[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [isOpenPopoverConfirmBet, setIsOpenPopoverConfirmBet] =
    useState<boolean>(false);
  useEffect(() => {
    if (!socket || !roomId) return;
    socket.emit(SocketEvents.EMIT.GET_ROOM_INFO, roomId);
    socket.on(
      SocketEvents.ON.GET_ROOM_INFO_SUCCESS,
      (data: AppResponse<Room>) => {
        setCurrentRoom(data.data as Room);
        setListDucks(data.data?.ducks || []);
        setTotalDucks(data.data?.totalDuck || 0);

        setIsRacing(false);
        setIsResetGame(true);
        setGameStatus("waiting");
        setIsCompletedAll(false);
        setOpenModalShowRank(false);
        setOpenModalShowResult(false);
        setListDuckPicked([]);
        setIsConfirmedBet(false);

        navigate(
          `${ROUTES.ROOM_DETAIL.replace(
            ":roomId",
            (data.data as Room)?.roomId
          )}?gameId=${(data.data as Room)?.currentGame}`
        );
      }
    );
    socket.on(SocketEvents.ON.GET_ROOM_INFO_FAILED, () => {});
    socket.on(
      SocketEvents.ON.GET_MEMBER_OF_ROOM_SUCCESS,
      (data: AppResponse<User[]>) => {
        setRoomMembers(data.data || []);
      }
    );
    socket.on(SocketEvents.ON.GET_MEMBER_OF_ROOM_FAILED, () => {});
    socket.on(
      SocketEvents.ON.GET_DUCKS_OF_ROOM_SUCCESS,
      (data: AppResponse<IDuck[]>) => {
        setListDucks(data.data || []);
        setTotalDucks(data.data?.length || 0);
      }
    );
    socket.on(SocketEvents.ON.GET_DUCKS_OF_ROOM_FAILED, () => {});
    socket.on(
      SocketEvents.ON.REMOVE_USER_FROM_GAME_SUCCESS,
      (data: AppResponse<Room>) => {
        setCurrentRoom(data.data as Room);
        setListDucks(data.data?.ducks || []);
        setTotalDucks(data.data?.totalDuck || 0);
      }
    );
    socket.on(SocketEvents.ON.REMOVE_USER_FROM_GAME_FAILED, () => {});
    socket.on(SocketEvents.ON.ADD_DUCK_TO_ROOM_FAILED, () => {});
    socket.on(SocketEvents.ON.ADD_DUCK_TO_ROOM_SUCCESS, () => {
      setAddDuckText("");
      setTabs("list-user");
    });
    socket.on(
      SocketEvents.ON.UPDATE_LIST_DUCK_OF_ROOM_SUCCESS,
      (data: AppResponse<Room>) => {
        setCurrentRoom(data.data as Room);
        setListDucks(data.data?.ducks || []);
        setTotalDucks(data.data?.totalDuck || 0);
        setTabs("list-user");
      }
    );
    socket.on(SocketEvents.ON.UPDATE_LIST_DUCK_OF_ROOM_FAILED, () => {});
    return () => {
      socket.off(SocketEvents.ON.GET_ROOM_INFO_SUCCESS);
      socket.off(SocketEvents.ON.GET_ROOM_INFO_FAILED);
      socket.off(SocketEvents.ON.GET_DUCKS_OF_ROOM_SUCCESS);
      socket.off(SocketEvents.ON.GET_DUCKS_OF_ROOM_FAILED);
      socket.off(SocketEvents.ON.REMOVE_USER_FROM_GAME_SUCCESS);
      socket.off(SocketEvents.ON.REMOVE_USER_FROM_GAME_FAILED);
      socket.off(SocketEvents.ON.ADD_DUCK_TO_ROOM_FAILED);
      socket.off(SocketEvents.ON.ADD_DUCK_TO_ROOM_SUCCESS);
      socket.off(SocketEvents.ON.UPDATE_LIST_DUCK_OF_ROOM_SUCCESS);
      socket.off(SocketEvents.ON.UPDATE_LIST_DUCK_OF_ROOM_FAILED);
    };
  }, [
    navigate,
    roomId,
    setCurrentRoom,
    setListDucks,
    setTotalDucks,
    socket,
    setAddDuckText,
    setTabs,
    setRoomMembers,
    setIsRacing,
    setIsResetGame,
    setGameStatus,
    setIsCompletedAll,
    setOpenModalShowRank,
    setOpenModalShowResult,
    setListDuckPicked,
    setIsConfirmedBet,
  ]);

  useEffect(() => {
    setOpenModalShowUser(true);
    setIsRacing(false);
    setIsCompletedAll(false);
  }, [setIsCompletedAll, setIsRacing, setOpenModalShowUser]);

  useEffect(() => {
    if (listDucks.length === 0) {
      return;
    }
    const newList = listDucks.sort(
      (a, b) => b.score.totalScore - a.score.totalScore
    );
    setListDuckToShow(newList.slice(0, 3));
  }, [listDucks]);

  useEffect(() => {
    if (!socket || !gameId) return;
    socket.on(SocketEvents.ON.LEFT_ROOM_SUCCESS, (data) => {
      setCurrentGame(null);
      setListDucks([]);
      setTotalDucks(0);
      navigate(ROUTES.ROOM);

      window.Mezon.WebView?.postEvent(
        "LEAVE_ROOM" as MezonWebViewEvent,
        {
          roomId: (data.data as Room).roomId,
        },
        () => {}
      );
    });

    socket.on(
      SocketEvents.ON.USER_LEFT_ROOM,
      (
        data: AppResponse<{
          userId: string;
          roomInfo: Room;
        }>
      ) => {
        setCurrentRoom(data.data?.roomInfo as Room);
      }
    );
    socket.on(SocketEvents.ON.OUT_ROOM_FAILED, () => {
      navigate(ROUTES.HOME);
    });

    socket.on(SocketEvents.ON.START_GAME_SUCCESS, (data: AppResponse<Room>) => {
      setListDucks(data.data?.ducks || []);
      setTotalDucks(data.data?.totalDuck || 0);
      setIsRacing(true);
      setIsResetGame(false);
      setGameStatus("racing");
      setIsCompletedAll(false);

      if (countdownRef.current) {
        countdownRef.current.startCountdown();
      }
    });
    socket.on(SocketEvents.ON.START_GAME_FAILED, () => {});
    socket.on(SocketEvents.ON.START_TURN_SUCCESS, (data: AppResponse<Room>) => {
      setListDucks(data.data?.ducks || []);
    });
    socket.on(SocketEvents.ON.START_TURN_FAILED, () => {});
    socket.on(
      SocketEvents.ON.CREATE_NEW_GAME_SUCCESS,
      (data: AppResponse<Room>) => {
        setCurrentRoom(data.data as Room);
        setListDucks(data.data?.ducks || []);
        setTotalDucks(data.data?.totalDuck || 0);
        setIsRacing(false);
        setIsResetGame(true);
        setGameStatus("waiting");
        setIsCompletedAll(false);
        setOpenModalShowRank(false);
        setOpenModalShowResult(false);
        setListDuckPicked([]);
        setIsConfirmedBet(false);
        navigate(
          `${ROUTES.ROOM_DETAIL.replace(
            ":roomId",
            (data.data as Room).roomId
          )}?gameId=${(data.data as Room).currentGame}`
        );
        if (countdownRef.current) {
          countdownRef.current.resetCountdown();
        }
      }
    );
    socket.on(SocketEvents.ON.CREATE_NEW_GAME_FAILED, () => {});
    socket.on(
      SocketEvents.ON.CHANGE_TIME_SUCCESS,
      (data: AppResponse<Room>) => {
        setCurrentRoom(data.data as Room);
      }
    );
    socket.on(SocketEvents.ON.CHANGE_TIME_FAILED, () => {});
    socket.on(SocketEvents.ON.START_BET_SUCCESS, () => {
      setGameStatus("betting");
      setOpenModalBet(true);
    });
    socket.on(SocketEvents.ON.START_BET_FAILED, () => {});
    socket.on(SocketEvents.ON.END_BET, () => {
      setOpenModalBet(false);
    });
    socket.on(SocketEvents.ON.START_GAME_NOW, () => {
      socket.emit(SocketEvents.EMIT.START_GAME, {
        gameId,
        roomId,
        userId: currentUser?.id,
      });
      setGameStatus("racing");
    });
    socket.on(
      SocketEvents.ON.GET_GAME_BETTORS_SUCCESS,
      (data: AppResponse<BettorOfDucks[]>) => {
        setListBettorOfDucks(data.data || []);
      }
    );
    socket.on(
      SocketEvents.ON.BET_FOR_DUCK_SUCCESS,
      (data: AppResponse<BetForDuckResponse>) => {
        setIsOpenPopoverConfirmBet(false);
        toast.success("You have bet for duck successfully");
        if (currentUser.wallet && data.data?.betAmount) {
          changeWallet(currentUser.wallet - data.data?.betAmount);
        }
        setIsConfirmedBet(true);
      }
    );
    return () => {
      socket.off(SocketEvents.ON.START_GAME_SUCCESS);
      socket.off(SocketEvents.ON.START_TURN_FAILED);
      socket.off(SocketEvents.ON.START_TURN_SUCCESS);
      socket.off(SocketEvents.ON.START_GAME_FAILED);
      socket.off(SocketEvents.ON.LEFT_ROOM_SUCCESS);
      socket.off(SocketEvents.ON.LEFT_ROOM_FAILED);
      socket.off(SocketEvents.ON.CREATE_NEW_GAME_SUCCESS);
      socket.off(SocketEvents.ON.CREATE_NEW_GAME_FAILED);
      socket.off(SocketEvents.ON.CHANGE_TIME_SUCCESS);
      socket.off(SocketEvents.ON.CHANGE_TIME_FAILED);
      socket.off(SocketEvents.ON.START_BET_SUCCESS);
      socket.off(SocketEvents.ON.START_BET_FAILED);
      socket.off(SocketEvents.ON.END_BET);
      socket.off(SocketEvents.ON.START_GAME_NOW);
      socket.off(SocketEvents.ON.GET_GAME_BETTORS_SUCCESS);
      socket.off(SocketEvents.ON.USER_LEFT_ROOM);
      socket.off(SocketEvents.ON.BET_FOR_DUCK_SUCCESS);
    };
  }, [
    setListDucks,
    setTotalDucks,
    socket,
    gameId,
    setCurrentGame,
    navigate,
    setIsRacing,
    setIsResetGame,
    setGameStatus,
    setIsCompletedAll,
    setOpenModalBet,
    roomId,
    currentUser?.id,
    setListBettorOfDucks,
    setCurrentRoom,
    setOpenModalShowRank,
    setOpenModalShowResult,
    setListDuckPicked,
    setIsConfirmedBet,
    setIsOpenPopoverConfirmBet,
    currentUser.wallet,
    changeWallet,
  ]);

  useEffect(() => {
    window.Mezon.WebView?.postEvent(
      "GET_CLAN_USERS" as MezonWebViewEvent,
      {},
      () => {}
    );
    window.Mezon.WebView?.onEvent(
      "CLAN_USERS_RESPONSE" as MezonAppEvent,
      (_, data?: IMezonUser[]) => {
        if (!data) return;
        const users: IMezonUser[] = data.map((user: any) => ({
          id: user.id,
          role_id: user.role_id ?? [],
          userChannelId: user.userChannelId,
          user: {
            display_name: user?.display_name,
            username: user?.username,
            avatar_url: user?.avatar_url,
          },
          isSelected: false,
        }));
        setMezonClanUsers(users);
      }
    );
    window.Mezon.WebView?.postEvent(
      "GET_CLAN_ROLES" as MezonWebViewEvent,
      {},
      () => {}
    );
    window.Mezon.WebView?.onEvent(
      "CLAN_ROLES_RESPONSE" as MezonAppEvent,
      (_, data) => {
        setMezonClanRoles(data as IMezonClan[]);
        if ((data as IMezonClan[]).length > 0) {
          setSelectedClanRole((data as IMezonClan[])[0].id);
        }
      }
    );
  }, [setMezonClanRoles, setMezonClanUsers, setSelectedClanRole]);

  useEffect(() => {
    listDuckPickedRef.current = listDuckPicked;
  }, [listDuckPicked]);
  useEffect(() => {
    if (!socket) return;
    window.Mezon.WebView?.onEvent(
      "SEND_TOKEN_RESPONSE_SUCCESS" as MezonAppEvent,
      () => {
        socket.emit(SocketEvents.EMIT.BET_FOR_DUCK, {
          ducks: listDuckPickedRef.current?.map((duck) => duck.duckId),
          gameId: currentRoom?.currentGame,
          userId: currentUser.id,
          betAmount: currentRoom?.roomInfo.roomBet ?? 1,
          roomId: currentRoom?.roomId,
        });
        setIsConfirmedBet(true);
      }
    );
    window.Mezon.WebView?.onEvent(
      "SEND_TOKEN_RESPONSE_FAILED" as MezonAppEvent,
      (_, response) => {
        console.log("send token failed", response);
      }
    );
    return () => {
      window.Mezon.WebView?.offEvent(
        "SEND_TOKEN_RESPONSE_SUCCESS" as MezonAppEvent,
        () => {}
      );
      window.Mezon.WebView?.offEvent(
        "SEND_TOKEN_RESPONSE_FAILED" as MezonAppEvent,
        () => {}
      );
    };
  }, [
    currentRoom?.currentGame,
    currentRoom?.roomId,
    currentRoom?.roomInfo.roomBet,
    currentUser.id,
    setIsConfirmedBet,
    socket,
  ]);

  useEffect(() => {
    setIsLoading(true);
    const timeOut = setTimeout(() => {
      setIsLoading(false);
    }, 3000);
    return () => {
      clearTimeout(timeOut);
    };
  }, []);
  const countdownRef = useRef<{
    startCountdown: () => void;
    resetCountdown: () => void;
  }>(null);

  const handleStartGame = () => {
    if (!socket) return;
    if (gameStatus !== "waiting") {
      toast.warning("Game is racing, please wait for the next game");
      return;
    }
    if (currentRoom?.ownerId !== currentUser.id) {
      toast.warning("You are not the owner of this room");
      return;
    }
    if (listDucks.length < 6) {
      toast.warning("You need at least 6 ducks to start the game");
      return;
    }
    socket.emit(SocketEvents.EMIT.START_BET, {
      gameId,
      roomId,
      userId: currentUser?.id,
    });
  };

  const handleResetGame = () => {
    if (!socket) return;
    if (gameStatus === "racing") {
      toast.warning("Game is racing, you can't reset game");
      return;
    }
    if (currentRoom?.ownerId !== currentUser.id) {
      toast.warning("You are not the owner of this room");
      return;
    }
    socket.emit(SocketEvents.EMIT.CREATE_NEW_GAME, {
      roomId,
      userId: currentUser?.id,
    });
  };

  const handleOutGame = () => {
    if (!socket) return;
    socket.emit(SocketEvents.EMIT.LEFT_ROOM, {
      roomId,
      userId: currentUser?.id,
    });
  };
  return (
    <div className="h-full w-full bg-[#21107266] rounded-lg relative">
      <ListPlayer />
      {isLoading && <Loading />}
      <div className="absolute top-2 left-0 w-full flex justify-center items-center">
        <div
          onClick={handleOutGame}
          className="w-[60px] h-[60px] flex justify-center items-center cursor-pointer absolute top-0 left-2 hover:scale-[0.98] transition-all active:scale-[1.0]"
        >
          <img src="/Buttons/SmallButton-pressed.png" />
          <img
            className="w-[30px] absolute top-[12px] left-[12px]"
            src="/Icons/ExitIcon.png"
          />
        </div>
        <ModalRoomInfomation />
        <div
          className="w-[500px] h-[150px] flex justify-center items-center absolute top-0 left-[50%] translate-x-[-50%]"
          style={{ filter: "drop-shadow(0 0 .3rem rgba(124, 6, 226, .874))" }}
        >
          <img className="w-full h-full" src="/Window/SmallSubstrate.png" />
          <div className="w-[150px] h-[55px] justify-center items-center left-[10px] absolute top-[20px] ">
            <img className="w-full h-full" src="/Buttons/Button-hover.png" />
            <CountdownTime
              ref={countdownRef}
              initTime={currentRoom?.expiredTime}
            />
          </div>
          {isRacing ? (
            <button
              onClick={handleResetGame}
              disabled={gameStatus === "racing"}
              className="w-[150px] h-[55px] cursor-pointer justify-center items-center left-[10px] absolute bottom-[10px] filter hover:drop-shadow-[0_0_0.1rem_rgba(124,6,226,0.874)] transition-all active:drop-shadow-[0_0_0.2rem_rgba(124,6,226,0.874)]"
            >
              <img
                className="w-full h-full"
                src={
                  gameStatus !== "waiting" ||
                  currentRoom?.ownerId !== currentUser.id
                    ? "/Buttons/Button-disabled.png"
                    : "/Buttons/Button.png"
                }
              />
              <div className="flex  justify-center items-center gap-2 text-[30px] font-titan text-white absolute top-1 left-1/2 transform -translate-x-1/2 ">
                <img className="w-[20px] " src="/Icons/RefreshIcon.png" />
                <span>Reset</span>
              </div>
            </button>
          ) : (
            <button
              onClick={handleStartGame}
              disabled={gameStatus !== "waiting"}
              className="w-[150px] h-[55px] cursor-pointer justify-center items-center left-[10px] absolute bottom-[10px] filter hover:drop-shadow-[0_0_0.1rem_rgba(124,6,226,0.874)] transition-all active:drop-shadow-[0_0_0.2rem_rgba(124,6,226,0.874)]"
            >
              <img
                className="w-full h-full"
                src={
                  gameStatus !== "waiting" ||
                  currentRoom?.ownerId !== currentUser.id
                    ? "/Buttons/Button-disabled.png"
                    : "/Buttons/Button.png"
                }
              />
              <div className="flex  justify-center items-center gap-2 text-[30px] font-titan text-white absolute top-1 left-1/2 transform -translate-x-1/2 ">
                <img className="w-[20px] " src="/Icons/PlayIcon.png" />
                <span>Start</span>
              </div>
            </button>
          )}
          <div
            style={{ filter: "drop-shadow(0 0 .3rem rgba(124, 6, 226, .874))" }}
            className="w-[300px] h-[130px] justify-center items-center right-[10px] absolute top-[50%] translate-y-[-50%] "
          >
            <img className="w-full h-full" src="/Window/SmallSubstrate.png" />
            <div className=" items-center w-full py-1 px-3 text-[20px] font-titan text-white absolute top-2 left-1/2 transform -translate-x-1/2 ">
              {gameStatus !== "waiting" &&
                listDuckToShow?.map((player, index) => (
                  <div
                    key={index}
                    className="flex items-center gap-2 text-gray-500"
                  >
                    <div className="bg-[url('/Icons/StarIcon.png')] flex items-center justify-center bg-center bg-no-repeat bg-cover w-[35px] h-[35px]">
                      {index + 1}
                    </div>
                    <span>{player.name ?? player.user?.display_name}</span>
                  </div>
                ))}
            </div>
          </div>
        </div>

        {currentRoom?.ownerId === currentUser.id ? (
          <>
            <ModalShowResult onResetGame={handleResetGame} />
            <ModalBet
              isPopoverOpen={isOpenPopoverConfirmBet}
              setIsPopoverOpen={setIsOpenPopoverConfirmBet}
            />
            <ModalUser />
            <ModalSetup />
            <ModalShowRank />
          </>
        ) : (
          <>
            <ModalShowResult onResetGame={handleResetGame} />
            <ModalShowRank />
            <ModalBet
              isPopoverOpen={isOpenPopoverConfirmBet}
              setIsPopoverOpen={setIsOpenPopoverConfirmBet}
            />
          </>
        )}
        <div className="fixed top-3 right-3">
          <UserInformation />
        </div>
      </div>
    </div>
  );
};
export default GamePage;
