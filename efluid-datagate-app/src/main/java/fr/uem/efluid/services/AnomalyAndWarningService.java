package fr.uem.efluid.services;

import fr.uem.efluid.model.AnomalyContextType;
import fr.uem.efluid.model.entities.Anomaly;
import fr.uem.efluid.model.repositories.AnomalyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A core service to manage warnings and anomalies identified during any datagate commit process
 *
 * @author elecomte
 * @version 1
 * @since v2.0.19
 */
@Service
public class AnomalyAndWarningService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnomalyAndWarningService.class);

    private final AnomalyRepository anomalies;
    private final ScheduledExecutorService executor;

    private Queue<Anomaly> toWrite = new ConcurrentLinkedQueue<>();


    public AnomalyAndWarningService(@Autowired AnomalyRepository anomalies) {
        this.anomalies = anomalies;
        this.executor = Executors.newScheduledThreadPool(1);
    }

    @PostConstruct
    public void startWriteTask() {
        this.executor.scheduleWithFixedDelay(this::writeDownAnomalies, 0, 3, TimeUnit.SECONDS);
    }

    @Transactional
    public List<Anomaly> getAnomaliesSince(LocalDateTime time) {
        return this.anomalies.findByDetectTimeAfter(time);
    }

    @Transactional
    public List<Anomaly> getAnomaliesForContext(AnomalyContextType contextType, String contextName) {
        return this.anomalies.findByContextTypeAndContextName(contextType, contextName);
    }

    @Transactional
    public List<String> getContextNamesForType(AnomalyContextType contextType) {
        return this.anomalies.findContextNamesForType(contextType);
    }

    @Transactional
    public List<Anomaly> getAnomaliesSince(AnomalyContextType contextType, LocalDateTime time) {
        return this.anomalies.findByDetectTimeAfter(time);
    }

    /**
     * Keep one
     */
    public void addAnomaly(AnomalyContextType contextType, String contextName, String code, String message) {
        Anomaly an = new Anomaly();
        an.setContextType(contextType);
        an.setContextName(contextName);
        an.setCode(code);
        an.setDetectTime(LocalDateTime.now());
        an.setMessage(message);
        this.toWrite.add(an);
    }

    @Transactional
    void writeDownAnomalies() {

        LOGGER.debug("Run anomaly writing");

        int todo = this.toWrite.size();
        if (todo > 0) {

            for (int i = 0; i < 200 && i < todo; i++) {
                this.anomalies.save(this.toWrite.remove());
            }

            this.anomalies.flush();
        }
    }
}
