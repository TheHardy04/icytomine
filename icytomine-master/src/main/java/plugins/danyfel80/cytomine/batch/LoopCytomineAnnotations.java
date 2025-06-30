/**
 * 
 */
package plugins.danyfel80.cytomine.batch;

import java.util.List;

import org.bioimageanalysis.icy.icytomine.core.model.AbstractAnnotation;

import plugins.adufour.blocks.lang.Loop;
import plugins.adufour.blocks.util.VarList;
import plugins.adufour.vars.lang.Var;
import plugins.adufour.vars.lang.VarGenericArray;
import vars.cytomine.VarCytomineAbstractAnnotation;

/**
 * @author Daniel Felipe Gonzalez Obando
 */
public class LoopCytomineAnnotations extends Loop
{

    @Override
    public void declareInput(VarList inputMap)
    {
        super.declareInput(inputMap);
        setInputMap(inputMap);
        initializeInputVariables();
        addInputVariables();
    }

    private VarList inputVarMap;

    private void setInputMap(VarList inputMap)
    {
        this.inputVarMap = inputMap;
    }

    private VarGenericArray<AbstractAnnotation[]> varInAnnotations;

    private void initializeInputVariables()
    {
        this.varInAnnotations = new VarGenericArray<AbstractAnnotation[]>("Annotations", AbstractAnnotation[].class,
                new AbstractAnnotation[0]);
    }

    private void addInputVariables()
    {
        inputVarMap.add(varInAnnotations.getName(), varInAnnotations);
    }

    private VarCytomineAbstractAnnotation varOutCurrentAnnotation;

    @Override
    public void declareOutput(VarList outputMap)
    {
        super.declareOutput(outputMap);
        varOutCurrentAnnotation = VarCytomineAbstractAnnotation.ofNullable(null);
        varOutCurrentAnnotation.setEnabled(false);
        outputMap.add(varOutCurrentAnnotation.getName(), varOutCurrentAnnotation);
    }

    @Override
    public void declareLoopVariables(List<Var<?>> loopVars)
    {
        for (Var<?> var : inputVarMap)
        {
            loopVars.add(var);
        }
        loopVars.add(varOutCurrentAnnotation);
    }

    @Override
    public void initializeLoop()
    {
        readParameters();
        initializeIterators();
    }

    private AbstractAnnotation[] targetAnnotations;

    private void readParameters()
    {
        this.targetAnnotations = varInAnnotations.getValue();
    }

    private int currentAnnotationIndex;

    private void initializeIterators()
    {
        currentAnnotationIndex = 0;
    }

    @Override
    public boolean isStopConditionReached()
    {
        return !(currentAnnotationIndex < targetAnnotations.length);
    }

    @Override
    public void beforeIteration()
    {
        varOutCurrentAnnotation.setValue(targetAnnotations[currentAnnotationIndex]);
    }

    @Override
    public void afterIteration()
    {
        currentAnnotationIndex++;
        super.afterIteration();
    }
}
