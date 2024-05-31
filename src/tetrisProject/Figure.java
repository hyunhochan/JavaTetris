package tetrisProject;

import java.awt.Color;
import java.util.HashMap;
import java.util.Random;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Figure extends Object {

    public static final int SQUARE_FIGURE = 1;

    public static final int LINE_FIGURE = 2;

    public static final int S_FIGURE = 3;

    public static final int Z_FIGURE = 4;

    public static final int RIGHT_ANGLE_FIGURE = 5;

    public static final int LEFT_ANGLE_FIGURE = 6;

    public static final int TRIANGLE_FIGURE = 7;

    private SquareBoard board = null;
    private SquareBoard2 board2 = null;
    private NextBoard nextBoard = null;
    private HoldBoard holdBoard = null;
    private Game game;

    private int xPos = 0;

    private int yPos = 0;
    
    private int ghostYPos = 0; // 고스트 블록의 Y 위치를 추적합니다.

    private int orientation = 0;

    private int maxOrientation = 4;

    private int[] shapeX = new int[4];

    private int[] shapeY = new int[4];

    private Color color = Color.white;
    
    private BufferedImage blockImage;
    private BufferedImage blockImage2;
    private BufferedImage grayImage;
    private BufferedImage itemImage;
    private BufferedImage itemImage2;
    
    public int type;
    
    private boolean ghostLock = false;
    
    public boolean isSpaced = false;
    
    public boolean isOther;
    
    // 아이템 파일들의 경로를 저장하는 HashMap 선언
    private HashMap<Integer, String> itemImages;
    private HashMap<Integer, String> ghostItemImages;
    
    private final Random random;
    
    public boolean hasItemBlock; // 아이템 블록 여부를 나타내는 플래그
    private int itemBlockIndex; // 아이템 블록 인덱스
    
    private int itemX; // 아이템 블록의 X 위치
    private int itemY; // 아이템 블록의 Y 위치
    private int itemType; // 아이템의 타입
    
    private int randomNum;
    

    public Figure(Game game, int type) throws IllegalArgumentException {
    	this.game = game;
    	this.type = type; // Store the type
    	
    	itemImages = new HashMap<>();
    	itemImages.put(1, "src/items/plus.png"); //구현가능
    	itemImages.put(2, "src/items/plus2.png"); //구현가능
    	//itemImages.put(3, "src/items/bomb.png");
    	//itemImages.put(4, "src/items/steal.png"); 
    	itemImages.put(3, "src/items/speedup.png"); //구현가능
    	//itemImages.put(6, "src/items/dark.png");
    	//itemImages.put(7, "src/items/delete.png");
    	//itemImages.put(8, "src/items/twist.png");
    	//itemImages.put(9, "src/items/switch.png");
    	itemImages.put(4, "src/items/minus.png"); //구현가능
    	itemImages.put(5, "src/items/minus2.png"); //구현가능
    	//itemImages.put(12, "src/items/mirror.png");
    	//itemImages.put(13, "src/items/10t.png");
    	itemImages.put(6, "src/items/cut.png"); //가능
    	//itemImages.put(15, "src/items/drop.png");
    	itemImages.put(7, "src/items/clear.png"); //가능
    	//itemImages.put(17, "src/items/power.png");
        
    	ghostItemImages = new HashMap<>();
    	ghostItemImages.put(1, "src/items/plusGhost.png"); //구현가능
    	ghostItemImages.put(2, "src/items/plus2Ghost.png"); //구현가능
    	ghostItemImages.put(3, "src/items/speedupGhost.png"); //구현가능
    	ghostItemImages.put(4, "src/items/minusGhost.png"); //구현가능
    	ghostItemImages.put(5, "src/items/minus2Ghost.png"); //구현가능
    	ghostItemImages.put(6, "src/items/cutGhost.png"); //구현가능
    	ghostItemImages.put(7, "src/items/clearGhost.png"); //구현가능
    	
    	random = new Random();
    	
    	// 아이템 블록을 결정합니다.
        itemBlockIndex = random.nextInt(shapeX.length); // 네모 블록 중 하나를 선택
        hasItemBlock = random.nextInt(100) < 30; // 1% 확률로 아이템 블록 설정
    	randomNum = random.nextInt(7) + 1;
    	
        
    	initialize(type);
        
    }
    
        private void initialize(int type) throws IllegalArgumentException {

        // 기본 변수 초기화
        board = null;
        board2 = null;
        nextBoard = null;
        holdBoard = null;
        xPos = 0;
        yPos = 0;
        orientation = 0;
        BufferedImage fullImage = null;
        
        
        
        
        try {
            // graysquare.png 이미지를 로드합니다.
        	grayImage = ImageIO.read(new File("src/images/blocksquare.png"));
        	//아이템 이미지 중 하나 선택
        	
            itemType = randomNum; //아이템 배열의 몇 번째 아이템을 저장 중인지 추적시작함
            String imgPath = itemImages.get(randomNum);
            String imgPath2 = ghostItemImages.get(randomNum);
            itemImage = ImageIO.read(new File(imgPath)); // 한 블럭당 생성되는 아이템의 종류는 1개.
            itemImage2 = ImageIO.read(new File(imgPath2)); // 한 블럭당 생성되는 아이템의 종류는 1개.
        } catch (IOException e) {
            e.printStackTrace();
            return; // 이미지 로드에 실패한 경우 함수를 종료합니다.
        }

        // 도형 유형 변수 초기화
        switch (type) {
            case SQUARE_FIGURE:
                maxOrientation = 4;
                
                try {
    				blockImage = ImageIO.read(new File("src/images/sqsquare.png"));
    				blockImage2 = ImageIO.read(new File("src/images/sqghost.png"));
    			} catch (IOException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
                
                shapeX[0] = -1;	shapeY[0] = -1;
                shapeX[1] = 0;	shapeY[1] = -1;
                shapeX[2] = -1;	shapeY[2] = 0;
                shapeX[3] = 0;	shapeY[3] = 0;
                break;
            case LINE_FIGURE:
                maxOrientation = 4;
                
                try {
    				blockImage = ImageIO.read(new File("src/images/longsquare.png"));
    				blockImage2 = ImageIO.read(new File("src/images/longghost.png"));
    			} catch (IOException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
                
                shapeX[0] = 0;	shapeY[0] = 1;
                shapeX[1] = 0;	shapeY[1] = 0;
                shapeX[2] = 0;	shapeY[2] = -1;
                shapeX[3] = 0;	shapeY[3] = -2;
                break;
            case S_FIGURE:
                maxOrientation = 4;
                
                try {
    				blockImage = ImageIO.read(new File("src/images/ssquare.png"));
    				blockImage2 = ImageIO.read(new File("src/images/sghost.png"));
    			} catch (IOException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
                
                shapeX[0] = 0;	shapeY[0] = 0;
                shapeX[1] = 0;	shapeY[1] = -1;
                shapeX[2] = -1;	shapeY[2] = 1;
                shapeX[3] = -1;	shapeY[3] = 0;
                break;
            case Z_FIGURE:
                maxOrientation = 4;
                
                try {
    				blockImage = ImageIO.read(new File("src/images/zsquare.png"));
    				blockImage2 = ImageIO.read(new File("src/images/zghost.png"));
    			} catch (IOException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
                
                shapeX[0] = 0;	shapeY[0] = 1;
                shapeX[1] = 0;	shapeY[1] = 0;
                shapeX[2] = -1;	shapeY[2] = 0;
                shapeX[3] = -1;	shapeY[3] = -1;
                break;
            case RIGHT_ANGLE_FIGURE: //주황
                maxOrientation = 4;
                
                try {
    				blockImage = ImageIO.read(new File("src/images/lsquare.png"));
    				blockImage2 = ImageIO.read(new File("src/images/lghost.png"));
    			} catch (IOException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
                
                shapeX[0] = 0;	shapeY[0] = 1;
                shapeX[1] = 0;	shapeY[1] = 0;
                shapeX[2] = 0;	shapeY[2] = -1;
                shapeX[3] = -1;	shapeY[3] = -1;
                break;
            case LEFT_ANGLE_FIGURE: //파랑
                maxOrientation = 4;
                
                try {
    				blockImage = ImageIO.read(new File("src/images/rlsquare.png"));
    				blockImage2 = ImageIO.read(new File("src/images/rlghost.png"));
    			} catch (IOException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
                
                shapeX[0] = -1;	shapeY[0] = -1;
                shapeX[1] = -1;	shapeY[1] = 0;
                shapeX[2] = -1;	shapeY[2] = 1;
                shapeX[3] = 0;	shapeY[3] = -1;
                break;
            case TRIANGLE_FIGURE:
                maxOrientation = 4;
                
                try {
    				blockImage = ImageIO.read(new File("src/images/trisquare.png"));
    				blockImage2 = ImageIO.read(new File("src/images/trighost.png"));
    			} catch (IOException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
                
                shapeX[0] = 0;	shapeY[0] = 1;
                shapeX[1] = 0;	shapeY[1] = 0;
                shapeX[2] = 0;	shapeY[2] = -1;
                shapeX[3] = -1;	shapeY[3] = 0;
                break;
            default:
                throw new IllegalArgumentException("No figure constant: " +
                        type);
        }
    }

    public boolean isAttached() {
    	if(board != null) {
    		return true;
    	}else {
    		if(this.isOther == true) {
    			return true;
    		}else {
    			return false;
    		}
    	}
    	
    }

    public boolean attach(SquareBoard board, boolean center) {
        int newX;
        int newY;
        int i;

        // 이전 연결 여부 확인
        if (isAttached()) {
        	
            detach();
        }

        // 위치 초기화 (올바른 조작을 위해)
        xPos = 0;
        yPos = 0;

        // 위치 계산 (보드 가운데에 놓기)
        newX = board.getBoardWidth() / 2;
        if (center) {
            newY = board.getBoardHeight() / 2;
        } else {
            newY = 0;
            for (i = 0; i < shapeX.length; i++) {
                if (getRelativeY(i, orientation) - newY > 0) {
                    newY = -getRelativeY(i, orientation);
                }
            }
        }

        // 위치 확인
        this.board = board;
        if (!canMoveTo(newX, newY, orientation)) {
        	
            this.board = null;
            return false;
        }

        
        //clearGhost();
        // 도형 그리기
        xPos = newX;
        yPos = newY;
        
        paint(color);
        board.update();
        

        return true;
    }

    public void detach() {    	
    	int x;
    	int y;
    	for (int i = 0; i < shapeX.length; i++) {
            x = xPos + getRelativeX(i, orientation);
            y = yPos + getRelativeY(i, orientation);
            
                // 지정된 블록이 아이템 블록이면 아이템 이미지를 그립니다. (squareboard1일 경우에만.)
                if (i == itemBlockIndex && hasItemBlock && board!=null) {
                    
                    //아이템이 어느 위치에 있는지에 대해 정보를 저장함,
                    //블럭이 '착지'할 때 작동하는 함수이므로 최종 위치가 영구 저장됨.
                    itemX = x;
                    itemY = y;
                    System.out.println("item PosX : " + itemX + "item PosY : " + itemY);
                } else {
                    //nothing
                }
            
        }
    	
    	
    	
        board = null;
        
        
    }
    
    // nextBoard에 figure를 부착하는 메서드
    public boolean attachToNextBoard(NextBoard board, boolean center) {
    	
    	int newX;
        int newY;
        int i;

        // 이전 연결 여부 확인
        if (isAttached()) {
        	
            detach();
        }

        // 위치 초기화 (올바른 조작을 위해)
        xPos = 0;
        yPos = 0;

     // 위치 계산
        newX = board.getBoardWidth() / 2;
        if (center) {
            newY = board.getBoardHeight() / 2;
        } else {
            newY = 0;
            for (i = 0; i < shapeX.length; i++) {
                if (getRelativeY(i, orientation) - newY > 0) {
                    newY = -getRelativeY(i, orientation);
                }
            }
        }

        // 위치 확인
        this.nextBoard = board;

        
        //clearGhost();
        // 도형 그리기
        xPos = newX;
        yPos = newY;
        
        paintNextBoard(color);
        board.update();
        

        return true;
    }
    
 // nextBoard에 figure를 부착하는 메서드
    public boolean attachToHoldBoard(HoldBoard board, boolean center) {
    	
    	int newX;
        int newY;
        int i;

        // 이전 연결 여부 확인
        if (isAttached()) {
        	
            detach();
        }

        // 위치 초기화 (올바른 조작을 위해)
        xPos = 0;
        yPos = 0;

     // 위치 계산
        newX = board.getBoardWidth() / 2;
        if (center) {
            newY = board.getBoardHeight() / 2;
        } else {
            newY = 0;
            for (i = 0; i < shapeX.length; i++) {
                if (getRelativeY(i, orientation) - newY > 0) {
                    newY = -getRelativeY(i, orientation);
                }
            }
        }

        // 위치 확인
        this.holdBoard = board;

        
        
        // 도형 그리기
        xPos = newX;
        yPos = newY;
        
        //paint(null);
        paintHoldBoard(color);
        //board.clear
        board.update();
        

        return true;
    }

    public boolean isAllVisible() {
//    	public boolean isAttached() {
//            return board != null;
//        }
        if (!isAttached()) {
            return false;
        }
        for (int i = 0; i < shapeX.length; i++) {
            if (yPos + getRelativeY(i, orientation) < 0) {
                return false;
            }
        }
        return true;
    }

    public boolean hasLanded() {
        return !isAttached() || !canMoveTo(xPos, yPos + 1, orientation);
    }

    public void moveLeft() {
        if (isAttached() && canMoveTo(xPos - 1, yPos, orientation)) {
        	paint(null);
            clearGhost();
            xPos--;      
            BgmPlayer.playsfx("src/sounds/sfx_move.wav"); // Add this line to play the sound
            makeGhost();
            paint(color);
            board.update();

        }else {
        	BgmPlayer.playsfx("src/sounds/sfx_movefail.wav"); // Add this line to play the sound
        }
       
    }

    public void moveRight() {
        if (isAttached() && canMoveTo(xPos + 1, yPos, orientation)) {
            paint(null);
            clearGhost();
            xPos++;
            BgmPlayer.playsfx("src/sounds/sfx_move.wav"); // Add this line to play the sound
            makeGhost();
            paint(color);
            board.update();
        }else {
        	BgmPlayer.playsfx("src/sounds/sfx_movefail.wav"); // Add this line to play the sound
        }
    }

    public void moveDown() {
        if (isAttached() && canMoveTo(xPos, yPos + 1, orientation)) {
            paint(null);
            yPos++;
            paint(color);
            board.update();
        }
    }

    public void moveAllWayDown() {
        int y = yPos;

        // 보드 확인
        if (!isAttached()) {
            return;
        }

        // 가장 낮은 위치 찾기
        while (canMoveTo(xPos, y + 1, orientation)) {
            y++;
        }

        // 업데이트
        if (y != yPos) {
        	clearGhost();
        	//BgmPlayer.playsfx("src/sounds/sfx_harddrop.wav"); // Add this line to play the sound
        	
            paint(null);
            yPos = y;
            paint(color);
            board.update();
        }
    }
    
    public void makeGhost() {
    	
        if(board!= null) {
	    	ghostYPos = yPos;
	        if (!isAttached()) {
	            return;
	        }
	        
	
	        // 새 고스트 블록 위치 계산
	        while (canMoveTo(xPos, ghostYPos + 1, orientation)) {
	            ghostYPos++;
	        }
	
	        // 고스트 블록을 그립니다.
	        paintGhost(ghostYPos);
        }
    }
    
    

    public int getRotation() {
        return orientation;
    }

    public void setRotation(int rotation) {
        int newOrientation;

        // 새로운 방향 설정
        newOrientation = rotation % maxOrientation;

        // 새 위치 확인
        if (!isAttached()) {
            orientation = newOrientation;
        } else if (canMoveTo(xPos, yPos, newOrientation)) {
            BgmPlayer.playsfx("src/sounds/sfx_rotate.wav"); // Add this line to play the sound
            paint(null);
            clearGhost();
            orientation = newOrientation;
            paint(color);
            board.update();
        } else if (!canMoveTo(xPos, yPos, newOrientation)) {
        	BgmPlayer.playsfx("src/sounds/sfx_movefail.wav"); // Add this line to play the sound
        }
    }
    
    
    
    
    
    public void rotateRandom() {
        setRotation((int) (Math.random() * 4.0) % maxOrientation);
    }

    public void rotateClockwise() {
    	
        if (maxOrientation == 1) {
        	makeGhost();
        	
            return;
        } else {
            setRotation((orientation + 1) % maxOrientation);
            makeGhost();
        }
    }

    public void rotateCounterClockwise() {
    	
        if (maxOrientation == 1) {
        	makeGhost();
            return;
        } else {
            setRotation((orientation + 3) % 4);
            makeGhost();
        }
    }

    private boolean isInside(int x, int y) {
        for (int i = 0; i < shapeX.length; i++) {
            if (x == xPos + getRelativeX(i, orientation)
                    && y == yPos + getRelativeY(i, orientation)) {

                return true;
            }
        }
        return false;
    }

    private boolean canMoveTo(int newX, int newY, int newOrientation, boolean ignoreCurrentPiece) {
	        
    	if(this.board!=null) {
    		for (int i = 0; i < 4; i++) {
	            int x = newX + getRelativeX(i, newOrientation);
	            int y = newY + getRelativeY(i, newOrientation);
	            if ((ignoreCurrentPiece || !isInside(x, y)) && !board.isSquareEmpty(x, y)) {
	            	
	                return false;
	            }
	        }
	        return true;
    	}else {
    		return false;
    	}
    }

    // 기존 canMoveTo 호출에 대한 오버로딩을 통해 호환성 유지
    private boolean canMoveTo(int newX, int newY, int newOrientation) {
        return canMoveTo(newX, newY, newOrientation, false);
    }

    private int getRelativeX(int square, int orientation) { //회전변환
        switch (orientation % 4) {
            case 0:
            	if(type == 1) { //블록이 네모일 경우
            		return shapeX[square]; //0번 로테이션은 기본 그대로
            	}else { //다른 블록들
            		return shapeX[square];
            	}
            case 1:
            	if(type == 1) { //블록이 네모일 경우 //왼쪽1칸 아래1칸 움직임. //3번째 블록 //(회전변환 완료)
            		return -shapeY[square]-1;
            	}else { //다른 블록들
                	return -shapeY[square];
            	}
            case 2:
            	if(type == 1) { //블록이 네모일 경우 //(회전변환 완료)
            		return -shapeX[square] - 1;
            	}else { //다른 블록들
                	return -shapeX[square];
            	}
            case 3:
            	if(type == 1) { //블록이 네모일 경우 //(회전변환 완료)
            		return shapeY[square];
            	}else { //다른 블록들
                	return shapeY[square];
            	}
            default:
                return 0; // 발생하지 않아야 함
        }
    }

    private int getRelativeY(int square, int orientation) { //회전변환
        switch (orientation % 4) {
            case 0:
            	if(type == 1) { //블록이 네모일 경우 //(회전변환 완료)
            		return shapeY[square];
            	}else { //다른 블록들
                	return shapeY[square];
            	}
            case 1:
            	if(type == 1) { //블록이 네모일 경우 //(회전변환 완료)
            		return shapeX[square];
            	}else { //다른 블록들
                	return shapeX[square];
            	}
            case 2:
            	if(type == 1) { //블록이 네모일 경우 //(회전변환 완료)
            		return -shapeY[square] - 1;
            	}else { //다른 블록들
                	return -shapeY[square];
            	}
            case 3:
            	if(type == 1) { //블록이 네모일 경우 //(회전변환 완료)
            		return -shapeX[square]-1;
            	}else { //다른 블록들
                	return -shapeX[square];
            	}
            default:
                return 0; // 발생하지 않아야 함
        }
    }
    
    

    
 // Add a getter for the type if needed
    public int getType() {
        return type;
    }
    
    public void paint(Color color) {
        int x, y;

        // 먼저 이전 위치의 블록을 지웁니다.
        for (int i = 0; i < shapeX.length; i++) {
            x = xPos + getRelativeX(i, orientation);
            y = yPos + getRelativeY(i, orientation);
            if (color == null) { // 만약 color가 null이면 지워야 할 경우입니다.
                board.setSquareImage(x, y, null);
            }
        }
        
        // 아이템 블록의 인덱스를 무작위로 선택합니다.
        // int itemBlockIndex = random.nextInt(shapeX.length);
        
        // 그리고 새 위치에 블록을 그립니다.
        for (int i = 0; i < shapeX.length; i++) {
            x = xPos + getRelativeX(i, orientation);
            y = yPos + getRelativeY(i, orientation);
            if (color != null) {
                // 지정된 블록이 아이템 블록이면 아이템 이미지를 그립니다.
                if (i == itemBlockIndex && hasItemBlock) {
                    board.setSquareImage(x, y, itemImage);
//                    //아이템이 어느 위치에 있는지에 대해 정보를 저장함,
//                    //paint(Color color)는 블럭이 '착지'할 때까지 그리는 함수이므로 위치가 영구 저장됨.
//                    itemX = x;
//                    itemY = y;
//                    System.out.println("item PosX : " + itemX + "item PosY : " + itemY);
                } else {
                    board.setSquareImage(x, y, blockImage);
                }
            }
        }
        
        
//                // 그리고 새 위치에 블록을 그립니다.
//        for (int i = 0; i < shapeX.length; i++) {
//            x = xPos + getRelativeX(i, orientation);
//            y = yPos + getRelativeY(i, orientation);
//            if (color != null) { // 색상이 null이 아니라면 새 블록을 그려야 할 경우입니다.
//                board.setSquareImage(x, y, blockImage);
//            }
//        }
    }
    
    public void setblock(SquareBoard2 bd2, Color color) { //조그만 squareboard2에 블럭을 갖다 놓는 역할
    	this.board2 = bd2;
    	
        int x, y;
                // 그리고 새 위치에 블록을 그립니다.
        for (int i = 0; i < shapeX.length; i++) {
            x = xPos + getRelativeX(i, orientation);
            y = yPos + getRelativeY(i, orientation);
            if (color != null) {
                // 지정된 블록이 아이템 블록이면 아이템 이미지를 그립니다.
                if (i == itemBlockIndex && hasItemBlock) {
                    board2.setSquareImage(x, y, itemImage);
                } else {
                	board2.setSquareImage(x, y, blockImage);
                }
            }
        }
    }
    
    public void clearBlock() {
        int x, y;
        for (int i = 0; i < shapeX.length; i++) {
            x = xPos + getRelativeX(i, orientation);
            y = yPos + getRelativeY(i, orientation);
                board.setSquareImage(x, y, null); // 고스트 블록 지우기
            
    }
    }
    
    private void paintNextBoard(Color color) {
        int x, y;

        // 먼저 이전 위치의 블록을 지웁니다.
        for (int i = 0; i < shapeX.length; i++) {
            x = xPos + getRelativeX(i, orientation);
            y = yPos + getRelativeY(i, orientation);
            if (color == null) { // 만약 color가 null이면 지워야 할 경우입니다.
                nextBoard.setSquareImage(x, y, null);
            }
        }
     // 그리고 새 위치에 블록을 그립니다.
        for (int i = 0; i < shapeX.length; i++) {
            x = xPos + getRelativeX(i, orientation);
            y = yPos + getRelativeY(i, orientation);
            if (color != null) {
                // 지정된 블록이 아이템 블록이면 아이템 이미지를 그립니다.
                if (i == itemBlockIndex && hasItemBlock) {
                    nextBoard.setSquareImage(x, y, itemImage);
                } else {
                	nextBoard.setSquareImage(x, y, blockImage);
                }
            }
        }
    }
    
    private void paintHoldBoard(Color color) {
        int x, y;

        // 먼저 이전 위치의 블록을 지웁니다.
        for (int i = 0; i < shapeX.length; i++) {
            x = xPos + getRelativeX(i, orientation);
            y = yPos + getRelativeY(i, orientation);
            if (color == null) { // 만약 color가 null이면 지워야 할 경우입니다.
                holdBoard.setSquareImage(x, y, null);
            }
        }
     // 그리고 새 위치에 블록을 그립니다.
        for (int i = 0; i < shapeX.length; i++) {
            x = xPos + getRelativeX(i, orientation);
            y = yPos + getRelativeY(i, orientation);
            if (color != null) {
                // 지정된 블록이 아이템 블록이면 아이템 이미지를 그립니다.
                if (i == itemBlockIndex && hasItemBlock) {
                    holdBoard.setSquareImage(x, y, itemImage);
                } else {
                	holdBoard.setSquareImage(x, y, blockImage);
                }
            }
        }
    }
    
    
    public void remakeGhost(int lines) {
    	clearGhost();
        // 고스트 블록을 현재 위치에 다시 만듭니다.
    	ghostYPos = ghostYPos - lines;
    	clearGhost();
        paintGhost(ghostYPos, false);
    }
    public void remakeGhostMinus(int lines) {
    	
    	clearGhost();
    	if(ghostYPos + 1 == 20){
    		paintGhost(ghostYPos, false); // 고스트 블록을 현재 위치에 다시 만듭니다.
    	}else if(ghostYPos + 2 == 20 && lines == 2) { //1줄 남았는데 2줄 지워버리면 고스트 잔상이 남는 문제가 있음.
    		ghostYPos = ghostYPos + 1;
    		paintGhost(ghostYPos, false); // 고스트 블록을 현재 위치에 다시 만듭니다.
    	}else {
    		ghostYPos = ghostYPos + lines;
    		paintGhost(ghostYPos, false); // 고스트 블록을 현재 위치에 다시 만듭니다.
    	}

    	

        
    }
    
    public void clearGhost() {
        // 고스트 블록의 현재 위치를 지웁니다.
        paintGhost(ghostYPos, true);
    }
    
    private void paintGhost(int ghostY) {
        paintGhost(ghostY, false);
    }
    private void paintGhost(int ghostY, boolean clear) {
    	
        int x, y;
        if(board!=null && !ghostLock) {
	        for (int i = 0; i < shapeX.length; i++) {
	            x = xPos + getRelativeX(i, orientation);
	            y = ghostY + getRelativeY(i, orientation);
	            if (clear) {
	                board.setGhostImage(x, y, null); // 고스트 블록 지우기
	            } else {
	            	if (i == itemBlockIndex && hasItemBlock) {
	                    board.setGhostImage(x, y, itemImage2);
	                } else {
	                	board.setGhostImage(x, y, blockImage2);
	                }
	            	
	            	//board.setGhostImage(x, y, blockImage2); // 고스트 블록 그리기
	            }
	        }
    	}
    }

    
    public void clearFigure() {
        // 일반 블록의 현재 위치를 지웁니다.
        clearFigures(true);
    }
    
    public void remakeFigure(int lines) {
        // 일반 블록의 현재 위치를 지웁니다.
    	clearFigures(true);
    	yPos = yPos - lines;
        clearFigures(false);
    }
    public void remakeFigureMinus(int lines) {
        // 일반 블록의 현재 위치를 지웁니다.
    	clearFigures(true);
    	yPos = yPos + lines;
        clearFigures(false);
    }
    
    public void clearFigures(boolean clear) {
        int x, y;
        if (board != null) {
	        // 먼저 이전 위치의 블록을 지웁니다.
	        for (int i = 0; i < shapeX.length; i++) {
	            x = xPos + getRelativeX(i, orientation);
	            y = yPos + getRelativeY(i, orientation);
	            if (clear) { // 만약 color가 null이면 지워야 할 경우입니다.
	                board.setSquareImage(x, y, null);
	            }else {
	            	// 지정된 블록이 아이템 블록이면 아이템 이미지를 그립니다.
	                if (i == itemBlockIndex && hasItemBlock) {
	                    board.setSquareImage(x, y, itemImage);
	                } else {
	                    board.setSquareImage(x, y, blockImage);
	                }
	            }
	         
	        }
        }
    }
    
    
    public int returnItemX() {
    	return itemX;
    }
    
    public int returnItemY() {
    	return itemY;
    }
    
    public int returnItemType() {
    	return itemType;
    }

    
    public int returnPosX() {
    	return xPos;
    }
    
    public int returnPosY() {
    	return yPos;
    }
    public void setPosX(int x) {
    	this.isOther = true;
    	xPos = x;
    }
    public void setPosY(int y) {
    	yPos = y;
    }
    
    
    public int returnOrientation() {
    	return orientation;
    }
    
    public void setOrientation(int o) {
    	orientation = o;
    }
    
    public boolean returnHasItemBlock() {
    	return hasItemBlock;
    }
    
    public int returnItemIndex() {
    	return itemBlockIndex;
    }
    
    public void setHasItemBlock(boolean value) { //set 이후에 호출되므로 능동적으로 조절 가능.
    	hasItemBlock = value;
    }
    
    public void setItemIndex(int index) { //set 이후에 호출되므로 능동적으로 조절 가능.
    	itemBlockIndex = index;
    }
    
    public void setGhostLock(boolean value) {
    	ghostLock = value;
    }
    
    public void setItemType(int type) {
    	
    	try {
        	//아이템 이미지 중 하나 선택
        	
    		itemType = type; //아이템 배열의 몇 번째 아이템을 저장 중인지 추적시작함
            String imgPath = itemImages.get(itemType);
            String imgPath2 = ghostItemImages.get(itemType);
            itemImage = ImageIO.read(new File(imgPath)); // 한 블럭당 생성되는 아이템의 종류는 1개.
            itemImage2 = ImageIO.read(new File(imgPath2)); // 한 블럭당 생성되는 아이템의 종류는 1개.
        } catch (IOException e) {
            e.printStackTrace();
            return; // 이미지 로드에 실패한 경우 함수를 종료합니다.
        }
    }

}