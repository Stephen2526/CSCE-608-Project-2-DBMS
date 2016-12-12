import java.util.*;

public class Parser {
    
    HashMap<String, Integer> priority;
    public Parser() {
        priority = new HashMap<String, Integer>();
        priority.put("OR", 0);
        priority.put("AND",1);
        priority.put("=", 2);
        priority.put(">", 2);
        priority.put("<", 2);
        priority.put("+", 3);
        priority.put("-", 3);
        priority.put("*", 4);
        priority.put("/", 4);
    }
    
    public Node parse(String cmd) {
    System.out.println("Start to parse query: "+ cmd +"...\n");
    return parse(cmd.split(" "), "INI");
    }
    
    public Node parse(String[] cmd_array, String key) {
        Node node = null;
        /*
        *parse the first key words
        */
        if (key.equalsIgnoreCase("INI")) {
            
            
            if (cmd_array[0].equalsIgnoreCase("CREATE")) {
                node = parse(cmd_array, "CREATE");
            }
            
            if (cmd_array[0].equalsIgnoreCase("INSERT")) {
                node = parse(cmd_array, "INSERT");
            }
            
            if (cmd_array[0].equalsIgnoreCase("DELETE")) {
                node = parse(cmd_array, "DELETE");
            }
            
            if (cmd_array[0].equalsIgnoreCase("SELECT")) {
                node = parse(cmd_array, "SELECT");
            }
            
            if (cmd_array[0].equalsIgnoreCase("DROP")) {
                node = new Node("DROP");
                node.getChildren().add(leaf(cmd_array[2],"TABLE"));
            }
            
            
        }//end if ini
        
        /*
        *parse the remainings after knows the leading one
        */
        
        //CREATE TABLE course (sid INT, homework INT, project INT, exam INT, grade STR20)
        if (key.equalsIgnoreCase("CREATE")) {
            Node tmp_node = new Node(key);
            tmp_node.getChildren().add(leaf(cmd_array[2],  "TABLE"));
            tmp_node.getChildren().add(parse(Arrays.copyOfRange(cmd_array, 3, cmd_array.length), "CREATE_ATTR"));
            node = tmp_node;
        }
        
        //(sid INT, homework INT, project INT, exam INT, grade STR20)
        if (key.equalsIgnoreCase("CREATE_ATTR")) {
            Node tmp_node =  new Node(key);
            if (cmd_array.length % 2 != 0) {
                System.out.println("ERROR! check attribute list format");
            }
            
            for (int i = 0; i < cmd_array.length / 2; i++) {
                tmp_node.getChildren().add(parse(Arrays.copyOfRange(cmd_array, 2*i, 2*i+2), "ATTR_DETAIL"));
            }
            node = tmp_node;
        }
    
        //(sid INT, //homework INT, //project INT, //exam INT, //grade STR20)
        if (key.equalsIgnoreCase("ATTR_DETAIL")) {
            Node tmp_node = new Node(key);
            String attr_name = cmd_array[0];
            String attr_type = cmd_array[1];
            
            if (attr_name.charAt(0) == '(' || attr_name.charAt(0) == ',') {
                attr_name = attr_name.substring(1);
            }
            if (attr_name.charAt(attr_name.length()-1) == ',' || attr_name.charAt(attr_name.length()-1) == ')') {
                attr_name = attr_name.substring(0, attr_name.length()-1);
            }
            if (attr_type.charAt(attr_type.length()-1) == ',' || attr_type.charAt(attr_type.length()-1) == ')') {
                attr_type= attr_type.substring(0, attr_type.length()-1);
            }
            
            tmp_node.getChildren().add(leaf(attr_name, "ATTR_NAME"));
            tmp_node.getChildren().add(leaf(attr_type, "ATTR_TYPE"));
            
            node = tmp_node;
        }
        
        //INSERT INTO course (sid, homework, project, exam, grade) VALUES (1, 99, 100, 100, "A")
        //INSERT INTO course (sid, homework, project, exam, grade) SELECT * FROM course
        if (key.equalsIgnoreCase("INSERT")) {
            int value_ind = 0;
            int select_ind = 0;
            //get index of VALUES and SELECT
            for (int i = 0; i < cmd_array.length; i++) {
                if (cmd_array[i].equalsIgnoreCase("VALUES")) value_ind = i;
                if (cmd_array[i].equalsIgnoreCase("SELECT")) select_ind = i;
            }
            
            //parse table, column and values
            if (value_ind > 0) {
                Node tmp_node = new Node(key);
                tmp_node.getChildren().add(leaf(cmd_array[2], "TABLE"));
                tmp_node.getChildren().add(parse(Arrays.copyOfRange(cmd_array, 3, value_ind), "ATTR_LIST"));
                tmp_node.getChildren().add(parse(Arrays.copyOfRange(cmd_array, value_ind+1, cmd_array.length), "VALUES"));
                node = tmp_node;
            }
            
            if (select_ind > 0) {
                Node tmp_node =  new Node(key);
                tmp_node.getChildren().add(leaf(cmd_array[2],"TABLE"));
                tmp_node.getChildren().add(parse(Arrays.copyOfRange(cmd_array, 3, select_ind), "ATTR_LIST"));
                tmp_node.getChildren().add(parse(Arrays.copyOfRange(cmd_array,select_ind,cmd_array.length),"SELECT"));
                node = tmp_node; 
            }
        
        }
        
        if (key.equalsIgnoreCase("VALUES")) 
        {
                Node tmp_node = new Node(key);
                
                
                for (String elem_in_cmd: cmd_array)
                {
                    String item = trim(elem_in_cmd);
                    tmp_node.getChildren().add(leaf(item,"VALUE"));
                }
                node = tmp_node;
        }
        
        if (key.equalsIgnoreCase("SELECT")) {
            Node tmp_node = new Node(key);
            int pos_F = 0, pos_W = 0, pos_OB = 0;
            int scan=1;
            while(scan < cmd_array.length)
            {
                if (cmd_array[scan].equalsIgnoreCase("FROM")) 
                {
                    pos_F = scan;
                    Node tmp_attr = parse(Arrays.copyOfRange(cmd_array,1,pos_F),"ATTR_LIST");
                    tmp_node.getChildren().add(tmp_attr);
                }
                else if (cmd_array[scan].equalsIgnoreCase("WHERE")) 
                {
                    pos_W = scan;
                    Node tmp_from = parse(Arrays.copyOfRange(cmd_array, pos_F+1,pos_W),"FROM");
                    tmp_node.getChildren().add(tmp_from);
                }
                else if (cmd_array[scan].equalsIgnoreCase("ORDER")) 
                {
                    pos_OB = scan;
                    if (pos_W != 0)
                    {
                        Node tmp_where = parse(Arrays.copyOfRange(cmd_array, pos_W+1, pos_OB),"WHERE");
                        tmp_node.getChildren().add(tmp_where);
                    }
                    else if (pos_W == 0)
                    {
                        Node tmp_from = parse(Arrays.copyOfRange(cmd_array, pos_F+1, pos_OB),"FROM");
                        tmp_node.getChildren().add(tmp_from);
                    }
                    Node tmp_order = parse(Arrays.copyOfRange(cmd_array, pos_OB+2, cmd_array.length),"ORDER");
                        tmp_node.getChildren().add(tmp_order);
                }
                scan++;
            }
            if (pos_OB == 0 && pos_W !=0)
            {
                 Node tmp_where = parse(Arrays.copyOfRange(cmd_array, pos_W+1, cmd_array.length),"WHERE");
                 tmp_node.getChildren().add(tmp_where);
            }
            if (pos_OB == 0 && pos_W == 0)
            {
                Node tmp_from = parse(Arrays.copyOfRange(cmd_array, pos_F+1, cmd_array.length),"FROM"); 
                tmp_node.getChildren().add(tmp_from);
            }
            node = tmp_node;   
        }
        
        if (key.equalsIgnoreCase("FROM")) {
            Node tmp_node = new Node(key);
            int scan = 0;
            for (scan = 0; scan<cmd_array.length;scan++)
            {
                String elem_in_cmd = cmd_array[scan];
                if(elem_in_cmd.charAt(elem_in_cmd.length()-1)==',')
                {
                    elem_in_cmd = elem_in_cmd.substring(0,elem_in_cmd.length()-1);
                }
                tmp_node.getChildren().add(leaf(elem_in_cmd,"TABLE"));
            }
            node = tmp_node;
        }
        
        if (key.equalsIgnoreCase("WHERE")) {
            Node tmp_node = new Node("EXPRESSION");  
            //System.out.println("condition returns: " + condition(cmd_array).getAttr());
            tmp_node.getChildren().add(condition(cmd_array));
            
            node = tmp_node;
        }
        
        if(key.equalsIgnoreCase("ORDER")) {
            Node tmp_node = new Node("ORDER");
            tmp_node.getChildren().add(leaf(cmd_array[0],"ATTR_NAME"));
            node = tmp_node;
        }
        
        if(key.equalsIgnoreCase("ATTR_LIST")) {
            Node tmp_node = new Node(key);
            if (cmd_array[0].equalsIgnoreCase("DISTINCT")) {
                tmp_node.getChildren().add( new Node("DISTINCT"));
                Node attr = tmp_node.getChildren().get(0);
                for (int i=1; i<cmd_array.length; i++)
                {
                    String elem_in_cmd = cmd_array[i];
                    if (elem_in_cmd.length()>0)
                    {
                        attr.getChildren().add(leaf(elem_in_cmd.charAt(elem_in_cmd.length()-1)==','?elem_in_cmd.substring(0,elem_in_cmd.length()-1):elem_in_cmd,"ATTR_NAME"));
                       
                    }
                }
            }
            else
            {
                for (String elem_in_cmd : cmd_array) {
                    if(elem_in_cmd.length() > 0) {
                        String item = elem_in_cmd;
                         //System.out.println("item in cmd = "+ item + " elem length:" + elem_in_cmd.length()+ "cmd_array"+cmd_array.length);
                        if(item.charAt(0) == '(') 
                        {
                            item = item.substring(1, item.length());
                        }
                        if(item.charAt(item.length()-1)==')'||item.charAt(item.length()-1)==',') 
                        {
                            item = item.substring(0, item.length()-1);
                        }
                        tmp_node.getChildren().add(leaf(item, "ATTR_NAME"));
                    }
                }    
            }
            node = tmp_node;
        }
        
        if(key.equalsIgnoreCase("DROP"))
        {
            Node tmp_node = new Node(key);
            tmp_node.getChildren().add(leaf(cmd_array[2],"TABLE"));
            node = tmp_node;
        }
        
        if(key.equalsIgnoreCase("DELETE"))
        {
            Node tmp_node = new Node("DELETE");
            String table = cmd_array[2];
            tmp_node.getChildren().add(leaf(table,"TABLE"));
            if(cmd_array.length > 3 && cmd_array[3].equalsIgnoreCase("WHERE"))
            {
                tmp_node.getChildren().add(parse(Arrays.copyOfRange(cmd_array, 4, cmd_array.length), "WHERE"));
            }
            node = tmp_node;
        }
        return node;
    }
    
    
    public Node leaf(String str, String key) {
        if (key.equalsIgnoreCase("ATTR_NAME")) {
            Node tmp_node = new Node("ATTR_NAME");
            String[] name = str.split("\\.");
            for (String elem_in_name : name) {
                tmp_node.getChildren().add(new Node(elem_in_name, true));
            }
            return tmp_node;
        } 
        else if(key.equalsIgnoreCase("TABLE"))
        {
            Node tmp_node = new Node("TABLE");
            tmp_node.getChildren().add(new Node(str,true));
            return tmp_node;
        }
        else {
            //System.out.println("pending...");
            Node tmp_node = new Node(key);
            tmp_node.getChildren().add(new Node(str, true));
            return tmp_node;
        }
        
    }
    
