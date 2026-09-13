package com.vincenthuto.putapinthat.client;

import java.util.ArrayList;
import java.util.List;

public final class HudStackLayout {
    public static final double MAX_SCALE = 0.75;
    public static final double WIDTH_FRACTION = 0.40;
    public static final int MARGIN = 8;
    public static final int GAP = 4;

    private HudStackLayout() {
    }

    public static Result calculate(int screenWidth, int screenHeight, List<Size> cards) {
        if (cards.isEmpty()) {
            return Result.EMPTY;
        }

        int maxWidth = cards.stream().mapToInt(Size::width).max().orElse(0);
        int naturalHeight = cards.stream().mapToInt(Size::height).sum() + GAP * (cards.size() - 1);
        double availableWidth = Math.max(1, screenWidth * WIDTH_FRACTION - MARGIN);
        double availableHeight = Math.max(1, screenHeight - MARGIN * 2.0);
        double scale = Math.min(MAX_SCALE,
                Math.min(availableWidth / maxWidth, availableHeight / naturalHeight));

        double scaledHeight = naturalHeight * scale;
        double y = Math.max(MARGIN, (screenHeight - scaledHeight) / 2.0);
        List<Double> offsets = new ArrayList<>(cards.size());
        double offset = 0;
        for (Size card : cards) {
            offsets.add(offset * scale);
            offset += card.height() + GAP;
        }
        return new Result(scale, MARGIN, y, List.copyOf(offsets));
    }

    public record Size(int width, int height) {
        public Size {
            if (width <= 0 || height <= 0) {
                throw new IllegalArgumentException("card dimensions must be positive");
            }
        }
    }

    public record Result(double scale, double x, double y, List<Double> cardOffsetsY) {
        public static final Result EMPTY = new Result(0, 0, 0, List.of());
    }
}
