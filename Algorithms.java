import storageManager.*;

import java.util.*;
import java.util.List;

/**
 * Contains basic sorting/distinct algorithms, read/write disk operations
 */
public abstract class Algorithms {

    public static void travesal(CrossRelation relations , int level) {
        for (int i = 0; i < level; i++) {
            System.out.print(" ");
        }
        for (String str: relations.subRelation) {
            System.out.print(str+" ");
        }
        System.out.println();
        if (relations.joinBy != null) {
            for (CrossRelation cr : relations.joinBy) {
                travesal(cr, level + 1);
            }
        }
    }

    public static CrossRelation findOptimal(List<HashMap<Set<String>, CrossRelation>> tempRelations,
                                     Set<String> finalGoal, int memSize) {
        if (tempRelations.get(finalGoal.size()-1).containsKey(finalGoal)) {
            return tempRelations.get(finalGoal.size()-1).get(finalGoal);
        }
        int block = 0;
        int tuple = 0;
        int fieldNum = 0;
        int minCost = Integer.MAX_VALUE;
        List<CrossRelation> joinBy = null;
        List<SetPair> permutation = cutSet(finalGoal);
        for (SetPair pair: permutation) {
            Set<String> setOne = pair.set1;
            Set<String> setTwo = pair.set2;
            CrossRelation c1 = findOptimal(tempRelations, setOne, memSize);
            CrossRelation c2 = findOptimal(tempRelations, setTwo, memSize);
            if (c1.cost + c2.cost + calcCost(memSize, c1.blockNum, c2.blockNum) < minCost) {
                joinBy = new ArrayList<>();
                joinBy.add(c1);
                joinBy.add(c2);
                tuple = c1.tupleNum * c2.tupleNum;
                block = blocksAfterJoin(c1.tupleNum, c2.tupleNum, 8,c1.fieldNum + c2.fieldNum);
                fieldNum = c1.fieldNum + c2.fieldNum;
                minCost = c1.cost + c2.cost + calcCost(memSize, c1.blockNum, c2.blockNum);
            }
        }

        CrossRelation ret = new CrossRelation(finalGoal, block, tuple);
        ret.joinBy = joinBy;
        ret.fieldNum = fieldNum;
        ret.cost = minCost;
        tempRelations.get(finalGoal.size()-1).put(finalGoal, ret);
        return ret;
    }

    static class SetPair {
        Set<String> set1;
        Set<String> set2;
        public SetPair(Set<String> s1, Set<String> s2) {
            this.set1 = s1;
            this.set2 = s2;
        }

        public String toString() {
            return "[" + set1.toString() + ", " + set2.toString() + "]";
        }
    }

    public static List<SetPair> cutSet(Set<String> input) {
        List<SetPair> result = new ArrayList<>();
        for (int i = 1; i <= input.size() / 2; i++) {
            Set<String> tmpSet = new HashSet<>(input);
            Set<String> pickedSet = new HashSet<>();
            helper(tmpSet, i, 0, pickedSet, result);
        }
        return result;
    }

    public static void helper(Set<String> input, int count, int startPos, Set<String> picked, List<SetPair> result) {
        if (count == 0) result.add(new SetPair(input, picked));
        List<String> inputList = new ArrayList<>(input);
        for (int i = startPos; i < inputList.size(); i++) {
            Set<String> inputTmp = new HashSet<>(input);
            Set<String> pickedTmp = new HashSet<>(picked);
            inputTmp.remove(inputList.get(i));
            pickedTmp.add(inputList.get(i));
            helper(inputTmp, count - 1, i, pickedTmp, result);
        }
    }

    public static int blocksAfterJoin(int tupleNum1, int tupleNum2, int blockSize, int fieldPerTuple) {
        int totalTuples = tuplesAfterJoin(tupleNum1, tupleNum2);
        return totalTuples * fieldPerTuple % blockSize == 0? totalTuples * fieldPerTuple / blockSize:totalTuples * fieldPerTuple / blockSize+ 1;
    }

    public static int tuplesAfterJoin(int tupleNum1, int tupleNum2) {
        return tupleNum1 * tupleNum2;
    }

    public static int calcCost(int memSize, int blockNum1, int blockNum2) {
        if(Math.min(blockNum1, blockNum2) <= memSize) return blockNum1 + blockNum2;
        else return blockNum1 * blockNum2 + Math.min(blockNum1, blockNum2);
    }

