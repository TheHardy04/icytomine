package org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.abstractannotations.layers;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

import org.bioimageanalysis.icy.icytomine.core.model.AbstractAnnotation;
import org.bioimageanalysis.icy.icytomine.core.model.Entity;

public class AnnotationLayer
{
    public static enum LayerType
    {
        USER, ALGORITHM, REVIEW
    }

    private String name;
    private LayerType type;
    private Entity typeEntity;
    private Set<AbstractAnnotation> includedAnnotations;

    public AnnotationLayer(String name, LayerType type, Entity typeEntity, Set<AbstractAnnotation> includedAnnotations)
            throws NullPointerException
    {
        Objects.requireNonNull(name);
        Objects.requireNonNull(includedAnnotations);
        Objects.requireNonNull(type);

        this.name = name;
        this.type = type;
        this.typeEntity = typeEntity;
        this.includedAnnotations = includedAnnotations;
    }

    public String getName()
    {
        return name;
    }

    public LayerType getLayerType()
    {
        return type;
    }

    public Entity getLayerTypeEntity()
    {
        return typeEntity;
    }

    public Set<AbstractAnnotation> getIncludedAnnotations()
    {
        return Collections.unmodifiableSet(includedAnnotations);
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AnnotationLayer other = (AnnotationLayer) obj;
        if (name == null)
        {
            if (other.name != null)
                return false;
        }
        else if (!name.equals(other.name))
            return false;
        return true;
    }

    @Override
    public String toString()
    {
        return name;
    }

}
