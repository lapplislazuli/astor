package fr.inria.astor.core.entities;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.inria.astor.core.manipulation.bytecode.entities.CompilationResult;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtType;

/**
 * A ProgramVariant is a variant of a program. The variant can change over time.
 * 
 * Each program variant contains a list of "Gen", which represents all locations (i.e., statements) that can be modified for that variant during the whole evolution. For single-point repair, only one of those gens is affected by a mutation operator.. In case of multipoint repair (which can be
 * activated in astor by a command line argument), different gens from a ProgramVariant can be modified by mutation operator. Note that 2 gens could be modified a) in the same generation, and/or b) in different generations (thus the variant changes over time). For instance, in generation X you
 * modified gen at position i, and in the generation X+1 you modify position j. ProgramVariant contains a Map “operations” that tracks the history, i.e. the operations done over gens in each generation.
 * 
 * @author Matias Martinez, matias.martinez@inria.fr
 *
 */
public class ProgramVariant {
	

	public static String DEFAULT_ORIGINAL_VARIANT = "default";

	/**
	 * Variand ID
	 */
	protected int id = 0;
	
	/**
	 * List of gens (statements that can be modified for finding a patch) of the program 
	 */
	protected List<Gen> genList = null;
	/**
	 * Reference to the loaded classes from the spoon model
	 */
	protected Map<String, CtClass> loadClasses = new HashMap<String, CtClass>();
	
	/**
	 * operations applied to gen, organizated by generations
	 */
	protected Map<Integer,List<GenOperationInstance>> operations  = null;
	/**
	 * Fitness value of the variant	
	 */
	protected double fitness = Double.MAX_VALUE;
	
	/**
	 * Parent Variant
	 */
	protected ProgramVariant parent = null;
	/**
	 * Id of the generation this variant born
	 */
	protected int generationSource = 0;

	
	protected CompilationResult compilationResult = null;

	protected boolean isSolution = false;	
	/**
	 * When we want to analyze one gen per generation, 
	 * we need to track the last gen analyzed
	 */
	protected int lastGenAnalyzed = 0;
	
	/**
	 * Date the variant were born
	 */
	protected Date bornDate = new Date(); 

	public ProgramVariant(){
		genList = new ArrayList<Gen>();
		operations = new HashMap<Integer,List<GenOperationInstance>>();
	}

	public ProgramVariant(int id) {
		this();
		this.id = id;
	}

	public List<Gen> getGenList() {
		return genList;
	}

	public void setGenList(List<Gen> genList) {
		this.genList = genList;
	}

	public double getFitness() {
		return fitness;
	}

	public void setFitness(double fitness) {
		this.fitness = fitness;
	}

	public Map<String, CtClass> getBuiltClasses() {
		return loadClasses;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	public void putGenOperation(int generation, GenOperationInstance op){
		List<GenOperationInstance> genList = operations.get(generation);
		if(genList == null){
			genList = new ArrayList<GenOperationInstance>();
			operations.put(generation, genList);
		}
		genList.add(op);
		
	}
	public Map<Integer, List<GenOperationInstance>> getOperations() {
		return operations;
	}
	public List<GenOperationInstance> getOperations(int generation) {
		return  operations.get(generation);
	}

	
	public ProgramVariant getParent() {
		return parent;
	}

	public void setParent(ProgramVariant parent) {
		this.parent = parent;
	}
	
	public int getGenerationSource() {
		return generationSource;
	}

	public void setGenerationSource(int generationSource) {
		this.generationSource = generationSource;
	}

	
	public CompilationResult getCompilation() {
		return compilationResult;
	}

	public void setCompilation(CompilationResult compilation) {
		this.compilationResult = compilation;
	}
	
	@Override
	public String toString(){
		return "[Variant id: "+this.id+(this.isSolution()?" (SOL) ":"") +", #gens: "+this.getGenList().size()+ ", #ops: "+this.operations.values().size()+", parent:"+((this.parent==null)?"-":this.parent.id)+"]";
	}
	
	public String currentMutatorIdentifier() {
		return (id >= 0)? ( "variant-" + id) : DEFAULT_ORIGINAL_VARIANT;
	}
	
	public List<CtType<?>> getAffectedClasses(){
		List<CtType<?>> r = new ArrayList<CtType<?>>();
		for (CtClass c:loadClasses.values()) {
			r.add(c);
		}
		return r;
	}
	
	public boolean isSolution() {
		return isSolution;
	}

	public void setIsSolution(boolean solution) {
		this.isSolution = solution;
	}
	
	public void resetOperations(){
		this.operations.clear();
	}

	public int getLastGenAnalyzed() {
		return lastGenAnalyzed;
	}

	public void setLastGenAnalyzed(int lastGenAnalyzed) {
		this.lastGenAnalyzed = lastGenAnalyzed;
	}

	public Date getBornDate() {
		return bornDate;
	}

	public void setBornDate(Date bornDate) {
		this.bornDate = bornDate;
	}
}
