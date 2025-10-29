package game;

import java.awt.Color;

import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import blocks.Block;

public class GameModel extends JPanel {

    private final GameView gameBoard;
    private final int ROWS = 23;
    private final int COLS = 12;

    private final int INNER_TOP = 2;
    private final int INNER_BOTTOM = ROWS - 2;
    private final int INNER_LEFT = 1;
    private final int INNER_RIGHT = COLS - 2;


    private GameTimer gameTimer;
    // WeightBlock 애니메이션 상태
    private boolean weightAnimating = false;
    private Timer weightTimer;

    // 단순 라인 클리어 깜빡임 상태 (한 번 검은색으로 표시한 뒤 클리어)
    private boolean lineClearAnimating = false;
    private boolean flashBlack = false;
    private java.util.List<Integer> flashingRows = new java.util.ArrayList<>();
    private Timer lineClearTimer;

    // AllClear(값 2) 애니메이션 상태
    private boolean allClearAnimating = false;
    private boolean allClearFlashBlack = false;
    private Timer allClearTimer;

    // BoxClear(값 3, 5x5 폭발) 애니메이션 상태
    private boolean boxClearAnimating = false;
    private boolean boxFlashBlack = false;
    private java.util.List<int[]> boxFlashCenters = new java.util.ArrayList<>();

    public int[][] board ;   // 게임 보드를 나타내는 2차원 배열
    protected Block currentBlock;  // 플레이어가 현재 조작하는 블록
    public Block nextBlock;
    public int itemGenerateCount = 0;
    public int lineClearCount = 0;
    public int divisor = 10;
    // 쌓인 블록의 색상을 보관 (ARGB). 0이면 비어있음
    private int[][] colorBoard;


    public int[][] boardArray = new int[ROWS][COLS];





