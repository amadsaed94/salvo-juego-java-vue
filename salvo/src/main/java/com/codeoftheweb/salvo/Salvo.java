package com.codeoftheweb.salvo;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.*;

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

  public Map<String,Object> makesalvoDTO(){
    Map<String, Object> dto = new LinkedHashMap<>();
    dto.put("turn", this.getTurn());
    dto.put("salvoLocations", this.getSalvoLocations());
    dto.put("player", this.getGamePlayer().getPlayer().getId());
    return dto;
  }
}
