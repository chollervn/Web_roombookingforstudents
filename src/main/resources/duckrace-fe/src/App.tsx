import { useEffect } from "react";
import { ToastContainer } from "react-toastify";
import "./App.css";
import AppProvider from "./providers/AppProvider.tsx";
import RouteManager from "./routes";
import { MezonAppEvent, MezonWebViewEvent } from "./types/webview";
function App() {
  useEffect(() => {
    window?.Mezon?.WebView?.postEvent("PING" as MezonWebViewEvent, { message: "Hello Mezon!" }, () => {});
    window?.Mezon?.WebView?.onEvent("PONG" as MezonAppEvent, (data) => {
      console.log("Hello Mezon Again!", data);
    });
  });
  return (
    <AppProvider>
      <div className='flex justify-center items-center h-screen'>
        <RouteManager />
        <ToastContainer
          position='bottom-left'
          autoClose={5000}
          hideProgressBar={false}
          newestOnTop={false}
          closeOnClick={false}
          rtl={false}
          pauseOnFocusLoss
          draggable
          pauseOnHover
          theme='light'
        />
      </div>
    </AppProvider>
  );
}

export default App;
