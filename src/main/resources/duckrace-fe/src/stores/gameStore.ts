/* eslint-disable @typescript-eslint/no-explicit-any */
// stores/counterStore.ts
import { BettorOfDucks, DuckPicked, IGame, IGameResult } from "@/interface/game/Game.ts";
import { create } from "zustand";

interface GameState {
  currentGame: IGame | null;
  isRacing: boolean | null;
  isCompletedAll: boolean | null;
  isResetGame: boolean;
  gameStatus: "waiting" | "betting" | "confirming" | "racing" | "completed";
  openModalBet: boolean;
  listBettorOfDucks: BettorOfDucks[];
  listDuckPicked: DuckPicked[];
  gameResult: IGameResult | null;
  isConfirmedBet: boolean;

  setCurrentGame: (currentGame: IGame | null) => void;
  setIsRacing: (isRacing: boolean | null) => void;
  setIsResetGame: (isResetGame: boolean) => void;
  setIsCompletedAll: (isCompletedAll: boolean | null) => void;
  setGameStatus: (gameStatus: "waiting" | "betting" | "confirming" | "racing" | "completed") => void;
  setOpenModalBet: (openModalBet: boolean) => void;
  setListBettorOfDucks: (listBettorOfDucks: BettorOfDucks[]) => void;
  setListDuckPicked: (listDuckPicked: DuckPicked[]) => void;
  setGameResult: (gameResult: IGameResult | null) => void;
  setIsConfirmedBet: (isConfirmedBet: boolean) => void;
}

const useGameStore = create<GameState>((set) => ({
  currentGame: null,
  isRacing: false,
  isResetGame: false,
  isCompletedAll: null,
  gameStatus: "waiting",
  openModalBet: false,
  listBettorOfDucks: [],
  listDuckPicked: [],
  gameResult: null,
  isConfirmedBet: false,

  setCurrentGame: (currentGame: IGame | null) => set({ currentGame }),
  setIsRacing: (isRacing: boolean | null) => set({ isRacing }),
  setIsResetGame: (isResetGame: boolean) => set({ isResetGame }),
  setIsCompletedAll: (isCompletedAll: boolean | null) => set({ isCompletedAll }),
  setGameStatus: (gameStatus: "waiting" | "betting" | "confirming" | "racing" | "completed") => set({ gameStatus }),
  setOpenModalBet: (openModalBet: boolean) => set({ openModalBet }),
  setListBettorOfDucks: (listBettorOfDucks: BettorOfDucks[]) => set({ listBettorOfDucks }),
  setListDuckPicked: (listDuckPicked: DuckPicked[]) => set({ listDuckPicked }),
  setGameResult: (gameResult: IGameResult | null) => set({ gameResult }),
  setIsConfirmedBet: (isConfirmedBet: boolean) => set({ isConfirmedBet }),
}));

export default useGameStore;
