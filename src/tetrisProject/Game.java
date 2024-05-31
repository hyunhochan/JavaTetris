package tetrisProject;

import java.awt.Color;
import java.util.Timer;
import java.util.TimerTask;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class Game extends Object {
    public static final int STATE_GETREADY = 1;
    public static final int STATE_PLAYING = 2;
    public static final int STATE_PAUSED = 3;
    public static final int STATE_GAMEOVER = 4;


    private final PropertyChangeSupport PCS = new PropertyChangeSupport(this);

    private final SquareBoard board;
    
    private final NextBoard nextboard;
    private final HoldBoard holdboard;
    private final ItemBoard itemboard;
    public boolean holdAvailable = true;
    private boolean isDoingSpdUp = false;

    
//    private Figure[] figures = {
//            new Figure(this, Figure.SQUARE_FIGURE),
//            new Figure(this, Figure.LINE_FIGURE),
//            new Figure(this, Figure.S_FIGURE),
//            new Figure(this, Figure.Z_FIGURE),
//            new Figure(this, Figure.RIGHT_ANGLE_FIGURE),
//            new Figure(this, Figure.LEFT_ANGLE_FIGURE),
//            new Figure(this, Figure.TRIANGLE_FIGURE)
//    };
//    Figure은 동적으로 그때그때 생성해야만 함.
    
//    private Figure[] otherfigures = {
//            new Figure(this, Figure.SQUARE_FIGURE),
//            new Figure(this, Figure.LINE_FIGURE),
//            new Figure(this, Figure.S_FIGURE),
//            new Figure(this, Figure.Z_FIGURE),
//            new Figure(this, Figure.RIGHT_ANGLE_FIGURE),
//            new Figure(this, Figure.LEFT_ANGLE_FIGURE),
//            new Figure(this, Figure.TRIANGLE_FIGURE)
//    };
//   otherFigures는 동적으로 그때그때 생성해야만 함.
    
    private final GameThread thread;

    private int level = 1;

    private int score = 0;

    private Figure figure = null;

    private Figure nextFigure = null;
    
    private Figure holdFigure = null;
    
    private Figure otherFigure = null;

    private int nextRotation = 0;

    private boolean preview = true;

    private boolean moveLock = false;
    
    private int clientCount;
    private int totalPlayers;

    private int state;
    
    private BgmPlayer bgmPlayer;
    
    private Tetris tetrisInstance;
    
    private int width;
    private int height;
    
    public boolean isOtherdead;
    public boolean isSingle;
    
    public HashMap<Integer, SquareBoard2> otherBoardsMap;
    
    public HashMap<Integer, ItemBoard2> otherItemBoardsMap;
    
    public List<Point3D> itemonBoardList = new ArrayList<>();
    
    private final Object somethingLock = new Object(); //쓸지말지 고민중
    
    private boolean player1dead = false;
    private boolean player2dead = false;
    private boolean player3dead = false;
    private boolean player4dead = false;
    private boolean player5dead = false;
    private boolean player6dead = false;
    
    
    
    public Game(Tetris tetris) {
        this(10, 21); //10유닛, 21유닛 단위 게임화면 설정
        this.tetrisInstance = tetris;
        this.clientCount = tetrisInstance.myNumber;
        this.totalPlayers = tetrisInstance.totalPlayers;
        
        
    }

    public Game(int width, int height) {
    	this.bgmPlayer = new BgmPlayer();
    	this.width = width;
    	this.height = height;
        
        board = new SquareBoard(this, width, height);
        itemboard = new ItemBoard(this, 13, 362, 26);
        
        
        otherBoardsMap = new HashMap<>();
        otherItemBoardsMap = new HashMap<>();
        
        nextboard = new NextBoard(this, 3, 4);
        holdboard = new HoldBoard(this, 3, 4);
        thread = new GameThread();
        handleGetReady();
        board.getComponent().setFocusable(true);
        board.getComponent().addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                handleKeyEvent(e);
            }
        });
     //채팅창 누르면 게임 안되는 문제 해결
        board.getComponent().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                board.getComponent().requestFocusInWindow();
            }
        });
    }

    
    public void addPropertyChangeListener(PropertyChangeListener l) {
        PCS.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        PCS.removePropertyChangeListener(l);
    }

    public int getState() {
        return state;
    }

    public int getLevel() {
        return level;
    }

    public int getScore() {
        return score;
    }

    public int getRemovedLines() {
        return board.getRemovedLines();
    }

    public Component getSquareBoardComponent() {
        return board.getComponent();
    }
    
    public Component getSquareBoardComponent2(int number) {
    	// Check if the board already exists to prevent overwriting it
        if (!otherBoardsMap.containsKey(number)) {
            SquareBoard2 board2 = new SquareBoard2(this, width, height, number);
            otherBoardsMap.put(number, board2);
            // Perform any additional setup needed for the board here
            
            //System.out.println("number : " + number + board2);
            
        }
        
        return otherBoardsMap.get(number).getComponent();
    }

    public Component getNextBoardComponent() {
        return nextboard.getComponent();
    }
    
    public Component getHoldBoardComponent() {
        return holdboard.getComponent();
    }
    
    public Component getItemBoardComponent() {
        return itemboard.getComponent();
    }
    
    public Component getItemBoard2Component(int number) {
    	// Check if the board already exists to prevent overwriting it
        if (!otherItemBoardsMap.containsKey(number)) {
            ItemBoard2 item2 = new ItemBoard2(this, 81, 9, 26);
            otherItemBoardsMap.put(number, item2);
            // Perform any additional setup needed for the board here
            
            //System.out.println("number : " + number + board2);
            
        }
        
        return otherItemBoardsMap.get(number).getComponent();
    }
    
    private int randomFigureType() {
        return (int) (Math.random() * 7) + 1; // 1과 7 사이의 랜덤한 숫자 반환
    }

    
    public void init() {
        if (state == STATE_GAMEOVER) {
        	board.getComponent().setFocusable(true);
            handleGetReady();
        }
    }

    public void start() {
        handleStart();
    }

    public void pause() {
        if (state == STATE_PLAYING) {
            handlePause();
        }
    }

    public void resume() {
        if (state == STATE_PAUSED) {
            handleResume();
        }
    }

    public void terminate() {
        handleGameOver();
    }
    
    public static void playSound(String soundFilePath) { //효과음 재생
        new Thread(() -> {
            try {
                File soundFile = new File(soundFilePath);
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
                Clip clip = AudioSystem.getClip();
                clip.open(audioIn);
                clip.start();
                
                // Optional: if you want to wait for the sound to finish
                Thread.sleep(clip.getMicrosecondLength() / 1000);
                
            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    public static void playLoopingSound(String soundFilePath) { // bgm 재생
        new Thread(() -> {
            try {
                File soundFile = new File(soundFilePath);
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
                Clip clip = AudioSystem.getClip();
                clip.open(audioIn);

                // 무한 루프를 위해 LOOP_CONTINUOUSLY를 설정합니다.
                clip.loop(Clip.LOOP_CONTINUOUSLY);
                clip.start();
                
                // 선택적: 사운드가 끝날 때까지 기다리는 것이 필요하지 않기 때문에
                // Thread.sleep() 호출을 제거합니다.

            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
                e.printStackTrace();
            }
        }).start();
    }



    private void handleStart() {
    	state = STATE_GETREADY;
        PCS.firePropertyChange("state", -1, STATE_GETREADY);
        
    	System.out.println("my number : " + clientCount);
    	
    	itemboard.clearItems();
    	for (ItemBoard2 item2 : otherItemBoardsMap.values()) {
            item2.clearItems();
        }
    	thread.setPaused(true);
    	board.resetCombo();
    	
    	 player1dead = false;
    	 player2dead = false;
    	 player3dead = false;
    	 player4dead = false;
    	 player5dead = false;
    	 player6dead = false;
    	
    	board.hasRankImageDrwan = false;
    	figure = null;
    	itemonBoardList.clear();
    	board.itemThatIHave.clear();
    	
    	board.update();
    	board.clear(false);
    	for (SquareBoard2 board2 : otherBoardsMap.values()) {
            board2.clear();
            board2.amIdead = false;
        }
    	nextboard.clear();
    	holdboard.clear();
    	holdAvailable = true;
    	bgmPlayer.stopBgm();
    	thread.setPaused(true);
    	
            // Countdown loop
            for (int i = 3; i >= 1; i--) {
                final String number = Integer.toString(i);
                board.setMessage(number);
                BgmPlayer.playsfx("src/sounds/count_n.wav"); // Play countdown sound
                long startTime = System.currentTimeMillis(); // 시작 시간을 기록
		        long elapsedTime = 0L;
		        
		        while (elapsedTime < 700) { // 4초간 딜레이. (순위를 확인하게 하기 위함)
		            elapsedTime = System.currentTimeMillis() - startTime;
		        }
                if(i==2) {
                	bgmPlayer.startRandomBgm();
                }
            }
            board.setMessage("GO!");
            BgmPlayer.playsfx("src/sounds/count_go.wav"); // Play GO sound
            long startTime = System.currentTimeMillis(); // 시작 시간을 기록
	        long elapsedTime = 0L;
	        
	        while (elapsedTime < 600) { // 4초간 딜레이. (순위를 확인하게 하기 위함)
	            elapsedTime = System.currentTimeMillis() - startTime;
	        }
            
            
            // Start the game
            board.setMessage(null); // Clear the message
            // ... code to actually start the game ...
        state = STATE_PLAYING;
        PCS.firePropertyChange("state", -1, STATE_PLAYING);
            
        level = 1;
        score = 0;
        figure = null;
        nextFigure = new Figure(this, randomFigureType());
        nextRotation = 0;
        thread.setPaused(false);
        board.setMessage(null);
        board.clear(false);
        nextboard.clear();
        holdboard.clear();
        handleLevelModification();
        handleScoreModification();

        
        handleFigureStart();
        thread.reset();
        
    }

    public void handleGameOver() {
    	
    	System.out.println("over1");
    	board.coverWithGray();
        thread.setPaused(true);
        
        
        if (figure != null) {
        	figure.clearGhost();
            figure.detach();
        }
        figure = null;
        if (nextFigure != null) {
            nextFigure.detach();
        }
        nextFigure = null;

        state = STATE_GAMEOVER;
        
        
        
        bgmPlayer.stopBgm();
        BgmPlayer.playsfx("src/sounds/sfx_gameover.wav"); // Add this line to play the sound
        
        long startTime = System.currentTimeMillis(); // 시작 시간을 기록
        long elapsedTime = 0L;

        while (elapsedTime < 500) { // 0.5초 (500밀리초) 동안 실행, 게임 끝나고 굳는 효과 주려고
            elapsedTime = System.currentTimeMillis() - startTime;
        }
        
        BgmPlayer.playsfx("src/sounds/lose.wav");
        
        
        
        tetrisInstance.sendOverStatus();
        PCS.firePropertyChange("state", -1, STATE_GAMEOVER);
    }
    
    public void handleGameOverSingle() {

        thread.setPaused(true);
        

        if (figure != null) {
            figure.detach();
        }
        figure = null;
        if (nextFigure != null) {
            nextFigure.detach();
        }
        nextFigure = null;
        board.clear(false);
    	nextboard.clear();
    	holdboard.clear();
    	holdAvailable = true;
    	bgmPlayer.stopBgm();
        board.update();
        

        state = STATE_GAMEOVER;
        
        bgmPlayer.stopBgm();
        //BgmPlayer.playsfx("src/sounds/sfx_gameover.wav"); // Add this line to play the sound
        
        
        PCS.firePropertyChange("state", -1, STATE_GAMEOVER);
    }
    
    public void handleGameOverOther(int clientNumber, int yerrank) {
    	
    	
    	switch(clientNumber) {
    	case 1:
    		player1dead = true;
    		break;
    	case 2:
    		player2dead = true;
    		break;
    	case 3:
    		player3dead = true;
    		break;
    	case 4:
    		player4dead = true;
    		break;
    	case 5:
    		player5dead = true;
    		break;
    	case 6:
    		player6dead = true;
    		break;
    	}
    	
    	
    	SquareBoard2 boardForPlayer = otherBoardsMap.get(clientNumber);
    	
    	boardForPlayer.coverWithGray();
    	boardForPlayer.amIdead = true;
    	
        if (otherFigure != null) {
        	otherFigure.detach();
        }
        otherFigure = null;

        
        BgmPlayer.playsfx("src/sounds/sfx_gameover.wav"); // Add this line to play the sound
        
        long startTime = System.currentTimeMillis(); // 시작 시간을 기록
        long elapsedTime = 0L;

        while (elapsedTime < 500) { // 0.5초 (500밀리초) 동안 실행, 게임 끝나고 굳는 효과 주려고
            elapsedTime = System.currentTimeMillis() - startTime;
        }
        //boardForPlayer.setMessage("Game Over");
//        BgmPlayer.playsfx("src/sounds/lose.wav");
        
        boardForPlayer.showRankImage(yerrank);
        
    }
    
    public boolean getrankImageDrawn() {
    	if(board!=null) {
	    	if(board.hasRankImageDrwan) {
	    		return true;    		
	    	}
	    	 return false;
    	}
    	return false;
    }
    
    public void getMyRank(int myrank) {
    	tetrisInstance.isexed = true;
    	board.showRankImage(myrank);
    	
    }
    
    public void itemLanded(int x, int y, int type) {
    	clearGhost();
    	
    	System.out.println(itemonBoardList.add(new Point3D(x, y, type)));
    	System.out.println("itemlandededed");
    }
    
    public void getLines(int lines, int combo) {
    	clearGhost();
    	int randomNum = (int)(Math.random()*10);
    	System.out.println("otherLine is : " + tetrisInstance.setFinalLines(lines, combo));
    	addOtherLines(tetrisInstance.setFinalLines(lines, combo), randomNum);
    	tetrisInstance.hasremovedLine(lines, combo, randomNum);
    	
    }
    
    public void addLines(int lines, int randomHole) { // 타인이 지웠을 때 내 화면에 추가되는 블록
    	
	    	if(state != STATE_GAMEOVER && lines>0) { //딱히 여러줄 지우지도 않았는데 블럭 깜빡이느니 이게 나음.
	    		board.addGarbageLine(lines, randomHole);
	    	
    	}
    }
    
    public synchronized void addOtherLines(int lines, int randomHole) { //내가 지웠을 때 타인에게 영향
    	//synchronized (somethingLock) {
    		clearGhost();
	    	for (SquareBoard2 boardForPlayer : otherBoardsMap.values()) {
	    		if(!boardForPlayer.amIdead) {
	    			boardForPlayer.addGarbageLine(lines, randomHole);
	    		}
	        }
    	//}
    	
    }
    
    public synchronized void addOtherLines(int lines, int randomHole, int clientNumber) { // 타인이 지웠을 때 타인에게 영향
    	//synchronized (somethingLock2) {
    	if(lines>0) {
	    	clearGhost(); //딱히 여러줄 지우지도 않았는데 고스트 지워지느니 이게 나음.
	    	int i = 0;
	    	for (SquareBoard2 boardForPlayer : otherBoardsMap.values()) {
	    		if(!boardForPlayer.amIdead && (clientNumber != tetrisInstance.arrangeOther[i])) {
	    			boardForPlayer.addGarbageLine(lines, randomHole);
	    		}
	    		i++;
	    	//}
	    	}
    	}
    }
    
    public synchronized void useItem(int clientNumber, int itemType, int randomNum) { //아이템의 효과가 나에게 적용되어 화면에 표시됨
    	
	    	System.out.println("item " + itemType + " has used to me");
	    	
	    	if(clientNumber != clientCount) { //상대가 나에게 쓴 아이템 
		    	ItemBoard2 boardForPlayer = otherItemBoardsMap.get(clientNumber);
		    	boardForPlayer.useItem(); //아이템을 쓴 "다른 유저"의 아이템 보관소에서 1개 삭제.
		    	//하지만 내가 나에게 쓴 아이템이라면?
	    	}else { //내가 나에게 쓴 아이템
	    		//...아무것도 하지 않음
	    	}
	    	
	    	switch(itemType) {
		    	case 1:
		    		board.Plus1(randomNum);
		    		break;
		    	case 2:
		    		board.Plus2(randomNum);
		    		break;
		    	case 3: //스피드업은 구현이 난잡해서 특수 처리
		    		if(clientNumber != clientCount) { //상대가 나에게 쓴 아이템 
		    			BgmPlayer.playsfx("src/sounds/sfx_itemuse.wav"); // Add this line to play the sound
		    			exeSpeedUp();
		    		}else { //내가 나에게 쓴 아이템
		    			exeSpeedUp();
		    		}
		    		break;
		    	case 4:
		    		board.Minus1();
		    		break;
		    	case 5:
		    		board.Minus2();
		    		break;
		    	case 6:
		    		board.cut();
		    		break;
		    	case 7:
		    		board.clear(true);
		    		break;
	    	}
    	
    }
    
    public synchronized void useOtherItem(int clientNumber, int calledNumber, int itemType, int randomNum) { //아이템의 효과가 타인에게 적용되어 otherboard 화면에 표시됨
    	
    	synchronized (somethingLock) {
    	
	    	if(clientCount != clientNumber) { //다른 유저가 쓴 아이템만 otherboard를 참조하여야 함.
	    		ItemBoard2 boardForPlayer = otherItemBoardsMap.get(clientNumber);
	    		
	    		boardForPlayer.useItem(); //아이템을 쓴 "다른 유저"의 아이템 보관소에서 1개 삭제.
	    		//2번 클라에서 1번 눌러서 실행했는데, 왜 코드가 이 쪽으로 들어오지?
	    	}
	    	
	    	//그러니까, 다른 유저가 "사용"한 아이템인 경우에는 해당 인스턴스를 찾아서 지우는 것이고,
	    	//2번 클라에서 1번을 눌러서 실행했으니 실행 주체는 "나"이고, 적용되는 것은 otherboard[key=1]인 인스턴스일 것인데.
	    	//실행 주체가 나일 경우에는 루프문에 들어갈 이유가 없다는 것이다.
	    	//그래서 clientCount = 2일 것이고, clientNumber는 아이템을 사용한 클라이언트니까 2일 텐데, 왜 루프문 속에 들어와서 구태여 오류를 일으키냐는 것이다.
	    	//=> game의 초기화를 너무 일찍 해버려서, tetris 내에서 myNumber를 초기화하는 정보를 받지 못해 0으로 인식되고 있었다.
	    	
	    	
	    	SquareBoard2 boardForPlayer = otherBoardsMap.get(calledNumber);
	    	
	    	
	    	switch(itemType) {
		    	case 1:
		    		boardForPlayer.Plus1(randomNum);
		    		break;
		    	case 2:
		    		boardForPlayer.Plus2(randomNum);
		    		break;
		    	case 3:
		    		//otherboard에선 떨어지는 figure가 표시되지 않으므로 굳이 otherboard에 구현할 필요 없음
		    		break;
		    	case 4:
		    		boardForPlayer.Minus1();
		    		break;
		    	case 5:
		    		boardForPlayer.Minus2();
		    		break;
		    	case 6:
		    		boardForPlayer.cut();
		    		break;
		    	case 7:
		    		boardForPlayer.clear();
		    		break;
	    	}
    	
    	}
    	
    }
    
    public void rotateOther(int clientNumber) {
    	synchronized (somethingLock) {
        	
	    	if(clientCount != clientNumber) { //다른 유저가 한 행동만 otherboard를 참조하여야 함.
	    		ItemBoard2 boardForPlayer = otherItemBoardsMap.get(clientNumber);
	    		
	    		boardForPlayer.rotateItems(); //아이템을 쓴 "다른 유저"의 아이템 회전.
	    	}
    	}
    }
    
    public void remakeFig(int lines) {
    	if(figure!=null ) {
    		
    		figure.remakeFigure(lines);
    	}
    	
    }
    
    public void remakeFigMinus(int lines) {
    	if(figure!=null ) {
    		
    		figure.remakeFigureMinus(lines);
    	}
    	
    }
    
    public void clearFig() {
    	boolean exed = false;
    	if(figure!=null) {
    		figure.clearFigure();
    		exed = true;
    	}
    	if(!exed) {
    		if(figure!=null) {
        		figure.clearFigure();
        		exed = true;
        	}
    	}
    }
    
    public void clearGhost() {
    	if(figure!=null) {
    		figure.clearGhost();
    	}
    }
    
    public void remakeGhost(int lines) {
    	if(figure!=null) {
    		figure.remakeGhost(lines);
    	}
    }
    
    public void remakeGhostMinus(int lines) {
    	if(figure!=null) {
    		figure.remakeGhostMinus(lines);
    	}
    }
    
    public void exePlus1 () { //테스트용 함수
    	
    	
    	int randomNum = (int)(Math.random()*10);
    	board.Plus1(randomNum);
    	
    }
    
    public void exePlus2() {//테스트용 함수
    	
    	
//    	tetrisInstance.함수(randomNum);
    	
    }
    
    public void exeSpeedUp() {
    	if(!isDoingSpdUp) {
    		isDoingSpdUp = true;
	    	final int temp = level;
	        level = 9;
	        PCS.firePropertyChange("level", -1, level);
	        thread.adjustSpeed();
	        
	
	        // Schedule a task to execute after 10 seconds without blocking
	        Timer timer = new Timer("SpeedUpTimer", true); // true to run as a daemon thread
	        timer.schedule(new TimerTask() {
	            @Override
	            public void run() {
	            	level = temp;
	            	PCS.firePropertyChange("level", -1, temp);
	                thread.adjustSpeed();
	                isDoingSpdUp = false;
	            }
	        }, 5000); // Schedule for 10 seconds later
    	}else {
    		return;
    	}
    }
    
    public void exeMinus1() { //테스트용 함수
    	board.Minus1();
    }
    
    public void exeMinus2() {//테스트용 함수
    	board.Minus2();
    }
    
    public void exeCut() {//테스트용 함수
    	
    }
    
    public void exeClear() {//테스트용 함수
    	
    }
    
    public void setFigureGhostLock(boolean value) {
    	figure.setGhostLock(value);
    }
    
    public void setFigureYPosplusone() {
    	if(figure!=null) {
    		figure.setPosY(figure.returnPosY()-1);
    	}
    }

    private void handleGetReady() {
        //board.setMessage("Get Ready");
        bgmPlayer.playLoopingSound("src/bgm/bgm_waitingafterdead.wav"); // 랜덤 BGM 재생
        board.clear(false);
        nextboard.clear();
        state = STATE_GETREADY;
        PCS.firePropertyChange("state", -1, STATE_GETREADY);
    }

    private void handlePause() {
        thread.setPaused(true);
        state = STATE_PAUSED;
        bgmPlayer.pauseBgm();
        board.setMessage("Paused");
        PCS.firePropertyChange("state", -1, STATE_PAUSED);
        tetrisInstance.singlePaused();
    }

    private void handleResume() {
    	bgmPlayer.resumeBgm();
        state = STATE_PLAYING;
        board.setMessage(null);
        thread.setPaused(false);
        PCS.firePropertyChange("state", -1, STATE_PLAYING);
        tetrisInstance.singleResume();
    }

    private void handleLevelModification() {
        PCS.firePropertyChange("level", -1, level);
        thread.adjustSpeed();
    }

    private void handleScoreModification() {
        PCS.firePropertyChange("score", -1, score);
    }
    
//    private void hold() {    	
//    	nextboard.clear();
//        nextFigure.attachToNextBoard(nextboard, true);
//        nextFigure.detach();
//    }

    // hold 기능을 구현하는 메서드
    public void hold() {
        if (!holdAvailable) {
        	BgmPlayer.playsfx("src/sounds/sfx_movefail.wav"); // Add this line to play the sound
        	return; // hold가 이미 사용된 경우
        }

        figure.clearGhost();
        
        if (holdFigure == null) {
        	if(figure.type != nextFigure.type) { //처음 저장할 블럭과 다음 블럭이 같으면 왠진 모르지만 오류가 나니 타입을 비교해서피하자.
	            figure.setRotation(0); //아무리 돌려서 박아도 원 상태 그대로 저장시켜야..
        		holdFigure = figure; // 현재 블록을 hold에 저장
	            figure.clearGhost();
	            figure.clearBlock();
		        figure.detach();
		        BgmPlayer.playsfx("src/sounds/sfx_hold.wav"); // Add this line to play the sound
		        
		        holdboard.clear(); // nextBoard 지우기
		        holdFigure.attachToHoldBoard(holdboard, true); // nextFigure를 nextBoard의 중앙에 부착
		        holdFigure.detach(); // 이미지는 그대로 남아야
		        holdboard.update(); // nextBoard 갱신 변경 사항 적용
		        
		        handleFigureStart(); //첫 홀드 사용일 때만 다음 블록 소환
        	}
        } else {
        	if(figure.type != holdFigure.type) { //블럭이 같으면 왠진 모르지만 오류가 나니 타입을 비교해서피하자.
	            // 현재 블록과 hold 블록 교체
        		figure.setRotation(0); //아무리 돌려서 박아도 원 상태 그대로 저장시켜야..
	            Figure temp = holdFigure;
	            holdFigure = figure;
	            figure.clearGhost();
	            figure.clearBlock();
		        figure.detach();
	            figure = temp;
	            figure.attach(board,  false);
	            BgmPlayer.playsfx("src/sounds/sfx_hold.wav"); // Add this line to play the sound
	            
	            holdboard.clear(); // nextBoard 지우기
	            holdFigure.attachToHoldBoard(holdboard, true); // nextFigure를 nextBoard의 중앙에 부착
	            holdFigure.detach(); // 이미지는 그대로 남아야
	            holdboard.update(); // nextBoard 갱신 변경 사항 적용
	            
        	}else { //블럭 종류가 같아서 못쓰면
        		BgmPlayer.playsfx("src/sounds/sfx_movefail.wav"); // Add this line to play the sound
        	}
            
        }
        
        //figure = null; => 이새끼때문에 홀드가 동작하지 않았음. figure 그리자마자 detach한거나 다름없는 것. 개씨벌
        holdAvailable = false;
        
    }
    
    private void handleFigureStart() {
    	//홀드가 일어났을 때, holdfigure = figure, figure = null, nextfigure = available. 
    	
        int rotation;
        

        if(nextFigure == null) {
        	nextFigure = new Figure(this, randomFigureType());;
        }
        
        

        figure = nextFigure;
        figure.isSpaced = false;
        rotation = 0;
        figure.setRotation(rotation);
        moveLock = false;
        nextFigure = new Figure(this, randomFigureType());;
        nextFigure.setRotation(0);
        //nextFigure.rotateRandom();
        nextRotation = 0;

        if (preview) {
        	try {
            nextboard.clear();
            nextFigure.attachToNextBoard(nextboard, true);
            nextFigure.detach();
        	}catch(NullPointerException e) {
        		
        	}
        }
        
        
        
        if (!figure.attach(board, false)) { //현재 설정된 figure을 보드에 attach 하고, 실패할 시:
            
            System.out.println("over1");
            figure.detach();
            handleGameOver();
            System.out.println("over2");
        }
        
    }

    public void handleFigureLanded() {
    	
    	
    	
    	boolean spaced;
    	spaced = figure.isSpaced;
    	holdAvailable = true;
        
        if (figure.isAllVisible()) {
            score += 10;
            handleScoreModification();
        } else { //isallvisible = false
            handleGameOver();
            System.out.println("over3");
            return;
        }
        if (figure != null) {
        	spaced = figure.isSpaced;
        }else {
        	handleGameOver();
        	System.out.println("over4");
        }
        
        
        
        figure.clearGhost();
    	figure.detach();
        if(figure.hasItemBlock) {
	    	itemLanded(figure.returnItemX(), figure.returnItemY(), figure.returnItemType());
	    	System.out.println("itemx : " + figure.returnItemX() + "itemy : " + figure.returnItemY() + "itemtype : " + figure.returnItemType());
	    	System.out.println("itemlanded");
    	}
        
        
        moveLock = true;
        if (!spaced) { //스페이스로 하드드롭 했으면 실행 안함
        	holdAvailable = true;
        	BgmPlayer.playsfx("src/sounds/sfx_harddrop.wav");
        	//System.out.println("trigger sound");
        	if(board.hasFullLines() ) {
        	    board.removeFullLines();
	            PCS.firePropertyChange("lines", -1, board.getRemovedLines());
	            if (level < 9 && board.getRemovedLines() / 20 > level) {
	                level = board.getRemovedLines() / 20;
	                playSound("src/sounds/levelup.wav"); // Add this line to play the sound
                    board.setMessage("Level UP");
                    bgmPlayer.stopBgm();
                    bgmPlayer.startRandomBgm();
                	board.isShortMessage = true;
	                handleLevelModification();
	            }
	    	}
        	tetrisInstance.returnPosition(figure.returnPosX(), figure.returnPosY(), figure.type, figure.returnOrientation(), figure.returnHasItemBlock(), figure.returnItemIndex(), figure.returnItemType());
        } 
        figure = null;
    }
    
    

    private synchronized void handleTimer() {
    	//synchronized (somethingLock) {
	        if (figure == null && thread.isPaused() == false) {
	            handleFigureStart();
	            //System.out.println("timer.figurestart" + figure + thread.isPaused());
	            if(figure != null) {
	            	figure.makeGhost();
	            }
	        } else if (figure.hasLanded()) {
	            handleFigureLanded();
	        } else {
	            figure.moveDown();
	        }
    	//}
    }

    private synchronized void handlePauseOnOff() {
        if (nextFigure == null) {
            handleStart();
        } else if (thread.isPaused()) {
            handleResume();
        } else {
            handlePause();
        }
    }

    private synchronized void handleKeyEvent(KeyEvent e) {
        if (state == STATE_GETREADY) {
        	
            handleStart();
            return;
        }

        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
        	if(isSingle) {
        		handlePauseOnOff();
        	}
            return;
        }

        if (figure == null || moveLock || thread.isPaused()) {
            return;
        }

        switch (e.getKeyCode()) {
        
	        case KeyEvent.VK_SHIFT:
	        	hold();
	        	break;

            case KeyEvent.VK_LEFT:
                figure.moveLeft();
                break;

            case KeyEvent.VK_RIGHT:
                figure.moveRight();
                break;

            case KeyEvent.VK_DOWN:
            	figure.moveDown();
            	BgmPlayer.playsfx("src/sounds/sfx_softdrop.wav"); // Add this line to play the sound
                break;

            case KeyEvent.VK_UP:
            if (e.isControlDown()) {
            		figure.rotateClockwise();
                } else {
                    figure.rotateCounterClockwise();
                }
                break;
                
                
                
            case KeyEvent.VK_SPACE:
            	
            	figure.moveAllWayDown();
            	figure.isSpaced = true;
            	
            	
            	
            	holdAvailable = true;
            	figure.clearGhost();
                figure.detach();
                if(figure.hasItemBlock) {
                	System.out.println("itemlanded");
        	    	itemLanded(figure.returnItemX(), figure.returnItemY(), figure.returnItemType());
        	    	System.out.println("itemx : " + figure.returnItemX() + "itemy : " + figure.returnItemY() + "itemtype : " + figure.returnItemType());
        	    	
            	}
            	BgmPlayer.playsfx("src/sounds/sfx_harddrop.wav");
	            	if (board.hasFullLines()) {
	                    board.removeFullLines();
	                    PCS.firePropertyChange("lines", -1, board.getRemovedLines());
	                    if (level < 9 && board.getRemovedLines() / 20 > level) {
	                        level = board.getRemovedLines() / 20;
	                        playSound("src/sounds/levelup.wav"); // Add this line to play the sound
	                        board.setMessage("Level UP");
	                        board.isShortMessage = true;
	                        bgmPlayer.stopBgm();
	                        bgmPlayer.startRandomBgm();
	                    	
	                        handleLevelModification();
	                    }
	                
	            	}
	            	tetrisInstance.returnPosition(figure.returnPosX(), figure.returnPosY(), figure.type, figure.returnOrientation(), figure.returnHasItemBlock(), figure.returnItemIndex(), figure.returnItemType());
	                figure = null;
                moveLock = true;
            	break;
            	
            case KeyEvent.VK_1:
            	// 내 ClientNumber와 squareboard2의 clientNumber를 레이어에 맞게 123456으로 정할 수 없으니, (내 화면을 제외한 다섯 개의 번호를 otherboard에 배치했기 때문.)
            	// 우선 아이템 사용 내역을 서버로 쏜 다음 tetris 내부에서 분류하여 적용시킨다.
            	if(!player1dead) { //해당 플레이어가 안죽었어야 실행함
	            	if(!isSingle) { //싱글모드로 할 때는 상대에게 영향이 가면 안 된다.
		            	if(!itemboard.isEmpty()) {
		            		//otherBoardsMap.get(1)
		            		tetrisInstance.returnUsedItem(clientCount, itemboard.useItem(), 1); //내 번호, 아이템 타입, 몇 번 키를 눌러 호출하였는지
		            		BgmPlayer.playsfx("src/sounds/sfx_itemuse.wav"); // Add this line to play the sound
		            	}else {
		            		BgmPlayer.playsfx("src/sounds/sfx_movefail.wav"); // Add this line to play the sound
		            	}
	            	}else {
	            		if(!itemboard.isEmpty()) {
	            			int randomNum = (int)(Math.random()*10);
	            			useItem(clientCount, itemboard.useItem(), randomNum);
		            		BgmPlayer.playsfx("src/sounds/sfx_itemuse.wav"); // Add this line to play the sound
		            	}else {
		            		BgmPlayer.playsfx("src/sounds/sfx_movefail.wav"); // Add this line to play the sound
		            	}
	            	}
            	}else {
            		BgmPlayer.playsfx("src/sounds/sfx_movefail.wav"); // Add this line to play the sound
            		// 해당 플레이어가 죽었으면 아이템이 써지면 안됨
            	}
            	break;
            case KeyEvent.VK_2:
            	// 내 ClientNumber와 squareboard2의 clientNumber를 레이어에 맞게 123456으로 정할 수 없으니, (내 화면을 제외한 다섯 개의 번호를 otherboard에 배치했기 때문.)
            	// 우선 아이템 사용 내역을 서버로 쏜 다음 tetris 내부에서 분류하여 적용시킨다.
            	if(!player2dead) { //해당 플레이어가 안죽었어야 실행함
	            	if(!isSingle) { //싱글모드로 할 때는 상대에게 영향이 가면 안 된다.
		            	if(totalPlayers >= 2) {
			            	if(!itemboard.isEmpty()) {
			            	
			            		tetrisInstance.returnUsedItem(clientCount, itemboard.useItem(), 2); //내 번호, 아이템 타입, 몇 번 키를 눌러 호출하였는지
			            		BgmPlayer.playsfx("src/sounds/sfx_itemuse.wav"); // Add this line to play the sound
			            	}else {
			            		BgmPlayer.playsfx("src/sounds/sfx_movefail.wav"); // Add this line to play the sound
			            	}
		            	}else {
		            		BgmPlayer.playsfx("src/sounds/sfx_movefail.wav"); // Add this line to play the sound
		            	}
	            	}else { //싱글모드로 할 때는 상대에게 영향이 가면 안 된다.
	            		if(!itemboard.isEmpty()) {
			            	
	            			int randomNum = (int)(Math.random()*10);
	            			useItem(clientCount, itemboard.useItem(), randomNum);
		            		BgmPlayer.playsfx("src/sounds/sfx_itemuse.wav"); // Add this line to play the sound
		            	}else {
		            		BgmPlayer.playsfx("src/sounds/sfx_movefail.wav"); // Add this line to play the sound
		            	}
	            	}
            	}else {
            		BgmPlayer.playsfx("src/sounds/sfx_movefail.wav"); // Add this line to play the sound
            		// 해당 플레이어가 죽었으면 아이템이 써지면 안됨
            	}
            	break;
            case KeyEvent.VK_3:
            	// 내 ClientNumber와 squareboard2의 clientNumber를 레이어에 맞게 123456으로 정할 수 없으니, (내 화면을 제외한 다섯 개의 번호를 otherboard에 배치했기 때문.)
            	// 우선 아이템 사용 내역을 서버로 쏜 다음 tetris 내부에서 분류하여 적용시킨다.
            	if(!player3dead) { //해당 플레이어가 안죽었어야 실행함
            		if(!isSingle) { //싱글모드로 할 때는 상대에게 영향이 가면 안 된다.
		            	if(totalPlayers >= 3) {
			            	if(!itemboard.isEmpty()) {
			            	
			            		tetrisInstance.returnUsedItem(clientCount, itemboard.useItem(), 3); //내 번호, 아이템 타입, 몇 번 키를 눌러 호출하였는지
			            		BgmPlayer.playsfx("src/sounds/sfx_itemuse.wav"); // Add this line to play the sound
			            	}else {
			            		BgmPlayer.playsfx("src/sounds/sfx_movefail.wav"); // Add this line to play the sound
			            	}
		            	}else {
		            		BgmPlayer.playsfx("src/sounds/sfx_movefail.wav"); // Add this line to play the sound
		            	}
	            	}else { //싱글모드로 할 때는 상대에게 영향이 가면 안 된다.
	            		if(!itemboard.isEmpty()) {
			            	
	            			int randomNum = (int)(Math.random()*10);
	            			useItem(clientCount, itemboard.useItem(), randomNum);
		            		BgmPlayer.playsfx("src/sounds/sfx_itemuse.wav"); // Add this line to play the sound
		            	}else {
		            		BgmPlayer.playsfx("src/sounds/sfx_movefail.wav"); // Add this line to play the sound
		            	}
	            	}
            	}else {
            		BgmPlayer.playsfx("src/sounds/sfx_movefail.wav"); // Add this line to play the sound
            		// 해당 플레이어가 죽었으면 아이템이 써지면 안됨
            	}
            	break;
            case KeyEvent.VK_4:
            	// 내 ClientNumber와 squareboard2의 clientNumber를 레이어에 맞게 123456으로 정할 수 없으니, (내 화면을 제외한 다섯 개의 번호를 otherboard에 배치했기 때문.)
            	// 우선 아이템 사용 내역을 서버로 쏜 다음 tetris 내부에서 분류하여 적용시킨다.
            	if(!player4dead) { //해당 플레이어가 안죽었어야 실행함
	            	if(!isSingle) { //싱글모드로 할 때는 상대에게 영향이 가면 안 된다.
		            	if(totalPlayers >= 4) {
			            	if(!itemboard.isEmpty()) {
			            	
			            		tetrisInstance.returnUsedItem(clientCount, itemboard.useItem(), 4); //내 번호, 아이템 타입, 몇 번 키를 눌러 호출하였는지
			            		BgmPlayer.playsfx("src/sounds/sfx_itemuse.wav"); // Add this line to play the sound
			            	}else {
			            		BgmPlayer.playsfx("src/sounds/sfx_movefail.wav"); // Add this line to play the sound
			            	}
		            	}else {
		            		BgmPlayer.playsfx("src/sounds/sfx_movefail.wav"); // Add this line to play the sound
		            	}
	            	}else { //싱글모드로 할 때는 상대에게 영향이 가면 안 된다.
	
		            	if(!itemboard.isEmpty()) {
		            	
		            		int randomNum = (int)(Math.random()*10);
	            			useItem(clientCount, itemboard.useItem(), randomNum);
		            		BgmPlayer.playsfx("src/sounds/sfx_itemuse.wav"); // Add this line to play the sound
		            	}else {
		            		BgmPlayer.playsfx("src/sounds/sfx_movefail.wav"); // Add this line to play the sound
		            	}
	            	}
            	}else {
            		BgmPlayer.playsfx("src/sounds/sfx_movefail.wav"); // Add this line to play the sound
            		// 해당 플레이어가 죽었으면 아이템이 써지면 안됨
            	}
            	break;
            case KeyEvent.VK_5:
            	// 내 ClientNumber와 squareboard2의 clientNumber를 레이어에 맞게 123456으로 정할 수 없으니, (내 화면을 제외한 다섯 개의 번호를 otherboard에 배치했기 때문.)
            	// 우선 아이템 사용 내역을 서버로 쏜 다음 tetris 내부에서 분류하여 적용시킨다.
            	if(!player5dead) { //해당 플레이어가 안죽었어야 실행함
	            	if(!isSingle) { //싱글모드로 할 때는 상대에게 영향이 가면 안 된다.
		            	if(totalPlayers >= 5) {
			            	if(!itemboard.isEmpty()) {
			            	
			            		tetrisInstance.returnUsedItem(clientCount, itemboard.useItem(), 5); //내 번호, 아이템 타입, 몇 번 키를 눌러 호출하였는지
			            		BgmPlayer.playsfx("src/sounds/sfx_itemuse.wav"); // Add this line to play the sound
			            	}else {
			            		BgmPlayer.playsfx("src/sounds/sfx_movefail.wav"); // Add this line to play the sound
			            	}
		            	}else {
		            		BgmPlayer.playsfx("src/sounds/sfx_movefail.wav"); // Add this line to play the sound
		            	}
	            	}else { //싱글모드로 할 때는 상대에게 영향이 가면 안 된다.
	            		if(!itemboard.isEmpty()) {
			            	
	            			int randomNum = (int)(Math.random()*10);
	            			useItem(clientCount, itemboard.useItem(), randomNum);
		            		BgmPlayer.playsfx("src/sounds/sfx_itemuse.wav"); // Add this line to play the sound
		            	}else {
		            		BgmPlayer.playsfx("src/sounds/sfx_movefail.wav"); // Add this line to play the sound
		            	}
	            	}
            	}else {
            		BgmPlayer.playsfx("src/sounds/sfx_movefail.wav"); // Add this line to play the sound
            		// 해당 플레이어가 죽었으면 아이템이 써지면 안됨
            	}
            	break;
            case KeyEvent.VK_6:
            	// 내 ClientNumber와 squareboard2의 clientNumber를 레이어에 맞게 123456으로 정할 수 없으니, (내 화면을 제외한 다섯 개의 번호를 otherboard에 배치했기 때문.)
            	// 우선 아이템 사용 내역을 서버로 쏜 다음 tetris 내부에서 분류하여 적용시킨다.
            	if(!player6dead) { //해당 플레이어가 안죽었어야 실행함
	            	if(!isSingle) { //싱글모드로 할 때는 상대에게 영향이 가면 안 된다.
		            	if(totalPlayers == 6) {
			            	if(!itemboard.isEmpty()) {
			            	
			            		tetrisInstance.returnUsedItem(clientCount, itemboard.useItem(), 6); //내 번호, 아이템 타입, 몇 번 키를 눌러 호출하였는지
			            		BgmPlayer.playsfx("src/sounds/sfx_itemuse.wav"); // Add this line to play the sound
			            	}else {
			            		BgmPlayer.playsfx("src/sounds/sfx_movefail.wav"); // Add this line to play the sound
			            	}
		            	}else {
		            		BgmPlayer.playsfx("src/sounds/sfx_movefail.wav"); // Add this line to play the sound
		            	}
	            	}else { //싱글모드로 할 때는 상대에게 영향이 가면 안 된다.
	            		if(!itemboard.isEmpty()) {
			            	
	            			int randomNum = (int)(Math.random()*10);
	            			useItem(clientCount, itemboard.useItem(), randomNum);
		            		BgmPlayer.playsfx("src/sounds/sfx_itemuse.wav"); // Add this line to play the sound
		            	}else {
		            		BgmPlayer.playsfx("src/sounds/sfx_movefail.wav"); // Add this line to play the sound
		            	}
	            	}
            	}else {
            		BgmPlayer.playsfx("src/sounds/sfx_movefail.wav"); // Add this line to play the sound
            		// 해당 플레이어가 죽었으면 아이템이 써지면 안됨
            	}
            	break;
            	
            case KeyEvent.VK_ALT:
            	BgmPlayer.playsfx("src/sounds/swoosh.wav"); // Add this line to play the sound
            	if(!isSingle) {
	            	itemboard.rotateItems();
	            	board.rotateItems();
	            	tetrisInstance.rotatedItems();
            	}else {
            		itemboard.rotateItems();
	            	board.rotateItems();
            	}
            	break;
            	
            case KeyEvent.VK_7: //test
            	exeMinus1();
            	break;
            	
            case KeyEvent.VK_8: //test
            	tetrisInstance.returnUsedItem(clientCount, 5, 1); //내 번호, 아이템 타입, 몇 번 키를 눌러 호출하였는지
            	break;
            	
            case KeyEvent.VK_9: //test
            	exePlus1();
            	break;
            	
            case KeyEvent.VK_0: //test
            	tetrisInstance.returnUsedItem(clientCount, 2, 1); //내 번호, 아이템 타입, 몇 번 키를 눌러 호출하였는지
            	break;

            case KeyEvent.VK_S: //test
                if (level < 9) {
                    level++;
                    handleLevelModification();
                }
                break;

        }
    }

    public synchronized void makeFigure(int clientNumber, int x, int y, int type, int orientation, boolean hasItemBlock, int itemIndex, int itemType) {
    	synchronized (somethingLock) {
    	//	synchronized (somethingLock2) {
	    	SquareBoard2 boardForPlayer = otherBoardsMap.get(clientNumber);
	    	otherFigure = new Figure(this, type);
	    	otherFigure.setPosX(x);
	    	otherFigure.setPosY(y);
	    	
	    	otherFigure.setHasItemBlock(hasItemBlock);
	    	otherFigure.setItemIndex(itemIndex);
	    	otherFigure.setItemType(itemType);
	    	
	    	otherFigure.setOrientation(orientation); // setOrientation에 paint()가 포함되어 있으므로, item 정보를 덮어씌운 후 refresh의 역할을 수행할 것 같음.
	    	
	    	otherFigure.setblock(boardForPlayer, Color.white);
	    	//System.out.println("type = " + otherFigure.type + "pos : " + otherFigure.returnPosX() + otherFigure.returnPosY() + "orient : " +otherFigure.returnOrientation());
	    //}
    	}
    }
    
    public synchronized void handleotherFigureLanded(int clientNumber) {
        otherFigure.detach();
        otherFigure = null;
        
        SquareBoard2 boardForPlayer = otherBoardsMap.get(clientNumber);
        
        if(boardForPlayer.hasFullLines() ) {
        	boardForPlayer.removeFullLines();
            PCS.firePropertyChange("lines", -1, boardForPlayer.getRemovedLines());
            
    	}
    }
    
    public void mute() {
    	bgmPlayer.mute();
    }
    
    public void unmute() {
    	bgmPlayer.unmute();
    }
    
    public void collectItem(int itemType) {
    	
    	clearGhost();
    	itemboard.addItem(itemType);
    	System.out.println("collectitem");
    	
    	tetrisInstance.gotItems(itemType);
    }
    
    public synchronized void collectItem2(int clientNumber, int itemType) {
    	
    	ItemBoard2 boardForPlayer = otherItemBoardsMap.get(clientNumber);
    	boardForPlayer.addItem(itemType);
	
    }

    public void makeFinishClear() { //모두가 끝났을 때 보드 클리어
    	for (SquareBoard2 board2 : otherBoardsMap.values()) {
            board2.clear();
        }
    	
    	for (ItemBoard2 boardForPlayer : otherItemBoardsMap.values()) {
    		boardForPlayer.clearItems();
        }
    	
    	board.clear(false);
    }
    
    public void setClientNum(int count) {
    	clientCount = count;
    }
    
    public void setReadyCount(int count) {
    	totalPlayers = count;
    }

    private class GameThread extends Thread {

        private boolean paused = true;

        private int sleepTime = 500;

        public GameThread() {
        }

        public void reset() {
            adjustSpeed();
            setPaused(false);
            if (!isAlive()) {
                this.start();
            }
        }

        public boolean isPaused() {
            return paused;
        }

        public void setPaused(boolean paused) {
            this.paused = paused;
        }

        public void adjustSpeed() {
            sleepTime = 4500 / (level + 5) - 250;
            if (sleepTime < 50) {
                sleepTime = 50;
            }
        }

        public void run() {
            while (thread == this && !paused) {
                handleTimer();

                try{
                	figure.makeGhost();
                }catch(NullPointerException e) {
                	
                }

                long startTime = System.currentTimeMillis(); // 시작 시간을 기록
    	        long elapsedTime = 0L;
    	        
    	        while (elapsedTime < sleepTime) { // sleepTime초간 딜레이. (thread.sleep으로 하니 부작용이 너무 많음)
    	            elapsedTime = System.currentTimeMillis() - startTime;
    	        }

                while (paused && thread == this) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ignore) {
                    }
                }
            }
        }
    }
}