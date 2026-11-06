function toast(msg, isError) {
  if (!msg) return;
  const el = document.createElement('div');
  el.className = 'toast' + (isError ? ' error' : '');
  el.textContent = msg;
  document.body.appendChild(el);
  setTimeout(() => el.remove(), 2600);
}

axios.interceptors.response.use(
  function (response) { return response.data; },
  function (error) {
    const data = error.response && error.response.data;
    const msg = data && data.message ? data.message : '网络请求失败';
    toast(msg, true);
    return Promise.reject(error);
  }
);
