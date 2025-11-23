// export const getCookie = (cookieName: string): string | undefined => {
//   const cookie = document.cookie.split("; ").find((row) => row.startsWith(`${cookieName}=`));
//   return cookie?.split("=")[1];
// };

// export const setCookie = (cookieName: string, value: string, expireInSeconds: number = 8640000): void => {
//   const expires = new Date(new Date().getTime() + expireInSeconds);
//   document.cookie = `${cookieName}=${value}; expires=${expires.toUTCString()}; path=/`;
// };

// export const deleteCookie = (cookieName: string): void => {
//   document.cookie = `${cookieName}=; expires=Thu, 01 Jan 1970 00:00:00 GMT; path=/`;
// };

export const setToLocalStorage = (key: string, value: string): void => {
  localStorage.setItem(key, value);
};
export const getFromLocalStorage = (key: string): string | null => {
  return localStorage.getItem(key);
};
export const removeFromLocalStorage = (key: string): void => {
  localStorage.removeItem(key);
};
export const clearLocalStorage = (): void => {
  localStorage.clear();
};
