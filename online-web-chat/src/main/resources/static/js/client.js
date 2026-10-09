function initSwitchTab() {
  let tabSession = document.querySelector(".tab .tab-session");
  let tabFriend = document.querySelector(".tab .tab-friend");
  let lists = document.querySelectorAll(".list");

  tabSession.onclick = function () {
    tabSession.classList.add("active");
    tabFriend.classList.remove("active");
    lists[0].classList = "list";
    lists[1].classList = "list hide";
  };

  tabFriend.onclick = function () {
    tabFriend.classList.add("active");
    tabSession.classList.remove("active");
    lists[0].classList = "list hide";
    lists[1].classList = "list";
  };

  tabSession.click();
}

initSwitchTab();

const AVATAR_COLORS = [
  "#4f7cff",
  "#e0607e",
  "#26a69a",
  "#f0932b",
  "#8e44ad",
  "#2ecc71",
  "#e17055",
  "#00a8cc",
  "#c0392b",
  "#5f27cd",
];

function avatarText(name) {
  if (name === null || name === undefined) {
    return "?";
  }
  name = String(name);
  if (name.length === 0) {
    return "?";
  }
  if (typeof Intl !== "undefined" && Intl.Segmenter) {
    const segmenter = new Intl.Segmenter("zh", { granularity: "grapheme" });
    for (const segment of segmenter.segment(name)) {
      return segment.segment;
    }
  }
  return Array.from(name)[0];
}

function avatarColor(name) {
  let hash = 0;
  const str = name === null || name === undefined ? "" : String(name);
  for (let i = 0; i < str.length; i++) {
    hash = (hash * 31 + str.charCodeAt(i)) >>> 0;
  }
  return AVATAR_COLORS[hash % AVATAR_COLORS.length];
}

function avatarHTML(name, extraClass) {
  let className = "avatar" + (extraClass ? " " + extraClass : "");
  return (
    '<span class="' +
    className +
    '" style="background-color:' +
    avatarColor(name) +
    '">' +
    avatarText(name) +
    "</span>"
  );
}

function showChatView() {
  let main = document.querySelector(".main");
  if (main) {
    main.classList.add("show-chat");
  }
}

function showListView() {
  let main = document.querySelector(".main");
  if (main) {
    main.classList.remove("show-chat");
  }
}

function initMobileBack() {
  let backButton = document.querySelector(".right .mobile-back");
  if (backButton) {
    backButton.onclick = showListView;
  }
}

initMobileBack();

let webSocket = new WebSocket(
  `${window.location.protocol === "https:" ? "wss:" : "ws:"}//${window.location.host}/WebSocketMessage`,
);
webSocket.onopen = function () {
  console.log("WebSocket连接成功");
};
webSocket.onmessage = function (e) {
  console.log("WebSocket收到消息" + e.data);

  let response = JSON.parse(e.data);
  if (response.type == "message") {
    handleMessage(response);
  } else {
    console.log("类型不符合");
  }
};
webSocket.onclose = function () {
  console.log("WebSocket断开连接");
};
webSocket.onerror = function () {
  console.log("WebSocket连接异常");
};

function handleMessage(response) {
  let curSessionLi = findSessionLi(response.sessionId);
  if (curSessionLi == null) {
    curSessionLi = document.createElement("li");
    curSessionLi.setAttribute("message-session-id", response.sessionId);
    curSessionLi.innerHTML =
      avatarHTML(response.fromName) +
      '<div class="li-body"><h3>' +
      response.fromName +
      "</h3><p></p></div>";
    curSessionLi.onclick = function () {
      clickSession(curSessionLi);
    };
  }

  let p = curSessionLi.querySelector("p");
  p.innerHTML = response.content;
  if (p.innerHTML.length > 10) {
    p.innerHTML = p.innerHTML.substring(0, 10) + "...";
  }

  let sessionListUL = document.querySelector("#session-list");
  sessionListUL.insertBefore(curSessionLi, sessionListUL.children[0]);

  if (curSessionLi.className == "selected") {
    let messageShowDiv = document.querySelector(".right .message-show");

    addMessage(messageShowDiv, response);
    scrollBottom(messageShowDiv);
  }
}

