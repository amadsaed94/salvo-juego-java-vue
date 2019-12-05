package com.codeoftheweb.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class SalvoController {

    @Autowired
    private GameRepository gameRepository;
    @Autowired
    private SalvoRepository salvoRepository;
    @Autowired
    private ScoreRepository scoreRepository;
    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private GamePlayerRepository gamePlayerRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ShipRepository shipRepository;



    @RequestMapping("/games")
    public  Map<String,Object>getGames(Authentication authentication){
        Map<String,Object> dto = new LinkedHashMap<>();

        if (isGuest(authentication)){
            dto.put("player","Guest");
        }
        else {
            Player player = playerRepository.findByUserName(authentication.getName());
            dto.put("player", player.getPlayerDTO());
        }

        dto.put("games",gameRepository.findAll()
                .stream()
                .map(game -> game.makeGameDTO())
                .collect(Collectors.toList()));
        return dto;
    }

    @RequestMapping("/game_view/{id}")
    public  Map<String, Object> getGameView (@PathVariable long id){

        GamePlayer gamePlayer = gamePlayerRepository.findById(id).get();

        Map<String, Object> dto = new LinkedHashMap<>();

        dto.put("id", gamePlayer.getGame().getId());
        dto.put("creationDate"  , gamePlayer.getGame().getCreationDate());
        dto.put("gamePlayers", gamePlayer.getGame().getGamePlayersList());
        dto.put("ships", gamePlayer.getShips().stream().map(ship -> ship.makeshipDTO()).collect(Collectors.toList()));
        dto.put("salvoes", gamePlayer.getGame().getGamePlayers().stream()
                .flatMap(gp -> gp.getSalvoes()
                        .stream()
                        .map(salvo -> salvo.makesalvoDTO())
                )
                .collect(Collectors.toList())
        );

        return dto;
    }

    @RequestMapping("/leaderboard")
    public List<Map<String,Object>> getLeaderBoards (){
        return playerRepository.findAll()
                .stream()
                .map(player ->  player.getScoreDTO())
                .collect(Collectors.toList());
    }

    @RequestMapping(path = "/players", method = RequestMethod.POST)
    public ResponseEntity<Object> register(@RequestParam String username, @RequestParam String password) {

        if (username.isEmpty() || password.isEmpty()) {
            return new ResponseEntity<>("Missing data", HttpStatus.FORBIDDEN);
        }

        if (playerRepository.findByUserName(username) !=  null) {
            return new ResponseEntity<>("Name already in use", HttpStatus.FORBIDDEN);
        }

        playerRepository.save(new Player(username, passwordEncoder.encode(password)));
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    private boolean isGuest(Authentication authentication) {
        return authentication == null || authentication instanceof AnonymousAuthenticationToken;
    }


    @RequestMapping(path = "/games", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> createGame(Authentication authentication) {
        if (isGuest(authentication)) {
            return new ResponseEntity<>(MakeMap("error", "No player logged in"), HttpStatus.FORBIDDEN);
        }
        else {
        Game game = new Game();
        gameRepository.save(game);

        Player player = playerRepository.findByUserName(authentication.getName());

        GamePlayer gamePlayer = gamePlayerRepository.save(new GamePlayer(player, game));
        return new ResponseEntity<>(MakeMap("game_player_id", gamePlayer.getId()), HttpStatus.CREATED);
        }

    }

    @RequestMapping(path = "/games/{id}/players")

    public ResponseEntity<Map<String, Object>> joinGame (Authentication authentication, @PathVariable long id) {
        Game game = gameRepository.findById(id).orElse(null);
        if (game == null) {
            return new ResponseEntity<>(MakeMap("error", "There is no game"), HttpStatus.FORBIDDEN);
        }

        if (isGuest(authentication)) {
            return new ResponseEntity<>(MakeMap("error", "There is no player"), HttpStatus.FORBIDDEN);
        }

        if  (game.getGamePlayers().stream().count()>1){
            return new ResponseEntity<>(MakeMap("error", "full players"), HttpStatus.FORBIDDEN);
        }

        if (game.getGamePlayers().stream().map(gamePlayer -> gamePlayer.getPlayer().getUserName()).collect(Collectors.toList()).contains(authentication.getName())){
            return new ResponseEntity<>(MakeMap("error", "You are in your game !!"),HttpStatus.FORBIDDEN);
        }

        Player player = playerRepository.findByUserName(authentication.getName());
        GamePlayer gamePlayer = new GamePlayer( player,  game);
        gamePlayerRepository.save(gamePlayer);

        return new ResponseEntity<>(MakeMap("game_player_id", gamePlayer.getId()), HttpStatus.CREATED);
    }

    public Map<String,Object> MakeMap (String key, Object value){
        Map<String, Object> crearMapa = new LinkedHashMap<>();
        crearMapa.put (key, value);
        return crearMapa;
    }

    @RequestMapping(path = "/games/players/{gamePlayerId}/ships",method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> shipList(Authentication authentication ,@PathVariable long gamePlayerId, @RequestBody List<Ship> ships )
    {
        if (isGuest(authentication)) {
            return new ResponseEntity<>(MakeMap("error", "No player logged in"), HttpStatus.UNAUTHORIZED);
        }
        GamePlayer gamePlayer = gamePlayerRepository.findById(gamePlayerId).orElse(null);
        if (gamePlayer == null) {
            return new ResponseEntity<>(MakeMap("error", "There is no gamePlayer"), HttpStatus.UNAUTHORIZED);
        }
        Player player = playerRepository.findByUserName(authentication.getName());
        if (player.getId() != gamePlayer.getPlayer().getId()){
            return new ResponseEntity<>(MakeMap("error", "player is not playing"), HttpStatus.UNAUTHORIZED);

        }

        if(gamePlayer.getShips().size() > 0){
            return new ResponseEntity<>(MakeMap("error", "the player already has ships placed !"), HttpStatus.FORBIDDEN);

        }



          ships.forEach(ship -> {
              ship.setGamePlayer(gamePlayer);
              shipRepository.save (new Ship(ship.getShipType(),gamePlayer,ship.getShipLocations()));
          });
        List<Ship> shipsSaved = new LinkedList<>(ships);

        return new ResponseEntity<>(MakeMap("response","ships Guardados"), HttpStatus.CREATED);


    }
    @RequestMapping(path = "/games/players/{gamePlayerId}/salvos",method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> salvos(Authentication authentication ,@PathVariable long gamePlayerId, @RequestBody List<String> locations)
    {
        if (isGuest(authentication)) {
            return new ResponseEntity<>(MakeMap("error", "No player logged in"), HttpStatus.UNAUTHORIZED);
        }
        GamePlayer gamePlayer = gamePlayerRepository.findById(gamePlayerId).orElse(null);
        if (gamePlayer == null) {
            return new ResponseEntity<>(MakeMap("error", "There is no gamePlayer"), HttpStatus.UNAUTHORIZED);
        }
        Player player = playerRepository.findByUserName(authentication.getName());
        if (player.getId() != gamePlayer.getPlayer().getId()){
            return new ResponseEntity<>(MakeMap("error", "player is not playing"), HttpStatus.UNAUTHORIZED);

        }

        int mySalvoesCount = gamePlayer.getSalvoes().size();
        if(gamePlayer.getGame().getGamePlayers().size() > 1){
            GamePlayer enemy = gamePlayer.getGame().getGamePlayers().stream().filter(gp -> gp.getId() != gamePlayerId).findFirst().get();

            if(enemy.getSalvoes().size() < mySalvoesCount){
                return new ResponseEntity<>(MakeMap("error", "the player already has submitted a salvo for the turn listed !"), HttpStatus.FORBIDDEN);

            }
        }

        Salvo salvo = new Salvo(mySalvoesCount + 1, locations, gamePlayer);

        salvoRepository.save(salvo);

        return new ResponseEntity<>(MakeMap("response","Salvos Guardados"), HttpStatus.CREATED);

    }





    }
