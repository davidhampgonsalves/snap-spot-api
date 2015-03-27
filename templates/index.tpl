<h1>{{ title }}</h1>
<p class="desc">{{ desc }}</p>
<ul>
  {{#tags}}
    <li class="tag">{{ tag }}</li>{{/tags}}
  {{# hidden }}
    this will not show, if hidden is false or empty list
  {{/ hidden }}
</ul>
<script>
var tripId = window.location.pathname.substr(1);
var positionSocket = new WebSocket("ws://localhost:9000/position/subscribe/" + tripId);
positionSocket.onmessage = function (event) {
  console.log(event.data);
}
</script>
