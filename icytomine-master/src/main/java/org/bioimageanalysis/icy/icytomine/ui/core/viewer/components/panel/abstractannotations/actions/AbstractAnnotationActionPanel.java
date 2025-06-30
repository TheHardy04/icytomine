/**
 * 
 */
package org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.abstractannotations.actions;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import org.bioimageanalysis.icy.icytomine.core.model.Term;

/**
 * Panel offering user multiple actions for the current annotation selection.
 * 
 * @author Daniel Felipe Gonzalez Obando
 */
public class AbstractAnnotationActionPanel extends JPanel
{
    private static final long serialVersionUID = 8091126232634101873L;

    public interface AnnotationTermAssociationListener
    {
        void onTermSelectionChangeRequested(Set<Term> selectedTerms);
    }

    /**
     * Creates the panel.
     */
    public AbstractAnnotationActionPanel()
    {
        setupUI();
        createListenerLists();
        setUIListeners();
    }

    private void setupUI()
    {
        setTitledBorder();
        setGridBagLayout();
        addTermAssociationControls();
        addAnnotationDeletionControls();
    }

    private void setTitledBorder()
    {
        setBorder(new TitledBorder(null, "Actions", TitledBorder.LEADING, TitledBorder.TOP, null, null));
    }

    private void setGridBagLayout()
    {
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[] {0, 0, 0, 0};
        gridBagLayout.rowHeights = new int[] {0, 0, 0};
        gridBagLayout.columnWeights = new double[] {0.0, 1.0, 0.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[] {0.0, 0.0, Double.MIN_VALUE};
        setLayout(gridBagLayout);
    }

    private AnnotationTermSelector termSelector;
    private JButton btnSetTerms;

    private void addTermAssociationControls()
    {
        JLabel lblAssociatedTerms = new JLabel("Associated terms");
        GridBagConstraints gbc_lblAssociatedTerms = new GridBagConstraints();
        gbc_lblAssociatedTerms.insets = new Insets(0, 0, 5, 5);
        gbc_lblAssociatedTerms.anchor = GridBagConstraints.EAST;
        gbc_lblAssociatedTerms.gridx = 0;
        gbc_lblAssociatedTerms.gridy = 0;
        add(lblAssociatedTerms, gbc_lblAssociatedTerms);

        termSelector = new AnnotationTermSelector();
        GridBagConstraints gbc_termSelector = new GridBagConstraints();
        gbc_termSelector.insets = new Insets(0, 0, 5, 5);
        gbc_termSelector.fill = GridBagConstraints.HORIZONTAL;
        gbc_termSelector.gridx = 1;
        gbc_termSelector.gridy = 0;
        add(termSelector, gbc_termSelector);

        btnSetTerms = new JButton("Set");
        GridBagConstraints gbc_btnSetTerms = new GridBagConstraints();
        gbc_btnSetTerms.insets = new Insets(0, 0, 5, 0);
        gbc_btnSetTerms.gridx = 2;
        gbc_btnSetTerms.gridy = 0;
        add(btnSetTerms, gbc_btnSetTerms);
    }

    private JButton btnDeleteAnnotation;

    private void addAnnotationDeletionControls()
    {
        JLabel lblDeleteAnnotation = new JLabel("Delete selected annotation(s)");
        GridBagConstraints gbc_lblDeleteAnnotation = new GridBagConstraints();
        gbc_lblDeleteAnnotation.anchor = GridBagConstraints.EAST;
        gbc_lblDeleteAnnotation.insets = new Insets(0, 0, 0, 5);
        gbc_lblDeleteAnnotation.gridx = 0;
        gbc_lblDeleteAnnotation.gridy = 1;
        add(lblDeleteAnnotation, gbc_lblDeleteAnnotation);

        btnDeleteAnnotation = new JButton("Delete");
        GridBagConstraints gbc_btnDeleteAnnotation = new GridBagConstraints();
        gbc_btnDeleteAnnotation.anchor = GridBagConstraints.WEST;
        gbc_btnDeleteAnnotation.insets = new Insets(0, 0, 0, 5);
        gbc_btnDeleteAnnotation.gridx = 1;
        gbc_btnDeleteAnnotation.gridy = 1;
        add(btnDeleteAnnotation, gbc_btnDeleteAnnotation);
    }

    private List<AnnotationTermAssociationListener> termAssociationChangeListeners;

    private void createListenerLists()
    {
        termAssociationChangeListeners = new ArrayList<>(1);
    }

    private void setUIListeners()
    {
        btnSetTerms.addActionListener(this::onTermAssociationRequested);
    }

    private void onTermAssociationRequested(ActionEvent event)
    {
        termAssociationChangeListeners.forEach(l -> l.onTermSelectionChangeRequested(termSelector.getSelectedTerms()));
    }

    /**
     * Sets the terms available on the term selector. To this set of term, an additional option is added (No Term).
     * 
     * @param terms
     *        The terms to be displayed in the term selector.
     */
    public void setAvailableTerms(Collection<Term> terms)
    {
        termSelector.setAvailableTerms(terms);
    }

    public Set<Term> getAvailableTerms()
    {
        return termSelector.getAvailableTerms();
    }

    /**
     * Sets the selected terms on the term selector. If the selection is empty, the "No Term" option is selected
     * 
     * @param terms
     *        Terms to set as selected.F
     */
    public void setSelectedTerms(Set<Term> terms)
    {
        termSelector.setSelectedTerms(terms);
    }

    /**
     * Adds an annotation term association event handler.
     * 
     * @param listener
     *        The listener handling these events.
     */
    public void addAnnotationTermAssociationListener(AnnotationTermAssociationListener listener)
    {
        this.termAssociationChangeListeners.add(listener);
    }

    /**
     * Removes an annotation term association event handler.
     * 
     * @param listener
     *        The listener handling these events.
     */
    public void removeAnnotationTermAssociationListener(AnnotationTermAssociationListener listener)
    {
        this.termAssociationChangeListeners.remove(listener);
    }

    /**
     * Adds an annotation deletion event handler.
     * 
     * @param listener
     *        The action listener handling these events.
     */
    public void addAnnotationDeleteListener(ActionListener listener)
    {
        this.btnDeleteAnnotation.addActionListener(listener);
    }

    /**
     * Removes an annotation deletion event handler.
     * 
     * @param listener
     *        The action listener handling these events.
     */
    public void removeAnnotationDeleteListener(ActionListener listener)
    {
        this.btnDeleteAnnotation.removeActionListener(listener);
    }
}
