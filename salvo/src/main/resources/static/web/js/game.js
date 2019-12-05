

$(function () {
  loadData();
});


function loadData() {
  $.get('/api/game_view/' + getParameterByName('gp'))
    .done(function (data) {

      var playerInfo;
      if (data.gamePlayers.length>1){
           if (data.gamePlayers[0].id == getParameterByName('gp'))
              playerInfo = [data.gamePlayers[0].player, data.gamePlayers[1].player];
           else
              playerInfo = [data.gamePlayers[1].player, data.gamePlayers[0].player];
           $('#playerInfo').text(playerInfo[0].email + '(you) vs ' + playerInfo[1].email);
           }
      else{
            playerInfo=[data.gamePlayers[0].player]
           $('#playerInfo').text(playerInfo[0].email + '(you) vs ' + "waiting for other player..");
      }

      data.ships.forEach(function (shipPiece) {
        shipPiece.shipLocations.forEach(function (shipLocation) {
          let turnHitted = isHit(shipLocation,data.salvoes,playerInfo[0].id)
          if(turnHitted >0){
            $('#B_' + shipLocation).addClass('ship-piece-hited');
            $('#B_' + shipLocation).text(turnHitted);
          }
          else
            $('#B_' + shipLocation).addClass('ship-piece');
        });
      });
      data.salvoes.forEach(function (salvo) {
        console.log(salvo);
        if (playerInfo[0].id === salvo.player) {
          salvo.salvoLocations.forEach(function (location) {
            $('#S_' + location).addClass('salvo');
          });
        } else {
          salvo.salvoLocations.forEach(function (location) {
            $('#_' + location).addClass('salvo');
          });
        }
      });
    })
    .fail(function (jqXHR, textStatus) {
      alert('Failed: ' + textStatus);
    });
}

function isHit(shipLocation,salvoes,playerId) {
  var hit = 0;
  salvoes.forEach(function (salvo) {
    if(salvo.player != playerId)
      salvo.salvoLocations.forEach(function (location) {
        if(shipLocation === location)
          hit = salvo.turn;
      });
  });
  return hit;
}


//jquery para pintar las posiciones del salvo
 $("td[id^=S_]") .click(function(){
        if(!$(this).hasClass("salvo") && !$(this).hasClass("target") && $(".target").length < 5)
          {
            $(this).addClass("target");
          } else if($(this).hasClass("target")){
            $(this).removeClass("target");}
    })


//una funcion para obtener el id del juego
function getParameterByName(name) {
    var match = RegExp('[?&]' + name + '=([^&]*)').exec(window.location.search);
    return match && decodeURIComponent(match[1].replace(/\+/g, ' '));
};
var gpId = getParameterByName("gp")
console.log(gpId);




function fire(){

//para hacer un array tiene las posiciones de los salvos

    var locations=[];

    $(".target").each(function(){

        let location = $(this).attr("id").substring(2);

        locations.push(location);
    })

//jquery para pasar la informacion al servidor

    var url = "/api/games/players/" + getParameterByName("gp") + "/salvos"
    $.post({
        url: url,
        data: JSON.stringify(locations),
        dataType: "text",
        contentType: "application/json"
    })
    .done(function (response, status, jqXHR) {
        alert( "Salvo added: " + response );
        location.reload();
    })
    .fail(function (jqXHR, status, httpError){
        alert("Failed to add salvo: " + status + " " + httpError);
    })
    }



