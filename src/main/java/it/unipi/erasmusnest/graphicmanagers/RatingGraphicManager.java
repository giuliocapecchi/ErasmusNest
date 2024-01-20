package it.unipi.erasmusnest.graphicmanagers;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.util.ArrayList;
import java.util.Objects;

public class RatingGraphicManager {

    private final ArrayList<ImageView> ratingImages;
    private final Integer maxRating;

    public RatingGraphicManager(ArrayList<ImageView> ratingImages, Integer maxRating){
        this.ratingImages = ratingImages;
        this.maxRating = maxRating;
    }

    public void showRating(Double actualRating){
        for(int i = 0; i<maxRating; i++){
            setStarImage(actualRating, ratingImages.get(i));
            actualRating--;
        }
    }

    private void setStarImage(Double rating, ImageView imageView){
        String star = "/media/full_star.png";
        String half_star = "/media/half_star.png";
        String empty_star = "/media/empty_star.png";
        if(rating >= 1) {
            imageView.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream(star))));
        }else if(rating > 0.5) {
            imageView.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream(half_star))));
        }else {
            imageView.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream(empty_star))));
        }
        imageView.setSmooth(true);
    }

}
