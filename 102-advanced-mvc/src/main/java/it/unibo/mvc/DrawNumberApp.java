package it.unibo.mvc;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Application entry point for DrawNumber game.
 */
public final class DrawNumberApp implements DrawNumberViewObserver {

    private final DrawNumber model;
    private final List<DrawNumberView> views;

    /**
     * Constructor.
     *
     * @param views
     *            the views to attach
     */
    public DrawNumberApp(final DrawNumberView... views) {
        /*
         * Side-effect proof
         */
        this.views = Arrays.asList(Arrays.copyOf(views, views.length));
        for (final DrawNumberView view: views) {
            view.setObserver(this);
            view.start();
        }
        Configuration config;
        try {
            config = Configuration.fromResource("config.yml");
        } catch (final IOException | IllegalStateException | NumberFormatException e) {
            for (final DrawNumberView view: this.views) {
                view.displayError(e.getMessage());
            }
            config = new Configuration.Builder().build();
        }
        this.model = new DrawNumberImpl(config.getMin(), config.getMax(), config.getAttempts());
    }

    @Override
    public void newAttempt(final int n) {
        try {
            final DrawResult result = model.attempt(n);
            for (final DrawNumberView view: views) {
                view.result(result);
            }
        } catch (final IllegalArgumentException e) {
            for (final DrawNumberView view: views) {
                view.numberIncorrect();
            }
        }
    }

    @Override
    public void resetGame() {
        this.model.reset();
    }

    @Override
    @SuppressFBWarnings(
        value = "DM_EXIT",
        justification = "Acceptable for exercising purposes."
    )
    public void quit() {
        /*
         * A bit harsh. A good application should configure the graphics to exit by
         * natural termination when closing is hit. To do things more cleanly, attention
         * should be paid to alive threads, as the application would continue to persist
         * until the last thread terminates.
         */
        System.exit(0);
    }

    /**
     * Application entry point.
     *
     * @throws FileNotFoundException if the log.txt file cannot be created
     */
    public static void main() throws FileNotFoundException {
        new DrawNumberApp(new DrawNumberViewImpl(),
                            new DrawNumberViewImpl(),
                            new PrintStreamView("log.txt"), 
                            new PrintStreamView(System.out));
    }

}