    public static void removeDuplicate(ArrayList<Tuple> tuples, List<String> fieldNames) {
        if (tuples.isEmpty()) {
            System.out.println("Empty Relation");
            return;
        }
        Tuple tuple = tuples.get(0);
        if (fieldNames.get(0).equals("*")) {
            fieldNames = tuple.getSchema().getFieldNames();
        }

        int pointer1 = 1, pointer2 = 1;
        while (pointer2 < tuples.size()) {
            if(compareTuple(tuple, tuples.get(pointer2), fieldNames)!=0) {
                tuple = tuples.get(pointer2);
                tuples.set(pointer1, tuples.get(pointer2));
                pointer1 += 1;
            }
            pointer2 += 1;
        }
        for (int i = tuples.size()-1; i >= pointer1; i--) {
            tuples.remove(i);
        }
    }
    public static int compareTuple(Tuple o1, Tuple o2, List<String> fieldNames) {
        for (String name: fieldNames){
            if (o1.getField(name).type == FieldType.INT) {
                if (Integer.compare(o1.getField(name).integer, o2.getField(name).integer)!= 0) {
                    return Integer.compare(o1.getField(name).integer, o2.getField(name).integer);
                }
            } else {
                if (o1.getField(name).str.compareTo(o2.getField(name).str) != 0) {
                    return o1.getField(name).str.compareTo(o2.getField(name).str);
                }
            }
        }
        return 0;
    }
    public static int compareTuple(Tuple o1, Tuple o2, List<String> fieldNames, String orderBy) {
        if(o1.getField(orderBy).type == FieldType.INT) {
            if(Integer.compare(o1.getField(orderBy).integer, o2.getField(orderBy).integer)!=0)
                return Integer.compare(o1.getField(orderBy).integer, o2.getField(orderBy).integer);
        } else {
            if(o1.getField(orderBy).str.compareTo(o2.getField(orderBy).str)!=0)
                return o1.getField(orderBy).str.compareTo(o2.getField(orderBy).str);
        }
        for (String name: fieldNames){
            if(!name.equalsIgnoreCase(orderBy)) {
                if (o1.getField(name).type == FieldType.INT) {
                    if (Integer.compare(o1.getField(name).integer, o2.getField(name).integer) != 0) {
                        return Integer.compare(o1.getField(name).integer, o2.getField(name).integer);
                    }
                } else {
                    if (o1.getField(name).str.compareTo(o2.getField(name).str) != 0) {
                        return o1.getField(name).str.compareTo(o2.getField(name).str);
                    }
                }
            }
        }
        return 0;
    }
    public static void sortInMemory(ArrayList<Tuple> tuples, final String orderBy){
        System.out.println("SELECT: One pass for sorting on 1 relation\n");
        final List<String> fieldNames = tuples.get(0).getSchema().getFieldNames();
        Collections.sort(tuples, new Comparator<Tuple>() {
            @Override
            public int compare(Tuple o1, Tuple o2) {
                if(orderBy == null) {
                    return compareTuple(o1, o2, fieldNames);
                } else {
                    return compareTuple(o1, o2, fieldNames, orderBy);
                }
            }
        });
    }

    public static void twoPassSort(String compareBase, Relation relation, MainMemory mem) {
        int currentBlockNum = 0, readBlockNum;
        //List<String> fieldNames = relation.getSchema().getFieldNames();
        while (currentBlockNum < relation.getNumOfBlocks()) {
            readBlockNum = (relation.getNumOfBlocks() - currentBlockNum)>mem.getMemorySize()
                    ?mem.getMemorySize():(relation.getNumOfBlocks() - currentBlockNum);
            relation.getBlocks(currentBlockNum, 0, readBlockNum);
            //Read a certain number of blocks into memory.
            ArrayList<Tuple> tupleList = mem.getTuples(0, readBlockNum);
            sortInMemory(tupleList, compareBase);
            mem.setTuples(0, tupleList);
            relation.setBlocks(currentBlockNum, 0, readBlockNum);
            currentBlockNum += readBlockNum;
        }
    }

    public static HashSet<String> tableInExpression(Node node) {
        node.contains_table = new HashSet<>();
        if(node.getAttr().equalsIgnoreCase("ATTR_NAME")) {
            assert node.getChildren().size() > 1;
            node.contains_table.add(node.getChildren().get(0).getAttr());
            return node.contains_table;
        } else {
            if(node.getChildren()==null||node.getChildren().size()==0) {
                return node.contains_table;
            }else {
                for (Node subStatement: node.getChildren()) {
                    node.contains_table.addAll(tableInExpression(subStatement));
                }
                return node.contains_table;
            }
        }
    }

    /**
     * This is just a patch.
     */
    public static void mergeField(Node node) {
        /**
         * This function is for mergeField of several table, in a node.
         */

        if (node.getAttr().equalsIgnoreCase("ATTR_NAME")) {
            List<Node> cols = node.getChildren();
            StringBuilder s = new StringBuilder();
            List<Node> tempBranches = new ArrayList<>();
            for (Node field: cols) {
                s.append(field.getAttr()+".");
            }
            s.deleteCharAt(s.length()-1);
            tempBranches.add(new Node(s.toString(), true));
            node.setChildren(tempBranches);
        } else {
            if(node.getChildren()!=null&&node.getChildren().size()>0) {
                for (Node st : node.getChildren()) {
                    mergeField(st);
                }
            }
        }
        //return statament;
    }

}
