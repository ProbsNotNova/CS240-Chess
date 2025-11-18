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
    private static ServerFacade serverFacade;

    private static UserData existingUser;
    private static UserData newUser;
    private static String loginResult;

    // ### TESTING SETUP/CLEANUP ###

    @BeforeAll
    public static void init() throws IOException {
        server = new Server();
        var port = server.run(0);
        serverFacade = new ServerFacade(port);
        System.out.println("Started test HTTP server on " + port);
        existingUser = new UserData ("ExistingUser", "existingUserPassword", "eu@mail.com");
        newUser = new UserData("NewUser", "newUserPassword", "nu@mail.com");
        serverFacade.register(existingUser.username(), existingUser.password(), existingUser.email());

    }

    @AfterAll
    static void stopServer() throws IOException {
        serverFacade.clear();
        server.stop();
    }

    @BeforeEach
    public void setup() throws IOException {
        loginResult = serverFacade.login(existingUser.username(), existingUser.password());
    }

    // ### server-LEVEL UNIT TESTS ###

// GOOD REGISTER TEST
    @Test
    @Order(4)
    @DisplayName("Normal User Registration")
    public void registering() {
        Assertions.assertDoesNotThrow(()->{
            serverFacade.register(newUser.username(), newUser.password(), newUser.email());

        });
    }
// BAD REGISTER TEST
    @Test
    @Order(5)
    @DisplayName("Register Invalid Params")
    public void registerBadRequest() throws IOException {
        serverFacade.logout(loginResult);
        Assertions.assertThrows(IOException.class, ()->serverFacade.register(newUser.password(), null, null));
    }
    // GOOD LOGIN TEST
    @Test
    @Order(2)
    @DisplayName("Normal User Login")
    public void loggingIn() {
        Assertions.assertDoesNotThrow(()->{
            serverFacade.login(existingUser.username(), existingUser.password());
        });
    }
    // BAD LOGIN TEST
    @Test
    @Order(3)
    @DisplayName("Login Invalid Params")
    public void loginInvalidParams() {
        Assertions.assertThrows(IOException.class, ()->serverFacade.login(null, null));
    }
// GOOD LOGOUT TEST
    @Test
    @Order(6)
    @DisplayName("Normal Logout")
    public void loggingOut() {
        //log out existing user
        Assertions.assertDoesNotThrow(()-> {
            serverFacade.logout(loginResult);
        });
    }
// BAD LOGOUT TEST
    @Test
    @Order(7)
    @DisplayName("Logout Invalid Params")
    public void logoutInvalidParams() {
        Assertions.assertThrows(IOException.class, ()->serverFacade.logout(newUser.password()));
    }
// GOOD CREATE GAME TEST
    @Test
    @Order(8)
    @DisplayName("Valid Creation")
    public void creatingGame() {
        Assertions.assertDoesNotThrow(()->{
            serverFacade.createGame("Game", loginResult);
        });
    }
    // BAD CREATE GAME TEST 1
    @Test
    @Order(9)
    @DisplayName("Create with Invalid Params")
    public void createGameInvalidParams() {
        Assertions.assertThrows(IOException.class, ()->serverFacade.createGame(null, loginResult));
    } // make sure to change all to actual server facade and get auth etc
// GOOD JOIN TEST
    @Test
    @Order(10)
    @DisplayName("Join Created Game")
    public void joiningGame() {
        //create game
        Assertions.assertDoesNotThrow(()-> {
            int gameID = serverFacade.createGame("game", loginResult);
            serverFacade.joinGame("WHITE", gameID, loginResult);
        });
    }
// BAD JOIN TEST
    @Test
    @Order(11)
    @DisplayName("Join Invalid Params")
    public void joinGameInvalidParams() {
        Assertions.assertThrows(IOException.class, ()->serverFacade.joinGame("one", 3, loginResult));
    }
// GOOD LIST GAMES TEST
    @Test
    @Order(12)
    @DisplayName("List Games")
    public void listingGames() {
        Assertions.assertDoesNotThrow(()-> {
            serverFacade.createGame("uwu", loginResult);
            serverFacade.createGame("owo", loginResult);
            serverFacade.listGames(loginResult);
        });
    }
// BAD LIST GAMES TEST
    @Test
    @Order(12)
    @DisplayName("List Games Invalid Params")
    public void listingGamesInvalidParams() {
        Assertions.assertThrows(IOException.class, ()->serverFacade.listGames("bad auth"));
    }

}

