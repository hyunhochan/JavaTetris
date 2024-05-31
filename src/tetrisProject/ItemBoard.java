package tetrisProject;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.MediaTracker;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import javax.imageio.ImageIO;
import javax.swing.JComponent;

// 현재 획득한 아이템의 상황을 보여주는 미니 컴포넌트
public class ItemBoard extends JComponent {

	private final int width;
    private final int height;
    private final ItemBoardComponent component;
    private Game game;
    public int combo = 0;
	private int clientNumber;
    public boolean isShortMessage = false;
    public boolean isVeryShortMessage = false;
    private Queue<BufferedImage> itemQueue = new LinkedList<>();
    private Queue<Integer> itemTypeQueue = new LinkedList<>();
    private int itemWidth; // 아이템 이미지의 너비
    private int itemHeight; // 아이템 이미지의 높이
    private int maxItems; // 최대 표시 가능한 아이템 수
    
 // 아이템 타입별 이미지를 저장하는 해시맵
    private HashMap<Integer, BufferedImage> itemImages = new HashMap<>(); 
    

    public ItemBoard(Game game, int width, int height, int maxItems) {
    	
    	this.itemWidth = 13;
        this.itemHeight = 13;
        this.maxItems = maxItems;
    	
        this.width = width;
        this.height = height;
        this.component = new ItemBoardComponent();
        this.game = game;
        
        this.maxItems = maxItems;
        loadItemImages(); // 아이템 이미지를 로드합니다.
        
        
        //아이템 표시 테스트 코드
//        addItem(2);
//        addItem(7);
//        addItem(1);
    }
    

    
    
    // 아이템 이미지를 로드하는 메서드
    private void loadItemImages() {
        try {
            // 여기에 각 아이템 타입별로 이미지를 로드하는 코드 추가
            // 예제로 몇 가지 아이템을 로드하고 있습니다.
        	itemImages.put(1, ImageIO.read(new File("src/items/plus.png")));
            itemImages.put(2, ImageIO.read(new File("src/items/plus2.png")));
            itemImages.put(3, ImageIO.read(new File("src/items/speedup.png")));
            itemImages.put(4, ImageIO.read(new File("src/items/minus.png")));
            itemImages.put(5, ImageIO.read(new File("src/items/minus2.png")));
            itemImages.put(6, ImageIO.read(new File("src/items/cut.png")));
            itemImages.put(7, ImageIO.read(new File("src/items/clear.png")));
            // 나머지 아이템들도 비슷한 방식으로 로드
            // ...

            // 아이템 이미지의 너비와 높이 설정
            // 모든 아이템 이미지의 크기가 동일하다고 가정합니다.
            if (!itemImages.isEmpty()) {
                BufferedImage firstImage = itemImages.values().iterator().next();
                itemWidth = 13;
                itemHeight = 14;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addItem(int itemType) {
        BufferedImage itemImage = itemImages.get(itemType); // 여기에서 itemType에 해당하는 이미지를 가져옵니다.
        if (itemImage != null) {
            if (itemQueue.size() >= maxItems) {
                itemQueue.poll(); // 맨 아래의 아이템을 제거합니다.
                itemTypeQueue.poll();
            }
            itemQueue.offer(itemImage); // 맨 위에 새로운 아이템을 추가합니다.
            itemTypeQueue.offer(itemType);
            component.repaint(); // 컴포넌트를 다시 그립니다.
            repaint(); // 컴포넌트를 다시 그립니다.
        }
    }
    
    public int useItem() {
		itemQueue.poll(); // 맨 아래의 아이템을 제거합니다.
        int itemtype = itemTypeQueue.poll();
		//아이템 사용 로직
		
        component.repaint(); // 컴포넌트를 다시 그립니다.
        repaint(); // 컴포넌트를 다시 그립니다.
        return itemtype;
    }
    
    public void clearItems() {
    	itemQueue.clear();
    	itemTypeQueue.clear();
    	component.repaint(); // 컴포넌트를 다시 그립니다.
            repaint(); // 컴포넌트를 다시 그립니다.
        
    }
    
    public void rotateItems() {
    	
    	BufferedImage tempImage = itemQueue.poll();
    	int temp = itemTypeQueue.poll();
    	
    	itemQueue.offer(tempImage); // 맨 위에 새로운 아이템을 추가합니다.
        itemTypeQueue.offer(temp);
        component.repaint(); // 컴포넌트를 다시 그립니다.
        repaint(); // 컴포넌트를 다시 그립니다.
    	
    }
    
    public boolean isEmpty() {
    	return itemQueue.isEmpty();
    }

    // 이 메서드는 JComponent의 paintComponent를 오버라이드합니다.
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int y = 0;
        for (BufferedImage item : itemQueue) {
            g.drawImage(item, 0, y, null);
            System.out.println("wrote");
            y += itemHeight;
        }
    }


    public Component getComponent() {
        return component;
    }

    private class ItemBoardComponent extends JComponent {

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

    
        
        public ItemBoardComponent() {
        	
        	backgroundImage = null;
        	// 배경을 투명하게 만듭니다.
        	setOpaque(false);
            
        }
        public boolean isDoubleBuffered() {
            return true;
        }

        public Dimension getPreferredSize() {
            return new Dimension(width * 20, height * 20); // 단위 유닛 블록 크기 설정
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
                insets.bottom = size.height - height * squareSize.height;
                
                
                
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
        	g.setColor(new Color(16, 25, 41)); //16, 25, 41
            g.fillRect(0, 0, width * squareSize.width, height * squareSize.height);
            
            int y = 350;
            for (BufferedImage item : itemQueue) {
                g.drawImage(item, 0, y, itemWidth, itemHeight, this);
                System.out.println("wrote");
                y -= itemHeight;
            }
            
            
            
          
        }

        


     
    }
}