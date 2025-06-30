package org.bioimageanalysis.icy.icytomine.core.model.filters.abstrct;

import java.util.HashSet;
import java.util.Set;

import org.bioimageanalysis.icy.icytomine.core.model.AbstractAnnotation;

import com.google.common.base.Objects;

/**
 * An abstract implementation for abstract annotations filters.
 * 
 * @author Daniel Felipe Gonzalez Obando
 */
public abstract class AbstractAnnotationFilter
{

    public enum ComputationMode
    {
        RECOMPUTE_ALL,
        RECOMPUTE_JUST_THIS,
        USE_LAST_RESULT
    }

    public interface AbstractAnnotationFilterUpdateListener
    {
        void filterUpdated(Set<AbstractAnnotation> activeAnnotations);
    }

    private AbstractAnnotationFilter previousFilter;
    private AbstractAnnotationFilterUpdateListener previousFilterUpdateHandler;
    protected Set<AbstractAnnotation> activeAnnotations;
    private Set<AbstractAnnotationFilterUpdateListener> updateListeners;

    public AbstractAnnotationFilter()
    {
        updateListeners = new HashSet<>();
    }

    public void setPreviousFilter(AbstractAnnotationFilter newPreviousFilter)
    {
        if (!Objects.equal(this.previousFilter, newPreviousFilter))
        {
            if (this.previousFilter != null)
            {
                this.previousFilter.removeAnnotationFilterUpdateListener(getPreviousFilterUpdateHandler());
            }

            this.previousFilter = newPreviousFilter;

            if (this.previousFilter != null)
            {
                this.previousFilter.addAnnotationFilterUpdateListener(getPreviousFilterUpdateHandler());
                computeActiveAnnotations(ComputationMode.RECOMPUTE_JUST_THIS);
            }
        }
    }

    private AbstractAnnotationFilterUpdateListener getPreviousFilterUpdateHandler()
    {
        if (previousFilterUpdateHandler == null)
        {
            previousFilterUpdateHandler = activeAnnotations -> {
                computeActiveAnnotations(ComputationMode.RECOMPUTE_JUST_THIS);
            };
        }
        return previousFilterUpdateHandler;
    }

    public AbstractAnnotationFilter getPreviousFilter()
    {
        return previousFilter;
    }

    public Set<AbstractAnnotation> getActiveAnnotations(ComputationMode mode)
    {
        if (!isComputed() || mode != ComputationMode.USE_LAST_RESULT)
        {
            computeActiveAnnotations(mode);
        }
        return activeAnnotations;
    }

    private boolean isComputed()
    {
        return activeAnnotations != null;
    }

    public void computeActiveAnnotations(ComputationMode mode)
    {
        Set<AbstractAnnotation> inputAnnotations = null;
        if (mode == ComputationMode.USE_LAST_RESULT)
        {
            if (!isComputed())
            {
                inputAnnotations = previousFilter.getActiveAnnotations(mode);
                activeAnnotations = applyFilter(inputAnnotations);
                notifyUpdateListeners();
            }
        }
        else
        {
            if (mode == ComputationMode.RECOMPUTE_ALL)
            {
                inputAnnotations = previousFilter.getActiveAnnotations(mode);
            }
            else
            {
                inputAnnotations = previousFilter.getActiveAnnotations(ComputationMode.USE_LAST_RESULT);
            }
            activeAnnotations = applyFilter(inputAnnotations);
            notifyUpdateListeners();
        }
    }

    protected abstract Set<AbstractAnnotation> applyFilter(Set<AbstractAnnotation> inputAnnotations);

    protected void notifyUpdateListeners()
    {
        updateListeners.forEach(l -> l.filterUpdated(activeAnnotations));
    }

    public void addAnnotationFilterUpdateListener(AbstractAnnotationFilterUpdateListener listener)
    {
        this.updateListeners.add(listener);
    }

    public void removeAnnotationFilterUpdateListener(AbstractAnnotationFilterUpdateListener listener)
    {
        this.updateListeners.remove(listener);
    }

    public void disconnect()
    {
        AbstractAnnotationFilter previousFilter = getPreviousFilter();
        Set<AbstractAnnotationFilterUpdateListener> updateListenersCopy = new HashSet<>(updateListeners);
        for (AbstractAnnotationFilterUpdateListener listener : updateListenersCopy)
        {
            removeAnnotationFilterUpdateListener(listener);
            previousFilter.addAnnotationFilterUpdateListener(listener);
        }
        setPreviousFilter(null);
        previousFilter.computeActiveAnnotations(ComputationMode.RECOMPUTE_JUST_THIS);
    }

}
