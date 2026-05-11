package client;

import chess.*;

public class ClientMain {
    public static void main(String[] args) {
        //var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        //System.out.println("♕ 240 Chess Client: " + piece);

        String serverURL = "http://localhost:8080";
        if(args.length == 1) {
            serverURL = args[0];
        }

        new PreLoginREPL(serverURL).run();

    }
}
