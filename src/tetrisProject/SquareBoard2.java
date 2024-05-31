package tetrisProject;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

// squareboard가 1인칭 컴포넌트라면, squareboard2는 타인의 진행 상황을 보여주는 미니 컴포넌트
public class SquareBoard2 extends Object {

	private final int width;
    private final int height;
    private BufferedImage[][] imageMatrix;
    private String message = null;
    private int removedLines = 0;
    private final SquareBoardComponent component;
    private Game game;
    public int combo = 0;
	private int clientNumber;
    public boolean isShortMessage = false;
    public boolean isVeryShortMessage = false;
    public boolean amIdead = false;
    private BufferedImage blockSquareImage;
    
    // 순위에 따른 이미지 파일 경로를 저장하는 HashMap 선언
    private HashMap<Integer, String> rankImages;

    public SquareBoard2(Game game, int width, int height, int clientNumber) {
        this.width = width;
        this.height = height;
        this.imageMatrix = new BufferedImage[height][width];
        this.component = new SquareBoardComponent();
        this.game = game;
        this.clientNumber = clientNumber;
        amIdead = false;
        
        rankImages = new HashMap<>();
        rankImages.put(1, "src/images/ranks1.png");
        rankImages.put(2, "src/images/ranks2.png");
        rankImages.put(3, "src/images/ranks3.png");
        rankImages.put(4, "src/images/ranks4.png");
        rankImages.put(5, "src/images/ranks5.png");
        rankImages.put(6, "src/images/ranks6.png");
        
        try {
            blockSquareImage = ImageIO.read(new File("src/images/blocksquare.png"));
        } catch (IOException e) {
            e.printStackTrace();
            // 로드 실패 시 처리를 결정합니다.
            blockSquareImage = null;
        }
        
        clear();
    }

    public boolean isSquareEmpty(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return x >= 0 && x < width && y < 0;
        } else {
            return imageMatrix[y][x] == null;
        }
    }

    public boolean isLineEmpty(int y) {
        if (y < 0 || y >= height) {
            return false;
        }
        for (int x = 0; x < width; x++) {
            if (imageMatrix[y][x] != null) {
                return false;
            }
        }
        return true;
    }

    public boolean isLineFull(int y) {
        if (y < 0 || y >= height) {
            return true;
        }
        for (int x = 0; x < width; x++) {
            if (imageMatrix[y][x] == null) {
                return false;
            }
        }
        return true;
    }

    public boolean hasFullLines() {
        for (int y = height - 1; y >= 0; y--) {
            if (isLineFull(y)) {
                return true;
            }
        }
        combo = 0; //블록 놨는데 지운 거 없으면 콤보 초기화
        return false;
    }
    
    public Component getComponent() {
        return component;
    }

    public int getBoardHeight() {
        return height;
    }

    public int getBoardWidth() {
        return width;
    }

    public int getRemovedLines() {
        return removedLines;
    }
    
    
   
    public void setSquareImage(int x, int y, BufferedImage image) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return;
        }
        imageMatrix[y][x] = image;
        if (component != null) {
            component.invalidateSquare(x, y);
            component.repaint();
        }
    }
    
    public void coverWithGray() {
        
        
        // 게임 보드의 각 셀을 순회합니다.
        for (int x = 0; x < this.getBoardWidth(); x++) {
            for (int y = 0; y < this.getBoardHeight(); y++) {
                // 배경이 아닌 경우에만 회색 블록으로 덮습니다.
                if (!this.isSquareEmpty(x, y)) {
                    this.setSquareImage(x, y, blockSquareImage);
                }
            }
        }
        this.update(); // 보드를 업데이트하여 변경 사항을 화면에 반영합니다.
    }
    
 // 순위 이미지를 로드하고 표시하는 메서드
    public void showRankImage(int rank) {
        String imagePath = rankImages.get(rank);
        if (imagePath == null) {
            // 이미지 경로가 존재하지 않으면 함수 종료
            return;
        }
        try {
            BufferedImage rankImage = ImageIO.read(new File(imagePath));
            Graphics g = getComponent().getGraphics();
            // 이미지를 반투명으로 만들기 위해 AlphaComposite 사용
            Graphics2D g2d = (Graphics2D) g;
            AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f); // 50% 투명도
            g2d.setComposite(ac);
            g2d.drawImage(rankImage, -2, 0, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void setMessage(String message) {
        this.message = message;
        if (component != null) {
            component.redrawAll();
        }
    }

    public void clear() {
        removedLines = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                this.imageMatrix[y][x] = null;
            }
            
        }
        if (component != null) {
            component.redrawAll();
        }
    }


    
    public void removeFullLines() {
        boolean repaint = false;
        boolean specialsfx = false;
        int linenumber = 0;
        for (int y = height - 1; y >= 0; y--) {
            if (isLineFull(y)) {
                removeLine(y);
                linenumber++;
                removedLines++;
                
                repaint = true;
                y++;
            }
        }
        
        if(linenumber>0 ) {
        	combo++;
        	//System.out.println("combo = " + combo);
        	//System.out.println("lineNum = " + linenumber);
        	//System.out.println("isSpecial" + specialsfx);
        	
        }
       
        
        

        if (repaint && component != null) {
            component.redrawAll();
        }
    }

    private void removeLine(int y) {
        for (; y > 0; y--) { // Start from the cleared line and move upwards.
            for (int x = 0; x < width; x++) {
                imageMatrix[y][x] = imageMatrix[y - 1][x]; // Move each block one line down.
            }
        }
        for (int x = 0; x < width; x++) {
            imageMatrix[0][x] = null; // Clear the top line after moving everything down.
        }
    }


    private void forcedremoveLine(int lines) { //아이템 구현할 시간이 남을 때 구현 시작
    	for(int k=0;k<lines;k++) {
	    	
		    	for (int y = height-1; y > 0; y--) { // Start from the cleared line and move upwards.
		            for (int x = 0; x < width; x++) {
		                imageMatrix[y][x] = imageMatrix[y - 1][x];
		            }
		        }
		    	
		        for (int x = 0; x < width; x++) {
		        	
		            imageMatrix[0][x] = null; // Clear the top line after moving everything down.
		        }
		        
		        
		        
		        if (component != null) {
		            component.redrawAll();
		            component.repaint();
		            update();
		        }
	    	
    	}
    }
    	
    public void cut() {
	    		for (int y = height-1; y > 0; y--) { // Start from the cleared line and move upwards.
		            for (int x = 0; x < width; x++) {
		            	BufferedImage currentBlockImage = imageMatrix[y][x];
	                    
	                    // 현재 블록이 blockSquareImage와 동일한지 확인합니다.
	                    if (currentBlockImage != null && !currentBlockImage.equals(blockSquareImage)) {
	                    	System.out.println("is equal");
	                        // blockSquareImage와 동일하지 않다면 해당 블록을 제거합니다.
	                        imageMatrix[y][x] = null;
	                    }
		            }
		            
		            
		        }
		    	
		        for (int x = 0; x < width; x++) {
		        	
		            imageMatrix[0][x] = null; // Clear the top line after moving everything down.
		        }		    	
		        
		        if (component != null) {
		            component.redrawAll();
		            component.repaint();
		            update();
		        }
	    	
    	
    }
    
    
