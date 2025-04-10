package pcd.ass01;

import pcd.ass01.task.PositionUpdateTask;
import pcd.ass01.task.VelocityUpdateTask;
import pcd.ass01.utility.SyncRunning;
import pcd.ass01.utility.SyncSuspension;
import pcd.ass01.utility.SafeBoolean;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class NewTaskBoidsSimulator {

    private static final int FRAMERATE = 25;
    private static final int N_THREAD = Runtime.getRuntime().availableProcessors() + 1;

    private final BoidsModel model;
    private final SyncRunning runningMonitor;
    private final SyncSuspension suspensionMonitor;

    private Lock lock;

    private Optional<BoidsView> view;
    private int framerate;

    private ExecutorService executor;

    private int nBoids;

    private SafeBoolean safeIsRunning;
    private SafeBoolean safeIsStopped;


    public NewTaskBoidsSimulator(BoidsModel model) {
        this.model = model;
        this.runningMonitor = new SyncRunning();
        this.suspensionMonitor = new SyncSuspension();
        this.view = Optional.empty();
        this.lock = new ReentrantLock();
        this.safeIsRunning = new SafeBoolean();
        this.safeIsStopped = new SafeBoolean();
        this.nBoids = 0;
    }

    public void startSimulation(int numBoids) {
        this.nBoids = numBoids;
        this.runningMonitor.start();
    }

    public void run() {
        while(true) {
            this.runningMonitor.waitUntilStart();
            model.createSimulation(nBoids);
            executor = Executors.newFixedThreadPool(N_THREAD);
            runSimulation();
        }
    }

    public void stateUpdate() {
        try {
            var boids = model.getBoids();

            final List<Future<Void>> resultsUpdateVelocity = new LinkedList<>();

            for (Boid boid : boids) {
                final Future<Void> res = executor.submit(new VelocityUpdateTask(model,boid));
                resultsUpdateVelocity.add(res);
            }

            for (Future<Void> updateVelocity : resultsUpdateVelocity) {
                updateVelocity.get();
            }

            final List<Future<Void>> resultsUpdatePosition = new LinkedList<>();

            for (Boid boid : boids) {
                final Future<Void> res = executor.submit(new PositionUpdateTask(model, boid));
                resultsUpdatePosition.add(res);
            }

            for (Future<Void> updatePos : resultsUpdatePosition) {
                updatePos.get();
            }

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public void GUIUpdate(final long t0) {
        if (view.isPresent()) {
            view.get().update(framerate);
            var t1 = System.currentTimeMillis();
            var dtElapsed = t1 - t0;
            var framePeriod = 1000 / FRAMERATE;

            if (dtElapsed < framePeriod) {
                try {
                    Thread.sleep(framePeriod - dtElapsed);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                framerate = FRAMERATE;
            } else {
                framerate = (int) (1000 / dtElapsed);
            }
        }
    }

    public void runSimulation() {
        while (!this.safeIsStopped.get()) {
            var t0 = System.currentTimeMillis();
            suspensionMonitor.suspensionUntilResume();
            stateUpdate();
            GUIUpdate(t0);
        }
    }

    public void toggleSuspendResume() {
        if (this.safeIsStopped.get()) {
            this.safeIsStopped.setFalse();
        } else {
            this.safeIsStopped.setTrue();
            suspensionMonitor.resumeAll();
        }
        view.ifPresent(v -> v.updateSuspendResumeButtonText(this.safeIsStopped.get() ? "Resume" : "Suspend"));
    }

    public void stopSimulation() {
        try {
            executor.shutdown();
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        model.stopSimulation();
        if (view.isPresent()) {
            view.get().resetToInitialScreen();
        }
    }


    public void attachView(BoidsView view) {
        this.view = Optional.of(view);
    }

    public BoidsModel getModel() { return model; }
}