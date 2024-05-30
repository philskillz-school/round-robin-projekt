import java.util.*;

public class RoundRobin {

    public final List<Process> processes;
    public final int quantum;

    public RoundRobin(List<Process> processes, int timeQuantum) {
        this.processes = processes;
        this.quantum = timeQuantum;
    }

    public boolean isDone() {
        return processes.stream().allMatch(p -> p.getBurstTime() == 0);
    }

    public void reset() {
        processes.forEach(Process::reset);
    }

    public List<ProcessQuantum> executeAll() {
        Queue<Process> queue = new LinkedList<>(processes);
        List<ProcessQuantum> processQuantums = new ArrayList<>();

        int currentTime = 0;

        while (!queue.isEmpty()) {
            Process currentProcess = queue.poll();

            if (currentProcess.getStartTime() > currentTime) {
                // If process hasn't arrived yet, push it back to the end of the queue
                queue.offer(currentProcess);
                continue;
            }

            ProcessQuantum pq = currentProcess.execute(quantum);
            processQuantums.add(pq);
//            currentTime += quantum;
            currentTime += pq.quantum;

            if (currentProcess.getBurstTime() > 0) {
                queue.offer(currentProcess);
            }
        }

        return processQuantums;
    }
}