public void addGarbageLine(int lines, int randomHole) {
    	
    	
        for(int i=0; i<lines; i++) {
	        for (int y = 0; y < height - 1; y++) { // Move each block one line up, except the top line.
	            for (int x = 0; x < width; x++) {
	                imageMatrix[y][x] = imageMatrix[y + 1][x];
	            }
	        }
	
	        
	        
	        for (int x = 0; x < width; x++) {
	            if (x == randomHole) {
	                imageMatrix[height - 1][x] = null; // Leave an empty space for gameplay continuity.
	            } else {
	                imageMatrix[height - 1][x] = blockSquareImage; // Assign a garbage block image.
	            }
	        }
        }
        
        
        if (component != null) {
            component.redrawAll();
        }
        
    }


	public void Plus1 (int randomNum) {
		addGarbageLine(1, randomNum);
	}
	
	public void Plus2(int randomNum) {
		addGarbageLine(2, randomNum);
	}
	
	public void SpeedUp() {
		//game 내에서 구현 가능할 듯 
	}
	
	public void Minus1() {
		forcedremoveLine(1);
	}
	
	public void Minus2() {
		forcedremoveLine(2);
	}
	
	public void Cut() {
		
	}
	
	public void Clear() {
		
	}

    
    public void update() {
    	component.redrawAll(); 
        component.redraw();
    }

    private class SquareBoardComponent extends JComponent {

        /**
         * The component size. If the component has been resized, that
         * will be detected when the paint method executes. If this
         * value is set to null, the component dimensions are unknown.
         */
        private Dimension size = null;

        private Insets insets = new Insets(0, 0, 0, 0);

        private Dimension squareSize = new Dimension(0, 0);

        private Image bufferImage = null;
        
        private Image backgroundImage;

        private Rectangle bufferRect = new Rectangle();

        private Color messageColor = Color.white;

        private boolean updated = true;

        private Rectangle updateRect = new Rectangle();

        
        
        public SquareBoardComponent() {
        	
        	backgroundImage = null;
        	// 배경을 투명하게 만듭니다.
        	setOpaque(false);
            
        }

        public void invalidateSquare(int x, int y) {
            if (updated) {
                updated = false;
                updateRect.x = x;
                updateRect.y = y;
                updateRect.width = 0;
                updateRect.height = 0;
            } else {
                if (x < updateRect.x) {
                    updateRect.width += updateRect.x - x;
                    updateRect.x = x;
                } else if (x > updateRect.x + updateRect.width) {
                    updateRect.width = x - updateRect.x;


                }
                if (y < updateRect.y) {
                    updateRect.height += updateRect.y - y;
                    updateRect.y = y;
                } else if (y > updateRect.y + updateRect.height) {
                    updateRect.height = y - updateRect.y;
                }
            }
        }


        public void redraw() {
            Graphics g;

            if (!updated) {
                updated = true;
                g = getGraphics();
                if (g == null) return;
                g.setClip(insets.left + updateRect.x * squareSize.width,
                        insets.top + updateRect.y * squareSize.height,
                        (updateRect.width + 1) * squareSize.width,
                        (updateRect.height + 1) * squareSize.height);
                paint(g);
            }
        }


        public void redrawAll() {
            Graphics g;

            updated = true;
            g = getGraphics();
            if (g == null) return;
            g.setClip(insets.left,
                    insets.top,
                    width * squareSize.width,
                    height * squareSize.height);
            paint(g);
        }

        public boolean isDoubleBuffered() {
            return true;
        }

        public Dimension getPreferredSize() {
            return new Dimension(width * 8, height * 8); // 단위 유닛 블록 크기 설정
        }

        public Dimension getMinimumSize() {
            return getPreferredSize();
        }


        public Dimension getMaximumSize() {
            return getPreferredSize();
        }

        public synchronized void paint(Graphics g) {
            Graphics bufferGraphics;
            Rectangle rect;

            MediaTracker tracker = new MediaTracker(this);
            tracker.addImage(backgroundImage, 0);
            try {
                tracker.waitForAll();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            g.drawImage(backgroundImage, 0, 0, this.getWidth(), this.getHeight(), this);
            
            if (size == null || !size.equals(getSize()) || bufferImage == null) {
                size = getSize();
                squareSize.width = size.width / width;
                squareSize.height = (size.height - (squareSize.height / 2)) / height;
                

                //if (squareSize.width <= squareSize.height) {
                //} else {
                //}

                insets.left = (size.width - width * squareSize.width) / 2;
                insets.right = insets.left;
                insets.top = 0;
                insets.bottom = size.height - height * squareSize.height ;
                
                
                
                bufferImage = createImage(width * squareSize.width,
                        height * squareSize.height);
            }
            
            

            rect = g.getClipBounds();
            bufferGraphics = bufferImage.getGraphics();
            bufferGraphics.setClip(rect.x - insets.left,
                    rect.y - insets.top,
                    rect.width,
                    rect.height);
            doPaintComponent(bufferGraphics);

            g.drawImage(bufferImage, insets.left, insets.top, getBackground(), null);
        }

        private void doPaintComponent(Graphics g) {
        	
        	super.paintComponent(g);

            g.fillRect(0, 0, width * squareSize.width, height * squareSize.height);

            
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (imageMatrix[y][x] != null) {
                        paintSquare(g, x, y);
                    }
                }
            }
            
            

            if (message != null) {
                paintMessage(g, message);
            }
        }

        private Dimension calculateSquareSize() {
            return new Dimension(getWidth() / width, getHeight() / height);
        }

        private void paintSquare(Graphics g, int x, int y) {
            BufferedImage image = imageMatrix[y][x];
            int xMin = x * squareSize.width;
            int yMin = y * squareSize.height;

            g.drawImage(image, xMin, yMin, squareSize.width, squareSize.height, this);
        }
        

        private void paintMessage(Graphics g, String msg) {
            int fontWidth;
            int offset;
            int x;
            int y;
            
            int timer = 2000000000;
            float alpha = 1f;
            
            
            if(isShortMessage) {
            	timer = 2000;
            	alpha = 0.5f;
            }
            
            if(isVeryShortMessage) {
            	timer = 700;
            	alpha = 0.5f;
            }

            g.setFont(new Font("SansSerif", Font.BOLD, squareSize.width + 4));
            fontWidth = g.getFontMetrics().stringWidth(msg);

            x = (width * squareSize.width - fontWidth) / 2;
            y = height * squareSize.height / 2;

            offset = squareSize.width / 10;
            g.setColor(Color.black);
            g.drawString(msg, x - offset, y - offset);
            g.drawString(msg, x - offset, y);
            g.drawString(msg, x - offset, y - offset);
            g.drawString(msg, x, y - offset);
            g.drawString(msg, x, y + offset);
            g.drawString(msg, x + offset, y - offset);
            g.drawString(msg, x + offset, y);
            g.drawString(msg, x + offset, y + offset);

            g.setColor(messageColor);
            g.drawString(msg, x, y);
            
         // 메시지 표시 타이머 설정
            new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        // 1초 후에 실행되는 코드
                        // 메시지를 숨기거나 다시 그리기를 통해 메시지를 제거해야 함
                        message = null;
                        repaint();
                        isShortMessage = false;
                        isVeryShortMessage = false;
                    }
                },
                timer // 메시지를 표시할 시간 (밀리초 단위)
            );
            
            
        }
    }
}