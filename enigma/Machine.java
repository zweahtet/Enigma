package enigma;

import java.util.HashMap;
/** Class that represents a complete enigma machine.
 *  @author Zwea Htet
 */
class Machine {

    /** Number of rotors that Enigma machine has. */
    private int _numRotors;

    /** Number of pawls that Enigma machine has. */
    private int _pawls;

    /** PlugBoard of Enigma machine. */
    private Permutation _plugBoard;

    /** Stores all possible rotors from config file. */
    private HashMap<String, Rotor> _allRotors;

    /** Reposition all rotors in slots and set their setting. */
    private HashMap<Integer, Rotor> _rotorsSlot;

    /** A new Enigma machine with alphabet ALPHA, 1 < NUMROTORS rotor slots,
     *  and 0 < PAWLS < NUMROTORS pawls.  ALLROTORS contains all the
     *  available rotors. */
    Machine(Alphabet alpha, int numRotors, int pawls,
            HashMap<String, Rotor> allRotors) {
        if (numRotors == 0 && pawls >= numRotors) {
            throw new EnigmaException("Invalid number of rotors and pawls");
        }
        _alphabet = alpha;
        _numRotors = numRotors;
        _pawls = pawls;
        _plugBoard = new Permutation("", alpha);
        _allRotors = allRotors;
        _rotorsSlot = new HashMap<>();
    }

    /** Return the number of rotor slots I have. */
    int numRotors() {
        return _numRotors;
    }

    /** Return the number pawls (and thus rotating rotors) I have. */
    int numPawls() {
        return _pawls;
    }

    /** Return Rotor #K, where Rotor #0 is the reflector, and Rotor
     *  #(numRotors()-1) is the fast Rotor.  Modifying this Rotor has
     *  undefined results. */
    Rotor getRotor(int k) {
        assert (k >= 0 && k < _numRotors);
        return _rotorsSlot.get(k);
    }

    Alphabet alphabet() {
        return _alphabet;
    }

    /** Set my rotor slots to the rotors named ROTORS from my set of
     *  available rotors (ROTORS[0] names the reflector).
     *  Initially, all rotors are set at their 0 setting. */
    void insertRotors(String[] rotors) {
        validateRotors(rotors);
        int index = 0;
        for (String name: rotors) {
            _rotorsSlot.put(index, _allRotors.get(name));
            index += 1;
        }
    }

    /**
     * This method validates rotor positions and checks for duplicates.
     * since slot 1 should contain Reflector, slot 2 is either Beta or Gamma
     * and each of 3 to # available slots is for other rotors (non-moving or
     * moving).
     * @param rotors
     */
    private void validateRotors(String[] rotors) {
        HashMap<String, Integer> rotorsCount = new HashMap<>();
        for (String name: rotors) {
            if (!_allRotors.containsKey(name)) {
                throw new EnigmaException(String.format("%s is not in the "
                        + "given rotors.", name));
            } else if (rotorsCount.containsKey(name)) {
                throw new EnigmaException(String.format("This %s rotor is "
                        + "repeated, so we can't assign it to slots.", name));
            } else {
                rotorsCount.put(name, 1);
            }
        }

        String firstRotorName = rotors[0];
        if (!(_allRotors.get(firstRotorName) instanceof Reflector)) {
            throw new EnigmaException(String.format("%s is not a "
                    + "Reflector rotor. First rotor should be a reflector.",
                    firstRotorName));
        }

        for (int position = 1; position < _numRotors - _pawls; position++) {
            if (!(_allRotors.get(rotors[position]) instanceof FixedRotor)) {
                throw new EnigmaException("Fixed Rotor in wrong position!");
            }
        }

        for (int position = _numRotors - _pawls; position < _numRotors;
             position++) {
            if (!(_allRotors.get(rotors[position]) instanceof MovingRotor)) {
                throw new EnigmaException("MovingRotor in wrong position!");
            }
        }
    }

    private void checkAlphabet(String str) {
        char[] letters = str.toCharArray();
        for (char letter: letters) {
            if (!_alphabet.contains(letter)) {
                throw new EnigmaException(String.format("%c is not in the "
                                + "alphabet!", letter));
            }
        }
    }

