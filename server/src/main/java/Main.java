import server.Server;

public class Main {
    public static void main(String[] args) {
        Server server = new Server();
        server.run(8080);

        System.out.println("♕ 240 Chess Server");
    }





//    private void run() {
//    ...
//        Javalin.create()
//                .post("/name/{name}", new Handler() {
//                    public void handle(Context context) throws Exception {
//                        names.add(context.pathParam("name"));
//                        listNames(context);
//                    }
//                })
//    ...
//    }
//
//    private void listNames(Context context) {
//        String jsonNames = new Gson().toJson(Map.of("name", names));
//
//        context.contentType("application/json");
//        context.result(jsonNames);
//    }



//    private void run() {
//    ...
//    Javalin.create()
//        .post("/name{name}", context -> addName(context))
//    ...
//}
//
//private void addName(Context context) {
//    names.add(context.pathParam("name"));
//    listNames(context);
//}
//
//private void listNames(Context context) {
//    String jsonNames = new Gson().toJson(Map.of("name", names));
//    context.json(jsonNames);
//}

//    Javalin.create()
//    .post("/name{name}", this::addName)


}
//import chess.*;
//
//public class Main {
//    public static void main(String[] args) {
//        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
//        System.out.println("♕ 240 Chess Server: " + piece);
//    }
//}