package client;

import model.GameData;
import static chess.ChessGame.TeamColor;
import client.RequestDataStructures.*;

public class PostLoginREPL extends CommonREPLFunctions {
    //Lists to make it easier to verify input meanings
    private static final String[] CREATE_GAME_INPUT_OPTIONS =
            {"1", "1.", "create game", "creategame", "create", "cre", "c", "-c", "--c", "cg", "-cg", "--cg"};
    private static final String[] LIST_GAMES_INPUT_OPTIONS =
            {"2", "2.", "list games", "list", "listgames", "games", "g", "-g", "--g", "lg", "-lg", "--lg"};
    private static final String[] LOAD_GAME_INPUT_OPTIONS =
            {"3", "3.", "load game", "load", "loadgame", "p", "-p", "--p", "pg", "-pg", "--pg"};
    private static final String[] OBSERVE_GAME_INPUT_OPTIONS =
            {"4", "4.", "observe game", "observe", "observegame", "o", "-o", "--o", "og", "-og", "--og"};
    private static final String[] HELP_MENU_INPUT_OPTIONS = {"5", "5."};
    private static final String[] LOGOUT_INPUT_OPTIONS =
            {"logout", "log", "log out", "log-out", "logout/exit", "l", "-l", "--l", "lo", "-lo", "--lo"};



    //Object properties
    private final ServerFacade server;
    private final String username;
    private final String authToken;
    private String url;

    public PostLoginREPL(ServerFacade server, String username, String authToken, String url) {
        this.server = server;
        this.username = username;
        this.authToken = authToken;
        this.url = url;
    }

    public void run() {
        String input;
        while(true) {
            displayMenu();
            input = sc.next();
            sc.nextLine(); //Move scanner up to next input position, ignore anything else on the current line
            if(isInputExit(input) || isInList(LOGOUT_INPUT_OPTIONS, input)) {
                try {
                    server.doLogout(authToken);
                } catch (ClientDataAccessException ex) {
                    errorHandler(ex);
                    continue;
                }
                break;
            } else if(isInputHelp(input) || isInList(HELP_MENU_INPUT_OPTIONS, input)) {
                displayHelp();
            } else if(isInList(CREATE_GAME_INPUT_OPTIONS, input)) {
                createGamesMenu();
            } else if(isInList(LIST_GAMES_INPUT_OPTIONS, input)) {
                listGamesMenu();
            } else if(isInList(LOAD_GAME_INPUT_OPTIONS, input)) {
                loadGameMenu();
            } else if(isInList(OBSERVE_GAME_INPUT_OPTIONS, input)) {
                observeGameMenu();
            } else {
                System.out.println("That input was not recognized, please try again.\n");
                waitForInput();
            }
        }

        programExitDisplay();
    }

    private void displayMenu() {
        clearScreen();
        System.out.println("----------------------------------------");
        System.out.println("        WELCOME BACK " + username);
        System.out.println("----------------------------------------");
        System.out.println("Please select from the following options:");
        System.out.println(">> 1. Create Game");
        System.out.println(">> 2. List Games");
        System.out.println(">> 3. Load Game");
        System.out.println(">> 4. Observe Game");
        System.out.println(">> 5. Help");
        System.out.println(">> 0. Logout/Exit");
        System.out.println();
        System.out.print(  "Input >> ");
    }

    private void displayHelp() {
        clearScreen();
        System.out.println("----------------------------------------");
        System.out.println("                  HELP");
        System.out.println("----------------------------------------");
        System.out.println("Here are the available options from this menu:");
        System.out.println("\tCreate Game   (-c) Create a new account with the Chess server.");
        System.out.println("\tList Games    (-g) Login to an existing account on the Chess Server.");
        System.out.println("\tPlay Game     (-p) Enter a game as a player.");
        System.out.println("\tObserve Game  (-o) Enter a game as an oberver instead of as a player.");
        System.out.println("\tHelp          (-h) Redirect to the help page of the given menu.");
        System.out.println("\tLogout/Exit   (-l) Exit the given menu, or exit the program.");
        System.out.println();
        waitForInput();
    }

    private void createGamesMenu() {
        clearScreen();
        System.out.println("----------------------------------------");
        System.out.println("              CREATE GAMES");
        System.out.println("----------------------------------------");
        System.out.print("New Game Name: ");
        String gameName = sc.next();
        sc.nextLine(); //Move scanner up to next input position, ignore anything else on the current line
        if(isInputExit(gameName)) {
            System.out.println("Exit code detected, returning to previous menu.");
            waitForInput();
            return;
        }

        //IMPLEMENT CREATE GAMES REQUEST
        try {
            NewGameData newGameData = new NewGameData(gameName);
            server.doCreateGame(authToken, newGameData);
        } catch (ClientDataAccessException ex) {
            errorHandler(ex);
            return;
        }
        System.out.println("New game successfully created!");
        waitForInput();
    }

