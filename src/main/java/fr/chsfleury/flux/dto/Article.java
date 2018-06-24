package fr.chsfleury.flux.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class Article {

    private String title;
    private String subtitle;
    private String content;
    private List<String> tags;

}
