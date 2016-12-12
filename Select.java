import storageManager.*;

import java.lang.reflect.Array;
import java.util.*;



//Write to file package

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Select extends Guider {
    @Override
    
    public Parameter execute(Parameter parameter) 
    {
        try
        {
            parameter.fw=new FileWriter("Result.txt", true);
        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
        }
            
        HashMap<String, List<Node>> map = new HashMap<String, List<Node>>();
        //Node statement = parameter.para_list.get(0);
        Node col = null, from = null, order = null;
        Expression expr = null;
        SchemaManager schema_manager = parameter.schema_manager;
        MainMemory memory = parameter.memory;

        for (Node s: parameter.para_list) {
            //Test.traversal(s, 0);
            //System.out.println("The element in parameter is : "+s.getAttr());
            if(s.getAttr().equalsIgnoreCase("ATTR_LIST")) col = s;
            if(s.getAttr().equalsIgnoreCase("FROM")) from = s;
            if(s.getAttr().equalsIgnoreCase("EXPRESSION")) expr = new Expression(s);
            if(s.getAttr().equalsIgnoreCase("ORDER")) order = s;
        }

        assert from != null;
        assert col != null;

        /**
         * This is a patch
         */
        Node cols = col;
        if(cols.getChildren().get(0).getAttr().equalsIgnoreCase("DISTINCT")) {
            cols = cols.getChildren().get(0);
        }
        ArrayList<String> fieldList = new ArrayList<>();
        for (Node s: cols.getChildren()) {
            //output
            //System.out.println("s.getAttr "+ s.getAttr() );
            assert s.getAttr().equals("ATTR_NAME");
            StringBuilder fieldName = new StringBuilder();
            for (Node subField: s.getChildren()) {
                //output
                //System.out.println("subField.getAttr "+ subField.getAttr() );
                fieldName.append(subField.getAttr()+".");
                //System.out.println("fieldName "+ fieldName );
            }
            fieldName.deleteCharAt(fieldName.length()-1);
            fieldList.add(fieldName.toString());
        }
        /**
         * End of patch
         */


        /**
         * This Part is for select from single Relation.
         */
        if (from.getChildren().size() == 1) {
            // select from single table
            assert from.getChildren().get(0).getAttr().equalsIgnoreCase("TABLE");
            String relationName = from.getChildren().get(0).getChildren().get(0).getAttr();
            Relation relation = schema_manager.getRelation(relationName);
            /**
             * This part is for the case that all table could be fit in main memory.
             */
            if(relation.getNumOfBlocks() <= memory.getMemorySize()) {
                // All blocks of the relation could be fit into main memory
                relation.getBlocks(0,0,relation.getNumOfBlocks());
                ArrayList<Tuple> tuples;
                tuples = memory.getTuples(0, relation.getNumOfBlocks());
                if (expr != null) {
                    ArrayList<Tuple> where = new ArrayList<>();
                    for (Tuple tuple: tuples) {
                        if(expr.evaluateBoolean(tuple)) {
                            where.add(tuple);
                        }
                    }
                    tuples = where;
                }

                if (order != null || col.getChildren().get(0).getAttr().equalsIgnoreCase("DISTINCT")) {
                    //Make them in order
                    if(tuples.size() == 0) {
                        System.out.println("Empty Table");
                        return null;
                    }
                    Algorithms.sortInMemory(tuples, order==null?null:order.getChildren().get(0).getChildren().get(0).getAttr());
                 
                    if(col.getChildren().get(0).getAttr().equalsIgnoreCase("DISTINCT")) {
                        Algorithms.removeDuplicate(tuples, fieldList);
                    }
                }
                
                try(BufferedWriter bw = new BufferedWriter(parameter.fw)) 
                {
                    System.out.println("--------------------------------");
			        String content = "--------------------------------\n";
			        bw.write(content);
    			    if (col.getChildren().get(0).getChildren().get(0).getAttr().equals("*")) {
                        //System.out.println(tuples.get(0).toString(true));
                        try {
                            //System.out.print( "121 ");
                            for (String s : tuples.get(0).getSchema().getFieldNames()) {
                                System.out.print(s + "  ");
                                 
    			                 bw.write(s + "  ");
                            }
                            System.out.println();
                             bw.write("\n");
                            for (Tuple t : tuples) {
                                System.out.println(t);
                                String tuplestring=t.toString(false);
                                bw.write(tuplestring+"\n");
                            }
                        }catch (Exception exp) {
                            System.out.println("No tuples");
    
            			        bw.write("No tuples"+"\n");
    
                        }
                } else {
                    if (col.getChildren().get(0).getAttr().equalsIgnoreCase("DISTINCT")) { // with distinct
                        for (Node field: col.getChildren().get(0).getChildren()) {
                            if (field.getChildren().size() == 1) { // attr
                                System.out.print(field.getChildren().get(0).getAttr() + "  ");
        			            bw.write(field.getChildren().get(0).getAttr() + "  ");
                            }else { // table.attr
                                System.out.print(field.getChildren().get(0).getAttr() + "."+ field.getChildren().get(1).getAttr() + "  ");
        			            bw.write(field.getChildren().get(0).getAttr() + "."+ field.getChildren().get(1).getAttr() + "  ");
                            }
                        }
                    }else { // without distinct
                        for (Node field: col.getChildren()) {
                            if (field.getChildren().size() == 1) { // attr
                                System.out.print(field.getChildren().get(0).getAttr() + "  ");
        			            bw.write(field.getChildren().get(0).getAttr() + "  ");
                            }else { // table.attr
                                System.out.print(field.getChildren().get(0).getAttr() + "."+ field.getChildren().get(1).getAttr() + "  ");
        			            bw.write(field.getChildren().get(0).getAttr() + "."+ field.getChildren().get(1).getAttr() + "  ");
                            }
                        }
                    }
                    
                    System.out.println();
			        bw.write("\n");
                	
                    for (Tuple t: tuples) {
                        if (col.getChildren().get(0).getAttr().equalsIgnoreCase("DISTINCT")) { //has distinct
                            for (Node field: col.getChildren().get(0).getChildren()) {
                                //System.out.println( "157 : field.getChildren().get(0).getAttr()="+field.getAttr());
                                if (field.getChildren().size() == 1)
                                {
                                    if (t.getSchema().getFieldType(field.getChildren().get(0).getAttr())==FieldType.INT) {
                                    //System.out.println( "159 ");
                                        System.out.print(t.getField(field.getChildren().get(0).getAttr()).integer + "   ");
                                    
                    			        bw.write(t.getField(field.getChildren().get(0).getAttr()).integer + "   ");
                    			       
                                	
                                    } else {
                                        //System.out.print( "166 ");
                                        System.out.print(t.getField(field.getChildren().get(0).getAttr()).str + "   ");
                                    
                    			        bw.write(t.getField(field.getChildren().get(0).getAttr()).str + "   ");
                    			       
                                	                                
                                    }
                                }
                                else
                                {
                                    if (t.getSchema().getFieldType(field.getChildren().get(1).getAttr())==FieldType.INT) {
                                        //System.out.print( "159 ");
                                        System.out.print(t.getField(field.getChildren().get(1).getAttr()).integer + "   ");
                                    
                    			        bw.write(t.getField(field.getChildren().get(1).getAttr()).integer + "   ");
                                    }else {
                                        //System.out.print( "166 ");
                                        System.out.print(t.getField(field.getChildren().get(1).getAttr()).str + "   ");
                                        
                        			    bw.write(t.getField(field.getChildren().get(1).getAttr()).str + "   ");
                        			       
                                    } 
                                }
                            }
                        }else { //without distinct
                                for (Node field: col.getChildren()) {
                                //System.out.println( "157 : field.getChildren().get(0).getAttr()="+field.getAttr());
                                if (field.getChildren().size() == 1)
                                {
                                    if (t.getSchema().getFieldType(field.getChildren().get(0).getAttr())==FieldType.INT) {
                                    //System.out.print( "159 ");
                                        System.out.print(t.getField(field.getChildren().get(0).getAttr()).integer + "   ");
                                    
                    			        bw.write(t.getField(field.getChildren().get(0).getAttr()).integer + "   ");
                    			       
                                	
                                    } else {
                                        //System.out.print( "166 ");
                                        System.out.print(t.getField(field.getChildren().get(0).getAttr()).str + "   ");
                                    
                    			        bw.write(t.getField(field.getChildren().get(0).getAttr()).str + "   ");
                    			       
                                	                                
                                    }
                                }
                                else
                                {
                                    if (t.getSchema().getFieldType(field.getChildren().get(1).getAttr())==FieldType.INT) {
                                        //System.out.print( "159 ");
                                        System.out.print(t.getField(field.getChildren().get(1).getAttr()).integer + "   ");
                                    
                    			        bw.write(t.getField(field.getChildren().get(1).getAttr()).integer + "   ");
                                    }else {
                                        //System.out.print( "166 ");
                                        System.out.print(t.getField(field.getChildren().get(1).getAttr()).str + "   ");
                                        
                        			    bw.write(t.getField(field.getChildren().get(1).getAttr()).str + "   ");
                        			       
                                    } 
                                }
                            }
                        }
                        
                        System.out.println();
                        
        			        bw.write("\n");
        			       
                     
                    }
                }
                System.out.println("--------------------------------");
               
			        bw.write("--------------------------------\n\n\n");
			       
            	
			        
            	} catch (IOException e) 
            	{
            			//e.printStackTrace();
            	}
            	
            	

                
            }
            /**
             * The whole table could not fit in main memory.
             */
            else {
                ArrayList<String> fields = new ArrayList<>();
                boolean distinct = false;
                if (col.getChildren().get(0).getAttr().equalsIgnoreCase("DISTINCT")) {
                    distinct = true;
                    col = col.getChildren().get(0);
                }
                
                for (Node field: col.getChildren()) {
                    if (field.getChildren().size() == 1) { //attr
                        fields.add(field.getChildren().get(0).getAttr());
                    }else { //table.attr
                        fields.add(field.getChildren().get(0).getAttr() + "." + field.getChildren().get(1).getAttr());
                    }
                    
                }
                if (order == null && !distinct) {
                    // basic select operation with/without where.
                    System.out.println("Basic select operation with/without where");
                    basicSelect(memory, schema_manager, relationName, fields, expr);
                } else {
                    //select from one table in order/distinct
                    System.out.println("Select from one table in order/distinct");
                    String orderField = order == null? null:order.getChildren().get(0).getChildren().get(0).getAttr();
                    advancedSelect(memory, schema_manager, relationName, fields, expr, orderField, distinct);
                }
            }
            /**
             * Single Relation DONE!
             */
        }
        /**
         *  This part is for select from multi-Relation
         */
        else {
            boolean distinct = false;
            if(col.getChildren().get(0).getAttr().equalsIgnoreCase("DISTINCT")) {
                distinct = true;
                col.setChildren(col.getChildren().get(0).getChildren());
            }

            if(expr != null && expr.exp_node.getChildren().get(0).getAttr().equals("=")) {
                Node eqs = expr.exp_node.getChildren().get(0);
                if(eqs.getChildren().get(0).getAttr().equalsIgnoreCase("ATTR_NAME")
                        &&eqs.getChildren().get(1).getAttr().equalsIgnoreCase("ATTR_NAME")) {
                    String table1 = eqs.getChildren().get(0).getChildren().get(0).getAttr();
                    String table2 = eqs.getChildren().get(1).getChildren().get(0).getAttr();
                    String field0 = eqs.getChildren().get(0).getChildren().get(1).getAttr();
                    String field1 = eqs.getChildren().get(1).getChildren().get(1).getAttr();
                    if(field0.equals(field1)) {
                        System.out.println("Natural join optimization is applied");
                        Relation r = Helper.executeNaturalJoin(schema_manager, memory, table1, table2, field0, 1);
                        Algorithms.mergeField(expr.exp_node);
                        Algorithms.mergeField(col);
                        ArrayList<String> fields = new ArrayList<>();
                        
                        for (Node ids: col.getChildren()) {
                            if (ids.getChildren().size() == 1) {// attr
                                fields.add(ids.getChildren().get(0).getAttr());
                            }else {//table.attr
                                fields.add(ids.getChildren().get(0).getAttr() + "." + ids.getChildren().get(1).getAttr());
                            }
                        }
                        if(!distinct && order == null) {
                            //debuging
                            //System.out.println("SELECT 333 DEBUG: reach here");
                            /*for (String tmp : fields) {
                                System.out.println("SELECT 335 DEBUG: "+ tmp);
                            }*/
                            
                            Helper.filter(schema_manager, memory, r, expr, fields, 0);
                            //System.out.println("SELECT 339 DEBUG: reach here");
                            return null;
                        }
                        
                        //System.out.println("SELECT 343 DEBUG: reach here");
                        Relation ra = Helper.filter(schema_manager, memory, r, expr, fields, 1);//get tuple
                        //System.out.println("SELECT 345 DEBUG: reach here");
                        
                        if(distinct && order == null) {
                            if (fields.get(0).equals("*")) {
                                //System.out.print( "259");
                                fields = ra.getSchema().getFieldNames();
                            }
                            Helper.executeDistinct(schema_manager, memory, ra, fields, 0);
                            return null;
                        }
                        if(!distinct && order!=null) {
                            Algorithms.mergeField(order);
                            ArrayList<String> orderField = new ArrayList<>();
                            
                            if (order.getChildren().get(0).getChildren().size() == 1) {// attr
                                orderField.add(order.getChildren().get(0).getChildren().get(0).getAttr());
                            }else {//table.attr
                                orderField.add(order.getChildren().get(0).getChildren().get(0).getAttr() + "." + order.getChildren().get(0).getChildren().get(1).getAttr());
                            }
                            
                            
                            Helper.executeOrder(schema_manager, memory, ra, orderField,0);
                            return null;
                        }
                        if(distinct && order != null) {
                            if (fields.get(0).equals("*")) {
                                //System.out.print( "274 ");
                                fields = ra.getSchema().getFieldNames();
                            }
                            Algorithms.mergeField(order);
                            ArrayList<String> orderField = new ArrayList<>();
                            orderField.add(order.getChildren().get(0).getChildren().get(0).getAttr());
                            Helper.executeOrder(schema_manager, memory, Helper.executeDistinct(schema_manager, memory, ra, fields, 1), orderField,0);
                            return null;
                        }

                        return null;
                    }
                }
            }

            System.out.println("Execute Select in multi-relation");
                /**
                 * This is the part that cannot apply natural join while may have to chance to optimize by
                 * change join order.
                 */
                //System.out.println("Execute Select in multi-relation2");



                //System.out.println("No natural join optimization for this command");
                ArrayList<String> relationList = new ArrayList<>();
                for (Node relation: from.getChildren()) {
                    assert relation.getAttr().equalsIgnoreCase("TABLE");
                    relationList.add(relation.getChildren().get(0).getAttr());
                }
                System.out.println(relationList);
                //six tables
                if (!distinct&&order==null&&col.getChildren().get(0).getChildren().get(0).getAttr().equals("*")&&expr==null) {
                    //System.out.println("reach here");
                    
                    MultiRelationCrossJoin(schema_manager, memory, relationList, 0);
                    return null;
                }
                //debuging 
                /*for (String tmp_ss : relationList) {
                    System.out.println("SELECT 407 DEBUG: relationList: " + tmp_ss);
                }*/
                
                //three tables
                if (relationList.size() == 3) {
                    //run a DP algorithm to determine the order of join.
                    int memsize = memory.getMemorySize();
                    HashMap<Set<String> ,CrossRelation> singleRelation = new HashMap<>();
                    for (String name: relationList) {
                        HashSet<String> set = new HashSet<>();
                        set.add(name);
                        Relation relation = schema_manager.getRelation(name);
                        CrossRelation temp = new CrossRelation(set, relation.getNumOfBlocks(), relation.getNumOfTuples());
                        temp.cost = relation.getNumOfBlocks();
                        temp.fieldNum = relation.getSchema().getNumOfFields();
                        singleRelation.put(set, temp);
                    }
                    //List of HashMap should be DP table
                    List<HashMap<Set<String> ,CrossRelation>> costRelationList = new ArrayList<>();
                    costRelationList.add(singleRelation);
                    for (int i = 1; i < relationList.size(); i++) {
                        costRelationList.add(new HashMap<Set<String> ,CrossRelation>());
                    }
        
                    Set<String> finalGoal = new HashSet<>(relationList);
                    CrossRelation cr = Algorithms.findOptimal(costRelationList, finalGoal, memsize);
                    Algorithms.travesal(cr, 0);
                    
                    //get the join attrs
                    Node eql_node = expr.exp_node.getChildren().get(0).getChildren().get(0);
                    String joinR1 = eql_node.getChildren().get(0).getChildren().get(0).getAttr();
                    String joinF1 = eql_node.getChildren().get(0).getChildren().get(1).getAttr();
                    String joinR2 = eql_node.getChildren().get(1).getChildren().get(0).getAttr();
                    String joinF2 = eql_node.getChildren().get(1).getChildren().get(1).getAttr();
                    
                    //debuging
                    //System.out.println("SELECT 446 DEBUG: joinR1: " + joinR1 + " joinF1: " + joinF1 + " joinR2: " + joinR2 + " joinF2: " + joinF2);
                    
                    /*
                    *natural join first two tables
                    */
                    System.out.println("Start to join first two tables");
                    Relation firstJoinR = Helper.executeNaturalJoin(schema_manager, memory, joinR1, joinR2, joinF1, 1);
                    //Algorithms.mergeField(expr.exp_node);
                    //Algorithms.mergeField(col);
                    
                    //debuging
                    String dub_field_names = firstJoinR.getSchema().fieldNamesToString();
                    //System.out.println("SELECT 457 DEBUG: field names after first join: " + dub_field_names);
                    /*
                    *natural join the intermediate table and the remain one
                    */
                    Node second_eql_node = expr.exp_node.getChildren().get(0).getChildren().get(1).getChildren().get(0);
                    String joinR3 = second_eql_node.getChildren().get(1).getChildren().get(0).getAttr();
                    String joinF3 = second_eql_node.getChildren().get(1).getChildren().get(1).getAttr();
                    
                    System.out.println("Start to join the intermediate and third tables");
                    Relation secondJoinR = Helper.executeNaturalJoin(schema_manager, memory, firstJoinR.getRelationName(), joinR3, joinF3, 1);
                    //Algorithms.mergeField(expr.exp_node);
                    
                    //debuging
                    //String dub2_field_names = secondJoinR.getSchema().fieldNamesToString();
                    //System.out.println("SELECT 471 DEBUG: field names after second join: " + dub2_field_names);
                    
                    //selection on third expression
                    Node tmpThirdEql = new Node("=");
                    tmpThirdEql.setChildren(expr.exp_node.getChildren().get(0).getChildren().get(1).getChildren().get(1).getChildren());
                    for (Node tmp_node : tmpThirdEql.getChildren()) {
                        //System.out.println("SELECT 479 DEBUG: tmp_node: " + tmp_node.getChildren().get(0).getAttr());
                        if (tmp_node.getChildren().get(0).getAttr().equalsIgnoreCase("t")) {
                            //debuging
                            //System.out.println("SELECT 481 DEBUG: ");
                            tmp_node.getChildren().get(0).setAttr("rnaturalt.t");
                        }
                    }
                    
                    Expression thirdExpr = new Expression(tmpThirdEql);
                    ArrayList<String> fields = new ArrayList<String>();
                    fields.add("*");
                    Helper.filter(schema_manager, memory, secondJoinR, thirdExpr, fields, 0);
                    return null;
                }
                Relation relationAfterCross = MultiRelationCrossJoin(schema_manager, memory, relationList, 1);


                // order, distinct, where
            Algorithms.mergeField(col);

            ArrayList<String> fields = new ArrayList<>();
                // distinct and order doesn't support *
                /*for (Node ids: col.getChildren()) {
                    if (ids.getChildren().size() == 1) {// attr
                        fields.add(ids.getChildren().get(0).getAttr());
                    }else {//table.attr
                        fields.add(ids.getChildren().get(0).getAttr() + "." + ids.getChildren().get(1).getAttr());
                    }
                }*/
                fields = relationAfterCross.getSchema().getFieldNames();
                
                Scanner in = new Scanner(System.in);
                in.nextLine();
                
                //debuging
                /*for (String tmp_s : fields) {
                    System.out.println("SELECT 430 DEBUG: fields: " + tmp_s);
                }*/
            

                if(expr != null) {
                    Algorithms.mergeField(expr.exp_node);
                    if(!distinct&&order==null) {
                        //debuging
                        //System.out.println("Select 416 DEBUG: ");
                        
                        Helper.filter(schema_manager, memory, relationAfterCross, expr, fields, 0);
                        
                        //System.out.println("Select 420 DEBUG: ");
                        return null;
                    }else {
                        //debuging
                        //System.out.println("Select 424 DEBUG: ");
                        
                        relationAfterCross = Helper.filter(schema_manager, memory, relationAfterCross, expr, fields, 1);
                        
                        //System.out.println("Select 428 DEBUG: ");
                    }
                }

                // if(expr == null && )

                if(distinct) {
                    if (fields.get(0).equals("*")) {
                        //System.out.print( "SELECT 336 DEBUG: ");
                        fields = relationAfterCross.getSchema().getFieldNames();
                    }
                    if(order == null) {
                        Helper.executeDistinct(schema_manager, memory, relationAfterCross, fields, 0);
                        return null;
                    } else {
                        //debuging
                        //System.out.println("SELECT 460 DEBUG: ");
                        relationAfterCross = Helper.executeDistinct(schema_manager, memory, relationAfterCross, fields, 1);
                        //System.out.println("SELECT 462 DEBUG: ");
                    }
                }

                if(order != null) {
                    fields = new ArrayList<>();
                    fields.add(order.getChildren().get(0).getChildren().get(0).getAttr());
                    Helper.executeOrder(schema_manager, memory, relationAfterCross, fields, 0);
                    return null;
                }

                if (expr == null && !fields.get(0).equals("*")) {
                    int total = relationAfterCross.getNumOfBlocks();
                    for (int i = 0; i < total; i++) {
                        relationAfterCross.getBlock(i, 0);
                        ArrayList<Tuple> tuples = memory.getBlock(0).getTuples();
                        for (Tuple tp: tuples) {
                            for(String f: fields){
                                //System.out.print( "361 ");
                                System.out.print(tp.getField(f).toString() +"  ");
                            }
                            System.out.println();
                        }
                    }
                }
        }

        return null;
    }


    public static Relation MultiRelationCrossJoin(SchemaManager schema_manager, MainMemory memory, ArrayList<String> relationName, int mode) {
        //cross join plan
        int memsize = memory.getMemorySize();
        if (relationName.size() == 2) {
            /**
             * This is the part that two table natural join.
             */
            return Helper.executeCrossJoin(schema_manager, memory, relationName, mode);
        } else {
            //run a DP algorithm to determine the order of join.
            HashMap<Set<String> ,CrossRelation> singleRelation = new HashMap<>();
            for (String name: relationName) {
                HashSet<String> set = new HashSet<>();
                set.add(name);
                Relation relation = schema_manager.getRelation(name);
                CrossRelation temp = new CrossRelation(set, relation.getNumOfBlocks(), relation.getNumOfTuples());
                temp.cost = relation.getNumOfBlocks();
                temp.fieldNum = relation.getSchema().getNumOfFields();
                singleRelation.put(set, temp);
            }
            //List of HashMap should be DP table
            List<HashMap<Set<String> ,CrossRelation>> costRelationList = new ArrayList<>();
            costRelationList.add(singleRelation);
            for (int i = 1; i < relationName.size(); i++) {
                costRelationList.add(new HashMap<Set<String> ,CrossRelation>());
            }

            Set<String> finalGoal = new HashSet<>(relationName);
            CrossRelation cr = Algorithms.findOptimal(costRelationList, finalGoal, memsize);
            Algorithms.travesal(cr, 0);
            if (mode == 0) {
                helper(cr, memory, schema_manager, 0);
            } else {
                return helper(cr, memory, schema_manager, 1);
            }

            /**
             * A lot to be done
             */
            return null;
        }
    }
    public static Relation helper(CrossRelation cr, MainMemory memory, SchemaManager schema_manager, int mode) {
        //mode 0 display, mode 1 output
        if(cr.joinBy == null||cr.joinBy.size()<2) {
            List<String> relation = new ArrayList<>(cr.subRelation);
            assert relation.size() == 1;
            return schema_manager.getRelation(relation.get(0));
        } else {
            assert cr.joinBy.size() == 2;
            if(mode == 0) {
                String subRelation1 = helper(cr.joinBy.get(0), memory, schema_manager, 1).getRelationName();
                String subRelation2 = helper(cr.joinBy.get(1), memory, schema_manager, 1).getRelationName();
                ArrayList<String> relationName = new ArrayList<>();
                relationName.add(subRelation1);
                relationName.add(subRelation2);
                return Helper.executeCrossJoin(schema_manager, memory, relationName, 0);
            } else {
                String subRelation1 = helper(cr.joinBy.get(0), memory, schema_manager, 1).getRelationName();
                String subRelation2 = helper(cr.joinBy.get(1), memory, schema_manager, 1).getRelationName();
                ArrayList<String> relationName = new ArrayList<>();
                relationName.add(subRelation1);
                relationName.add(subRelation2);
                /*
                System.out.println(subRelation1);
                System.out.println(subRelation2);
                System.out.println("-------------");
                System.out.println(schema_manager.getRelation(subRelation1).getSchema().getFieldNames());
                System.out.println("-------------");
                System.out.println(schema_manager.getRelation(subRelation2).getSchema().getFieldNames());
                System.out.println("-------------");
                */
                return Helper.executeCrossJoin(schema_manager, memory, relationName, 1);
            }
        }
    }


    private void print(Tuple tuple, List<String> fieldList) {
        if (fieldList.get(0).equals("*")) {
            System.out.println(tuple);
            return;
        }
        
        for (String field: fieldList) {
            //System.out.print( "458 field: " + field + " ");
            //System.out.println("indexOf point: " + field.indexOf('.'));
            if (field.indexOf('.') > 0) { //table.attr
                String tmp_field = field.substring(field.indexOf('.')+1);
                System.out.print((tuple.getSchema().getFieldType(tmp_field)==FieldType.INT?
                    tuple.getField(tmp_field).integer:tuple.getField(tmp_field).str) + "   ");                
            }else { //attr
                System.out.print((tuple.getSchema().getFieldType(field)==FieldType.INT?
                    tuple.getField(field).integer:tuple.getField(field).str) + "   ");
            }
        }
        
        
        System.out.println();
    }

    private void printTitle(Tuple tuple, List<String> fieldList) {
        if (fieldList.get(0).equals("*")) {
            //System.out.print( "467 ");
            for (String fieldNames: tuple.getSchema().getFieldNames()) {
                System.out.print(fieldNames + "   ");
            }
            System.out.println();
        }
        else {
            for (String str: fieldList) {
                //System.out.print("544 ");
                System.out.print(str + "    ");
            }
            System.out.println();
        }
    }

    public void advancedSelect(MainMemory memory, SchemaManager schema_manager, String relationName,
                               ArrayList<String> field, Expression exp, String orderBy, boolean distinct) {
        Relation relation = schema_manager.getRelation(relationName);
        /**
         * If where condition exists, apply where and generate a new relation
         */
        if (exp != null) {
            Schema schema = relation.getSchema();
            Relation tempRelation = schema_manager.createRelation(relationName+"temp", schema);
            int tempRelationCurrentBlock = 0;
            Block tempBlock = memory.getBlock(1);
            tempBlock.clear();
            int count = 0;
            for (int i = 0; i < relation.getNumOfBlocks(); i++) {
                relation.getBlock(i, 0);
                ArrayList<Tuple> tupes = memory.getBlock(0).getTuples();
                for (Tuple tupe: tupes) {
                    if(exp.evaluateBoolean(tupe)) {
                        if(!tempBlock.isFull()) tempBlock.appendTuple(tupe);
                        else {
                            memory.setBlock(1, tempBlock);
                            tempRelation.setBlock(tempRelationCurrentBlock, 1);
                            tempRelationCurrentBlock += 1;
                            tempBlock.clear();
                            tempBlock.appendTuple(tupe);
                        }
                    } /*else {
                        System.out.print("Dumped   ");
                        System.out.println(tupe);
                    }*/
                }
            }

            if(!tempBlock.isEmpty()) {
                //System.out.println("reachHere");
                memory.setBlock(1, tempBlock);
                tempRelation.setBlock(tempRelationCurrentBlock, 1);
                tempBlock.clear();
            }
            relation = tempRelation;
        }

        System.out.println("Number of tuples: " + relation.getNumOfTuples() + "*******");
        /**
         * This part ends here
         */

        if(relation.getNumOfBlocks() <= memory.getMemorySize()) {
            relation.getBlocks(0, 0, relation.getNumOfBlocks());
            ArrayList<Tuple> tuples = memory.getTuples(0, relation.getNumOfBlocks());
            Algorithms.sortInMemory(tuples, orderBy);
            if(distinct) {
                Algorithms.removeDuplicate(tuples, field);
            }
            printTitle(tuples.get(0), field);
            for (Tuple tuple: tuples) {
                //System.out.println("619 ");
                print(tuple, field);
            }
        } else {
            System.out.println("Two pass condition");
            ArrayList<String> order = new ArrayList<>();
            if(orderBy != null) {
                order.add(orderBy);
            }
            if(field.get(0).equals("*")) {
                //System.out.print( "546 ");
                field = relation.getSchema().getFieldNames();
            }
            if(distinct && orderBy!=null) {
                relation = Helper.executeDistinct(schema_manager, memory, relation, field, 1);
                Helper.executeOrder( schema_manager, memory, relation, order, 0);
            }else if (distinct) {
                Helper.executeDistinct(schema_manager, memory, relation, field, 0);
            }else if (orderBy != null) {
                Helper.executeOrder(schema_manager, memory, relation, order, 0);
            }
        }
    }

    private Relation basicSelect(MainMemory memory, SchemaManager schema_manager, String relationName, List<String> field, Expression exp) {
        int currentBlockCount = 0;
        Relation relation = schema_manager.getRelation(relationName);
        boolean show = false;
        while (currentBlockCount < relation.getNumOfBlocks()) {
            int readBlocks = relation.getNumOfBlocks()-currentBlockCount > memory.getMemorySize()?
                    memory.getMemorySize(): relation.getNumOfBlocks()-currentBlockCount;
            relation.getBlocks(currentBlockCount, 0, readBlocks);
            ArrayList<Tuple> tuples = memory.getTuples(0, readBlocks);
            if(!show) {
                show = true;
                if(field.get(0).equals("*")) {
                    //System.out.print( "572 ");
                    for (String fieldNames: tuples.get(0).getSchema().getFieldNames()) {
                        System.out.print(fieldNames + "   ");
                    }
                    System.out.println();
                }
                else {
                    for (String name: field) System.out.print(name + "  ");
                    System.out.println();
                }
            }
            for(Tuple tuple: tuples) {
                if(exp == null) print(tuple, field);
                else {
                    if (exp.evaluateBoolean(tuple)) print(tuple, field);
                }
            }
            currentBlockCount += readBlocks;
        }
        return null;
    }

}
