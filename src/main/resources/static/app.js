const stompClient = new StompJs.Client({
    brokerURL: 'ws://localhost:8080/stock'
});

stompClient.onConnect = (frame) => {
    setConnected(true);
    console.log('Connected: ' + frame);

    const tickerSymbols = ['005930'];
    tickerSymbols.forEach(tickerSymbol => {
        console.log('subscribe ' + tickerSymbol);
        stompClient.subscribe('/sub/portfolio/1/currentPrice/' + tickerSymbol, (response) => {
            let jsonBody = JSON.parse(response.body);
            console.log(jsonBody)
            showPrice(jsonBody);
        });
    });

    const bodyMap = {
        tickerSymbols: tickerSymbols
    };
    stompClient.publish({
        destination: "/pub/portfolio/1/currentPrice",
        body: JSON.stringify(bodyMap)
    });
};

stompClient.onWebSocketError = (error) => {
    console.error('Error with websocket', error);
};

stompClient.onStompError = (frame) => {
    console.error('Broker reported error: ' + frame.headers['message']);
    console.error('Additional details: ' + frame.body);
};

function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
    if (connected) {
        $("#conversation").show();
    } else {
        $("#conversation").hide();
    }
    $("#greetings").html("");
}

function connect() {
    stompClient.activate();
}

function disconnect() {
    stompClient.deactivate();
    setConnected(false);
    console.log("Disconnected");
}

function sendName() {
    console.log($("#name").val())
    const bodyMap = {
        tickerSymbols: [$("#name").val()],
    };
    console.log(JSON.stringify(bodyMap));
    stompClient.publish({
        destination: "/pub/currentPrice",
        body: JSON.stringify(bodyMap)
    });
}

function showPrice(message) {
    $("#greetings").append("<tr><td>" + message + "</td></tr>");
}

$(function () {
    $("form").on('submit', (e) => e.preventDefault());
    $("#connect").click(() => connect());
    $("#disconnect").click(() => disconnect());
    $("#send").click(() => sendName());
});
