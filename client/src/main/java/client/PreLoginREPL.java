package client;

import client.RequestDataStructures.*;

public class PreLoginREPL extends CommonREPLFunctions{
    //Lists to make it easier to verify input meanings
    private static final String[] REGISTER_INPUT_OPTIONS =
            {"1", "1.", "register", "-register", "--register", "reg", "-reg", "--reg", "r", "-r", "--r"};
    private static final String[] LOGIN_INPUT_OPTIONS =
            {"2", "2.", "login", "-login", "--login", "log", "-log", "--log", "l", "-l", "--l"};
    private static final String[] CLEAR_INPUT_OPTIONS =
            {"3", "3.", "clear", "-clear", "--clear", "c", "-c", "--c"};
    private static final String[] HELP_MENU_INDEX_OPTIONS = {"4", "4."};


    //Object properties
    private final ServerFacade server;
    private String url;


    public PreLoginREPL(String url) {
        server = new ServerFacade(url);
        this.url = url;
    }

    public void run() {
        String input;
        while(true) {
            displayMenu();
            input = sc.next();
            sc.nextLine(); //Move scanner up to next input position, ignore anything else on the current line
            if(isInputExit(input)) {
                break;
            } else if(isInputHelp(input) || isInList(HELP_MENU_INDEX_OPTIONS, input)) {
                displayHelp();
            } else if(isInList(REGISTER_INPUT_OPTIONS, input)) {
                startRegister();
            } else if(isInList(LOGIN_INPUT_OPTIONS, input)) {
                startLogin();
            } else if(isInList(CLEAR_INPUT_OPTIONS, input)) {
                clearServer();
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
        System.out.println("            WELCOME TO CHESS");
        System.out.println("----------------------------------------");
        System.out.println("Please select from the following options:");
        System.out.println(">> 1. Register");
        System.out.println(">> 2. Login");
        System.out.println(">> 3. Clear");
        System.out.println(">> 4. Help");
        System.out.println(">> 0. Exit");
        System.out.println();
        System.out.print(  "Input >> ");
    }

    private void displayHelp() {
        clearScreen();
        System.out.println("----------------------------------------");
        System.out.println("                  HELP");
        System.out.println("----------------------------------------");
        System.out.println("Here are the available options from this menu:");
        System.out.println("\tRegister  (-r) Create a new account with the Chess server.");
        System.out.println("\tLogin     (-l) Login to an existing account on the Chess Server.");
        System.out.println("\tClear     (-c) Completely clear the server database.");
        System.out.println("\tHelp      (-h) Redirect to the help page of the given menu.");
        System.out.println("\tExit      (-q) Exit the given menu, or exit the program.");
        System.out.println();
        waitForInput();
    }

    private void clearServer() {
        try {
            server.doClear();
            System.out.println("Server database has been cleared");
            waitForInput();
        } catch (ClientDataAccessException ex) {
            errorHandler(ex);
        }
    }

    private void startRegister() {
        clearScreen();
        System.out.println("----------------------------------------");
        System.out.println("              REGISTRATION");
        System.out.println("----------------------------------------");

        System.out.print("New Username: ");
        String username = sc.next();
        sc.nextLine(); //Move scanner up to next input position, ignore anything else on the current line
        if(exitCommandEarly(username)) {
            return;
        }

        System.out.print("New Password: ");
        String password = sc.next();
        sc.nextLine(); //Move scanner up to next input position, ignore anything else on the current line
        if(exitCommandEarly(password)) {
            return;
        }

        System.out.print("New Email: ");
        String email = sc.next();
        sc.nextLine(); //Move scanner up to next input position, ignore anything else on the current line
        if(exitCommandEarly(email)) {
            return;
        }

        //IMPLEMENT REGISTER HERE
        try {
            LoginResult registerResult = server.doRegister(new RegisterRequest(username, password, email));
            System.out.println("Your account has been successfully created!");
            waitForInput();
            movePostLogin(registerResult);
        } catch(ClientDataAccessException ex) {
            errorHandler(ex);
        }
    }

    private void startLogin() {
        clearScreen();
        System.out.println("----------------------------------------");
        System.out.println("                 LOG-IN");
        System.out.println("----------------------------------------");

        System.out.print("Username: ");
        String username = sc.next();
        sc.nextLine(); //Move scanner up to next input position, ignore anything else on the current line
        if(exitCommandEarly(username)) {
            return;
        }

        System.out.print("Password: ");
        String password = sc.next();
        sc.nextLine(); //Move scanner up to next input position, ignore anything else on the current line
        if(exitCommandEarly(password)) {
            return;
        }

        //IMPLEMENT LOGIN HERE
        try {
            LoginResult loginResult = server.doLogin(new LoginRequest(username, password));
            movePostLogin(loginResult);
        } catch(ClientDataAccessException ex) {
            errorHandler(ex);
        }
    }

    private void movePostLogin(LoginResult loginResult) {
        new PostLoginREPL(server, loginResult.username(), loginResult.authToken(), url).run();
    }

    private void programExitDisplay() {
        clearScreen();
        System.out.println("The program has ended.");
        System.out.println();
        System.out.println("Have a great day!");
    }
}
