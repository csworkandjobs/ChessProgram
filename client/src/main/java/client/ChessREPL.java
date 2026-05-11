package client;

import chess.*;
import model.GameData;

import static chess.ChessGame.TeamColor;
import static chess.ChessGame.TeamColor.*;
import static ui.EscapeSequences.*;
import client.RequestDataStructures.*;
import websocket.messages.LoadGameMessage;
import websocket.messages.*;
import websocket.commands.*;

import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

public class ChessREPL extends CommonREPLFunctions {
    //Lists of accepted inputs for each type of input
    private static final String[] REDRAW_BOARD_OPTIONS =
            {"1", "1.", "redraw", "draw", "board", "rb", "-rb", "--rb", "b", "-b", "--b", "d", "-d", "--d"};
    private static final String[] HIGHLIGHT_MOVES_OPTIONS =
            {"2", "2.", "highlight", "high", "light", "hl", "-hl", "--hl"};
    private static final String[] HELP_MENU_INDEX_OPTIONS = {"3", "3."};
    private static final String[] MAKE_MOVE_OPTIONS =
            {"4", "4.", "make", "move", "makemove", "m", "-m", "--m"};
    private static final String[] RESIGN_INPUT_OPTIONS =
            {"5", "5.", "resign", "re", "r", "-r", "--r", "rs", "-rs", "--rs"};
    private static final String[] LEAVE_INPUT_OPTIONS =
            {"leave","l", "-l"};
    private static final String[] CONFIRM_INPUT_OPTIONS =
            {"yes","y", "-y", "--y", "confirm", "c", "-c", "--c"};
    private static final String[] DECLINE_INPUT_OPTIONS =
            {"no","n", "-n", "--n", "deny", "d", "-d", "--d"};

    //Object properties
    private final ServerFacade server;
    private final String username;
    private final String authToken;
    private final TeamColor myColor;
    private GameData game;
    private ChessReplHelper displayFunctions;

    enum DisplayState {
        HIGHLIGHT_INPUT,
        MAIN_MENU,
        HELP,
        OBSERVER_MENU,
        OBSERVER_HELP,
        PAWN_PROMOTION,
        MAKE_MOVE_FROM,
        MAKE_MOVE_TO,
        RESIGN_CONFIRM
    }

    //Class Variables
    private ChessPosition selectedPosition;
    private Collection<ChessMove> legalMoves = new ArrayList<ChessMove>(); //Stores highlighted spaces until cleared
    private ArrayList<String> notifications = new ArrayList<String>(List.of("Notifications:"));
    private WebSocketFacade webSocketFacade;
    private DisplayState displayState;
    private String url;

