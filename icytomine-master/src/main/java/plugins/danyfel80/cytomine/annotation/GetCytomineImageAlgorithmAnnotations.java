package plugins.danyfel80.cytomine.annotation;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.bioimageanalysis.icy.icytomine.core.model.AbstractAnnotation;
import org.bioimageanalysis.icy.icytomine.core.model.AlgorithmAnnotation;
import org.bioimageanalysis.icy.icytomine.core.model.Image;
import org.bioimageanalysis.icy.icytomine.core.model.Term;
import org.bioimageanalysis.icy.icytomine.core.model.UserJob;

import icy.plugin.abstract_.Plugin;
import plugins.adufour.blocks.lang.Block;
import plugins.adufour.blocks.util.VarList;
import plugins.adufour.vars.lang.VarGenericArray;
import plugins.adufour.vars.lang.VarString;
import vars.cytomine.VarCytomineImage;

/**
 * @author Daniel Felipe Gonzalez Obando
 */
public class GetCytomineImageAlgorithmAnnotations extends Plugin implements Block
{

    private VarCytomineImage varInImage;
    private VarString varInUserJobIdFilter;
    private VarString varInTermNameFilter;

    @Override
    public void declareInput(VarList inputMap)
    {
        varInImage = VarCytomineImage.ofNullable(null);
        varInUserJobIdFilter = new VarString("User Job ids", "");
        varInTermNameFilter = new VarString("Term name", "No Term");

        inputMap.add(varInImage.getName(), varInImage);
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

    private Image targetImage;
    private Set<String> userJobIds;
    private Set<String> termNames;

    private void readParameters()
    {
        this.targetImage = varInImage.getValue(true);
        this.userJobIds = Arrays.stream(varInUserJobIdFilter.getValue(true).toLowerCase().split(" *, *")).distinct()
                .collect(Collectors.toSet());
        this.termNames = Arrays.stream(varInTermNameFilter.getValue(true).toLowerCase().split(" *, *"))
                .filter(t -> !t.isEmpty()).distinct()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }

    private Set<AlgorithmAnnotation> targetAnnotations;

    private void computeAnnotations()
    {
        List<UserJob> userJobs = targetImage.getProject().getUserJobs(false);
        Set<UserJob> targetUserJobs =
                userJobs.stream().filter(u -> userJobIds.contains(u.getId().toString()))
                        .collect(Collectors.toSet());

        Map<Long, AlgorithmAnnotation> algoAnnotations = targetUserJobs.isEmpty()
            ? targetImage.getClient().getImageAlgorithmAnnotations(targetImage)
            : targetImage.getClient().getImageAlgorithmAnnotations(targetImage,
                    targetUserJobs.stream().map(UserJob::getId).collect(Collectors.toSet()));

        Set<Term> terms = targetImage.getProject().getOntology().getTerms(false);
        Set<Term> targetTerms = terms.stream().filter(t -> termNames.contains(t.getName().orElse("").toLowerCase()))
                .collect(Collectors.toSet());
        boolean containsNoTerm = termNames.contains("No Term");

        targetAnnotations = algoAnnotations.values().stream()
                .filter(a -> {
                    Set<Term> aTerms = a.getAssociatedTerms();
                    return termNames.isEmpty()
                            || aTerms.stream().anyMatch(t -> targetTerms.contains(t))
                            || (containsNoTerm && aTerms.isEmpty());
                })
                .collect(Collectors.toSet());
    }

    private void setOutputValues()
    {
        varOutAnnotations.setValue(targetAnnotations.toArray(new AbstractAnnotation[targetAnnotations.size()]));
    }

}
