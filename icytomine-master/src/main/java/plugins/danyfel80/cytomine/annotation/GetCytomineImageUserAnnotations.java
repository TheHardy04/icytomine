package plugins.danyfel80.cytomine.annotation;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.bioimageanalysis.icy.icytomine.core.model.AbstractAnnotation;
import org.bioimageanalysis.icy.icytomine.core.model.Image;
import org.bioimageanalysis.icy.icytomine.core.model.Term;
import org.bioimageanalysis.icy.icytomine.core.model.User;
import org.bioimageanalysis.icy.icytomine.core.model.UserAnnotation;

import icy.plugin.abstract_.Plugin;
import plugins.adufour.blocks.lang.Block;
import plugins.adufour.blocks.util.VarList;
import plugins.adufour.vars.lang.VarGenericArray;
import plugins.adufour.vars.lang.VarString;
import vars.cytomine.VarCytomineImage;

/**
 * @author Daniel Felipe Gonzalez Obando
 */
public class GetCytomineImageUserAnnotations extends Plugin implements Block
{
    private VarCytomineImage varInImage;
    private VarString varInUserNameFilter;
    private VarString varInTermNameFilter;

    @Override
    public void declareInput(VarList inputMap)
    {
        varInImage = VarCytomineImage.ofNullable(null);
        varInUserNameFilter = new VarString("User names", "noUser");
        varInTermNameFilter = new VarString("Term name", "No Term");

        inputMap.add(varInImage.getName(), varInImage);
        inputMap.add(varInUserNameFilter.getName(), varInUserNameFilter);
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
    private Set<String> usernames;
    private Set<String> termNames;

    private void readParameters()
    {
        this.targetImage = varInImage.getValue(true);
        this.usernames = Arrays.stream(varInUserNameFilter.getValue(true).toLowerCase().split(" *, *")).distinct()
                .collect(Collectors.toSet());
        this.termNames = Arrays.stream(varInTermNameFilter.getValue(true).split(" *, *"))
                .filter(t -> !t.isEmpty()).distinct()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }

    private Set<UserAnnotation> targetAnnotations;

    private void computeAnnotations()
    {
        List<User> users = targetImage.getProject().getUsers(false);
        Set<User> targetUsers =
                users.stream().filter(u -> usernames.contains(u.getName().orElse("").toLowerCase()))
                        .collect(Collectors.toSet());
        Set<Long> targetUserIds = targetUsers.stream().map(User::getId).collect(Collectors.toSet());

        Map<Long, UserAnnotation> userAnnotations = targetUsers.isEmpty()
            ? targetImage.getClient().getImageUserAnnotations(targetImage)
            : targetImage.getClient().getImageUserAnnotations(targetImage,targetUserIds);
        final Map<Long, UserAnnotation> userGeometries = targetUsers.isEmpty()
                ? targetImage.getClient().getImageUserAnnotationGeometries(targetImage)
                : targetImage.getClient().getImageUserAnnotationGeometries(targetImage, targetUserIds);

        Set<Term> terms = targetImage.getProject().getOntology().getTerms(false);
        Set<Term> targetTerms = terms.stream().filter(t -> termNames.contains(t.getName().orElse("").toLowerCase()))
                .collect(Collectors.toSet());
        boolean containsNoTerm = termNames.contains("No Term");

        targetAnnotations = userAnnotations.values().stream()
                .filter(a -> {
                    Set<Term> aTerms = a.getAssociatedTerms();
                    return termNames.isEmpty()
                            || aTerms.stream().anyMatch(t -> targetTerms.contains(t))
                            || (containsNoTerm && aTerms.isEmpty());
                })
                .collect(Collectors.toSet());
        
        for(UserAnnotation annot: targetAnnotations)
        {
            // find geometry for the annotation
            final UserAnnotation geometry = userGeometries.get(annot.getId());
            // set location (geometry) in annotation (faster to retrieve geometry then)
            if (geometry != null)
                annot.getInternalAnnotation().set("location", geometry.getInternalAnnotation().getStr("location"));
        }
    }

    private void setOutputValues()
    {
        varOutAnnotations.setValue(targetAnnotations.toArray(new AbstractAnnotation[targetAnnotations.size()]));
    }

}
