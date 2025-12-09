package ui;

import websocket.MessageException;

import java.io.IOException;
import java.util.Scanner;

import static ui.EscapeSequences.*;

public class Console {
    public final ChessClient client;
    private final Scanner scanner = new Scanner(System.in);
    public Console(int serverPort, String serverURL) throws MessageException, IOException {
        client = new ChessClient(serverPort, serverURL);
    }
    private void printPrompt() {
        System.out.print("\n" + RESET_TEXT_COLOR + ">>> " + SET_TEXT_COLOR_GREEN);
    }

    // Full Read Eval Print Loop
    public void run() {
        System.out.print(client.help());
        var result = "";
        while(!result.equals("quit")) {

            client.printPrompt();
            String line = scanner.nextLine();

            try {
                result = client.eval(line);
                System.out.print(SET_TEXT_COLOR_BLUE + result);

            } catch (Throwable e) {
                var msg = e.getMessage();
                System.out.print(msg);
            }
        }
    }
}


