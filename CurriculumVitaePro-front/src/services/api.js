import axios from "axios";

const api = axios.create({
  baseURL: "/api",
  timeout: 15000,
});

api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("token");

    if (token) {
      config.headers = config.headers || {};
      config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
  },
  (error) => Promise.reject(error)
);

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response) {
      const status = error.response.status;

      if (status === 401) {
        localStorage.removeItem("token");
        console.warn("401 Unauthorized -> token removed");
      }

      if (status === 403) {
        console.warn("403 Forbidden:", error.response.data);
      }
    } else {
      console.warn("Network error:", error.message);
    }

    return Promise.reject(error);
  }
);

export default api;
