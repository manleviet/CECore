/*
 * CECore - Core components of a Configuration Environment
 *
 * Copyright (c) 2022
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.fma.featuremodel;

import at.tugraz.ist.ase.fm.core.*;
import lombok.Getter;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AnomalyAwareFeatureModel extends FeatureModel {
    @Getter
    private List<AnomalyAwareFeature> features;
    @Getter
    private FeatureModel featureModel;

    public AnomalyAwareFeatureModel(FeatureModel fm) {
        featureModel = fm;

        features = new ArrayList<>(Collections.emptyList());
        for (Feature f : fm.getBfFeatures()) {
            features.add(new AnomalyAwareFeature(f.getName(), f.getId()));
        }
    }

    @Override
    public void addFeature(@NonNull String fname, @NonNull String id) {
        featureModel.addFeature(fname, id);
        features.add(new AnomalyAwareFeature(fname, id));
    }

    public AnomalyAwareFeature getAnomalyAwareFeature(int index) {
        return features.get(index);
    }

    @Override
    public Feature getFeature(int index) {
        return features.get(index);
    }

    @Override
    public Feature getFeature(@NonNull String id) throws FeatureModelException {
        for (Feature f : features) {
            if (f.getId().equals(id)) {
                return f;
            }
        }

        return null;
    }

    @Override
    public int getNumOfFeatures() {
        return featureModel.getNumOfFeatures();
    }

    @Override
    public boolean isMandatoryFeature(@NonNull Feature feature) {
        return featureModel.isMandatoryFeature(feature);
    }

    @Override
    public boolean isOptionalFeature(@NonNull Feature feature) {
        return featureModel.isOptionalFeature(feature);
    }

    @Override
    public List<Feature> getRightSideOfRelationships(@NonNull Feature leftSide) throws FeatureModelException {
        return featureModel.getRightSideOfRelationships(leftSide);
    }

    @Override
    public List<Feature> getMandatoryParents(@NonNull Feature rightSide) throws FeatureModelException {
        return featureModel.getMandatoryParents(rightSide);
    }

    @Override
    public List<Relationship> getRelationshipsWith(@NonNull Feature feature) {
        return featureModel.getRelationshipsWith(feature);
    }

    @Override
    public void addRelationship(RelationshipType type, @NonNull Feature leftSide, @NonNull List<Feature> rightSide) {
        featureModel.addRelationship(type, leftSide, rightSide);
    }

    @Override
    public int getNumOfRelationships() {
        return featureModel.getNumOfRelationships();
    }

    @Override
    public int getNumOfRelationships(RelationshipType type) {
        return featureModel.getNumOfRelationships(type);
    }

    @Override
    public void addConstraint(RelationshipType type, @NonNull Feature leftSide, @NonNull List<Feature> rightSide) {
        featureModel.addConstraint(type, leftSide, rightSide);
    }

    @Override
    public void addConstraint(RelationshipType type, String constraint3CNF) {
        featureModel.addConstraint(type, constraint3CNF);
    }

    @Override
    public int getNumOfConstraints() {
        return featureModel.getNumOfConstraints();
    }

    @Override
    public String toString() {
        return featureModel.toString();
    }

    @Override
    public void dispose() {
        featureModel.dispose();
    }

    @Override
    public String getName() {
        return featureModel.getName();
    }

    @Override
    public List<Feature> getBfFeatures() {
        return featureModel.getBfFeatures();
    }

    @Override
    public List<Relationship> getRelationships() {
        return featureModel.getRelationships();
    }

    @Override
    public List<Relationship> getConstraints() {
        return featureModel.getConstraints();
    }

    @Override
    public boolean isConsistency() {
        return featureModel.isConsistency();
    }

    @Override
    public void setName(String name) {
        featureModel.setName(name);
    }

    @Override
    public void setConsistency(boolean consistency) {
        featureModel.setConsistency(consistency);
    }
}
