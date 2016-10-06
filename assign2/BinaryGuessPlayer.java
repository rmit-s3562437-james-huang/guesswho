import java.io.*;
import java.util.*;

/**
 * Binary-search based guessing player.
 * This player is for task C.
 * <p>
 * You may implement/extend other interfaces or classes, but ensure ultimately
 * that this class implements the Player interface (directly or indirectly).
 */
public class BinaryGuessPlayer implements Player {
    private PlayerFromFile chosenPlayer = null;

    private List<PlayerFromFile> players = new ArrayList<PlayerFromFile>();

    private List<PlayerFromFile> candidates = new ArrayList<PlayerFromFile>();

    private List<Map.Entry<String, String>> guesses = new ArrayList<Map.Entry<String, String>>();

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
        // reads game file
        Scanner gameFileScan = new Scanner(new File(gameFilename));

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

        for (PlayerFromFile player : players) {
            candidates.add(player);
        }

    } // end of BinaryGuessPlayer()

    private Map<String, List<String>> readAttributes(Scanner gameFileScan) {
        Map<String, List<String>> attributes = new HashMap<String, List<String>>();

        while (gameFileScan.hasNextLine()) {
            //read line by line
            String line = gameFileScan.nextLine();

            //check newline
            if (line.equals("")) {
                break;
            }

            Scanner lineScan = new Scanner(line);
            lineScan.useDelimiter(" ");

            //this idea is from the 4.1 Details of Files
            //in the assignment specs

            //[attribute] [LIST of values it can take]

            //get the first item of the line
            String key = lineScan.next();
            List<String> values = new ArrayList<String>();

            //then put the rest in the arraylist as the values
            while (lineScan.hasNext()) {
                String value = lineScan.next();
                values.add(value);
            }
            attributes.put(key, values);
        }
        return attributes;
    }

    private PlayerFromFile readPlayerFromFile(Scanner gameFileScan) {
        String name = "";
        Map<String, String> attributes = new HashMap<String, String>();

        while (gameFileScan.hasNextLine()) {
            //read attribute and value of the P player
            String line = gameFileScan.nextLine();
            if (line.equals("")) {
                break;
            }

            //person format
            //[person name]
            //[attribute n] [value of attribute n]

            String[] keyValue = line.split(" ");
            //check whether it the players name or the attribute
            if (keyValue.length > 1) {
                attributes.put(keyValue[0], keyValue[1]);
            } else {
                //vaue < 1 then must be the name
                name = line;
            }
        }
        return new PlayerFromFile(name, attributes);
    }


    public Guess guess() {
        String key = "";
        String value = "";
        if (candidates.size() == 1) {
            return new Guess(Guess.GuessType.Person, "", candidates.get(0).getName());
        }

        Map<Map.Entry<String, String>, Integer> freq = new HashMap<Map.Entry<String, String>, Integer>();

        //find all the frequency of each attribute
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
        //remove the attribute already guessed.
        for (Map.Entry<String, String> guess : guesses) {
            freq.remove(guess);
        }

        //get the middle
        int middle = candidates.size() / 2;

        //get the key as close to half candidates as said IN THE ASSIGNENT SPEC
        for (Map.Entry<Map.Entry<String, String>, Integer> attribute : freq.entrySet()) {
            if (attribute.getValue() >= middle) {
                key = attribute.getKey().getKey();
                value = attribute.getKey().getValue();
                //add the chosenAttribute to guessesList so we can eliminate guesses
                guesses.add(attribute.getKey());
                break;
            }
        }
        return new Guess(Guess.GuessType.Attribute, key, value);
    } // end of guess()


    public boolean answer(Guess currGuess) {
        switch (currGuess.getType()) {
            case Person:
                // true if currGuess name == chosen players name
                return currGuess.getValue().equals(chosenPlayer.getName());
            case Attribute:
                //check every single attributes chosen player has and its value.
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

            //foreach loop won't work
            //it doesnt remove player properly
            for (Iterator<PlayerFromFile> iter = candidates.iterator(); iter.hasNext(); ) {
                //get player
                PlayerFromFile player = iter.next();
                //get player's attributes
                Map<String, String> attributes = player.getAttributes();
                //testing value
                boolean hasValue = attributes.get(attribute).equals(value);

                //eliminate all candidates who dont have value v for attribute a
                // OR eliminate all candidates that have the value v for attribute a.
                if ((answer && !hasValue) || (!answer && hasValue)) {
                    iter.remove();
                }
            }
        }
        return false;
    } // end of receiveAnswer()

} // end of class BinaryGuessPlayer
