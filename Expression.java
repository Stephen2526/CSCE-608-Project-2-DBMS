import storageManager.FieldType;
import storageManager.Tuple;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

/**
 * 
 */
public class Expression {
    class Temp {
        String type;
        String tempString;
        int tempInteger;
        public boolean equals(Temp t2) {
            if (!this.type.equalsIgnoreCase(t2.type)) return false;
            if(this.type.equals("INT")) {
                return this.tempInteger == t2.tempInteger;
            } else {
                return this.tempString.equals(t2.tempString);
            }
        }
    }

    Node exp_node;
    public Expression(Node exp_node) {
        this.exp_node = exp_node;
    }

    public boolean evaluateBoolean(Tuple tuple) {
        //debuging
        //System.out.println("EXPRESSION 33 DEBUG: expression: "+ exp_node.getAttr());
        switch (exp_node.getAttr()) {
            case "EXPRESSION":
            {
                
                return new Expression(exp_node.getChildren().get(0)).evaluateBoolean(tuple);
                
            }
            case "AND": {
                /**
                 * This is the first version, still lot of things to be done <>Push selection done</>
                 */
                return new Expression(exp_node.getChildren().get(0)).evaluateBoolean(tuple)
                        &&new Expression(exp_node.getChildren().get(1)).evaluateBoolean(tuple);
            }
            case "OR": {
                return new Expression(exp_node.getChildren().get(0)).evaluateBoolean(tuple)
                        ||new Expression(exp_node.getChildren().get(1)).evaluateBoolean(tuple);
            }
            case "=": {
                Expression left = new Expression(exp_node.getChildren().get(0));
                Expression right = new Expression(exp_node.getChildren().get(1));
                
                //debuging
                //System.out.println("EXPRESSION 57 DEBUG: left: " + exp_node.getChildren().get(0).getChildren().get(0).getAttr());
                //System.out.println("EXPRESSION 58 DEBUG: right: " + exp_node.getChildren().get(1).getChildren().get(0).getAttr());
                return left.evaluateUnknown(tuple).equals(right.evaluateUnknown(tuple));
            }
            case ">": {
                return new Expression(exp_node.getChildren().get(0)).evaluateInt(tuple)
                        >new Expression(exp_node.getChildren().get(1)).evaluateInt(tuple);
            }
            case "<": {
                return new Expression(exp_node.getChildren().get(0)).evaluateInt(tuple)
                        <new Expression(exp_node.getChildren().get(1)).evaluateInt(tuple);
            }
            case "NOT": {
                return !new Expression(exp_node.getChildren().get(0)).evaluateBoolean(tuple);
            }
            default: try {
                throw new Exception("Unknown Operator");
            }catch (Exception err) {
                err.printStackTrace();
            }
        }
        return false;
    }

    public int evaluateInt(Tuple tuple) {
        switch (exp_node.getAttr()) {
            case "+": {
                return new Expression(exp_node.getChildren().get(0)).evaluateInt(tuple)
                        + new Expression(exp_node.getChildren().get(1)).evaluateInt(tuple);
            }
            case "-": {
                return new Expression(exp_node.getChildren().get(0)).evaluateInt(tuple)
                        - new Expression(exp_node.getChildren().get(1)).evaluateInt(tuple);
            }
            case "*": {
                return new Expression(exp_node.getChildren().get(0)).evaluateInt(tuple)
                        * new Expression(exp_node.getChildren().get(1)).evaluateInt(tuple);
            }
            case "/": {
                return new Expression(exp_node.getChildren().get(0)).evaluateInt(tuple)
                        / new Expression(exp_node.getChildren().get(1)).evaluateInt(tuple);
            }
            case "ATTR_NAME": {
                StringBuilder fieldName = new StringBuilder();
                for (Node name: exp_node.getChildren()) {
                    fieldName.append(name.getAttr()+".");
                }
                fieldName.deleteCharAt(fieldName.length()-1);
                String name = fieldName.toString();
                return tuple.getField(name).integer;
            }
            case "INT": {
                return Integer.parseInt(exp_node.getChildren().get(0).getAttr());
            }
        }

        return 0;
    }

    public Temp evaluateUnknown(Tuple tuple) {
        Temp temp = new Temp();
        if (exp_node.getAttr().equalsIgnoreCase("STRING")) {
            temp.type = "STRING";
            temp.tempString = exp_node.getChildren().get(0).getAttr();
        } else if (exp_node.getAttr().equalsIgnoreCase("INT")) {
            temp.type = "INT";
            temp.tempInteger = Integer.parseInt(exp_node.getChildren().get(0).getAttr());
        } else if (exp_node.getAttr().equalsIgnoreCase("ATTR_NAME")) {
            StringBuilder fieldName = new StringBuilder();
            for (Node name: exp_node.getChildren()) {
                fieldName.append(name.getAttr()+".");
            }
            fieldName.deleteCharAt(fieldName.length()-1);
            String name = fieldName.toString();
            FieldType type = tuple.getSchema().getFieldType(name);
            if (type == FieldType.INT) {
                temp.type = "INT";
                temp.tempInteger = tuple.getField(name).integer;
            } else {
                temp.type = "STRING";
                temp.tempString = tuple.getField(name).str;
            }
        } else {
            temp.type = "INT";
            temp.tempInteger = evaluateInt(tuple);
        }
        return temp;
    }

}
