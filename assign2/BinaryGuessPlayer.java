import java.io.*;
import java.util.*;

/** s3426571: Thoung Nguyen **/
/** s3562437: James Huang   **/

/**
 * Binary-search based guessing player.
 * This player is for task C.
 * <p>
 * You may implement/extend other interfaces or classes, but ensure ultimately
 * that this class implements the Player interface (directly or indirectly).
 */
public class BinaryGuessPlayer implements Player {
    // Initializing Database structures
    private PlayerFromFile chosenPlayer = null;
    private List<PlayerFromFile> players = new ArrayList<PlayerFromFile>();
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
    public BinaryGuessPlayer(String gameFilename, String chosenName)
            throws IOException {
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
    } // end of BinaryGuessPlayer()

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
        String key = "";
        String value = "";
        
        // Returns a candidate if there is only 1 person
        if (candidates.size() == 1) {
            return new Guess(Guess.GuessType.Person, "", candidates.get(0).getName());
        }
        // Attributes data set
        Map<Map.Entry<String, String>, Integer> freq = new HashMap<Map.Entry<String, String>, Integer>();

        // Find all the frequencies of each attribute
        for (PlayerFromFile candidate : candidates) {
            Map<String, String> candidateAttributes = candidate.getAttributes();
            for (Map.Entry<String, String> entry : candidateAttributes.entrySet()) {
                Integer f = freq.get(entry);
                if (f != null) {
                    freq.put(entry, f + 1);
                } else {
                    freq.put(entry, 1);
                }
            }
        }
        
        // Remove the attribute already guessed
        for (Map.Entry<String, String> guess : guesses) {
            freq.remove(guess);
        }

        // Get the middle
        int middle = candidates.size() / 2;

        // Get the key "eliminates as close to half the candidates"
        for (Map.Entry<Map.Entry<String, String>, Integer> attribute : freq.entrySet()) {
            if (attribute.getValue() >= middle) {
                key = attribute.getKey().getKey();
                value = attribute.getKey().getValue();
                // Add the chosenAttribute to guessesList so we can eliminate guesses
                guesses.add(attribute.getKey());
                break;
            }
        }
        return new Guess(Guess.GuessType.Attribute, key, value);
    } // end of guess()


    public boolean answer(Guess currGuess) {
        switch (currGuess.getType()) {
            case Person:
                // True if currGuess name == chosen players name
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
        if (currGuess.getType() == Guess.GuessType.Person) {
            if (!answer) {
                // If the answer is incorrect, remove that player from candidates
                for (Iterator<PlayerFromFile> iter = candidates.iterator(); iter.hasNext(); ) {
                    PlayerFromFile player = iter.next();
                    if (player.getName().equals(currGuess.getValue())) {
                        players.remove(player);
                    }
                }
            } else {
                return true;
            }
        }
        if (currGuess.getType() == Guess.GuessType.Attribute) {
            String attribute = currGuess.getAttribute();
            String value = currGuess.getValue();

            // Foreach loop won't work
            // It doesnt remove player properly
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

} // end of class BinaryGuessPlayer
