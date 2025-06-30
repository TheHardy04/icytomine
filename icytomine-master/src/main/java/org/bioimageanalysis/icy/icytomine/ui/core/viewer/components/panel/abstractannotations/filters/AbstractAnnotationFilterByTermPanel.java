/**
 * 
 */
package org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.abstractannotations.filters;

import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.ComboBoxModel;

import org.bioimageanalysis.icy.icytomine.core.model.Term;
import org.bioimageanalysis.icy.icytomine.core.model.filters.abstrct.AbstractAnnotationFilter;
import org.bioimageanalysis.icy.icytomine.core.model.filters.abstrct.AbstractAnnotationFilter.ComputationMode;
import org.bioimageanalysis.icy.icytomine.core.model.filters.abstrct.AbstractAnnotationFilterByTerm;
import org.bioimageanalysis.icy.icytomine.core.model.filters.abstrct.AbstractAnnotationFilterByTerm.TermItem;
import org.bioimageanalysis.icy.icytomine.ui.general.JCheckableItem;
import org.bioimageanalysis.icy.icytomine.ui.general.JCheckedComboBox;

/**
 * @author Daniel Felipe Gonzalez Obando
 */
public class AbstractAnnotationFilterByTermPanel extends AbstractAnnotationFilterPanel<TermItem>
{
    private static final long serialVersionUID = -2425902310352423296L;

    private AbstractAnnotationFilterByTerm termFilter;

    /**
     * 
     */
    public AbstractAnnotationFilterByTermPanel()
    {
        setLabelText("Terms:");
        termFilter = new AbstractAnnotationFilterByTerm();
        setAnnotationFilter(termFilter);
    }

    public void setPreviousFilter(AbstractAnnotationFilter previousFilter)
    {
        termFilter.setPreviousFilter(previousFilter);
    }

    private Set<TermItem> termItems;

    public void setAvailableTerms(Set<Term> terms)
    {
        termItems = terms.stream().map(t -> new TermItem(t)).collect(Collectors.toSet());
        termItems.add(TermItem.NO_TERM);
        termItems.add(TermItem.ALL);
        termItems.add(TermItem.NONE);

        termFilter.setActiveTerms(new HashSet<>(termItems));
        setModel(termItems.toArray(new TermItem[termItems.size()]), item -> item.toString(), item -> item != TermItem.NONE);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void choiceChanged(ActionEvent e)
    {
        JCheckableItem<TermItem> checkableItem = ((JCheckableItem<TermItem>) (((JCheckedComboBox<TermItem>) (e
                .getSource())).getSelectedItem()));
        TermItem termItem = checkableItem.object;
        if (termItem == TermItem.ALL)
        {
            Set<TermItem> activeTerms = termFilter.getActiveTerms();
            termItems.stream().filter(t -> t != TermItem.ALL && t != TermItem.NONE).forEach(t -> activeTerms.add(t));
            ComboBoxModel<JCheckableItem<TermItem>> model = ((JCheckedComboBox<TermItem>) (e.getSource())).getModel();
            for (int i = 0; i < model.getSize(); i++)
            {
                JCheckableItem<TermItem> elem = model.getElementAt(i);
                if (elem.getObject() == TermItem.NONE)
                    elem.setSelected(false);
                else
                    elem.setSelected(true);
            }
            ((JCheckedComboBox<TermItem>) (e.getSource())).invalidate();
        }
        else if (termItem == TermItem.NONE)
        {
            termFilter.getActiveTerms().removeAll(termItems);
            ComboBoxModel<JCheckableItem<TermItem>> model = ((JCheckedComboBox<TermItem>) (e.getSource())).getModel();
            for (int i = 0; i < model.getSize(); i++)
            {
                JCheckableItem<TermItem> elem = model.getElementAt(i);
                elem.setSelected(false);
            }
            ((JCheckedComboBox<TermItem>) (e.getSource())).invalidate();
        }
        else if (!checkableItem.isSelected())
        {
            termFilter.getActiveTerms().add(termItem);
        }
        else
        {
            termFilter.getActiveTerms().remove(termItem);
        }
        termFilter.computeActiveAnnotations(ComputationMode.RECOMPUTE_JUST_THIS);
    }

}
