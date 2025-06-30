/**
 * 
 */
package plugins.danyfel80.cytomine.roi.annotation;

import java.util.Arrays;
import java.util.stream.Collectors;

import icy.roi.ROI;
import icy.roi.ROIDescriptor;
import icy.sequence.Sequence;

/**
 * Descriptor for Cytomine annotation ROIs
 * 
 * @author Daniel Felipe Gonzalez Obando
 */
public class RoiCytomineAnnotationTermsDescriptor extends ROIDescriptor
{
    public static final String ID = "Annotation Terms";

    public RoiCytomineAnnotationTermsDescriptor()
    {
        super(ID, "Annotation Terms", String.class);
    }

    @Override
    public String getDescription()
    {
        return "Cytomine annotation terms";
    }

    @Override
    public String compute(ROI roi, Sequence sequence) throws UnsupportedOperationException, InterruptedException
    {
        return getTerms(roi);
    }

    public static String getTerms(ROI roi)
    {
        String termsString = roi.getProperty("cytomine.terms");
        if (termsString == null || termsString.isEmpty())
            return "";
        return Arrays.stream(termsString.split("[\\[\\],]")).filter(s -> !s.isEmpty()).collect(Collectors.joining(","));
    }

}
