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

import javax.swing.JComponent;


public class HoldBoard extends Object {

	private final int width;
    private final int height;
    private BufferedImage[][] holdMatrix;
    private final HoldBoardComponent component;
    private Game game;
    public int combo = 0;

    public HoldBoard(Game game, int width, int height) {
        this.width = width;
        this.height = height;
        this.holdMatrix = new BufferedImage[height][width];
        this.component = new HoldBoardComponent();
        this.game = game;
        clear();
    }

    public boolean isSquareEmpty(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return x >= 0 && x < width && y < 0;
        } else {
            return holdMatrix[y][x] == null;
        }
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

   
    public void setSquareImage(int x, int y, BufferedImage image) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return;
        }
        holdMatrix[y][x] = image;
        if (component != null) {
            component.invalidateSquare(x, y);
            component.repaint();
        }
    }

    public void clear() {
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                this.holdMatrix[y][x] = null;
            }
        }
        if (component != null) {
            component.redrawAll();
        }
    }

    public void update() {
    	component.redrawAll(); 
        component.redraw();
    }

    private class HoldBoardComponent extends JComponent {

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

        private boolean updated = true;

        private Rectangle updateRect = new Rectangle();

        
        
        public HoldBoardComponent() {
        	
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
            return new Dimension(width * 12, height * 12); // 단위 유닛 블록 크기 설정
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
        	g.setColor(new Color(16, 25, 41));
            g.fillRect(0, 0, width * squareSize.width, height * squareSize.height);

            
            
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (holdMatrix[y][x] != null) {
                        paintSquare(g, x, y);
                    }
                }
            }
          
        }

        private Dimension calculateSquareSize() {
            return new Dimension(getWidth() / width, getHeight() / height);
        }

        private void paintSquare(Graphics g, int x, int y) {
            BufferedImage image = holdMatrix[y][x];
            int xMin = x * squareSize.width;
            int yMin = y * squareSize.height;

            g.drawImage(image, xMin, yMin, squareSize.width, squareSize.height, this);
        }

}
}