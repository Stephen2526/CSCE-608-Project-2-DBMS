import storageManager.Relation;
import storageManager.Tuple;

import java.util.ArrayList;
import java.util.List;

/**
 * Delete a tuple from a del_relationtion. 
 * Kelly and Stephen.
 */
public class Delete extends Guider{
    public Parameter execute(Parameter parameter) {
        List<Node> del_args = parameter.para_list;
        assert del_args != null : "ERROR: Not delete node";

        Node table = null;
        Node condition = null;
        for (Node item : del_args) {
            if (item.getAttr().equalsIgnoreCase("TABLE")) table = item;
            else if (item.getAttr().equalsIgnoreCase("EXPRESSION")) condition = item;
        }

            assert table != null;
            if (condition == null) {
                Relation del_relation = parameter.schema_manager.getRelation(table.getChildren().get(0).getAttr());
                
                
                
                del_relation.deleteBlocks(0);
            } else {
                Relation del_relation = parameter.schema_manager.getRelation(table.getChildren().get(0).getAttr());
                Expression del_condition = new Expression(condition);
                //Test.traversal(del_condition.statement, 0);
                
                int blk_num = del_relation.getNumOfBlocks();
                for (int i = 0; i < blk_num; i++) {
                    boolean deleted = false;
                    del_relation.getBlock(i, 0);
                    ArrayList<Tuple> find_tuples = parameter.memory.getBlock(0).getTuples();
                    for (int j = 0; j < find_tuples.size(); j++) {
                        if (del_condition.evaluateBoolean(find_tuples.get(j))) {
                            //System.out.println(find_tuples.get(j));
                            parameter.memory.getBlock(0).invalidateTuple(j);
                            deleted = true;
                           
                        }
                    }
                    if (deleted) {
                        del_relation.setBlock(i, 0);
                        System.out.println("DELETE COMPLETE");
                    }
                }
            }
       
        return null;
    }
}
