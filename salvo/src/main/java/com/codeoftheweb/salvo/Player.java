package com.codeoftheweb.salvo;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Entity
public class    Player {


    //Atributos

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    private String userName;

    private String password ;

    @OneToMany(mappedBy="player", fetch=FetchType.EAGER)
    Set<GamePlayer> gamePlayers;

    @OneToMany(mappedBy="player", fetch=FetchType.EAGER)
    Set<Score> scores;


    //Constructores
    public Player() { }

    public Player(String userName,String password) {
        this.userName = userName;
        this.password = password;
    }


    //getter
    public long getId() {
        return id;
    }

    public String getUserName() {
        return userName;
    }

    public Set<GamePlayer> getGamePlayers() {
        return gamePlayers;
    }

    public String getPassword() {
        return password;
    }



    public Set<Score> getScores() {
        return scores;
    }


    public double totalScore(){

        return this.wins()*1 + this.ties()* 0.5 + this.looses()*0;
    }

    public double wins(){
       return this.getScores().stream().filter(puntaje -> puntaje.getScore() == 1).count();
    }

    public double ties(){
        return  this.getScores().stream().filter(puntaje -> puntaje.getScore() == 0.5).count();
    }

    public double looses(){
        return this.getScores().stream().filter(puntaje -> puntaje.getScore() == 0).count();
    }



    public Map<String,Object> getScoreDTO(){
        Map<String,Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", this.getId());
        dto.put("userName", this.getUserName() );
        dto.put("gamesPlayed", this.getScores().size());
        dto.put("totalScore", this.totalScore() );
        dto.put("wins", this.wins() );
        dto.put("ties", this.ties() );
        dto.put("loss",  this.looses());
        return dto;
    }

    public Map<String,Object> getPlayerDTO(){
        Map<String,Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", this.getId());
        dto.put("email", this.getUserName());
        return dto;
    }

}