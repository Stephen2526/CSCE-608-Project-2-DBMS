import storageManager.*;

import java.util.*;

/*
 * INSERT executor
 * INSERT INTO r (a, b) VALUES (0, 0)
*/

public class Insert extends Guider {
    @Override
    public Parameter execute(Parameter parameter) {
        assert parameter.schema_manager != null;
        assert parameter.memory != null;

        List<Node> list = parameter.para_list;
        List<String> col_name_list;
        ArrayList<String> value_list = new ArrayList<String>();
        String relation_name = null;
        //Parameter col = new Parameter();
        List<Node> col_list = null;

        for (Node sub_node: list) {
            if (sub_node.getAttr().equalsIgnoreCase("TABLE")) {
                relation_name = sub_node.getChildren().get(0).getAttr();
            }
            else if (sub_node.getAttr().equalsIgnoreCase("ATTR_LIST")) {
                col_list =sub_node.getChildren();
            } 
            else if (sub_node.getAttr().equalsIgnoreCase("VALUES")) {
                Relation relation = parameter.schema_manager.getRelation(relation_name);
                Tuple newTuple = relation.createTuple();
                //handle error
                assert col_list != null : "ERROE: column list is null";
                
                int ind = 0;
                for (Node tmp_node : col_list) {
                    assert tmp_node.getAttr().equalsIgnoreCase("ATTR_NAME") : "ERROR: not ATTR_NAME node";
                    assert sub_node.getChildren().get(ind).getAttr().equalsIgnoreCase("VALUE") : "ERROR: not VALUE node";
                    assert tmp_node.getChildren().size() == 1 : "ERROR: ATTR_NAME list length not 1";
                    assert newTuple.getSchema().getFieldType(tmp_node.getChildren().get(0).getAttr()) != null : "ERROR: cannot get attr type";
                    
                    String value = sub_node.getChildren().get(ind).getChildren().get(0).getAttr();
                    if(newTuple.getSchema().getFieldType(tmp_node.getChildren().get(0).getAttr()).equals(FieldType.INT)) {
                        newTuple.setField(tmp_node.getChildren().get(0).getAttr(), Integer.parseInt(value));
                    } else {
                        newTuple.setField(tmp_node.getChildren().get(0).getAttr(), value);
                    }
                    ind += 1;
                }
                appendTupleToRelation(relation, parameter.memory, 0, newTuple);
            } else if (sub_node.getAttr().equalsIgnoreCase("SELECT")) {
                /**
                 * need to be more general here
                 */
                Relation tempRelation = Helper.selectHandler(parameter.schema_manager, parameter.memory, "SELECT * FROM course", 1);
                
                //debuging
                /*ArrayList<Tuple> tuples;
                tuples = parameter.memory.getTuples(0, tempRelation.getNumOfBlocks());
                System.out.println("========================");
                for (Tuple t : tuples) {
                    System.out.println(t);
                }
                System.out.println("========================");*/
                
                
                String[] tp = {"sid","homework","project","exam","grade"};
                ArrayList<String> tempList = new ArrayList<>(Arrays.asList(tp));
                Helper.insertFromSel(parameter.schema_manager, parameter.memory, parameter.schema_manager.getRelation(relation_name), tempList,tempRelation);
            }
        }
        System.out.println("INSERT COMPLETE");
        //col.value.put("COL", parameter.value.get())
        //col_name_list = super.stringMachineHashMap.get("COL").execute()

        return null;
    }

    private static void appendTupleToRelation(Relation relation_reference, MainMemory memory, int memory_block_index, Tuple tuple) {
        Block block_reference;
        if (relation_reference.getNumOfBlocks()==0) {
            // System.out.print("The relation is empty" + "\n");
            // System.out.print("Get the handle to the memory block " + memory_block_index + " and clear it" + "\n");
            block_reference=memory.getBlock(memory_block_index);
            block_reference.clear(); //clear the block
            block_reference.appendTuple(tuple); // append the tuple
            relation_reference.setBlock(relation_reference.getNumOfBlocks(),memory_block_index);
        } else {
            relation_reference.getBlock(relation_reference.getNumOfBlocks()-1,memory_block_index);
            block_reference=memory.getBlock(memory_block_index);
            if (block_reference.isFull()) {
                // System.out.print("(The block is full: Clear the memory block and append the tuple)" + "\n");
                block_reference.clear(); //clear the block
                block_reference.appendTuple(tuple); // append the tuple
                // System.out.print("Write to a new block at the end of the relation" + "\n");
                relation_reference.setBlock(relation_reference.getNumOfBlocks(),memory_block_index); //write back to the relation
            } else {
                // System.out.print("(The block is not full: Append it directly)" + "\n");
                block_reference.appendTuple(tuple); // append the tuple
                // System.out.print("Write to the last block of the relation" + "\n");
                relation_reference.setBlock(relation_reference.getNumOfBlocks()-1,memory_block_index); //write back to the relation
            }
        }
    }
}
