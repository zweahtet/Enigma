package enigma;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ucb.util.CommandArgs;

import static enigma.EnigmaException.*;

/** Enigma simulator.
 *  @author Zwea Htet
 */
public final class Main {

    /** Process a sequence of encryptions and decryptions, as
     *  specified by ARGS, where 1 <= ARGS.length <= 3.
     *  ARGS[0] is the name of a configuration file.
     *  ARGS[1] is optional; when present, it names an input file
     *  containing messages.  Otherwise, input comes from the standard
     *  input.  ARGS[2] is optional; when present, it names an output
     *  file for processed messages.  Otherwise, output goes to the
     *  standard output. Exits normally if there are no errors in the input;
     *  otherwise with code 1. */
    public static void main(String... args) {
        try {
            CommandArgs options =
                new CommandArgs("--verbose --=(.*){1,3}", args);
            if (!options.ok()) {
                throw error("Usage: java enigma.Main [--verbose] "
                            + "[INPUT [OUTPUT]]");
            }

            _verbose = options.contains("--verbose");
            new Main(options.get("--")).process();
            return;
        } catch (EnigmaException excp) {
            System.err.printf("Error: %s%n", excp.getMessage());
        }
        System.exit(1);
    }

    /** Open the necessary files for non-option arguments ARGS (see comment
      *  on main). */
    Main(List<String> args) {
        _config = getInput(args.get(0));

        if (args.size() > 1) {
            _input = getInput(args.get(1));
        } else {
            _input = new Scanner(System.in);
        }

        if (args.size() > 2) {
            _output = getOutput(args.get(2));
        } else {
            _output = System.out;
        }
    }

    /** Return a Scanner reading from the file named NAME. */
    private Scanner getInput(String name) {
        try {
            return new Scanner(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Return a PrintStream writing to the file named NAME. */
    private PrintStream getOutput(String name) {
        try {
            return new PrintStream(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Configure an Enigma machine from the contents of configuration
     *  file _config and apply it to the messages in _input, sending the
     *  results to _output. */
    private void process() {
        Machine machine = readConfig();
        String settings = "";
        Pattern settingsPattern = Pattern
                .compile("^\\*([\\s|\\t]\\w+)+([\\s|\\t](\\(\\w+\\))+)*");
        do {
            String currentLine = _input.nextLine();
            if (checkMatch(settingsPattern, currentLine)) {
                settings = currentLine;
                setUp(machine, settings);
            } else if (!settings.equals("")) {
                printMessageLine(machine.convert(currentLine));
            } else {
                throw new EnigmaException("Missing or Invalid setting!");
            }
        } while (_input.hasNextLine());
    }

    /**
     * Returns true if the string s matches pattern p
     * (referenced from hw4).
     * @param p Pattern object
     * @param s regrex String
     * @return true if pattern and string matches.
     */
    private static boolean checkMatch(Pattern p, String s) {
        Matcher mat = p.matcher(s);
        return mat.matches();
    }

    /** Return an Enigma machine configured from the contents
     *  of configuration file _config. */
    private Machine readConfig() {
        try {
            _allRotors = new HashMap<>();
            String alphabet = _config.next();
            _alphabet = new Alphabet(alphabet);

            _numRotors = _config.nextInt();
            _numPawls = _config.nextInt();

            Pattern rotorDescPattern = Pattern
                    .compile("^(\\s*[^\\(\\)\\s]+\\s*){2}"
                            + "(\\([\\w|.]*\\)\\s*)*");
            while (_config.hasNextLine()) {
                String rotorDesc = _config.nextLine().trim();

                if (checkMatch(rotorDescPattern, rotorDesc)) {
                    while (_config.hasNext("(\\s*\\([\\w|.]"
                            + "*\\)\\s*)*")) {
                        rotorDesc += _config.nextLine().trim();
                    }
                    if (rotorDesc.equals("")) {
                        continue;
                    } else {
                        String[] components = rotorDesc
                                .split("[\s|\t]+");
                        String rotorName = components[0];
                        _allRotors.put(rotorName, readRotor(components));
                    }
                }
            }
            return new Machine(_alphabet, _numRotors, _numPawls, _allRotors);
        } catch (NoSuchElementException excp) {
            throw error("configuration file truncated");
        }
    }

    /**
     * Return a rotor, reading its description from _config.
     * @param components String array of rotor descriptions
     * @return a Rotor
     */
    private Rotor readRotor(String[] components) {
        try {
            String rotorName = "", rotorTypeAndNotches = "", cycles = "";
            for (int i = 0; i < components.length; i++) {
                if (i == 0) {
                    rotorName = components[i];
                } else if (i == 1) {
                    rotorTypeAndNotches = components[i];
                } else {
                    cycles += components[i];
                }
            }
            Permutation perm = new Permutation(cycles, _alphabet);
            char rotorType = rotorTypeAndNotches.charAt(0);
            if (rotorType == 'M') {
                String notches = rotorTypeAndNotches.substring(1);
                return new MovingRotor(rotorName, perm, notches);
            } else if (rotorType == 'N') {
                return new FixedRotor(rotorName, perm);
            } else if (rotorType == 'R') {
                return new Reflector(rotorName, perm);
            } else {
                throw new EnigmaException("Invalid rotor type found!");
            }
        } catch (NoSuchElementException excp) {
            throw error("bad rotor description");
        }
    }

    /** Set M according to the specification given on SETTINGS,
     *  which must have the format specified in the assignment. */
    private void setUp(Machine M, String settings) {
        String[] rotors = new String[_numRotors];
        if (settings.trim().startsWith("*")) {
            settings = settings.substring(2);
        } else {
            throw new EnigmaException("Settings should start with *");
        }
        String[] settingArr = settings.split("\s");
        String cycles = "", initialSetting = "", ringSetting = "";
        for (int i = 0; i < settingArr.length; i++) {
            if (i < _numRotors) {
                rotors[i] = settingArr[i];
            } else if (settingArr[i].startsWith("(")
                    && settingArr[i].endsWith(")")) {
                cycles += settingArr[i] + " ";
            } else if (i == _numRotors) {
                initialSetting = settingArr[i];
            } else {
                ringSetting = settingArr[i];
            }
        }
        M.insertRotors(rotors);
        M.setRotors(initialSetting);
        M.setRingSetting(ringSetting);
        Permutation plugboard = new Permutation(cycles, _alphabet);
        M.setPlugboard(plugboard);
    }

    /** Return true iff verbose option specified. */
    static boolean verbose() {
        return _verbose;
    }

    /** Print MSG in groups of five (except that the last group may
     *  have fewer letters). */
    private void printMessageLine(String msg) {
        int start = 0, msgLength = msg.length(), size = 5;
        String output = "";
        while (msgLength >= size) {
            output += msg.substring(start, start + size) + " ";
            start += size; msgLength -= size;
        }
        if (msgLength > 0) {
            output += msg.substring(start, start + msgLength);
        }
        _output.print(output.trim());
        if (_input.hasNextLine()) {
            _output.print("\r\n");
        }
    }

    /** Alphabet used in this machine. */
    private Alphabet _alphabet;

    /** All Rotors read from config represented as Comparable. */
    private HashMap<String, Rotor> _allRotors;

    /** Source of input messages. */
    private Scanner _input;

    /** Source of machine configuration. */
    private Scanner _config;

    /** File for encoded/decoded messages. */
    private PrintStream _output;

    /** True if --verbose specified. */
    private static boolean _verbose;

    /** Number of Rotors. */
    private int _numRotors;

    /** Number of Pawls. */
    private int _numPawls;
}
