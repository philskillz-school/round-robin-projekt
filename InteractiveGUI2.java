//import javax.swing.*;
//import javax.swing.Timer;
//import java.awt.*;
//import java.awt.event.MouseEvent;
//import java.awt.event.MouseMotionAdapter;
//import java.util.*;
//import java.util.List;
//import java.util.stream.Collectors;
//
//public class InteractiveGUI2 extends JFrame {
//
//    public enum HorizontalAlign {
//        LEFT,
//        CENTER,
//        RIGHT
//    }
//    public enum VerticalAlign {
//        TOP,
//        CENTER,
//        BOTTOM
//    }
//
//    private final JLabel mousePositionLabel = new JLabel();
//    public static final int windowWidth = 800;
//    public static final int windowHeight = 600;
//
//    public static final int yWindowSpacing = 56; // window takes up some space at the top
//    public static final int xWindowSpacing = 18; // window takes up some space left and right
//
//    /**
//     * Panel to display the process info
//     */
//    @SuppressWarnings("DuplicatedCode")
//    public static class ProcessGridPane extends JPanel {
//
//        // Robin constants
//        private final int TIME_QUANTUM = 3;
//        private final Object[][] processes; // String: name, int burstTime, int startTime
//        private final byte[] processQueue;
//        private int processQueueIndex = 0; // instead of rotating array, this points to the current process in the queue
//        private int processesFinished = 0;
//        private final byte[] delayedProcessQueue;
//        private byte delayedProcessQueueIndex = 0;
//        private int currentTime = 0;
//        private Object[][] executed;
//        private int executedIndex = 0;
//
//        // GUI constants
//        private final int cols = 6;
//        final int timeQuantum = 3; // am besten nur ungerade zahlen
//
//        final int paddingX = 10;
//        final int paddingY = 10;
//
//        final int processInfoStartX = paddingX;
//        final int processInfoEndX = windowWidth-paddingX;
//        final int processInfoStartY = paddingY+40;
//        final int processInfoEndY = processInfoStartY+100;
//
//        final int queueStartX = paddingX;
//        final int queueEndX = windowWidth-paddingX;
//        final int queueStartY = processInfoEndY+40;
//        final int queueEndY = queueStartY+75;
//
//        final int progressStartX = paddingX;
//        final int progressEndX = windowWidth-paddingX;
//        final int progressStartY = queueEndY+40;
//        final int progressEndY = windowHeight-paddingY;
//
//        Button addProcessButton = new Button("Add Process");
//        Button singleQuantumButton = new Button("1");
//        Button allQuantumButton = new Button("All");
//        Button resetButton = new Button("Reset");
//        Button randomizeColorsButton = new Button("Randomize Colors");
//
//        Timer colorRandomizer;
//
//        // End GUI constants
//
//        /**
//         * Panel to display the process info
//         */
//        public ProcessGridPane() {
//            this.setLayout(null); // use absolute positioning
//
//            byte pCount = 5;
//            processes = new Object[pCount][3]; // name, burstTime, startTime
//            processes[0] = new Object[]{"P1", 4, 0};
//            processes[1] = new Object[]{"P2", 4, 0};
//            processes[2] = new Object[]{"P3", 4, timeQuantum*2};
//            processes[3] = new Object[]{"P4", 6, 4};
//            processes[4] = new Object[]{"P5", 7, 4};
//
//            Arrays.sort(processes, new Comparator<Object[]>() {
//                @Override
//                public int compare(Object[] o1, Object[] o2) {
//                    return Integer.compare((int) o1[2], (int) o2[2]);
//                }
//            });
//
//            // initialize process queue and delayed process queue
//            processQueue = new byte[pCount];
//            byte pQueueCount = 0;
//            delayedProcessQueue = new byte[pCount];
//            byte dpQueueCount = 0;
//
//            for (byte i=0; i<pCount; i++) {
//                if ((int) processes[i][2] == 0) {
//                    processQueue[pQueueCount] = i;
//                    pQueueCount++;
//                } else {
//                    delayedProcessQueue[dpQueueCount] = i;
//                    dpQueueCount++;
//                }
//            }
//
//            for (byte i=pQueueCount; i<pCount; i++) {
//                processQueue[i] = -1;
//            }
//            for (byte i=dpQueueCount; i<pCount; i++) {
//                delayedProcessQueue[i] = -1;
//            }
//
//            // calculate total executions
//            int executions = 0;
//            for (byte i=0; i<pCount; i++) {
//                executions += ((int) processes[i][1])/timeQuantum + (((int) processes[i][1])%timeQuantum == 0 ? 0 : 1);
//            }
//
//            executed = new Object[executions][2]; // process, quantum
//
//            singleQuantumButton.setBounds(paddingX, progressStartY-23, 50, 20);
//            singleQuantumButton.addActionListener(e -> {
//                executeOne();
//                this.repaint();
//            });
//
//            allQuantumButton.setBounds(paddingX+50, progressStartY-23, 50, 20);
//            allQuantumButton.addActionListener(e -> {
//                executeAll();
//                this.repaint();
//            });
//
//            resetButton.setBounds(paddingX+100, progressStartY-23, 50, 20);
//            resetButton.addActionListener(e -> {
//                resetProgress();
//                this.repaint();
//            });
//
//            colorRandomizer = new Timer(50, e -> {
//                randomizeColors();
//                this.repaint();
//            });
//
//
//            addProcessButton.setBounds(windowWidth-paddingX-150, progressStartY-23, 150, 20);
//            addProcessButton.addActionListener(e -> {
//                Process p = new Process("P" + (robin.processList.size()+1), 5, robin.getCurrentTime());
//                robin.addProcess(p);
//                this.repaint();
//            });
//
//            randomizeColorsButton.setBounds(windowWidth-paddingX-300, progressStartY-23, 150, 20);
//            randomizeColorsButton.addActionListener(e -> {
//                if (colorRandomizer.isRunning()) {
//                    colorRandomizer.stop();
//                } else {
//                    colorRandomizer.start();
//                }
//            });
//
//            this.add(addProcessButton);
//            this.add(singleQuantumButton);
//            this.add(allQuantumButton);
//            this.add(resetButton);
//            this.add(randomizeColorsButton);
//        }
//
//        private void executeAll() {
//            while (!robin.isDone()) {
//                executeOne();
//            }
//        }
//
//        private void executeOne() {
//            byte currentProcessIndex = processQueue[processQueueIndex];
//            if (currentProcessIndex == -1) {
//                return;
//            }
//
//            Object[] currentProcess = processes[currentProcessIndex];
//            int burstTime = (int) currentProcess[1];
//
//            // execute one quantum
//            int quantum = Math.min(burstTime, timeQuantum);
//            burstTime -= quantum;
//            processes[currentProcessIndex][1] = burstTime;
//
//            Object[] processQuantum = new Object[]{currentProcessIndex, Math.min(burstTime+quantum, timeQuantum)};
//            executed[executedIndex] = processQuantum;
//            executedIndex++;
//            currentTime += quantum;
//
//            // add processes to queue that can now start
//            for (byte i=delayedProcessQueueIndex; i<delayedProcessQueue.length; i++) {
//                byte delayedProcessIndex = delayedProcessQueue[i];
//                if (delayedProcessIndex == -1) {
//                    break; // since the list is sorted, we can break here
//                }
//
//                Object[] delayedProcess = processes[delayedProcessIndex];
//                if ((int) delayedProcess[2] <= currentTime) {
//                    processQueue[processQueueIndex] = delayedProcessIndex;
//                    processQueueIndex++;
//                    delayedProcessQueue[i] = -1;
//                    delayedProcessQueueIndex++;
//                    processesAdded++;
//                } else {
//                    break;
//                }
//            }
//
//            // calculate the next process index
//            if (processQueueIndex < processQueue.length-1 && ) { // if the next index is -1 set to start (where the first is not -1)
//            }
//        }
//
//        /**
//         * Rounds a double to a specified precision
//         * @param value Value to round
//         * @param precision Precision
//         * @return Rounded value
//         */
//        public static double round(double value, int precision) {
//            int scale = (int) Math.pow(10, precision);
//            return (double) Math.round(value * scale) / scale;
//        }
//
//        /**
//         * Draws a string with advanced alignment options
//         * @param g Graphics instance
//         * @param text Text to draw
//         * @param bounds Bounds of the text
//         * @param hAlign Horizontal alignment
//         * @param vAlign Vertical alignment
//         * @param font Font to use
//         */
//        public static void drawStringAdvanced(Graphics g, String text, Rectangle bounds, HorizontalAlign hAlign, VerticalAlign vAlign, Font font) {
//            // Get the FontMetrics
//            FontMetrics metrics = g.getFontMetrics(font);
//
//
//            int sWidth = metrics.stringWidth(text);
//            int x;
//            int y;
//
//            if (hAlign == HorizontalAlign.LEFT) {
//                x = bounds.x;
//            } else if (hAlign == HorizontalAlign.RIGHT) {
//                x = bounds.x + bounds.width - sWidth;
//            } else { // default to center
//                x = bounds.x + (bounds.width - sWidth) / 2;
//            }
//
//            if (vAlign == VerticalAlign.TOP) {
//                y = bounds.y + metrics.getAscent();
//            } else if (vAlign == VerticalAlign.BOTTOM) {
//                y = bounds.y + bounds.height ; //- metrics.getHeight();
//            } else { // default to center
//                y = bounds.y + (bounds.height - metrics.getHeight()) / 2 + metrics.getAscent();
//            }
//
//            Font oldFont = g.getFont();
//            g.setFont(font);
//            // Draw the String
//            g.drawString(text, x, y);
//            g.setFont(oldFont);
//        }
//
//        /**
//         * Draws the process info bar
//         * @param g2d Graphics2D instance
//         * @param startX Start X coordinate
//         * @param startY Start Y coordinate
//         * @param endX End X coordinate
//         * @param endY End Y coordinate
//         */
//        @SuppressWarnings("SameParameterValue")
//        void drawProcessInfo(Graphics2D g2d, int startX, int startY, int endX, int endY) {
//            int totalBurstTime = robin.processList.stream().mapToInt(Process::getBurstTimeOriginal).sum();
//            int totalWidth = endX - startX;
//            int height = endY - startY;
//
//            g2d.setColor(Color.BLACK);
//            g2d.drawRect(startX, startY, totalWidth, height);
//
//            g2d.setColor(Color.CYAN);
//            g2d.fillRect(startX+1, startY+1, totalWidth-1, 30);
//            g2d.setColor(Color.BLACK);
//            int barHeight = 25;
//            drawStringAdvanced(g2d,  "<<<<    " + totalBurstTime + " units total execution time    >>>>", new Rectangle(startX, startY, totalWidth, barHeight), HorizontalAlign.CENTER, VerticalAlign.CENTER, g2d.getFont().deriveFont(Font.BOLD, 16f));
//            startY += barHeight;
//            height -= barHeight;
//
//            totalWidth -= 1; // subtract border width
//            height -= 1; // subtract border width
//            startY += 1;
//
//            Color color = Color.GRAY;
//            int x = startX+1; // width for border
//            for (Process p : robin.processList) { // draw each process in queue
//                float ratio = p.getBurstTimeOriginal() / ((float) totalBurstTime);
//                int width = (int) (ratio * totalWidth);
//                color = p.getColor();
//                g2d.setColor(color);
//                g2d.fillRect(x, startY, width, height);
//
//                g2d.setColor(Color.BLACK);
//
//                Rectangle bounds = new Rectangle(x, startY+height/4, width, height/2);
//                drawStringAdvanced(g2d, p.getName(), bounds, HorizontalAlign.CENTER, VerticalAlign.TOP, g2d.getFont());
//                drawStringAdvanced(g2d, p.getBurstTimeOriginal() + "u (" + round(ratio*100, 1) + "%)", bounds, HorizontalAlign.CENTER, VerticalAlign.CENTER, g2d.getFont());
//                drawStringAdvanced(g2d, p.getStartTime() + "u delay", bounds, HorizontalAlign.CENTER, VerticalAlign.BOTTOM, g2d.getFont());
//                x += width;
//            }
//
//            if (x < endX) {
//                g2d.setColor(color);
//                g2d.fillRect(x, startY, endX-x, height);
//            }
//        }
//
//        /**
//         * Draws a bar chart of the processes
//         * @param g2d Graphics2D instance
//         * @param startX Start X coordinate
//         * @param startY Start Y coordinate
//         * @param endX End X coordinate
//         * @param endY End Y coordinate
//         */
//        @SuppressWarnings("SameParameterValue")
//        void drawProcessQueue(Graphics2D g2d, int startX, int startY, int endX, int endY) {
//            g2d.setColor(Color.BLACK);
//            drawStringAdvanced(g2d, "Process queue", new Rectangle(startX, startY-25, endX, 20), HorizontalAlign.CENTER, VerticalAlign.CENTER, g2d.getFont().deriveFont(Font.BOLD, 20f));
//
//            int totalBurstTimeInQueue = robin.processQueue.stream().mapToInt(p -> Math.min(p.getBurstTime(), timeQuantum)).sum();
//            int totalWidth = endX - startX;
//            int height = endY - startY;
//
//            g2d.setColor(Color.BLACK);
//            g2d.drawRect(startX, startY, totalWidth, height);
//
//            g2d.setColor(Color.BLACK);
//
//            totalWidth -= 1; // subtract border width
//            height -= 1; // subtract border width
//            startY += 1;
//
//
//
//            Color color = Color.GRAY;
//            int x = startX+1; // width for border
//            for (Process p : robin.processQueue) { // draw each process in queue
//                float donePercentage = 1-((float)p.getBurstTime()/p.getBurstTimeOriginal());
//                float ratio = Math.min(p.getBurstTime(), timeQuantum) / ((float) totalBurstTimeInQueue);
//                int width = (int) (ratio * totalWidth);
//
//                color = p.getColor();
//                g2d.setColor(color);
//                g2d.fillRect(x, startY, width, height);
//
//                g2d.setColor(Color.BLACK);
//
//                Rectangle bounds = new Rectangle(x, startY+height/4, width, height/2);
//                drawStringAdvanced(g2d, p.getName(), bounds, HorizontalAlign.CENTER, VerticalAlign.TOP, g2d.getFont());
//                drawStringAdvanced(g2d, p.getBurstTime() + "u left", bounds, HorizontalAlign.CENTER, VerticalAlign.CENTER, g2d.getFont());
//                drawStringAdvanced(g2d, round(donePercentage*100, 1) + "% done", bounds, HorizontalAlign.CENTER, VerticalAlign.BOTTOM, g2d.getFont());
////                drawStringAdvanced(g2d, p.getStartTime() + "u delay", bounds, HorizontalAlign.CENTER, VerticalAlign.BOTTOM, g2d.getFont());
//                x += width;
//            }
//
//            if (x < endX) { // fill remaining space with last color
//                g2d.setColor(color);
//                g2d.fillRect(x, startY, endX-x, height);
//            }
//        }
//
//        /**
//         * Formats the process quantums into a grid
//         * @return 2D List of ProcessQuantum instances
//         */
//        private List<List<ProcessQuantum>> formatQuantumsToRows() {
//            final int maxQuantumsPerRow = cols*robin.timeQuantum;
//            int quantumsElapsedInRow = 0;
//            List<List<ProcessQuantum>> formatted = new ArrayList<>();
//
//            List<ProcessQuantum> currentRow = new ArrayList<>();
//            for (ProcessQuantum pq : robin.executed) {
//                if (quantumsElapsedInRow+pq.quantum <= maxQuantumsPerRow) {
//                    currentRow.add(pq);
//                    quantumsElapsedInRow += pq.quantum;
//                } else {
//                    int remaining = maxQuantumsPerRow - quantumsElapsedInRow;
//                    if (remaining > 0) {
//                        currentRow.add(new ProcessQuantum(pq.process, remaining)); // add the remaining quantum
//                    }
//                    quantumsElapsedInRow = 0;
//                    formatted.add(currentRow);
//                    currentRow = new ArrayList<>();
//                    currentRow.add(new ProcessQuantum(pq.process, pq.quantum - remaining)); // add the remaining quantum
//                    quantumsElapsedInRow += pq.quantum - remaining;
//                }
//            }
//
//            if (!currentRow.isEmpty()) {
//                formatted.add(currentRow);
//            }
//
//            return formatted;
//        }
//
//        /**
//         * Draws a row of processes in the grid
//         * @param g2d Graphics2D instance
//         * @param pqs List of ProcessQuantum instances
//         * @param row Row index
//         * @param startX Start X coordinate
//         * @param startY Start Y coordinate
//         * @param quantumWidth Width of a quantum
//         * @param cellHeight Height of a cell
//         */
//        void drawRow(Graphics2D g2d, List<ProcessQuantum> pqs, int row, int startX, int startY, int quantumWidth, int cellHeight) {
//            int quantumsElapsed = row*cols*robin.timeQuantum;
//
//            g2d.setColor(Color.BLACK);
//
//            int rowQuantumsElapsed = 0;
//            //noinspection ForLoopReplaceableByForEach
//            for (int i=0; i<pqs.size(); i++) {
//                ProcessQuantum pq = pqs.get(i);
//
//                int cellPosX = startX + rowQuantumsElapsed * quantumWidth;
//                int cellWidth = pq.quantum * quantumWidth;
//
//                g2d.setColor(Color.BLACK);
//                g2d.drawRect(cellPosX, startY, cellWidth, cellHeight);
//
//                g2d.setColor(pq.process.getColor());
//                g2d.fillRect(cellPosX+1, startY+1, cellWidth-1, cellHeight-1);
//
//                g2d.setColor(Color.BLACK);
//                drawStringAdvanced(g2d, pq.process.getName(), new Rectangle(cellPosX, startY, cellWidth, cellHeight-10), HorizontalAlign.CENTER, VerticalAlign.CENTER, g2d.getFont());
//                drawStringAdvanced(g2d, pq.quantum + "u", new Rectangle(cellPosX, startY+10, cellWidth, cellHeight), HorizontalAlign.CENTER, VerticalAlign.CENTER, g2d.getFont());
//
//                quantumsElapsed += pq.quantum;
//                rowQuantumsElapsed += pq.quantum;
//
//                drawStringAdvanced(g2d, String.valueOf(quantumsElapsed), new Rectangle(cellPosX, startY, cellWidth-5, cellHeight-5), HorizontalAlign.RIGHT, VerticalAlign.BOTTOM, g2d.getFont());
//            }
//
////            drawStringAdvanced(g2d, String.valueOf(quantumsElapsed-rowQuantumsElapsed), new Rectangle(startX+5+10, startY, quantumWidth-5-10, cellHeight-5), HorizontalAlign.LEFT, VerticalAlign.BOTTOM, g2d.getFont());
//        }
//
//
//        /**
//         * Draws the process start bars on the left side on each quantum where a process starts
//         * If multiple processes start at the same time, the available space is split and the vertical bars are stacked
//         * @param g2d Graphics2D instance
//         * @param startX Start X coordinate
//         * @param startY Start Y coordinate
//         * @param quantumWidth Width of a quantum
//         * @param cellHeight Height of a cell
//         */
//        void drawProcessStartBars(Graphics2D g2d, int startX, int startY, int quantumWidth, int cellHeight) {
//            Map<Integer, List<Process>> groupedByStartTime = robin.processList.stream()
//                .filter(p -> p.getStartTime() < robin.getCurrentTime())
//                .collect(Collectors.groupingBy(Process::getStartTime));
//
//            groupedByStartTime.forEach((startTime, processes) -> {
//                if (processes == null) {
//                    return;
//                }
//
//                final int xStart = startX + (startTime % (cols*robin.timeQuantum)) * quantumWidth;
//                final int barHeight = cellHeight/processes.size();
//                final int barWidth = 10;
//
//                int yStart = startY + (startTime/(cols*robin.timeQuantum))*cellHeight;
//                int y = yStart + 1;
//
//                Color color = Color.GRAY;
//                //noinspection ForLoopReplaceableByForEach
//                for (int i=0; i<processes.size(); i++) {
//                    Process p = processes.get(i);
//
//                    color = p.getColor();
//                    g2d.setColor(color);
//                    g2d.fillRect(xStart+1, y, barWidth, Math.min(barHeight, yStart + cellHeight - y));
//                    y += barHeight;
//                }
//
//                if (y < yStart + cellHeight) {
//                    g2d.setColor(color);
//                    g2d.fillRect(xStart+1, y, barWidth, yStart + cellHeight - y);
//                }
//
//                // draw line to separate the bars from the grid
//                g2d.setColor(Color.BLACK);
//                g2d.drawLine(xStart, yStart, xStart, yStart+cellHeight); // border to the left
//                g2d.drawLine(xStart+barWidth, yStart, xStart+barWidth, yStart+cellHeight); // border to the right
//            });
//        }
//
//        /**
//         * Draws a grid of the processes
//         * @param g2d Graphics2D instance
//         * @param startX Start X coordinate
//         * @param startY Start Y coordinate
//         * @param endX End X coordinate
//         * @param endY End Y coordinate
//         */
//        @SuppressWarnings("SameParameterValue")
//        void drawGrid(Graphics2D g2d, int startX, int startY, int endX, int endY) {
//            g2d.setColor(Color.BLACK);
//            drawStringAdvanced(g2d, "Prozessabfolge", new Rectangle(startX, startY-25, endX, 20), HorizontalAlign.CENTER, VerticalAlign.CENTER, g2d.getFont().deriveFont(Font.BOLD, 20f));
//
//            List<List<ProcessQuantum>> pqs = formatQuantumsToRows();
//            if (pqs.isEmpty()) {
//                return;
//            }
//
//            int panelHeight = endY - startY; // Höhe des Panels
//            int cellHeight = panelHeight / pqs.size(); // Höhe einer Zelle
//            int quantumWidth = (endX - startX) / (cols*robin.timeQuantum);
//
//            for (int i=0; i<pqs.size(); i++) {
//                drawRow(g2d, pqs.get(i), i, startX, startY + i*cellHeight, quantumWidth, cellHeight);
//            }
//
//            drawProcessStartBars(g2d, startX, startY, quantumWidth, cellHeight);
//        }
//
//
//        @Override
//        protected void paintComponent(Graphics g) {
//            super.paintComponent(g);
//            Graphics2D g2d = (Graphics2D) g.create();
//            drawStringAdvanced(g2d, "Round Robin", new Rectangle(0, 0, 800, 40), HorizontalAlign.CENTER, VerticalAlign.CENTER, g2d.getFont().deriveFont(Font.BOLD, 24f));
//
//            g2d.drawString("Quantum: " + robin.timeQuantum + " units", 10, 40);
//
//            drawProcessInfo(g2d, processInfoStartX, processInfoStartY, processInfoEndX, processInfoEndY);
//            drawProcessQueue(g2d, queueStartX, queueStartY, queueEndX, queueEndY);
//            drawGrid(g2d, progressStartX, progressStartY, progressEndX, progressEndY);
//
//            g2d.dispose();
//        }
//
//    }
//
//
//    public InteractiveGUI2() {
//        super();
//
//        initialize();
//
//    }
//
//
//    /**
//     * Initialize the contents of the frame.
//     */
//    private void initialize() {
//        this.setSize(windowWidth+xWindowSpacing, windowHeight+yWindowSpacing);
//
//        this.setTitle("Round Robin");
//        this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("skz logo.png")));
//
//        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//
//        // Add the mouse position label to the bottom right of the frame
//        mousePositionLabel.setHorizontalAlignment(SwingConstants.RIGHT);
//        this.add(mousePositionLabel, BorderLayout.SOUTH);
//
//        ProcessGridPane processGridPane = new ProcessGridPane();
//        this.add(processGridPane);
//        this.setUndecorated(false);
//        this.setVisible(true);
//
//        // Add a MouseMotionListener to the ProcessGridPane to update the mouse position label
//        processGridPane.addMouseMotionListener(new MouseMotionAdapter() {
//            @Override
//            public void mouseMoved(MouseEvent e) {
//                mousePositionLabel.setText("Mouse Position: X=" + e.getX() + ", Y=" + e.getY());
//            }
//        });
//    }
//}