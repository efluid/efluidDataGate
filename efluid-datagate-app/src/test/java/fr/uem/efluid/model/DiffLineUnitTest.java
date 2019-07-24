package fr.uem.efluid.model;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.UUID;

import static fr.uem.efluid.model.entities.IndexAction.*;

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
                DiffLine.combined(dict, key, "ADD1", ADD, 3),
                DiffLine.combined(dict, key, null, REMOVE, 4),
                DiffLine.combined(dict, key, "ADD2", ADD, 5),
                DiffLine.combined(dict, key, null, REMOVE, 6)), false);

        Assert.assertNull(combined);
    }

    @Test
    public void testCombinedDiffLineStayAdd() {

        UUID dict = UUID.randomUUID();
        String key = "NAME1";

        DiffLine combined = DiffLine.combinedOnSameTableAndKey(Arrays.asList(
                DiffLine.combined(dict, key, "ADD1", ADD, 7),
                DiffLine.combined(dict, key, null, REMOVE, 8),
                DiffLine.combined(dict, key, "ADD2", ADD, 9)), false);

        Assert.assertEquals(ADD, combined.getAction());
        Assert.assertEquals("ADD2", combined.getPayload());
    }

    @Test
    public void testCombinedDiffLineStayAddFromModified() {

        UUID dict = UUID.randomUUID();
        String key = "NAME1";

        DiffLine combined = DiffLine.combinedOnSameTableAndKey(Arrays.asList(
                DiffLine.combined(dict, key, "ADD1", ADD, 1),
                DiffLine.combined(dict, key, null, REMOVE, 2),
                DiffLine.combined(dict, key, "ADD2", ADD, 3),
                DiffLine.combined(dict, key, "MODIF3", UPDATE, 4)), false);

        Assert.assertEquals(ADD, combined.getAction());
        Assert.assertEquals("MODIF3", combined.getPayload());
    }

    @Test
    public void testCombinedDiffLineComplex() {

        UUID dict = UUID.randomUUID();
        String key = "NAME1";

        DiffLine combined = DiffLine.combinedOnSameTableAndKey(Arrays.asList(
                DiffLine.combined(dict, key, "ADD1", ADD, 1),
                DiffLine.combined(dict, key, "MODIF1", UPDATE, 2),
                DiffLine.combined(dict, key, null, REMOVE, 3),
                DiffLine.combined(dict, key, "ADD2", ADD, 4),
                DiffLine.combined(dict, key, "MODIF2", UPDATE, 5),
                DiffLine.combined(dict, key, "MODIF3", UPDATE, 6)), false);

        Assert.assertEquals(ADD, combined.getAction());
        Assert.assertEquals("MODIF3", combined.getPayload());
    }
}
