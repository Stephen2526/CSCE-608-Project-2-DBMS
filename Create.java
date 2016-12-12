import storageManager.Field;
import storageManager.FieldType;
import storageManager.Relation;
import storageManager.Schema;

import java.util.ArrayList;
import java.util.List;

/**
 * Create executor
 */
public class Create extends Guider {
    @Override
    public Parameter execute(Parameter parameter) {
        ArrayList<String> field_name = new ArrayList<String>();
        ArrayList<FieldType> field_type = new ArrayList<FieldType>();
        Node table = parameter.para_list.get(0);
        //help debuging
        assert table.getAttr().equalsIgnoreCase("TABLE") : "ERROE: not TABLE node!";
        
        String relation_name = table.getChildren().get(0).getAttr();

        List<Node> col_details = parameter.para_list.get(1).getChildren();
        for (Node tmp_node: col_details) {
            //help debuging
            assert tmp_node.getAttr().equalsIgnoreCase("CREATE_ATTR") : "ERROR: not CREATE_ATTR node!";
            field_name.add(tmp_node.getChildren().get(0).getChildren().get(0).getAttr());
            String type = tmp_node.getChildren().get(1).getChildren().get(0).getAttr();
            if(type.equals("INT")) {
                field_type.add(FieldType.INT);
            } else if (type.equals("STR20")) {
                field_type.add(FieldType.STR20);
            } else {
                System.out.println("ERROR: invalid attr type!");
                return null;
            }
        }
        Schema schema = new Schema(field_name, field_type);
        if(parameter.schema_manager == null) System.out.println("ERROR: storageManager creates schema Manager failed!");
        Relation newRelation = parameter.schema_manager.createRelation(relation_name, schema);
        
        //handle error
        if (newRelation != null) {
            System.out.println("CREATE: successfully created relation " + relation_name);
        }else {
            System.out.println("CREATE: failed to creat relation " + relation_name);
        }
        return null;
    }
}
