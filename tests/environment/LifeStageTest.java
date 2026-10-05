package environment;

import core.Organism;
import genetics.Genotype;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LifeStageTest {
    @Test void organismMustMatureBeforeReproducingAndBecomesSenescent() {
        Organism organism = new Organism(new Genotype());
        assertFalse(organism.canReproduce());
        organism.ageUp();
        assertEquals(LifeStage.ADULT, organism.getLifeStage());
        assertTrue(organism.canReproduce());
        organism.ageUp();
        assertEquals(LifeStage.SENESCENT, organism.getLifeStage());
        assertFalse(organism.canReproduce());
    }
}