    public ChessREPL(ServerFacade server, String username, String authToken, TeamColor myColor, GameData game, String url) {
        this.server = server;
        this.username = username;
        this.authToken = authToken;
        this.game = game;
        this.myColor = myColor;
        this.url = url;
        displayState = DisplayState.MAIN_MENU;
        displayFunctions = new ChessReplHelper(myColor);

        //Connect to game as player (if relevant)
        if(myColor != null) {
            //This is inefficient (there are two different enums storing team color), but there isn't time to fix it.
            PlayerColor correctedColor = PlayerColor.WHITE;
            if (this.myColor != WHITE) {
                correctedColor = PlayerColor.BLACK;
            }

            try {
                server.doJoinGame(authToken, new JoinGameData(correctedColor, game.gameID()));
            } catch (ClientDataAccessException ex) {
                errorHandler(ex, "Error: That color is already taken!");
                return;
            }
        }

        //Setup websockets
        try {
            webSocketFacade = new WebSocketFacade(url, this);
        } catch(Exception ex) {

        }

        webSocketFacade.sendToServer(new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, game.gameID()));
    }

    public void playerRun() {
        String input;
        while(true) {
            displayState = DisplayState.MAIN_MENU;
            displayFunctions.displayMenu(game, selectedPosition, legalMoves, notifications);
            input = sc.next();
            sc.nextLine(); //Move scanner up to next input position to avoid problems on the next read.
            if(isInputExit(input) || isInList(LEAVE_INPUT_OPTIONS, input)) { //Check if game is being exited
                try {
                    doLeave();
                } catch (ClientDataAccessException ex) {
                    errorHandler(ex);
                    continue;
                }
                break;
            } else if (isInputHelp(input) || isInList(HELP_MENU_INDEX_OPTIONS, input)) { //Display help menu
                displayState = ChessREPL.DisplayState.HELP;
                displayFunctions.displayHelp(game, selectedPosition, legalMoves, notifications);
            } else if (isInList(REDRAW_BOARD_OPTIONS, input)) {
                clearHighlighting();
                //No additional instructions needed, board will redraw when loop resets.
            } else if (isInList(MAKE_MOVE_OPTIONS, input)) { //Move a piece
                doMove();
            } else if (isInList(HIGHLIGHT_MOVES_OPTIONS, input)) { //Do move highlighting
                doMoveHighlights();
            } else if(isInList(RESIGN_INPUT_OPTIONS, input)) { //Resign
                doResign();
            } else {
                System.out.println("That input was not recognized, please try again.\n");
                waitForInput();
            }
        }
        programExitDisplay();
    }

    //Similar to Run, but only used for observers.
    public void observerRun() {
        //IMPLEMENTATION NOT COMPLETE - JUST DISPLAYS GAME THEN RETURNS
        String input;
        while(true) {
            displayState = DisplayState.OBSERVER_MENU;
            displayFunctions.displayObserverMenu(game, selectedPosition, legalMoves, notifications);
            input = sc.next();
            sc.nextLine(); //Move scanner up to next input position to avoid problems on the next read.
            if(isInputExit(input) || isInList(LEAVE_INPUT_OPTIONS, input)) {
                try {
                    doLeave();
                } catch (ClientDataAccessException ex) {
                    errorHandler(ex);
                    continue;
                }
                break;
            } else if (isInputHelp(input) || isInList(HELP_MENU_INDEX_OPTIONS, input)) {
                displayState = DisplayState.OBSERVER_HELP;
                displayFunctions.displayObserverHelp(game, selectedPosition, legalMoves, notifications);
            } else if (isInList(REDRAW_BOARD_OPTIONS, input)) {
                clearHighlighting();
                //No additional instructions needed, board will redraw when loop resets.
            } else if (isInList(HIGHLIGHT_MOVES_OPTIONS, input)) {
                doMoveHighlights();
            } else {
                System.out.println("That input was not recognized, please try again.\n");
                waitForInput();
            }
        }
        programExitDisplay();
    }

    private void doLeave() throws ClientDataAccessException {
        //ASK SERVER TO LEAVE
        webSocketFacade.sendToServer(new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, game.gameID()));
    }

    private void doMove() {
        //Clear legalMoves so that the helper functions of doMove can do their own highlighting
        clearHighlighting();

        ChessPosition moveFrom = getMoveFrom();
        //If exit function is given, exit without doing anything.
        if(moveFrom == null) {
            return;
        }

        //Highlight moves for selected piece
        this.selectedPosition = moveFrom;
        legalMoves = game.game().validMoves(selectedPosition);

        //Find position to move to
        ChessPosition moveTo = getMoveTo();

        //If exit function is given, reset so a new piece to move may be selected.
        if(moveTo == null) {
            return;
        }

        //Pawn promotion
        ChessPiece.PieceType promotionType = null;
        if(game.game().getBoard().getPiece(moveFrom).getPieceType().equals(ChessPiece.PieceType.PAWN)) {
            if((myColor.equals(ChessGame.TeamColor.BLACK) && moveTo.getRow() == 1) ||
                (myColor.equals(ChessGame.TeamColor.WHITE) && moveTo.getRow() == 8)) {
                    promotionType = getPromotionType();
            }
        }

        //moveFrom and moveTo are valid, clear board highlighting and execute move.
        clearHighlighting();

        //Send to server to move piece
        ChessMove move = new ChessMove(moveFrom, moveTo, promotionType);
        webSocketFacade.sendToServer(new MakeMoveCommand(move, UserGameCommand.CommandType.MAKE_MOVE, authToken, game.gameID()));

    }

    public ChessPiece.PieceType getPromotionType() {
        displayState = DisplayState.PAWN_PROMOTION;
        displayFunctions.displayPawnPromotion(game, selectedPosition, legalMoves, notifications);
        String input = sc.next();
        sc.nextLine();
        clearScreen();
        if("queen".equalsIgnoreCase(input)) {
            return ChessPiece.PieceType.QUEEN;
        } else if("rook".equalsIgnoreCase(input)) {
            return ChessPiece.PieceType.ROOK;
        } else if("bishop".equalsIgnoreCase(input)) {
            return ChessPiece.PieceType.BISHOP;
        } else if("knight".equalsIgnoreCase(input)) {
            return ChessPiece.PieceType.KNIGHT;
        } else if("pawn".equalsIgnoreCase(input)) {
            return ChessPiece.PieceType.PAWN;
        } else {
            System.out.println("Error: Input not recognized, please try again.");
            return getPromotionType();
        }
    }

    private void doMoveHighlights() {
        displayState = DisplayState.HIGHLIGHT_INPUT;
        clearHighlighting(); //Make sure previous iteration of legal moves does not interfere with new iteration.
        displayFunctions.displayHighlightInput(game, selectedPosition, legalMoves, notifications);

        String input = sc.next().toLowerCase();
        sc.nextLine(); //Prep for next input

        // Check if an exit command was given
        if(isInputExit(input)) {
            clearHighlighting();
            System.out.println("Exit code detected, returning to previous menu.");
            waitForInput();
            return;
        }

        // Check if input is a valid move, recurse if it is not.
        if(!validatePosition(input)) {
            System.out.println("The provided input is not a valid space, please try again.");
            waitForInput();
            doMoveHighlights();
            return;
        }

        //Converting input to int data, and converting that data into a chess position
        int row = (int)input.charAt(1) - 48; //Convert string 1-8 to int 1-8;
        int col = (int)input.charAt(0) - 96; //Convert a-h to 1-8
        ChessPosition selectedPosition = new ChessPosition(row, col);

        // Pulling the data from the indicated chess position.
        ChessPiece pieceToMove = game.game().getBoard().getPiece(selectedPosition);

        // Check if the space is occupied, ask for new input
        if(pieceToMove == null) {
            System.out.println("The provided input is an unoccupied space, please try again.");
            waitForInput();
            doMoveHighlights();
            return;
        }

        // Chess piece is real, check for and save legal moves
        this.selectedPosition = selectedPosition;
        legalMoves = game.game().validMoves(selectedPosition);
    }

    private void doResign() {
        //Confirm resignation
        displayState = DisplayState.RESIGN_CONFIRM;
        displayFunctions.displayResignConfirm(game, selectedPosition, legalMoves, notifications);

        String input = sc.next();
        sc.nextLine();
        if(isInputExit(input)) {
            if(isInputExit(input)) {
                System.out.println("Exit code detected, returning to previous menu.");
                waitForInput();
            }
        } else if(isInList(DECLINE_INPUT_OPTIONS, input)) {
            System.out.println("Returning to game!");
            waitForInput();
        } else if(isInList(CONFIRM_INPUT_OPTIONS, input)) {
            webSocketFacade.sendToServer(new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, game.gameID()));
            System.out.println("You have resigned.");
            waitForInput();
        } else {
            System.out.println("Error: That input is not recognized! Please try again!");
            waitForInput();
            doResign();
        }
    }

    private  void programExitDisplay() {
        clearScreen();
        System.out.println("You have exited the game.");
        waitForInput();
    }


    /////////////////////////////////////////
    // Helper functions
    /////////////////////////////////////////

    /// doMove() helper functions
    private ChessPosition getMoveFrom() {
        displayState = DisplayState.MAKE_MOVE_FROM;
        clearScreen();
        displayFunctions.displayMoveFrom(game, selectedPosition, legalMoves, notifications);

        String input = sc.next().toLowerCase();
        sc.nextLine(); //Prep for next input

        // Check if an exit command was given
        if(isInputExit(input)) {
            System.out.println("Exit code detected, returning to previous menu.");
            waitForInput();
            return null;
        }

        //Verify the given position exists
        if(!validatePosition(input)) {
            System.out.println("That is not a valid space, please try again.");
            waitForInput();
            return getMoveFrom();
        }

        //Verify there is a piece at the given position
        ChessPosition currentPosition = inputToPosition(input);
        ChessPiece currentPiece = game.game().getBoard().getPiece(currentPosition);
        if(currentPiece == null) {
            System.out.println("There is no piece at that position, please try again.");
            waitForInput();
            return getMoveFrom();
        }

        //Verify that the piece is from this user's team
        if(currentPiece.getTeamColor() != myColor) {
            System.out.println("The selected piece is from the other team!  Please try again.");
            waitForInput();
            return getMoveFrom();
        }

        //Selected piece is valid
        return currentPosition;
    }

    private ChessPosition getMoveTo() {
        displayState = DisplayState.MAKE_MOVE_TO;
        clearScreen();
        displayFunctions.displayMoveTo(game, selectedPosition, legalMoves, notifications);

        String input = sc.next().toLowerCase();
        sc.nextLine(); //Prep for next input

        // Check if an exit command was given
        if(isInputExit(input)) {
            System.out.println("Exit code detected, a new piece to move may be selected.");
            waitForInput();
            return null;
        }

        //Verify the given position exists
        if(!validatePosition(input)) {
            System.out.println("That is not a valid space, please try again.");
            waitForInput();
            return getMoveTo();
        }

        //Verify the position is legal
        ChessPosition moveTo = inputToPosition(input);
        for(ChessMove move : legalMoves) {
            if(move.getEndPosition().equals(moveTo)) {
                return moveTo;
            }
        }

        //Given position is not a legal move
        System.out.println("That is not a legal move, please try again.");
        waitForInput();
        return getMoveTo();
    }

    private ChessPosition inputToPosition(String input) {
        return new ChessPosition((int)input.charAt(1)-48, (int)input.charAt(0)-96);
    }

    private boolean validatePosition(String input) {
        return (input.length() == 2) && (input.matches("[a-h][1-8]"));
    }

    /// Draw Functions
    private void clearHighlighting() {
        legalMoves.clear();
        selectedPosition = null;
    }

    public void updateScreen() {
        switch(displayState) {
            case HIGHLIGHT_INPUT:
                displayFunctions.displayHighlightInput(game, selectedPosition, legalMoves, notifications);
                break;
            case MAIN_MENU:
                displayFunctions.displayMenu(game, selectedPosition, legalMoves, notifications);
                break;
            case HELP:
                displayFunctions.displayHelp(game, selectedPosition, legalMoves, notifications);
                break;
            case OBSERVER_MENU:
                displayFunctions.displayObserverMenu(game, selectedPosition, legalMoves, notifications);
                break;
            case OBSERVER_HELP:
                displayFunctions.displayObserverHelp(game, selectedPosition, legalMoves, notifications);
                break;
            case PAWN_PROMOTION:
                displayFunctions.displayPawnPromotion(game, selectedPosition, legalMoves, notifications);
                break;
            case MAKE_MOVE_FROM:
                displayFunctions.displayMoveFrom(game, selectedPosition, legalMoves, notifications);
                break;
            case MAKE_MOVE_TO:
                displayFunctions.displayMoveTo(game, selectedPosition, legalMoves, notifications);
                break;
            case RESIGN_CONFIRM:
                displayFunctions.displayResignConfirm(game, selectedPosition, legalMoves, notifications);
                break;
        }
    }

    //Websocket functions
    public void updateGame(LoadGameMessage loadGame) {
        game = loadGame.getGame();
        clearScreen();
        updateScreen();
    }

    public void notify(String newNotification) {
        if(notifications.size() >= 10) {
            notifications.remove(1);
        }
        notifications.add(newNotification);
        clearScreen();
        updateScreen();
    }
}