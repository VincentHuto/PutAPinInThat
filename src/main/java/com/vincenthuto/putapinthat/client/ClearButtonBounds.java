package com.vincenthuto.putapinthat.client;

public record ClearButtonBounds(int x, int y, int width, int height) {
    private static final int BUTTON_SIZE = 20;
    private static final int LEFT_MARGIN = 6;
    private static final int BOTTOM_MARGIN = 6;
    private static final int BUTTON_GAP = 2;

    public static ClearButtonBounds forScreenHeight(int screenHeight) {
        int x = LEFT_MARGIN + 2 * (BUTTON_SIZE + BUTTON_GAP);
        return new ClearButtonBounds(x, screenHeight - BOTTOM_MARGIN - BUTTON_SIZE, BUTTON_SIZE, BUTTON_SIZE);
    }

    public boolean contains(double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
}
