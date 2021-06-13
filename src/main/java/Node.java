import java.util.ArrayList;
import java.util.Collections;

public class Node implements Comparable<Node>{

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

    public void clearChild(){
        childs.clear();
    }

    public void dfs(Node node){
        if(node.getTerminal()!=null){
            System.out.print(node.getTerminal().getValue()+" ");
            return;
        }

        ArrayList<Node> childs = node.getChilds();
        Collections.sort(childs);
        for(Node n:childs){
            dfs(n);
        }
    }

    public void print(){
        dfs(this);
        System.out.println();
    }

    @Override
    public int compareTo(Node o) {
        if(this.getOrder()>o.getOrder()) return 1;
        else if(this.getOrder()==o.getOrder()) return  0;
        return -1;
    }


}