    private void listGamesMenu() {
        clearScreen();
        System.out.println("----------------------------------------");
        System.out.println("               LIST GAMES");
        System.out.println("----------------------------------------");
        System.out.println("Here are the current games:");

        try {
            listGames();
            waitForInput();
        } catch (ClientDataAccessException ex) {
            errorHandler(ex);
        }
    }

    //List games, but not as a menu (just the list)
    private GameData[] listGames() throws ClientDataAccessException {
        //IMPLEMENT GET GAMES
        GameData[] gamesList = server.doListGames(authToken);

        for(int i = 0; i <= gamesList.length-1; i++) {
            System.out.println(String.format("%d. %s \tWHITE:%s \tBLACK:%s", i+1,
                    gamesList[i].gameName(), gamesList[i].whiteUsername(), gamesList[i].blackUsername()));
        }

        if(gamesList.length == 0) {
            System.out.println("No games are currently active.");
        }

        return gamesList;
    }

    private void loadGameMenu() {
        clearScreen();
        System.out.println("----------------------------------------");
        System.out.println("               ENTER GAME");
        System.out.println("----------------------------------------");
        System.out.println("Here are the current games:");

        //List current games, and saves a copy of the list to verify gameID is valid later on
        GameData[] gamesList;
        try {
            gamesList = listGames();
        } catch (ClientDataAccessException ex) {
            errorHandler(ex);
            return;
        }

        //If the game list doesn't exist, exit back to the previous menu.
        if(gamesList.length == 0) {
            waitForInput();
            return;
        }

        //Ask user for which game they want to join.
        System.out.println();
        System.out.print("Select which game to join: ");
        String requestedGame = sc.next();
        sc.nextLine();

        //Check if exit requested
        if(isInputExit(requestedGame)) {
            System.out.println("Exit code detected, returning to previous menu.");
            waitForInput();
            return;
        }

        //Ask user for what team to join
        System.out.println();
        System.out.print("Select team would you like to join: ");
        String teamInput = sc.next();
        sc.nextLine();

        //Check if exit requested
        if(isInputExit(teamInput)) {
            System.out.println("Exit code detected, returning to previous menu.");
            waitForInput();
            return;
        }

        TeamColor selectedTeam = parseTeam(teamInput);

        //If ID is valid, join game
        if((selectedTeam != null) && (requestedGame.matches("[0-9]+"))) { //Value is a number
            int gameID = Integer.parseInt(requestedGame)-1;
            if(gameID <= gamesList.length-1) {
                new ChessREPL(server, username, authToken, selectedTeam, gamesList[gameID], url).playerRun();
                return;
            }
        }

        //Input not recognized or ID invalid
        System.out.println("Input could not be recognized, please try again.");
        waitForInput();
        loadGameMenu(); //This is the easiest way to loop back since return type is void
    }

    private void observeGameMenu() {
        clearScreen();
        System.out.println("----------------------------------------");
        System.out.println("              OBSERVE GAME");
        System.out.println("----------------------------------------");
        System.out.println("Here are the current games:");

        //List current games, but save a copy of the list to verify valid gameID
        GameData[] gamesList;
        try {
            gamesList = listGames();
        } catch (ClientDataAccessException ex) {
            errorHandler(ex);
            return;
        }

        //Do not continue if not games exist
        if(gamesList.length == 0) {
            waitForInput();
            return;
        }

        System.out.println();
        System.out.print("Select which game to join: ");
        String input = sc.next();
        sc.nextLine();

        if(isInputExit(input)) {
            System.out.println("Exit code detected, returning to previous menu.");
            waitForInput();
            return;
        }

        if(input.matches("[0-9]+")) { //Value is a number
            int gameID = Integer.parseInt(input)-1;
            if(gameID <= gamesList.length-1) {
                new ChessREPL(server, username, authToken, null, gamesList[gameID], url).observerRun();
                return;
            }
        }

        System.out.println("Input could not be recognized, please try again.");
        waitForInput();
        observeGameMenu(); //This is the easiest way to loop back since return type is void
    }

    private void programExitDisplay() {
        clearScreen();
        System.out.println("You have been logged out.");
        waitForInput();
    }
}
