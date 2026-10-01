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

let webSocket = new WebSocket("ws://127.0.0.1:8080/message");
webSocket.onopen = function () {
  console.log("WebSocket连接成功");
};
webSocket.onmessage = function (e) {
  console.log("WebSocket收到消息" + e.data);
};
webSocket.onclose = function () {
  console.log("WebSocket断开连接");
};
webSocket.onerror = function () {
  console.log("WebSocket连接异常");
};

function initSendButton() {
  let sendButton = document.querySelector(".right .ctrl button");
  let messageInput = document.querySelector(".right .message-input");
  sendButton.onclick = function () {
    if (!messageInput.value) {
      return;
    }
    let selectedLi = document.querySelector("#session-list .selected");
    if (selectedLi == null) {
      return;
    }
    let sessionId = selectedLi.getAttribute("message-session-id");
    let request = {
      type: "message",
      sessionId: sessionId,
      content: messageInput.value,
    };
    request = JSON.stringify(request);
    webSocket.send(request);
    messageInput.value = "";
  };
}

initSendButton();

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

        li.onclick = function () {
          clickFriend(friend);
        };
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
        if (session.lastMessage.length > 10) {
          session.lastMessage = session.lastMessage.substring(0, 10) + "...";
        }
        let li = document.createElement("li");
        li.setAttribute("message-session-id", session.sessionId);
        li.innerHTML =
          "<h3>" +
          session.friends[0].friendName +
          "</h3>" +
          "<p>" +
          session.lastMessage +
          "</p>";
        sessionlistUL.appendChild(li);
        li.onclick = function () {
          clickSession(li);
        };
      }
    },
  });
}

getSessionList();

function clickSession(currentLi) {
  let allLis = document.querySelectorAll("#session-list>li");
  activeSession(allLis, currentLi);
  let sessionId = currentLi.getAttribute("message-session-id");
  getHistoryMessage(sessionId);
}

function activeSession(allLis, currentLi) {
  for (let li of allLis) {
    if (li == currentLi) {
      li.className = "selected";
    } else {
      li.className = "";
    }
  }
}

function getHistoryMessage(sessionId) {
  let titleDiv = document.querySelector(".right .title");
  titleDiv.innerHTML = "";
  let messageShowDiv = document.querySelector(".right .message-show");
  messageShowDiv.innerHTML = "";
  let selectedH3 = document.querySelector("#session-list .selected>h3");
  if (selectedH3) {
    titleDiv.innerHTML = selectedH3.innerHTML;
  }

  $.ajax({
    type: "get",
    url: "/message?sessionId=" + sessionId,
    success: function (result) {
      for (let message of result) {
        addMessage(messageShowDiv, message);
      }
      scrollBottom(messageShowDiv);
    },
  });
}

function addMessage(messageShowDiv, message) {
  let messageDiv = document.createElement("div");
  let selfUserName = document.querySelector(".left .user").innerHTML;
  if (selfUserName == message.fromName) {
    messageDiv.className = "message message-right";
  } else {
    messageDiv.className = "message message-left";
  }
  messageDiv.innerHTML =
    '<div class="box">' +
    "<h4>" +
    message.fromName +
    "</h4>" +
    "<p>" +
    message.content +
    "</p>" +
    "</div>";
  messageShowDiv.appendChild(messageDiv);
}

function scrollBottom(element) {
  let clientHeight = element.offsetHeight;
  let scrollHeight = element.scrollHeight;
  element.scrollTo(0, scrollHeight - clientHeight);
}

function clickFriend(friend) {
  let sessionLi = findSessionByName(friend.friendName);
  let sessionListUL = document.querySelector("#session-list");
  if (sessionLi) {
    sessionListUL.insertBefore(sessionLi, sessionListUL.children[0]);
    clickSession(sessionLi);
  } else {
    sessionLi = document.createElement("li");
    sessionLi.innerHTML = "<h3>" + friend.friendName + "</h3>" + "<p></p>";
    sessionListUL.insertBefore(sessionLi, sessionListUL.children[0]);
    sessionLi.onclick = function () {
      clickSession(sessionLi);
    };
    sessionLi.click();

    createSession(friend.friendId, sessionLi);
  }

  let tabSession = document.querySelector(".tab .tab-session");
  tabSession.click();
}

function findSessionByName(userName) {
  let sessionLis = document.querySelectorAll("#session-list>li");
  for (let sessionLi of sessionLis) {
    let h3 = sessionLi.querySelector("h3");
    if (h3.innerHTML == userName) {
      return sessionLi;
    }
  }
  return null;
}

function createSession(friendId, sessionLi) {
  $.ajax({
    type: "post",
    url: "session?toUserId=" + friendId,
    success: function (result) {
      setAttribute("message-session-id", result.sessionId);
    },
    error: function () {},
  });
}
