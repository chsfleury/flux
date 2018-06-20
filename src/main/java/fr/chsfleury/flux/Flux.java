package fr.chsfleury.flux;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Flux {

    private String title;
    private String subtitle;

    private List<Article> articles;

    public Flux(String title, String subtitle) {
        this.title = title;
        this.subtitle = subtitle;
        this.articles = new ArrayList<>();
    }
}
