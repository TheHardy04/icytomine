/**
 * 
 */
package plugins.danyfel80.cytomine.annotation;

import java.awt.Dimension;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.bioimageanalysis.icy.icytomine.core.image.annotation.AnnotationInserter;
import org.bioimageanalysis.icy.icytomine.core.image.importer.TiledImageImporter;
import org.bioimageanalysis.icy.icytomine.core.model.AbstractAnnotation;
import org.bioimageanalysis.icy.icytomine.core.view.converters.MagnitudeResolutionConverter;

import icy.plugin.abstract_.PluginActionable;
import icy.sequence.Sequence;
import plugins.adufour.blocks.lang.Block;
import plugins.adufour.blocks.util.VarList;
import plugins.adufour.vars.lang.VarInteger;
import plugins.adufour.vars.lang.VarSequence;
import vars.cytomine.VarCytomineAbstractAnnotation;
import vars.geom.VarDimension;

/**
 * @author Daniel Felipe Gonzalez Obando
 */
public class GetCytomineAnnotationImage extends PluginActionable implements Block
{
    private VarCytomineAbstractAnnotation varInTargetAnnotation;
    private VarInteger varInResolutionLevel;
    private VarDimension varInPaddingSize;

    @Override
    public void declareInput(VarList inputMap)
    {
        varInTargetAnnotation = VarCytomineAbstractAnnotation.ofNullable(null);
        varInResolutionLevel = new VarInteger("Resolution level", 0);
        varInPaddingSize = new VarDimension("Padding size");

        inputMap.add(varInTargetAnnotation.getName(), varInTargetAnnotation);
        inputMap.add(varInResolutionLevel.getName(), varInResolutionLevel);
        inputMap.add(varInPaddingSize.getName(), varInPaddingSize);
    }

    private VarSequence varOutAnnotationSequence;

    @Override
    public void declareOutput(VarList outputMap)
    {
        varOutAnnotationSequence = new VarSequence("Annotation sequence", null);
        outputMap.add(varOutAnnotationSequence.getName(), varOutAnnotationSequence);
    }

    @Override
    public void run()
    {
        try
        {
            readParameters();
            computeTargetImageBounds();
            retrieveTargetImage();
            buildAnnotationSequence();
            setOutputValues();
        }
        finally
        {
            cleanReferences();
        }
    }

    private AbstractAnnotation targetAnnotation;
    private int resolutionLevel;
    private Dimension paddingSize;

    private void readParameters()
    {
        targetAnnotation = varInTargetAnnotation.getValue(true);
        resolutionLevel = varInResolutionLevel.getValue(true);
        paddingSize = Optional.ofNullable(varInPaddingSize.getValue()).orElse(new Dimension(0, 0));
    }

    private Rectangle2D targetImageBounds;

    private void computeTargetImageBounds()
    {
        targetImageBounds = targetAnnotation.getYAdjustedBounds();
        Dimension2D paddingAtResolutionZero =
                MagnitudeResolutionConverter.convertDimension2D(paddingSize, resolutionLevel, 0);
        double w = targetImageBounds.getWidth() + 2 * paddingAtResolutionZero.getWidth();
        double h = targetImageBounds.getHeight() + 2 * paddingAtResolutionZero.getHeight();
        targetImageBounds = new Rectangle2D.Double(targetImageBounds.getX() - paddingAtResolutionZero.getWidth(),
                targetImageBounds.getY() - paddingAtResolutionZero.getHeight(), w == 0 ? 1 : w, h == 0 ? 1 : h);
    }

    private BufferedImage targetImage;

    private void retrieveTargetImage() throws RuntimeException
    {
        TiledImageImporter importer = new TiledImageImporter(targetAnnotation.getImage());
        Future<BufferedImage> futureTileImage = importer.requestImage(resolutionLevel, targetImageBounds);
        try
        {
            targetImage = futureTileImage.get();
        }
        catch (InterruptedException | ExecutionException e)
        {
            throw new RuntimeException(e);
        }
    }

    private Sequence annotationSequence;

    private void buildAnnotationSequence()
    {
        annotationSequence = new Sequence(targetImage);
        insertAnnotationsIntoAnnotationSequence();
        setAnnotationSequenceMetadata();
    }

    private void insertAnnotationsIntoAnnotationSequence()
    {
        AnnotationInserter inserter = new AnnotationInserter(annotationSequence);
        Set<AbstractAnnotation> annotations = new HashSet<>();
        annotations.add(targetAnnotation);
        inserter.insertAnnotations(targetImageBounds, resolutionLevel, annotations, false);
    }

    private void setAnnotationSequenceMetadata()
    {
        double pixelSize = targetAnnotation.getImage().getResolution().orElse(1d);
        double scaleFactor = Math.pow(2, resolutionLevel);
        double pixelSizeAtTargetResolution = pixelSize * scaleFactor;

        annotationSequence.setPositionX(targetImageBounds.getX() * pixelSize);
        annotationSequence.setPositionY(targetImageBounds.getY() * pixelSize);
        annotationSequence.setPixelSizeX(pixelSizeAtTargetResolution);
        annotationSequence.setPixelSizeY(pixelSizeAtTargetResolution);
        annotationSequence
                .setName(targetAnnotation.getImage().getName().orElse("Imported image") + " at Annotation "
                        + targetAnnotation.getId());
    }

    private void setOutputValues()
    {
        varOutAnnotationSequence.setValue(annotationSequence);
    }

    private void cleanReferences()
    {
        this.targetAnnotation = null;
        this.paddingSize = null;
        this.targetImageBounds = null;
        this.targetImage = null;
        this.annotationSequence = null;
    }
}
