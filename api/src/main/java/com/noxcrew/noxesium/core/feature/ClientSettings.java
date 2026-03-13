package com.noxcrew.noxesium.core.feature;

/**
 * Contains all server-relevant settings of a client that Noxesium communicates
 * with the server so it can be used to tailor visuals to the client.
 *
 * @param configuredGuiScale      The GUI scale set in the settings menu.
 * @param trueGuiScale            The true GUI scale calculated by the game.
 * @param width                   The width of the game window.
 * @param height                  The height of the game window.
 * @param enforceUnicode          Whether unicode fonts are being enforced.
 * @param touchScreenMode         Whether touch screen mode is enabled.
 * @param notificationDisplayTime What the notification display time is set to.
 * @param chatVisibility          The chat visibility setting.
 * @param chatWidth               The chat width (this is given on a scale 0-1 from 40px to 320px, default is 320px or 1.0).
 * @param chatHeight              The unfocused chat height (this is given on a scale 0-1 from 20px to 180px, default is 90px or 0.4375).
 * @param fov                     The current FOV value.
 * @param fovEffects              The impact FOV effects have on the FOV.
 */
public record ClientSettings(
        int configuredGuiScale,
        double trueGuiScale,
        int width,
        int height,
        boolean enforceUnicode,
        boolean touchScreenMode,
        double notificationDisplayTime,
        ChatVisibility chatVisibility,
        double chatWidth,
        double chatHeight,
        int fov,
        double fovEffects) {

    public ClientSettings(
            int configuredGuiScale,
            double trueGuiScale,
            int width,
            int height,
            boolean enforceUnicode,
            boolean touchScreenMode,
            double notificationDisplayTime) {
        this(
                configuredGuiScale,
                trueGuiScale,
                width,
                height,
                enforceUnicode,
                touchScreenMode,
                notificationDisplayTime,
                ChatVisibility.FULL,
                1f,
                defaultUnfocusedHeight(),
                70,
                1f);
    }

    /**
     * Returns the default height of the unfocused chat (90px).
     */
    public static double defaultUnfocusedHeight() {
        return 70.0 / 160.0;
    }
}
