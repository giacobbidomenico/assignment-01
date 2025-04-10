package pcd.ass01;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class BoidsView implements ChangeListener {

    private final JFrame frame;
    private final InitialPanel initialPanel;
    private SimulationPanel simulationPanel;
    private final TaskBoidSimulator simulator;
    private final int width, height;

    public BoidsView(TaskBoidSimulator simulator, int width, int height) {
        this.simulator = simulator;
        this.width = width;
        this.height = height;

        frame = new JFrame("Boids Simulation");
        frame.setSize(width, height);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initialPanel = new InitialPanel(this);
        frame.setContentPane(initialPanel);
        frame.setVisible(true);
    }

    public void update(int frameRate) {
        if (simulationPanel != null) {
            simulationPanel.update(frameRate);
        }
    }

    public String getSuspendResumeButtonText() {
        return simulationPanel != null ? simulationPanel.getSuspendResumeButtonText() : "";
    }

    public void updateSuspendResumeButtonText(String text) {
        if (simulationPanel != null) {
            simulationPanel.updateSuspendResumeButtonText(text);
        }
    }

    public void resetToInitialScreen() {
        frame.setContentPane(initialPanel);
        frame.revalidate();
        frame.repaint();
    }

    public void showSimulationScreen(BoidsModel model) {
        simulationPanel = new SimulationPanel(this, model);
        frame.setContentPane(simulationPanel);
        frame.revalidate();
        frame.repaint();
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (simulationPanel == null) return;
        JSlider source = (JSlider) e.getSource();
        double value = source.getValue() * 0.1;
        if (source == simulationPanel.getSeparationSlider()) {
            simulator.setSeparationWeight(value);
        } else if (source == simulationPanel.getCohesionSlider()) {
            simulator.setCohesionWeight(value);
        } else if (source == simulationPanel.getAlignmentSlider()) {
            simulator.setAlignmentWeight(value);
        }
    }

    public TaskBoidSimulator getSimulator() { return simulator; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}