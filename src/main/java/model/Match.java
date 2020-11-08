package model;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Calendar;

public class Match {

    /*
    O_Player is first player
    X_player is second player
     */

    private static final int X_PLAYER = 1;
    private static final int O_PLAYER = 2;
    private static final int TIE = 3;
    private static final int UN_FINISH = 0;

    private SocketChannel xPlayer;
    private SocketChannel oPlayer;

    private int currentTurn;

    private int[][] chessboard;

    public Match(SocketChannel xPlayer, SocketChannel oPlayer) {
        this.xPlayer = xPlayer;
        this.oPlayer = oPlayer;
    }

    private void initChessboard() {
        chessboard = new int[20][40];
    }

    private int isFinish() {
        boolean flag = false;
        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 40; j++) {
                if (chessboard[i][j] == 0) {
                    flag = true;
                    continue;
                }
                int row = i;
                int col = j;
                if (col <= 35) {
                    int result = 1;
                    for (int k = 0; k < 5; k++) {
                        result *= chessboard[row][col + k];
                    }
                    if (result == 1) return X_PLAYER;
                    if (result == 32) return O_PLAYER;
                }
                if (row <= 15) {
                    int result = 1;
                    for (int k = 0; k < 5; k++) {
                        result *= chessboard[row + k][col];
                    }
                    if (result == 1) return X_PLAYER;
                    if (result == 32) return O_PLAYER;
                }
                if (col <= 35 && row <= 15) {
                    int result = 1;
                    for (int k = 0; k < 5; k++) {
                        result *= chessboard[row + k][col + k];
                    }
                    if (result == 1) return X_PLAYER;
                    if (result == 32) return O_PLAYER;
                }
                if (row <= 15 && col >= 4) {
                    int result = 1;
                    for (int k = 0; k < 5; k++) {
                        result *= chessboard[row + k][col - k];
                    }
                    if (result == 1) return X_PLAYER;
                    if (result == 32) return O_PLAYER;
                }
            }
        }
        return flag ? UN_FINISH : TIE;
    }

    public SocketChannel getxPlayer() {
        return xPlayer;
    }

    public void setxPlayer(SocketChannel xPlayer) {
        this.xPlayer = xPlayer;
    }

    public SocketChannel getoPlayer() {
        return oPlayer;
    }

    public void setoPlayer(SocketChannel oPlayer) {
        this.oPlayer = oPlayer;
    }

    public void startGame() throws IOException {
        initChessboard();
        ByteBuffer buffer = ByteBuffer.wrap(Response.YOU_ARE_X.getBytes());
        xPlayer.write(buffer);
        buffer = ByteBuffer.wrap(Response.YOU_ARE_O.getBytes());
        oPlayer.write(buffer);
        currentTurn = X_PLAYER;
        sendTurn();
    }

    public void sendChessboard() throws IOException {
        StringBuilder builder = new StringBuilder();
        builder.append(Response.CHESS_BOARD).append("/");
        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 40; j++) {
                builder.append(chessboard[i][j]);
            }
        }
        ByteBuffer buffer = ByteBuffer.wrap(builder.toString().getBytes());
        xPlayer.write(buffer);
        oPlayer.write(buffer);
    }

    public void moveChess(int row, int col, int player) throws IOException {
        chessboard[row][col] = player;
        sendChessboard();
        int isFinish = isFinish();
        if (isFinish == UN_FINISH) {
            if (currentTurn == X_PLAYER) {
                currentTurn = O_PLAYER;
            } else {
                currentTurn = X_PLAYER;
            }
            sendTurn();
        } else {
            announceTheWinner(isFinish);
        }
    }

    private void announceTheWinner(int status) throws IOException {
        StringBuilder win = new StringBuilder();
        win.append(Response.NOTICE_MATCH_STATUS)
                .append("/")
                .append(Response.MATCH_STATUS_YOU_WIN);
        StringBuilder lose = new StringBuilder();
        lose.append(Response.NOTICE_MATCH_STATUS)
                .append("/")
                .append(Response.MATCH_STATUS_YOU_LOSE);
        StringBuilder tie = new StringBuilder();
        tie.append(Response.NOTICE_MATCH_STATUS)
                .append("/")
                .append(Response.MATCH_STATUS_TIE);
        if (status == X_PLAYER) {
            ByteBuffer buffer = ByteBuffer.wrap(win.toString().getBytes());
            xPlayer.write(buffer);
            buffer = ByteBuffer.wrap(lose.toString().getBytes());
            oPlayer.write(buffer);
        } else if (status == O_PLAYER) {
            ByteBuffer buffer = ByteBuffer.wrap(win.toString().getBytes());
            oPlayer.write(buffer);
            buffer = ByteBuffer.wrap(lose.toString().getBytes());
            xPlayer.write(buffer);
        } else {
            ByteBuffer buffer = ByteBuffer.wrap(tie.toString().getBytes());
            xPlayer.write(buffer);
            oPlayer.write(buffer);
        }
    }

    private void sendTurn() throws IOException {
        long endTime = Calendar.getInstance().getTimeInMillis() + 15000;
        StringBuilder builder = new StringBuilder();
        builder.append(Response.YOUR_TURN).append("/").append(endTime);
        ByteBuffer buffer = ByteBuffer.wrap(builder.toString().getBytes());
        StringBuilder builder1 = new StringBuilder();
        builder.append(Response.OPPONENTS_TURN).append("/").append(endTime);
        if (currentTurn == X_PLAYER) {
            xPlayer.write(buffer);
            buffer = ByteBuffer.wrap(builder1.toString().getBytes());
            oPlayer.write(buffer);
        } else {
            oPlayer.write(buffer);
            buffer = ByteBuffer.wrap(builder1.toString().getBytes());
            xPlayer.write(buffer);
        }
    }
}