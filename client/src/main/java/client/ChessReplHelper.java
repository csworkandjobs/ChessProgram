package client;

import chess.*;
import model.*;
import java.util.ArrayList;
import java.util.Collection;

import static chess.ChessGame.TeamColor.BLACK;
import static ui.EscapeSequences.*;
import static ui.EscapeSequences.SET_TEXT_COLOR_WHITE;

public class ChessReplHelper extends CommonREPLFunctions {
    //Object Constants - Maybe later allow these to be customized by user
    private final static String BOARD_TEXT_COLOR = SET_TEXT_COLOR_BLACK;
    private final static String DARK_SQUARE_COLOR = SET_BG_COLOR_BLUE;
    private final static String LIGHT_SQUARE_COLOR = SET_BG_COLOR_WHITE;
    private final static String SELECTED_HIGHLIGHT_SQUARE_COLOR = SET_BG_COLOR_YELLOW;
    private final static String CAPTURE_HIGHLIGHT_SQUARE_COLOR = SET_BG_COLOR_RED;
    private final static String DARK_HIGHLIGHT_SQUARE_COLOR = SET_BG_COLOR_DARK_GREEN;
    private final static String LIGHT_HIGHLIGHT_SQUARE_COLOR = SET_BG_COLOR_GREEN;
    private final static String BORDER_COLOR = SET_BG_COLOR_DARK_GREY;
    private final static String BORDER_TEXT_COLOR = SET_TEXT_COLOR_WHITE;

    //Constant properties derived from other properties - just avoids having to pull them over and over
    private final char firstCol;
    private final char firstRow;
    private final int indexDirection;
    private final String mySquareColor;
    private final String opponentSquareColor;

    //Object properties
    private final ChessGame.TeamColor myColor;

    //Setup
    public ChessReplHelper(ChessGame.TeamColor myColor) {
        this.myColor = myColor;

        if(myColor == BLACK) {
            firstCol = 'H';
            firstRow = 1;
            indexDirection = -1;
            mySquareColor = DARK_SQUARE_COLOR;
            opponentSquareColor = LIGHT_SQUARE_COLOR;
        } else {
            firstCol = 'A';
            firstRow = 8;
            indexDirection = 1;
            mySquareColor = LIGHT_SQUARE_COLOR;
            opponentSquareColor = DARK_SQUARE_COLOR;
        }
    }

    ////////////////////////////
    //DISPLAY HELPER FUNCTIONS//
    ////////////////////////////

    private void displayGame(GameData game, ChessPosition selectedPosition, Collection<ChessMove> legalMoves,
                             ArrayList<String> notifications) {
        ChessBoard board = game.game().getBoard();
        for(int row = 0; row < 10; row++) {
            //Build top and bottom Borders, including lettering
            if(row == 0 || row == 9) {
                //Setting Coloration
                System.out.print(BORDER_COLOR);
                System.out.print(BORDER_TEXT_COLOR);

                //Draw top and bottom borders
                System.out.print(EMPTY);
                for(char col = 0; col < 8; col++) {
                    System.out.print(String.format(" %c ", firstCol+(col*indexDirection)));
                }
                System.out.print(EMPTY);
                //Print notifications and move to next line
                finishLine(row, notifications);
                continue;
            }

            //Print Middle Rows
            for(int col = 0; col < 10; col++) {
                //Draw in border text, then move to next column
                if(col == 0 || col == 9) {
                    //Setting Coloration
                    System.out.print(BORDER_COLOR);
                    System.out.print(BORDER_TEXT_COLOR);
                    System.out.print(String.format(" %s ", firstRow - ((row-1)*indexDirection)));
                    continue;
                }

                //Identify the current position and the piece on it
                System.out.print(BOARD_TEXT_COLOR); //Set text color for the board
                ChessPosition currentPosition;
                if(myColor == BLACK) {
                    currentPosition = new ChessPosition(row, 9-col);
                } else {
                    currentPosition = new ChessPosition(9-row, col);
                }
                ChessPiece currentPiece = game.game().getBoard().getPiece(currentPosition);

                //Identify current square color
                String currentSquareColor = LIGHT_SQUARE_COLOR;
                if((col+row) % 2 != 0) {
                    currentSquareColor = DARK_SQUARE_COLOR;
                }

                //Check for highlighing
                currentSquareColor = checkForHighlighting(currentSquareColor, currentPiece, currentPosition,
                        selectedPosition, legalMoves);

                //Output square
                System.out.print(currentSquareColor + pieceToChar(currentPiece));
            }
            //Print notifications and move to the next line
            finishLine(row, notifications);
        }
        System.out.print("");
    }

