import random


class Ship:
    # a ship is defined as a set of coordinate tuples
    # python sets allow for avg O(1) lookup and deletion
    def __init__(self, positions):
        self.pos = positions
    
    def hitShip(self, x, y):
        if (x, y) in self.pos:
            self.pos.remove((x, y))
            if len(self.pos) == 0:
                print('SHIP SUNK')
                return 'SHIP SUNK'
            else:
                print('HIT')
                return 'HIT'

    # check if ship's position overlaps other ship
    def isIntersecting(self, ship):
        if len(self.pos.intersection(ship.pos)) == 0:
            return False
        return True
    
    # checks if ship is entirely within the game board
    def isOnBoard(self, board_size):
        for position in self.pos:
            row, col = position
            if row < 0 or row >= board_size or col < 0 or col >= board_size:
                return False
        return True
    
    # positions ship in one of the 4 directions from its
    # origin or endpoint
    def rotateShip(self, origin, length, direction):
        self.pos = set()
        row, col = origin
        for i in range(length):
            if direction == 'N':
                self.pos.add((row - i, col))
            if direction == 'S':
                self.pos.add((row + i, col))
            if direction == 'E':
                self.pos.add((row, col + i))
            if direction == 'W':
                self.pos.add((row, col - i))
               
                
        
        
class Ocean:
    # stores the set of ships on the board as well
    # a dictionary with row keys pointing to sets
    # of column coordinates in which there is a 
    # ship to allow for avg O(1) determination of a hit or miss
    # guess. So if we have N cells which ships occupy,
    # we use 2*N space to store the data of their location.
    def __init__(self, ships):
        self.ships = ships
        self.coords = dict()
        for ship in ships:
            self.updateCoordDict(ship)
    
    def printShips(self):
        print('---------------------')
        for ship in self.ships:
            print(ship.pos)
    
    def getOpenPosition(self, board_size):
        # chooses random open position for origin of new
        # ship to be placed
        row = random.randint(0, board_size - 1)
        if row in self.coords:
            col = random.choice(list(set(range(board_size)) - self.coords[row]))
        else:
            col = random.randint(0, board_size - 1)
        return (row, col)
    
    def noOverlaps(self, new_ship):
        for ship in self.ships:
            if new_ship.isIntersecting(ship):
                return False
        return True
    
    def positionNewShip(self, length, board_size):
        # randomly positions reaminder of new ship about the 
        # origin point until a suitable position is found
        # on the board and not overlapping other ships
        origin = self.getOpenPosition(board_size)
        temp_ship = Ship(set())
        directions = ['N', 'S', 'E', 'W']
        random.shuffle(directions)
        for direction in directions:
            temp_ship.rotateShip(origin, length, direction)
            if temp_ship.isOnBoard(board_size) and self.noOverlaps(temp_ship):
                return temp_ship
        return False
            
    def updateCoordDict(self, ship):
        for (row, col) in ship.pos:
            if row in self.coords:
                self.coords[row].add(col)
            else:
                self.coords[row] = {col}
    
    def addShip(self, length, board_size):
        new_ship = self.positionNewShip(length, board_size)
        while(new_ship == False):
            new_ship = self.positionNewShip(length, board_size)
        self.ships.add(new_ship)
        self.updateCoordDict(new_ship)
    
    def isHit(self, row, col):
        if row in self.coords:
            if col in self.coords[row]:
                return True
        return False
    
    def recordHit(self, row, col):
        # removes hit ship cell from the coordinate 
        # dict and ship set data structures
        assert(self.isHit(row, col) == True)
        self.coords[row].remove(col)
        for ship in self.ships:
            if ship.hitShip(row, col) == 'SHIP SUNK':
                self.ships.remove(ship)
                if len(self.ships) == 0:
                    print('game over')
                    return
                return
                
            
            
