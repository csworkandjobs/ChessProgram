package chess;
import java.util.Collection;
import java.util.ArrayList;
import chess.ChessPiece.PieceType;
public class ChessGame {
    private ChessBoard gameBoard = new ChessBoard();
    private TeamColor teamTurn = TeamColor.WHITE;
    private ChessPosition whiteKingPosition = null;
    private ChessPosition blackKingPosition = null;
    private ChessPosition enPassantOpening = null;
    private boolean[][] castlingOpenings = {{true, true}, {true, true}}; //[[whiteCastleLeft, whiteCastleRight], [blackCastleLeft, blackCastleRight]]
    public ChessGame() {
        gameBoard.resetBoard();
    }
    public TeamColor getTeamTurn() {
        return teamTurn;
    }
    public void setTeamTurn(TeamColor team) {
        teamTurn = team;
    }
    public enum TeamColor {
        WHITE,
        BLACK
    }
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece currentPiece = gameBoard.getPiece(startPosition);
        if(currentPiece == null) {
            return null;
        }
        Collection<ChessMove> possibleMoves = currentPiece.pieceMoves(gameBoard, startPosition);
        checkEnPassantOpportunity(currentPiece, startPosition, possibleMoves);
        checkCastlingOpportunity(currentPiece, startPosition, possibleMoves);
        ArrayList<ChessMove> legalMoves = new ArrayList<>();
        gameBoard.addPiece(startPosition, null);
        for(ChessMove move : possibleMoves) {
            ChessPosition moveTo = move.getEndPosition();
            ChessPiece saveEndPositionPiece = gameBoard.getPiece(moveTo);
            gameBoard.addPiece(moveTo, currentPiece);
            updateKingPosition(currentPiece, moveTo);
            if(!isInCheck(currentPiece.getTeamColor())) {
                legalMoves.add(move);
            }
            gameBoard.addPiece(moveTo, saveEndPositionPiece);
        }
        gameBoard.addPiece(startPosition, currentPiece);
        updateKingPosition(currentPiece, startPosition);

