package enigma;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AlphabetTest {

    /** Testing time limit. */
    @Rule
    public Timeout globalTimeout = Timeout.seconds(5);

    private Alphabet _alpha = new Alphabet("ABCDE");
    private Permutation _perm = new Permutation("(BACD)", _alpha);

    @Test(expected = EnigmaException.class)
    public void testNotInAlphabet1() {
        try {
            _perm.invert('F');
        } catch (EnigmaException err) {
            System.out.println(String.format("%s (Error detected)", err));
        }
    }

    @Test
    public void testNotInAlphabet2() {
        assertTrue("expected E", _alpha.contains('E'));
        assertFalse("not expected F", _alpha.contains('F'));
    }

    @Test (expected = EnigmaException.class)
    public void testDuplicateAlphabet() {
        try {
            Alphabet alpha = new Alphabet("ABCDA");
        } catch (EnigmaException err) {
            System.out.println(String.format("%s (Error detected)", err));
        }
    }
}
