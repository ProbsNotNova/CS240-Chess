package backend;

import com.google.gson.Gson;
import model.*;

import java.io.IOError;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collection;

public class ServerFacade {

    private HttpRequest buildRequest(String path, String method, Object body, String authToken) throws IOException {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder();
            builder.uri(new URI("http://localhost:8080/" + path));
            builder.method(method, bodyBuilder(body));
            if (authToken != null) {
                builder.header("Authorization", authToken);
            }
            return builder.build();
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
    }

    private HttpRequest.BodyPublisher bodyBuilder(Object body) {
        if (body == null) {
            return HttpRequest.BodyPublishers.noBody();
        }
        return HttpRequest.BodyPublishers.ofString(new Gson().toJson(body));
    }

    private <T> T sendRequest(HttpRequest request, Class<T> responseClass) throws IOException {
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                if (responseClass == null) {
                    return null;
                }
                return new Gson().fromJson(response.body(), responseClass);
            }
            throw new IOException(new Gson().fromJson(response.body(), ErrorResponse.class).message());
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }

    /// SIGNEDOUT State Methods
    public String register(String uName, String pWord, String email) throws IOException {
        HttpRequest request = buildRequest("user", "POST", new UserData(uName, pWord, email), null);
        AuthData auth = sendRequest(request, AuthData.class);
        assert auth != null;
        return auth.authToken();
    }

    public String login(String uName, String pWord) throws IOException {
        HttpRequest request = buildRequest("session", "POST", new UserData(uName, pWord, null), null);
        AuthData auth = sendRequest(request, AuthData.class);
        assert auth != null;
        return auth.authToken();
    }

    /// SIGNEDIN State Methods
    public GameData joinGame(String playerColor, int gameID, String authToken) throws IOException {
        HttpRequest request = buildRequest("game", "PUT", new JoinGameRequest(playerColor, gameID), authToken);
        return sendRequest(request, GameData.class);
    }
    public int createGame(String gameName, String authToken) throws IOException {
        HttpRequest request = buildRequest("game", "POST", new CreateGameRequest(gameName), authToken);
        GameData game = sendRequest(request, GameData.class);
        assert game != null;
        return game.gameID();
    }
    public Collection<GameData> listGames(String authToken) throws IOException {
        HttpRequest request = buildRequest("game", "GET", null, authToken);
        ListGamesResult games = sendRequest(request, ListGamesResult.class);
        assert games != null;
        return games.games();
    }
    public void logout(String authToken) throws IOException {
        HttpRequest request = buildRequest("session", "DELETE", null, authToken);
        sendRequest(request, AuthData.class);
    }
    public void clear() throws IOException {
        HttpRequest request = buildRequest("db", "DELETE", null, null);
        sendRequest(request, AuthData.class);
    }
}