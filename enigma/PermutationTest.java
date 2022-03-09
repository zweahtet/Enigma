package enigma;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.Timeout;
import static org.junit.Assert.*;

import static enigma.TestUtils.*;

/** The suite of all JUnit tests for the Permutation class.
 *  @author Zwea Htet
 */
public class PermutationTest {

    /** Testing time limit. */
    @Rule
    public Timeout globalTimeout = Timeout.seconds(5);

    /* ***** TESTING UTILITIES ***** */
    /**
     * For this lab, you must use this to get a new Permutation,
     * the equivalent to:
     * new Permutation(cycles, alphabet)
     * @return a Permutation with cycles as its cycles and alphabet as
     * its alphabet
     * @see Permutation for description of the Permutation conctructor
     */
    private Permutation getNewPermutation(String cycles, Alphabet alphabet) {
        return new Permutation(cycles, alphabet);
    }

    /**
     * For this lab, you must use this to get a new Alphabet,
     * the equivalent to:
     * new Alphabet(chars)
     * @return an Alphabet with chars as its characters
     * @see Alphabet for description of the Alphabet constructor
     */
    private Alphabet getNewAlphabet(String chars)  {
        return new Alphabet(chars);
    }


    /**
     * For this lab, you must use this to get a new Alphabet,
     * the equivalent to:
     * new Alphabet()
     * @return a default Alphabet with characters ABCD...Z
     * @see Alphabet for description of the Alphabet constructor
     */
    private Alphabet getNewAlphabet() {
        return new Alphabet(UPPER_STRING);
    }

    /** Check that PERM has an ALPHABET whose size is that of
     *  FROMALPHA and TOALPHA and that maps each character of
     *  FROMALPHA to the corresponding character of FROMALPHA, and
     *  vice-versa. TESTID is used in error messages. */
    private void checkPerm(String testId,
                           String fromAlpha, String toAlpha,
                           Permutation perm, Alphabet alpha) {
        int N = fromAlpha.length();
        assertEquals(testId + " (wrong length)", N, perm.size());
        for (int i = 0; i < N; i += 1) {
            char c = fromAlpha.charAt(i), e = toAlpha.charAt(i);
            assertEquals(msg(testId, "wrong translation of '%c'", c),
                    e, perm.permute(c));
            assertEquals(msg(testId, "wrong inverse of '%c'", e),
                    c, perm.invert(e));
            int ci = alpha.toInt(c), ei = alpha.toInt(e);
            assertEquals(msg(testId, "wrong translation of %d", ci),
                    ei, perm.permute(ci));
            assertEquals(msg(testId, "wrong inverse of %d", ei),
                    ci, perm.invert(ei));
        }
    }

    @Test
    public void checkIdTransform() {
        Alphabet alpha = getNewAlphabet();
        Permutation perm = getNewPermutation("", alpha);
        checkPerm("identity", UPPER_STRING, UPPER_STRING, perm, alpha);
    }

    private Alphabet _alpha = getNewAlphabet("ABCDE");
    private Permutation _perm = getNewPermutation("(BACD)", _alpha);

    @Test
    public void testSize() {
        assertEquals(_alpha.size(), _perm.size());
    }

    @Test
    public void testPermuteChar() {
        char c = _alpha.toChar(2),
                b = _alpha.toChar(1),
                e = _alpha.toChar(4);
        assertEquals("incorrect character", 'C', c);
        assertEquals("incorrect character", 'B', b);
        assertEquals("incorrect character", 'E', e);
        assertEquals('C', _perm.permute('A'));
        assertEquals('B', _perm.permute('D'));
        assertEquals('E', _perm.permute('E'));
    }

    @Test
    public void testPermuteInt() {
        int charAi = _alpha.toInt('A'),
                charDi = _alpha.toInt('D'),
                charEi = _alpha.toInt('E');

        assertTrue("(wrong index)", charAi == 0);
        assertTrue("(wrong index)", charDi == 3);
        assertTrue("(wrong index)", charEi == 4);
        assertEquals(msg("index", "wrong transform of %d", charAi),
                2, _perm.permute(charAi));
        assertEquals(msg("index", "wrong transform of %d", charDi),
                1, _perm.permute(charDi));
        assertEquals(msg("index", "wrong transform of %d", charEi),
                4, _perm.permute(charEi));
    }

    @Test
    public void testInvertChar() {
        assertEquals('B', _perm.invert('A'));
        assertEquals('D', _perm.invert('B'));
        assertEquals('E', _perm.invert('E'));
    }

    @Test
    public void testInvertInt() {
        int charAi = _alpha.toInt('A'),
                charBi = _alpha.toInt('B'),
                charEi = _alpha.toInt('E');
        assertTrue("(wrong index)", charAi == 0);
        assertTrue("(wrong index)", charBi == 1);
        assertTrue("(wrong index)", charEi == 4);
        assertEquals(msg("index", "wrong inverse of %d", charAi),
                1, _perm.invert(charAi));
        assertEquals(msg("index", "wrong inverse of %d", charBi),
                3, _perm.invert(charBi));
        assertEquals(msg("index", "wrong inverse of %d", charEi),
                4, _perm.permute(charEi));
    }

    @Test
    public void testPermAlphabet() {
        assertTrue("incorrect alphabet", _alpha.equals(_perm.alphabet()));
    }

    @Test
    public void testDerangement() {
        Permutation perm1 = getNewPermutation("(AB) (CD)", _alpha);
        assertFalse(perm1.derangement());
        Permutation perm2 = getNewPermutation("(AB) (CDE)", _alpha);
        assertTrue(perm2.derangement());

    }


}