    public void displayMenu(GameData game, ChessPosition selectedPosition, Collection<ChessMove> legalMoves,
                            ArrayList<String> notifications) {
        clearScreen();
        System.out.println("----------------------------------------");
        System.out.println("        " + game.gameName());
        System.out.println("----------------------------------------");
        displayGame(game, selectedPosition, legalMoves, notifications);
        System.out.println("Please select from the following options:");
        System.out.println(">> 1. Redraw Board");
        System.out.println(">> 2. Highlight Moves");
        System.out.println(">> 3. Help");
        System.out.println(">> 4. Make Move");
        System.out.println(">> 5. Resign");
        System.out.println(">> 0. Leave/Exit");
        System.out.println();
        System.out.print(  "Input >> ");
    }

    public void displayObserverMenu(GameData game, ChessPosition selectedPosition, Collection<ChessMove> legalMoves,
                                    ArrayList<String> notifications) {
        clearScreen();
        System.out.println("----------------------------------------");
        System.out.println("        " + game.gameName());
        System.out.println("----------------------------------------");
        displayGame(game, selectedPosition, legalMoves, notifications);
        System.out.println("Please select from the following options:");
        System.out.println(">> 1. Redraw Board");
        System.out.println(">> 2. Highlight Moves");
        System.out.println(">> 3. Help");
        System.out.println(">> 0. Leave/Exit");
        System.out.println();
        System.out.print(  "Input >> ");
    }

    public void displayHelp(GameData game, ChessPosition selectedPosition, Collection<ChessMove> legalMoves,
                            ArrayList<String> notifications) {
        clearScreen();
        System.out.println("----------------------------------------");
        System.out.println("                  HELP");
        System.out.println("----------------------------------------");
        displayGame(game, selectedPosition, legalMoves, notifications);
        System.out.println("Here are the available options from this menu:");
        System.out.println("\tRedraw Board      (-d)  Redraw the board, clearing any highlighting.");
        System.out.println("\tMake Move         (-m)  Make a move with a given chess piece.");
        System.out.println("\tHighlight Moves   (-hl) Highlight all legal moves for a selected piece.");
        System.out.println("\tHelp              (-h)  Redirect to the help page of the given menu.");
        System.out.println("\tResign            (-r)  Resign from the game.");
        System.out.println("\tLeave/Exit        (-l)  Exit the given menu, or exit the program.");
        System.out.println();
        waitForInput();
    }

    public void displayObserverHelp(GameData game, ChessPosition selectedPosition, Collection<ChessMove> legalMoves,
                                    ArrayList<String> notifications) {
        clearScreen();
        System.out.println("----------------------------------------");
        System.out.println("                  HELP");
        System.out.println("----------------------------------------");
        displayGame(game, selectedPosition, legalMoves, notifications);
        System.out.println("Here are the available options from this menu:");
        System.out.println("\tRedraw Board      (-d)  Redraw the board, clearing any highlighting.");
        System.out.println("\tHighlight Moves   (-hl) Highlight all legal moves for a selected piece.");
        System.out.println("\tHelp              (-h)  Redirect to the help page of the given menu.");
        System.out.println("\tLeave/Exit        (-l)  Exit the given menu, or exit the program.");
        System.out.println();
        waitForInput();
    }

    public void displayHighlightInput(GameData game, ChessPosition selectedPosition, Collection<ChessMove> legalMoves,
                                      ArrayList<String> notifications) {
        clearScreen();
        System.out.println("----------------------------------------");
        System.out.println("            HIGHLIGHT MOVES");
        System.out.println("----------------------------------------");
        displayGame(game, selectedPosition, legalMoves, notifications);
        System.out.print("Please select a piece: ");
    }

