package fr.uem.efluid.tools;

import fr.uem.efluid.utils.ApplicationException;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class FutureAsyncDriverUnitTest {

    @Test
    public void testFutureAsyncDriverNoTimeout() throws InterruptedException {

        FutureAsyncDriver driver = new FutureAsyncDriver(3, 1000, 30);

        TestProcess proc = new TestProcess();

        driver.start(proc, (p) -> p.process(driver));

        // Before timeout
        Thread.sleep(150);

        // Must not be cleaned
        assertThat(driver.listCurrentInSurvey()).hasSize(1);

        // And proc not interrupted
        assertThat(proc.getInterruptedCount()).isEqualTo(0);
    }

    @Test
    public void testFutureAsyncDriverTimeoutAndCleanup() throws InterruptedException {

        FutureAsyncDriver driver = new FutureAsyncDriver(3, 100, 30);

        TestProcess proc = new TestProcess();

        driver.start(proc, (p) -> p.process(driver));

        // After timeout
        Thread.sleep(200);

        // Must be active
        assertThat(driver.listCurrentInSurvey()).isEmpty();

        // And proc interrupted
        assertThat(proc.getInterruptedCount()).isEqualTo(2);
    }

    @Test
    public void testFutureAsyncDriverKill() throws InterruptedException {

        FutureAsyncDriver driver = new FutureAsyncDriver(3, 2000, 30);

        TestProcess proc = new TestProcess();

        driver.start(proc, (p) -> p.process(driver));

        // Must be active
        assertThat(driver.listCurrentInSurvey()).hasSize(1);
        assertThat(proc.getInterruptedCount()).isEqualTo(0);

        // Let it start
        Thread.sleep(200);

        // Manual kill
        driver.kill(proc.getIdentifier());

        // Must be active
        assertThat(driver.listCurrentInSurvey()).isEmpty();

        // And proc interrupted
        assertThat(proc.getInterruptedCount()).isEqualTo(2);
    }

    private static class TestProcess implements AsyncDriver.AsyncSourceProcess {

        private final UUID uuid = UUID.randomUUID();
        private final String desc = "TEST " + this.uuid;
        private final LocalDateTime start = LocalDateTime.now();

        private AtomicInteger interruptedCount = new AtomicInteger(0);

        @Override
        public UUID getIdentifier() {
            return this.uuid;
        }

        @Override
        public String getDescription() {
            return this.desc;
        }

        void process(AsyncDriver driver) {
            try {
                // Main step
                driver.processSteps(Arrays.asList(this::work1Second, this::work1Second), this);
            } catch (InterruptedException e) {
                this.interruptedCount.incrementAndGet();
            }

        }

        String work1Second() {
            try {
                Thread.sleep(1000);
                return "done";
            } catch (InterruptedException e) {
                this.interruptedCount.incrementAndGet();
            }
            return "inter";
        }

        int getInterruptedCount() {
            return this.interruptedCount.get();
        }

        @Override
        public LocalDateTime getCreatedTime() {
            return this.start;
        }

        @Override
        public int getAyncStepNbr() {
            return 2;
        }

        @Override
        public boolean hasSourceFailure() {
            return false;
        }

        @Override
        public int getPercentDone() {
            return 0;
        }

        @Override
        public <F> F fail(ApplicationException error) {
            return null;
        }

        @Override
        public ApplicationException getSourceFailure() {
            return null;
        }
    }
}
