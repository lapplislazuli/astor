package fr.inria.astor.core.loop.spaces.ingredients.scopes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.log4j.Logger;

import com.martiansoftware.jsap.JSAPException;

import fr.inria.astor.core.entities.ProgramVariant;
import fr.inria.astor.core.manipulation.filters.AbstractFixSpaceProcessor;
import fr.inria.astor.core.setup.ConfigurationProperties;
import spoon.reflect.code.CtCodeElement;
import spoon.reflect.code.CtExpression;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.declaration.CtType;

/**
 * 
 * @author Matias Martinez
 *
 */
public class ExpressionIngredientSpace extends AstorCtIngredientSpace {

	MultiKeyMap mkp = new MultiKeyMap();

	protected Logger log = Logger.getLogger(this.getClass().getName());

	public ExpressionIngredientSpace(List<AbstractFixSpaceProcessor<?>> processors) throws JSAPException {
		super(processors);
	}

	@Override
	public void defineSpace(ProgramVariant variant) {
		// List<CtType<?>> affected =
		// MutationSupporter.getFactory().Type().getAll();
		List<CtType<?>> affected = variant.getAffectedClasses();
		log.debug("Creating Expression Ingredient space: ");
		for (CtType<?> classToProcess : affected) {

			List<CtCodeElement> ingredients = this.ingredientProcessor.createFixSpace(classToProcess);
			AbstractFixSpaceProcessor.mustClone = true;

			for (CtCodeElement ctIngredient : ingredients) {
				String keyLocation = mapKey(ctIngredient);
				if (ctIngredient instanceof CtExpression) {
					CtExpression ctExpr = (CtExpression) ctIngredient;
					String typeExpression = ctExpr.getClass().getSimpleName();

					if (ctExpr.getType() == null) {
						continue;
					}

					String returnTypeExpression = (ctExpr.getType() != null) ? ctExpr.getType().getSimpleName()
							: "null";
					List<CtCodeElement> ingredientsKey = (List<CtCodeElement>) mkp.get(keyLocation, typeExpression,
							returnTypeExpression);

					if (!mkp.containsKey(keyLocation, typeExpression, returnTypeExpression)) {
						ingredientsKey = new CacheList<CtCodeElement>();
						mkp.put(keyLocation, typeExpression, returnTypeExpression, ingredientsKey);
						log.debug(" Adding new key location: " + keyLocation + " " + typeExpression + " "
								+ returnTypeExpression);
					}
					if (ConfigurationProperties.getPropertyBool("duplicateingredientsinspace")
							|| !ingredientsKey.contains(ctIngredient)) {
						ingredientsKey.add(ctIngredient);
						log.debug("Adding ingredient: "+ ctIngredient);
					}
				}
			}
		}
		int nrIng = 0;
		//Printing summary: 
		for (Object ingList : mkp.values()) {
			nrIng+= ((List)ingList).size();
		}
		log.info("".format("Ingredient search space info : number keys %d , number values %d ", mkp.keySet().size(), nrIng));

	}

	@Override
	public IngredientSpaceScope spaceScope() {
		return null;
	}

	@Override
	public String calculateLocation(CtElement elementToModify) {

		return elementToModify.getParent(CtPackage.class).getQualifiedName();
	}

	@Override
	protected String getType(CtCodeElement element) {

		return element.getClass().getSimpleName();
	}

	@Override
	public List<CtCodeElement> getIngredients(CtElement location) {
		log.error("Not supported operation");
		return null;
	}

	@Override
	public List<CtCodeElement> getIngredients(CtElement element, String type) {
		
		if (element instanceof CtExpression) {
			String keyLocation = mapKey(element);
			CtExpression ctExpr = (CtExpression) element;
			String typeExpression = ctExpr.getClass().getSimpleName();
			String returnTypeExpression = (ctExpr.getType() == null)? "null" :ctExpr.getType().getSimpleName();
			List ingredients = (List<CtCodeElement>) mkp.get(keyLocation, typeExpression, returnTypeExpression);
			return ingredients;
		}
		log.error("Element is not a expression: " + element.getClass().getCanonicalName());
		return null;
	}

	@Override
	public List<String> getLocations() {
		List<String> keys = new ArrayList<>(mkp.keySet());
		return keys;
	}

	@Override
	public List<CtCodeElement> getAllIngredients() {
		List<CtCodeElement> allIngredients = new ArrayList<>();
		for (Iterator iterator = mkp.values().iterator(); iterator.hasNext();) {
			allIngredients.addAll((List) iterator.next());
		}
		return allIngredients;
	}


}