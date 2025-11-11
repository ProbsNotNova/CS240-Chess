package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.CreateGameRequest;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;
import org.opentest4j.AssertionFailedError;
import service.UserService;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collection;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MySqlUnitTests {

    private static DataAccess dataAccess;
    private static UserService userServiceFacade;

    private static UserData existingUser;
    private static UserData newUser;
    private static CreateGameRequest createRequest;
    private static int existingGameId;
    private static GameData existingGame;
    private String existingAuth;

    // ### TESTING SETUP/CLEANUP ###

    @BeforeAll
    public static void init() {
        System.out.println("Started test UserService");
        dataAccess = new SqlDataAccess();
        userServiceFacade = new UserService(dataAccess);
        existingGameId = 0;
        existingGame = new GameData(1, null, null, "CREATE GAME", new ChessGame());
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
            existingGameId = userServiceFacade.createGame(existingAuth, "CREATE GAME");
        });

    }

    // ### USERSERVICE-LEVEL UNIT TESTS ###

    // GOOD GET USER TEST
    @Test
    @Order(2)
    @DisplayName("Get User")
    public void getUserTest() {
        Assertions.assertDoesNotThrow(()->{
            UserData getResult = dataAccess.getUser(existingUser.username());
            Assertions.assertEquals(existingUser.username(), getResult.username(),
                    "Response did not give the same username as user");
            Assertions.assertNotNull(getResult.password(), "Response did not return authentication String");
        });
    }
    // BAD GET USER TEST
    @Test
    @Order(3)
    @DisplayName("Get User Bad Request")
    public void getUserBadRequest() {
        try {
            UserData[] incompleteGetUserRequests = {
                    new UserData(null, existingUser.password(), null),
                    new UserData(null, null, null),
            };
            for (UserData incompleteGetUserRequest : incompleteGetUserRequests) {
                dataAccess.getUser(incompleteGetUserRequest.username());
            }
        } catch (DataAccessException e) {
            Assertions.assertEquals(500, e.getStatusCode());
        }
    }
    // GOOD GET AUTH TEST
    @Test
    @Order(2)
    @DisplayName("Get Auth")
    public void getAuthTest() {
        Assertions.assertDoesNotThrow(()->{
            AuthData getResult = dataAccess.getAuth(existingAuth);
            Assertions.assertEquals(existingUser.username(), getResult.username(),
                    "Response did not give the same username as user");
            Assertions.assertNotNull(getResult.authToken(), "Response did not return authentication String");
        });
    }
    // BAD GET AUTH TEST
    @Test
    @Order(3)
    @DisplayName("Get Auth Bad Request")
    public void getAuthBadRequest() {
        try {
            Assertions.assertNull(dataAccess.getAuth("BAD AUTH"));
        } catch (DataAccessException e) {
            Assertions.assertEquals(500, e.getStatusCode());
        }
    }
    // GOOD GET GAME TEST
    @Test
    @Order(2)
    @DisplayName("Get Game")
    public void getGameTest() {
        Assertions.assertDoesNotThrow(()->{
            GameData getResult = dataAccess.getGame(existingGameId);
            Assertions.assertEquals(existingGame, getResult,
                    "Response did not give the same game as existingGame");
            Assertions.assertNotNull(getResult, "Response did not return a game");
        });
    }
    // BAD GET GAME TEST
    @Test
    @Order(3)
    @DisplayName("Get Game Bad Request")
    public void getGameBadRequest() {
        try {
            int badId = 5;
            Assertions.assertNull(dataAccess.getGame(badId));
        } catch (DataAccessException e) {
            Assertions.assertEquals(500, e.getStatusCode());
        }
    }

// GOOD CREATE USER TEST
    @Test
    @Order(2)
    @DisplayName("Create User")
    public void createUserTest() {
        Assertions.assertDoesNotThrow(()->{
            dataAccess.createUser(newUser);
            UserData getResult = dataAccess.getUser(newUser.username());
            Assertions.assertEquals(newUser.username(), getResult.username(),
                    "Response did not give the same username as user");
            Assertions.assertNotNull(getResult.password(), "Response did not return authentication String");
        });
    }
