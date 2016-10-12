import java.io.*;
import java.util.*;

/** s3426571: Thoung Nguyen **/
/** s3562437: James Huang   **/

/**
 * Random guessing player.
 * This player is for task B.
 * <p>
 * You may implement/extend other interfaces or classes, but ensure ultimately
 * that this class implements the Player interface (directly or indirectly).
 */
public class RandomGuessPlayer implements Player {
    // Initializing Database structures
    private Random rand = new Random();
    private PlayerFromFile chosenPlayer = null;
    private List<PlayerFromFile> candidates = new ArrayList<PlayerFromFile>();
    private List<Map.Entry<String, String>> guesses = new ArrayList<Map.Entry<String, String>>();
    private Map<String, List<String>> allAttributes = new HashMap<>();
    private Map<String, String> chosenPlayerAttributes = new HashMap<String, String>();
    /**
     * Loads the game configuration from gameFilename, and also store the chosen
     * person.
     *
     * @param gameFilename Filename of game configuration.
     * @param chosenName   Name of the chosen person for this player.
     * @throws IOException If there are IO issues with loading of gameFilename.
     *                     Note you can handle IOException within the constructor and remove
     *                     the "throws IOException" method specification, but make sure your
     *                     implementation exits gracefully if an IOException is thrown.
     */
    public RandomGuessPlayer(String gameFilename, String chosenName)
            throws IOException {
        List<PlayerFromFile> players = new ArrayList<PlayerFromFile>();
        // Scanning for gameFilename
        Scanner gameFileScan = new Scanner(new File(gameFilename));
        allAttributes = readAttributes(gameFileScan);

        while (gameFileScan.hasNextLine()) {
            PlayerFromFile player = readPlayerFromFile(gameFileScan);
            players.add(player);
        }

        for (PlayerFromFile player : players) {
            if (player.getName().equals(chosenName)) {
                chosenPlayer = player;
                chosenPlayerAttributes = player.getAttributes();
            }
        }
        candidates.addAll(players);
    } // end of RandomGuessPlayer()

    private Map<String, List<String>> readAttributes(Scanner gameFileScan) {
        Map<String, List<String>> attributes = new HashMap<String, List<String>>();

        while (gameFileScan.hasNextLine()) {
            // Read line-by-line
            String line = gameFileScan.nextLine();
            // Breaks, after all attributes have been scanned
            if (line.equals("")) {
                break;
            }
            Scanner lineScan = new Scanner(line);
            lineScan.useDelimiter(" ");
            
            // 4.1 Details of Files
            // Following the format of config file [attribute] [LIST of values it can take]
            // Scans key as the attribute
            
            String key = lineScan.next();
            List<String> values = new ArrayList<String>();

            // Scans values as the list of attribute values
            while (lineScan.hasNext()) {
                String value = lineScan.next();
                values.add(value);
            }
            attributes.put(key, values);
        }
        return attributes;
    } // end of readAttributes()

    private PlayerFromFile readPlayerFromFile(Scanner gameFileScan) {
        String name = "";
        Map<String, String> attributes = new HashMap<String, String>();
        // Reads attributes and values of the P player
        while (gameFileScan.hasNextLine()) {
            String line = gameFileScan.nextLine();
            if (line.equals("")) {
                break;
            }
            
            // 4.1 Details of Files
            // Following the format of config file [person name]
            // Following the format of config file [attribute n] [value of attribute n]

            String[] keyValue = line.split(" ");
            // Checking for players name based of array length, since a array length of the name is only 1
            // Else the following will be attibutes alongside the list of attributes
            if (keyValue.length > 1) {
                attributes.put(keyValue[0], keyValue[1]);
            } else {
                name = line;
            }
        }
        return new PlayerFromFile(name, attributes);
    } // end of readPlayerFromFile()

    public Guess guess() {
        // Returns a candidate if there is only 1 person
        if (candidates.size() == 1) {
            return new Guess(Guess.GuessType.Person, "", candidates.get(0).getName());
        }
        // Attributes data set
        Set<Map.Entry<String, String>> setAttributes = new HashSet<Map.Entry<String, String>>();
        
        // Loop through candidates and add thier attributes to setAttributes
        for (Iterator<PlayerFromFile> iter = candidates.iterator(); iter.hasNext(); ) {
            PlayerFromFile player = iter.next();
            setAttributes.addAll(player.getAttributes().entrySet());
        }

        // Remove the past guesses from setAttributes
        setAttributes.removeAll(guesses);

        // Picking a random number for setAttributes
        int r = rand.nextInt(setAttributes.size());
        // Store the random attribute with an iterator to mark the current position 
        Iterator<Map.Entry<String, String>> iterator = setAttributes.iterator();
        // Move from item to item within the collection
        for (int i = 0; i < r ; i++) {
            iterator.next();
        }
        
        // Final guess is made from iterator.next() and stored in guessAttribute map
        Map.Entry<String, String> guessAttribute = iterator.next();
        guesses.add(guessAttribute);
        
        // Returning the new Guess
        return new Guess(Guess.GuessType.Attribute, guessAttribute.getKey(), guessAttribute.getValue());
        
    } // end of guess()

    public boolean answer(Guess currGuess) {
        switch (currGuess.getType()) {
            case Person:
                // True if currGuess name is equal to the chosen players name
                return currGuess.getValue().equals(chosenPlayer.getName());
            case Attribute:
                // Loop attributes of chosen player and 
                // see if currGuess [attribute n] [value of attribute n] matches up
                for (Map.Entry<String, String> entry : chosenPlayerAttributes.entrySet()) {
                    if (entry.getKey().equals(currGuess.getAttribute())) {
                        if (entry.getValue().equals(currGuess.getValue())) {
                            return true;
                        }
                    }
                }
        }
        return false;
    } // end of answer()

    public boolean receiveAnswer(Guess currGuess, boolean answer) {
        String attribute = currGuess.getAttribute();
        String value = currGuess.getValue();

        if (currGuess.getType() == Guess.GuessType.Person) {
            if (!answer) {
                // If the answer is incorrect, remove that player from candidates
                for (Iterator<PlayerFromFile> iter = candidates.iterator(); iter.hasNext(); ) {
                    PlayerFromFile player = iter.next();
                    if (player.getName().equals(value)) {
                        iter.remove();
                    }
                }
            } else {
                return true;
            }
        }
        if (currGuess.getType() == Guess.GuessType.Attribute) {
            for (Iterator<PlayerFromFile> iter = candidates.iterator(); iter.hasNext(); ) {
                // Get player
                PlayerFromFile player = iter.next();
                // Get player's attributes
                Map<String, String> attributes = player.getAttributes();
                // Testing value
                boolean hasValue = attributes.get(attribute).equals(value);

                // Eliminate all candidates who don't have value V for attribute A
                // OR eliminate all candidates that have the value V for attribute A
                if ((answer && !hasValue) || (!answer && hasValue)) {
                    iter.remove();
                }
            }
        }
        return false;
    } // end of receiveAnswer()
    
} // end of class RandomGuessPlayer

class PlayerFromFile {
    // Storing all data of players from the file
    private String name;
    private Map<String, String> attributes;
    
    PlayerFromFile(String name, Map<String, String> attributes) {
        this.name = name;
        this.attributes = attributes;
    }
    
    public String getName() {
        return name;
    }
    
    public Map<String, String> getAttributes() {
        return attributes;
    } // end of class PlayerFromFile 

}
