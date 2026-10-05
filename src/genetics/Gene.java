package genetics;

public class Gene {
    private final String name;
    private final ExpressionRule expressionRule;

    public Gene(String name, ExpressionRule expressionRule) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Gene name is required");
        if (expressionRule == null) throw new IllegalArgumentException("Expression rule is required");
        this.name = name;
        this.expressionRule = expressionRule;
    }

    public String getName() { return name; }

    public double computePhenotype(Allele allele1, Allele allele2) {
        return expressionRule.express(allele1, allele2);
    }
}
