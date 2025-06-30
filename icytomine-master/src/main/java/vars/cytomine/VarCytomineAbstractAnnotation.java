package vars.cytomine;

import org.bioimageanalysis.icy.icytomine.core.model.AbstractAnnotation;

import plugins.adufour.vars.lang.Var;

/**
 * @author Daniel Felipe Gonzalez Obando
 */
public class VarCytomineAbstractAnnotation extends Var<AbstractAnnotation>
{
    public static VarCytomineAbstractAnnotation of(AbstractAnnotation annotation) throws IllegalArgumentException
    {
        if (annotation == null)
            throw new IllegalArgumentException("Cannot create an annotation variable from null annotation");
        return new VarCytomineAbstractAnnotation("Annotation", annotation);
    }

    public static VarCytomineAbstractAnnotation ofNullable(AbstractAnnotation annotation)
    {
        return new VarCytomineAbstractAnnotation("Annotation", annotation);
    }

    private VarCytomineAbstractAnnotation(String name, AbstractAnnotation defaultValue)
    {
        super(name, AbstractAnnotation.class, defaultValue, null);
    }
}
