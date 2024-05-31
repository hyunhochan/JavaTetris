package tetrisProject;

import java.io.File;
import javax.sound.sampled.FloatControl;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class BgmPlayer {
	private static boolean isMuted = false; // Global mute state
    private Clip clip;
    private boolean stopRequested = false; // 음악 중단 요청을 추적하는 플래그
    private Thread bgmThread;
    FloatControl fc2;
    
    public List<String> bgmList = List.of("src/bgm/bgm_02.wav", "src/bgm/bgm_03.wav", "src/bgm/bgm_04.wav", "src/bgm/bgm_05.wav", "src/bgm/bgm_06.wav", "src/bgm/bgm_08.wav", "src/bgm/bgm_10.wav", "src/bgm/bgm_11.wav", "src/bgm/bgm_12.wav", "src/bgm/bgm_13.wav", "src/bgm/bgm_14.wav", "src/bgm/bgm_15.wav", "src/bgm/bgm_16.wav", "src/bgm/bgm_17.wav", "src/bgm/bgm_18.wav"); // BGM 파일 이름 목록

    public void startRandomBgm() {
    	// 현재 재생 중인 BGM이 있다면 중지하고 자원을 해제합니다.
        if (clip != null) {
            clip.stop();
            clip.close();
            clip = null;
        }
        Random rand = new Random();
        int randomIndex = rand.nextInt(bgmList.size());
        String soundFilePath = bgmList.get(randomIndex);
        bgmThread = new Thread(() -> {
        	playSound(soundFilePath);
        });
        bgmThread.start();
    }

    public void playSound(String soundFilePath) {
    	stopRequested = false;
        try {
            File soundFile = new File(soundFilePath);
            AudioInputStream audioIn1 = AudioSystem.getAudioInputStream(soundFile);
            clip = AudioSystem.getClip();
            clip.open(audioIn1);
            fc2 = (FloatControl)clip.getControl(FloatControl.Type.MASTER_GAIN);
            if (isMuted && fc2 != null) {
                fc2.setValue(fc2.getMinimum());
            }else if (!isMuted && fc2 != null) {
            	fc2.setValue(0);
            }

	            clip.addLineListener(event -> {
	            	if(stopRequested == false) {
	                // STOP 이벤트가 발생하면 노래가 끝났다고 간주하고 새로운 BGM을 시작합니다.
	                if (event.getType() == LineEvent.Type.STOP) {
	                    clip.close();
	                    startRandomBgm();
	                    stopRequested = false;// 다음 랜덤 BGM을 시작합니다.
	                }
	            	}
	            });
            
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }
    
    public void playLoopingSound(String soundFilePath) {
    	stopRequested = false;
        try {
            if (clip != null) {
                clip.close(); // 이전 클립을 닫습니다.
            }
            File soundFile = new File(soundFilePath);
            AudioInputStream audioIn2 = AudioSystem.getAudioInputStream(soundFile);
            clip = AudioSystem.getClip();
            clip.open(audioIn2);
            fc2 = (FloatControl)clip.getControl(FloatControl.Type.MASTER_GAIN);
            if (isMuted && fc2 != null) {
                fc2.setValue(fc2.getMinimum());
            }else if (!isMuted && fc2 != null) {
            	fc2.setValue(0);
            }
            
            
            clip.start();
            clip.loop(Clip.LOOP_CONTINUOUSLY); // 무한 반복을 제거합니다.
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }
    
    public static void playsfx(String soundFilePath) {
        new Thread(() -> {
            try {
                File soundFile = new File(soundFilePath);
                AudioInputStream audioIn3 = AudioSystem.getAudioInputStream(soundFile);

                DataLine.Info info = new DataLine.Info(Clip.class, audioIn3.getFormat());
                Clip clip = (Clip) AudioSystem.getLine(info);

                clip.open(audioIn3);

                // Check if the MASTER_GAIN control is supported
                if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                    FloatControl fc = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                    // If muted, set volume to minimum, else set to a default or previous level
                    if (isMuted) {
                        fc.setValue(fc.getMinimum());
                    } else {
                        fc.setValue(0); // or restore previous volume
                    }
                } else {
                    // If MASTER_GAIN is not supported, log it or notify the user
                    System.out.println("MASTER_GAIN not supported for this audio clip.");
                }

                clip.start();

                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clip.close();
                    }
                });

            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
                e.printStackTrace();
            }
        }).start();
    }


    public void stopBgm() {
        stopRequested = true; // 중단 요청 플래그를 설정합니다.
        if (clip != null) {
            clip.stop(); // 음악 재생을 중지합니다.
            clip.close(); // 시스템 리소스를 해제합니다.
            clip = null; // clip 참조를 해제합니다.
            
        }
    }
    
    public void pauseBgm() {
    	stopRequested = true;
        if (clip != null && clip.isRunning()) {
            clip.stop(); // BGM 재생을 일시 중지합니다.
        }
    }

    public void resumeBgm() {
    	stopRequested = false;
        if (clip != null && !clip.isRunning()) {
            clip.start(); // BGM 재생을 다시 시작합니다.
        }
    }
    

    
 // 음소거를 위한 메서드
    public void mute() {
    	isMuted = true;
    	if(fc2 != null) {
    		fc2.setValue(fc2.getMinimum());
    	}
    	if(fc2 != null) {
    		fc2.setValue(fc2.getMinimum());
    	}
    }

    // 음소거 해제를 위한 메서드
    public void unmute() {
    	isMuted = false;
    	if(fc2 != null) {
    		fc2.setValue(0);
    	}
    	if(fc2 != null) {
    		fc2.setValue(0);
    	}
    }
    	

}
