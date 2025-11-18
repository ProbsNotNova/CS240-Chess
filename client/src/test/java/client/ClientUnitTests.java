package client;

import backend.ServerFacade;
import chess.ChessGame;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.CreateGameRequest;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;
import server.Server;
import service.UserService;
import ui.ChessClient;
import ui.Console;

import javax.naming.Context;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.sql.Array;
import java.util.*;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ClientUnitTests {

    private static ServerFacade serverFacade;
    private static final Console console = new Console();

    private static UserData existingUser;
    private static UserData newUser;
    private static CreateGameRequest createRequest;

    private String existingAuth;

    // ### TESTING SETUP/CLEANUP ###

    @BeforeAll
    public static void init() {
        System.out.println("Started Client Unit Tests");
        existingUser = new UserData ("ExistingUser", "existingUserPassword", "eu@mail.com");
        newUser = new UserData("NewUser", "newUserPassword", "nu@mail.com");
        createRequest = new CreateGameRequest("testGame");
    }

    @BeforeEach
    public void setup() throws IOException {
        console.client.clear();
        Assertions.assertDoesNotThrow(()->{
            console.client.register(existingUser.username(), existingUser.password(), existingUser.email());
            console.client.logout();
        });

    }

    // ### server-LEVEL UNIT TESTS ###

// GOOD LOGIN TEST
    @Test
    @Order(2)
    @DisplayName("Normal User Login")
    public void loggingIn() {
        Assertions.assertDoesNotThrow(()->{
            String loginResult = console.client.login(existingUser.username(), existingUser.password());
            Assertions.assertEquals(String.format("Signed in as %s", existingUser.username()), loginResult,
                    "Response didn't have same string return");
        });
    }
// BAD LOGIN TEST
    @Test
    @Order(3)
    @DisplayName("Login Invalid Params")
    public void loginInvalidParams() {
        Assertions.assertThrows(IOException.class, ()->console.client.login("wee"));
    }
// GOOD REGISTER TEST
    @Test
    @Order(4)
    @DisplayName("Normal User Registration")
    public void registering() {
        Assertions.assertDoesNotThrow(()->{
            String regResult = console.client.register(newUser.username(), newUser.password(), newUser.email());
            Assertions.assertEquals("Registered Successfully", regResult,
                    "Response didn't have same string return");

        });
    }
// BAD REGISTER TEST
    @Test
    @Order(5)
    @DisplayName("Register Invalid Params")
    public void registerBadRequest() {
        Assertions.assertThrows(IOException.class, ()->console.client.register(newUser.password()));
    }
// GOOD LOGOUT TEST
    @Test
    @Order(6)
    @DisplayName("Normal Logout")
    public void loggingOut() {
        //log out existing user
        Assertions.assertDoesNotThrow(()-> {
            console.client.login(existingUser.username(), existingUser.password());
            String regResult = console.client.logout();
            Assertions.assertEquals("Logging Out", regResult,
                    "Response didn't have same string return");
        });
    }
// BAD LOGOUT TEST
    @Test
    @Order(7)
    @DisplayName("Logout Invalid Params")
    public void logoutInvalidParams() {
        Assertions.assertThrows(IOException.class, ()->console.client.logout(newUser.password()));
    }
// GOOD CREATE GAME TEST
    @Test
    @Order(8)
    @DisplayName("Valid Creation")
    public void creatingGame() {
        Assertions.assertDoesNotThrow(()->{
            console.client.login(existingUser.username(), existingUser.password());
            console.client.createGame("Game");
        });
    }
    // BAD CREATE GAME TEST 1
    @Test
    @Order(9)
    @DisplayName("Create with Invalid Params")
    public void createGameInvalidParams() {
        //log out user so auth is invalid
        Assertions.assertThrows(IOException.class, console.client::createGame);
    }
// GOOD JOIN TEST
    @Test
    @Order(10)
    @DisplayName("Join Created Game")
    public void joiningGame() {
        //create game
        Assertions.assertDoesNotThrow(()-> {
            console.client.login(existingUser.username(), existingUser.password());
            console.client.createGame("game");
            String[] tokens = {"1", "WHITE"};
            String[] params = Arrays.copyOfRange(tokens, 0, tokens.length);
            console.client.joinGame(params);

        });
    }
// BAD JOIN TEST
    @Test
    @Order(11)
    @DisplayName("Join Invalid Params")
    public void joinGameInvalidParams() {
        Assertions.assertThrows(IOException.class, ()->console.client.joinGame("one"));
    }
// GOOD LIST GAMES TEST
    @Test
    @Order(12)
    @DisplayName("List Games")
    public void listingGames() {
        Assertions.assertDoesNotThrow(()-> {
            console.client.login(existingUser.username(), existingUser.password());
            console.client.createGame("uwu");
            console.client.createGame("owo");
            console.client.listGames();
        });
    }
// BAD LIST GAMES TEST
    @Test
    @Order(12)
    @DisplayName("List Games Invalid Params")
    public void listingGamesInvalidParams() throws DataAccessException {
        Assertions.assertThrows(IOException.class, ()->console.client.listGames("GAME NAME", "woah cool"));
    }

}

