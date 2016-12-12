import storageManager.Disk;
import storageManager.MainMemory;
import storageManager.SchemaManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/*
*This class is used for execute one tiny-SQL command
*/

public class Executor {
    //members and constructor
    MainMemory memory;
    Disk disk;
    SchemaManager schema_manager;
    
    //A guider map help executor guide to certian key-word executor
    HashMap<String, Guider> executor_guider_map = new HashMap<String, Guider>();
    
    public Executor(MainMemory memory, Disk disk, SchemaManager schema_manager) {
        //initial StorageManager parameters
        this.memory = memory;
        this.disk = disk;
        this.schema_manager = schema_manager;
        
        //set up the guider map
        executor_guider_map.put("CREATE", new Create());
        executor_guider_map.put("INSERT", new Insert());
        executor_guider_map.put("SELECT", new Select());
        executor_guider_map.put("DELETE", new Delete());
        executor_guider_map.put("DROP", new Drop());
        
        //copy guider map to Guider class
        Guider.guider_map = executor_guider_map;
    }
    
    /*
    *execute one command
    *input: Node from parser
    */
    public void execute(Node node) {
        //record initial simulated time and dick I/O
        double simu_time = disk.getDiskTimer();
        long simu_IO = disk.getDiskIOs();
        
        //set up parameter
        List<Node> tmp_list = node.getChildren();
        String tmp_state = node.getAttr();
        Parameter parameter = new Parameter(tmp_list);
        parameter.schema_manager = this.schema_manager;
        parameter.memory = this.memory;
        parameter.disk = this.disk;
        
        Guider guider = executor_guider_map.get(tmp_state);
        guider.execute(parameter);
        
        //output simulated elapse time and disk IO
        System.out.printf("Simulated processing time = %.2f ms\n"  ,(disk.getDiskTimer()-simu_time));
        System.out.println("Simulated Disk I/Os = " + (disk.getDiskIOs()-simu_IO));
        System.out.println("\n");
    }    
}
