package fr.chsfleury.flux.controllers;

import ratpack.handling.Context;

/**
 * @author Charles Fleury
 * @since 26/06/18.
 */
public class HomeController {

    public static void get(final Context ctx) {
        ctx.render("Hello World!");
    }

}