// BAD CREATE USER TEST
    @Test
    @Order(3)
    @DisplayName("Create User Bad Request")
    public void createUserBadRequest() {
        Assertions.assertThrows(DataAccessException.class, ()-> dataAccess.createUser(new UserData(null, null, null)));
    }
// GOOD CREATE AUTH TEST
    @Test
    @Order(2)
    @DisplayName("Create Auth")
    public void createAuthTest() {
        Assertions.assertDoesNotThrow(()->{
            AuthData newAuth = new AuthData(newUser.username(), "TestAuth");
            dataAccess.createAuth(newAuth);
            Assertions.assertEquals(dataAccess.getAuth("TestAuth").authToken(), newAuth.authToken(),
                    "Response did not give the same username as user");
            Assertions.assertNotNull(newAuth.authToken(), "Response did not return authentication String");
        });
    }
// BAD CREATE AUTH TEST
    @Test
    @Order(3)
    @DisplayName("Create Auth Bad Request")
    public void createAuthBadRequest() {
        Assertions.assertThrows(DataAccessException.class, ()-> dataAccess.createAuth(new AuthData(null, null)));
    }
// GOOD CREATE GAME TEST
    @Test
    @Order(2)
    @DisplayName("Create Game")
    public void createGameTest() {
        Assertions.assertDoesNotThrow(()-> {
            int createResult = dataAccess.createGame("NEW GAME");
            Assertions.assertTrue(createResult > 0, "Result returned invalid game ID");
        });
    }
// BAD CREATE GAME TEST
    @Test
    @Order(3)
    @DisplayName("Create Game Bad Request")
    public void createGameBadRequest() {
        Assertions.assertThrows(DataAccessException.class, ()-> dataAccess.createGame(null));
    }
// GOOD UPDATE GAME TEST
    @Test
    @Order(2)
    @DisplayName("Update Game")
    public void updateGameTest() {
        Assertions.assertDoesNotThrow(()-> {
            int createResult = dataAccess.createGame("NEW GAME");
            Assertions.assertTrue(createResult > 0, "Result returned invalid game ID");
        });
    }
// BAD UPDATE GAME TEST
    @Test
    @Order(3)
    @DisplayName("Update Game Bad Request")
    public void updateGameBadRequest() {
        Assertions.assertThrows(DataAccessException.class, ()-> {
            dataAccess.updateGame(5, ChessGame.TeamColor.WHITE, existingUser.username());
        });
    }

// GOOD LIST GAMES TEST
    @Test
    @Order(12)
    @DisplayName("List Multiple Games")
    public void listingGamesGood() {
        try {
            dataAccess.clearAllData();
            //1 of each from C
            String game1Name = "GG";
            int game = dataAccess.createGame(game1Name);
            dataAccess.updateGame(game, ChessGame.TeamColor.BLACK, existingUser.username());
            GameData game1 = dataAccess.getGame(game);
            Collection<GameData> expectedList = listGamesGeneric(game1);

            Collection<GameData> listResult = dataAccess.listGames();
            Assertions.assertEquals(3, listResult.size());
            Assertions.assertNotNull(listResult, "List result did not contain a list of games");
            //check
            Assertions.assertArrayEquals(expectedList.toArray(), listResult.toArray(), "Returned Games list was incorrect");
        } catch (DataAccessException ex) {
            Assertions.assertEquals(500, ex.getStatusCode());
        }
    }

