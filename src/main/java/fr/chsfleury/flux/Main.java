package fr.chsfleury.flux;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {

    /*
     * https://korben.info/feed > "main > article > div.entry-container"
     * https://www.lesnumeriques.com/rss.xml
     */

    public static void main(final String[] args) throws Exception {
        FluxApp app = new FluxApp();
        app.start();
    }
}
