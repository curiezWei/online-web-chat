function initSwitchTab() {
  let tabSession = document.querySelector(".tab .tab-session");
  let tabFriend = document.querySelector(".tab .tab-friend");
  let lists = document.querySelectorAll(".list");

  tabSession.onclick = function () {
    tabSession.style.backgroundImage = "url(img/留言选中.png)";
    tabFriend.style.backgroundImage = "url(img/用户.png)";
    lists[0].classList = "list";
    lists[1].classList = "list hide";
  };

  tabFriend.onclick = function () {
    tabSession.style.backgroundImage = "url(img/留言.png)";
    tabFriend.style.backgroundImage = "url(img/用户选中.png)";
    lists[0].classList = "list hide";
    lists[1].classList = "list";
  };
}

initSwitchTab();

function getUserInfo() {
  $.ajax({
    type: "get",
    url: "userInfo",
    success: function (result) {
      if (result && result.userId > 0) {
        let userDiv = document.querySelector(".main .left .user");
        userDiv.innerHTML = result.userName;
        userDiv.setAttribute("user-id", result.userId);
      } else {
        alert("当前未登录");
        location.href = "login.html";
      }
    },
  });
}

getUserInfo();

function getFriendList() {
  $.ajax({
    type: "get",
    url: "friendList",
    success: function (result) {
      let friendListUL = document.querySelector("#friend-list");
      friendListUL.innerHTML = "";
      for (let friend of result) {
        let li = document.createElement("li");
        li.innerHTML = "<h4>" + friend.friendName + "</h4>";
        li.setAttribute("friend-id", friend.friendId);
        friendListUL.appendChild(li);
      }
    },
    error: function () {
      alert("获取好友列表失败");
    },
  });
}

getFriendList();

function getSessionList() {
  $.ajax({
    type: "get",
    url: "sessionList",
    success: function (result) {
      let sessionlistUL = document.querySelector("#session-list");
      sessionlistUL.innerHTML = "";
      for (let session of result) {
        let li = document.createElement("li");
        li.setAttribute("message-session-id", session.sessionId);
        li.innerHTML =
          "<h3>" +
          session.friends[0].friendName +
          "</h3>" +
          "<p>" +
          session.lastMessage +
          "</p>";
      }
    },
  });
}
getSessionList();
