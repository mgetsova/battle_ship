import java.util.*;
class Coordinate
{
    int row;
    int col;
    public Coordinate(int row, int col) {
        this.row = row;
        this.col = col;
    }
    public boolean isEqual(Coordinate coord){
        if (this.getRow() == coord.getRow() && this.getCol() == coord.getCol()){
            return true;
        }
        return false;
    }
    public int getRow(){
        return row;
    }
    public int getCol(){
        return col;
    }
    public void printCoord(){
        System.out.printf("(%d, %d)", this.row, this.col);
    }
}
class Ship
{
    /* A ship is defined as a HashSet of Coordinate tuples
    which allows for O(1) removal of a coordinate from the
    set. Unlike in the python implementation, we must still
    iterate over all coordinates in the set to check if a
    given coordinate is contained in the set. I am not sure
    if there is a way to therefore take advantage of the O(1)
    lookup time of the HashSet when it contains standard data
    types. There is a library for java tuples that may be helpful
    here.
     */
    Set<Coordinate> positions = new HashSet<Coordinate>();
    public Set<Coordinate> getPositions() {
        return positions;
    }
    public void addCoord(Coordinate newCoord) {
        this.positions.add(newCoord);
    }
    public boolean hitShip(int x, int y){
        Coordinate coords = new Coordinate(x, y);
        for (Coordinate coord : this.positions){
            if (coord.isEqual(coords)){
                this.positions.remove(coord);
            }
            return true;
        }
        return false;
    }
    public boolean isSunk(){
        if(this.positions.isEmpty()){
            System.out.println("SHIP SUNK");
            return true;
        }
        return false;
    }
    public boolean isOnBoard(int board_size) {
        for (Coordinate position : positions) {
            int row = position.getRow();
            int col = position.getCol();
            if (row < 0 || row >= board_size || col < 0 || col >= board_size) {
                return false;
            }
        }
        return true;
    }
    public void rotateShip(Coordinate origin, int length, String direction){
        Set pos = new HashSet<Coordinate>();
        pos.add(origin);
        int row = origin.getRow();
        int col = origin.getCol();
        for (int i = 1; i < length; i++){
            int new_row = row;
            int new_col = col;
            if (direction.equals("N")){
                new_row = row - i;
            }
            if (direction.equals("S")){
                new_row = row + i;
            }
            if (direction.equals("E")){
                new_col = col + i;
            }
            if (direction.equals("W")){
                new_col = col - i;
            }
            pos.add(new Coordinate(new_row, new_col));
        }
        this.positions = pos;
    }
    public void printShip(){
        System.out.printf("[");
        for (Coordinate coord : this.positions){
            coord.printCoord();
        }
        System.out.printf("]%n");
    }
}


class Ocean{
    /*
    Stores the set of ships on the board as well as a dictionary
    (HashMap) with row keys pointing to sets of column coordinates
    in which there is a ship to allow for O(1) determination of a hit
    or miss guess. Thus, we are still using (worst case) 2*N space where N is the
    number of occupied cells in the board to store the data for all
    ship locations. In fact, it is less than 2N since we only
    store row integers once in the coordDict.
     */
    Set<Ship> ships = new HashSet<Ship>();
    HashMap<Integer, Set<Integer>> coordDict = new HashMap<>();
    private void updateCoordDict(Ship ship){
        for(Coordinate coord : ship.getPositions()){
            int row = coord.getRow();
            int col = coord.getCol();
            if(coordDict.containsKey(row)){
                coordDict.get(row).add(col);
            }
            else {
                Set<Integer> new_set = new HashSet<Integer>();
                new_set.add(col);
                coordDict.put(row, new_set);
            }
        }
    }
    private Coordinate getOpenPosition(int board_size){
        /* Chooses random open position for origin of new ship
        to be placed on board.
         */
        Random random = new Random();
        int row = random.nextInt(board_size);
        int col = random.nextInt(board_size);
        if (coordDict.containsKey(row)){
            // If the row is not in the dict yet, the column is guaranteed
            // not to be in the dict.
            if(!coordDict.get(row).contains(col)){
                // If randomly chosen coordinates are not occupied,
                // we return this as the position.
                return new Coordinate(row, col);
            }
            else{
                List<Integer> freeCols = new ArrayList<Integer>();
                // Get unoccupied columns at chosen row:
                for(int i = 0; i < board_size; i++){
                    if (!coordDict.get(row).contains(i)){
                        freeCols.add(i);
                    }
                }
                // Pick one of them at random:
                col = freeCols.get(random.nextInt(freeCols.size()));
                return new Coordinate(row, col);
            }
        }
        return new Coordinate(row, col);
    }
    public boolean isHit(int row, int col){
        // Returns true if there is a ship at a given position
        if (this.coordDict.containsKey(row)){
            if (this.coordDict.get(row).contains(col)){
                return true;
            }
        }
        return false;
    }

