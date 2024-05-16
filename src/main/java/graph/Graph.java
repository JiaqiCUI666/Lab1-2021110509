package graph;
import guru.nidi.graphviz.attribute.Style;
import guru.nidi.graphviz.model.Factory;
import util.Document;
import guru.nidi.graphviz.attribute.Color;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.Link;
import guru.nidi.graphviz.model.MutableNode;
import guru.nidi.graphviz.model.MutableGraph;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.SparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import javax.swing.JFrame;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class Graph {

    public Graph(){
        source = "./src/main/file/words.txt";
    }
    public Graph(String path){
        source = path;
    }
    private static List<String> shortestPath = new ArrayList<>();   // 递归过程中使用的记录数组
    private static List<String> shortestPathList = new ArrayList<>(); // 最短路径的列表
    private static StringBuilder randomWalkPath = new StringBuilder();    // 随机游走的结果保存
    private static String randomWalkMsg = "";
    private static int shortestPathNumber = 1;              // 路径标号
    static final int INF = 0x3f3f3f3f;                      // 无穷大值
    static String source;
    static Map<String, MutableNode> nodeMap = new HashMap<>();
    static Boolean debug = true;
    static List<NodeList> graph_array = new ArrayList<>();    // 静态变量用来表示图
    public List<String> getAllWords(){
        List<String> list = new ArrayList<>();
        for(NodeList nodeList : graph_array){
            list.add(nodeList.name);
        }
        return list;
    }
    static SparseGraph<String, String> localGraph;
    public JFrame frame;
    static List<String> bridges = new ArrayList<>();

    private static NodeList getEdges(String name){
        for(NodeList list: graph_array){
            if(name.equals(list.name)){
                return list;
            }
        }
        return null;
    }

    static class Node {
        String value;
        int weight;
        Node(String value) {
            this.value = value;
            this.weight = 1;
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            else if (obj == null || getClass() != obj.getClass()) return false;
            Node o = (Node) obj;
            return value.equals(o.value);
        }
    }

    // 定义了新的数据结构，用来存储不同的桶
    static class NodeList{
        String name;
        List<Node> next_node_list;
        NodeList(String name){
            this.name = name;
            this.next_node_list = new ArrayList<>();
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            else if (obj == null || getClass() != obj.getClass()) return false;
            NodeList o = (NodeList) obj;
            return name.equals(o.name);
        }
    }

    static void resetGraph(){
        graph_array.clear();
        graph_array = new ArrayList<>();
    }

    public static void generateGraph() throws IOException {
        nodeMap.clear();
        resetGraph();
        graph_array = new ArrayList<>();
        //从文件中读取单词
        List<String> node_words = Document.ReadFromFile(source);
        if(node_words.isEmpty())return;
        for(int i= 0; i < node_words.size()-1; i++) {
            Node node = new Node(node_words.get(i));
            NodeList node_list = new NodeList(node.value);
            //如果不在列表中就加入，如果在的话就检查是否冲突并更新权值
            if (!graph_array.contains((node_list))) {
                graph_array.add(node_list);
                node_list.next_node_list.add(new Node(node_words.get(i+1)));
            }
            else{
                for (NodeList nodeList : graph_array) {
                    if (nodeList.name.equals(node.value)) {
                        Node temp = new Node(node_words.get(i + 1));
                        if(nodeList.next_node_list.contains(temp)){
                            for(Node temp_node : nodeList.next_node_list){
                                if(temp_node.value.equals(temp.value)) temp_node.weight++;
                            }
                        }
                        else{
                            nodeList.next_node_list.add(temp);
                        }
                        break;
                    }
                }

            }
        }
        Node node = new Node(node_words.getLast());
        NodeList node_list = new NodeList(node.value);
        if (!graph_array.contains((node_list))) {
            graph_array.add(node_list);
        }
    }

    // 展示有向图
    public static void showDirectedGraph() throws IOException {
        nodeMap.clear();
        File outputFIle = new File("./src/main/file/graph.png");
        if(outputFIle.exists()){
            outputFIle.delete();
        }
        MutableGraph graph = Factory.mutGraph(new Timer().toString()).setDirected(true);
        for (NodeList nodeList : Graph.graph_array) {
            MutableNode parentNode = nodeMap.computeIfAbsent(nodeList.name, name -> createNode(name, getColor(0)));
            for (Node node : nodeList.next_node_list) {
                MutableNode childNode = nodeMap.computeIfAbsent(node.value, name -> createNode(name, getColor(0)));
                graph.add(parentNode.addLink(Link.to(childNode).with(guru.nidi.graphviz.attribute.Label.of(Integer.toString(node.weight)))));
            }
        }
//        System.out.println(graph);
        Graphviz.fromGraph(graph).width(1000).render(Format.PNG).toFile(outputFIle);
        Graphviz.releaseEngine();
        return;
    }

    public void showBridge(List<String> words) throws IOException {
        nodeMap.clear();
        File outputFIle = new File("./src/main/file/graph.png");
        if(outputFIle.exists()){
            outputFIle.delete();
        }
        MutableGraph graph = Factory.mutGraph(new Timer().toString()).setDirected(true);
        for (NodeList nodeList : Graph.graph_array) {
            MutableNode parentNode;
            if(bridges.contains(nodeList.name)){
                parentNode = nodeMap.computeIfAbsent(nodeList.name, name -> createNode(name, Color.RED));
            }
            else if(words.contains(nodeList.name)){
                parentNode = nodeMap.computeIfAbsent(nodeList.name, name -> createNode(name, Color.BLUE));
            }
            else{
                parentNode = nodeMap.computeIfAbsent(nodeList.name, name -> createNode(name, getColor(0)));
            }
            for (Node node : nodeList.next_node_list) {
                MutableNode childNode;
                if(bridges.contains(node.value)){
                    childNode = nodeMap.computeIfAbsent(node.value, name -> createNode(name, Color.RED));
                }
                else if(words.contains(node.value)){
                    childNode = nodeMap.computeIfAbsent(node.value, name -> createNode(name, Color.BLUE));
                }
                else{
                    childNode = nodeMap.computeIfAbsent(node.value, name -> createNode(name, getColor(0)));
                }
                graph.add(parentNode.addLink(Link.to(childNode).with(guru.nidi.graphviz.attribute.Label.of(Integer.toString(node.weight)))));
            }
        }
        Graphviz.fromGraph(graph).width(1000).render(Format.PNG).toFile(outputFIle);
        Graphviz.releaseEngine();
        return;
    }

    public void showPathGraph(List<String> pathlist) throws IOException {
            MutableNode parentNode;
            MutableNode childNode;
            nodeMap.clear();
            File outputFIle = new File("./src/main/file/graph.png");
            if (outputFIle.exists()) {
                outputFIle.delete();
            }
            int count = 3;
            MutableGraph graph = Factory.mutGraph(new Timer().toString()).setDirected(true);
        for (String path : pathlist) {
            String[] words = path.split(" ");
            for (int i = 0; i < words.length; i++) {
                int finalCount = count;
                if(i == 0){
                    nodeMap.put(words[i],createNode(words[i], getColor(finalCount - 1)));
                }
                else if(i == words.length - 1){
                    nodeMap.put(words[i],createNode(words[i], getColor(finalCount + 1)));
                }
                else{
                    nodeMap.put(words[i],createNode(words[i], getColor(finalCount)));
                }
            }
        }
            for (NodeList nodeList : Graph.graph_array) {
                parentNode = nodeMap.computeIfAbsent(nodeList.name, name -> createNode(name, getColor(0)));
                for (Node node : nodeList.next_node_list) {
                    childNode = nodeMap.computeIfAbsent(node.value, name -> createNode(name, getColor(0)));
                    graph.add(parentNode.addLink(Link.to(childNode).with(guru.nidi.graphviz.attribute.Label.of(Integer.toString(node.weight)))));
                }
            }

            Graphviz.fromGraph(graph).width(1000).render(Format.PNG).toFile(outputFIle);
            Graphviz.releaseEngine();
    }

    public List<String> getShortest(){
        return shortestPathList;
    }

    public void showPathByString(String path, int index) throws IOException {
        nodeMap.clear();
        File outputFIle = new File("./src/main/file/graph.png");
        if(outputFIle.exists()){
            outputFIle.delete();
        }

        MutableGraph graph = Factory.mutGraph(new Timer().toString()).setDirected(true);
        String[] words = path.split(" ");
        words = Arrays.copyOfRange(words, 0, index);
        if(words.length == 1){
            nodeMap.computeIfAbsent(words[0], name -> createNode(name, getColor(2)));
            nodeMap.put(words[0],createNode(words[0], getColor(2)));
        }
        for(int i=0; i<words.length-1;i++){
            if(i == 0){
                nodeMap.computeIfAbsent(words[i], name -> createNode(name, getColor(2)));
                nodeMap.put(words[i],createNode(words[i], getColor(2)));
            }
            else{
                nodeMap.computeIfAbsent(words[i], name -> createNode(name, getColor(2)));
                nodeMap.put(words[i],createNode(words[i], getColor(2)));
            }
            if(i == words.length - 2){
                nodeMap.computeIfAbsent(words[i+1], name -> createNode(name, getColor(1)));
                nodeMap.put(words[i+1],createNode(words[i+1], getColor(1)));
            }
            else{
                nodeMap.computeIfAbsent(words[i+1], name -> createNode(name, getColor(2)));
                nodeMap.put(words[i+1],createNode(words[i+1], getColor(2)));
            }
        }
        for (NodeList nodeList : Graph.graph_array) {
            MutableNode parentNode = nodeMap.computeIfAbsent(nodeList.name, name -> createNode(name, getColor(0)));
            for (Node node : nodeList.next_node_list) {
                MutableNode childNode = nodeMap.computeIfAbsent(node.value, name -> createNode(name, getColor(0)));
                graph.add(parentNode.addLink(Link.to(childNode).with(guru.nidi.graphviz.attribute.Label.of(Integer.toString(node.weight)))));
            }
        }
        Graphviz.fromGraph(graph).width(1000).render(Format.PNG).toFile(outputFIle);
        Graphviz.releaseEngine();
    }

    public static Color getColor(int weight){
        return switch (weight % 6) {
            case 0 -> Color.WHITESMOKE;
            case 1 -> Color.LIGHTPINK;
            case 2 -> Color.GREEN;
            case 3 -> Color.PINK;
            case 4 -> Color.RED;
            case 5 -> Color.PINK;
            default -> Color.BLACK;
        };
    }

    private static MutableNode createNode(String name, Color color) {
        return Factory.mutNode(name).add(Style.FILLED, color);
    }

    public void ShowGraph(){
        createWindow();
        init_graph();
        show_graph(frame);
    }

    public void createWindow() {
        frame = new JFrame("GraphPoetView");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    public static void init_graph(){
        localGraph = new SparseGraph<String, String>();
        for(NodeList node_list : graph_array){
            localGraph.addVertex(node_list.name);
            for(Node node : node_list.next_node_list){
                localGraph.addEdge( node_list.name + "-" + Integer.toString(node.weight) + "-" + node.value, node_list. name,node.value, EdgeType.DIRECTED);
            }
        }
    }

    public void show_graph(JFrame frame){
        Layout<String, String> layout = new CircleLayout<String,String>(localGraph);
        layout.setSize(new Dimension(600, 600));
        BasicVisualizationServer<String, String> vv =
                new BasicVisualizationServer<String, String>(layout);
        vv.setPreferredSize(new Dimension(650, 650));
        vv.getRenderContext().setVertexShapeTransformer(s -> new Ellipse2D.Double(-15, -15, 62, 62));
        vv.getRenderContext().setVertexFillPaintTransformer(s -> java.awt.Color.YELLOW);
        vv.getRenderContext().setEdgeStrokeTransformer(s -> new BasicStroke());
        vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
        vv.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller());
        vv.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);
        frame.getContentPane().add(vv);
        frame.pack();
        frame.setVisible(true);
    }

    public static String queryBridgeWords(String word1, String word2){
        String phrase = "The bridge words from '"+word1+"' to '" +word2+"'";
        String error1 = "No bridge words from '"+word1+"' to '"+word2+"'!";
        String error2 = "No word1 or word2 in the graph!";
        NodeList bridge_nodes = getEdges(word1);
        bridges.clear();
        // 更新桥接词数组
        if (bridge_nodes != null) {
            for(Node node : bridge_nodes.next_node_list){
                NodeList end_list = getEdges(node.value);
                if (end_list != null && end_list.next_node_list.contains(new Node(word2))) {
                    bridges.add(node.value);
                }
            }
        }
        // 开始控制输出
        else{
            if(debug){
                System.out.println(error1);
            }
            return null;
        }
        if(bridges.isEmpty()) {
            if(debug) {
                System.out.println(error1);
            }
            return null;
        }
        else if(bridges.size() == 1){
            if(debug){
                System.out.println(phrase  + " is: " + bridges.getLast());
            }
            return randomSelectKey(bridges);
        }
        phrase += " are: ";
        int i;
        for(i = 0 ; i < bridges.size()-1 ; i++){
            phrase = phrase +"'"+bridges.get(i) + "', ";
        }
        if(bridges.size() == 2) phrase += " '" + bridges.get(i) + "'.";
        else
            phrase += "and '" + bridges.get(i)+ "'.";
        if(debug){
            System.out.println(phrase);
        }
        return randomSelectKey(bridges);
    }

    public static String generateNewText(String inputText){
        if (inputText == null || inputText.isEmpty()){
            System.out.println("输入内容为空！");
            return null;
        }
        // 首先将输入的文本划分成为单词串，所有的非字母字符（空格、标点、特殊符号等会被忽略）
        String[] wordsArray = inputText.split("\\P{Alpha}+");
        // 创建一个结果字符串，用来保存结果
        StringBuilder outputText = new StringBuilder();
        // 过程中无需打印出桥接词
        debug = false;
        // 遍历单词串，用于增加桥接词
        for(int i = 0; i < wordsArray.length - 1; i++){
            // 存入基本原本的单词，单词之间用空格分开
            outputText.append(wordsArray[i]).append(" ");
            // 查询桥接词
            String bridgeWord = queryBridgeWords(wordsArray[i].toLowerCase(), wordsArray[i + 1].toLowerCase());
            if (bridgeWord != null){
                // 如果查询桥接词不是空的，那么就加入这个桥接词
                outputText.append(bridgeWord).append(" ");
            }
        }
        // 收尾工作，把最后一个单词放入串中
        outputText.append(wordsArray[wordsArray.length - 1]);
        // 输出文本
        System.out.println(outputText);
        debug = true;
        return outputText.toString();
    }

    private static void deepFirstSearch(String word1, String word2, int weight, int targetWeight){
        // 源点的邻接表项
        NodeList chooseNodeList = getEdges(word1);
        // 记录单词
        shortestPath.add(word1);
        if (targetWeight < weight){
            shortestPath.removeLast();
            return;
        }

        if (targetWeight == weight && Objects.equals(word1, word2)){
            System.out.println(shortestPathNumber + ". " + String.join(" -> ", shortestPath));
            shortestPathList.add(String.join(" ", shortestPath));
            shortestPath.removeLast();
            shortestPathNumber += 1;
            return;
        }

        if (targetWeight >= INF){
            System.out.println("从 " + word1 + " 到 " + word2 + " 不存在可达路径");
            return;
        }

        assert chooseNodeList != null;
        for(Node node : chooseNodeList.next_node_list){
            deepFirstSearch(node.value, word2, weight + node.weight, targetWeight);
        }
        shortestPath.removeLast();
    }

    public static String calcShortestPath(String word1, String word2){
        // 利用 Dijkstra 算法实现
        // 节点键值表
        List<String> keyList = new ArrayList<String>(nodeMap.keySet());
        // 如果没有源点单词就随机选取第一个单词
        if (word1 == null || word1.isEmpty()){
            word1 = randomSelectKey(keyList);
            System.out.println("未输入源点单词，随机选择单词： " + word1);
        }
        // 如果只输入了一个单词就随机选取第二个单词
        Boolean haveWord2 = true;
        if (word2 == null || word2.isEmpty()){
            // word2 = randomSelectKey(keyList);
            // System.out.println("未输入目标单词，随机选择单词： " + word2);
            haveWord2 = false;
        }
        // 创建一个访问表
        HashMap<String, Boolean> visitTable = new HashMap<>();
        // 创建距离数组
        HashMap<String, Integer> distanceTable = new HashMap<>();
        // 创建相应的邻近节点表，用于保存前驱节点表
        // HashMap<String, String> closeTable = new HashMap<>();
        // 所选点的邻接表项
        NodeList chooseNodeList = getEdges(word1);
        // 保证每次调用的结果都是最新的
        shortestPathList.clear();
        if (chooseNodeList == null){
            System.out.println("返回的表项是空的！原图中可能没有这个点！\n");
            return null;
        }
        // 初始化
        for (NodeList node : graph_array){
                visitTable.put(node.name, false);
                distanceTable.put(node.name, INF);
        }
        for (Node node : chooseNodeList.next_node_list){
                distanceTable.put(node.value, node.weight);
                // closeTable.put(node.value, word1);
        }
        visitTable.put(word1, true);
        // 开始进行算法
        for (NodeList oriNode : graph_array) { // 扫描所有的节点
            String closestNode = ""; // 距离最近的节点名字
            int distance = INF;
            // 获取最邻近的节点
            for (String nodeName : distanceTable.keySet()){
                int theWeight = distanceTable.get(nodeName);
                if (distance > theWeight && !visitTable.get(nodeName)){
                    closestNode = nodeName;
                    distance = theWeight;
                }
            }
            // 对于出度为0的点，不会有最短路径的更新
            if (Objects.equals(closestNode, "")){
                continue;
            }
            // 这个点被包含进去了
            visitTable.put(closestNode, true);
            NodeList reachNodeList = getEdges(closestNode);
            if (reachNodeList == null){
                System.out.println("返回的表项是空的！更新过程中的原图中可能没有这个点！\n");
                return null;
            }
            // 根据这个点更新距离表
            for (Node node : reachNodeList.next_node_list){
                if (!visitTable.get(node.value) && distanceTable.get(closestNode) + node.weight < distanceTable.get(node.value) ){
                    distanceTable.put(node.value, distanceTable.get(closestNode) + node.weight);
                    // closeTable.put(node.value, closestNode);
                }
            }
        }
        // 源点的距离设置为 0
        distanceTable.put(word1, 0);
        if (haveWord2){
            // 得到最短路径长度
            int shortestDistance = distanceTable.get(word2);
            // 打印所有的最短路径
            if (shortestDistance < INF){
                System.out.println("从 " + word1 + " 到 " + word2 + " 所有的最短路径是：");
                shortestPathNumber = 1;
                deepFirstSearch(word1, word2, 0, shortestDistance);
                System.out.println("最短路径长度是：" + shortestDistance);
                return shortestPathList.getFirst();
            }
            else{
                System.out.println("从 " + word1 + " 到 " + word2 + " 不存在可达路径");
                return null;
            }
        }
        else{
            for (String key : keyList){
                // 得到最短路径长度
                int shortestDistance = distanceTable.get(key);
                // 打印所有的最短路径
                if (shortestDistance < INF){
                    System.out.println("从 " + word1 + " 到 " + key + " 所有的最短路径是：");
                    shortestPathNumber = 1;
                    deepFirstSearch(word1, key, 0, shortestDistance);
                    System.out.println("最短路径长度是：" + shortestDistance);
                }
                else{
                    System.out.println("从 " + word1 + " 到 " + key + " 不存在可达路径");
                }
            }
        }
        return shortestPathList.getFirst();
    }

    private static String randomSelectKey(List<String> keyList){
        // 输入一个键列表，随机返回一个键
        if (keyList.isEmpty()) {
            return null;
        }
        Random rand = new Random();
        return keyList.get(rand.nextInt(keyList.size()));
    }

    public static String randomWalk() throws IOException{
        // 初始化一个访问矩阵，记录一条边是否被访问过
        List<String> keyList = new ArrayList<String>(nodeMap.keySet());
        HashMap<String, HashMap<String, Boolean>> accessMatrix = new HashMap<>();
        // 初始化结果
        randomWalkPath = new StringBuilder();
        for (String firstWord : keyList){
            accessMatrix.put(firstWord, new HashMap<>());
            for (String secondWord : keyList){
                accessMatrix.get(firstWord).put(secondWord, false);
            }
        }
        randomWalkMsg = "";
        // 随机选取一个开始点
        String beginNode = randomSelectKey(keyList);
        // 输出随机游走的信息
        for(;;){
            // 输出这个点
            if (beginNode == null){
                System.out.println("图中没有单词节点！");
                return null;
            }
            // writer.write(beginNode);
            randomWalkPath.append(beginNode);
            // 从图中查找相邻的点
            List<String> nextWordsList = new ArrayList<String>();
            for (Node node: getEdges(beginNode).next_node_list){
                nextWordsList.add(node.value);
            }
            // 这个点的出度为0，停止游走
            if (nextWordsList.isEmpty()){
                // writer.write("\n");
                // System.out.println("随机游走的序列是：");
                // System.out.println(randomWalkPath);
                // System.out.println("顶点" + beginNode + "没有出边而停止");
                randomWalkMsg = "顶点" + beginNode + "没有出边而停止";
                break;
            }
            // 随机选择一个点
            String nextNode = randomSelectKey(nextWordsList);
            if (nextNode == null){
                System.out.println("图中没有单词节点！");
                return null;
            }
            // 这个边已经访问过了，停止游走
            if (accessMatrix.get(beginNode).get(nextNode)){
                // writer.write(" ");
                // System.out.println("边(" + beginNode + ", " + nextNode + ")访问重复而停止");
                randomWalkMsg = "边(" + beginNode + ", " + nextNode + ")访问重复而停止";
                break;
            }
            // 标记这个边已经访问过
            accessMatrix.get(beginNode).put(nextNode, true);
            beginNode = nextNode;
            // writer.write(" ");
            randomWalkPath.append(" ");
        }
        // System.out.println("随机游走的结果保存在: " + randomWalkResult);
        return randomWalkPath.toString();
    }

    public String getRandomWalkMsg(){
        return randomWalkMsg;
    }

    public static void main(String[] args) throws IOException {
        source = "./src/main/file/words.txt";
        generateGraph();
        showDirectedGraph();
        calcShortestPath("To", "and");
        randomWalk();
        return;
    }
}