    public Node condition(String[] cmd_all)
    {
        Stack<Node> cmd_list_stack = new Stack<Node>();
        int i = 0;
        for (i = 0; i < cmd_all.length; i++)
        {
            if (priority.containsKey(cmd_all[i]))
            {
                if(cmd_list_stack.size() >= 3)
                {
                    Node last = cmd_list_stack.pop();
                    if(priority.get(cmd_all[i]) >= priority.get(cmd_list_stack.peek().getAttr()))
                    {
                        cmd_list_stack.push(last);
                        cmd_list_stack.push(new Node(cmd_all[i]));
                    }
                    else
                    {
                        while (cmd_list_stack.size()>0 && priority.get(cmd_list_stack.peek().getAttr())>priority.get(cmd_all[i]))
                        {
                            Node operation = cmd_list_stack.pop();
                            
                            Node operation2 = cmd_list_stack.pop();
                            operation.getChildren().add(operation2);
                            operation.getChildren().add(last);
                            last = operation;
                        }
                        
                        cmd_list_stack.push(last);
                        cmd_list_stack.push(new Node(cmd_all[i]));
                    }
                }
                else
                {
                    cmd_list_stack.push(new Node(cmd_all[i]));
                }
            }
            else if(integer(cmd_all[i]))
            {
                cmd_list_stack.push(leaf(cmd_all[i], "INT"));
            }
            else if(cmd_all[i].charAt(0) == '"')
            {
                cmd_list_stack.push(leaf(cmd_all[i].substring(1, cmd_all[i].length()-1),"STRING"));
            }
            else if(cmd_all[i].charAt(0) == '(')
            {
                String[] sub_where_operation = new String[3];
                sub_where_operation[0]=trim(cmd_all[i]);
                sub_where_operation[1]=cmd_all[i+1];
                sub_where_operation[2]=trim(cmd_all[i+2]);
                cmd_list_stack.push(condition(sub_where_operation));
                i=i+2;
            }
            else
            {
                cmd_list_stack.push(leaf(cmd_all[i],"ATTR_NAME"));
            }
        }
        
        if(cmd_list_stack.size() >= 3)
        {
            Node current_elem = cmd_list_stack.pop();
            
            while(cmd_list_stack.size() >= 2)
            {
                Node operation = cmd_list_stack.pop();
                operation.getChildren().add(cmd_list_stack.pop());
                operation.getChildren().add(current_elem);
                current_elem = operation;
            }
            return current_elem;
        }
        else
        {
            return cmd_list_stack.peek();
        }
    }
    
    public String trim(String substring)
    {
        String str = substring;
        if(str.length() == 0) return null;
        if (str.charAt(0)=='(') str = str.substring(1);
        if (str.charAt(0)=='"') str = str.substring(1);
        if (str.charAt(str.length()-1)==')') str = str.substring(0, str.length()-1);
        if (str.charAt(str.length()-1)==',') str = str.substring(0, str.length()-1);
        if (str.charAt(str.length()-1)=='"') str = str.substring(0, str.length()-1);
        return str;
    }
    
    public static boolean integer(String substring)
    {
        try 
        {
            Integer.parseInt(substring);
            return true;
        }
        catch (NumberFormatException err) 
        {
            return false;
        }
    }
}

