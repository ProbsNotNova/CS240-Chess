package ui;
import java.util.Scanner;

import static ui.EscapeSequences.*;

public class Console {
    public final ChessClient client = new ChessClient();
    private final Scanner scanner = new Scanner(System.in);

    private void printPrompt() {
        System.out.print("\n" + RESET_TEXT_COLOR + ">>> " + SET_TEXT_COLOR_GREEN);
    }

    // Full Read Eval Print Loop
    public void run() {
        System.out.print(client.help());
        var result = "";
        while(!result.equals("quit")) {

            printPrompt();
            String line = scanner.nextLine();

            try {
                result = client.eval(line);
                System.out.print(SET_TEXT_COLOR_BLUE + result);
//                if (client.currentPlayerColor.equals("WHITE")) {
//
//                } else if (client.currentPlayerColor.equals("BLACK")) {
//
//                }

            } catch (Throwable e) {
                var msg = e.toString();
                System.out.print(msg);
            }
        }
    }
}


