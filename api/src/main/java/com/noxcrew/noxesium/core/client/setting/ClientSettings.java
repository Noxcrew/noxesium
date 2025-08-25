package com.noxcrew.noxesium.core.client.setting;

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
 */
public record ClientSettings(
        int configuredGuiScale,
        double trueGuiScale,
        int width,
        int height,
        boolean enforceUnicode,
        boolean touchScreenMode,
        double notificationDisplayTime) {}
