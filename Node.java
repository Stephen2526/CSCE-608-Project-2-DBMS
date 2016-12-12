import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/*
* define a class to store parse tree node
*/

public class Node {
    public String attr; //key words or content
    public List<Node> children; // define variable children which is List<Note> type
    HashSet<String> contains_table;
    
    public Node(String attr) {
        this.attr = attr;
        children = new ArrayList<Node>();
    }
    
    //leaf
    public Node(String attr, boolean leaf) {
        this.attr = attr;
        children = null;//is leaf, children = null
    }
    
    public List<Node> getChildren() {
        return children;
    }
    
    public String getAttr() {
        return attr;
    }
    
    public void setAttr(String attr) {
        this.attr = attr;
    }
    
    public void setChildren(List<Node> children) {
        this.children = children;
    }
}
