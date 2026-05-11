package client;

import static ui.EscapeSequences.*;
import java.util.Scanner;
import chess.ChessGame.TeamColor;
import client.ClientDataAccessException.FailCode;


public class CommonREPLFunctions {
    //Lists for common formats representing CLI commands.
    private static final String[] EXIT_INPUT_OPTIONS =
            {"0", "0.", "quit", "-quit", "--quit", "q", "-q", "--q", "exit", "-exit", "--exit", "e", "-e", "--e"};
    private static final String[] HELP_INPUT_OPTIONS =
            {"411", "411.", "help", "-help", "--help", "h", "-h", "--h", "tldr", "man", "manual"};

    //Lists for common formats for user to give TeamColor
    private static final String[] WHITE_TEAM_INPUT_OPTIONS =
            {"0", "0.", "white", "-white", "--white", "w", "-w", "--w"};
    private static final String[] BLACK_TEAM_INPUT_OPTIONS =
            {"1", "1.", "black", "-black", "--black", "b", "-b", "--b"};

    //Important varaibles for REPL functions
    Scanner sc = new Scanner(System.in);

    // Check if a string exists in a list (case insensitive).  Useful for REPL options with multiple allowed inputs.
    public boolean isInList(String[]stringList, String input) {
        String adjustedInput = input.toLowerCase();
        for(String option : stringList) {
            if(option.equals(adjustedInput)) {
                return true;
            }
        }
        return false;
    }

    //Checks if the help menu should be opened (shortcut function so the option list doesn't have to be passed).
    public boolean isInputHelp(String input) {
        return isInList(HELP_INPUT_OPTIONS, input);
    }

    //Checks if program should be exited (shortcut function so the option list doesn't have to be passed).
    public boolean isInputExit(String input) {
        return isInList(EXIT_INPUT_OPTIONS, input);
    }

    //Clear the screen
    public void clearScreen() {
        System.out.print(RESET_TEXT_COLOR);
        System.out.println(RESET_BG_COLOR);
        System.out.print(ERASE_SCREEN);
    }

    //Move to Next Line, used to combine recoloration and new line call
    public void lineReturn() {
        System.out.print(RESET_TEXT_COLOR);
        System.out.println(RESET_BG_COLOR);
    }

    //Pause on a screen until enter is pressed
    public void waitForInput() {
        System.out.print("Press enter to continue: ");

        //Brief timer to prevent the menu from passing before the user has let go of the enter key
        try{Thread.sleep(500);}
        catch(Exception ignore){}

        sc.nextLine();
    }

    //Check if input mid-command is an exit code
    public boolean exitCommandEarly(String input) {
        if(isInputExit(input)) {
            System.out.println("\nExit code given, returning to previous menu.");
            waitForInput();
            return true;
        }
        return false;
    }

    public TeamColor parseTeam(String input) {
        if(isInList(WHITE_TEAM_INPUT_OPTIONS, input)) {
            return TeamColor.WHITE;
        } else if(isInList(BLACK_TEAM_INPUT_OPTIONS, input)) {
            return TeamColor.BLACK;
        }
        return null; //Failure
    }

    public void errorHandler(ClientDataAccessException ex, String errorMessage) {
        System.out.println(errorMessage);
        waitForInput();
    }

    public void errorHandler(ClientDataAccessException ex) {
        switch (ex.getFailCode()) {
            case FailCode.badRequest:
                errorHandler(ex, "Error: The server rejected that request!");
                break;
            case FailCode.alreadyTaken:
                errorHandler(ex, "Error: That username is already taken!");
                break;
            case FailCode.unauthorized:
                errorHandler(ex, "Error: Action unauthorized.");
                break;
            default:
                errorHandler(ex, "Unknown Error: Action failed, returning to previous menu.");
                break;
        }

    }
}
