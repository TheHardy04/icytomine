/**
 * 
 */
package org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.abstractannotations.actions;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;

import org.bioimageanalysis.icy.icytomine.core.model.Term;
import org.bioimageanalysis.icy.icytomine.ui.general.JCheckableItem;
import org.bioimageanalysis.icy.icytomine.ui.general.JCheckedComboBox;

import danyfel80.common.stream.StreamUtils;

/**
 * @author Daniel Felipe Gonzalez Obando
 */
public class AnnotationTermSelector extends JPanel
{
    private static final long serialVersionUID = 5914334147655073551L;

    private static class TermItem
    {
        public static final TermItem NO_TERM = new TermItem(null);

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
                return term.getName().orElse("Not specified");
            else
                return "No term";
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
            }
            else if (!term.equals(other.term))
            {
                return false;
            }
            return true;
        }

    }

    private JCheckedComboBox<TermItem> checkedComboBox;

    /**
     * Creates the UI.
     */
    public AnnotationTermSelector()
    {
        setGridBagLayout();
        addCheckedComboBox();

    }

    private void setGridBagLayout()
    {
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[] {1, 0};
        gridBagLayout.rowHeights = new int[] {1, 0};
        gridBagLayout.columnWeights = new double[] {1.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[] {0.0, Double.MIN_VALUE};
        setLayout(gridBagLayout);
    }

    private void addCheckedComboBox()
    {
        checkedComboBox = new JCheckedComboBox<>();
        GridBagConstraints gbc_checkedComboBox = new GridBagConstraints();
        gbc_checkedComboBox.fill = GridBagConstraints.HORIZONTAL;
        gbc_checkedComboBox.gridx = 0;
        gbc_checkedComboBox.gridy = 0;
        add(checkedComboBox, gbc_checkedComboBox);
    }

    public void setAvailableTerms(Collection<Term> terms)
    {
        Set<TermItem> availableTerms = terms.stream()
                .map(StreamUtils.wrapFunction(t -> new TermItem(t)))
                .collect(Collectors.toSet());
        availableTerms.add(TermItem.NO_TERM);
        checkedComboBox.setModel(createSelectionModel(availableTerms));
        checkedComboBox.setSelectedItem(TermItem.NO_TERM);
    }

    private ComboBoxModel<JCheckableItem<TermItem>> createSelectionModel(Set<TermItem> termItems)
    {
        @SuppressWarnings("unchecked")
        ComboBoxModel<JCheckableItem<TermItem>> model = new DefaultComboBoxModel<>(termItems.stream()
                .map(StreamUtils.wrapFunction(t -> new JCheckableItem<TermItem>(t, t.toString(), false)))
                .toArray(JCheckableItem[]::new));
        return model;
    }

    public Set<Term> getAvailableTerms()
    {
        Set<Term> selectedTerms = new HashSet<>();
        ComboBoxModel<JCheckableItem<TermItem>> model = checkedComboBox.getModel();
        for (int i = 0; i < model.getSize(); i++)
        {
            JCheckableItem<TermItem> item = model.getElementAt(i);
            if (item.getObject() != TermItem.NO_TERM)
                selectedTerms.add(item.getObject().getTerm());
        }
        return selectedTerms;
    }

    public void setSelectedTerms(Set<Term> terms)
    {
        ComboBoxModel<JCheckableItem<TermItem>> model = checkedComboBox.getModel();
        for (int i = 0; i < model.getSize(); i++)
        {
            JCheckableItem<TermItem> checkableItem = model.getElementAt(i);

            if (checkableItem.getObject() == TermItem.NO_TERM)
            {
                if (terms.isEmpty())
                {
                    checkableItem.setSelected(true);
                }
            }
            else if (terms.contains(checkableItem.getObject().getTerm()))
            {
                checkableItem.setSelected(true);
            }
            else
            {
                checkableItem.setSelected(false);
            }
        }
        checkedComboBox.updateUI();
    }

    public Set<Term> getSelectedTerms()
    {
        Set<Term> selectedTerms = new HashSet<>();
        ComboBoxModel<JCheckableItem<TermItem>> model = checkedComboBox.getModel();
        for (int i = 0; i < model.getSize(); i++)
        {
            JCheckableItem<TermItem> item = model.getElementAt(i);
            if (item.isSelected() && item.getObject() != TermItem.NO_TERM)
                selectedTerms.add(item.getObject().getTerm());
        }
        return selectedTerms;
    }

}
