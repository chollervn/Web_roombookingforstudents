import { User } from "../user/User.ts";

export interface ICreateRoomData {
  roomName: string;
  roomBet: number;
  isBetting: boolean;
  ownerId?: string;
}
export interface IScore {
  oldScore: number;
  newScore: number;
  totalScore: number;
}

export interface IDuck {
  id: string;
  name: string;
  score: IScore;
  order: number;
  role_id?: string[];
  colorNumber: number;
  userChannelId?: string;
  user?: {
    display_name: string;
    username: string;
    avatar_url: string;
  };
  isSelected?: boolean;
}

export interface RoomInfo {
  roomId: string;
  roomName: string;
  roomBet: number;
  roomPassword: string;
  roomUsePassword: boolean;
}

export interface Room {
  roomId: string;
  ownerId: string;
  roomInfo: RoomInfo;
  currentGame: string;
  members: string[];
  isPlaying: string;
  owner?: User;

  expiredTime: number;
  currentTime: number;
  ducks: IDuck[];
  totalDuck: number;
  status: "waiting" | "racing" | "completed";
}
export interface IMezonClan {
  id: string;
  clan_id: string;
  color: string;
  slug: string;
  title: string;
}
export interface IMezonUser {
  id: string;
  role_id: string[];
  userChannelId: string;
  user: {
    display_name: string;
    username: string;
    avatar_url: string;
  };
  isSelected: boolean;
}
