// stores/counterStore.ts
import { ITransactionData } from "@/interface/transaction/Transaction.ts";
import { IAccessToken, IUserHashInfo, User } from "@/interface/user/User.ts";
import { create } from "zustand";

interface UserState {
  listUser: User[];
  currentUser: User;
  userHashInfo?: IUserHashInfo;
  accessToken?: IAccessToken;
  historyTransaction: ITransactionData[];

  // Function
  setListUser: (users: User[]) => void;
  setCurrentUser: (user: User) => void;
  setUserHashInfo: (userHashInfo?: IUserHashInfo) => void;
  setAccessToken: (accessToken?: IAccessToken) => void;
  changeWallet: (amount: number) => void;
  changeHistoryTransaction: (historyTransaction: ITransactionData[]) => void;
}

const useUserStore = create<UserState>((set) => ({
  listUser: [],
  currentUser: {
    id: "",
    name: "",
    userName: "",
    avatar: "",
    email: "",
    wallet: 0,
  },
  userHashInfo: undefined,
  accessToken: undefined,
  historyTransaction: [],

  // Function
  setListUser: (users: User[]) => set({ listUser: users }),
  setCurrentUser: (user: User) => set({ currentUser: user }),
  changeWallet: (amount: number) => set((state) => ({ currentUser: { ...state.currentUser, wallet: amount } })),
  setUserHashInfo: (userHashInfo?: IUserHashInfo) => set({ userHashInfo }),
  setAccessToken: (accessToken?: IAccessToken) => set({ accessToken }),
  changeHistoryTransaction: (historyTransaction: ITransactionData[]) => set({ historyTransaction }),
}));

export default useUserStore;
