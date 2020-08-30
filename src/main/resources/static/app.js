let stompClient = null;

function setConnected(connected) {
  $("#connect").prop("disabled", connected);
  $("#disconnect").prop("disabled", !connected);
  if (connected) {
    $("#conversation").show();
  } else {
    $("#conversation").hide();
  }
  $("#messages").html("");
}

function connect() {
  let username = $("#name").val();
  let headers = {
    login: username,
    "X-Authorization": "Basic " + btoa(username + ":" + $("#password").val())
  };
  const socket = new SockJS('/ws');
  stompClient = Stomp.over(socket);
  stompClient.connect(headers, function (frame) {
    setConnected(true);
    console.log('Connected: ' + frame);
    stompClient.subscribe('/user/messages', function (greeting) {
      showMessage(JSON.parse(greeting.body).content);
    });
  });
}

function disconnect() {
  if (stompClient !== null) {
    stompClient.disconnect();
  }
  setConnected(false);
  console.log("Disconnected");
}

function showMessage(message) {
  $("#messages").append("<tr><td>" + message + "</td></tr>");
}

$(() => {
  $("form").on('submit', function (e) {
    e.preventDefault();
  });

  $("#connect").click(() => {
    connect();
  });

  $("#disconnect").click(() => {
    disconnect();
  });
});

