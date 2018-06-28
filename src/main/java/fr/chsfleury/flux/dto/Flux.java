package fr.chsfleury.flux.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Flux {

    private final String title;
    private final String description;
    private final String url;
    private final List<Article> articles;

    public Flux(String title, String description, String url) {
        this.title = title;
        this.description = description;
        this.url = url;
        this.articles = new ArrayList<>();
    }
}
