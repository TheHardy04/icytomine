/**
 * 
 */
package plugins.danyfel80.cytomine.annotation;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.bioimageanalysis.icy.icytomine.core.model.AbstractAnnotation;
import org.bioimageanalysis.icy.icytomine.core.model.AlgorithmAnnotation;
import org.bioimageanalysis.icy.icytomine.core.model.Term;
import org.bioimageanalysis.icy.icytomine.core.model.UserJob;

import com.vividsolutions.jts.geom.Geometry;

import icy.plugin.abstract_.Plugin;
import plugins.adufour.blocks.lang.Block;
import plugins.adufour.blocks.util.VarList;
import plugins.adufour.vars.lang.VarGenericArray;
import plugins.adufour.vars.lang.VarString;
import vars.cytomine.VarCytomineAbstractAnnotation;

/**
 * @author Daniel Felipe Gonzalez Obando
 */
public class GetCytomineAlgorithmAnnotationsInAnnotation extends Plugin implements Block
{

    private VarCytomineAbstractAnnotation varInTargetAnnotation;
    private VarString varInUserJobIdFilter;
    private VarString varInTermNameFilter;

    @Override
    public void declareInput(VarList inputMap)
    {
        varInTargetAnnotation = VarCytomineAbstractAnnotation.ofNullable(null);
        varInUserJobIdFilter = new VarString("User Job ids", "");
        varInTermNameFilter = new VarString("Term name", "No Term");

        inputMap.add(varInTargetAnnotation.getName(), varInTargetAnnotation);
        inputMap.add(varInUserJobIdFilter.getName(), varInUserJobIdFilter);
        inputMap.add(varInTermNameFilter.getName(), varInTermNameFilter);
    }

    private VarGenericArray<AbstractAnnotation[]> varOutAnnotations;

    @Override
    public void declareOutput(VarList outputMap)
    {
        varOutAnnotations = new VarGenericArray<AbstractAnnotation[]>("Annotations", AbstractAnnotation[].class,
                new AbstractAnnotation[0]);
        outputMap.add(varOutAnnotations.getName(), varOutAnnotations);
    }

    @Override
    public void run()
    {
        readParameters();
        computeAnnotations();
        setOutputValues();
    }

    private AbstractAnnotation targetAnnotation;
    private Set<String> userJobIds;
    private Set<String> termNames;

    private void readParameters()
    {
        this.targetAnnotation = varInTargetAnnotation.getValue(true);
        this.userJobIds = Arrays.stream(varInUserJobIdFilter.getValue(true).toLowerCase().split(" *, *")).distinct()
                .collect(Collectors.toSet());
        this.termNames =
                Arrays.stream(varInTermNameFilter.getValue(true).split(" *, *"))
                        .filter(t -> !t.isEmpty()).distinct()
                        .map(String::toLowerCase)
                        .collect(Collectors.toSet());
    }

    Set<AlgorithmAnnotation> resultAnnotations;

    private void computeAnnotations()
    {
        List<UserJob> userJobs = targetAnnotation.getImage().getProject().getUserJobs(false);
        Set<Long> targetUserJobIds = userJobs.stream()
                .filter(u -> userJobIds.contains(u.getId().toString()))
                .map(u -> u.getId())
                .collect(Collectors.toSet());
        Map<Long, AlgorithmAnnotation> foundAnnotations = targetUserJobIds.isEmpty()
            ? targetAnnotation.getClient().getImageAlgorithmAnnotationsAt(targetAnnotation.getImage().getId(),
                    targetAnnotation.getBounds())
            : targetAnnotation.getClient()
                    .getImageAlgorithmAnnotationsAt(targetAnnotation.getImage().getId(), targetUserJobIds,
                            targetAnnotation.getBounds());

        Set<Term> terms = targetAnnotation.getImage().getProject().getOntology().getTerms(false);
        Set<Term> targetTerms = terms.stream()
                .filter(t -> termNames.contains(t.getName().orElse("").toLowerCase()))
                .collect(Collectors.toSet());
        boolean containsNoTerm = termNames.contains("No Term");
        Geometry targetGeometry = targetAnnotation.getGeometryAtZeroResolution(false);
        
        resultAnnotations = foundAnnotations.values().stream()
                .filter(a -> !a.getId().equals(targetAnnotation.getId()))
                .filter(a -> {
                    Set<Term> aTerms = a.getAssociatedTerms();
                    return termNames.isEmpty()
                            || aTerms.stream().anyMatch(t -> targetTerms.contains(t))
                            || (containsNoTerm && aTerms.isEmpty());
                })
                .filter(a -> a.getGeometryAtZeroResolution(false).intersects(targetGeometry))
                .collect(Collectors.toSet());
    }

    private void setOutputValues()
    {
        varOutAnnotations.setValue(resultAnnotations.stream().toArray(AbstractAnnotation[]::new));
    }

}