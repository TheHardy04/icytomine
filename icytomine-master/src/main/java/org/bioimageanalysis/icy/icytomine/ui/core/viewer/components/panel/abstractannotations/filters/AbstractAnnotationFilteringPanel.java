package org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.abstractannotations.filters;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.JPanel;

import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClientException;
import org.bioimageanalysis.icy.icytomine.core.model.AbstractAnnotation;
import org.bioimageanalysis.icy.icytomine.core.model.Image;
import org.bioimageanalysis.icy.icytomine.core.model.Term;
import org.bioimageanalysis.icy.icytomine.core.model.User;
import org.bioimageanalysis.icy.icytomine.core.model.filters.abstrct.AbstractAnnotationFilter;
import org.bioimageanalysis.icy.icytomine.core.model.filters.abstrct.AbstractAnnotationFilter.AbstractAnnotationFilterUpdateListener;
import org.bioimageanalysis.icy.icytomine.core.model.filters.abstrct.AbstractAnnotationFilter.ComputationMode;
import org.bioimageanalysis.icy.icytomine.core.model.filters.abstrct.AbstractAnnotationFilterBypass;

/**
 * @author Daniel Felipe Gonzalez Obando
 */
public class AbstractAnnotationFilteringPanel extends JPanel
{
    private static final long serialVersionUID = -238444864338788296L;

    /**
     * 
     */
    public AbstractAnnotationFilteringPanel()
    {
        setupUI();
        setListenerLists();
        initializeFilterList();
        setUIListeners();
    }

    private void setupUI()
    {
        setGridBagLayout();
        addFilterAdditionPanel();
        setFilterPanelList();
        addFiltersPanel();
    }

    private GridBagLayout gridBagLayout;

    private void setGridBagLayout()
    {

        gridBagLayout = new GridBagLayout();
        gridBagLayout.rowWeights = new double[] {0.0, 1.0, Double.MIN_VALUE};
        gridBagLayout.rowHeights = new int[] {0, 0, 0};
        gridBagLayout.columnWeights = new double[] {1.0, Double.MIN_VALUE};
        gridBagLayout.columnWidths = new int[] {0, 0};
        setLayout(gridBagLayout);
    }

    private AbstractAnnotationFilterAdditionPanel abstractAnnotationFilterAdditionPanel;

    private void addFilterAdditionPanel()
    {
        abstractAnnotationFilterAdditionPanel = new AbstractAnnotationFilterAdditionPanel();
        GridBagLayout gridBagLayout_1 = (GridBagLayout) abstractAnnotationFilterAdditionPanel.getLayout();
        gridBagLayout_1.rowHeights = new int[] {0, 0};
        GridBagConstraints gbc_abstractAnnotationFilterAdditionPanel = new GridBagConstraints();
        gbc_abstractAnnotationFilterAdditionPanel.fill = GridBagConstraints.BOTH;
        gbc_abstractAnnotationFilterAdditionPanel.gridx = 0;
        gbc_abstractAnnotationFilterAdditionPanel.gridy = 0;
        add(abstractAnnotationFilterAdditionPanel, gbc_abstractAnnotationFilterAdditionPanel);
    }

    private List<AbstractAnnotationFilterPanel<?>> addedFilterPanels;
    private JPanel panelFilters;

    private void setFilterPanelList()
    {
        addedFilterPanels = new ArrayList<>();
    }

    private void addFiltersPanel()
    {
        panelFilters = new JPanel();
        GridBagConstraints gbc_panelFilters = new GridBagConstraints();
        gbc_panelFilters.fill = GridBagConstraints.BOTH;
        gbc_panelFilters.gridx = 0;
        gbc_panelFilters.gridy = 1;
        add(panelFilters, gbc_panelFilters);
        GridBagLayout gbl_panelFilters = new GridBagLayout();
        gbl_panelFilters.columnWidths = new int[] {0};
        gbl_panelFilters.rowHeights = new int[] {1};
        gbl_panelFilters.columnWeights = new double[] {Double.MIN_VALUE};
        gbl_panelFilters.rowWeights = new double[] {Double.MIN_VALUE};
        panelFilters.setLayout(gbl_panelFilters);
    }

    private List<AbstractAnnotationFilterUpdateListener> filterUpdateListeners;

    private void setListenerLists()
    {
        filterUpdateListeners = new ArrayList<>();
    }

    private LinkedList<AbstractAnnotationFilter> activeFilters;

