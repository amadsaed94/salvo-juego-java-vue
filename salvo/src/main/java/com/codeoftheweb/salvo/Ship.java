package com.codeoftheweb.salvo;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Entity
public class Ship {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    private String shipType;


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="gamePlayer_id")
    private GamePlayer gamePlayer;

    @ElementCollection
    @Column(name="shipLocation")
    private List<String> shipLocations = new ArrayList<>();

    public Ship() {
    }

    public Ship(String shipType, GamePlayer gamePlayer, List<String> shipLocations) {
        this.shipType = shipType;
        this.gamePlayer = gamePlayer;
        this.shipLocations = shipLocations;
    }

    public long getId() {
        return id;
    }

    public GamePlayer getGamePlayer() {
        return gamePlayer;
    }

    public String getShipType() {
        return shipType;
    }

    public List<String> getShipLocations() {
        return shipLocations;
    }

    public Map<String,Object> makeshipDTO(){
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("shipType", this.getShipType());
        dto.put("shipLocations", this.getShipLocations());
        return dto;
    }

    public void setGamePlayer(GamePlayer gamePlayer) {
        this.gamePlayer = gamePlayer;
    }
}
