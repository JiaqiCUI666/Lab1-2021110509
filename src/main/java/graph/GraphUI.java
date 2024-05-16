package graph;

import com.formdev.flatlaf.themes.FlatMacLightLaf;
import util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import util.Document;

public class GraphUI extends JFrame {

    private JComboBox<String> inputTextField1;
    private JComboBox<String> inputTextField2;
    private JTextField inputTextField3;
    private JComboBox<String> file_path;
    private JTextArea outputTextArea;
    private JLabel imageLabel;
    private Graph graph;
    private List<String> Node_List;
    private String Random_walk_result;
    private List<String> path_list = new ArrayList<>();
    private int image_count = 0;
    private boolean has_calculate = false;
    private List<String> previous;

    public GraphUI() {
        setTitle("Graph UI");
        setSize(1000, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        final int[] rand_walk = {0};
        final int[] distance = {0};
        final int[] current = {2};
        // Input panel for text fields
        JPanel inputPanel = new JPanel(new GridLayout(3, 4));
        inputPanel.add(new JLabel("       File path is:"), BorderLayout.CENTER);
        file_path = new JComboBox<>();
        TxtFinder file_handler = new TxtFinder();
        List<Path> src_list = file_handler.findTxtFiles("./src/main/file");
        for (Path src : src_list) {
            file_path.addItem(src.toString());
        }
        inputPanel.add(file_path);
        inputPanel.add(new JLabel("       Input 1:"), BorderLayout.CENTER);
        inputTextField1 = new JComboBox<>();
        inputPanel.add(inputTextField1);
        inputPanel.add(new JLabel("       Input 2:"), BorderLayout.CENTER);
        inputTextField2 = new JComboBox<>();
        inputPanel.add(inputTextField2);

        // Button panel for actions
        JButton generateGraphButton = new JButton("Generate Graph");
        final int[] generated = {0};
        generateGraphButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                outputTextArea.setText("");
                try {
                    graph = new Graph(file_path.getSelectedItem().toString());
                    graph.generateGraph();
                    graph.showDirectedGraph();
                    displayGraphImage();
                    outputTextArea.setText("Graph generated successfully.\n");
                    Node_List = graph.getAllWords();
                    inputTextField1.removeAllItems();
                    inputTextField2.removeAllItems();
                    inputTextField1.addItem("");
                    inputTextField2.addItem("");
                    for(String node: Node_List){
                        inputTextField1.addItem(node);
                        inputTextField2.addItem(node);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                    outputTextArea.setText("Error generating graph.");
                }
                generated[0] = 1;
            }
        });

