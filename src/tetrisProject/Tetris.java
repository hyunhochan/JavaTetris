package tetrisProject;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicScrollBarUI;


public class Tetris {
	
	final static int ServerPort = 5123;
	public JTextField textField;
	public JTextArea textAreaUp;
	public JTextArea textAreaDown;
	DataInputStream is;
	DataOutputStream os;
	private Game game;
	public int myNumber;
	public boolean hasGivenNum;
	public int totalPlayers = 0;
	public int totalReady = 0;
	public int[] arrangeOther;
	public int slotnum = 0;
	public int playernum = 0;
	public boolean isexed = false;
	private MyFrame f;
	
	private final Object messageLock = new Object();
	//여기서, }else if(msg.contains("removed")) {쪽을 처리하는 도중 서버에서 }else if(msg.contains("OVER")) {와 관련된 내용이 도착해 버릴 경우,
	//removed 관련 로직을 내 화면에 업데이트 하기도 전에 handleOthersOverStatus(msg)가 실행되어 버리는 문제가 있음. 뮤텍스가 필요함.
	
	private final Object garbageLock = new Object();
	
	// 안착된 블럭이 반영되는 것과, 그 사이에 쓰레기 블록이 추가되는 것 사이에도 뮤텍스가 필요함.
	
	public Tetris() throws IOException {
		
		game = new Game(this);
		f = new MyFrame(game);
		InetAddress ip = InetAddress.getByName("localhost");
//		Socket s = new Socket("000.000.0.0", ServerPort);
		Socket s = new Socket(ip, ServerPort);
		is = new DataInputStream(s.getInputStream());
		os = new DataOutputStream(s.getOutputStream());
		arrangeOther = new int[5];
        

		Thread thread2 = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						
						String msg = is.readUTF();
						//System.out.println("the message : " +msg);
						
						
						if ("START_GAME".equals(msg)) {
						    // 서버로부터 게임 시작 메시지를 받았을 때 game.start()를 호출합니다.
						    
							f.textAreaUp.append(new String(msg) + "\n");
						    // Ensure that we get the text length from textAreaUp, not textAreaDown
						    int pos = f.textAreaUp.getText().length();
						    // The invokeLater ensures that the caret is set after the GUI has been updated
						    SwingUtilities.invokeLater(new Runnable() {
						        @Override
						        public void run() {
						            // Check if the position is not out of bounds
						            if (pos <= f.textAreaUp.getDocument().getLength()) {
						                f.textAreaUp.setCaretPosition(pos);
						            }
						        }
						    });
							f.startGameComponents();
							
							
						}else if (msg.contains("landed")) {
							synchronized (messageLock) {
							
						        // 메시지 파싱: 예상 메시지 형식 "client 0 landed : x,y,type,orientation"
						        String[] parts = msg.split(":")[1].trim().split(",");
						        int clientNumber = Integer.parseInt(parts[0].trim());
						        int x = Integer.parseInt(parts[1].trim());
						        int y = Integer.parseInt(parts[2].trim());
						        int type = Integer.parseInt(parts[3].trim());
						        int orientation = Integer.parseInt(parts[4].trim());
						        boolean hasItemBlock = Boolean.parseBoolean(parts[5].trim());
						        int itemIndex = Integer.parseInt(parts[6].trim());
						        int itemType = Integer.parseInt(parts[7].trim());
						        //System.out.println("myclientnumber : " + this.myNumber + "senderNum" + clientNumber + "x" + x + "y" + y + "type" + type + "orientation" + orientation);
						        if(clientNumber != myNumber) {
						        	synchronized (garbageLock) {
								        game.makeFigure(clientNumber, x, y, type, orientation, hasItemBlock, itemIndex, itemType);
								        game.handleotherFigureLanded(clientNumber);
						        	}
						        }else {
						        	
						        }
							}
							
						 }else if (msg.contains("YourNumber")) { 
							 synchronized (messageLock) {
	                        	//System.out.println("yernum");
	                        	if(hasGivenNum == false) {
		                        	String numberStr = msg.substring("YourNumber:".length());
		                            myNumber = Integer.parseInt(numberStr);
		                            game.setClientNum(myNumber); //game frame에서 끌어다 쓴다고 선언을 frame에서 tetris로 옮겨 버려서 오류가 생겼던 것. 한번 업데이트 해 주자. 
		                            totalPlayers = Integer.parseInt(numberStr); 
		                            hasGivenNum = true;
		                            f.setVisibleMyName();
		                            
		                            //내가 3번째로 들어왔으면 yournumber는 2,
		                            //밑에 루프에서는 i = 0, 1, 2
		                            //arrangeOther[0] = 0, arrangeOther[1] = 1
		                            
		                            
	    				 	       if(myNumber >1) { //들어왔는데 내가 첫 번째가 아니라면?
	    				 	    	  //System.out.println("yernum2");
	    				 	    	  
	    				 	    	  
	    				 	    	  
	    				 	    	   	int index = 0; //배열 정립에 사용할 인덱스
	    				 	        	for (int i = 1; i < totalPlayers+1; i++) { //배열 재정립
		    				 	            if (i != myNumber) {
		    				 	            	arrangeOther[index] = i; //myNum 6이면 0-1 1-2 2-3 3-4 4-5
		    				 	            	index++;
		    				 	            } //(왼쪽의 otherBoard는 5칸이고, 나를 제외한 5명의 번호가 오름차 순서대로 가야 함.)
	    				 	        	}
	    				 	    	   
		                            	for(int j = 0; j<myNumber-1; j++) { //내 번호가 3이라면 2까지.
		                            		f.addPlayer(j); //먼저 들어온놈들 후딱 추가하기.
		                            		slotnum++;
		                            	}
	    				 	        	
	    				 	       }
		                            
	                        	}else { //내가 들어온 이후 인원 유입이 있을 경우
	                        		
	                        		//System.out.println("새 유입");
	                        		String numberStr = msg.substring("YourNumber:".length());
		                            totalPlayers = Integer.parseInt(numberStr); //새로 들어온 놈의 번호
		                            
	                        		
		                            int index = 0; //배열 정립에 사용할 인덱스
	    				 	        for (int i = 1; i < totalPlayers+1; i++) { //1명씩 들어올 때마다 배열 재정립, i는 123456
	    				 	            if (i != myNumber) { //i가 내 번호(이를테면 1)과 같으면 i와 index를 1 늘린다.
	    				 	            	arrangeOther[index] = i;
	    				 	            	//System.out.print(arrangeOther[index]);
	    				 	            	index++;
	    				 	            	
	    				 	            }
	    				 	        }
	    				 	        
	    				 	       f.addPlayer(slotnum); //새로 들어온 놈 후딱 추가하기.
	    				 	       slotnum++;
	    				 	       //System.out.println("새 추가");
	                        	}
							 }
                        	
                        	
						}else if(msg.contains("Total Ready Count")) {
							
							String temp = msg.substring(36).trim();
					        totalReady = Integer.parseInt(temp);
					        game.setReadyCount(totalReady); //game frame에서 끌어다 쓴다고 선언을 frame에서 tetris로 옮겨 버려서 오류가 생겼던 것. 한번 업데이트 해 주자.
					        for(int i=0; i<5; i++ ) {
					        	System.out.println(arrangeOther[i]);
					        }
					        //System.out.println("totalReady : " + totalReady);
							
						}else if(msg.contains("Server Accepted")) {
						
							String temp = msg.substring(19).trim();
					        totalPlayers = Integer.parseInt(temp);
					        //System.out.println(totalPlayers);
							
						}else if(msg.contains("OVER")) {
							synchronized (messageLock) {
						
								handleOthersOverStatus(msg);
							}
							
						}else if(msg.contains("FINISH")) {
							synchronized (messageLock) {
							//모든 게임이 종료되었을 때 실행할 코드를 입력하기...
							System.out.println("GAME ALL OVER");
							
							//게임 끝나고 굳는 효과 주려는 코드
						    long startTime = System.currentTimeMillis(); // 시작 시간을 기록
					        long elapsedTime = 0L;
					        //BgmPlayer.playsfx("src/sounds/finishready.wav");
					        while (elapsedTime < 4000) { // 4초간 딜레이. (순위를 확인하게 하기 위함)
					            elapsedTime = System.currentTimeMillis() - startTime;
					        }
							
							f.gameAllOverHandler(); //4초 후 버튼 다시 띄우기.
						}
						}else if(msg.contains("removed")) {
							synchronized (messageLock) {
								synchronized (garbageLock) {
								
						
								//참고 : 어차피 figure 컨텍스트에서 board가 null이라는 것은 블럭을 놓고 다음 블럭을 생성하기 전 공백이라는 말이므로,
						 		//figure 및 ghost를 무시하고 repaint를 진행해도 된다! if (board!=null) 할 필요 x.
						 		System.out.println(msg);
						 		String[] parts = msg.substring(8).split(",");
						 		String trashValue = parts[0].trim();
						 		int clientNumber = Integer.parseInt(parts[1].trim());
						 		int removedLines = Integer.parseInt(parts[2].trim()); //상대가 지웠던 줄 수이지만 내가 "추가할 쓰레기 블록 개수"로 바꿀 예쩡.
						 		int combo = Integer.parseInt(parts[3].trim());
						 		int randomHole = Integer.parseInt(parts[4].trim());
						 		
						 		
						 		int finalLines = setFinalLines(removedLines,  combo,  clientNumber);
						 		// 지운 줄과 콤보에 따라 생겨날 패널티(쓰레기)블록의 라인 수를 여기서 미리 구하고 들어가자.
								
						 		
						 		
							    // Ensure that we get the text length from textAreaUp, not textAreaDown
							    int pos = f.textAreaUp.getText().length();
							    // The invokeLater ensures that the caret is set after the GUI has been updated
							    SwingUtilities.invokeLater(new Runnable() {
							        @Override
							        public void run() {
							            // Check if the position is not out of bounds
							            if (pos <= f.textAreaUp.getDocument().getLength()) {
							                f.textAreaUp.setCaretPosition(pos);
							            }
							        }
							    });
							    
							    
								
							    
							  //System.out.println("clientNumber" + clientNumber + "removedLines" + removedLines);
						 		if(clientNumber != myNumber) { //딴놈이 지운거
						 			game.addLines(finalLines, randomHole); //내 줄을 늘립니다.
						 			game.addOtherLines(finalLines, randomHole, clientNumber); // 딴놈이 지웠어도 내 화면에서 당사자 제외 다른 놈들 줄을 늘립니다.
						 			if(finalLines > 0) {
						 				BgmPlayer.playsfx("src/sounds/sfx_lineattack.wav");
						 			}
						 		}else { //내가 지운거
//						 			 game.addOtherLines(removedLines, randomHole); // 화면에 있는 딴놈들 줄을 늘립니다.
						 			//이걸 굳이 서버에서 구현해 가져올 필요가 있음? gmae에서 randomHole을 미리 만들고 서버로 직접 보내는 게 화면 업데이트 최적화에 좋을 것.
						 		}
								
								//handleOthersRemovedStatus(msg);
							    
						 		}
							}
							
						}else if(msg.contains("gotItem")) {
							synchronized (messageLock) {
							
					 		System.out.println(msg);
					 		String[] parts = msg.substring(8).split(",");
					 		String trashValue = parts[0].trim();
					 		int clientNumber = Integer.parseInt(parts[1].trim());
					 		int itemType = Integer.parseInt(parts[2].trim()); 
					 		
					 		if(clientNumber != myNumber) { //딴놈이 지운거
					 			
					 				game.collectItem2(clientNumber, itemType);
					 			
					 		}
						}
							
						}else if(msg.contains("rotate")) {
							synchronized (messageLock) {
								
						 		System.out.println(msg);
						 		String[] parts = msg.substring(8).split(",");
			                    int clientNumber = Integer.parseInt(parts[1].trim());
						 		
						 		if(clientNumber != myNumber) { //딴놈이 지운거
						 			
						 				game.rotateOther(clientNumber);
						 			
						 		}
							}
								
							
						}else if(msg.contains("usedItem")) {
							synchronized (messageLock) {
								
							
					 		System.out.println(msg);
					 		String[] parts = msg.substring(8).split(",");
					 		String trashValue = parts[0].trim();
					 		int clientNumber = Integer.parseInt(parts[1].trim());
					 		int itemType = Integer.parseInt(parts[2].trim());
					 		int calledNumber = Integer.parseInt(parts[3].trim()); 
					 		int randomNum = Integer.parseInt(parts[4].trim());
					 		
					 		if(calledNumber != myNumber) { //아이템의 효과가 적용되는 대상이 내가 아니라면, '내가 사용'했어도 적용됨
					 			
				 				game.useOtherItem(clientNumber, calledNumber, itemType, randomNum);
				 				
				 				switch(itemType) {
					 				case 1:
					 					f.textAreaUp.append("client " + clientNumber + " => " + "client " + calledNumber + "\n" + "플러스 (1줄)" + "\n");
					 					break;
					 				case 2:
					 					f.textAreaUp.append("client " + clientNumber + " => " + "client " + calledNumber + "\n" + "플러스 (2줄)" + "\n");
					 					break;
					 				case 3:
					 					f.textAreaUp.append("client " + clientNumber + " => " + "client " + calledNumber + "\n" + "스피드 업(5초)" + "\n");
					 					break;
					 				case 4:
					 					f.textAreaUp.append("client " + clientNumber + " => " + "client " + calledNumber + "\n" + "마이너스 (1줄)" + "\n");
					 					break;
					 				case 5:
					 					f.textAreaUp.append("client " + clientNumber + " => " + "client " + calledNumber + "\n" + "마이너스 (2줄)" + "\n");
					 					break;
					 				case 6:
					 					f.textAreaUp.append("client " + clientNumber + " => " + "client " + calledNumber + "\n" + "커트" + "\n");
					 					break;
					 				case 7:
					 					f.textAreaUp.append("client " + clientNumber + " => " + "client " + calledNumber + "\n" + "클리어" + "\n");
					 					break;
					 					
					 					default:
					 						break;				 					
				 				}
				 				int pos = f.textAreaUp.getText().length();
								f.textAreaUp.setCaretPosition(pos); // caret 포지션을 가장 마지막으로 이동
					 			
					 		}else { //아이템의 효과가 적용되는 대상이 나라면, '내가 사용'했어도 적용됨
					 			
					 			game.useItem(clientNumber, itemType, randomNum);
					 			
					 			switch(itemType) {
					 			case 1:					 			
					 				f.textAreaUp.append("client " + clientNumber + " => " + "client " + myNumber + "\n" + "플러스 (1줄)" + "\n");
					 				break;
					 			case 2:					 			
					 				f.textAreaUp.append("client " + clientNumber + " => " + "client " + myNumber + "\n" + "플러스 (2줄)" + "\n");
					 				break;
					 			case 3:					 			
					 				f.textAreaUp.append("client " + clientNumber + " => " + "client " + myNumber + "\n" + "스피드 업(5초)" + "\n");
					 				break;
					 			case 4:					 			
					 				f.textAreaUp.append("client " + clientNumber + " => " + "client " + myNumber + "\n" + "마이너스 (1줄))" + "\n");
					 				break;
					 			case 5:					 			
					 				f.textAreaUp.append("client " + clientNumber + " => " + "client " + myNumber + "\n" + "마이너스 (2줄)" + "\n");
					 				break;
					 			case 6:					 			
					 				f.textAreaUp.append("client " + clientNumber + " => " + "client " + myNumber + "\n" + "커트" + "\n");
					 				break;
					 			case 7:					 			
					 				f.textAreaUp.append("client " + clientNumber + " => " + "client " + myNumber + "\n" + "클리어" + "\n");
					 				break;
					 				default:
					 					break;
					 			}
					 			
					 			int pos = f.textAreaUp.getText().length();
								f.textAreaUp.setCaretPosition(pos); // caret 포지션을 가장 마지막으로 이동
					 			
					 		}
						}
							
						}else {
						
							f.textAreaDown.append(new String(msg) + "\n");
							int pos = f.textAreaDown.getText().length();
							f.textAreaDown.setCaretPosition(pos); // caret 포지션을 가장 마지막으로 이동
						}
						// 받은 패킷을 텍스트 영역에 표시한다.
						if(!msg.contains("YourNumber")) {
							System.out.println(msg);
						}
						
						} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});
		thread2.start();
	}
	

    // 배경을 그리는 패널
    static class BackgroundPanel extends JPanel {
        private Image backgroundImage;

        public BackgroundPanel(String fileName) {
            backgroundImage = new ImageIcon(fileName).getImage();
            setPreferredSize(new Dimension(800, 600));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(backgroundImage, 0, 0, this.getWidth(), this.getHeight(), this);
        }
    }

    // 게임 영역을 그리는 패널
    static class GamePanel extends JPanel {
        private Image gameImage;

        public GamePanel(String fileName) {
            gameImage = new ImageIcon(fileName).getImage();
            setOpaque(false);
            
            setPreferredSize(new Dimension(gameImage.getWidth(null), gameImage.getHeight(null)));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(gameImage, 0, 0, this);
        }
    }
    

    

    // 내부 클래스 정의
 	class MyFrame extends JFrame implements ActionListener {
 		
 		private JTextField textField;
		private JTextArea textAreaDown;
		private JTextArea textAreaUp;
		private Game game;
		private JButton btnStart;
	    private JButton btnStart2;
	    private JButton btnStart3;
	    private Component squareBoardComponent;
	    private Component squareBoardComponent2;
	    private Component squareBoardComponent3;
	    private Component squareBoardComponent4;
	    private Component squareBoardComponent5;
	    private Component squareBoardComponent6;
	    private Component nextBoardComponent;
	    private Component holdBoardComponent;
	    private Component itemBoardComponent;
	    private Component itemBoard2Component2;
	    private Component itemBoard2Component3;
	    private Component itemBoard2Component4;
	    private Component itemBoard2Component5;
	    private Component itemBoard2Component6;
	    private JLabel namelabel1;
	    private JLabel namelabel2;
	    private JLabel namelabel3;
	    private JLabel namelabel4;
	    private JLabel namelabel5;
	    private JLabel mynamelabel;
	    private JLayeredPane layeredPane; // 여기에 선언
	    private GamePanel gamePanel; // GamePanel 멤버 변수 선언

		
	public MyFrame(Game game) {
	    	// 프레임 설정
	    	super("Tetris");
	    	this.game = game;
	    	this.layeredPane = getLayeredPane();
	    	
	    	addWindowFocusListener(new WindowAdapter() {
	    	    @Override
	    	    public void windowGainedFocus(WindowEvent e) {
	    	        // 창이 포커스를 얻었을 때 소리 재생을 활성화
	    	        game.unmute(); // 음소거 해제
	    	    }

	    	    @Override
	    	    public void windowLostFocus(WindowEvent e) {
	    	        // 창이 포커스를 잃었을 때 소리 재생을 비활성화
	    	        game.mute(); // 음소거
	    	    }
	    	});

			

	        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	        setLayout(new BorderLayout());
	        setSize(800, 600); // 프레임의 크기 설정
	
	
	        // 배경 패널 설정
	        BackgroundPanel backgroundPanel = new BackgroundPanel("src/images/bg2.png");
	        backgroundPanel.setLayout(new GridBagLayout()); // 패널 중앙 정렬을 위한 GridBagLayout 사용
	
	        
	        
	        // 게임 패널 설정
	        gamePanel = new GamePanel("src/images/matrix18x18.png"); // 게임 영역 이미지 파일명을 정확히 입력하세요.
	        GridBagConstraints gbc = new GridBagConstraints();
	        gbc.anchor = GridBagConstraints.CENTER; // 중앙 정렬
	        backgroundPanel.add(gamePanel, gbc);
	        
	        
	        squareBoardComponent = game.getSquareBoardComponent();
	        squareBoardComponent.setPreferredSize(new Dimension(250, 450)); // 게임 보드 크기 설정
	        gamePanel.add(squareBoardComponent);
	        
	        
	        
	        
	        
	     // 절대 위치 패널 설정
	        JPanel absolutePanel = new JPanel();
	        absolutePanel.setLayout(null);
	        absolutePanel.setOpaque(false);
	        absolutePanel.setPreferredSize(new Dimension(800, 600));
	
	
	     // nextBoardComponent 설정
	        nextBoardComponent = game.getNextBoardComponent();
	        nextBoardComponent.setBounds(535, 162, 36, 48); // 위치와 크기 설정
	        absolutePanel.add(nextBoardComponent);
	
	        // holdBoardComponent 설정
	        holdBoardComponent = game.getHoldBoardComponent();
	        holdBoardComponent.setBounds(255, 161, 36, 48); // 위치와 크기 설정
	        absolutePanel.add(holdBoardComponent);
	        
	        // itemBoardComponent 설정
	        itemBoardComponent = game.getItemBoardComponent();
	        itemBoardComponent.setBounds(497, 117, 13, 362); // 위치와 크기 설정
	        absolutePanel.add(itemBoardComponent);
	        
	    
	        
	        gamePanel.setOpaque(false);
	        gamePanel.setLayout(new GridBagLayout()); // 게임 패널 내 컴포넌트 중앙 정렬을 위한 GridBagLayout 사용
	        GridBagConstraints gbcGame = new GridBagConstraints();
	        gbcGame.anchor = GridBagConstraints.CENTER; // 중앙 정렬
	        
	        
	        gamePanel.add(squareBoardComponent, gbcGame);
	        
	
	     // JLayeredPane을 사용하여 absolutePanel을 맨 위에 놓습니다.
	        //JLayeredPane layeredPane = getLayeredPane(); //맨 위에 이미 선언함
	        layeredPane.add(absolutePanel, JLayeredPane.DEFAULT_LAYER);
	        absolutePanel.setBounds(0, 0, 800, 600);
	        
	        
	        
	        JPanel ButtonPanel = new JPanel();
	        ButtonPanel.setLayout(new BoxLayout(ButtonPanel, BoxLayout.Y_AXIS)); // 세로로 쌓기 위한 레이아웃
	        ButtonPanel.setOpaque(false); // 패널 배경을 투명하게 설정
	        
	        
	     // 버튼의 기본 크기와 최소 크기를 동일하게 설정
	        Dimension buttonSize = new Dimension(120, 60);
	        
	     // 시작 버튼 설정
	        btnStart = new JButton();
	        btnStart.setText("Ready");
	        btnStart.setPreferredSize(buttonSize);
	        btnStart.setMinimumSize(buttonSize);
	        btnStart.setMaximumSize(buttonSize);
	        btnStart.setBounds(400-btnStart.getPreferredSize().width/2, 300-btnStart.getPreferredSize().width/2, btnStart.getPreferredSize().width, btnStart.getPreferredSize().height);
	        layeredPane.add(btnStart, Integer.valueOf(2));
	        
	        
	        
	     // 시작 버튼 설정2
	        btnStart2 = new JButton();
	        btnStart2.setText("Single Start");
	        btnStart2.setPreferredSize(buttonSize);
	        btnStart2.setMinimumSize(buttonSize);
	        btnStart2.setMaximumSize(buttonSize);
	        btnStart2.setBounds(400-btnStart2.getPreferredSize().width/2, 360-btnStart2.getPreferredSize().width/2, btnStart2.getPreferredSize().width, btnStart2.getPreferredSize().height);
	        layeredPane.add(btnStart2, Integer.valueOf(2));
	        
	        
	     // 스타트 버튼 액션 리스너
	        btnStart.addActionListener(new ActionListener() {
	            @Override
	            public void actionPerformed(ActionEvent e) {
	                // 게임 보드를 보이게 하고 게임 시작
	            	btnStart.setEnabled(false);
	            	BgmPlayer.playsfx("src/sounds/ready.wav");
	            	sendReadyStatus();
	                
	                //game.start(); // 게임의 start 메서드를 호출합니다.
	            }
	        });
	        
	     // 스타트 버튼 액션 리스너2
	        btnStart2.addActionListener(new ActionListener() {
	            @Override
	            public void actionPerformed(ActionEvent e) {
	                // 게임 보드를 보이게 하고 게임 시작
	            	game.isSingle = true;
	            	btnStart.setVisible(false);
	            	btnStart2.setVisible(false);
	            	squareBoardComponent.setVisible(true);
	                nextBoardComponent.setVisible(true);
	                holdBoardComponent.setVisible(true);
	                itemBoardComponent.setVisible(true);
	                btnStart.setVisible(false);
	     		    btnStart2.setVisible(false);
	                
	                game.start(); // 게임의 start 메서드를 호출합니다.
	            }
	        });
	        
	        
//	        int x = (800 - ButtonPanel.getPreferredSize().width) / 2;
//	        int y = (600 - ButtonPanel.getPreferredSize().height) / 2;
//	        ButtonPanel.setBounds(x, y, ButtonPanel.getPreferredSize().width, ButtonPanel.getPreferredSize().height); // Set the bounds of the button
//	        //버튼패널 중앙정렬
//	        layeredPane.add(ButtonPanel, Integer.valueOf(2));
//	        //layerdpane에 한층 더 쌓기
	        
	        
	        
	        
	        
	        
	        
	        
	        
	        JPanel chatPanel = new JPanel();
	        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS)); // 세로로 쌓기 위한 레이아웃
	        chatPanel.setOpaque(false); // 패널 배경을 투명하게 설정
	        
	        
	     // JTextArea textAreaUp 추가
	        this.textAreaUp = new JTextArea(8, 14);
	        textAreaUp.setEditable(false);
	        textAreaUp.setLineWrap(true);
	        textAreaUp.setWrapStyleWord(true);
	        textAreaUp.setBackground(new Color(50, 90, 165)); // 색
	        textAreaUp.setForeground(new Color(49, 140, 214)); // Set the text color
	        textAreaUp.setBorder(BorderFactory.createLineBorder(new Color(50, 90, 165)));
	        JScrollPane scrollPaneUp = new JScrollPane(textAreaUp, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER); // 스크롤 가능하도록 JScrollPane에 추가
	        scrollPaneUp.setBorder(BorderFactory.createLineBorder(new Color(50, 90, 165)));
	        chatPanel.add(scrollPaneUp);
	        
	      //teztAreaDown에 딸린--scrollPaneDown에 딸린-- Scrollbar에 딸린-부속물을 관리하는 코드 
	        JScrollBar verticalScrollBar1 = scrollPaneUp.getVerticalScrollBar();
	        verticalScrollBar1.setUI(new BasicScrollBarUI() {
	            @Override
	            protected void configureScrollBarColors() {
	                this.thumbColor = new Color(46, 145, 220);
	                this.trackColor = new Color(24, 83, 160);
	            }
	            
	            //왠진 모르겠지만 아래의 코드를 쓰면 버튼이 깔끔하게 사라진다.
	            @Override
	            protected JButton createDecreaseButton(int orientation) {
	                return createButton();
	            }
	
	            @Override
	            protected JButton createIncreaseButton(int orientation) {
	                return createButton();
	            }
	            
	            private JButton createButton() {
	                JButton button = new JButton();
	                // You can set the border to empty if you want to hide the border
	                button.setBorder(BorderFactory.createEmptyBorder());
	                return button;
	            }
	            //왠진 모르겠지만 위의 코드를 쓰면 버튼이 깔끔하게 사라진다.
	            
	        });
	        
	        
	        
	
	        // JTextArea textAreaDown 추가
	        this.textAreaDown = new JTextArea(8, 14);
	        textAreaDown.setEditable(false);
	        textAreaDown.setLineWrap(true);
	        textAreaDown.setWrapStyleWord(false);
	        textAreaDown.setBackground(new Color(30, 75, 130)); // 색
	        textAreaDown.setForeground(new Color(80, 158, 71)); // Set the text color
	        textAreaDown.setBorder(BorderFactory.createLineBorder(new Color(30, 75, 130)));
	        JScrollPane scrollPaneDown = new JScrollPane(textAreaDown, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER); // 스크롤 가능하도록 JScrollPane에 추가
	        scrollPaneDown.setBorder(BorderFactory.createLineBorder(new Color(30, 75, 130)));
	        chatPanel.add(scrollPaneDown);
	        
	     //teztAreaDown에 딸린--scrollPaneDown에 딸린-- Scrollbar에 딸린-부속물을 관리하는 코드 
	        JScrollBar verticalScrollBar = scrollPaneDown.getVerticalScrollBar();
	        verticalScrollBar.setUI(new BasicScrollBarUI() {
	            @Override
	            protected void configureScrollBarColors() {
	                this.thumbColor = new Color(46, 145, 220);
	                this.trackColor = new Color(24, 83, 160);
	            }
	            
	            //왠진 모르겠지만 아래의 코드를 쓰면 버튼이 깔끔하게 사라진다.
	            @Override
	            protected JButton createDecreaseButton(int orientation) {
	                return createButton();
	            }
	
	            @Override
	            protected JButton createIncreaseButton(int orientation) {
	                return createButton();
	            }
	            
	            private JButton createButton() {
	                JButton button = new JButton();
	                // You can set the border to empty if you want to hide the border
	                button.setBorder(BorderFactory.createEmptyBorder());
	                return button;
	            }
	            //왠진 모르겠지만 위의 코드를 쓰면 버튼이 깔끔하게 사라진다.
	            
	        });
	
	        
	
	        // JTextField textField 추가
	        this.textField = new JTextField(14);
	        this.textField.addActionListener(this);
	        chatPanel.add(textField);
	        
	     // chatPanel을 JLayeredPane에 추가
	        layeredPane.add(chatPanel, Integer.valueOf(1));
	        chatPanel.setBounds(605, 240, chatPanel.getPreferredSize().width, chatPanel.getPreferredSize().height);
	

	        //채팅창 누르면 게임 영원히 안되는 문제 해결!!!!!!
	        // 채팅창의 JTextField에 FocusListener 추가
	        chatPanel.addFocusListener(new FocusAdapter() {
	            public void focusLost(FocusEvent e) {
	                // 포커스가 채팅창에서 벗어날 때 게임 패널로 포커스 이동
	            	squareBoardComponent.requestFocusInWindow();
	            }
	        });
	        
	        
	
	        // frame에 backgroundPanel을 추가합니다.
	        setContentPane(backgroundPanel);
	        setResizable(false); // 프레임의 크기 조정 불가능하게 설정
	        pack();
	        setLocationRelativeTo(null); // 창을 화면 중앙에 배치
	        setVisible(true); // 프레임을 보이게 설정
	        
	        

    }
		
		
 		public void actionPerformed(ActionEvent evt) { //채팅을 입력했을 때 일어나는 코드
			String s = textField.getText();
			try {
				os.writeUTF(s);
			} catch (IOException e) {
				e.printStackTrace();
			}
			//textAreaDown.append("SENT: " + s + "\n");
			textField.setText(""); // Clear the textField
			textAreaDown.setCaretPosition(textAreaDown.getDocument().getLength());
		}
 	// Inside the MyFrame class
 		public void startGameComponents() {
 		    // Method body
 			game.handleGameOverSingle();
 		    btnStart.setVisible(false);
 		    btnStart2.setVisible(false);
 		    squareBoardComponent.setVisible(true);
 		    
 		    setVisibleCmp(totalReady);
 		    
 		    nextBoardComponent.setVisible(true);
 		    holdBoardComponent.setVisible(true);
 		    itemBoardComponent.setVisible(true);
 		    btnStart.setVisible(false);
		    btnStart2.setVisible(false);
		   
		    //싱글 게임 끝나고 굳는 효과 주려는 코드
		    long startTime = System.currentTimeMillis(); // 시작 시간을 기록
	        long elapsedTime = 0L;
	        BgmPlayer.playsfx("src/sounds/finishready.wav");
	        while (elapsedTime < 700) { // 0.5초 (500밀리초) 동안 실행, 
	            elapsedTime = System.currentTimeMillis() - startTime;
	        }
	        game.isSingle = false;
	        
 		    game.start(); // Call the start method of game
 		    
 		   squareBoardComponent.requestFocusInWindow();
 		    
 		}
 		
 		
 		
 		public void gameAllOverHandler() {
 			game.makeFinishClear();
 			btnStart.setEnabled(true);
 			btnStart.setVisible(true);
        	btnStart2.setVisible(true);
            nextBoardComponent.setVisible(false);
            holdBoardComponent.setVisible(false);
            itemBoardComponent.setVisible(false);
            btnStart.setVisible(true);
 		    btnStart2.setVisible(true);
 		}

 		
 		public void addPlayer2() {
 			squareBoardComponent2 = game.getSquareBoardComponent2(arrangeOther[0]);	        
 	        squareBoardComponent2.setPreferredSize(squareBoardComponent2.getPreferredSize()); // Set the new preferred size
 	        squareBoardComponent2.setBounds(31, 51, squareBoardComponent2.getPreferredSize().width, squareBoardComponent2.getPreferredSize().height);
 	        layeredPane.add(squareBoardComponent2, Integer.valueOf(3));
 	        
 	       // itemBoardComponent 설정
	        itemBoard2Component2 = game.getItemBoard2Component(arrangeOther[0]);
	        itemBoard2Component2.setBounds(30, 219, 81, 9); // 위치와 크기 설정
	        layeredPane.add(itemBoard2Component2, Integer.valueOf(3));
	        
	        
	        ImageIcon imageIcon2 = new ImageIcon("src/images/name0" + Integer.toString(arrangeOther[0]) + ".png");
	        namelabel1 = new JLabel(imageIcon2);
	        namelabel1.setBounds(24, 219, 93, 87);
	        layeredPane.add(namelabel1, Integer.valueOf(3));
	        
 		}
 		
 		public void addPlayer3() {
 			squareBoardComponent3 = game.getSquareBoardComponent2(arrangeOther[1]);	        
 	        squareBoardComponent3.setPreferredSize(squareBoardComponent3.getPreferredSize()); // Set the new preferred size
 	        squareBoardComponent3.setBounds(126, 51, squareBoardComponent3.getPreferredSize().width, squareBoardComponent3.getPreferredSize().height);
 	        layeredPane.add(squareBoardComponent3, Integer.valueOf(3));
 	        
 	    // itemBoardComponent 설정
	        itemBoard2Component3 = game.getItemBoard2Component(arrangeOther[1]);
	        itemBoard2Component3.setBounds(125, 219, 81, 9); // 위치와 크기 설정
	        layeredPane.add(itemBoard2Component3, Integer.valueOf(3));
	        
	        ImageIcon imageIcon3 = new ImageIcon("src/images/name0" + Integer.toString(arrangeOther[1]) + ".png");
	        namelabel2 = new JLabel(imageIcon3);
	        namelabel2.setBounds(119, 219, 93, 87);
	        layeredPane.add(namelabel2, Integer.valueOf(3));
 	        }
 		
 		public void addPlayer4() {
 			squareBoardComponent4 = game.getSquareBoardComponent2(arrangeOther[2]);	        
 	        squareBoardComponent4.setPreferredSize(squareBoardComponent4.getPreferredSize()); // Set the new preferred size
 	        squareBoardComponent4.setBounds(16, 331, squareBoardComponent4.getPreferredSize().width, squareBoardComponent4.getPreferredSize().height);
 	        layeredPane.add(squareBoardComponent4, Integer.valueOf(3));
 	        
 	    // itemBoardComponent 설정
	        itemBoard2Component4 = game.getItemBoard2Component(arrangeOther[2]);
	        itemBoard2Component4.setBounds(15, 499, 81, 9); // 위치와 크기 설정
	        layeredPane.add(itemBoard2Component4, Integer.valueOf(3));
	        
	        ImageIcon imageIcon4 = new ImageIcon("src/images/name0" + Integer.toString(arrangeOther[2]) + ".png");
	        namelabel3 = new JLabel(imageIcon4);
	        namelabel3.setBounds(9, 499, 93, 87);
	        layeredPane.add(namelabel3, Integer.valueOf(3));
 	        }
 		
 		public void addPlayer5() {
 			squareBoardComponent5 = game.getSquareBoardComponent2(arrangeOther[3]);	        
 	        squareBoardComponent5.setPreferredSize(squareBoardComponent5.getPreferredSize()); // Set the new preferred size
 	        squareBoardComponent5.setBounds(111, 331, squareBoardComponent5.getPreferredSize().width, squareBoardComponent5.getPreferredSize().height);
 	        layeredPane.add(squareBoardComponent5, Integer.valueOf(3));
 	        
 	    // itemBoardComponent 설정
	        itemBoard2Component5 = game.getItemBoard2Component(arrangeOther[3]);
	        itemBoard2Component5.setBounds(110, 499, 81, 9); // 위치와 크기 설정
	        layeredPane.add(itemBoard2Component5, Integer.valueOf(3));
	        
	        ImageIcon imageIcon5 = new ImageIcon("src/images/name0" + Integer.toString(arrangeOther[3]) + ".png");
	        namelabel4 = new JLabel(imageIcon5);
	        namelabel4.setBounds(104, 499, 93, 87);
	        layeredPane.add(namelabel4, Integer.valueOf(3));
 		}
 		
 		public void addPlayer6() {
 			squareBoardComponent6 = game.getSquareBoardComponent2(arrangeOther[4]);	        
 	        squareBoardComponent6.setPreferredSize(squareBoardComponent6.getPreferredSize()); // Set the new preferred size
 	        squareBoardComponent6.setBounds(206, 331, squareBoardComponent6.getPreferredSize().width, squareBoardComponent6.getPreferredSize().height);
 	        layeredPane.add(squareBoardComponent6, Integer.valueOf(3));
 	        
 	    // itemBoardComponent 설정
	        itemBoard2Component6 = game.getItemBoard2Component(arrangeOther[4]);
	        itemBoard2Component6.setBounds(205, 499, 81, 9); // 위치와 크기 설정
	        layeredPane.add(itemBoard2Component6, Integer.valueOf(3));
	        
	        ImageIcon imageIcon6 = new ImageIcon("src/images/name0" + Integer.toString(arrangeOther[4]) + ".png");
	        namelabel5 = new JLabel(imageIcon6);
	        namelabel5.setBounds(199, 499, 93, 87);
	        layeredPane.add(namelabel5, Integer.valueOf(3));
 		}
 		
 		public void addPlayer(int num) { //플레이어 컴포넌트(검은 배경) 추가
 			//System.out.println("addplayer"+num);
 			switch(num) {
 			case 0:
 				addPlayer2();
 				break;
 			case 1:
 				addPlayer3();
 				break;
 			case 2:
 				addPlayer4();
 				break;
 			case 3:
 				addPlayer5();
 				break;
 			case 4:
 				addPlayer6();
 				break;
 				
			default:
				break;
 			}
 		}
 		
 		public void setVisible2() {
 	 		squareBoardComponent2.setVisible(true);
 	 		itemBoard2Component2.setVisible(true);
 	 		namelabel1.setVisible(true);
 	 	}
 	 	public void setVisible3() {
 	 		squareBoardComponent3.setVisible(true);
 	 		itemBoard2Component3.setVisible(true);
 	 		namelabel2.setVisible(true);
 	 	}
 	 	public void setVisible4() {
 	 		squareBoardComponent4.setVisible(true);
 	 		itemBoard2Component4.setVisible(true);
 	 		namelabel3.setVisible(true);
 	 	}
 	 	public void setVisible5() {
 	 		squareBoardComponent5.setVisible(true);
 	 		itemBoard2Component5.setVisible(true);
 	 		namelabel4.setVisible(true);
 	 	}
 	 	public void setVisible6() {
 	 		squareBoardComponent6.setVisible(true);
 	 		itemBoard2Component6.setVisible(true);
 	 		namelabel5.setVisible(true);
 	 	}
 	 	
 	 	public void setVisibleMyName() {
 	 		ImageIcon imageIcon1 = new ImageIcon("src/images/myname0" + myNumber + ".png");
	        mynamelabel = new JLabel(imageIcon1);
	        mynamelabel.setBounds(300, 482, 247, 41);
	        layeredPane.add(mynamelabel, Integer.valueOf(4));
 	 		mynamelabel.setVisible(true);
 	 	}
 	 	
 	 	public void setVisibleCmp(int num) {
 	 	
 	 		switch(num) {
 	 		case 2:
 	 			setVisible2();
 	 			break;
 	 		case 3:
 	 			setVisible2();
 	 			setVisible3();
 	 			break;
 	 		case 4:
 	 			setVisible2();
 	 			setVisible3();
 	 			setVisible4();
 	 			break;
 	 		case 5:
 	 			setVisible2();
 	 			setVisible3();
 	 			setVisible4();
 	 			setVisible5();
 	 			break;
 	 		case 6:
 	 			setVisible2();
 	 			setVisible3();
 	 			setVisible4();
 	 			setVisible5();
 	 			setVisible6();
 	 			break;
 	 		default:
 	 			break;
 	 		
 	 		}
 	 	}
 	 	
 	 	public void singlePaused() {
 	 		btnStart.setBounds(400-btnStart.getPreferredSize().width/2, 270-btnStart.getPreferredSize().width/2, btnStart.getPreferredSize().width, btnStart.getPreferredSize().height);
			btnStart.setVisible(true);
			
		}
		
		public void singleResume() {
			btnStart.setVisible(false);
			btnStart.setBounds(400-btnStart.getPreferredSize().width/2, 300-btnStart.getPreferredSize().width/2, btnStart.getPreferredSize().width, btnStart.getPreferredSize().height);
			
		}

 	}
 	
 	public void singlePaused() {
			f.singlePaused();
			
		}
		
		public void singleResume() {
			f.singleResume();
			
		}
    
 	private void sendReadyStatus() {
 	    try {
 	        // 서버와의 연결이 있는 Socket을 가정하며, OutputStream이 이미 설정되어 있다고 가정합니다.
 	        
 	        os.writeUTF("READY");
 	        os.flush();
 	    } catch (IOException e) {
 	        e.printStackTrace();
 	    }
 	}

 	public void returnPosition(int x, int y, int type, int orientation, boolean hasItemBlock, int itemIndex, int itemType) {
 		if(!game.isSingle) {
	        try {
	            // os는 인스턴스 변수이므로 이제 직접 접근할 수 있습니다.
	            os.writeUTF("Position: " + this.myNumber + "," + x + "," + y + "," + type + "," + orientation + "," + hasItemBlock + "," + itemIndex + "," + itemType);
	            os.flush();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
 		}
    }
 	
 	public void sendOverStatus() {
 	    try {
 	        //others가 게임 종료되었을 때 서버에 해당 사실을 알림
 	        os.writeUTF("OVER" + "," + this.myNumber);
 	        os.flush();
 	    } catch (IOException e) {
 	        e.printStackTrace();
 	    }
 	}
 	
 	private void handleOthersOverStatus(String msg) { //서버로부터 받은 over 사인을 처리
 	    System.out.println(msg);
 		String[] parts = msg.substring(8).split(",");
 		String trashValue = parts[0].trim();
 		int clientNumber = Integer.parseInt(parts[1].trim());
 		int yerRank = Integer.parseInt(parts[2].trim());
 		
 		//System.out.println(clientNumber);
 		//딴놈이 2등인데 나는 살아있다면 자동 1등 --라는 조건을 일단 오버된 2등거를 처리하면서 넣는 게 좋지 않을까?
 		
 		if(clientNumber != this.myNumber) {
 			 
 			
 				//딴놈이 걍 죽으면
 				game.handleGameOverOther(clientNumber, yerRank); //일단 딴놈(이를테면 2등)을 처리해 주고,
 			
 				if(yerRank == 2 && (game.getState() != 4)) { //딴놈거를 처리했는데 걔가 2등이고 내가 살아있다?
 	 				game.handleGameOver();
 	 				System.out.println("Forced Finish : 1st Place");
 	 			}
 				
 		}else { //내가 해당 등수일 경우 내 등수를 화면에 출력
	 			game.getMyRank(yerRank); //가끔 thread.sleep때문에 씹힐수도 있음
	 			
	 			if(isexed = false) { //씹힐경우 다시 실행
	 			game.getMyRank(yerRank);
	 			}
	 			if(!game.getrankImageDrawn()) { //또 씹힐경우 다시 실행
	 				game.getMyRank(yerRank);
	 			}
	 			
	 			if(yerRank ==1) { //모든 게임이 완전히 종료되었을 경우
	 				try {
	 		 	        //others가 게임 종료되었을 때 서버에 해당 사실을 알림
	 		 	        os.writeUTF("ALL OVER");
	 		 	        os.flush();
	 		 	    } catch (IOException e) {
	 		 	        e.printStackTrace();
	 		 	    }
	 			}
 		}
 	}
 	
 	
 	
 	
 	public void hasremovedLine(int lines, int combo, int randomNum) {
 		if(!game.isSingle) {
	 	    try {
	 	        // 서버와의 연결이 있는 Socket을 가정하며, OutputStream이 이미 설정되어 있다고 가정합니다.
	 	        //others가 게임 종료되었을 때 서버에 해당 사실을 알림
	 	        os.writeUTF("removed : " + this.myNumber + "," + lines + "," + combo + "," + randomNum);
	 	        os.flush();
	 	    } catch (IOException e) {
	 	        e.printStackTrace();
	 	    }
 		}
 	}
 	
 	public void gotItems(int type) {
 		if(!game.isSingle) {
	 		try {
	 	        // 서버와의 연결이 있는 Socket을 가정하며, OutputStream이 이미 설정되어 있다고 가정합니다.
	 	        //others가 게임 종료되었을 때 서버에 해당 사실을 알림
	 	        os.writeUTF("gotItem : " + this.myNumber + "," + type);
	 	        os.flush();
	 	    } catch (IOException e) {
	 	        e.printStackTrace();
	 	    }
 		}	
 	}
 	
 	public void returnUsedItem(int clientNumber, int itemType, int calledNumber) {
 		if(!game.isSingle) {
	 		try {
	 	        // 서버와의 연결이 있는 Socket을 가정하며, OutputStream이 이미 설정되어 있다고 가정합니다.
	 	        // 아이템 사용 사실을 서버에 알림
	 			int randomNum = (int)(Math.random()*10);
	 	    	
	 	        os.writeUTF("usedItem : " + this.myNumber + "," + itemType + "," + calledNumber + "," + randomNum);
	 	        os.flush();
	 	    } catch (IOException e) {
	 	        e.printStackTrace();
	 	    }
 		}
 	}
 	
 	public void rotatedItems() {
 		if(!game.isSingle) {
	 		try {
	 	        // 서버와의 연결이 있는 Socket을 가정하며, OutputStream이 이미 설정되어 있다고 가정합니다.
	 	        // 아이템 체인지 사실을 서버에 알림
	 	    	
	 	        os.writeUTF("rotate : " + this.myNumber + "," + "trashvalue");
	 	        os.flush();
	 	    } catch (IOException e) {
	 	        e.printStackTrace();
	 	    }
 		}
 	}
 	
 	public int setFinalLines(int removedLines, int comboTemp) {
 		//콤보의 기준에서,, 123 456 789 1줄 2줄 3줄 10+ 4줄, 2연부터 1콤보 취급
 		int finalLines = removedLines;
 		int combo = comboTemp;
 		switch(removedLines) {
 		case 1:
 			switch(combo) {
 				case 1:
 					finalLines = 0;
 					
 					break;
 				
	 			case 2:
	 			case 3:
	 			case 4:
	 				finalLines = 1;
	 				break;
	 			case 5:
	 			case 6:
	 			case 7:
	 				finalLines = 2;
	 				break;
	 			case 8:
	 			case 9:
	 			case 10:
	 				finalLines = 3;
	 				break;
	 			case 11:
	 			case 12:
	 			case 13:
	 				finalLines = 4;
	 				break;
	 				default:
	 					break;
 			}
 			break;
 		case 2:
 			finalLines = 1;
 			switch(combo) {
	 			case 2:
	 			case 3:
	 			case 4:
	 				finalLines = finalLines+1;
	 				break;
	 			case 5:
	 			case 6:
	 			case 7:
	 				finalLines = finalLines+2;
	 				break;
	 			case 8:
	 			case 9:
	 			case 10:
	 				finalLines = finalLines+3;
	 				break;
	 			case 11:
	 			case 12:
	 			case 13:
	 				finalLines = finalLines+4;
	 				break;
	 				default:
	 					break;
 			}
 			break;
 		case 3:
 			finalLines = 2;
 			switch(combo) {
 			case 2:
 			case 3:
 			case 4:
 				finalLines = finalLines+1;
 				break;
 			case 5:
 			case 6:
 			case 7:
 				finalLines = finalLines+2;
 				break;
 			case 8:
 			case 9:
 			case 10:
 				finalLines = finalLines+3;
 				break;
 			case 11:
 			case 12:
 			case 13:
 				finalLines = finalLines+4;
 				break;
 				default:
 					break;
			}
 			break;
 		case 4:
 			finalLines = 4;
 			switch(combo) {
 			case 2:
 			case 3:
 			case 4:
 				finalLines = finalLines+1;
 				break;
 			case 5:
 			case 6:
 			case 7:
 				finalLines = finalLines+2;
 				break;
 			case 8:
 			case 9:
 			case 10:
 				finalLines = finalLines+3;
 				break;
 			case 11:
 			case 12:
 			case 13:
 				finalLines = finalLines+4;
 				break;
 				default:
 					break;
			}
 			break;
 		default:
 			break;
 			
 		}
 		
 		return finalLines;
 	}

 	public int setFinalLines(int removedLines, int comboTemp, int clientNumber) {
 		//콤보의 기준에서,, 123 456 789 1줄 2줄 3줄 10+ 4줄, 2연부터 1콤보 취급
 		int combo = comboTemp;
 		//combo--;
 		int tempcombo = combo-1;
 		int finalLines = removedLines;
 		switch(removedLines) {
 		case 1:
 			switch(combo) {
 			case 1:
					finalLines = 0;
					
					break;
	 			case 2:
	 			case 3:
	 			case 4:
	 				f.textAreaUp.append("client " + clientNumber + " : " + tempcombo + "콤보 (1줄)" + "\n");
	 				finalLines = 1;
	 				break;
	 			case 5:
	 			case 6:
	 			case 7:
	 				f.textAreaUp.append("client " + clientNumber + " : " + tempcombo + "콤보 (2줄)" + "\n");
	 				finalLines = 2;
	 				break;
	 			case 8:
	 			case 9:
	 			case 10:
	 				f.textAreaUp.append("client " + clientNumber + " : " + tempcombo + "콤보 (3줄)" + "\n");
	 				finalLines = 3;
	 				break;
	 			case 11:
	 			case 12:
	 			case 13:
	 				f.textAreaUp.append("client " + clientNumber + " : " + tempcombo + "콤보 (4줄)" + "\n");
	 				finalLines = 4;
	 				break;
	 				default:
	 					break;
 			}
 			break;
 		case 2:
 			f.textAreaUp.append("client " + clientNumber + " : " + "더블 (1줄)" + "\n");
 			finalLines = 1;
 			switch(combo) {
	 			case 2:
	 			case 3:
	 			case 4:
	 				f.textAreaUp.append("client " + clientNumber + " : " + tempcombo + "콤보 (1줄)" + "\n");
	 				finalLines = finalLines+1;
	 				break;
	 			case 5:
	 			case 6:
	 			case 7:
	 				f.textAreaUp.append("client " + clientNumber + " : " + tempcombo + "콤보 (2줄)" + "\n");
	 				finalLines = finalLines+2;
	 				break;
	 			case 8:
	 			case 9:
	 			case 10:
	 				f.textAreaUp.append("client " + clientNumber + " : " + tempcombo + "콤보 (3줄)" + "\n");
	 				finalLines = finalLines+3;
	 				break;
	 			case 11:
	 			case 12:
	 			case 13:
	 				f.textAreaUp.append("client " + clientNumber + " : " + tempcombo + "콤보 (4줄)" + "\n");
	 				finalLines = finalLines+4;
	 				break;
	 				default:
	 					break;
 			}
 			break;
 		case 3:
 			f.textAreaUp.append("client " + clientNumber + " : " + "트리플 (2줄)" + "\n");
 			finalLines = 2;
 			switch(combo) {
 			case 2:
 			case 3:
 			case 4:
 				f.textAreaUp.append("client " + clientNumber + " : " + tempcombo + "콤보 (1줄)" + "\n");
 				finalLines = finalLines+1;
 				break;
 			case 5:
 			case 6:
 			case 7:
 				f.textAreaUp.append("client " + clientNumber + " : " + tempcombo + "콤보 (2줄)" + "\n");
 				finalLines = finalLines+2;
 				break;
 			case 8:
 			case 9:
 			case 10:
 				f.textAreaUp.append("client " + clientNumber + " : " + tempcombo + "콤보 (3줄)" + "\n");
 				finalLines = finalLines+3;
 				break;
 			case 11:
 			case 12:
 			case 13:
 				f.textAreaUp.append("client " + clientNumber + " : " + tempcombo + "콤보 (4줄)" + "\n");
 				finalLines = finalLines+4;
 				break;
 				default:
 					break;
			}
 			break;
 		case 4:
 			f.textAreaUp.append("client " + clientNumber + " : " + "테트리스 (4줄)" + "\n");
 			finalLines = 4;
 			switch(combo) {
 			case 2:
 			case 3:
 			case 4:
 				f.textAreaUp.append("client " + clientNumber + " : " + tempcombo + "콤보 (1줄)" + "\n");
 				finalLines = finalLines+1;
 				break;
 			case 5:
 			case 6:
 			case 7:
 				f.textAreaUp.append("client " + clientNumber + " : " + tempcombo + "콤보 (2줄)" + "\n");
 				finalLines = finalLines+2;
 				break;
 			case 8:
 			case 9:
 			case 10:
 				f.textAreaUp.append("client " + clientNumber + " : " + tempcombo + "콤보 (3줄)" + "\n");
 				finalLines = finalLines+3;
 				break;
 			case 11:
 			case 12:
 			case 13:
 				f.textAreaUp.append("client " + clientNumber + " : " + tempcombo + "콤보 (4줄)" + "\n");
 				finalLines = finalLines+4;
 				break;
 				default:
 					break;
			}
 			break;
 		default:
 			break;
 			
 		}
 		
 		return finalLines;
 	}
 	
    public static void main(String[] args) throws IOException { //main
    	
    	Tetris m = new Tetris();
	}
}