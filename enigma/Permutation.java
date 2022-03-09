package enigma;

import java.util.HashMap;
import java.util.Map;

/** Represents a permutation of a range of integers starting at 0 corresponding
 *  to the characters of an alphabet.
 *  @author Zwea Htet
 */
class Permutation {
    /** Store each cycle of a permutation in an String array. */
    private String[] _cycles;

    /** Store each character in a map to check for duplicates. */
    private Map<Character, Integer> _charCount = new HashMap<>();

    /** Set this Permutation to that specified by CYCLES, a string in the
     *  form "(cccc) (cc) ..." where the c's are characters in ALPHABET, which
     *  is interpreted as a permutation in cycle notation.  Characters in the
     *  alphabet that are not included in any cycle map to themselves.
     *  Whitespace is ignored. */
    Permutation(String cycles, Alphabet alphabet) {
        _alphabet = alphabet;
        _cycles = checkValidation(cycles);
    }

    /**
     * This method checks whether a letter (excluding (, ), space) is
     * in the alphabet or whether it is a duplicate. It also stripped
     * special characters that are not in the alphabet.
     * @param cycles String of cycles
     * @return the validated cycles
     */
    private String[] checkValidation(String cycles) {
        if (cycles.equals("")) {
            return new String[]{};
        }
        String[] cyclesArr = cycles.split("[\\(|\\|\\s|\\t)]");
        for (int index = 0; index < cyclesArr.length; index++) {
            cyclesArr[index] = cyclesArr[index];
            char[] arr = cyclesArr[index].toCharArray();
            for (char c : arr) {
                if (!_alphabet.contains(c)) {
                    throw new EnigmaException(String.format("%c is not "
                            + "in the alphabet!", c));
                } else if (_charCount.containsKey(c)) {
                    throw new EnigmaException(String.format("Duplicate %c "
                            + "is found! A character should appear once "
                            + "and in only one cycle.", c));
                } else {
                    _charCount.put(c, 1);
                }
            }
        }
        return cyclesArr;
    }

    /** Return the value of P modulo the size of this permutation. */
    final int wrap(int p) {
        int r = p % size();
        if (r < 0) {
            r += size();
        }
        return r;
    }

    /** Returns the size of the alphabet I permute. */
    int size() {
        return _alphabet.size();
    }

    /** Return the result of applying this permutation to P modulo the
     *  alphabet size. */
    int permute(int p) {
        int index = wrap(p);
        char c = _alphabet.toChar(index);
        char rightNeighbor = permute(c);
        return _alphabet.toInt(rightNeighbor);
    }

    /** Return the result of applying the inverse of this permutation
     *  to  C modulo the alphabet size. */
    int invert(int c) {
        int index = wrap(c);
        char ch = _alphabet.toChar(index);
        char leftNeighbor = invert(ch);
        return _alphabet.toInt(leftNeighbor);
    }

    /** Return the result of applying this permutation to the index of P
     *  in ALPHABET, and converting the result to a character of ALPHABET. */
    char permute(char p) {
        assert (_alphabet.contains(p));
        String cCycle = permutationHelper(p);
        int cPermIndex = cCycle.indexOf(p);

        if (cPermIndex < 0 || cCycle.length() == 1) {
            return p;
        }

        int rightNeighborIndex = (cPermIndex + 1) % cCycle.length();
        return cCycle.charAt(rightNeighborIndex);
    }

    /** Return the result of applying the inverse of this permutation to C. */
    char invert(char c) {
        assert (_alphabet.contains(c));
        String cCycle = permutationHelper(c);
        int cPermIndex = cCycle.indexOf(c);

        if (cPermIndex < 0 || cCycle.length() == 1) {
            return c;
        }

        int leftNeighborIndex = (cPermIndex - 1) % cCycle.length();

        if (leftNeighborIndex < 0) {
            leftNeighborIndex += cCycle.length();
        }

        return cCycle.charAt(leftNeighborIndex);
    }

    /**
     * This method returns a string representing a specific cycle in which the
     * target character is in, so that way we could figure out which cycle this
     * character is in and use its index (in permutation cycle) to find its left
     * or right neighbor.
     *
     * @param ch char - a character in a permutation
     * @return a string representing a specific cycle
     */
    private String permutationHelper(char ch) {
        String cCycle = "";
        for (String cycle: _cycles) {
            int temp = cycle.indexOf(ch);
            if (temp >= 0) {
                cCycle = cycle;
                break;
            }
        }
        return cCycle;
    }

    /** Return the alphabet used to initialize this Permutation. */
    Alphabet alphabet() {
        return _alphabet;
    }

    /** Return true iff this permutation is a derangement (i.e., a
     *  permutation for which no value maps to itself). */
    boolean derangement() {
        int numAlphabet = _alphabet.size();
        for (int index = 0; index < numAlphabet; index++) {
            char alpha = _alphabet.toChar(index);
            if (permute(alpha) == alpha) {
                return false;
            }
        }
        return true;
    }

    /** Alphabet of this permutation. */
    private Alphabet _alphabet;
}
