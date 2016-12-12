import storageManager.Disk;
import storageManager.MainMemory;
import storageManager.SchemaManager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

/*
 * main class
*/

public class TestParser {
    
    public static void nodeDisplay(List<Node> list){
        for (Node tmp_node : list) {
            String tmp_attr = tmp_node.getAttr();
            System.out.println(tmp_attr);
            if (tmp_node.getChildren() != null) {
                System.out.println(">>>new list");
                nodeDisplay(tmp_node.getChildren());
                System.out.println(">>>end new list");
            }
        }
    }
    
    public static void main(String[] args){
        boolean flag = true;
        Parser parser = new Parser();
        Scanner in = new Scanner(System.in);
        //storageManager parameter
        MainMemory memory=new MainMemory();
        Disk disk=new Disk();
        SchemaManager schema_manager=new SchemaManager(memory,disk);
        Executor executor = new Executor(memory, disk, schema_manager);
        while (flag) {
            System.out.println("-------------------------TinySQL-----------------------");
            System.out.println("1. Run with file, 2. Run with commend line, 3. exit test\n");
            System.out.println("Choose a number to start:");
            String res = in.nextLine();
            switch (res) {
                case "1":
                    System.out.println("-----------------File Mode---------------");
                    System.out.println("Input \"quit\" to exit command line mode\n\nPlease input file name:"); 
                    String file_name = in.nextLine();
                    
                    while(!file_name.equalsIgnoreCase("quit")) {
                        //read commands in the file
                        File file = new File(file_name);
                        List<String> command_list = Helper.fileReader(file);
                        for (String s: command_list) {
                            Node tmp_par = parser.parse(s);
                            
                            /*//print parsing results
                            String attr = tmp_par.getAttr();
                            List<Node> list = tmp_par.getChildren();
                            System.out.println(attr);
                            nodeDisplay(list);
                            
                            System.out.println("\n");
                            //
                            */
                            executor.execute(tmp_par);
                        }
                        //system waiting
                        System.out.println("-----------------File Mode---------------");
                        System.out.println("Input \"quit\" to exit File mode\n\nPlease input file name:"); 
                        file_name = in.nextLine();
                    }
                    
                    break;
                case "2":
                    System.out.println("--------------------Command Line Mode------------------");
                    System.out.println("Input \"quit\" to exit command line mode\n\nPlease input command:"); 
                    String command =  in.nextLine();
                    while(!command.equalsIgnoreCase("quit"))
                    {
                        //System.out.println("The command you just input is "+command);
                        Node cmd_par = parser.parse(command);
                        
                        /*
                         *testing 
                        */
                        /*
                        String attr = cmd_par.getAttr();
                         List<Node> list = cmd_par.getChildren();
                         System.out.println(attr);
                         nodeDisplay(list);*/
                        executor.execute(cmd_par);
                                                
                        System.out.println("--------------------Command Line Mode------------------");
                        System.out.println("Input \"quit\" to exit command line mode\n\nPlease input command:");
                        command =  in.nextLine();
                        //iterator.execute(parser.parse(command));
                    }
                    break;
                case "3":
                    flag = false;
                    break;
                default :
                    System.out.println("Unrecognizable choise. Choose again:");
                    res = in.nextLine();
            }
        }
    }
}