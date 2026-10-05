package evolution;

import environment.Region;
import genetics.AncestryRecorder;
import phylogenetics.DistanceMatrix;
import phylogenetics.TreeComparator;
import phylogenetics.UPGMAAlgorithm;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EvolutionEngineTest {
    @Test void stepsPreservePopulationAndRecordBothParents() {
        var ancestry = new AncestryRecorder();
        var founders = PopulationFactory.create(12, 0.5, 17L, "World");
        var engine = new EvolutionEngine(founders, new Region("World", "dark", 1.0), 0.0, 17L, ancestry);
        var first = engine.step();
        assertEquals(1, first.generation());
        assertEquals(12, first.population().size());
        assertEquals(12, ancestry.getRecords().size());
        var birth = ancestry.getBirthRecord(first.population().get(0).getId());
        assertNotNull(birth);
        assertTrue(founders.stream().anyMatch(o -> o.getId() == birth.parent1Id));
        assertTrue(founders.stream().anyMatch(o -> o.getId() == birth.parent2Id));
        assertEquals(1, first.population().get(0).getGeneration());
    }

    @Test void sameSeedProducesSameFounderGenotypes() {
        var a = PopulationFactory.create(8, 0.5, 91L, "World");
        var b = PopulationFactory.create(8, 0.5, 91L, "World");
        for (int i = 0; i < a.size(); i++) {
            assertArrayEquals(a.get(i).getGenotype().getAlleleVariantsAt(0), b.get(i).getGenotype().getAlleleVariantsAt(0));
        }
    }

    @Test void maternalProjectionAndInferredTreeUseMatchingTerminalIds() {
        var ancestry = new AncestryRecorder();
        var engine = new EvolutionEngine(PopulationFactory.create(8, 0.5, 14L, "World"),
            new Region("World", "dark", 1), 0.02, 14L, ancestry);
        engine.step(); engine.step();
        var population = engine.getPopulation();
        var inferred = new UPGMAAlgorithm().buildTree(DistanceMatrix.fromOrganisms(population));
        var projection = ancestry.maternalProjection(population.stream().map(o -> o.getId()).toList());
        assertEquals(inferred.getAllTaxa().size(), projection.getAllTaxa().size());
        assertEquals(0, TreeComparator.robinsonFoulds(projection, projection));
    }
}
