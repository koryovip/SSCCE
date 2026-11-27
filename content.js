chrome.storage.sync.get({ title: "默认标题" }, (data) => {
  const el = document.querySelector("h1");
  if (el) el.textContent = data.title;
});