    private boolean noOverlaps(Ship newShip){
        // Returns true if ship we want to add to the ocean
        // does not intersect any ships that are already placed
        // on the board/ocean.
        for (Coordinate coord : newShip.getPositions()) {
            if (this.isHit(coord.getRow(), coord.getCol())) {
                return false;
            }
        }
        return true;
    }
    private Ship positionNewShip(int length, int board_size){
        /* Randomly positions a new ship of given length on the
        board st. it does not intersect with any ships already on
        the board or hang off the board. Does so by picking a random
        open cell and testing if each of the 4 directions in which the
        ship with endpoint at that cell yield a valid position. Picks
        new cell and repeats if not.
         */
        Coordinate origin = this.getOpenPosition(board_size);
        Ship temp_ship = new Ship();
        temp_ship.addCoord(origin);
        List<String> directions = new ArrayList<>(
                Arrays.asList("N", "S", "W", "E"));
        Collections.shuffle(directions);
        for(String direction : directions){
            temp_ship.rotateShip(origin, length, direction);
            if (temp_ship.isOnBoard(board_size) && this.noOverlaps(temp_ship)){
                return temp_ship;
            }
        }
        return new Ship();
    }

    public void addShip(int length, int board_size){
        /* Public method for placing new ships on the board /
        in the ocean at the start of the game.
         */
        Ship new_ship = this.positionNewShip(length, board_size);
        while(new_ship.getPositions().isEmpty()){
            new_ship = this.positionNewShip(length, board_size);
        }
        this.ships.add(new_ship);
        this.updateCoordDict(new_ship);
    }

    public void recordHit(int row, int col){
        /* Records that a ship has been hit by removing the coordinates
        of the hit from the coordDict of occupied cells and also by removing
        that coordinate pair from the set of coordinate pairs of the hit
        ship (after identifying the hit ship).
         */
        assert this.isHit(row, col) : "Error: this is not a valid hit.";
        System.out.println("HIT");
        this.coordDict.get(row).remove(col);
        for (Ship ship : this.ships){
            if(ship.hitShip(row, col)){
                if(ship.isSunk()){
                    this.ships.remove(ship);
                    break;
                }
            }
        }
    }
    public void printShips(){
        for (Ship ship : this.ships){
            ship.printShip();
        }
    }
    public boolean isEmpty(){
        /* If there are no occupied cells left, all ships
        have been sunk and the corodDict is empty. Will use this to
        check if game is won after a ship is sunk.
         */
        if (this.ships.isEmpty()){
            return true;
        }
        return false;
    }
}