// BAD LIST GAMES TEST
    @Test
    @Order(12)
    @DisplayName("List Games bad request")
    public void listingGamesBad() {
        Assertions.assertThrows(AssertionFailedError.class, ()->{
            dataAccess.clearAllData();
            Collection<GameData> expectedList = listGamesGeneric(new GameData(5, null, null, null, null));

            Collection<GameData> listResult = dataAccess.listGames();
            Assertions.assertEquals(3, listResult.size());
            Assertions.assertNotNull(listResult, "List result did not contain a list of games");
            //check
            Assertions.assertArrayEquals(expectedList.toArray(), listResult.toArray(), "Returned Games list was incorrect");
        });

    }
    // GOOD DELETE AUTH TEST
    @Test
    @Order(2)
    @DisplayName("Delete Auth")
    public void deleteAuthTest() {
        Assertions.assertDoesNotThrow(()->{
            AuthData newAuth = new AuthData(existingUser.username(), "NEWAUTH-465656345vOWO-HDHD-UEFJLDsHJLDS");
            dataAccess.createAuth(newAuth);
            dataAccess.deleteAuth(dataAccess.getAuth(newAuth.authToken()).authToken());
            Assertions.assertNull(dataAccess.getAuth(newAuth.authToken()));
        });
    }
    // BAD DELETE AUTH TEST
    @Test
    @Order(3)
    @DisplayName("Delete Auth Bad Request")
    public void deleteAuthBadRequest() {
        try {
            AuthData newAuth = new AuthData(existingUser.username(), null);
            dataAccess.createAuth(newAuth);
//            Assertions.assertNotNull(newAuth);
            dataAccess.deleteAuth(newAuth.authToken());
            Assertions.assertNotNull(dataAccess.getAuth(newAuth.authToken()));
        } catch (DataAccessException e) {
            Assertions.assertEquals(500, e.getStatusCode());
        }
    }

    // GOOD CLEAR TEST
    @Test
    @Order(14)
    @DisplayName("Good Clear")
    public void clearTest() {
        try {
            listGamesGeneric(existingGame);
            dataAccess.clearAllData();
            Assertions.assertNull(dataAccess.getGame(3));
        } catch (DataAccessException e) {
            Assertions.assertEquals(500, e.getStatusCode());
        }
    }
// BAD CLEAR TEST 1
    @Test
    @Order(14)
    @DisplayName("Get Game after Clear")
    public void clearDataBad() {
        try {
            listGamesGeneric(existingGame);
            dataAccess.clearAllData();
            Assertions.assertThrows(AssertionFailedError.class, ()-> Assertions.assertNotNull(dataAccess.getGame(3)));
        } catch (DataAccessException e) {
                Assertions.assertEquals(500, e.getStatusCode());
        }
    }
    // ### HELPER ASSERTIONS ###

    private void assertOk(int result) {
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, result);
    }

    private Collection<GameData> listGamesGeneric(GameData game1) throws DataAccessException {
        //register a few users to create games
        UserData userA = new UserData("a", "A", "a.A");
        UserData userB = new UserData("b", "B", "b.B");
        UserData userC = new UserData("c", "C", "c.C");

        AuthData authA = new AuthData(userA.username(), "authy");
        AuthData authB = new AuthData(userB.username(), "author");
        AuthData authC = new AuthData(userC.username(), "authel");

        //1 as black from A
        String game2Name = "I'm numbah one!";
        int game2 = dataAccess.createGame(game2Name);
        dataAccess.updateGame(game2, ChessGame.TeamColor.WHITE, authA.username());

        //1 as white from B
        String game3Name = "Lonely";
        int game3 = dataAccess.createGame(game3Name);
        dataAccess.updateGame(game3, ChessGame.TeamColor.WHITE, authB.username());

        GameData g1 = new GameData(game1.gameID(), game1.whiteUsername(), game1.blackUsername(), game1.gameName(), game1.game());
        GameData g2 = new GameData(game2, authA.username(), null, "I'm numbah one!", new ChessGame());
        GameData g3 = new GameData(game3, authB.username(), null, "Lonely", new ChessGame());

        //list games
        Collection<GameData> expectedList = new ArrayList<>();
        expectedList.add(g1);
        expectedList.add(g2);
        expectedList.add(g3);
        return expectedList;
    }

}
