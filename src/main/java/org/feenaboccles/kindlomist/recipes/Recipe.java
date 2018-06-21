package org.feenaboccles.kindlomist.recipes;


import lombok.Builder;
import lombok.Value;

import java.net.URI;
import java.util.List;

/** A BBC good food recipe */
@Value
@Builder
public class Recipe {
    enum Difficulty { Easy, Medium, Difficult };

    private final String title;
    private final String description;
    private final URI uri;
    private final int prepTimesMinutes;
    private final int cookTimeMinutes;

    private final int peopleServedCount;

    private final List<String> ingredientsList;
    private final List<String> steps;
    private final List<URI> relatedRecipes;

    private final double kcal;
    private final double fat;
    private final double saturates;
    private final double carbs;
    private final double sugars;
    private final double fibre;
    private final double protein;
    private final double salt;

}
