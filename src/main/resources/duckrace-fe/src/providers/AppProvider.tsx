import GetUserProvider from "./GetUserProvider.tsx";
import { SocketProvider } from "./SocketProvider.tsx";
interface AppProviderProps {
  children: React.ReactNode;
}
const AppProvider = ({ children }: AppProviderProps) => {
  return (
    <>
      <GetUserProvider>
        <SocketProvider>{children}</SocketProvider>
      </GetUserProvider>
    </>
  );
};
export default AppProvider;
