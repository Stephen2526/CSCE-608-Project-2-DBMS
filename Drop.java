import java.util.List;

/**
 * guider class
 */
public class Drop extends Guider {
    @Override
    public Parameter execute(Parameter parameter) 
    {
        List<Node> drop_args = parameter.para_list;
        assert drop_args != null;
        String drop_relation = drop_args.get(0).getChildren().get(0).getAttr();
        parameter.schema_manager.deleteRelation(drop_relation);
        System.out.println("DROP: Successfully drop relation "+ drop_relation);
        return null;
    }
}
