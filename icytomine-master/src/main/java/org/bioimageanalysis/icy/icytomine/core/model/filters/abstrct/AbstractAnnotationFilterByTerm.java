package org.bioimageanalysis.icy.icytomine.core.model.filters.abstrct;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClientException;
import org.bioimageanalysis.icy.icytomine.core.model.AbstractAnnotation;
import org.bioimageanalysis.icy.icytomine.core.model.Term;

import com.google.common.base.Objects;

/**
 * @author Daniel Felipe Gonzalez Obando
 */
public class AbstractAnnotationFilterByTerm extends AbstractAnnotationFilter
{
    public static class TermItem
    {
        public static final TermItem NO_TERM = new TermItem(null);
        public static final TermItem ALL = new TermItem(null);
        public static final TermItem NONE = new TermItem(null);

        private Term term;

        public TermItem(Term term)
        {
            this.term = term;
        }

        public Term getTerm()
        {
            return term;
        }

        @Override
        public String toString()
        {
            if (term != null)
            {
                return term.getName().orElse("Not specified");
            }
            else if (this == ALL)
            {
                return "Select All";
            }
            else if (this == NONE)
            {
                return "Select None";
            }
            else
            {
                return "No term";
            }
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((term == null) ? 0 : term.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj == null)
            {
                return false;
            }
            if (!(obj instanceof TermItem))
            {
                return false;
            }
            TermItem other = (TermItem) obj;
            if (term == null)
            {
                if (other.term != null)
                {
                    return false;
                }
                else
                {
                    if (this == ALL)
                        return other == ALL;
                    else if (this == NONE)
                        return other == NONE;
                    else if (this == NO_TERM)
                        return other == NO_TERM;
                    else
                        return false;
                }
            }
            else if (!term.equals(other.term))
            {
                return false;
            }
            return true;
        }
    }

    private Set<TermItem> activeTerms;

    /**
     * 
     */
    public AbstractAnnotationFilterByTerm()
    {
        activeTerms = new HashSet<>(5);
    }

    public void setActiveTerms(Set<TermItem> activeTerms)
    {
        Set<TermItem> previousTerms = this.activeTerms;
        this.activeTerms = activeTerms;
        if (!Objects.equal(previousTerms, this.activeTerms))
        {
            computeActiveAnnotations(ComputationMode.RECOMPUTE_JUST_THIS);
        }
    }

    public Set<TermItem> getActiveTerms()
    {
        return activeTerms;
    }

    @Override
    protected Set<AbstractAnnotation> applyFilter(Set<AbstractAnnotation> inputAnnotations)
    {
        return inputAnnotations.parallelStream().filter(a -> isActive(a)).collect(Collectors.toSet());
    }

    private boolean isActive(AbstractAnnotation a)
    {
        Set<Term> ts;
        try
        {
            ts = a.getAssociatedTerms();
        }
        catch (CytomineClientException e)
        {
            e.printStackTrace();
            ts = Collections.emptySet();
        }
        Set<TermItem> tItems = ts.stream().map(t -> new TermItem(t)).collect(Collectors.toSet());
        if (tItems.isEmpty())
        {
            tItems.add(TermItem.NO_TERM);
        }

        tItems.retainAll(getActiveTerms());
        return !tItems.isEmpty();
    }

}
