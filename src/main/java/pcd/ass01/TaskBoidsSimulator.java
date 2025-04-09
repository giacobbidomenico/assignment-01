package pcd.ass01.task;

import pcd.ass01.Boid;
import pcd.ass01.BoidsModel;
import pcd.ass01.BoidsSimulator;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

public class TaskBoidsSimulator extends BoidsSimulator {

    private ExecutorService executor;

    public TaskBoidsSimulator(BoidsModel model) {
        super(model);
    }

    public void startSimulation(int numBoids) {
        executor = Executors.newFixedThreadPool(N_THREAD);

        if (runningMonitor.isRunning()) return;

        model.createSimulation(numBoids);

        runningMonitor.run();

        final Future<Void> run = executor.submit(() -> {
            runSimulation();
            return null;
        });
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
        if (!suspensionMonitor.isSuspended() && view.isPresent()) {
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
        while (runningMonitor.isRunning()) {
            var t0 = System.currentTimeMillis();
            suspensionMonitor.suspensionUntilResume();
            stateUpdate();
            GUIUpdate(t0);
        }
        stopSimulation();
    }

    public synchronized void toggleSuspendResume() {
        if (!runningMonitor.isRunning()) return;
        suspensionMonitor.changeState();
        if (!suspensionMonitor.isSuspended()) {
            suspensionMonitor.resumeAll();
        }
        if (view.isPresent()) {
            view.get().updateSuspendResumeButtonText(suspensionMonitor.isSuspended() ? "Resume" : "Suspend");
        }
    }

    public void stopSimulation() {
        if (!runningMonitor.isRunning()) return;
        runningMonitor.stop();
        suspensionMonitor.resumeIfSuspended();
        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        model.stopSimulation();
        if (view.isPresent()) {
            view.get().resetToInitialScreen();
        }
    }

}