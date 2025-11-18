package client;

import backend.ServerFacade;
import model.UserData;
import org.junit.jupiter.api.*;
import server.Server;

import java.io.IOException;
import java.util.*;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade SERVERFACADE;

    private static UserData existingUser;
    private static UserData newUser;
    private static String loginResult;

    // ### TESTING SETUP/CLEANUP ###

    @BeforeAll
    public static void init() throws IOException {
        server = new Server();
        var port = server.run(0);
        SERVERFACADE = new ServerFacade(port);
        System.out.println("Started test HTTP server on " + port);
        existingUser = new UserData ("ExistingUser", "existingUserPassword", "eu@mail.com");
        newUser = new UserData("NewUser", "newUserPassword", "nu@mail.com");
        SERVERFACADE.register(existingUser.username(), existingUser.password(), existingUser.email());

    }

    @AfterAll
    static void stopServer() throws IOException {
        SERVERFACADE.clear();
        server.stop();
    }

    @BeforeEach
    public void setup() throws IOException {
        loginResult = SERVERFACADE.login(existingUser.username(), existingUser.password());
    }

    // ### server-LEVEL UNIT TESTS ###

// GOOD REGISTER TEST
    @Test
    @Order(4)
    @DisplayName("Normal User Registration")
    public void registering() {
        Assertions.assertDoesNotThrow(()->{
            SERVERFACADE.register(newUser.username(), newUser.password(), newUser.email());

        });
    }
// BAD REGISTER TEST
    @Test
    @Order(5)
    @DisplayName("Register Invalid Params")
    public void registerBadRequest() throws IOException {
        SERVERFACADE.logout(loginResult);
        Assertions.assertThrows(IOException.class, ()->SERVERFACADE.register(newUser.password(), null, null));
    }
    // GOOD LOGIN TEST
    @Test
    @Order(2)
    @DisplayName("Normal User Login")
    public void loggingIn() {
        Assertions.assertDoesNotThrow(()->{
            SERVERFACADE.login(existingUser.username(), existingUser.password());
        });
    }
    // BAD LOGIN TEST
    @Test
    @Order(3)
    @DisplayName("Login Invalid Params")
    public void loginInvalidParams() {
        Assertions.assertThrows(IOException.class, ()->SERVERFACADE.login(null, null));
    }
// GOOD LOGOUT TEST
    @Test
    @Order(6)
    @DisplayName("Normal Logout")
    public void loggingOut() {
        //log out existing user
        Assertions.assertDoesNotThrow(()-> {
            SERVERFACADE.logout(loginResult);
        });
    }
// BAD LOGOUT TEST
    @Test
    @Order(7)
    @DisplayName("Logout Invalid Params")
    public void logoutInvalidParams() {
        Assertions.assertThrows(IOException.class, ()->SERVERFACADE.logout(newUser.password()));
    }
// GOOD CREATE GAME TEST
    @Test
    @Order(8)
    @DisplayName("Valid Creation")
    public void creatingGame() {
        Assertions.assertDoesNotThrow(()->{
            SERVERFACADE.createGame("Game", loginResult);
        });
    }
    // BAD CREATE GAME TEST 1
    @Test
    @Order(9)
    @DisplayName("Create with Invalid Params")
    public void createGameInvalidParams() {
        Assertions.assertThrows(IOException.class, ()->SERVERFACADE.createGame(null, loginResult));
    } // make sure to change all to actual server facade and get auth etc
// GOOD JOIN TEST
    @Test
    @Order(10)
    @DisplayName("Join Created Game")
    public void joiningGame() {
        //create game
        Assertions.assertDoesNotThrow(()-> {
            int gameID = SERVERFACADE.createGame("game", loginResult);
            SERVERFACADE.joinGame("WHITE", gameID, loginResult);
        });
    }
// BAD JOIN TEST
    @Test
    @Order(11)
    @DisplayName("Join Invalid Params")
    public void joinGameInvalidParams() {
        Assertions.assertThrows(IOException.class, ()->SERVERFACADE.joinGame("one", 3, loginResult));
    }
// GOOD LIST GAMES TEST
    @Test
    @Order(12)
    @DisplayName("List Games")
    public void listingGames() {
        Assertions.assertDoesNotThrow(()-> {
            SERVERFACADE.createGame("uwu", loginResult);
            SERVERFACADE.createGame("owo", loginResult);
            SERVERFACADE.listGames(loginResult);
        });
    }
// BAD LIST GAMES TEST
    @Test
    @Order(12)
    @DisplayName("List Games Invalid Params")
    public void listingGamesInvalidParams() {
        Assertions.assertThrows(IOException.class, ()->SERVERFACADE.listGames("bad auth"));
    }

}