class Guesses:
    # keeps track of what positions the CPU has already
    # guessed at and allows for new random guesses to 
    # be generated via a row -> columns dict
    def __init__(self, board_size):
        self.board_size = board_size
        self.coords = dict()
        for i in range(board_size):
            self.coords[i] = set()
            
    def isValid(self, row, col):
        if row in self.coords and col not in self.coords[row]:
            return True
        return False
    
    def addGuess(self, row, col):
        assert(self.isValid(row, col) == True)
        self.coords[row].add(col)
        # if the CPU has guessed all columns for a 
        # row then it can no longer guess in that row
        # so we can delete that entire key from the dict
        if len(self.coords[row]) == self.board_size:
            del self.coords[row]
    
    def randomGuess(self):
        row = random.choice(list(self.coords))
        # randomly pick column from the set of all columns not already having been 
        # guessed at this row
        col = random.choice(list(set(range(self.board_size)) - self.coords[row]))
        self.addGuess(row, col)
        return (row, col)
        
    
    
class Board:
    # generates boards if one wants to see the ships and guesses on a grid
    def __init__(self, ocean = None):
        self.ocean = ocean
        self.rows = []
        
    def makeBoard(self, board_size):
        self.rows.append('    1  2  3  4  5  6  7  8  9  10')
        chars = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J']
        for i in range(board_size):
            row_str = chars[i] + '  '
            for j in range(board_size):
                if self.ocean != None and self.ocean.isHit(i, j):
                    row_str += ' 1 '
                else:
                    row_str += '---'
            self.rows.append(row_str)
    
    def updateHit(self, row, col):
        str_lst = list(self.rows[row + 1])
        str_lst[4 + 3*col] = 'X'
        self.rows[row + 1] = ''.join(str_lst)


    def updateMiss(self, row, col):
        str_lst = list(self.rows[row + 1])
        str_lst[4 + 3*col] = '*'
        self.rows[row + 1] = ''.join(str_lst)
                
    def printBoard(self):
        for row in self.rows:
            print(row)
      
            
class Game: 
    def __init__(self, BOARD_SIZE = 10, print_boards = False):
        self.ship_lens = [2, 3, 4, 5]
        self.CPU_ocean = Ocean(set())
        self.Player_ocean = Ocean(set())
        for length in self.ship_lens:
            self.CPU_ocean.addShip(length, BOARD_SIZE)
            self.Player_ocean.addShip(length, BOARD_SIZE)
        self.print_boards = print_boards
        self.shipBoard = Board(self.Player_ocean)
        self.targetBoard = Board()
        if self.print_boards:
            self.shipBoard.makeBoard(BOARD_SIZE)
            self.targetBoard.makeBoard(BOARD_SIZE)
        self.CPU_guesses = Guesses(BOARD_SIZE)
        

    def CPU_turn(self):
        row, col = self.CPU_guesses.randomGuess()
        print('CPU guess is row = ' + str(row) + ' col = '+ str(col))
        if self.Player_ocean.isHit(row, col):
            self.Player_ocean.recordHit(row, col)
            if len(self.Player_ocean.ships) == 0:
                print("---CPU WIN!---")
                return False
            if self.print_boards:
                self.shipBoard.updateHit(row, col)
        else:
            print('MISS')
            if self.print_boards:
                self.shipBoard.updateMiss(row, col)
        if self.print_boards:
            print('SHIP BOARD:')
            self.shipBoard.printBoard()
        self.Player_turn()

    
    def Player_turn(self):
        inpt = input('Make a row, column guess (w/ 0-indexing): ').split()
        row = int(inpt[0])
        col = int(inpt[1])
        if self.CPU_ocean.isHit(row, col):
            self.CPU_ocean.recordHit(row, col)
            if len(self.CPU_ocean.ships) == 0:
                print("---YOU WON!---")
                return False
            if self.print_boards:
                self.targetBoard.updateHit(row, col)
        else:
            print('MISS')
            if self.print_boards:
                self.targetBoard.updateMiss(row, col)
        if self.print_boards:
            print('TARGET BOARD:')
            self.targetBoard.printBoard()
        self.CPU_turn()

        
def main():
    # change flag to True to print out player's boards
    new_game = Game(print_boards = True)
    print('Here are your ships:')
    new_game.Player_ocean.printShips()
    print('And here are the CPU ships:')
    new_game.CPU_ocean.printShips()
    print('NOTE: Input your target guesses in the form \'row col\' with 0' + 
        ' indexed values from 0-9 rather than A-J for the row and 1-10 for' +
        ' the columns despite the table outputs')
    new_game.Player_turn()
    
main()
        
        
        
        
                