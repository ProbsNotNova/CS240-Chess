package passoff.server;

import chess.ChessGame;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import model.AuthData;
import model.CreateGameRequest;
import model.GameData;
import model.UserData;
import org.eclipse.jetty.server.Authentication;
import org.junit.jupiter.api.*;
import passoff.model.*;
import server.Server;
import service.UserService;

import java.net.HttpURLConnection;
import java.util.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UnitTests {

    private static DataAccess dataAccess;
    private static UserService userServiceFacade;

    private static UserData existingUser;
    private static UserData newUser;
    private static CreateGameRequest createRequest;

    private String existingAuth;

    // ### TESTING SETUP/CLEANUP ###

    @BeforeAll
    public static void init() {
        System.out.println("Started test UserService");

        dataAccess = new MemoryDataAccess();
        userServiceFacade = new UserService(dataAccess);
        existingUser = new UserData ("ExistingUser", "existingUserPassword", "eu@mail.com");
        newUser = new UserData("NewUser", "newUserPassword", "nu@mail.com");
        createRequest = new CreateGameRequest("testGame");
    }

    @BeforeEach
    public void setup() {

        Assertions.assertDoesNotThrow(()->{
            userServiceFacade.clearApp();
            //one user already logged in
            AuthData regResult = userServiceFacade.register(existingUser);
            existingAuth = regResult.authToken();
        });

    }

    // ### USERSERVICE-LEVEL UNIT TESTS ###

// GOOD LOGIN TEST
    @Test
    @Order(2)
    @DisplayName("Normal User Login")
    public void loggingIn() {
        Assertions.assertDoesNotThrow(()->{
            AuthData loginResult = userServiceFacade.login(existingUser);
            Assertions.assertEquals(existingUser.username(), loginResult.username(),
                    "Response did not give the same username as user");
            Assertions.assertNotNull(loginResult.authToken(), "Response did not return authentication String");
        });
    }
// BAD LOGIN TEST 1
    @Test
    @Order(3)
    @DisplayName("Login Bad Request")
    public void loginBadRequest() {
        try {
            UserData[] incompleteLoginRequests = {
                    new UserData(null, existingUser.password(), null),
                    new UserData(existingUser.username(), null, null),
            };

            for (UserData incompleteLoginRequest : incompleteLoginRequests) {
                AuthData loginResult = userServiceFacade.login(incompleteLoginRequest);

                assertAuthFieldsMissing(loginResult);
            }
        } catch (DataAccessException e) {
            Assertions.assertEquals(400, e.getStatusCode());
        }
    }
// BAD LOGIN TEST 2
    @Test
    @Order(3)
    @DisplayName("Login Unauthorized (Multiple Forms)")
    public void loginUnauthorized() {
        try {
            UserData[] unauthorizedLoginRequests = {newUser, new UserData(existingUser.username(), "BAD!PASSWORD", null)};

            for (UserData unauthorizedLoginRequest : unauthorizedLoginRequests) {
                AuthData loginResult = userServiceFacade.login(unauthorizedLoginRequest);

                assertAuthFieldsMissing(loginResult);
            }
        } catch (DataAccessException e) {
            Assertions.assertEquals(401, e.getStatusCode());
        }
    }
// GOOD REGISTER TEST
    @Test
    @Order(4)
    @DisplayName("Normal User Registration")
    public void registering() {
        Assertions.assertDoesNotThrow(()->{
            //submit register request
            AuthData registerResult = userServiceFacade.register(newUser);

            Assertions.assertEquals(newUser.username(), registerResult.username(),
                    "Response did not have the same username as was registered");
            Assertions.assertNotNull(registerResult.authToken(), "Response did not contain an authentication string");
        });
    }
// BAD REGISTER TEST 1
    @Test
    @Order(5)
    @DisplayName("Re-Register User")
    public void registerTwice() {
        //submit register request trying to register existing user
        try {
            AuthData registerResult = userServiceFacade.register(existingUser);
            assertAuthFieldsMissing(registerResult);

        } catch (DataAccessException e) {
            Assertions.assertEquals(403, e.getStatusCode());
        }
    }
// BAD REGISTER TEST 2
    @Test
    @Order(5)
    @DisplayName("Register Bad Request")
    public void registerBadRequest() {
        //attempt to register a user without a password
        try {
            UserData registerRequest = new UserData(newUser.username(), null, newUser.email());
            AuthData registerResult = userServiceFacade.register(registerRequest);
            assertAuthFieldsMissing(registerResult);
        } catch (DataAccessException e) {
            Assertions.assertEquals(400, e.getStatusCode());
        }
    }
// GOOD LOGOUT TEST
    @Test
    @Order(6)
    @DisplayName("Normal Logout")
    public void loggingOut() {
        //log out existing user
        Assertions.assertDoesNotThrow(()-> {
            userServiceFacade.logout(existingAuth);
        });
    }
// BAD LOGOUT TEST
    @Test
    @Order(7)
    @DisplayName("Invalid Auth Logout")
    public void logoutTwice() {
        //log out user twice
        //second logout should fail
        try {
            userServiceFacade.logout(existingAuth);
            userServiceFacade.logout(existingAuth);
        } catch (DataAccessException e) {
            Assertions.assertEquals(401, e.getStatusCode());
        }
    }

// GOOD CREATE GAME TEST
    @Test
    @Order(8)
    @DisplayName("Valid Creation")
    public void creatingGame() {
        Assertions.assertDoesNotThrow(()-> {
            int createResult = userServiceFacade.createGame(existingAuth, createRequest.gameName());
            Assertions.assertTrue(createResult > 0, "Result returned invalid game ID");
        });
    }
// BAD CREATE GAME TEST 1
    @Test
    @Order(9)
    @DisplayName("Create with Bad Authentication")
    public void createGameUnauthorized() {
        //log out user so auth is invalid
        try {
            userServiceFacade.logout(existingAuth);

            userServiceFacade.createGame(existingAuth, createRequest.gameName());

        } catch (DataAccessException e) {
            Assertions.assertEquals(401, e.getStatusCode());
        }
    }
// BAD CREATE GAME TEST 2
    @Test
    @Order(9)
    @DisplayName("Create Bad Request")
    public void createGameBadRequest() {
        try {
            userServiceFacade.createGame(new CreateGameRequest("GAME NAME").gameName(), null);
        } catch (DataAccessException e) {
            Assertions.assertEquals(400, e.getStatusCode());
        }
    }
// GOOD JOIN TEST
    @Test
    @Order(10)
    @DisplayName("Join Created Game")
    public void joiningGame() {
        //create game
        Assertions.assertDoesNotThrow(()-> {
            int createResult = userServiceFacade.createGame(existingAuth, createRequest.gameName());

            //join as white
            userServiceFacade.joinGame(existingAuth, "WHITE", createResult);

            Collection<GameData> listResult = userServiceFacade.listGames(existingAuth);
            Assertions.assertNotNull(listResult, "List result did not contain games");
            Assertions.assertEquals(1, listResult.size(), "List result is incorrect size");
            for (GameData game : listResult) {
                Assertions.assertEquals(game.whiteUsername(), existingUser.username());
            }
        });
    }
// BAD JOIN TEST 1
    @Test
    @Order(11)
    @DisplayName("Join Bad Authentication")
    public void joinGameUnauthorized() {
        try {
            //create game
            int createResult = userServiceFacade.createGame(existingAuth, createRequest.gameName());

            //try join as white
            userServiceFacade.joinGame(existingAuth + "bad stuff", "WHITE", createResult);

            //check
        } catch (DataAccessException e) {
            Assertions.assertEquals(401, e.getStatusCode());
        }

    }
// BAD JOIN TEST 2
    @Test
    @Order(11)
    @DisplayName("Join Bad Team Color")
    public void joinGameBadColor() {
        try {
            int createResult = userServiceFacade.createGame(createRequest.gameName(), existingAuth);
            //If you use deserialize to the TeamColor enum instead of a String each of these will be read as null
            for (String color : new String[]{null, "", "GREEN"}) {
                userServiceFacade.joinGame(existingAuth, color, createResult);
            }
        } catch (DataAccessException e) {
            Assertions.assertEquals(401, e.getStatusCode());
        }
    }

    @Test
    @Order(12)
    @DisplayName("List No Games")
    public void listGamesEmpty() {
        Assertions.assertDoesNotThrow(()-> {
            Collection<GameData> result = userServiceFacade.listGames(existingAuth);

            Assertions.assertNotNull(result, "List result did not contain an empty game list");
            Assertions.assertEquals(0, result.size(), "Found games when none should be there");
        });
    }
// GOOD LIST GAMES TEST
    @Test
    @Order(12)
    @DisplayName("List Multiple Games")
    public void listingGames() {
        Assertions.assertDoesNotThrow(()-> {
            //register a few users to create games
            UserData userA = new UserData("a", "A", "a.A");
            UserData userB = new UserData("b", "B", "b.B");
            UserData userC = new UserData("c", "C", "c.C");

            AuthData authA = userServiceFacade.register(userA);
            AuthData authB = userServiceFacade.register(userB);
            AuthData authC = userServiceFacade.register(userC);

            //1 as black from A
            String game1Name = "I'm numbah one!";
            int game1 = userServiceFacade.createGame(authA.authToken(), game1Name);
            userServiceFacade.joinGame(authA.authToken(), "WHITE", game1);

            //1 as white from B
            String game2Name = "Lonely";
            int game2 = userServiceFacade.createGame(authB.authToken(), game2Name);
            userServiceFacade.joinGame(authB.authToken(), "WHITE", game2);
            //1 of each from C
            String game3Name = "GG";
            int game3 = userServiceFacade.createGame(authC.authToken(), game3Name);
            userServiceFacade.joinGame(authC.authToken(), "BLACK", game3);

            GameData G1 = new GameData(game1, authA.username(), null, "I'm numbah one!", new ChessGame());
            GameData G2 = new GameData(game2, authB.username(), null, "Lonely", new ChessGame());
            GameData G3 = new GameData(game3, null, authC.username(), "GG", new ChessGame());

            //list games
            Collection<GameData> expectedList = new ArrayList<>();
            expectedList.add(G1);
            expectedList.add(G2);
            expectedList.add(G3);

            Collection<GameData> listResult = userServiceFacade.listGames(existingAuth);
//            assertHttpOk(listResult);
            Assertions.assertEquals(3, listResult.size());

            Assertions.assertNotNull(listResult, "List result did not contain a list of games");
            //check
            Assertions.assertArrayEquals(expectedList.toArray(), listResult.toArray(), "Returned Games list was incorrect");
        });
    }

// BAD LIST GAMES TEST
@Test
@Order(12)
@DisplayName("List Games bad request")
public void listingGamesBad() {
    try {
        //register a few users to create games
        UserData userA = new UserData("a", "A", "a.A");
        UserData userB = new UserData("b", "B", "b.B");
        UserData userC = new UserData("c", "C", "c.C");

        AuthData authA = userServiceFacade.register(userA);
        AuthData authB = userServiceFacade.register(userB);
        AuthData authC = userServiceFacade.register(userC);

        //1 as black from A
        String game1Name = "I'm numbah one!";
        int game1 = userServiceFacade.createGame(authA.authToken(), game1Name);
        userServiceFacade.joinGame(authA.authToken(), "WHITE", game1);

        //1 as white from B
        String game2Name = "Lonely";
        int game2 = userServiceFacade.createGame(authB.authToken(), game2Name);
        userServiceFacade.joinGame(authB.authToken(), "WHITE", game2);
        //1 of each from C
        String game3Name = "GG";
        int game3 = userServiceFacade.createGame(authC.authToken(), game3Name);
        userServiceFacade.joinGame(authC.authToken(), "BLACK", game3);

        GameData G1 = new GameData(game1, authA.username(), null, "I'm numbah one!", new ChessGame());
        GameData G2 = new GameData(game2, authB.username(), null, "Lonely", new ChessGame());
        GameData G3 = new GameData(game3, null, authC.username(), "GG", new ChessGame());

        //list games
        Collection<GameData> expectedList = new ArrayList<>();
        expectedList.add(G1);
        expectedList.add(G2);
        expectedList.add(G3);

        Collection<GameData> listResult = userServiceFacade.listGames(existingAuth + "BADD STUF");
        Assertions.assertEquals(3, listResult.size());

        Assertions.assertNotNull(listResult, "List result did not contain a list of games");
        //check
        Assertions.assertArrayEquals(expectedList.toArray(), listResult.toArray(), "Returned Games list was incorrect");
    } catch (DataAccessException e) {
        Assertions.assertEquals(401, e.getStatusCode());
    }
}
// GOOD CLEAR TEST
@Test
@Order(14)
@DisplayName("Multiple Clears")
public void clearMultipleTimes() {
    try {
        //clear multiple times
        userServiceFacade.clearApp();
        userServiceFacade.clearApp();
        assertOk(200);

        //make sure returned good
    } catch (DataAccessException e) {
        Assertions.assertEquals(500, e.getStatusCode());
    }
}
// BAD CLEAR TEST 1
    @Test
    @Order(14)
    @DisplayName("Unauthorized Login after Clear")
    public void clearDataBad1() {
        try {
            //create filler games
            userServiceFacade.createGame(existingAuth, "Mediocre game");
            userServiceFacade.createGame(existingAuth, "Awesome game");

            //log in new user
            UserData user = new UserData("ClearMe", "cleared", "clear@mail.com");
            AuthData registerResult = userServiceFacade.register(user);

            //create and join game for new user
            int createResult = userServiceFacade.createGame("Clear game",
                    registerResult.authToken());

            userServiceFacade.joinGame(registerResult.authToken(), "WHITE", createResult);

            //do clear
            userServiceFacade.clearApp();

            //make sure neither user can log in
            //first user
            userServiceFacade.login(existingUser);

            //second user
            userServiceFacade.login(user);
        } catch (DataAccessException e) {
            Assertions.assertEquals(401, e.getStatusCode());

        }
    }
    // ### HELPER ASSERTIONS ###

    private void assertOk(int result) {
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, result);
    }

    private void assertAuthFieldsMissing(AuthData result) {
        Assertions.assertNull(result.username(), "Response incorrectly returned username");
        Assertions.assertNull(result.authToken(), "Response incorrectly return authentication String");
    }

}
