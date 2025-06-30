/**
 * 
 */
package plugins.danyfel80.cytomine.roi.annotation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import icy.plugin.abstract_.Plugin;
import icy.plugin.interface_.PluginROIDescriptor;
import icy.roi.ROI;
import icy.roi.ROIDescriptor;
import icy.sequence.Sequence;

/**
 * @author Daniel Felipe Gonzalez Obando
 */
public class RoiCytomineAnnotationMetadataDescriptors extends Plugin implements PluginROIDescriptor
{
    public static final String ID_TERMS = RoiCytomineAnnotationTermsDescriptor.ID;

    public static final RoiCytomineAnnotationTermsDescriptor termsDescriptor =
            new RoiCytomineAnnotationTermsDescriptor();

    @Override
    public List<ROIDescriptor> getDescriptors()
    {
        final List<ROIDescriptor> result = new ArrayList<>(1);

        result.add(termsDescriptor);

        return result;
    }

    @Override
    public Map<ROIDescriptor, Object> compute(ROI roi, Sequence sequence)
            throws UnsupportedOperationException, InterruptedException
    {
        final Map<ROIDescriptor, Object> result = new HashMap<ROIDescriptor, Object>();

        result.put(termsDescriptor, termsDescriptor.compute(roi, sequence));

        return result;
    }

}