    public GameModel(GameView gameBoard) {
        this.gameBoard = gameBoard;
    setOpaque(false);
    setVisible(true);
        // 생성 시 왼쪽, 오른쪽, 아랫벽을 true로 초기화
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                if (j == 0 || j == COLS - 1 || i == ROWS - 1) {
                    boardArray[i][j] = 10; // 벽
                } else {
                    boardArray[i][j] = 0;
                }
            }
        }
        // 색상 보드 초기화
        colorBoard = new int[ROWS][COLS];
        repaint();
    // make external board reference point to internal array so other code using `board` works
    this.board = this.boardArray;
    // spawn the initial block safely (avoid calling overridable methods from constructor)
    this.nextBlock = Block.spawn();
    this.currentBlock = this.nextBlock;
    this.nextBlock = Block.spawn();
    if (gameBoard != null) {
        gameBoard.setNextBlock(nextBlock);
    }

    }

    public Block getCurrentBlock(){  // 현재블록 getter
        return currentBlock;
    }

    public int[][] getBoard(){  // 게임보드 getter
        return board;
    }
    
    public void setBlockText(int row, int col) {
        if (isValidPosition(row, col)) {
            boardArray[row][col] = 1;
            repaint();
        }
    }

    public void boardInit() {
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                if (j == 0 || j == COLS - 1 || i == ROWS - 1) {
                    boardArray[i][j] = 10; // 벽
                } else {
                    boardArray[i][j] = 0;
                }
                // 색상 보드도 함께 초기화하여 잔상 제거
                colorBoard[i][j] = 0;
            }
        }


        repaint();
    }
    

    // 하드 드롭
    public void HardDrop() {
        if (currentBlock != null) {  // 현재 블록이 있는지 확인
            // 무게추(WeightBlock)는 일반 하드드랍 대신 드릴 애니메이션을 실행하고 소멸
            if (currentBlock instanceof blocks.item.WeightBlock) {
                applyWeightEffectAndDespawn();
                return;
            }
            // 일반 블록은 기존 하드드랍 로직 수행
            currentBlock.hardDrop(board);
            placePiece();  // 블록을 보드에 고정
            spawnNewBlock();  // 그리고 다음 블록을 생성
        }
    }

    // 블록을 보드에 고정시키는 역할
    protected void placePiece() {
        int[][] shape = currentBlock.getShape();
        int x = currentBlock.getX();
        int y = currentBlock.getY();
        Color color = currentBlock.getColor();
        int rgb = (color != null ? color.getRGB() : new Color(100,100,100).getRGB());
    boolean hasTwo = false;
    java.util.ArrayList<int[]> boxCenters = new java.util.ArrayList<>(); // (row,col) for value 3
    java.util.HashSet<Integer> rowsWithFour = new java.util.HashSet<>(); // rows containing value 4

        for (int row = 0 ; row < shape.length ; row++){
            for (int col = 0 ; col < shape[row].length ; col++){
                if (shape[row][col] != 0){
                    int br = y + row;
                    int bc = x + col;
                    board[br][bc] = shape[row][col];
                    colorBoard[br][bc] = rgb;
                    int v = shape[row][col];
                    if (v == 2) {
                        hasTwo = true; // AllClear
                    } else if (v == 3) {
                        boxCenters.add(new int[]{br, bc}); // BoxClear 중심
                    } else if (v == 4) {
                        rowsWithFour.add(br); // OneLineClear 대상 행
                    }
                }
            }
        }
        // '2'가 포함된 블록이면 전체 보드 AllClear 애니메이션 후 초기화
        if (hasTwo) {
            startAllClearAnimation();
            return;
        }
        // '3'이 포함된 경우: 각 중심을 기준으로 5x5 영역 클리어 (벽은 유지)
        if (!boxCenters.isEmpty()) {
            startBoxClearAnimation(boxCenters);
            return;
        }
        // '4'가 포함된 경우: 해당 row를 라인클리어처럼 한 줄 아래로 당기되, 블랙 깜빡임을 먼저 적용
        if (!rowsWithFour.isEmpty()) {
            java.util.ArrayList<Integer> rows = new java.util.ArrayList<>(rowsWithFour);
            rows.sort(java.util.Collections.reverseOrder()); // 여러 줄일 때도 안정적으로 처리
            // 아이템 라인 클리어용 깜빡임 + 실제 삭제/시프트를 수행하고 종료
            startItemRowFlashAndClear(rows);
            return;
        }
        // 하드드롭에서도 즉시 라인 클리어 실행 (기존에는 누락되어 줄이 안 지워짐)
        lineClear();
        // checkLines();  // 줄이 다 찼는지 확인
    }
 
    // AllClear(값 2) 애니메이션: 보드 전체를 잠깐 검게 플래시한 뒤 내부 영역을 비운다.
    private void startAllClearAnimation() {
        if (allClearAnimating) return;
        allClearAnimating = true;
        allClearFlashBlack = true;
        if (gameBoard != null) gameBoard.repaintBlock(); else repaint();

        if (allClearTimer != null) {
            allClearTimer.stop();
        }
        allClearTimer = new Timer(160, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                allClearTimer.stop();
                allClearTimer = null;
                allClearFlashBlack = false;

                // 실제 보드 초기화 (벽은 유지)
                boardInit();

                allClearAnimating = false;
                if (gameBoard != null) gameBoard.repaintBlock(); else repaint();
            }
        });
        allClearTimer.setRepeats(false);
        allClearTimer.start();
    }

    // BoxClear(값 3) 애니메이션: 5x5 영역을 검게 플래시한 뒤 지우고 중력 적용
    private void startBoxClearAnimation(java.util.List<int[]> centers) {
        if (centers == null || centers.isEmpty()) return;
        if (boxClearAnimating) return;
        boxFlashCenters.clear();
        // 방어적 복사
        for (int[] rc : centers) {
            boxFlashCenters.add(new int[]{rc[0], rc[1]});
        }
        boxClearAnimating = true;
        boxFlashBlack = true;
        if (gameBoard != null) gameBoard.repaintBlock(); else repaint();

        if (lineClearTimer != null) {
            // 다른 라인 클리어 타이머가 있다면 정지 (겹치기 방지)
            lineClearTimer.stop();
            lineClearTimer = null;
        }

        Timer boxTimer = new Timer(140, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ((Timer)e.getSource()).stop();
                boxFlashBlack = false;

                // 실제로 각 중심을 기준으로 5x5 영역 클리어
                for (int[] rc : boxFlashCenters) {
                    clearBox5x5(rc[0], rc[1]);
                }
                // 폭발 후 남은 블록이 아래로 떨어지도록 중력 적용
                applyGravity();

                boxFlashCenters.clear();
                boxClearAnimating = false;
                if (gameBoard != null) gameBoard.repaintBlock(); else repaint();

                // 아이템 처리 후 생긴 풀라인이 있으면 일반 라인클리어도 수행
                lineClear();
            }
        });
        boxTimer.setRepeats(false);
        boxTimer.start();
    }

    // 아이템(값 4)에 의해 선택된 행들을 블랙 플래시로 표시한 뒤 실제로 지우고 위를 한 칸씩 내리는 애니메이션
    private void startItemRowFlashAndClear(java.util.List<Integer> rows) {
        if (rows == null || rows.isEmpty()) return;
        // 이미 다른 라인 클리어 애니메이션 중이면 대기(스킵)
        if (lineClearAnimating) return;

        flashingRows.clear();
        flashingRows.addAll(rows);
        lineClearAnimating = true;
        flashBlack = true;
        if (gameBoard != null) gameBoard.repaintBlock(); else repaint();

        if (lineClearTimer != null) {
            lineClearTimer.stop();
        }
        lineClearTimer = new Timer(120, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                lineClearTimer.stop();
                lineClearTimer = null;
                flashBlack = false;

                // 실제로 각 행을 삭제하고 위를 한 칸씩 내리기 (라인 클리어처럼)
                // 내릴 때 인덱스 꼬임을 방지하기 위해 아래쪽부터 처리
                java.util.List<Integer> sorted = new java.util.ArrayList<>(flashingRows);
                sorted.sort(java.util.Collections.reverseOrder());
                for (int r : sorted) {
                    // 점수 더블 여부: 이 줄에 '5'가 포함되어 있으면 두 배 처리
                    boolean containsFive = false;
                    for (int c = INNER_LEFT; c <= INNER_RIGHT; c++) {
                        if (boardArray[r][c] == 5) { containsFive = true; break; }
                    }
                    if (containsFive) { lineClearScore(); lineClearScore(); } else { lineClearScore(); }
                    clearRowForce(r);
                    lineClearCount++;
                }

                flashingRows.clear();
                lineClearAnimating = false;
                if (gameBoard != null) gameBoard.repaintBlock(); else repaint();

                // 아이템 처리 후, 새로 생성된 풀라인이 있으면 일반 라인클리어도 수행(자체 블링크)
                lineClear();
            }
        });
        lineClearTimer.setRepeats(false);
        lineClearTimer.start();
    }

    // 점수 시스템은 다른 팀원이 구현할 예정이므로 여기서는 호출 훅만 제공
    // 일반 라인 클리어 시 1회 호출, '5' 포함 라인 클리어 시 2회 호출로 더블 처리
    protected void lineClearScore() {
        // no-op placeholder. Real scoring will be implemented elsewhere.
    }




    protected void spawnNewBlock(){
        if (nextBlock == null) {  
            nextBlock = Block.spawn();
        }
        currentBlock = nextBlock;  // 생성한 nextBlock을 currentBlock으로 설정
        if(lineClearCount/divisor > itemGenerateCount) {
            nextBlock = Block.spawn(); // 아이템 블록 생성
            nextBlock = Block.spawnItem(nextBlock);
            itemGenerateCount++;
        }
        else{
        nextBlock = Block.spawn(); // 새로운 nextBlock 생성
        }
        if (gameBoard != null) {
            gameBoard.setNextBlock(nextBlock); // GameView에 다음 블록 정보 전달
        }
    }

    // value 4가 있는 특정 행을 강제로 삭제하고 위 행들을 한 칸씩 내리는 동작 (벽은 유지)
    private void clearRowForce(int row) {
        if (row <= 0 || row >= ROWS) return;
        for (int r = row; r > 0; r--) {
            System.arraycopy(boardArray[r - 1], 0, boardArray[r], 0, COLS);
            System.arraycopy(colorBoard[r - 1], 0, colorBoard[r], 0, COLS);
        }
        for (int c = 0; c < COLS; c++) {
            boardArray[0][c] = 0;
            colorBoard[0][c] = 0;
        }
        boardArray[0][0] = 10; // 왼쪽 벽
        boardArray[0][COLS - 1] = 10; // 오른쪽 벽
    }

    // 중심 (cr, cc)를 기준으로 5x5(반경2) 영역을 0으로 클리어 (벽 10은 보존)
    private void clearBox5x5(int cr, int cc) {
        int r1 = Math.max(0, cr - 2);
        int r2 = Math.min(ROWS - 1, cr + 2);
        int c1 = Math.max(0, cc - 2);
        int c2 = Math.min(COLS - 1, cc + 2);
        for (int r = r1; r <= r2; r++) {
            for (int c = c1; c <= c2; c++) {
                // 바닥/좌우 벽은 유지
                if (r == ROWS - 1 || c == 0 || c == COLS - 1) continue;
                boardArray[r][c] = 0;
                colorBoard[r][c] = 0;
            }
        }
    }

    // 영역 클리어 후 공중에 떠 있는 블록들이 아래로 내려오도록 하는 열 단위 중력 적용
    private void applyGravity() {
        // 내부 표시 영역(가시 영역)만 대상으로 함: 행 2..ROWS-2, 열 1..COLS-2
        for (int c = INNER_LEFT; c <= INNER_RIGHT; c++) {
            int writeR = INNER_BOTTOM; // 아래쪽부터 채워넣음
            for (int r = INNER_BOTTOM; r >= INNER_TOP; r--) {
                int v = boardArray[r][c];
                if (v != 0) {
                    if (r != writeR) {
                        boardArray[writeR][c] = v;
                        colorBoard[writeR][c] = colorBoard[r][c];
                        boardArray[r][c] = 0;
                        colorBoard[r][c] = 0;
                    }
                    writeR--;
                }
            }
            // 위쪽 남은 부분 0으로 초기화 (안전)
            for (int r = writeR; r >= INNER_TOP; r--) {
                boardArray[r][c] = 0;
                colorBoard[r][c] = 0;
            }
        }
        if (gameBoard != null) {
            gameBoard.repaintBlock();
        } else {
            repaint();
        }
    }

    // 게임 재시작 시 블록 생성 상태 초기화
    public void resetBlocks() {
        this.nextBlock = Block.spawn();
        this.currentBlock = this.nextBlock;
        this.nextBlock = Block.spawn();
    }

    protected boolean canMoveto(int targetRow, int targetCol, int[][] shape){
        for (int row = 0 ; row < shape.length ; row++){
            for (int col = 0 ; col < shape[row].length ; col++){
                if (shape[row][col] == 0) 
                    continue;    // 블록에서 0인 부분은 검사하지 않아도 됨.
                if (targetRow + row < 2 || targetRow + row >= ROWS-1)  // 윗 2줄 숨겨진 공간 || 바닥 아래 1줄이상  벗어나는지 확인 
                    return false;
                if (targetCol + col < 1 || targetCol + col >= COLS-1) // 좌 1줄 숨겨진 공간 || 우 1줄 숨겨진 공간 벗어나는지 확인
                    return false;
                if(board[targetRow + row][targetCol + col] != 0) // 보드에 이미 다른 칸이 채워져 있는지 검사
                    return false;
            }
        }
        return true;
    }

    protected boolean canRotate(){
    // 기본 회전 시도
    if (canMoveto(currentBlock.getY(), currentBlock.getX(), currentBlock.Rotateshape())) {
        return true;
    }
    
    // 왼쪽으로 한 칸 이동 후 회전 시도
    if (canMoveto(currentBlock.getY(), currentBlock.getX() - 1, currentBlock.Rotateshape())) {
        currentBlock.setPosition(currentBlock.getX() - 1, currentBlock.getY());
        return true;
    }
    
    // 오른쪽으로 한 칸 이동 후 회전 시도
    if (canMoveto(currentBlock.getY(), currentBlock.getX() + 1, currentBlock.Rotateshape())) {
        currentBlock.setPosition(currentBlock.getX() + 1, currentBlock.getY());
        return true;
    }
    
    return false;
   
    }

    public void Rotate90() {
        // if(isGameOver() == true) return;
        if(canRotate() == true){
            currentBlock.getRotatedShape();
            repaint();
        }
    }

    public boolean isValidPosition(int row, int col) {
        if (boardArray[row][col] != 0) {
            return false;
        }
        else if (boardArray[row][col] == 0) {
            return true;
        }
        return row >= 0 && row < ROWS-1 && col >= 1 && col < COLS-1;
    }


    // Checks all visible rows and clears any full lines (Tetris behavior).
    // Visible inner rows are from INNER_TOP .. INNER_BOTTOM (inclusive).
    public void lineClear() {
        // 이미 애니메이션 중이면 중복 실행 방지
        if (lineClearAnimating) return;

        // 먼저 가득 찬 줄들을 수집
        java.util.List<Integer> fullRows = new java.util.ArrayList<>();
        for (int row = INNER_TOP; row <= INNER_BOTTOM; row++) {
            boolean full = true;
            for (int col = INNER_LEFT; col <= INNER_RIGHT; col++) {
                if (boardArray[row][col] == 0) { full = false; break; }
            }
            if (full) fullRows.add(row);
        }
        if (fullRows.isEmpty()) return; // 지울 줄 없음

        // 한 번 검은색으로 깜빡이기 위한 상태 세팅
        flashingRows.clear();
        flashingRows.addAll(fullRows);
        lineClearAnimating = true;
        flashBlack = true; // 첫 프레임: 검은색
        if (gameBoard != null) gameBoard.repaintBlock(); else repaint();

        // 짧은 지연 후 실제 클리어 수행 (검은색 표시가 보이도록)
        if (lineClearTimer != null) {
            lineClearTimer.stop();
        }
        lineClearTimer = new Timer(120, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 검은색 단계 종료 -> 실제 라인 클리어 실행
                lineClearTimer.stop();
                lineClearTimer = null;
                flashBlack = false;
                performImmediateLineClear();
                // 상태 해제
                flashingRows.clear();
                lineClearAnimating = false;
                if (gameBoard != null) gameBoard.repaintBlock(); else repaint();
            }
        });
        lineClearTimer.setRepeats(false);
        lineClearTimer.start();
    }

    // 기존 즉시 라인 클리어 로직을 메서드로 분리
    private void performImmediateLineClear() {
        boolean anyCleared = false;
        for (int row = INNER_TOP; row <= INNER_BOTTOM; row++) {
            boolean full = true;
            for (int col = INNER_LEFT; col <= INNER_RIGHT; col++) {
                if (boardArray[row][col] == 0) { full = false; break; }
            }
            if (full) {
                // 점수 더블 여부: 이 줄에 '5'가 포함되어 있으면 두 배 처리
                boolean containsFive = false;
                for (int c = INNER_LEFT; c <= INNER_RIGHT; c++) {
                    if (boardArray[row][c] == 5) { containsFive = true; break; }
                }
                if (containsFive) { lineClearScore(); lineClearScore(); } else { lineClearScore(); }
                for (int r = row; r > 0; r--) {
                    System.arraycopy(boardArray[r - 1], 0, boardArray[r], 0, COLS);
                    System.arraycopy(colorBoard[r - 1], 0, colorBoard[r], 0, COLS);
                }
                for (int c = 0; c < COLS; c++) {
                    boardArray[0][c] = 0;
                    colorBoard[0][c] = 0;
                }
                boardArray[0][0] = 10; // 왼쪽 벽
                boardArray[0][COLS - 1] = 10; // 오른쪽 벽
                anyCleared = true;
                row--;
                lineClearCount++;
            }
        }
        if (anyCleared) {
            // 후처리 훅 필요 시 여기에 추가 (예: 점수)
        }
    }

    // 애니메이션 상태 공개 메서드
    public boolean isLineClearAnimating() { return lineClearAnimating; }
    public boolean isFlashBlack() { return flashBlack; }
    public boolean isRowFlashing(int boardRow) { return lineClearAnimating && flashingRows.contains(boardRow); }
    // AllClear 애니메이션 상태
    public boolean isAllClearAnimating() { return allClearAnimating; }
    public boolean isAllClearFlashBlack() { return allClearAnimating && allClearFlashBlack; }
    // BoxClear 애니메이션 상태 및 특정 셀 여부
    public boolean isBoxClearAnimating() { return boxClearAnimating; }
    public boolean isBoxFlashBlack() { return boxClearAnimating && boxFlashBlack; }
    public boolean isCellInBoxFlash(int boardRow, int boardCol) {
        if (!boxClearAnimating) return false;
        for (int[] rc : boxFlashCenters) {
            int cr = rc[0], cc = rc[1];
            if (Math.abs(boardRow - cr) <= 2 && Math.abs(boardCol - cc) <= 2) {
                // 내부 영역만 유효 (벽 제외)
                if (boardRow >= INNER_TOP && boardRow <= INNER_BOTTOM && boardCol >= INNER_LEFT && boardCol <= INNER_RIGHT) {
                    return true;
                }
            }
        }
        return false;
    }
    

    public int[][] getColorBoard() {
        return colorBoard;
    }

    // WeightBlock 전용 처리: 현재 위치에서 아래로 바닥(10)을 만날 때까지 내려가며 경로를 0으로 지움.
    // 바닥에 닿으면 블록은 쌓이지 않고 소멸하고, 새 블록을 스폰한다.
    public void applyWeightEffectAndDespawn() {
        if (currentBlock == null) return;
        // 이미 애니메이션 중이면 중복 시작 방지
        if (weightAnimating) return;
        weightAnimating = true;

        final int delayMs = 60; // 프레임 간격(느낌 조절)
        weightTimer = new Timer(delayMs, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentBlock == null) {
                    stopWeightAnimation();
                    return;
                }
                int[][] shape = currentBlock.getShape();
                int x = currentBlock.getX();
                int y = currentBlock.getY();

                // 다음 단계가 바닥(10)에 닿는지 확인
                boolean hitBottom = false;
                outer: for (int r = 0; r < shape.length; r++) {
                    for (int c = 0; c < shape[r].length; c++) {
                        if (shape[r][c] == 0) continue;
                        int nx = x + c;
                        int ny = y + r + 1;
                        if (ny >= ROWS) { // 안전장치
                            hitBottom = true;
                            break outer;
                        }
                        if (boardArray[ny][nx] == 10) { // 바닥 도달
                            hitBottom = true;
                            break outer;
                        }
                    }
                }

                if (hitBottom) {
                    // 소멸: 보드에 쌓지 않고 다음 블록 스폰
                    stopWeightAnimation();
                    spawnNewBlock();
                    if (gameBoard != null) {
                        gameBoard.setFallingBlock(currentBlock);
                        gameBoard.repaintBlock();
                    } else {
                        repaint();
                    }
                    return;
                }

                // 다음 위치(한 칸 아래)의 경로를 0으로 지움 (벽은 보존)
                for (int r = 0; r < shape.length; r++) {
                    for (int c = 0; c < shape[r].length; c++) {
                        if (shape[r][c] == 0) continue;
                        int nx = x + c;
                        int ny = y + r + 1;
                        if (ny >= 0 && ny < ROWS && nx >= 0 && nx < COLS) {
                            if (!(ny == ROWS - 1 || nx == 0 || nx == COLS - 1)) {
                                boardArray[ny][nx] = 0;
                                colorBoard[ny][nx] = 0;
                            }
                        }
                    }
                }
                // 한 칸 하강 (충돌 무시)
                currentBlock.setPosition(x, y + 1);

                // 화면 갱신
                if (gameBoard != null) {
                    gameBoard.setFallingBlock(currentBlock);
                    gameBoard.repaintBlock();
                } else {
                    repaint();
                }
            }
        });
        weightTimer.start();
    }

    private void stopWeightAnimation() {
        if (weightTimer != null) {
            weightTimer.stop();
            weightTimer = null;
        }
        weightAnimating = false;
    }

    public boolean isWeightAnimating() {
        return weightAnimating;
    }

    public boolean isGameOver() {
        // 상단 감지 영역: row 0..(INNER_TOP-1), col 3..7 에 1이 있으면 게임 오버
        for (int row = INNER_TOP; row < INNER_TOP + 2; row++) {
            for (int col = 3; col < 8; col++) {
                if (boardArray[row][col] != 0) {
                    return true;


                }
            }
        }
        return false;
 
    }
    

    

}