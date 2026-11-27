document.getElementById("opt").onclick = () => {
  chrome.runtime.openOptionsPage();
  window.close();
};