        JButton showDirectedGraphButton = new JButton("Show Directed Graph");
        showDirectedGraphButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                outputTextArea.setText("");
                    if (generated[0] == 0) {
                        outputTextArea.setText("还没有执行图的初始化，请初始化！\n");
                        return;
                    }
                    graph.ShowGraph();
                    outputTextArea.setText("Directed graph shown successfully.");
                }
        });


        JButton ShowBridgeButton = new JButton("Show Bridge Nodes");
        ShowBridgeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (generated[0] == 0) {
                    outputTextArea.setText("还没有执行图的初始化，请初始化！\n");
                    return;
                }
                outputTextArea.setText("");
                String word1 = inputTextField1.getSelectedItem().toString();
                String word2 = inputTextField2.getSelectedItem().toString();
                graph.queryBridgeWords(word1, word2);
                List<String> words =  new ArrayList<>();
                words.add(word1);
                words.add(word2);
                try {
                    graph.showBridge(words);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                displayGraphImage();
//                outputTextArea.setText(result);
            }
        });


        JButton calcShortestPathButton = new JButton("Calc Shortest Path");
        calcShortestPathButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (generated[0] == 0) {
                    outputTextArea.setText("还没有执行图的初始化，请初始化！\n");
                    return;
                }
                outputTextArea.setText("");
                String word1 = inputTextField1.getSelectedItem().toString();
                String word2 = inputTextField2.getSelectedItem().toString();
                graph.calcShortestPath(word1, word2);
                image_count = 0;
                has_calculate = true;
                path_list = graph.getShortest();
            }
        });

        final JButton[] randomWalkButton = {new JButton("Random Walk")};
        randomWalkButton[0].addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (generated[0] == 0) {
                    outputTextArea.setText("还没有执行图的初始化，请初始化！\n");
                    return;
                }
                outputTextArea.setText("");
                distance[0] = 0;
                rand_walk[0] = 0;
                current[0] = 2;
                Random_walk_result = new String("");
                try {
                    String result = graph.randomWalk();
                    Random_walk_result = result;
                    String[] temp = Random_walk_result.split(" ");
                    System.out.println(temp[0] + " ");
                    distance[0] = result.split(" ").length;
                    graph.showPathByString(result, 1);
                    displayGraphImage();
                    rand_walk[0] = 1;
                } catch (IOException ex) {
                    ex.printStackTrace();
                    outputTextArea.setText("Error performing random walk.");
                }
            }
        });


        // TODO 完成桥接词插入
        inputTextField3 = new JTextField();
        JButton bridge = new JButton("Generate Bridge");
        bridge.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (generated[0] == 0) {
                    outputTextArea.setText("还没有执行图的初始化，请初始化！\n");
                    return;
                }
                outputTextArea.setText("");
                String result = graph.generateNewText(inputTextField3.getText());
            }
        });

        JButton image_changer = new JButton("New Image");
        image_changer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(has_calculate){
                    List <String> temp = new ArrayList<>();
                    temp.add(path_list.get(image_count));
                    try {
                        graph.showPathGraph(temp);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    displayGraphImage();
                    image_count = (image_count + 1) % path_list.size();
                }
                else{
                    outputTextArea.setText("计算结果还没有生成！\n");
                    return;
                }
            }
        });


        JButton exit = new JButton("Exit");
        exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (generated[0] == 0) {
                    outputTextArea.setText("还没有执行图的初始化，请初始化！\n");
                    return;
                }
                if(rand_walk[0] == 0){
                    outputTextArea.setText("随机游走未开始\n");
                    return;
                }
                outputTextArea.setText("随机游走已取消\n");
                try {
                    graph.generateGraph();
                    graph.showDirectedGraph();
                    displayGraphImage();
                    Document.WriteToFile(Random_walk_result, 0, current[0]-1);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                System.out.println("文件已保存\n");

                Random_walk_result = new String("");
                distance[0] = 0;
                rand_walk[0] = 0;
                current[0] = 2;
            }
        });

        JButton cont =  new JButton("Continue");
        cont.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (generated[0] == 0) {
                    outputTextArea.setText("还没有执行图的初始化，请初始化！\n");
                    return;
                }
                if(rand_walk[0] == 0){
                    outputTextArea.setText("随机游走未开始\n");
                    return;
                }
                if(Random_walk_result.isEmpty()){
                    outputTextArea.setText("随机游走失败\n");
                    return;
                }
                if(distance[0] >= current[0]){
                    try {
                        String[] temp = Random_walk_result.split(" ");
                        System.out.println(temp[current[0]-1]);
                        graph.showPathByString(Random_walk_result, current[0]);
                        current[0] ++ ;
                        displayGraphImage();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
                else{
                    outputTextArea.setText("随机游走已结束: " + graph.getRandomWalkMsg()+ "\n");
                    try {
                        Document.WriteToFile(Random_walk_result, 0, current[0]-1);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    System.out.println("文件已保存\n");
                    distance[0] = 0;
                    rand_walk[0] = 0;
                    current[0] = 2;
                    Random_walk_result = new String("");
                    return;
                }

            }
        });

        JPanel buttonPanel = new JPanel(new GridLayout(4, 2, 3, 3));
        JPanel initPanel = new JPanel(new GridLayout(3,1,3,3));
        JPanel calPanel = new JPanel(new GridLayout(1, 3,1,1));
        JPanel bridgePanel = new JPanel(new GridLayout(1,2,1,1));
        JPanel randomPanel = new JPanel(new GridLayout(1,3,1,1));
        initPanel.add(generateGraphButton);
        initPanel.add(showDirectedGraphButton);
        initPanel.add(ShowBridgeButton);
        calPanel.add(calcShortestPathButton);
        calPanel.add(image_changer);
        bridgePanel.add(inputTextField3);
        bridgePanel.add(bridge);
        randomPanel.add(randomWalkButton[0]);
        randomPanel.add(exit);
        randomPanel.add(cont);
        buttonPanel.add(initPanel);
        buttonPanel.add(calPanel);
        buttonPanel.add(bridgePanel);
        buttonPanel.add(randomPanel);



        JPanel controlPanel = new JPanel(new BorderLayout());
        controlPanel.add(inputPanel, BorderLayout.NORTH);
        controlPanel.add(buttonPanel, BorderLayout.CENTER);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(controlPanel, BorderLayout.NORTH);

        outputTextArea = new JTextArea();
        outputTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputTextArea);

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(mainPanel, BorderLayout.NORTH);
        leftPanel.add(scrollPane, BorderLayout.CENTER);

        imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        imageLabel.setVerticalAlignment(JLabel.TOP);
        JScrollPane imageScrollPane = new JScrollPane(imageLabel);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, imageScrollPane);
        splitPane.setDividerLocation(450);

        add(splitPane, BorderLayout.CENTER);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                displayGraphImage();
            }
        });

        redirectSystemStreams();
    }

    private void displayGraphImage() {
        try {
            ImageIcon graphImage = new ImageIcon("./src/main/file/graph.png");
            int width = imageLabel.getWidth();
            Image scaledImage = graphImage.getImage().getScaledInstance(width, -1, Image.SCALE_SMOOTH); // Maintain aspect ratio
            graphImage = new ImageIcon(scaledImage);
            imageLabel.setIcon(graphImage);
            imageLabel.setPreferredSize(new Dimension(width, graphImage.getIconHeight())); // Set preferred size to the image dimensions
        } catch (Exception e) {
            e.printStackTrace();
            outputTextArea.setText("Error displaying graph image.\n");
        }
    }

    private void redirectSystemStreams() {
        OutputStream out = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                appendText(String.valueOf((char) b));
            }
            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                appendText(new String(b, off, len));
            }
            @Override
            public void write(byte[] b) throws IOException {
                write(b, 0, b.length);
            }
        };
        System.setOut(new PrintStream(out, true));
        System.setErr(new PrintStream(out, true));
    }

    private void appendText(final String text) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                outputTextArea.append(text);
            }
        });
    }

    public void gui_start() {
        UIManager.put( "Button.arc", 3 );
        UIManager.put( "Component.arc", 3 );
        UIManager.put( "ProgressBar.arc", 3 );
        UIManager.put( "TextComponent.arc", 3 );
        UIManager.put( "Component.arrowType", "triangle" );
        UIManager.put( "Component.focusWidth", 1 );
        UIManager.put( "ScrollBar.trackArc", 999 );
        UIManager.put( "ScrollBar.thumbArc", 999 );
        UIManager.put( "ScrollBar.trackInsets", new Insets( 2, 4, 2, 4 ) );
        UIManager.put( "ScrollBar.thumbInsets", new Insets( 2, 2, 2, 2 ) );
        UIManager.put( "ScrollBar.track", new Color( 0xe0e0e0 ) );
        FlatMacLightLaf.setup();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new GraphUI().setVisible(true);
            }
        });
    }
}
