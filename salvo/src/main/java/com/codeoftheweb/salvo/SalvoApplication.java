package com.codeoftheweb.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.*;

@SpringBootApplication
public class SalvoApplication {
	@Bean
	public PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}
	@Autowired
	PasswordEncoder passwordEncoder;
	public static void main(String[] args) {
		SpringApplication.run(SalvoApplication.class, args);
	}

	@Bean
	public CommandLineRunner initData (PlayerRepository playerRepository,
																		 GameRepository gameRepository ,
																		 GamePlayerRepository gamePlayerRepository ,
																		 ShipRepository shipRepository,
																		 SalvoRepository salvoRepository,
																		 ScoreRepository scoreRepository) {
	return (args) -> {
		// save a couple of customers
		Player player1 = new Player("j.bauer@ctu.gov", passwordEncoder().encode("24"));
		Player player2 = new Player("c.obrian@ctu.gov", passwordEncoder().encode("42"));
		Player player3 = new Player("kim_bauer@gmail.com",  passwordEncoder().encode("kb"));
    Player player4 = new Player("t.almeida@ctu.gov", passwordEncoder().encode("mole"));
		playerRepository.save(player1);
		playerRepository.save(player2);
		playerRepository.save(player3);
        playerRepository.save(player4);

		Date date1 = new Date();
		Date date2 = Date.from(date1.toInstant().plusSeconds(3600));
		Date date3 = Date.from(date2.toInstant().plusSeconds(3600));
    Date date4 = Date.from(date2.toInstant().plusSeconds(3600));

		Game game1  = new Game(date1);
		Game game2  = new Game(date2);
		/*Game game3  = new Game(date3);
    Game game4  = new Game(date4);
    Game game5  = new Game(date4);
    Game game6  = new Game(date4);
    Game game7  = new Game(date4);*/

		gameRepository.save(game1);
		gameRepository.save(game2);
		/*gameRepository.save(game3);
		gameRepository.save(game4);
		gameRepository.save(game5);
		gameRepository.save(game6);
		gameRepository.save(game7);*/

		GamePlayer gamePlayer1 = new GamePlayer(player1,game1);
		GamePlayer gamePlayer2 = new GamePlayer(player2,game1);
		GamePlayer gamePlayer3 = new GamePlayer(player3,game2);
		GamePlayer gamePlayer4 = new GamePlayer(player4,game2);

		gamePlayerRepository.save(gamePlayer1);
		gamePlayerRepository.save(gamePlayer2);
		gamePlayerRepository.save(gamePlayer3);
		gamePlayerRepository.save(gamePlayer4);

		List<String> shipL1 = new LinkedList<>(Arrays.asList("H2","H3","H4"));
		List<String> shipL2 = new LinkedList<>(Arrays.asList("E1","F1","G1"));
		List<String> shipL3 = new LinkedList<>(Arrays.asList("F2","F3","F4"));
		List<String> shipL4 = new LinkedList<>(Arrays.asList("G2","G3","G4"));

		Ship ship1 = new Ship("Destroyer",gamePlayer1,shipL1);
		Ship ship2 = new Ship( "Cruiser",gamePlayer2, shipL2);
		Ship ship3 = new Ship("Submarine",gamePlayer3, shipL3);
		Ship ship4 = new Ship( "Battleship",gamePlayer4, shipL4);

		shipRepository.saveAll(Arrays.asList(ship1,ship2,ship3,ship4));

		Salvo salvo1 = new Salvo(1, Arrays.asList("H2","H3","E1"), gamePlayer1);
		Salvo salvo2= new Salvo(2, Arrays.asList("H1","H3","F1"), gamePlayer2);
		Salvo salvo3 = new Salvo(3, Arrays.asList("H1","C3","H4"), gamePlayer3);
		Salvo salvo4 = new Salvo(4, Arrays.asList("G2","A3","H4"), gamePlayer4);

salvoRepository.saveAll(Arrays.asList(salvo1,salvo2,salvo3,salvo4));


		Date finishDate1 = new Date();
		Date finishDate2 = new Date();
		Date finishDate3 = new Date();
		Date finishDate4 = new Date();



Score score1 = new Score(player1, game1, finishDate1, 1 );
Score score2 = new Score(player2, game2, finishDate2,  0.5);
/*Score score3 = new Score(player3, game3, finishDate3,  0);
Score score4 = new Score(player4, game4, finishDate4,  1);

		Score score5 = new Score(player1, game5, finishDate1, 0 );
		Score score6 = new Score(player1, game6, finishDate1, 0.5 );
		Score score7 = new Score(player1, game7, finishDate1, 1);
*/

scoreRepository.saveAll(Arrays.asList(score1,score2));
	};
}
}
@Configuration
class WebSecurityConfiguration extends GlobalAuthenticationConfigurerAdapter {

	@Autowired
	PlayerRepository personRepository;

	@Override
	public void init(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(inputName-> {
			Player player = personRepository.findByUserName(inputName);
			if (player != null) {
				return new User(player.getUserName(), player.getPassword(),
								AuthorityUtils.createAuthorityList("USER"));
			} else {
				throw new UsernameNotFoundException("Unknown user: " + inputName);
			}
		});
	}
}
@Configuration
@EnableWebSecurity
class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()
						.antMatchers("/rest/**").hasAuthority("ADMIN")
						.antMatchers("/api/game_view/*", "/api/logout", "/api/games/*/players", "/web/game.html*","/web/gridModificado.html", "/api/games/players/*/ships","/api/games/players/*/salvos" ).hasAuthority("USER")
						.antMatchers("/api/leaderboard").permitAll()
						.antMatchers("/api/login").permitAll()
						.antMatchers("/api/games").permitAll()
						.antMatchers("/web/css/**").permitAll()
						.antMatchers("/web/js/**").permitAll()
						.antMatchers("/web/games.html").permitAll()
						.antMatchers("/api/players").permitAll()
						.anyRequest().denyAll();

		http.formLogin()
						.usernameParameter("username")
						.passwordParameter("password")
						.loginPage("/api/login");

		http.logout().logoutUrl("/api/logout");

		// turn off checking for CSRF tokens
		http.csrf().disable();

		// if user is not authenticated, just send an authentication failure response
		http.exceptionHandling().authenticationEntryPoint((req, res, exc) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED));

		// if login is successful, just clear the flags asking for authentication
		http.formLogin().successHandler((req, res, auth) -> clearAuthenticationAttributes(req));

		// if login fails, just send an authentication failure response
		http.formLogin().failureHandler((req, res, exc) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED));

		// if logout is successful, just send a success response
		http.logout().logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler());
	}
	private void clearAuthenticationAttributes(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if (session != null) {
			session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);}
}}