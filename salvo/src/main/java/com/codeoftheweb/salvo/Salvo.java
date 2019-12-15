package com.codeoftheweb.salvo;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

@Entity
public class Salvo {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
  @GenericGenerator(name = "native", strategy = "native")
  private long id;
  private long turn;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "gamePlayer_id")
  private GamePlayer gamePlayer;

  @ElementCollection
  @Column(name="salvo_location")
  private List<String> salvoLocations = new ArrayList<>();

  public Salvo() {
  }

  public Salvo(long turn, List<String> salvoLocations, GamePlayer gamePlayer) {
    this.salvoLocations = salvoLocations;
    this.turn = turn;
    this.gamePlayer = gamePlayer;
  }

  public long getTurn() {
    return turn;
  }

  public GamePlayer getGamePlayer() {
    return gamePlayer;
  }

  public long getId() {
    return id;
  }

  public List<String> getSalvoLocations() {
    return salvoLocations;
  }


  public List<String> Hits (List<String> myShots , Set<Ship> enemyShips){

    List<String>  Enemy = new ArrayList<>();

    enemyShips.forEach(Ship ->Enemy.addAll(Ship.getShipLocations()));

    return myShots
            .stream()
            .filter(shot ->Enemy
                    .stream()
                    .anyMatch(loc-> loc.equals(shot)))
            .collect(Collectors.toList());

  }

  public  List<Ship> getSunk (Set<Salvo> mySalvos, Set<Ship> opponentShips){

    List<String> allShots = new ArrayList<>();

    mySalvos.forEach(salvo -> allShots.addAll(salvo.getSalvoLocations()));

    return opponentShips
            .stream()
            .filter(ship -> allShots.containsAll(ship.getShipLocations()))
            .collect(Collectors.toList());

  }

  public Map<String,Object> makesalvoDTO(){
    Map<String, Object> dto = new LinkedHashMap<>();
    dto.put("turn", this.getTurn());
    dto.put("salvoLocations", this.getSalvoLocations());
    dto.put("player", this.getGamePlayer().getPlayer().getUserName());

    GamePlayer Opponent = this.gamePlayer.getOpponent();

    if( Opponent != null){
      Set<Ship> enemyShips = Opponent.getShips();
      dto.put("hits", this.Hits(this.getSalvoLocations(), enemyShips));
      Set <Salvo> MySalvos = this.gamePlayer
              .getSalvoes()
              .stream()
              .filter(salvo -> salvo.getTurn() <= this.getTurn())
              .collect(Collectors.toSet());

      dto.put("suncken", this.getSunk ( MySalvos , enemyShips).stream().map(Ship::makeshipDTO));

    }

    return dto;
  }

}
