package com.example.towerdefense.game

data class MapDefinition(
    val id: String,
    val name: String,
    val cols: Int,
    val rows: Int,
    val waypoints: List<GridPos>,
    /** Abzweigende Pfade, die am letzten Hauptwaypoint beginnen. */
    val branchWaypoints: List<List<GridPos>> = emptyList(),
) {
    val cellSize: Float  = 48f
    val gameWidth: Float  get() = cols * cellSize
    val gameHeight: Float get() = rows * cellSize

    val hasBranch: Boolean get() = branchWaypoints.isNotEmpty()

    val pathCells: Set<GridPos> = buildSet {
        addSegments(waypoints)
        val fork = waypoints.lastOrNull() ?: return@buildSet
        for (branch in branchWaypoints) {
            addSegments(listOf(fork) + branch)
        }
    }

    private fun MutableSet<GridPos>.addSegments(wps: List<GridPos>) {
        for (i in 0 until wps.size - 1) {
            val a = wps[i]; val b = wps[i + 1]
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
         */
        val FOREST = MapDefinition(
            id        = "forest",
            name      = "Wald",
            cols      = 20,
            rows      = 12,
            waypoints = listOf(
                GridPos(0,  2), GridPos(4,  2), GridPos(4,  6),
                GridPos(9,  6), GridPos(9,  2), GridPos(14, 2),
                GridPos(14, 9), GridPos(19, 9),
            ),
        )

        /**
         * Karte 2 – Wüste: langer S-förmiger Pfad.
         */
        val DESERT = MapDefinition(
            id        = "desert",
            name      = "Wüste",
            cols      = 20,
            rows      = 12,
            waypoints = listOf(
                GridPos(0,  1), GridPos(18, 1), GridPos(18, 5),
                GridPos(2,  5), GridPos(2,  10), GridPos(19, 10),
            ),
        )

        /**
         * Karte 3 – Gebirge: dichter Zick-Zack-Pfad.
         */
        val MOUNTAIN = MapDefinition(
            id        = "mountain",
            name      = "Gebirge",
            cols      = 20,
            rows      = 12,
            waypoints = listOf(
                GridPos(0,  1), GridPos(3,  1), GridPos(3,  9),
                GridPos(7,  9), GridPos(7,  3), GridPos(11, 3),
                GridPos(11, 9), GridPos(15, 9), GridPos(15, 3),
                GridPos(19, 3),
            ),
        )

        /**
         * Karte 4 – Küste: langer U-förmiger Pfad.
         */
        val COAST = MapDefinition(
            id        = "coast",
            name      = "Küste",
            cols      = 20,
            rows      = 12,
            waypoints = listOf(
                GridPos(0,  0), GridPos(18, 0), GridPos(18, 6),
                GridPos(1,  6), GridPos(1,  11), GridPos(19, 11),
            ),
        )

        /**
         * Karte 5 – Vulkan: Pfad gabelt sich in der Mitte.
         * Gegner wählen am Gabelungspunkt zufällig einen der zwei Äste.
         *
         *   Hauptpfad: (0,5) ─────────→ (9,5)  [Gabel]
         *   Ast oben:              ↑ (9,5) → (9,1) → (19,1)
         *   Ast unten:             ↓ (9,5) → (9,10) → (19,10)
         */
        val VOLCANO = MapDefinition(
            id             = "volcano",
            name           = "Vulkan",
            cols           = 20,
            rows           = 12,
            waypoints      = listOf(GridPos(0, 5), GridPos(9, 5)),
            branchWaypoints = listOf(
                listOf(GridPos(9, 1),  GridPos(19, 1)),   // oberer Ast
                listOf(GridPos(9, 10), GridPos(19, 10)),  // unterer Ast
            ),
        )

        val ALL: List<MapDefinition> = listOf(FOREST, DESERT, MOUNTAIN, COAST, VOLCANO)
    }
}
