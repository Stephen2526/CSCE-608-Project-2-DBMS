import storageManager.Disk;
import storageManager.MainMemory;
import storageManager.SchemaManager;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

//Write to file 
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/*
 * pack up parsing resulting and storageManager parameters
*/

public class Parameter {
    //variables
    public List<Node> para_list;
    SchemaManager schema_manager;
    MainMemory memory;
    Disk disk;
    
    //methods
    public Parameter(List<Node> list) {
        para_list = list;
    }
    public FileWriter fw;
    
}