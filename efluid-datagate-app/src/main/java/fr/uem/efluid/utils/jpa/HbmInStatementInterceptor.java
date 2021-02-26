package fr.uem.efluid.utils.jpa;

import org.hibernate.EmptyInterceptor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An interceptor for all IN statement on hibernate relate mapping, to avoid Oracle error ORA01795 automatically
 *
 * @author elecomte
 * @version 1
 * @since v2.1.10
 */
public class HbmInStatementInterceptor extends EmptyInterceptor {

    private static final Pattern PATTERN = Pattern.compile("[^\\s]+\\s+in\\s*\\(\\s*\\?[^\\(]*\\)", Pattern.CASE_INSENSITIVE);
    private static final int IN_CAUSE_LIMIT = 1000;

    // Nbr of minimal estimated chars before match can apply
    private static final int MATCHING_SQL_LIMIT = IN_CAUSE_LIMIT * 2;

    @Override
    public String onPrepareStatement(String sql) {
        return super.onPrepareStatement(this.rewriteSqlToAvoidORA_01795(sql));
    }

    /**
     * For
     *
     * @param sql
     * @return
     */
    private String rewriteSqlToAvoidORA_01795(String sql) {

        // Can only match very large sql with at least 2 IN arg injection def
        if (sql.length() < IN_CAUSE_LIMIT) {
            return sql;
        }

        Matcher matcher = PATTERN.matcher(sql);
        while (matcher.find()) {

            // Rebuild the query as a in(?,?...) or in(?,?...) ...

            String inExpression = matcher.group();
            long countOfParameters = inExpression.chars().filter(ch -> ch == '?').count();

            if (countOfParameters <= IN_CAUSE_LIMIT) {
                continue;
            }

            String fieldName = inExpression.substring(0, inExpression.indexOf(' '));
            StringBuilder transformedInExpression = new StringBuilder(" ( ").append(fieldName).append(" in (");

            for (int i = 0; i < countOfParameters; i++) {
                if (i != 0 && i % IN_CAUSE_LIMIT == 0) {
                    transformedInExpression
                            .deleteCharAt(transformedInExpression.length() - 1)
                            .append(") or ").append(fieldName).append(" in (");
                }
                transformedInExpression.append("?,");
            }
            transformedInExpression.deleteCharAt(transformedInExpression.length() - 1).append("))");
            sql = sql.replaceFirst(Pattern.quote(inExpression), transformedInExpression.toString());
        }
        return sql;
    }
}
