package tetrisProject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class ChatWindow extends JFrame {
    private JTextArea textArea;
    private JTextField textField;
    private JScrollPane scrollPane;

    public ChatWindow() {
        // 창 설정
        setTitle("Chat Interface");
        setSize(300, 250); // 너비 300px, 최소 높이 250px (60px TextArea + 30px TextField + 여백)
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // TextArea 설정
        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        // TextArea를 ScrollPane에 추가
        scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(300, 60)); // 초기 높이 60px (2줄)
        add(scrollPane, BorderLayout.CENTER);

        // TextField 설정
        textField = new JTextField();
        textField.setPreferredSize(new Dimension(300, 30)); // 높이 30px
        add(textField, BorderLayout.SOUTH);

        // TextField에 ActionListener 추가
        textField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String s = textField.getText();
                textArea.append("SENT: " + s + "\n");
                textField.setText("");
            }
        });

        // TextField에 FocusListener 추가
        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                scrollPane.setPreferredSize(new Dimension(300, 180)); // 높이 180px (5줄)로 변경
                ChatWindow.this.pack(); // 창 크기를 내용에 맞게 조정
            }

            @Override
            public void focusLost(FocusEvent e) {
                scrollPane.setPreferredSize(new Dimension(300, 60)); // 높이 60px (2줄)로 변경
                ChatWindow.this.pack(); // 창 크기를 내용에 맞게 조정
            }
        });

        pack(); // 컴포넌트들을 포함하도록 창 크기 조정
        setLocationRelativeTo(null); // 창을 화면 중앙에 배치
        setVisible(true); // 창을 보이게 설정
    }

    public static void main(String[] args) {
        // 안전하게 UI를 생성하기 위해 이벤트 디스패치 스레드에서 실행
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ChatWindow();
            }
        });
    }
}
