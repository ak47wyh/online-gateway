package com.iboxpay.gateway.common;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.Spring;
import javax.swing.SpringLayout;

public class Main extends JFrame implements ActionListener {

    private JTextField urlField;
    private JTextArea contentTextArea;
    private JButton confirmButton;
    /** 
     *  
     */
    private static final long serialVersionUID = -6045918631932051025L;

    public Main() {
        init();
    }

    private void init() {
        Container container = getContentPane();
        SpringLayout springLayout = new SpringLayout();
        container.setLayout(springLayout);

        JLabel urlLabel = new JLabel("URL:");
        urlField = new JTextField(30);
        urlField.setText("");
        // JLabel charsetLable = new JLabel("字符集:");
        // final JTextField charsetField = new JTextField(30);

        JLabel contentLabel = new JLabel("报文体:");
        contentTextArea = new JTextArea(3, 30);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(contentTextArea);
        contentTextArea.setLineWrap(true);
        confirmButton = new JButton("提交");
        // JButton cancelButton = new JButton("cancel");
        Spring st = Spring.constant(10);
        Spring st2 = Spring.constant(30);
        container.add(urlLabel);
        springLayout.putConstraint(SpringLayout.NORTH, urlLabel, st, SpringLayout.NORTH, container);
        springLayout.putConstraint(SpringLayout.WEST, urlLabel, st, SpringLayout.WEST, container);
        container.add(urlField);
        springLayout.putConstraint(SpringLayout.WEST, urlField, st2, SpringLayout.EAST, urlLabel);
        springLayout.putConstraint(SpringLayout.NORTH, urlField, 0, SpringLayout.NORTH, urlLabel);
        springLayout.putConstraint(SpringLayout.EAST, urlField, Spring.minus(st), SpringLayout.EAST, container);
        container.add(contentLabel);
        springLayout.putConstraint(SpringLayout.WEST, contentLabel, 0, SpringLayout.WEST, urlLabel);
        springLayout.putConstraint(SpringLayout.NORTH, contentLabel, st, SpringLayout.SOUTH, urlLabel);
        container.add(scrollPane);
        springLayout.putConstraint(SpringLayout.NORTH, scrollPane, 0, SpringLayout.NORTH, contentLabel);
        springLayout.putConstraint(SpringLayout.WEST, scrollPane, 0, SpringLayout.WEST, urlField);
        // ///////////////////////////////////////////////////////////

        springLayout.putConstraint(SpringLayout.EAST, scrollPane, Spring.minus(st), SpringLayout.EAST, container);
        container.add(confirmButton);
        springLayout.putConstraint(SpringLayout.SOUTH, scrollPane, Spring.minus(st), SpringLayout.NORTH, confirmButton);
        springLayout.putConstraint(SpringLayout.EAST, confirmButton, Spring.minus(st), SpringLayout.EAST, container);
        springLayout.putConstraint(SpringLayout.SOUTH, confirmButton, Spring.minus(st), SpringLayout.SOUTH, container);
        addWindowFocusListener(new WindowAdapter() {

            @Override
            public void windowGainedFocus(WindowEvent e) {
                urlField.requestFocus();
            }
        });
        confirmButton.addActionListener(this);
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        Main frame = new Main();
        frame.setTitle("HTTP提交测试");
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBounds(200, 200, 800, 600);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // JOptionPane.showMessageDialog(this, "你好啊" , "hi",
        // JOptionPane.PLAIN_MESSAGE);
        Response response;
        try {
            response = post(this.urlField.getText(), this.contentTextArea.getText());
        } catch (IOException _) {
            JOptionPane.showMessageDialog(this, _.getMessage(), "出错", JOptionPane.ERROR_MESSAGE);
            _.printStackTrace();
            return;
        }
        // Object[] options = {"确定","复制内容"};
        // int select = JOptionPane.showOptionDialog(this, response.content,
        // "返回状态码："+response.status, JOptionPane.YES_OPTION,
        // JOptionPane.CANCEL_OPTION, null, options, options[0]);
        // if(select == 1){
        // Clipboard clipboard = getToolkit().getSystemClipboard();
        // StringSelection strSel = new StringSelection(response.content);
        // clipboard.setContents(strSel, null);
        // }
        new ResponseFrame(response).setVisible(true);
    }

    String charset = "UTF-8";

    private Response post(String urlStr, String contentBody) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) (url).openConnection();
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setConnectTimeout(0);
        conn.setReadTimeout(0);// 分钟转换成毫秒
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        //		conn.setRequestProperty("Expect", "100-continue");
        conn.connect();
        conn.getOutputStream().write(contentBody.getBytes(charset));
        Response response = new Response();
        InputStreamReader reader = new InputStreamReader(conn.getInputStream(), charset);
        StringBuilder buf = new StringBuilder();
        int ch;
        while ((ch = reader.read()) != -1) {
            buf.append((char) ch);
        }
        response.content = buf.toString();
        response.status = conn.getResponseCode();
        return response;
    }

    private static class Response {

        int status;
        String content;
    }

    static class ResponseFrame extends JFrame {

        private static final long serialVersionUID = 1L;

        public ResponseFrame(Response response) {
            setTitle("返回HTTP状态码：" + response.status);
            Container pane = getContentPane();
            pane.setLayout(new BorderLayout());
            javax.swing.JTextArea contentArea = new javax.swing.JTextArea();
            contentArea.setRows(MAXIMIZED_BOTH);
            contentArea.setText(response.content);
            pane.add(new JScrollPane(contentArea), BorderLayout.CENTER);
            //			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setResizable(true);
            setSize(800, 600);
            setLocationRelativeTo(null);
            //			setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        }
    }
}