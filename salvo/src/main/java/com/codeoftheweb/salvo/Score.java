package com.codeoftheweb.salvo;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Entity
public class Score {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
  @GenericGenerator(name = "native", strategy = "native")
  private long id;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name="player_id")
  private Player player;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name="game_id")
  private Game game;

  private Date finishDate ;

  private double score ;

  public Score() {
  }

  public Score(Player player, Game game, Date finishDate, double score) {
    this.player = player;
    this.game = game;
    this.finishDate = finishDate;
    this.score = score;
  }

  public long getId() {
    return id;
  }

  public Player getPlayer() {
    return player;
  }

  public Game getGame() {
    return game;
  }

  public Date getFinishDate() {
    return finishDate;
  }

  public double getScore() {
    return score;
  }


  public Map<String,Object> makeScoreDTO(){
    Map<String, Object> dto = new LinkedHashMap<>();
    dto.put("score", this.getScore());
    dto.put("finishDate", this.getFinishDate());
    return dto;
  }
}
