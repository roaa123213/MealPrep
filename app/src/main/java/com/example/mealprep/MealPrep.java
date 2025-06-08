package com.example.mealprep;

import java.io.Serializable;

public class MealPrep implements Serializable {

    private String id;
    private String name ;
    private String calories;
    private String prepTime;
    private String ingredients;
    private String photo;

    public MealPrep (){

    }

    public MealPrep(String name, String calories, String prepTime, String ingredients, String photo) {
        this.name = name;
        this.calories = calories;
        this.prepTime = prepTime;
        this.ingredients = ingredients;
        this.photo = photo;

    }


    public String getId(){return id;}

    public void setId(String id){
        this.id=id;
    }
    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getIngredients() {
        return ingredients;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    public String getPrepTime() {
        return prepTime;
    }

    public void setPrepTime(String prepTime) {
        this.prepTime = prepTime;
    }

    public String getCalories() {
        return calories;
    }

    public void setCalories(String calories) {
        this.calories = calories;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Medicine{" +
                "name='" + name + '\'' +
                ", calories='" + calories + '\'' +
                ", prepTime='" + prepTime + '\'' +
                ", ingredients='" + ingredients + '\'' +
                ", photo='" + photo + '\'' +
                '}';
    }


}
