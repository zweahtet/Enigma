package enigma;

import java.util.HashMap;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.Timeout;
import static org.junit.Assert.*;

/** The suite of all JUnit tests for the Machine class.
 *  @author Zwea Htet
 */
public class MachineTest {

    /** Testing time limit. */
    @Rule
    public Timeout globalTimeout = Timeout.seconds(5);

    /* ***** TESTS ***** */

    private static final Alphabet AZ = new Alphabet(TestUtils.UPPER_STRING);

    private static final HashMap<String, Rotor> ROTORS = new HashMap<>();
    private static final HashMap<String, Rotor> ROTORSCHECKPOINT =
            new HashMap<>();

    static {
        HashMap<String, String> nav = TestUtils.NAVALA;
        ROTORS.put("B", new Reflector("B", new Permutation(nav.get("B"), AZ)));
        ROTORS.put("Beta",
                new FixedRotor("Beta",
                        new Permutation(nav.get("Beta"), AZ)));
        ROTORS.put("III",
                new MovingRotor("III",
                        new Permutation(nav.get("III"), AZ), "V"));
        ROTORS.put("IV",
                new MovingRotor("IV", new Permutation(nav.get("IV"), AZ),
                        "J"));
        ROTORS.put("I",
                new MovingRotor("I", new Permutation(nav.get("I"), AZ),
                        "Q"));
    }

    static {
        HashMap<String, String> nav2 = TestUtils.CHECKPOINT;
        ROTORSCHECKPOINT.put("B", new Reflector("B",
                new Permutation(nav2.get("B"), AZ)));
        ROTORSCHECKPOINT.put("III",
                new MovingRotor("III",
                        new Permutation(nav2.get("III"), AZ), "M"));
        ROTORSCHECKPOINT.put("II",
                new MovingRotor("II", new Permutation(nav2.get("II"), AZ),
                        "B"));
        ROTORSCHECKPOINT.put("I",
                new MovingRotor("I", new Permutation(nav2.get("I"), AZ),
                        "A"));
    }

    private static final String[] ROTORS1 = { "B", "Beta", "III", "IV", "I" };
    private static final String SETTING1 = "AXLE";
    private static final String[] ROTORS2 = { "B", "III", "II", "I"};
    private static final String SETTING2 = "MAA";

    private Machine mach1() {
        Machine mach = new Machine(AZ, 5, 3, ROTORS);
        mach.insertRotors(ROTORS1);
        mach.setRotors(SETTING1);
        return mach;
    }

    private Machine mach2() {
        Machine mach2 = new Machine(AZ, 4, 3, ROTORSCHECKPOINT);
        mach2.insertRotors(ROTORS2);
        mach2.setRotors(SETTING2);
        return mach2;
    }

    @Test
    public void testInsertRotors() {
        Machine mach = new Machine(AZ, 5, 3, ROTORS);
        mach.insertRotors(ROTORS1);
        assertEquals(5, mach.numRotors());
        assertEquals(3, mach.numPawls());
        assertEquals(AZ, mach.alphabet());
        assertEquals(ROTORS.get("B"), mach.getRotor(0));
        assertEquals(ROTORS.get("Beta"), mach.getRotor(1));
        assertEquals(ROTORS.get("III"), mach.getRotor(2));
        assertEquals(ROTORS.get("IV"), mach.getRotor(3));
        assertEquals(ROTORS.get("I"), mach.getRotor(4));
    }

    @Test
    public void testAdvanceRotors() {
        Machine mach2 = mach2();
        mach2.setPlugboard(new Permutation("(AZ) (MN)", AZ));
        int result1 = mach2.convert(19);
        assertEquals(1, mach2.getRotor(3).setting());
        assertEquals(1, mach2.getRotor(2).setting());
        assertEquals(12, mach2.getRotor(1).setting());
        assertEquals(9, result1);

        int result2 = mach2.convert(0);
        assertEquals(2, mach2.getRotor(3).setting());
        assertEquals(2, mach2.getRotor(2).setting());
        assertEquals(13, mach2.getRotor(1).setting());
        assertEquals(25, result2);

        int result3 = mach2.convert(1);
        assertEquals(3, mach2.getRotor(3).setting());
        assertEquals(2, mach2.getRotor(2).setting());
        assertEquals(13, mach2.getRotor(1).setting());
        assertEquals(19, result3);
    }

    @Test
    public void testConvertChar() {
        Machine mach = mach1();
        mach.setPlugboard(new Permutation("(YF) (HZ)", AZ));
        assertEquals(25, mach.convert(24));
    }

    @Test
    public void testConvertMsg() {
        Machine mach = mach1();
        mach.setPlugboard(new Permutation("(HQ)(EX)(IP)"
                + "(TR)(BY)", AZ));
        assertEquals("QVPQSOKOILPUBKJZPISFXDW",
                mach.convert("FROMHISSHOULDERHIAWATHA"));
    }
}
