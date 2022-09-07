package minesweeper

import kotlin.random.Random



class GameBoard {
    private val mineCount: Int
    private val boardWidth = 9
    private val boardHeight = 9
    private val mineBoard: MutableList<MutableList<Char>> = MutableList(boardHeight) { MutableList(boardWidth) {'/'} }
    private val userBoard: MutableList<MutableList<Char>> = MutableList(boardHeight) { MutableList(boardWidth) {'.'} }

    init {
        print("How many mines do you want on the field? ")
        mineCount = readln().toIntOrNull()?: 10
        prettyPrint()
    }

    private fun Int.oob(): Boolean = this < 0 || this >= boardWidth
    private fun Pair<Int, Int>.neighborBelowLeft(): Pair<Int, Int> = Pair(this.first + 1, this.second - 1)
    private fun Pair<Int, Int>.neighborBelow(): Pair<Int, Int> = Pair(this.first + 1, this.second)
    private fun Pair<Int, Int>.neighborBelowRight(): Pair<Int, Int> = Pair(this.first + 1, this.second + 1)
    private fun Pair<Int, Int>.neighborLeft(): Pair<Int, Int> = Pair(this.first, this.second - 1)
    private fun Pair<Int, Int>.neighborRight(): Pair<Int, Int> = Pair(this.first, this.second + 1)
    private fun Pair<Int, Int>.neighborAboveLeft(): Pair<Int, Int> = Pair(this.first - 1, this.second - 1)
    private fun Pair<Int, Int>.neighborAbove(): Pair<Int, Int> = Pair(this.first - 1, this.second)
    private fun Pair<Int, Int>.neighborAboveRight(): Pair<Int, Int> = Pair(this.first - 1, this.second + 1)
    private fun Pair<Int, Int>.oob(): Boolean = this.first.oob() || this.second.oob()
    private fun Pair<Int, Int>.isAMine(): Boolean = mineBoard[this.first][this.second] == 'X'
    private fun Pair<Int, Int>.isUninitialized(): Boolean = mineBoard[this.first][this.second] == '/'
    private fun Pair<Int, Int>.getUserBoard(): Char = userBoard[this.first][this.second]
    private fun Pair<Int, Int>.setUserBoard(toSet :Char) {
        userBoard[this.first][this.second] = toSet
    }
    private fun Pair<Int, Int>.isLegal(): Boolean {
        return (!this.oob() &&
                !this.isAMine() &&
                (this.getUserBoard() == '.' || this.getUserBoard() == '*'))
    }
    private fun Pair<Int, Int>.hasAMine(): Boolean {
        return (!this.oob() && this.isAMine())
    }
    private fun Pair<Int, Int>.markCell() {
        if (this.getUserBoard() == '*') {
            this.setUserBoard('.')
        }
        else if (this.getUserBoard() == '.') {
            this.setUserBoard('*')
        }
        return
    }
    private fun Pair<Int, Int>.exploreCell(): Boolean {
        if (this.isUninitialized()) this.setupBoard()
        if (this.isAMine()) {
            gameLose()
            return true
        }
        else {
            this.balloon()
            prettyPrint()
        }
        return false
    }
    private fun Pair<Int, Int>.balloon() {
        when (val surroundingMines = this.checkSurroundings()) {
            in '1'..'8' -> {
                this.setUserBoard(surroundingMines)
            }
            '0' -> {
                this.setUserBoard('/')
                if (this.neighborAboveLeft().isLegal()) {this.neighborAboveLeft().balloon()}
                if (this.neighborAbove().isLegal()) {this.neighborAbove().balloon()}
                if (this.neighborAboveRight().isLegal()) {this.neighborAboveRight().balloon()}
                if (this.neighborLeft().isLegal()) {this.neighborLeft().balloon()}
                if (this.neighborRight().isLegal()) {this.neighborRight().balloon()}
                if (this.neighborBelowLeft().isLegal()) {this.neighborBelowLeft().balloon()}
                if (this.neighborBelow().isLegal()) {this.neighborBelow().balloon()}
                if (this.neighborBelowRight().isLegal()) {this.neighborBelowRight().balloon()}
            }
        }
    }
    private fun Pair<Int, Int>.checkSurroundings(): Char {
        var mineCounter = 0
        if (this.neighborAboveLeft().hasAMine()) {mineCounter++}
        if (this.neighborAbove().hasAMine()) {mineCounter++}
        if (this.neighborAboveRight().hasAMine()) {mineCounter++}
        if (this.neighborLeft().hasAMine()) {mineCounter++}
        if (this.neighborRight().hasAMine()) {mineCounter++}
        if (this.neighborBelowLeft().hasAMine()) {mineCounter++}
        if (this.neighborBelow().hasAMine()) {mineCounter++}
        if (this.neighborBelowRight().hasAMine()) {mineCounter++}
        return mineCounter.toString().first()
    }
    private fun Pair<Int, Int>.availCellsInRow(row: Int): Int {
        var toReturn = 0
        for (col in 0 until boardWidth) {
            if (mineBoard[row][col] == '/' && !(row == this.first && col == this.second)) toReturn++
        }
        return toReturn
    }
    private fun Pair<Int, Int>.findValidTarget(index: Int): Pair<Int, Int> {
        var tracker = index
        for (row in 0 until boardHeight) {
            val availableCells = this.availCellsInRow(row)
            if (availableCells <= tracker) {
                tracker -= availableCells
                continue
            }
            for (col in 0 until boardWidth) {
                if (mineBoard[row][col] == '/' && !(row == this.first && col == this.second)) {
                    if (tracker == 0) return Pair(row, col)
                    tracker--
                }
            }
        }
        return Pair(-1, -1)
    }
    private fun Pair<Int, Int>.setupBoard() {
        var placedMines = 0
        while (placedMines < mineCount) {
            val randInt = Random.nextInt(0, boardWidth * boardHeight - placedMines - 1)
//            val randCol = randInt % boardWidth
//            val randRow = randInt / boardHeight
            val target = this.findValidTarget(randInt)
//            if (randRow == this.first || randCol == this.second) continue
//            if (mineBoard[target.first][target.second] == '/') {
            mineBoard[target.first][target.second] = 'X'
            placedMines++
//            }
        }
        for (row in 0 until boardHeight) {
            for (col in 0 until boardWidth) {
                if (mineBoard[row][col] == '/') mineBoard[row][col] = '.'
            }
        }
    }
    private fun takeInput(): List<String> {
        print("Set/delete mine marks or claim a cell as free: ")
        val input = readln().split(" ")
        if (input.size < 3) {
            println("You need 3 arguments, an x, y, and either \"mine\" or \"free\"")
            return takeInput()
        }
        val command = input[2]
        if (!(command == "mine" || command == "free")) {
            println("Please enter either \"mine\" or \"free\"")
            return takeInput()
        }
        val col = (input[0].toIntOrNull()?: 0) - 1
        val row = (input[1].toIntOrNull()?: 0) - 1
        if ((col < 0 || col >= boardWidth) || row < 0 || row >= boardHeight) {
            println("That spot isn't within the game bounds!")
            return takeInput()
        }
        if (userBoard[row][col].isDigit() || userBoard[row][col] == '/') {
            println("This tile is already marked safe!")
            return takeInput()
        }
        return listOf(col.toString(), row.toString(), input[2])
    }
    private fun prettyPrint() {
        println()
        for (row in -2..boardHeight) {
            for (col in -2..boardWidth) {
                if (row in 0 until boardHeight && col in 0 until boardWidth) print(userBoard[row][col])
                else if (col == -2 && row == -2) print(" ")
                else if (col == -1 || col == boardWidth) print("│")
                else if (row == -1 || row == boardHeight) print("—")
                else if (row == -2) print(col+1)
                else print(row+1)
            }
            println()
        }
    }
    fun gameLoop() {
        if (gameOver(takeInput())) {
            return
        }
        else gameLoop()
    }
    private fun gameOver(myInput: List<String>): Boolean {
        val chosenTile = Pair(myInput[1].toInt(), myInput[0].toInt())
        when (myInput[2]) {
            "mine" -> {
                chosenTile.markCell()
                prettyPrint()
                return gameWin()
            }
            "free" -> {
                return chosenTile.exploreCell()
            }
        }
        return false
    }
    private fun gameWin(): Boolean {
        for (row in mineBoard.indices) {
            for (col in mineBoard.indices) {
                if (mineBoard[row][col] == 'X' && userBoard[row][col] != '*') return false
                if (userBoard[row][col] == '*' && mineBoard[row][col] != 'X') return false
            }
        }
        println("Congratulations! You found all the mines!")
        return true
    }
    private fun gameLose() {
        for (row in 0 until boardHeight) {
            for (col in 0 until boardWidth) {
                val current = Pair(row, col)
                if (current.isAMine()) current.setUserBoard('X')
            }
        }
        prettyPrint()
        println("You stepped on a mine and failed!")
    }

}

fun main() {
    val myBoard = GameBoard()
    myBoard.gameLoop()
}