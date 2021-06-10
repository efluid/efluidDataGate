package fr.uem.efluid.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.UUID;

import static fr.uem.efluid.model.entities.IndexAction.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author elecomte
 * @version 1
 * @since v0.0.1
 */
public class DiffLineUnitTest {

    @Test
    public void testCombinedDiffLineAllEliminated() {

        UUID dict = UUID.randomUUID();
        String key = "NAME1";

        DiffLine combined = DiffLine.combinedOnSameTableAndKey(Arrays.asList(
                DiffLine.combined(dict, key, "ADD1", null, ADD, 3),
                DiffLine.combined(dict, key, null, "ADD1", REMOVE, 4),
                DiffLine.combined(dict, key, "ADD2", null, ADD, 5),
                DiffLine.combined(dict, key, null, "ADD2", REMOVE, 6)), false);

        assertNull(combined);
    }

    @Test
    public void testCombinedDiffLineStayAdd() {

        UUID dict = UUID.randomUUID();
        String key = "NAME1";

        DiffLine combined = DiffLine.combinedOnSameTableAndKey(Arrays.asList(
                DiffLine.combined(dict, key, "ADD1", null, ADD, 7),
                DiffLine.combined(dict, key, null, "ADD1", REMOVE, 8),
                DiffLine.combined(dict, key, "ADD2", null, ADD, 9)), false);

        assertEquals(ADD, combined.getAction());
        assertEquals("ADD2", combined.getPayload());
    }

    @Test
    public void testCombinedDiffLineStayAddFromModified() {

        UUID dict = UUID.randomUUID();
        String key = "NAME1";

        DiffLine combined = DiffLine.combinedOnSameTableAndKey(Arrays.asList(
                DiffLine.combined(dict, key, "ADD1", null, ADD, 1),
                DiffLine.combined(dict, key, null, "ADD1", REMOVE, 2),
                DiffLine.combined(dict, key, "ADD2", null, ADD, 3),
                DiffLine.combined(dict, key, "MODIF3", "ADD2", UPDATE, 4)), false);

        assertEquals(ADD, combined.getAction());
        assertEquals("MODIF3", combined.getPayload());
    }

    @Test
    public void testCombinedDiffLineComplex() {

        UUID dict = UUID.randomUUID();
        String key = "NAME1";

        DiffLine combined = DiffLine.combinedOnSameTableAndKey(Arrays.asList(
                DiffLine.combined(dict, key, "ADD1", null, ADD, 1),
                DiffLine.combined(dict, key, "MODIF1", "ADD1", UPDATE, 2),
                DiffLine.combined(dict, key, null, "MODIF1", REMOVE, 3),
                DiffLine.combined(dict, key, "ADD2", null, ADD, 4),
                DiffLine.combined(dict, key, "MODIF2", "ADD2", UPDATE, 5),
                DiffLine.combined(dict, key, "MODIF3", "MODIF2", UPDATE, 6)), false);

        assertEquals(ADD, combined.getAction());
        assertEquals("MODIF3", combined.getPayload());
    }

    @Test
    public void testCombinedDiffLineIdentifyPreviousOnAdd() {

        UUID dict = UUID.randomUUID();
        String key = "NAME1";

        DiffLine combined = DiffLine.combinedOnSameTableAndKey(Arrays.asList(
                DiffLine.combined(dict, key, "ADD1", null, ADD, 1),
                DiffLine.combined(dict, key, "MODIF1", null, UPDATE, 2),
                DiffLine.combined(dict, key, null, null, REMOVE, 3),
                DiffLine.combined(dict, key, "ADD2", null, ADD, 4)), false);

        assertNull(combined.getPrevious());
    }

    @Test
    public void testCombinedDiffLineIdentifyPreviousOnDelete() {

        UUID dict = UUID.randomUUID();
        String key = "NAME1";

        DiffLine combined = DiffLine.combinedOnSameTableAndKey(Arrays.asList(
                DiffLine.combined(dict, key, "MODIF1", null, UPDATE, 2),
                DiffLine.combined(dict, key, null, null, REMOVE, 3),
                DiffLine.combined(dict, key, "ADD2", null, ADD, 4),
                DiffLine.combined(dict, key, "MODIF2", null, UPDATE, 5),
                DiffLine.combined(dict, key, null, null, REMOVE, 6)), true);

        assertEquals("MODIF2", combined.getPrevious());
    }

    @Test
    public void testCombinedDiffLineIdentifyPreviousOnModify() {

        UUID dict = UUID.randomUUID();
        String key = "NAME1";

        DiffLine combined = DiffLine.combinedOnSameTableAndKey(Arrays.asList(
                DiffLine.combined(dict, key, "ADD", null, ADD, 1),
                DiffLine.combined(dict, key, "MODIF1", null, UPDATE, 2),
                DiffLine.combined(dict, key, null, null, REMOVE, 3),
                DiffLine.combined(dict, key, "ADD2", null, ADD, 4),
                DiffLine.combined(dict, key, "MODIF2", null, UPDATE, 5),
                DiffLine.combined(dict, key, "MODIF3", null, UPDATE, 6)), false);

        assertNull(combined.getPrevious());
        assertEquals(ADD, combined.getAction());
    }


}
