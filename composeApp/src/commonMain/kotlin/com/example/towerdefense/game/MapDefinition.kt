package com.example.towerdefense.game

data class MapDefinition(
    val id: String,
    val name: String,
    val cols: Int,
    val rows: Int,
    val waypoints: List<GridPos>,
) {
    val cellSize: Float = 48f
    val gameWidth: Float  get() = cols * cellSize
    val gameHeight: Float get() = rows * cellSize

    val pathCells: Set<GridPos> = buildSet {
        for (i in 0 until waypoints.size - 1) {
            val a = waypoints[i]
            val b = waypoints[i + 1]
            if (a.col == b.col) {
                for (r in minOf(a.row, b.row)..maxOf(a.row, b.row)) add(GridPos(a.col, r))
            } else {
                for (c in minOf(a.col, b.col)..maxOf(a.col, b.col)) add(GridPos(c, a.row))
            }
        }
    }

    fun cellCenter(pos: GridPos): Vec2 = Vec2(
        pos.col * cellSize + cellSize / 2f,
        pos.row * cellSize + cellSize / 2f,
    )

    fun pixelToGrid(x: Float, y: Float): GridPos = GridPos(
        (x / cellSize).toInt().coerceIn(0, cols - 1),
        (y / cellSize).toInt().coerceIn(0, rows - 1),
    )

    companion object {
        /**
         * Karte 1 – Wald: klassischer Zick-Zack-Pfad.
         *
         *   row 2:  ════════╗         ╔════════╗
         *   row 6:          ╚════════╝         ║
         *   row 9:                              ╚════════▶
         */
        val FOREST = MapDefinition(
            id        = "forest",
            name      = "Wald",
            cols      = 20,
            rows      = 12,
            waypoints = listOf(
                GridPos(0,  2),
                GridPos(4,  2),
                GridPos(4,  6),
                GridPos(9,  6),
                GridPos(9,  2),
                GridPos(14, 2),
                GridPos(14, 9),
                GridPos(19, 9),
            ),
        )

        /**
         * Karte 2 – Wüste: langer S-förmiger Pfad über die gesamte Breite.
         *
         *   row 1:  ══════════════════╗
         *   row 5:  ╔════════════════╝
         *   row 5:  ║
         *   row 10: ╚══════════════════▶
         */
        val DESERT = MapDefinition(
            id        = "desert",
            name      = "Wüste",
            cols      = 20,
            rows      = 12,
            waypoints = listOf(
                GridPos(0,  1),
                GridPos(18, 1),
                GridPos(18, 5),
                GridPos(2,  5),
                GridPos(2,  10),
                GridPos(19, 10),
            ),
        )

        val ALL: List<MapDefinition> = listOf(FOREST, DESERT)
    }
}
