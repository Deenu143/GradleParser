package org.deenu.gradle.script.visitors;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.deenu.gradle.models.Include;

public class GradleSettingsScriptVisitor extends CodeVisitorSupport {

	private int includeLastLineNumber = -1;
	private boolean inInclude = false;
	private List<Include> includes = new ArrayList<>();
	private String includeConfigurationName;
	private Stack<Boolean> blockStatementStack = new Stack<>();

	public List<Include> getIncludes() {
		return includes;
	}

	public int getIncludesLastLineNumber() {
		return includeLastLineNumber;
	}

	@Override
	public void visitArgumentlistExpression(ArgumentListExpression argumentListExpression) {
		List<Expression> expressions = argumentListExpression.getExpressions();
		if (expressions != null) {
			if ((expressions.size() == 1) && (expressions.get(0) instanceof ConstantExpression)) {
				ConstantExpression constantExpression = (ConstantExpression) expressions.get(0);
				if (constantExpression != null) {
					int lineNumber = constantExpression.getLineNumber();
					String expressionText = constantExpression.getText();
					if (expressionText != null && inInclude) {
						Include include = new Include(expressionText);
						includes.add(include);
					}
				}
			}
		}
		super.visitArgumentlistExpression(argumentListExpression);
	}

	@Override
	public void visitMethodCallExpression(MethodCallExpression methodCallExpression) {
		int lineNumber = methodCallExpression.getLineNumber();
		if (lineNumber > includeLastLineNumber) {
			inInclude = false;
		}

		String methodName = methodCallExpression.getMethodAsString();
		if (methodName.equals("include")) {
			includeLastLineNumber = methodCallExpression.getLastLineNumber();
			inInclude = true;
		}
		if (inInclude) {
			includeConfigurationName = methodName;
			super.visitMethodCallExpression(methodCallExpression);
			includeConfigurationName = null;
		} else {
			super.visitMethodCallExpression(methodCallExpression);
		}
	}
}