    public void displayPawnPromotion(GameData game, ChessPosition selectedPosition, Collection<ChessMove> legalMoves,
                                     ArrayList<String> notifications) {
        clearScreen();
        System.out.println("----------------------------------------");
        System.out.println("            PAWN PROMOTION");
        System.out.println("----------------------------------------");
        displayGame(game, selectedPosition, legalMoves, notifications);
        System.out.println("Select a piece type to promote your pawn to: ");
        System.out.println("\tQUEEN");
        System.out.println("\tROOK");
        System.out.println("\tBISHOP");
        System.out.println("\tKIGHT");
        System.out.println("\tPAWN");
        System.out.print("Input >>> ");
    }

    public void displayMoveFrom(GameData game, ChessPosition selectedPosition, Collection<ChessMove> legalMoves,
                                ArrayList<String> notifications) {
        clearScreen();
        System.out.println("----------------------------------------");
        System.out.println("              MOVE FROM");
        System.out.println("----------------------------------------");
        displayGame(game, selectedPosition, legalMoves, notifications);
        System.out.print("Please select a piece to move: ");
    }

    public void displayMoveTo(GameData game, ChessPosition selectedPosition, Collection<ChessMove> legalMoves,
                              ArrayList<String> notifications) {
        clearScreen();
        System.out.println("----------------------------------------");
        System.out.println("                MOVE TO");
        System.out.println("----------------------------------------");
        displayGame(game, selectedPosition, legalMoves, notifications);
        System.out.print("Please select a position to move to: ");
    }

    public void displayResignConfirm(GameData game, ChessPosition selectedPosition, Collection<ChessMove> legalMoves,
                                     ArrayList<String> notifications) {
        clearScreen();
        System.out.println("----------------------------------------");
        System.out.println("                RESIGN?");
        System.out.println("----------------------------------------");
        displayGame(game, selectedPosition, legalMoves, notifications);
        System.out.print("Are you sure you want to resign: ");
    }


    //////////////////////////
    //OTHER HELPER FUNCTIONS//
    //////////////////////////

    // Handles moving to next line after printing chessboard, including printing any notifications
    private void finishLine(int row, ArrayList<String> notifications) {
        //Reprint notifications
        if(notifications.size() > row) {
            System.out.print(RESET_TEXT_COLOR);
            System.out.print(RESET_BG_COLOR);
            System.out.print(String.format("\t\t%s",notifications.get(row)));
        }

        //Move to next line
        lineReturn();
    }

    private String pieceToChar(ChessPiece piece) {
        if(piece == null) {
            return EMPTY;
        }

        //Get UTF-8 Code for chess piece, defaults to WHITE.
        char pieceCode = selectPiece(piece);
        if(piece.getTeamColor() == BLACK) {
            //Change to BLACK if necessary.
            pieceCode += 32;
        }
        return String.format(" %c ", pieceCode);
    }

    //Find the chess piece character for the WHITE version represented in UTF-16
    private char selectPiece(ChessPiece piece) {
        switch(piece.getPieceType()) {
            case KING:
                return 'K';
            case QUEEN:
                return 'Q';
            case ROOK:
                return 'R';
            case BISHOP:
                return 'B';
            case KNIGHT:
                return 'N';
            default: //PAWN
                return 'P';
        }
    }

    private String checkForHighlighting(String currentSquareColor, ChessPiece currentPiece,
                                        ChessPosition currentPosition, ChessPosition selectedPosition,
                                        Collection<ChessMove> legalMoves) {
        //Identify if the current square has been selected for move highlighting
        if((selectedPosition != null) && currentPosition.equals(selectedPosition)) {
            return SELECTED_HIGHLIGHT_SQUARE_COLOR;
        }
        // Check for other highlighting
        for(ChessMove move : legalMoves) {
            if (currentPosition.equals(move.getEndPosition())) {
                if(currentPiece != null) {
                    return CAPTURE_HIGHLIGHT_SQUARE_COLOR;
                } else if(currentSquareColor == LIGHT_SQUARE_COLOR) {
                    return LIGHT_HIGHLIGHT_SQUARE_COLOR;
                } else {
                    return DARK_HIGHLIGHT_SQUARE_COLOR;
                }
            }
        }

        return currentSquareColor;
    }
}