    private void initializeFilterList()
    {
        activeFilters = new LinkedList<>();
        AbstractAnnotationFilterBypass firstFilter = new AbstractAnnotationFilterBypass();
        // firstFilter.addAnnotationFilterUpdateListener(this::fireUpdateEvent);
        activeFilters.add(firstFilter);
    }

    // private void fireUpdateEvent(Set<AbstractAnnotation> newActiveAnnotations)
    // {
    // filterUpdateListeners.forEach(l -> l.filterUpdated(newActiveAnnotations));
    // }

    private void setUIListeners()
    {
        abstractAnnotationFilterAdditionPanel.addAnnotationFilterAdditionListener(this::onFilterAdditionRequested);
    }

    private void onFilterAdditionRequested(String filterName)
    {
        AbstractAnnotationFilterPanel<?> filterPanel = createRequestedFilterPanel(filterName);

        filterPanel.addRemoveButtonActionListener(e -> removeFilterPanel(filterPanel));
        AbstractAnnotationFilter prevFilter = getLastFilter();
        AbstractAnnotationFilter currFilter = filterPanel.getAnnotationFilter();
        for (AbstractAnnotationFilterUpdateListener listener : filterUpdateListeners)
        {
            prevFilter.removeAnnotationFilterUpdateListener(listener);
            currFilter.addAnnotationFilterUpdateListener(listener);
        }

        int row = addedFilterPanels.size();
        GridBagConstraints layoutConstraints = new GridBagConstraints();
        layoutConstraints.insets = new Insets(5, 0, 0, 0);
        layoutConstraints.fill = GridBagConstraints.HORIZONTAL;
        layoutConstraints.gridx = 0;
        layoutConstraints.gridy = row;
        panelFilters.add(filterPanel, layoutConstraints);
        panelFilters.revalidate();
        updateUI();
        repaint();

        activeFilters.add(currFilter);
        addedFilterPanels.add(filterPanel);
    }

    private AbstractAnnotationFilterPanel<?> createRequestedFilterPanel(String filterName)
    {
        AbstractAnnotationFilter lastFilter = getLastFilter();
        switch (filterName)
        {
            case "User":
                AbstractAnnotationFilterByUserPanel userFilterPanel = new AbstractAnnotationFilterByUserPanel();
                userFilterPanel.setPreviousFilter(lastFilter);
                userFilterPanel.setAvailableUsers(getImageUsers());
                return userFilterPanel;
            case "Term":
                AbstractAnnotationFilterByTermPanel termFilterPanel = new AbstractAnnotationFilterByTermPanel();
                termFilterPanel.setPreviousFilter(lastFilter);
                termFilterPanel.setAvailableTerms(getImageTerms());
                return termFilterPanel;
            default:
                return null;
        }
    }

    private AbstractAnnotationFilter getLastFilter()
    {
        return activeFilters.getLast();
    }

    private void removeFilterPanel(AbstractAnnotationFilterPanel<?> filterPanel)
    {
        panelFilters.remove(filterPanel);
        filterPanel.getAnnotationFilter().disconnect();
        addedFilterPanels.remove(filterPanel);
        activeFilters.remove(filterPanel.getAnnotationFilter());
        revalidate();
    }

    private Image image;

    public void setTargetImage(Image image)
    {
        this.image = image;
    }

    public Image getTargetImage()
    {
        return image;
    }

    private Set<User> getImageUsers()
    {
        try
        {
            return image == null ? Collections.emptySet() : new HashSet<>(image.getProject().getUsers(false));
        }
        catch (CytomineClientException e)
        {
            e.printStackTrace();
            return Collections.emptySet();
        }
    }

    private Set<Term> getImageTerms()
    {
        try
        {
            return image == null ? Collections.emptySet() : image.getProject().getOntology().getTerms(false);
        }
        catch (CytomineClientException e)
        {
            e.printStackTrace();
            return Collections.emptySet();
        }
    }

    public void setTargetAnnotations(Set<AbstractAnnotation> targetAnnotations)
    {
        ((AbstractAnnotationFilterBypass) activeFilters.getFirst()).setActiveAnnotations(targetAnnotations);
    }

    public Set<AbstractAnnotation> getActiveAnnotations()
    {
        return getLastFilter().getActiveAnnotations(ComputationMode.USE_LAST_RESULT);
    }

    public void addAnnotationFilterUpdateListener(AbstractAnnotationFilterUpdateListener listener)
    {
        filterUpdateListeners.add(listener);
    }

    public void removeAnnotationFilterUpdateListener(AbstractAnnotationFilterUpdateListener listener)
    {
        filterUpdateListeners.remove(listener);
    }
}