        return legalMoves;
    }
    public void checkEnPassantOpportunity(ChessPiece currentPiece, ChessPosition piecePosition,
                                          Collection<ChessMove> possibleMoves) {
        if(enPassantOpening == null) {
            return;
        }
        if(currentPiece.getPieceType() != PieceType.PAWN) {
            return;
        }
        int pieceCol = piecePosition.getColumn();
        int enPassantCol = enPassantOpening.getColumn();
        if(Math.abs(pieceCol-enPassantCol) != 1) {
            return;
        }
        int pieceRow = piecePosition.getRow();
        TeamColor pieceColor = currentPiece.getTeamColor();
        if((pieceColor == TeamColor.WHITE && pieceRow == 5) || //En Passant by white
           (pieceColor == TeamColor.BLACK && pieceRow == 4)){ //En Passant by black
            possibleMoves.add(new ChessMove(piecePosition, enPassantOpening, null));
        }
    }
    public void checkCastlingOpportunity(ChessPiece currentPiece, ChessPosition piecePosition, Collection<ChessMove> possibleMoves) {
        if(currentPiece.getPieceType() != PieceType.KING) {
            return;
        }
        int teamSelect = 0; //Default to white
        int row = 1;
        if(currentPiece.getTeamColor() == TeamColor.BLACK) {
            teamSelect = 1;
            row = 8;
        }
        for(int i = 0; i < 2; i++) {
            int rookCol = 1;
            int moveKingToCol = 3;
            if(i == 1) {
                rookCol = 8;
                moveKingToCol = 7;
            }

            if(castlingOpenings[teamSelect][i] &&
                    checkSpacesEmptyCastling(row, rookCol) &&
                    checkKingNotInCheck(row, rookCol)) {
                ChessPosition moveTo = new ChessPosition(row, moveKingToCol);
                possibleMoves.add(new ChessMove(piecePosition, moveTo, null));
            }
        }
    }
    public boolean checkSpacesEmptyCastling(int row, int rookCol) {
        int lowerCol = 3;
        int upperCol = 4;
        if(rookCol == 8) {
            lowerCol = 6;
            upperCol = 7;
        }

        for(int i = lowerCol; i <= upperCol; i++) {
            if(gameBoard.getPiece(new ChessPosition(row, i)) != null) {
                return false;
            }
        }
        return true;
    }
    public boolean checkKingNotInCheck(int row, int rookCol) {
        int lowerCol = 3;
        int upperCol = 5;
        if(rookCol == 8) {
            lowerCol = 5;
            upperCol = 7;
        }
        ChessPosition startingPosition = new ChessPosition(row, 5);
        ChessPiece movingKing = gameBoard.getPiece(startingPosition);
        TeamColor kingTeam = movingKing.getTeamColor();
        gameBoard.addPiece(startingPosition, null);
        for(int i = lowerCol; i <= upperCol; i++) {
            ChessPosition moveTo = new ChessPosition(row, i);
            gameBoard.addPiece(moveTo, movingKing);
            updateKingPosition(movingKing, moveTo);

            if(isInCheck(kingTeam)) {
                gameBoard.addPiece(moveTo, null);
                gameBoard.addPiece(startingPosition, movingKing);
                return false;
            }
            gameBoard.addPiece(moveTo, null);
        }
        gameBoard.addPiece(startingPosition, movingKing);
        updateKingPosition(movingKing, startingPosition);
        return true;
    }
    public void updateKingPosition(ChessPiece movedPiece, ChessPosition newPosition) {
        if(movedPiece.getPieceType() == PieceType.KING) {
            if(movedPiece.getTeamColor() == TeamColor.WHITE) {
                whiteKingPosition = newPosition;
            } else {
                blackKingPosition = newPosition;
            }
        }
    }
    public boolean checkMoveLegality(ChessPosition startPosition, ChessMove tryMove) {
        Collection<ChessMove> possibleMoves = validMoves(startPosition);
        for(ChessMove move : possibleMoves) {
            if(tryMove.equals(move)) {
                return true;
            }
        }
        return false;
    }
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPosition moveFrom = move.getStartPosition();
        ChessPosition moveTo = move.getEndPosition();
        ChessPiece currentPiece = gameBoard.getPiece(moveFrom);
        if(currentPiece == null) {
            throw new chess.InvalidMoveException("Given ChessMove does not relate to a piece.");
        }
        if(currentPiece.getTeamColor() != teamTurn) {
            throw new chess.InvalidMoveException("Wrong turn being taken.");
        }
        if(!checkMoveLegality(moveFrom, move)) {
            throw new chess.InvalidMoveException("Given move is invalid.");
        }
        gameBoard.addPiece(moveFrom, null);
        gameBoard.addPiece(moveTo, currentPiece);
        doSpecialPieceChanges(moveTo, move, currentPiece);
        if(teamTurn == TeamColor.WHITE) {
            setTeamTurn(TeamColor.BLACK);
        } else {
            setTeamTurn(TeamColor.WHITE);
        }
    }
    public void doSpecialPieceChanges(ChessPosition moveTo, ChessMove move, ChessPiece movedPiece) {
        switch(movedPiece.getPieceType()) {
            case PieceType.KING:
                doCastleMove(moveTo, move);
                updateKingPosition(moveTo);
                disableCastleOpenings(movedPiece);
                break;
            case PieceType.ROOK:
                disableCastlingByRook(movedPiece, move.getStartPosition());
                break;
            case PieceType.PAWN:
                checkForEnPassantCapture(moveTo);
                applyPawnPromotion(move, moveTo);
                break;
        }
        updateEnPassantOpening(move, moveTo, movedPiece);
    }
    public void doCastleMove(ChessPosition moveKingTo, ChessMove move) {
        int moveToCol = moveKingTo.getColumn();
        int moveFromCol = move.getStartPosition().getColumn();
        if(Math.abs(moveToCol - moveFromCol) != 2) {
            return;
        }
        int row = moveKingTo.getRow();
        int moveRookToCol = 4;
        int moveRookFromCol = 1;
        if(moveToCol == 7) {
            moveRookToCol = 6;
            moveRookFromCol = 8;
        }
        ChessPosition moveRookFrom = new ChessPosition(row, moveRookFromCol);
        ChessPosition moveRookTo = new ChessPosition(row, moveRookToCol);
        ChessPiece movingRook = gameBoard.getPiece(moveRookFrom);
        gameBoard.addPiece(moveRookFrom, null);
        gameBoard.addPiece(moveRookTo, movingRook);
    }
    public void updateKingPosition(ChessPosition moveTo) {
        if(teamTurn == TeamColor.WHITE) {
            whiteKingPosition = moveTo;
        } else {
            blackKingPosition = moveTo;
        }
    }
    public void disableCastleOpenings(ChessPiece movedKing) {
        TeamColor kingTeam = movedKing.getTeamColor();
        int teamSelect = 0;
        if(kingTeam == TeamColor.BLACK) {
            teamSelect = 1;
        }

        castlingOpenings[teamSelect] = new boolean[]{false, false};
    }
    public void disableCastlingByRook(ChessPiece movedRook, ChessPosition rookPosition) {
        TeamColor kingTeam = movedRook.getTeamColor();
        int teamSelect = 0;
        if(kingTeam == TeamColor.BLACK) {
            teamSelect = 1;
        }

        int rookCol = rookPosition.getColumn();
        if(rookCol == 1) {
            castlingOpenings[teamSelect][0] = false;
        } else if(rookCol == 8){
            castlingOpenings[teamSelect][1] = false;
        }
    }
    public void checkForEnPassantCapture(ChessPosition moveTo) {
        if(!moveTo.equals(enPassantOpening)) {
            return;
        }
        int movedToRow = moveTo.getRow();
        int movedToCol = moveTo.getColumn();
        ChessPosition removeFromPosition = new ChessPosition(movedToRow+1, movedToCol);
        if(movedToRow == 6) {
            removeFromPosition = new ChessPosition(movedToRow-1, movedToCol);
        }

        gameBoard.addPiece(removeFromPosition, null);
    }
    public void updateEnPassantOpening(ChessMove move, ChessPosition moveTo, ChessPiece movedPiece) {
        enPassantOpening = null;
        if(movedPiece.getPieceType() != PieceType.PAWN) {
            return;
        }
        int initialRow = move.getStartPosition().getRow();
        int endRow = moveTo.getRow();
        int totalRowMovement = endRow - initialRow;
        int direction = definePawnDirection(movedPiece);

        if(Math.abs(totalRowMovement) == 2) {
            enPassantOpening = new ChessPosition(initialRow+direction, moveTo.getColumn());
        }
    }
    public int definePawnDirection(ChessPiece movingPawn) {
        TeamColor teamColor = movingPawn.getTeamColor();
        int direction = 1;
        if(teamColor == TeamColor.BLACK) {
            direction = -1;
        }
        return direction;
    }
    public void applyPawnPromotion(ChessMove move, ChessPosition moveTo) {
        PieceType promotionType = move.getPromotionPiece();
        if(promotionType == null) {
            return;
        }
        TeamColor pawnColor = gameBoard.getPiece(moveTo).getTeamColor();
        ChessPiece promotionPiece = new ChessPiece(pawnColor, promotionType);
        gameBoard.addPiece(moveTo, promotionPiece);
    }
    public boolean isInCheck(TeamColor teamColor) {
        if (whiteKingPosition == null || blackKingPosition == null) {
            findKings();
        }
        ChessPosition kingPosition = whiteKingPosition;
        int direction = 1;
        if(teamColor == TeamColor.BLACK) {
            kingPosition = blackKingPosition;
            direction = -1;
        }
        int [][] modifiers = {{0,1}, {1,0}, {0,-1}, {-1,0}, {1,1}, {1,-1}, {-1,-1}, {-1,1}};
        for(int [] modifier : modifiers) {
            if(isDangerInLine(kingPosition, modifier, teamColor)) {
                return true;
            }
        }
        int[][] knightAttackPositions = {{-2,-1}, {-2,1}, {-1,2}, {1,2}, {2,1}, {2,-1}, {1,-2}, {-1,-2}};
        if(isEnemyInSpecificSpots(kingPosition, knightAttackPositions, PieceType.KNIGHT, teamColor)) {
            return true;
        }
        int[][] kingAttackPositions = {{-1,-1}, {-1,0}, {-1,1}, {0,-1}, {0,1}, {1,-1}, {1,0}, {1,1}};
        if(isEnemyInSpecificSpots(kingPosition, kingAttackPositions, PieceType.KING, teamColor)) {
            return true;
        }
        int[][] pawnAttackPositions = {{direction,1}, {direction,-1}};
        if(isEnemyInSpecificSpots(kingPosition, pawnAttackPositions, PieceType.PAWN, teamColor)) {
            return true;
        }
        return false;
    }
    public void findKings() {
        for(int i = 0; i < 8; i++) {
            for(int j = 0; j < 8; j++) {
                ChessPosition currentPosition = new ChessPosition(i+1, j+1);
                ChessPiece currentPiece = gameBoard.getPiece(currentPosition);
                if(currentPiece == null) {
                    continue;
                }
                if(currentPiece.getPieceType() != PieceType.KING) {
                    continue;
                }
                if (currentPiece.getTeamColor() == TeamColor.WHITE) {
                    whiteKingPosition = currentPosition;
                } else {
                    blackKingPosition = currentPosition;
                }
                if (whiteKingPosition != null && blackKingPosition != null) {
                    break;
                }
            }
        }
    }
    public boolean isEnemyInSpecificSpots(ChessPosition kingPosition, int[][] modifiers, PieceType attackType, TeamColor teamColor) {
        int kingRow = kingPosition.getRow();
        int kingCol = kingPosition.getColumn();
        for(int[] modifier : modifiers) {
            ChessPosition attackerPosition = new ChessPosition(kingRow+modifier[0], kingCol+modifier[1]);
            if(!ChessMovesCalculator.isSquareInBounds(attackerPosition)) {
                continue;
            }
            ChessPiece attacker = gameBoard.getPiece(attackerPosition);
            if(attacker == null) {
                continue;
            }
            if(attacker.getTeamColor() == teamColor) {
                continue;
            }
            if(attacker.getPieceType() == attackType) {
                return true;
            }
        }
        return false;
    }
    public boolean isDangerInLine(ChessPosition kingPosition, int[] modifier, TeamColor teamColor) {
        ChessPosition nextEnemyPosition = nextHostilePieceInLine(kingPosition, modifier, teamColor);
        if(nextEnemyPosition == null) {
            return false;
        }
        PieceType[] validAttackers;
        if(modifier[0] == 0 || modifier[1] == 0) { //The path of attack is along vertical or horizonal lines.
            validAttackers = new PieceType[]{PieceType.QUEEN, PieceType.ROOK};
        } else { //The path of attack is along horizontal lines.
            validAttackers = new PieceType[]{PieceType.QUEEN, PieceType.BISHOP};
        }

        PieceType enemyPieceType = gameBoard.getPiece(nextEnemyPosition).getPieceType();
        for(PieceType attackType : validAttackers) {
            if(enemyPieceType == attackType) {
                return true;
            }
        }
        return false;
    }
    public ChessPosition nextHostilePieceInLine(ChessPosition kingPosition, int[] modifier, TeamColor teamColor) {
        TeamColor nextSquareStatus = null;
        ChessPosition nextPosition = kingPosition;
        while (nextSquareStatus == null) {
            int nextRow = nextPosition.getRow() + modifier[0];
            int nextCol = nextPosition.getColumn() + modifier[1];
            nextPosition = new ChessPosition(nextRow, nextCol);
            nextSquareStatus = ChessMovesCalculator.isSquareValid(gameBoard, nextPosition, teamColor);
        }
        if(nextSquareStatus == teamColor) {
            return null;
        }
        return nextPosition;
    }
    public boolean isInCheckmate(TeamColor teamColor) {
        if(!isInCheck(teamColor)) {
            return false;
        }
        return !(isAnyMoveLegal(teamColor));
    }
    public boolean isInStalemate(TeamColor teamColor) {
        if(isInCheck(teamColor)) {
            return false;
        }
        return !(isAnyMoveLegal(teamColor));
    }
    public boolean isAnyMoveLegal(TeamColor teamColor) {
        for(int i = 0; i < 8; i++) {
            for(int j = 0; j < 8; j++) {
                ChessPosition checkForPiece = new ChessPosition(i+1, j+1);
                ChessPiece currentPiece = gameBoard.getPiece(checkForPiece);
                if(currentPiece == null) {
                    continue;
                }
                if(currentPiece.getTeamColor() != teamColor) {
                    continue;
                }
                if(!validMoves(checkForPiece).isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    public void setBoard(ChessBoard board) {
        for(int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                ChessPosition nextPosition = new ChessPosition(i+1,j+1);
                ChessPiece nextPiece = board.getPiece(nextPosition);
                gameBoard.addPiece(nextPosition, nextPiece);
            }
        }
        findKings();
        updateCastlingOptions();
    }
    public void updateCastlingOptions() {
        int[] rowColLimits = {1,8};
        for(int row : rowColLimits) {
            if (!updateCastlingOptionsFromKing(row)) {
                updateCastlingOptionsFromRooks(row);
            }
        }
    }
    public boolean updateCastlingOptionsFromKing(int row) {
        ChessPosition kingPosition = whiteKingPosition;
        int teamSelect = 0;
        if(row == 8) {
            kingPosition = blackKingPosition;
            teamSelect = 1;
        }
        if(!kingPosition.equals(new ChessPosition(row, 5))) {
            castlingOpenings[teamSelect] = new boolean[]{false, false};
            return false;
        }
        return true;
    }
    public void updateCastlingOptionsFromRooks(int row) {
        TeamColor teamColor = TeamColor.WHITE;
        int teamSelect = 0;
        if(row == 8) {
            teamColor = TeamColor.BLACK;
            teamSelect = 1;
        }
        ChessPiece testRook = new ChessPiece(teamColor, PieceType.ROOK);
        for(int i = 0; i < 2; i++) {
            if (!testRook.equals(gameBoard.getPiece(new ChessPosition(row, i*7+1)))) {
                castlingOpenings[teamSelect][i] = false;
            }
        }
    }
    public ChessBoard getBoard() {
        return gameBoard;
    }
    @Override
    public String toString() {
        return gameBoard.toString();
    }
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }
        if(obj == this) {
            return true;
        }
        if(this.getClass() != obj.getClass()) {
            return false;
        }
        ChessGame objChess = (ChessGame)obj;
        return (this.gameBoard.equals(objChess.getBoard()) &&
                this.teamTurn == objChess.getTeamTurn());
    }
    public int hashCode() {
        int hash = gameBoard.hashCode();
        hash = 31*hash + teamTurn.hashCode();
        return hash;
    }
}