function findSessionLi(targetSessionId) {
  let sessionLis = document.querySelectorAll("#session-list li");
  for (let li of sessionLis) {
    let sessionId = li.getAttribute("message-session-id");
    if (sessionId == targetSessionId) {
      return li;
    }
  }
  return null;
}

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
        userDiv.innerHTML =
          avatarHTML(result.userName) +
          '<span class="user-name">' +
          result.userName +
          "</span>";
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
      let oldLis = friendListUL.querySelectorAll("li[friend-id]");
      for (let oldLi of oldLis) {
        oldLi.remove();
      }
      for (let friend of result) {
        let li = document.createElement("li");
        li.innerHTML =
          avatarHTML(friend.friendName) +
          '<div class="li-body"><h4>' +
          friend.friendName +
          "</h4></div>";
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

function initSearch() {
  let searchButton = document.querySelector(".left .search button");
  let searchInput = document.querySelector(".left .search input");
  searchButton.onclick = function () {
    let keyword = searchInput.value.trim();
    if (keyword == "") {
      alert("请输入要搜索的用户名！");
      return;
    }
    $.ajax({
      type: "get",
      url: "searchUser",
      data: { userName: keyword },
      success: function (result) {
        showSearchResult(result);
      },
      error: function () {
        alert("搜索失败");
      },
    });
  };
}

initSearch();

function showSearchResult(result) {
  document.querySelector(".tab .tab-session").click();

  let allLis = document.querySelectorAll("#session-list>li");
  for (let li of allLis) {
    li.className = "";
  }

  document.querySelector(".right .title").innerHTML = "搜索结果为";
  let messageShowDiv = document.querySelector(".right .message-show");
  messageShowDiv.innerHTML = "";
  showChatView();

  if (!result || result.length == 0) {
    messageShowDiv.innerHTML =
      "<div class='message-search'><span class='search-empty'>没有找到相关用户</span></div>";
    return;
  }

  for (let user of result) {
    let div = document.createElement("div");
    div.className = "message-search";
    div.innerHTML =
      "<div class='search-box'>" +
      avatarHTML(user.userName) +
      "<span>" +
      user.userName +
      "</span><button class='add-friend-btn'>添加</button></div>";
    messageShowDiv.appendChild(div);

    let addButton = div.querySelector(".add-friend-btn");
    addButton.onclick = function () {
      addFriend(user.userId, addButton);
    };
  }
}

function addFriend(toUserId, addButton) {
  $.ajax({
    type: "post",
    url: "addFriend",
    data: { toUserId: toUserId },
    success: function (res) {
      if (res && res.code == 1) {
        alert(res.msg);
        addButton.disabled = true;
        addButton.innerHTML = "已申请";
      } else {
        alert(res ? res.msg : "添加失败");
      }
    },
    error: function () {
      alert("添加失败");
    },
  });
}

function initNewFriendEntry() {
  let entry = document.querySelector("#new-friend-entry");
  entry.onclick = function () {
    showFriendRequests();
  };
}

initNewFriendEntry();

function showFriendRequests() {
  document.querySelector(".tab .tab-friend").click();

  let friendLis = document.querySelectorAll("#friend-list>li");
  for (let li of friendLis) {
    if (li.id == "new-friend-entry") {
      li.className = "selected";
    } else {
      li.className = "";
    }
  }

  document.querySelector(".right .title").innerHTML = "新好友请求";
  let messageShowDiv = document.querySelector(".right .message-show");
  messageShowDiv.innerHTML = "";
  showChatView();

  $.ajax({
    type: "get",
    url: "friendRequestList",
    success: function (result) {
      if (!result || result.length == 0) {
        messageShowDiv.innerHTML =
          "<div class='message-search'><span class='search-empty'>暂无新的好友申请</span></div>";
        return;
      }
      for (let request of result) {
        let div = document.createElement("div");
        div.className = "message-search";
        div.innerHTML =
          "<div class='search-box'>" +
          avatarHTML(request.fromName) +
          "<span>" +
          request.fromName +
          "</span><button class='accept-btn'>通过</button><button class='reject-btn'>拒绝</button></div>";
        messageShowDiv.appendChild(div);

        div.querySelector(".accept-btn").onclick = function () {
          handleFriendRequest(request.requestId, true, div);
        };
        div.querySelector(".reject-btn").onclick = function () {
          handleFriendRequest(request.requestId, false, div);
        };
      }
    },
    error: function () {
      alert("获取好友申请失败");
    },
  });
}

function handleFriendRequest(requestId, accept, requestDiv) {
  $.ajax({
    type: "post",
    url: "handleFriendRequest",
    data: { requestId: requestId, accept: accept },
    success: function (res) {
      if (res && res.code == 1) {
        alert(res.msg);
        requestDiv.remove();
        if (accept) {
          getFriendList();
        }
      } else {
        alert(res ? res.msg : "处理失败");
      }
    },
    error: function () {
      alert("处理失败");
    },
  });
}

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
          avatarHTML(session.friends[0].friendName) +
          '<div class="li-body"><h3>' +
          session.friends[0].friendName +
          "</h3>" +
          "<p>" +
          session.lastMessage +
          "</p></div>";
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
  showChatView();
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
  let selectedH3 = document.querySelector("#session-list .selected h3");
  if (selectedH3) {
    titleDiv.innerHTML =
      selectedH3.innerHTML +
      "<button class='delete-friend-btn'>删除好友</button>";
    titleDiv.querySelector(".delete-friend-btn").onclick = function () {
      deleteFriend(sessionId);
    };
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

function deleteFriend(sessionId) {
  if (!confirm("确定删除该好友吗？删除后聊天记录不可恢复。")) {
    return;
  }
  $.ajax({
    type: "post",
    url: "deleteFriend",
    data: { sessionId: sessionId },
    success: function (res) {
      if (res && res.code == 1) {
        alert(res.msg);
        let sessionLi = findSessionLi(sessionId);
        if (sessionLi) {
          sessionLi.remove();
        }
        document.querySelector(".right .title").innerHTML = "";
        document.querySelector(".right .message-show").innerHTML = "";
        getFriendList();
      } else {
        alert(res ? res.msg : "删除失败");
      }
    },
    error: function () {
      alert("删除失败");
    },
  });
}

function addMessage(messageShowDiv, message) {
  let messageDiv = document.createElement("div");
  let selfNameEl = document.querySelector(".left .user .user-name");
  let selfUserName = selfNameEl ? selfNameEl.innerHTML : "";
  if (selfUserName == message.fromName) {
    messageDiv.className = "message message-right";
  } else {
    messageDiv.className = "message message-left";
  }
  messageDiv.innerHTML =
    avatarHTML(message.fromName) +
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
    sessionLi.innerHTML =
      avatarHTML(friend.friendName) +
      '<div class="li-body"><h3>' +
      friend.friendName +
      "</h3><p></p></div>";
    sessionListUL.insertBefore(sessionLi, sessionListUL.children[0]);
    sessionLi.onclick = function () {
      clickSession(sessionLi);
    };

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
      sessionLi.setAttribute("message-session-id", result.sessionId);
      clickSession(sessionLi);
    },
    error: function () {},
  });
}
