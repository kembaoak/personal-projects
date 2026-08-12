package com.threemusketeers.ai;

import com.threemusketeers.model.Position;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * How close a set of positions is to sharing a row or column, i.e. how close
 * Enemies are to winning by alignment.
 */
final class Alignment {

    private Alignment() {
    }

    static int progress(List<Position> positions) {
        return maxFrequency(positions, Position::row) + maxFrequency(positions, Position::col);
    }

    private static int maxFrequency(List<Position> positions, Function<Position, Integer> axis) {
        Map<Integer, Long> counts = positions.stream()
                .collect(Collectors.groupingBy(axis, Collectors.counting()));
        return counts.values().stream().mapToInt(Long::intValue).max().orElse(0);
    }
}
