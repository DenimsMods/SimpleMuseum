package denimred.simplemuseum.client.resources.data;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@OnlyIn(Dist.CLIENT)
public class ExpressionDataSection {

    public static final ExpressionDataSerializer SERIALIZER = new ExpressionDataSerializer();
    public static final ExpressionDataSection EMPTY = new ExpressionDataSection(Lists.newArrayList());

    private final List<ExpressionData> expressions;

    public ExpressionDataSection(List<ExpressionData> expressions) {
        this.expressions = expressions;
    }

    public ExpressionData getExpression(int id) {
        return this.expressions.get(id);
    }
    public ExpressionData getExpression(String expression) {
        return expressions.stream().filter((x) -> x.name.equals(expression)).findFirst().get();
    }
    public int getExpressionIndex(int id) {
        return this.expressions.get(id).getIndex();
    }
    public int getExpressionCount() {
        return this.expressions.size();
    }
    public boolean hasExpression(String expression) {
        return this.expressions.stream().anyMatch((x) -> Objects.equals(x.name, expression));
    }
    public List<ExpressionData> getExpressionList() {
        return this.expressions;
    }

}
