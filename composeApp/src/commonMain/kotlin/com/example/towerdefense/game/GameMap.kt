package com.example.towerdefense.game

/**
 * Globales Singleton, das die aktuell ausgewählte Kartendefinition hält.
 * Vor jedem Spielstart wird [current] auf die gewünschte [MapDefinition] gesetzt.
 */
object GameMap {
    var current: MapDefinition = MapDefinition.FOREST

    const val TOTAL_WAVES = 15

    val COLS:        Int   get() = current.cols
    val ROWS:        Int   get() = current.rows
    val CELL_SIZE:   Float get() = current.cellSize
    val GAME_WIDTH:  Float get() = current.gameWidth
    val GAME_HEIGHT: Float get() = current.gameHeight
    val waypoints:   List<GridPos>  get() = current.waypoints
    val pathCells:   Set<GridPos>   get() = current.pathCells

    fun cellCenter(pos: GridPos): Vec2            = current.cellCenter(pos)
    fun pixelToGrid(x: Float, y: Float): GridPos  = current.pixelToGrid(x, y)
}