    /** Set my rotors according to SETTING, which must be a string of
     *  numRotors()-1 characters in my alphabet. The first letter refers
     *  to the leftmost rotor setting (not counting the reflector).  */
    void setRotors(String setting) {
        checkAlphabet(setting);
        checkLength(setting);
        char[] letters = setting.toCharArray();
        int index = 1;
        for (char letter: letters) {
            _rotorsSlot.get(index).set(letter);
            index += 1;
        }
    }

    /** Set the ring setting to RINGSETTING. */
    void setRingSetting(String ringSetting) {
        checkAlphabet(ringSetting);
        checkRingSettingLength(ringSetting);
        char[] letters = ringSetting.toCharArray();
        int index = 1;
        if (ringSetting.equals("")) {
            for (int i = 1; i < _numRotors; i++) {
                ringSetting += _alphabet.toChar(0);
            }
        }
        for (char letter: letters) {
            _rotorsSlot.get(index).setRingSetting(letter);
            index += 1;
        }
    }

    private void checkLength(String setting) {
        if (setting.length() != (_numRotors - 1)) {
            throw new EnigmaException("Bad wheel settings!");
        }
    }

    private void checkRingSettingLength(String setting) {
        if (setting.length() > _numRotors - 1) {
            throw new EnigmaException("Bad wheel settings!");
        }
    }

    /** Return the current plugboard's permutation. */
    Permutation plugboard() {
        return _plugBoard;
    }

    /** Set the plugboard to PLUGBOARD. */
    void setPlugboard(Permutation plugboard) {
        _plugBoard = plugboard;
    }

    /** Returns the result of converting the input character C (as an
     *  index in the range 0..alphabet size - 1), after first advancing
     *  the machine. */
    int convert(int c) {
        advanceRotors();
        if (Main.verbose()) {
            System.err.printf("[");
            for (int r = 1; r < numRotors(); r += 1) {
                System.err.printf("%c",
                        alphabet().toChar(getRotor(r).setting()));
            }
            System.err.printf("] %c -> ", alphabet().toChar(c));
        }
        c = plugboard().permute(c);
        if (Main.verbose()) {
            System.err.printf("%c -> ", alphabet().toChar(c));
        }
        c = applyRotors(c);
        c = plugboard().permute(c);
        if (Main.verbose()) {
            System.err.printf("%c%n", alphabet().toChar(c));
        }
        return c;
    }

    /** Advance all rotors to their next position. */
    private void advanceRotors() {
        for (int position = _numRotors - _pawls; position < _numRotors;
             position++) {
            Rotor currentRotor = _rotorsSlot.get(position);
            if (position == _numRotors - 1) {
                currentRotor.advance();
                break;
            }
            Rotor nextRotor = _rotorsSlot.get(position + 1);
            if (nextRotor.atNotch()) {
                currentRotor.advance();
                if (position < _numRotors - 1) {
                    nextRotor.advance();
                    position += 1;
                }
            }
        }
    }


    /** Return the result of applying the rotors to the character C (as an
     *  index in the range 0..alphabet size - 1). */
    private int applyRotors(int c) {
        for (int position = _numRotors - 1; position >= 0; position--) {
            Rotor currentRotor = _rotorsSlot.get(position);
            c = currentRotor.convertForward(c);
        }
        for (int position = 1; position < _numRotors; position++) {
            Rotor currentRotor = _rotorsSlot.get(position);
            c = currentRotor.convertBackward(c);
        }

        return c;
    }

    /** Returns the encoding/decoding of MSG, updating the state of
     *  the rotors accordingly. */
    String convert(String msg) {
        String result = "";
        char[] charArr = msg.toCharArray();
        for (char c: charArr) {
            if (c != ' ') {
                result += _alphabet.toChar(convert(_alphabet.toInt(c)));
            }
        }
        return result;
    }

    /** Common alphabet of my rotors. */
    private final Alphabet _alphabet;
}
