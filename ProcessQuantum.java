public class ProcessQuantum {
    public final Process process;
    public final int quantum;

    /**
     * @param process The process
     * @param quantum The quantum it was processed for
     */
    public ProcessQuantum(Process process, int quantum) {
        this.process = process;
        this.quantum = quantum;
    }

}