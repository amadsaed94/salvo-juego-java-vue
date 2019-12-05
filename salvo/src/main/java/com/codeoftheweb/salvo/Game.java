package com.codeoftheweb.salvo;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

@Entity
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    private Date creationDate;

    @OneToMany(mappedBy="game", fetch=FetchType.EAGER)
    Set<GamePlayer> gamePlayers;

    @OneToMany(mappedBy="game", fetch=FetchType.EAGER)
    Set<GamePlayer> Scores;

    public Game() {
    }

    public Game(Date creationDate) {
        this.creationDate = creationDate;
    }

    public long getId() {
        return id;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public Set<GamePlayer> getGamePlayers() {
        return gamePlayers;
    }

    public Map<String,Object> makeGameDTO(){
        Map<String,Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", this.getId());
        dto.put("creationDate", this.getCreationDate());
        dto.put("gamePlayers", this.getGamePlayersList());
        return dto;
    }

    public List<Map<String,Object>> getGamePlayersList(){
        return this.getGamePlayers()
                .stream()
                .map(gamePlayer -> gamePlayer.makeGamePlayerDTO())
                .collect(Collectors.toList());
    }
}