class GuessTracker{
    /* Keeps track of what positions the CPU has already guessed at and
    allows for new random guesses to be generated via a row -> columns dict.
     */
    int board_size;
    HashMap<Integer, Set<Integer>> coordDict = new HashMap<>();
    public GuessTracker(int board_size){
        this.board_size = board_size;
        for (int i = 0; i < board_size; i ++){
            Set<Integer> newSet = new HashSet<>();
            this.coordDict.put(i, newSet);
        }
    }
    private boolean isValid(int row, int col){
        if(this.coordDict.containsKey(row)){
            if(!this.coordDict.get(row).contains(col)){
                return true;
            }
        }
        return false;
    }
    private void addGuess(Coordinate coord){
        int row = coord.getRow();
        int col = coord.getCol();
        assert this.isValid(row, col) : "CPU already guessed this";
        this.coordDict.get(row).add(col);
        /* If the CPU has guessed all teh columns for a row,
        then it can no longer guess in that row so we can delete
        that entire key from the dict.
         */
        if(this.coordDict.get(row).size() == this.board_size){
            this.coordDict.remove(row);
        }
    }
    public Coordinate randomGuess(){
        Random random = new Random();
        int row = random.nextInt(this.board_size);
        while(!this.coordDict.containsKey(row)){
            // If the row is not in the dictionary,
            // it has been removed because all columns have been
            // guessed, so we must choose another random row int.
            row = random.nextInt(this.board_size);
        }
        int col = random.nextInt(this.board_size);
        while(this.coordDict.get(row).contains(col)){
            // If a column is in the set for a row key, then it
            // has already been guessed so we must choose another
            // column int.
            col = random.nextInt(this.board_size);
        }
        Coordinate guess = new Coordinate(row, col);
        this.addGuess(guess);
        return guess;
    }
}
class Board {
    List<String> rows = new ArrayList<>();
    Ocean ocean;
    public Board(int board_size, Ocean ocean){
        this.ocean = ocean;
        this.rows.add("    1  2  3  4  5  6  7  8  9  10");
        List<String> chars = Arrays.asList("A", "B", "C", "D", "E", "F", "G", "H", "I", "J");
        for (int i = 0; i < board_size; i ++){
            String row_str = chars.get(i) + "   ";
            for (int j = 0; j < board_size; j++){
                if (this.ocean != null && this.ocean.isHit(i, j)){
                    row_str += " 1 ";
                }
                else {
                    row_str += "---";
                }
            }
            this.rows.add(row_str);
        }
    }
    public void updateHit(int row, int col){
        String[] str_lst = this.rows.get(row + 1).split("");
        str_lst[4 + 3*col] = "X";
        this.rows.set(row + 1, String.join("", str_lst));
    }
    public void updateMiss(int row, int col){
        String[] str_lst = this.rows.get(row + 1).split("");
        str_lst[4 + 3*col] = "*";
        this.rows.set(row + 1, String.join("", str_lst));
    }
    public void printBoard(){
        for (String row : this.rows){
            System.out.println(row);
        }
    }
}
class Game{
    int BOARD_SIZE;
    int max_ship_len;
    boolean print_boards;
    Ocean CPU_ocean = new Ocean();
    Ocean Player_ocean = new Ocean();
    GuessTracker CPU_guesses;
    Board shipBoard;
    Board targetBoard;
    public Game(int board, int max_len, boolean print_boards){
        this.BOARD_SIZE = board;
        this.max_ship_len = max_len;
        this.print_boards = print_boards;
        CPU_guesses = new GuessTracker(this.BOARD_SIZE);
    }
    public void play(){
        for(int i = 2; i <= this.max_ship_len; i++){
            this.CPU_ocean.addShip(i, this.BOARD_SIZE);
            this.Player_ocean.addShip(i, this.BOARD_SIZE);
        }
        System.out.println("Your ships are: ------------------");
        this.Player_ocean.printShips();
        System.out.println("CPU ships are : ------------------");
        this.CPU_ocean.printShips();
        if (this.print_boards){
            this.shipBoard = new Board(this.BOARD_SIZE, this.Player_ocean);
            this.targetBoard = new Board(this.BOARD_SIZE, null);
        }
        this.playerTurn();
    }
    private void CPU_turn(){
        System.out.println("CPU turn --------");
        Coordinate guess = this.CPU_guesses.randomGuess();
        System.out.printf("CPU guess is %d %d%n", guess.getRow(), guess.getCol());
        if (this.Player_ocean.isHit(guess.getRow(), guess.getCol())){
            this.Player_ocean.recordHit(guess.getRow(), guess.getCol());
            if (this.print_boards){
                this.shipBoard.updateHit(guess.getRow(), guess.getCol());
            }
            if (this.Player_ocean.isEmpty()){
                System.out.println("---CPU WIN!---");
                return;
            }
        }
        else {
            System.out.println("MISS");
            if (this.print_boards){
                this.shipBoard.updateMiss(guess.getRow(), guess.getCol());
            }
        }
        if (this.print_boards){
            System.out.println("SHIP BOARD");
            this.shipBoard.printBoard();
        }
        this.playerTurn();
    }

    private void playerTurn(){
        System.out.println("Make a row, column guess (w/ 0-indexing): ");
        Scanner scan = new Scanner(System.in);
        int row = scan.nextInt();
        int col = scan.nextInt();
        if(this.CPU_ocean.isHit(row, col)){
            this.CPU_ocean.recordHit(row, col);
            if (this.print_boards){
                this.targetBoard.updateHit(row, col);
            }
            if(this.CPU_ocean.isEmpty()){
                System.out.println("---YOU WON!---");
                return;
            }
        }
        else {
            System.out.println("MISS");
            if (this.print_boards){
                this.targetBoard.updateMiss(row, col);
            }
        }
        if (this.print_boards){
            System.out.println("TARGET BOARD");
            this.targetBoard.printBoard();
        }
        this.CPU_turn();
    }
}



public class Main {
    public static void main(String[] args) {
        // Change 3rd argument to 'false' to not print out the game boards.
        Game battle_ship = new Game(10, 5, true);
        battle_ship.play();
    }
}