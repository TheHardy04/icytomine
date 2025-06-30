/**
 * 
 */
package org.bioimageanalysis.icy.icytomine.core.model.filters.abstrct;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.bioimageanalysis.icy.icytomine.core.model.AbstractAnnotation;

/**
 * @author Daniel Felipe Gonzalez Obando
 */
public class AbstractAnnotationFilterBypass extends AbstractAnnotationFilter
{
    public AbstractAnnotationFilterBypass()
    {
        activeAnnotations = new HashSet<>();
    }

    public void setActiveAnnotations(Set<AbstractAnnotation> activeAnnotations)
    {
        Set<AbstractAnnotation> previousAnnotations = activeAnnotations;
        this.activeAnnotations = activeAnnotations;
        if (!Objects.equals(previousAnnotations, this.activeAnnotations))
        {
            notifyUpdateListeners();
        }
    }

    @Override
    public void computeActiveAnnotations(ComputationMode mode)
    {
        notifyUpdateListeners();
    }

    @Override
    protected Set<AbstractAnnotation> applyFilter(Set<AbstractAnnotation> inputAnnotations)
    {
        return inputAnnotations;
    }

}
