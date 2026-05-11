package chess;
import chess.ChessPiece.PieceType;
import chess.ChessGame.TeamColor;
import java.util.Objects;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {
    ChessPiece[][] squares = new ChessPiece[8][8];
    public ChessBoard() {
        
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        squares[position.getRow()-1][position.getColumn()-1] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return squares[position.getRow()-1][position.getColumn()-1];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        ChessPiece whitePiece;
        ChessPiece blackPiece;

        PieceType[] pieceTypes = {PieceType.KING, PieceType.QUEEN, PieceType.BISHOP, PieceType.KNIGHT, PieceType.ROOK};
        int[][] piecePositions = {{4}, {3}, {2,5}, {1,6}, {0,7}};

        //Sets all pieces except PAWNs
        for(int i = 0; i < pieceTypes.length; i++) {
            int[] positionSet = piecePositions[i];
            PieceType currentPieceType = pieceTypes[i];

            whitePiece = new ChessPiece(TeamColor.WHITE, pieceTypes[i]);
            blackPiece = new ChessPiece(TeamColor.BLACK, pieceTypes[i]);
            for(int position : positionSet) {
                squares[0][position] = whitePiece;
                squares[7][position] = blackPiece;
            }
        }

        //Set PAWNs
        whitePiece = new ChessPiece(TeamColor.WHITE,PieceType.PAWN);
        blackPiece = new ChessPiece(TeamColor.BLACK,PieceType.PAWN);
        for(int i = 0; i <=7; i++) {
            squares[1][i] = whitePiece;
            squares[6][i] = blackPiece;
        }
    }

    @Override
    public String toString() {
        String output = "\n";
        for(int i = 7; i >= 0; i--) {
            for(int j = 0; j <=7; j++) {
                ChessPiece piece = squares[i][j];
                if(piece == null) {
                    output += " ";
                } else {
                    String pieceChar;
                    if(piece.getPieceType() == PieceType.KING) {
                        pieceChar = "K";
                    } else if(piece.getPieceType() == PieceType.QUEEN) {
                        pieceChar = "Q";
                    } else if(piece.getPieceType() == PieceType.BISHOP) {
                        pieceChar = "B";
                    } else if(piece.getPieceType() == PieceType.KNIGHT) {
                        pieceChar = "N";
                    } else if(piece.getPieceType() == PieceType.ROOK) {
                        pieceChar = "R";
                    } else {
                        pieceChar = "P";
                    }
                    if(piece.getTeamColor() == TeamColor.BLACK) {
                        pieceChar = pieceChar.toLowerCase();
                    }
                    output += pieceChar;
                }
                output += "|";
            }
            output = output.substring(0,output.length()-1);
            output += "\n";
        }
        return output;
    }

    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }

        if(obj == this) {
            return true;
        }

        if(obj.getClass() != this.getClass()) {
            return false;
        }

        ChessBoard objBoard = (ChessBoard)obj;
        for(int i = 0; i <= 7; i++) {
            for(int j = 0; j <=7; j++) {
                ChessPosition position = new ChessPosition(i+1,j+1);
                //if(this.getPiece(position) != objBoard.getPiece(position)) {
                ChessPiece thisPiece = this.getPiece(position);
                ChessPiece objPiece = objBoard.getPiece(position);
                if((thisPiece == null)) {
                    if(thisPiece != objPiece){
                        return false;
                    }
                } else if(!thisPiece.equals(objPiece)) {
                    return false;
                }
            }
        }
        return true;
    }

    public int hashCode() {
        int hash = 0;
        for(int i = 0; i <= 7; i++) {
            for(int j = 0; j <= 7; j ++) {
                hash = 31 * hash + Objects.hashCode(squares[i][j]);
            }
        }
        return hash;
    }
}
