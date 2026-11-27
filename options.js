const input = document.getElementById("title");

// 打开页面时读取已保存的值
chrome.storage.sync.get({ title: "默认标题" }, (data) => {
  input.value = data.title;
});

// 点保存时写入
document.getElementById("save").onclick = () => {
  chrome.storage.sync.set({ title: input.value }, () => {
    document.getElementById("msg").textContent = "已保存";
  });
};