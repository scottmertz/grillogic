package grillogic.model;

/**
 * All measurement units GRILLOGIC supports for ingredients and recipes.
 * Using an enum instead of a String means invalid units (typos, casing
 * mismatches) are caught at compile time instead of causing silent bugs
 * at runtime — this is what the old Flask app's unit comparison bug came from.
 */
public enum Unit {

    // Weight units
    G,      // grams
    KG,     // kilograms
    OZ,     // ounces
    LB,     // pounds

    // Volume units
    ML,     // milliliters
    L,      // liters
    FL_OZ,  // fluid ounces
    CUP,
    TSP,    // teaspoon
    TBSP,   // tablespoon

    // Countable items — cannot convert to/from weight or volume
    EACH
}