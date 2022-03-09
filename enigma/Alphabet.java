package enigma;

import java.util.HashMap;
import java.util.Map;

/** An alphabet of encodable characters.  Provides a mapping from characters
 *  to and from indices into the alphabet.
 *  @author Zwea Htet
 */
class Alphabet {
    /**
     * Store alphabets in a string.
     */
    private String _chars;

    /** A new alphabet containing CHARS. The K-th character has index
     *  K (numbering from 0). No character may be duplicated. */
    Alphabet(String chars) {
        _chars = checkValidation(chars);
    }

    /**
     * This method validates the alphabet whether it contains duplicates
     * or other special characters (*, (, )), which are not valid .
     * @param chars
     * @return a validated string
     */
    private String checkValidation(String chars) {
        char[] charsArray = chars.toCharArray();
        Map<Character, Integer> charCount = new HashMap<>();
        for (char c: charsArray) {
            if (c == '*' || c == '(' || c == ')') {
                throw new EnigmaException("The \"*\", \"(\", and \")\" "
                        + "characters are not allowed to be an alphabet ");
            } else if (charCount.containsKey(c)) {
                throw new EnigmaException("Duplicate found in input chars "
                        + "for the alphabet!");
            } else {
                charCount.put(c, 1);
            }
        }
        return chars;
    }

    /** A default alphabet of all upper-case characters. */
    Alphabet() {
        this("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
    }

    /** Returns the size of the alphabet. */
    int size() {
        return _chars.length();
    }

    /** Returns true if CH is in this alphabet. */
    boolean contains(char ch) {
        return _chars.indexOf(ch) >= 0;
    }

    /** Returns character number INDEX in the alphabet, where
     *  0 <= INDEX < size(). */
    char toChar(int index) {
        if (index < 0 || index > size()) {
            throw new EnigmaException(String.format("The index %d is "
                    + "out of range in the current alphabet!"));
        }
        return _chars.charAt(index);
    }

    /** Returns the index of character CH which must be in
     *  the alphabet. This is the inverse of toChar(). */
    int toInt(char ch) {
        if (!contains(ch)) {
            throw new EnigmaException(String.format("%c not found in "
                    + "the alphabet! (Invalid letter found!)", ch));
        }
        return _chars.indexOf(ch);
    }

}
