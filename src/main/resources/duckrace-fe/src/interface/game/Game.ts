import { IDuck } from "../room/Room.ts";
import { User } from "../user/User.ts";

export interface IGame {
  id: string;
}
export interface BettorDetail {
  userId: string;
  duckId: string;
  betAmount: number;
  user: User;
}
export interface BettorOfDucks extends IDuck {
  bettors: BettorDetail[];
}

export interface BetForDuckResponse {
  ducks: string[];
  betAmount: number;
  gameId: string;
}

export interface DuckPicked {
  duckId: string;
  duckName: string;
  duckOrder: number;
}

export interface IGameResult {
  roomId: string;
  winners: string[];
  gameId: string;
  totalBet: number;
  winBet: number;
  bettors: string[];
}
