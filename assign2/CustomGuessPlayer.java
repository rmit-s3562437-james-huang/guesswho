import java.io.*;
import java.util.*;

/** s3426571: Thoung Nguyen **/
/** s3562437: James Huang   **/

/**
 * Your customised guessing player.
 * This player is for bonus task.
 *
 * You may implement/extend other interfaces or classes, but ensure ultimately
 * that this class implements the Player interface (directly or indirectly).
 */
public class CustomGuessPlayer implements Player
{
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
     * @param chosenName Name of the chosen person for this player.
     * @throws IOException If there are IO issues with loading of gameFilename.
     *    Note you can handle IOException within the constructor and remove
     *    the "throws IOException" method specification, but make sure your
     *    implementation exits gracefully if an IOException is thrown.
     */
    public CustomGuessPlayer(String gameFilename, String chosenName)
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
    } // end of CustomGuessPlayer()
	
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
    } //end of readPlayerFromFile()

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

    public Guess guess() {
		String key = "";
		String value = "";
		if (candidates.size() == 1) {
			return new Guess(Guess.GuessType.Person, "", candidates.get(0).getName());
		}

		// Loop through candidates
		for (PlayerFromFile player : candidates){
			// Get players attributes
			for (Map.Entry<String, String> entry : player.getAttributes().entrySet()) {
				// If that attribute exists in the guessesList
				// Then ask next attribute until all the attribute you 
				if (guesses.contains(entry)){
					continue;
				}
					key = entry.getKey();
					value = entry.getValue();
					guesses.add(entry);
					break;
			}
			break;
		}
		return new Guess(Guess.GuessType.Attribute, key, value);
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
			//get player
			PlayerFromFile player = iter.next();
			//get player's attributes
			Map<String, String> attributes = player.getAttributes();
			//testing value
			boolean hasValue = attributes.get(attribute).equals(value);

			// Eliminate all candidates who dont have value V for attribute A
			// OR eliminate all candidates that have the value V for attribute A
			if ((answer && !hasValue) || (!answer && hasValue)) {
				iter.remove();
			}
	    }
	}
	return false;
    } // end of receiveAnswer()

} // end of class CustomGuessPlayer
