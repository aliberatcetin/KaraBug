import java.util.ArrayList;

public class Node {

    private int order;
    private String nonTerminal;
    private Token terminal;
    private ArrayList<Node> childs;



    public Node(int order, String nonTerminal, Token terminal) {
        this.order = order;
        this.nonTerminal = nonTerminal;
        this.terminal = terminal;
        childs = new ArrayList<>();
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getNonTerminal() {
        return nonTerminal;
    }

    public void setNonTerminal(String nonTerminal) {
        this.nonTerminal = nonTerminal;
    }

    public Token getTerminal() {
        return terminal;
    }

    public void setTerminal(Token terminal) {
        this.terminal = terminal;
    }

    public ArrayList<Node> getChilds() {
        return childs;
    }

    public void addChild(Node child) {
        childs.add(child);
    }

    public void removeChild(Node node){
        childs.remove(node);
    }

}


