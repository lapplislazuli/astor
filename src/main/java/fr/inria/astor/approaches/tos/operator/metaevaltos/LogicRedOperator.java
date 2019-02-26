package fr.inria.astor.approaches.tos.operator.metaevaltos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import fr.inria.astor.approaches.cardumen.FineGrainedExpressionReplaceOperator;
import fr.inria.astor.core.entities.Ingredient;
import fr.inria.astor.core.entities.ModificationPoint;
import fr.inria.astor.core.entities.OperatorInstance;
import fr.inria.astor.core.entities.StatementOperatorInstance;
import fr.inria.astor.core.entities.meta.MetaOperator;
import fr.inria.astor.core.entities.meta.MetaOperatorInstance;
import fr.inria.astor.core.manipulation.MutationSupporter;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.cu.position.NoSourcePosition;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

/**
 * 
 * @author Matias Martinez
 *
 */
public class LogicRedOperator extends FineGrainedExpressionReplaceOperator implements MetaOperator {

	@Override
	public List<MetaOperatorInstance> createMetaOperatorInstances(ModificationPoint modificationPoint) {

		List<MetaOperatorInstance> opsMega = new ArrayList();

		CtType<?> target = modificationPoint.getCodeElement().getParent(CtType.class);
		Set<ModifierKind> modifiers = new HashSet<>();
		modifiers.add(ModifierKind.PRIVATE);
//		modifiers.add(ModifierKind.STATIC);

		CtTypeReference returnTypeBoolean = MutationSupporter.getFactory().createCtTypeReference(Boolean.class);

		// get all binary expressions
		List<CtExpression<Boolean>> booleanExpressionsInModificationPoints = modificationPoint.getCodeElement()
				.getElements(e -> e.getType() != null
						// we need the target is a binary
						&& e instanceof CtBinaryOperator && SupportOperators.isBooleanType(e));

		// Let's transform it
		List<CtBinaryOperator> binOperators = booleanExpressionsInModificationPoints.stream()
				.map(CtBinaryOperator.class::cast)
				.filter(op -> op.getKind().equals(BinaryOperatorKind.AND) || op.getKind().equals(BinaryOperatorKind.OR))
				.collect(Collectors.toList());

		log.debug("\nLogicExp: \n" + modificationPoint);

		// let's start with one, and let's keep the Zero for the default (all ifs are
		// false)
		// TODO: we only can activate one mutant
		int candidateNumber = 0;

		// As difference with var replacement, a metamutant for each expression
		for (CtBinaryOperator binaryToReduce : binOperators) {

			List<OperatorInstance> opsOfVariant = new ArrayList();

			int variableCounter = 0;
			Map<Integer, Ingredient> ingredientOfMapped = new HashMap<>();

			List<Ingredient> ingredients = this.computeIngredientsFromOperatorToReduce(modificationPoint,
					binaryToReduce);

			// The parameters to be included in the new method
			List<CtVariableAccess> varsToBeParameters = new ArrayList<>();

			// The variable from the existing expression must also be a parameter
			List<CtVariableAccess> varsFromExpression = modificationPoint.getCodeElement()
					.getElements(e -> e instanceof CtVariableAccess);
			for (CtVariableAccess ctVariableAccess : varsFromExpression) {
				if (!varsToBeParameters.contains(ctVariableAccess))
					varsToBeParameters.add(ctVariableAccess);

			}

			// List of parameters
			List<CtParameter<?>> parameters = new ArrayList<>();
			List<CtExpression<?>> realParameters = new ArrayList<>();
			for (CtVariableAccess ctVariableAccess : varsToBeParameters) {
				// the parent is null, it is setter latter
				CtParameter pari = MutationSupporter.getFactory().createParameter(null, ctVariableAccess.getType(),
						ctVariableAccess.getVariable().getSimpleName());
				parameters.add(pari);
				realParameters.add(ctVariableAccess.clone().setPositions(new NoSourcePosition()));
			}

			variableCounter++;
			CtTypeReference returnType = MutationSupporter.getFactory().createCtTypeReference(Boolean.class);

			MetaOperatorInstance megaOp = MetaGenerator.createMetaFineGrainedReplacement(modificationPoint, binaryToReduce, variableCounter,
					ingredients, parameters, realParameters, this, returnType);
			opsMega.add(megaOp);

		} // End variable

		return opsMega;
	}

	private List<Ingredient> computeIngredientsFromOperatorToReduce(ModificationPoint modificationPoint,
			CtBinaryOperator binaryToReduce) {

		List<Ingredient> ingredientsReducedExpressions = new ArrayList();

		CtExpression left = binaryToReduce.getLeftHandOperand().clone();
		addOperator(ingredientsReducedExpressions, binaryToReduce, left);
		CtExpression right = binaryToReduce.getRightHandOperand().clone();
		addOperator(ingredientsReducedExpressions, binaryToReduce, right);

		return ingredientsReducedExpressions;
	}

	public void addOperator(List<Ingredient> ingredientsReducedExpressions, CtBinaryOperator binaryOperator,
			CtExpression subterm) {

		MutationSupporter.clearPosition(subterm);
		Ingredient newIngredientExtended = new Ingredient(subterm);
		newIngredientExtended.setDerivedFrom(binaryOperator);
		ingredientsReducedExpressions.add(newIngredientExtended);
	}

	@Override
	public OperatorInstance getConcreteOperatorInstance(MetaOperatorInstance operatorInstance, int metaIdentifier) {

		Ingredient ingredient = operatorInstance.getAllIngredients().get(metaIdentifier);

		ModificationPoint modificationPoint = operatorInstance.getModificationPoint();

		CtExpression expressionSource = (CtExpression) ingredient.getDerivedFrom();
		CtExpression expressionTarget = (CtExpression) ingredient.getCode();

		System.out.println("Target element to clean " + expressionTarget);

		MutationSupporter.clearPosition(expressionTarget);

		List<OperatorInstance> opsOfVariant = new ArrayList();

		OperatorInstance opInstace = new StatementOperatorInstance(modificationPoint, this, expressionSource,
				expressionTarget);
		opsOfVariant.add(opInstace);

		return opInstace;
	}

	@Override
	public boolean canBeAppliedToPoint(ModificationPoint point) {

		// See that the modification points are statements
		return (point.getCodeElement() instanceof CtStatement
				// Let's check we have a binary expression
				&& (point.getCodeElement() instanceof CtBinaryOperator
						// the element has a binary expression
						|| point.getCodeElement().getElements(new TypeFilter<>(CtBinaryOperator.class)).size() > 0));
	}

}
