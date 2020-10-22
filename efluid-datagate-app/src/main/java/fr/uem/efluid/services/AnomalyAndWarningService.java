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
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    public List<Anomaly> getAllAnomalies() {
        List<Anomaly> founds = new ArrayList<>(this.anomalies.findAll());
        // Add not written anomalies
        founds.addAll(this.toWrite);
        return founds;
    }

    @Transactional
    public List<Anomaly> getAnomaliesSince(LocalDateTime time) {
        List<Anomaly> founds = new ArrayList<>(this.anomalies.findByDetectTimeAfter(time));
        this.toWrite.stream().filter(a -> a.getDetectTime().isAfter(time)).forEach(founds::add);
        return founds;
    }

    @Transactional
    public List<Anomaly> getAnomaliesForContext(AnomalyContextType contextType, String contextName) {
        List<Anomaly> founds = new ArrayList<>(this.anomalies.findByContextTypeAndContextName(contextType, contextName));
        this.toWrite.stream().filter(a ->
                a.getContextType() == contextType && a.getContextName().equals(contextName)
        ).forEach(founds::add);
        return founds;
    }

    @Transactional
    public List<String> getContextNamesForType(AnomalyContextType contextType) {
        return Stream.concat(
                this.anomalies.findContextNamesForType(contextType).stream(),
                this.toWrite.stream()
                        .filter(a -> a.getContextType() == contextType)
                        .map(Anomaly::getContextName)
        ).distinct().collect(Collectors.toList());
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
