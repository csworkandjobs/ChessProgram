# ChessProgram
--SUMMARY--
Java chess program that allows two remote clients to play the same game on a server with any number of observers.


--FUNCTIONALITY OVERVIEW--
Account registration, login, game creation, and game selection are handled using HTTP requests.  Passwords are hashed upon arrival to the server, and server information is made persistent through the use of MySQL databases.  Actual game management after a player has joined a given game is handeled using Websocket.

Many classes are shared by both the client and server, including chess game objects, chess move validation, and classes specifically designed to carry data between client and server.

Server exclusive classes focus on validating that provided information is of correct form, provided by authorized users, and contain legal actions.  Additionally, is also acts to make sure that its own persistent databases remain up-to-date and will send game updates to relevant users when something happens (player or observer joins, move was made, player disconnection, checkmate, etc.).

Client exclusive functions focus on robust and user-friendly interfaces.  The screen is automatically cleared after actions to ensure a clean output.  Many user input options are implemented behind the scenes to allow commands to be implemented in several ways depending on user preference.  Additionally, almost every user input is checked for an exit command, meaning at almost any point the user can revert to the previous menu.  Once entering the game, a colorized chess game is presented, as well as a notification menu.  From here, a user can take several actions, including highlighting moves from any piece (friendly and opposing, with yellow indicating the selected piece, green indicating a valid open space to move to, and red indicating a valid piece to capture), resignation, or leaving the game (without resigning, the spot becomes open for another player to join in).  Additionally, any number of observers can join a game to watch as well, with their own notification window and move highlighting option, but naturally cannot actually move pieces.


--NEXT STEPS--
The next steps for this program would be communication encryption (using public-private key encryption to pass through a symmetric key which would be used in all future communications), and thread syncronization.  Both were ignored up to this point because they were not part of the original spec (the original specifications had the client and the server on the same machine and was focused more on functionality and development speed rather than security).


--TESTING NOTES--
At current time, a clear function is available to the user to delete the server databases.  This is meant for testing purposes only and the relevant code on both client and server is to be removed before actual distribution.

Note that some testing functions only work if both client and server are run on the same machine as they use both client and server functions to ensure that actions between the two are functioning correctly.


--OTHER--
Project had to be uploaded in chunks due to GitHub limitations on number of files that can be uploaded at once.

Program was developed using the IntelliJ IDE and used Maven for compilation.

Project was developed into its current form between January and April of 2026.
