import java.awt.Color;

public class Process {

    private final String name;
    private int burstTime;
    private final int burstTimeOriginal;
    private final int startTime;
    private Color color;


    /**
     * Creates a new process with startTime=0 and a random color
     * @param name Name of the process
     * @param burstTime Burst time of the process
     */
    public Process(String name, int burstTime) {
        this(name, burstTime, 0);
    }

    /**
     * Creates a new process with a random color
     * @param name Name of the process
     * @param burstTime Burst time of the process
     * @param startTime Start time of the process
     */
    public Process(String name, int burstTime, int startTime) {
        this(name, burstTime, startTime, new Color((int) (Math.random() * 0x1000000)));
    }

    /**
     * Creates a new process
     * @param name Name of the process
     * @param burstTime Burst time of the process
     * @param startTime Start time of the process
     * @param color Color of the process
     */
    public Process(String name, int burstTime, int startTime, Color color) {
        this.name = name;
        this.burstTimeOriginal = burstTime;
        this.burstTime = burstTime;
        this.startTime = startTime;
        this.color = color;
    }

    /**
     * @return Name of the process
     */
    public String getName() {
        return name;
    }

    /**
     * This never changes
     * @return Original burst time of the process
     */
    public int getBurstTimeOriginal() {
        return burstTimeOriginal;
    }

    /**
     * This can vary as the process executes
     * @return Burst time of the process
     */
    public int getBurstTime() {
        return burstTime;
    }

    /**
     * @return Start time of the process
     */
    public int getStartTime() {
        return startTime;
    }

    /**
     * @return Color of the process
     */
    public Color getColor() {
        return color;
    }

    /**
     * Sets the color of the process
     */
    public void randomizeColor() {
        color = new Color((int) (Math.random() * 0x1000000));
    }

    /**
     * Resets the burst time of the process to its original value
     */
    public void reset() {
        burstTime = burstTimeOriginal;
    }

    /**
     * @return True if the process has no burst time left
     */
    public boolean isDone() {
        return burstTime == 0;
    }

    /**
     * Executes this process for a quantum
     * @param quantum Quantum to execute
     * @return ProcessQuantum instance
     */
    public ProcessQuantum execute(int quantum) {
        int q = Math.min(burstTime, quantum);
        burstTime -= q; // ensure burstTime does not go below 0
        return new ProcessQuantum(this, Math.min(burstTime+q, quantum));
    }
}
