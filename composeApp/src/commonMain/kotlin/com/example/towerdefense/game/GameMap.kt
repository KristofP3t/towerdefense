package com.example.towerdefense.game

object GameMap {
    const val COLS = 20
    const val ROWS = 12
    const val CELL_SIZE = 48f
    const val GAME_WIDTH = COLS * CELL_SIZE   // 960
    const val GAME_HEIGHT = ROWS * CELL_SIZE  // 576
    const val TOTAL_WAVES = 15

    /**
     * Path waypoints (col, row). The enemy follows straight horizontal/vertical
     * segments between consecutive waypoints.
     *
     * Visual layout (. = path, # = buildable):
     *   cols: 0123456789...
     *   row 2: ....→....↑.....
     *   row 6:     ↓....↑
     *                   └→ continues right
     */
    val waypoints: List<GridPos> = listOf(
        GridPos(0, 2),
        GridPos(4, 2),
        GridPos(4, 6),
        GridPos(9, 6),
        GridPos(9, 2),
        GridPos(14, 2),
        GridPos(14, 9),
        GridPos(19, 9),
    )

    /** All grid cells that belong to the path. Towers cannot be placed here. */
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
        pos.col * CELL_SIZE + CELL_SIZE / 2f,
        pos.row * CELL_SIZE + CELL_SIZE / 2f,
    )

    fun pixelToGrid(x: Float, y: Float): GridPos = GridPos(
        (x / CELL_SIZE).toInt().coerceIn(0, COLS - 1),
        (y / CELL_SIZE).toInt().coerceIn(0, ROWS - 1),
    )
}
