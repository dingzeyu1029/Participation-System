import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

public class MainFrame extends JFrame implements ActionListener {
    // Data tracker
    static StudentTracker tracker;
    // Button group for view radio buttons
    ButtonGroup viewSelectButton;

    // Top panel components
    JRadioButton studentViewRadioButton, eventViewRadioButton, reportViewRadioButton;
    JButton detailButton, eventDetailButton, returnButton, eventReturnButton;
    JComboBox<String> quarterListComboBox;
    private final String[] quarterStrings = {"2023Q1", "2023Q2", "2023Q3", "2023Q4"};

    // Bottom panel components
    JButton studentAddButton, eventAddButton, submitButton;
    AddStudentOrEventMenu addStudentOrEventMenu;

    // Detail view buttons (for update/remove)
    static JButton removeButton, studentUpdateButton, eventUpdateButton;
    JButton eventRemoveButton;

    // Center panel tables
    JTable studentTable, eventTable, detailTable, eventDetailTable, reportTable;

    // ComboBoxes for add menu (declared statically to be shared by inner classes)
    static JComboBox<String> classYearComboBox;
    private static final String[] classYearStrings = {"2023", "2024", "2025", "2026", "2027"};
    static JComboBox<String> durationComboBox;
    private static final String[] durationStrings = {"2023Q1", "2023Q2", "2023Q3", "2023Q4"};

    JPanel mainPanel;
    JPanel centerViewPanel = new JPanel();
    CardLayout centerCardLayout = new CardLayout();

    // Other UI components
    StudentPersonalInformation studentPersonalInformation;
    EventInformation eventInformation;
    QuarterlyWinnerInformation quarterlyWinnerInformation;

    // Table models
    DetailViewDataModel detailViewDataModel;
    EventDetailViewDataModel eventDetailViewDataModel;
    ReportViewDataModel reportViewDataModel;
    StudentViewDataModel studentViewDataModel;
    EventViewDataModel eventViewDataModel;

    // Used to remember the last selected target (student/event name)
    private static String selectedTarget = "";

    // Action command constants
    private static final String STUDENT_RADIOBUTTON = "STUDENT_RADIOBUTTON";
    private static final String EVENT_RADIOBUTTON = "EVENT_RADIOBUTTON";
    private static final String REPORT_RADIOBUTTON = "REPORT_RADIOBUTTON";
    private static final String DETAIL_BUTTON = "DETAIL_BUTTON";
    private static final String RETURN_BUTTON = "RETURN_BUTTON";
    private static final String STUDENTADD_BUTTON = "STUDENTADD_BUTTON";
    private static final String EVENTADD_BUTTON = "EVENTADD_BUTTON";
    private static final String SUBMIT_BUTTON = "SUBMIT_BUTTON";
    private static final String REMOVE_BUTTON = "REMOVE_BUTTON";
    private static final String STUDENTUPDATE_BUTTON = "STUDENTUPDATE_BUTTON";
    private static final String EVENTREMOVE_BUTTON = "EVENTREMOVE_BUTTON";
    private static final String EVENTDETAIL_BUTTON = "EVENTDETAIL_BUTTON";
    private static final String EVENTUPDATE_BUTTON = "EVENTUPDATE_BUTTON";
    private static final String EVENTRETURN_BUTTON = "EVENTRETURN_BUTTON";
    private static final String QUARTER_COMBOBOX = "QUARTER_COMBOBOX";

    public MainFrame(StudentTracker tracker) throws IOException {
        setSize(512, 550);
        setLocation(1024, 0);
        setTitle("Attendance");
        MainFrame.tracker = tracker;
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        mainPanel = new JPanel(new BorderLayout());
        setUpTopPanel();
        setUpBottomPanel();
        setUpCenterViewPanel();

        // Add table header and center view panel to mainPanel
        mainPanel.add(studentTable.getTableHeader(), BorderLayout.PAGE_START);
        mainPanel.add(centerViewPanel, BorderLayout.CENTER);
        add(mainPanel);
        setVisible(true);
    }

    // ----------------------- UI Setup Methods -----------------------

    private void setUpTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        JPanel selectionPanel = new JPanel(new FlowLayout());

        studentViewRadioButton = new JRadioButton("Student View", true);
        studentViewRadioButton.addActionListener(this);
        studentViewRadioButton.setActionCommand(STUDENT_RADIOBUTTON);

        eventViewRadioButton = new JRadioButton("Event View", false);
        eventViewRadioButton.addActionListener(this);
        eventViewRadioButton.setActionCommand(EVENT_RADIOBUTTON);

        reportViewRadioButton = new JRadioButton("Report View", false);
        reportViewRadioButton.addActionListener(this);
        reportViewRadioButton.setActionCommand(REPORT_RADIOBUTTON);

        quarterListComboBox = new JComboBox<>(quarterStrings);
        quarterListComboBox.setSelectedIndex(0);
        quarterListComboBox.addActionListener(this);
        quarterListComboBox.setActionCommand(QUARTER_COMBOBOX);
        quarterListComboBox.setRenderer(new CenteredComboBoxRenderer());

        detailButton = new JButton("Detail");
        detailButton.addActionListener(this);
        detailButton.setActionCommand(DETAIL_BUTTON);

        eventDetailButton = new JButton("Detail");
        eventDetailButton.addActionListener(this);
        eventDetailButton.setActionCommand(EVENTDETAIL_BUTTON);

        returnButton = new JButton("Return");
        returnButton.addActionListener(this);
        returnButton.setActionCommand(RETURN_BUTTON);

        eventReturnButton = new JButton("Return");
        eventReturnButton.addActionListener(this);
        eventReturnButton.setActionCommand(EVENTRETURN_BUTTON);

        quarterlyWinnerInformation = new QuarterlyWinnerInformation();

        selectionPanel.add(studentViewRadioButton);
        selectionPanel.add(eventViewRadioButton);
        selectionPanel.add(reportViewRadioButton);
        selectionPanel.add(quarterListComboBox);
        selectionPanel.add(returnButton);
        selectionPanel.add(eventReturnButton);
        selectionPanel.add(detailButton);
        selectionPanel.add(eventDetailButton);

        topPanel.add(selectionPanel, BorderLayout.NORTH);
        topPanel.add(quarterlyWinnerInformation, BorderLayout.SOUTH);

        // Hide controls not needed in the default view
        quarterListComboBox.setVisible(false);
        quarterlyWinnerInformation.setVisible(false);
        returnButton.setVisible(false);
        eventReturnButton.setVisible(false);
        detailButton.setVisible(false);
        eventDetailButton.setVisible(false);

        viewSelectButton = new ButtonGroup();
        viewSelectButton.add(studentViewRadioButton);
        viewSelectButton.add(eventViewRadioButton);
        viewSelectButton.add(reportViewRadioButton);

        mainPanel.add(topPanel, BorderLayout.NORTH);
    }

    private void setUpBottomPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout());
        JPanel addPanel = new JPanel(new FlowLayout());

        submitButton = new JButton("Submit");
        submitButton.addActionListener(this);
        submitButton.setActionCommand(SUBMIT_BUTTON);

        addStudentOrEventMenu = new AddStudentOrEventMenu(submitButton);

        studentAddButton = new JButton("Add");
        studentAddButton.addActionListener(this);
        studentAddButton.setActionCommand(STUDENTADD_BUTTON);

        eventAddButton = new JButton("Add");
        eventAddButton.addActionListener(this);
        eventAddButton.setActionCommand(EVENTADD_BUTTON);

        addPanel.add(studentAddButton);
        addPanel.add(eventAddButton);
        bottomPanel.add(addPanel, BorderLayout.NORTH);
        bottomPanel.add(addStudentOrEventMenu, BorderLayout.SOUTH);

        studentAddButton.setVisible(true);
        eventAddButton.setVisible(false);
        addStudentOrEventMenu.setVisible(false);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
    }

    private void setUpCenterViewPanel() throws IOException {
        // Create table models
        studentViewDataModel = new StudentViewDataModel(tracker);
        eventViewDataModel = new EventViewDataModel(tracker);
        detailViewDataModel = new DetailViewDataModel(selectedTarget, this);
        eventDetailViewDataModel = new EventDetailViewDataModel(selectedTarget, this);
        reportViewDataModel = new ReportViewDataModel(tracker);

        // Create tables using helper method
        studentTable = createCustomTable(studentViewDataModel);
        eventTable = createCustomTable(eventViewDataModel);
        detailTable = createCustomTable(detailViewDataModel);
        eventDetailTable = createCustomTable(eventDetailViewDataModel);
        reportTable = createCustomTable(reportViewDataModel);

        // Add hand-cursor mouse listeners to tables
        addHandCursorListener(studentTable);
        addHandCursorListener(eventTable);

        // Set winner information
        quarterlyWinnerInformation.topStudent.setValue(tracker.getTopStudentOfQuarter("2023Q1").getName());
        quarterlyWinnerInformation.randomWinner.setValue(tracker.getRandomWinner().getName());

        // Mouse listeners to capture selection from tables
        studentTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = studentTable.rowAtPoint(e.getPoint());
                int col = studentTable.columnAtPoint(e.getPoint());
                if (row >= 0 && studentTable.getValueAt(row, col) instanceof String) {
                    selectedTarget = (String) studentTable.getValueAt(row, col);
                    refreshDetails(selectedTarget);
                    detailButton.setVisible(true);
                }
            }
        });
        eventTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = eventTable.rowAtPoint(e.getPoint());
                int col = eventTable.columnAtPoint(e.getPoint());
                if (row >= 0 && eventTable.getValueAt(row, col) instanceof String) {
                    selectedTarget = (String) eventTable.getValueAt(row, col);
                    refreshEventDetails(selectedTarget);
                    eventDetailButton.setVisible(true);
                }
            }
        });

        // Create scroll panes for the tables
        JScrollPane studentScrollPane = new JScrollPane(studentTable);
        JScrollPane eventScrollPane = new JScrollPane(eventTable);
        JScrollPane reportScrollPane = new JScrollPane(reportTable);
        Dimension reportDim = reportScrollPane.getPreferredSize();
        reportScrollPane.setPreferredSize(new Dimension((int) reportDim.getWidth(), (int) reportDim.getHeight() - 50));

        // Panels for scroll panes
        JPanel studentPanel = new JPanel();
        studentPanel.add(studentScrollPane);
        JPanel eventPanel = new JPanel();
        eventPanel.add(eventScrollPane);
        JPanel reportPanel = new JPanel();
        reportPanel.add(reportScrollPane);

        // Add detail panels using helper methods below
        centerViewPanel.setLayout(centerCardLayout);
        centerViewPanel.add(studentPanel, "student");
        centerViewPanel.add(eventPanel, "event");
        centerViewPanel.add(reportPanel, "report");
        centerViewPanel.add(setUpDetailPanel(), "studentDetail");
        centerViewPanel.add(setUpEventDetailPanel(), "eventDetail");

        centerCardLayout.show(centerViewPanel, "student");
    }

    // ----------------------- Helper Methods -----------------------

    // Creates a table with a custom cell renderer for Integer values
    private JTable createCustomTable(AbstractTableModel model) {
        JTable table = new JTable(model) {
            public TableCellRenderer getCellRenderer(int row, int column) {
                Object value = getValueAt(row, column);
                return (value instanceof Integer) ? new CenteredTableCellRenderer() : super.getCellRenderer(row, column);
            }
        };
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);
        return table;
    }

    // Adds a mouse motion listener that changes the cursor to a hand when over a cell
    private void addHandCursorListener(JTable table) {
        table.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) {
                int x = e.getX(), y = e.getY();
                if (table.getRowCount() > 0) {
                    Rectangle top = table.getCellRect(0, 0, true);
                    Rectangle bottom = table.getCellRect(table.getRowCount() - 1, 0, true);
                    if (y >= top.getY() && y <= bottom.getY() + bottom.getHeight() &&
                            table.getBounds().contains(x, y))
                        table.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    else
                        table.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                } else {
                    table.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            }
        });
    }

    // View switching helper methods
    private void showStudentView() {
        centerCardLayout.show(centerViewPanel, "student");
        studentTable.clearSelection();
        quarterListComboBox.setVisible(false);
        studentAddButton.setVisible(true);
        eventAddButton.setVisible(false);
        detailButton.setVisible(false);
        eventDetailButton.setVisible(false);
        quarterlyWinnerInformation.setVisible(false);
        addStudentOrEventMenu.setVisible(false);
        studentViewRadioButton.setVisible(true);
        eventViewRadioButton.setVisible(true);
        reportViewRadioButton.setVisible(true);
        returnButton.setVisible(false);
        refreshTextFieldText();
    }

    private void showEventView() {
        centerCardLayout.show(centerViewPanel, "event");
        eventTable.clearSelection();
        quarterListComboBox.setVisible(false);
        detailButton.setVisible(false);
        eventDetailButton.setVisible(false);
        studentAddButton.setVisible(false);
        eventAddButton.setVisible(true);
        quarterlyWinnerInformation.setVisible(false);
        addStudentOrEventMenu.setVisible(false);
        studentViewRadioButton.setVisible(true);
        eventViewRadioButton.setVisible(true);
        reportViewRadioButton.setVisible(true);
        returnButton.setVisible(false);
        refreshTextFieldText();
    }

    private void showReportView() {
        refreshReport("2023Q1");
        reportTable.repaint();
        quarterListComboBox.setSelectedIndex(0);
        centerCardLayout.show(centerViewPanel, "report");
        quarterListComboBox.setVisible(true);
        quarterlyWinnerInformation.setVisible(true);
        detailButton.setVisible(false);
        eventDetailButton.setVisible(false);
        studentAddButton.setVisible(false);
        eventAddButton.setVisible(false);
        addStudentOrEventMenu.setVisible(false);
        refreshTextFieldText();
    }

    private void showStudentDetailView() {
        centerCardLayout.show(centerViewPanel, "studentDetail");
        studentViewRadioButton.setVisible(false);
        eventViewRadioButton.setVisible(false);
        reportViewRadioButton.setVisible(false);
        returnButton.setVisible(true);
        detailButton.setVisible(false);
        eventDetailButton.setVisible(false);
        quarterListComboBox.setVisible(false);
        quarterlyWinnerInformation.setVisible(false);
        studentAddButton.setVisible(false);
        eventAddButton.setVisible(false);
        addStudentOrEventMenu.setVisible(false);
        refreshTextFieldText();
    }

    private void showEventDetailView() {
        centerCardLayout.show(centerViewPanel, "eventDetail");
        studentViewRadioButton.setVisible(false);
        eventViewRadioButton.setVisible(false);
        reportViewRadioButton.setVisible(false);
        eventReturnButton.setVisible(true);
        eventDetailButton.setVisible(false);
        quarterListComboBox.setVisible(false);
        quarterlyWinnerInformation.setVisible(false);
        studentAddButton.setVisible(false);
        eventAddButton.setVisible(false);
        addStudentOrEventMenu.setVisible(false);
        refreshTextFieldText();
    }

    // ----------------------- Missing Methods -----------------------
    // Creates the panel for student detail view
    private JPanel setUpDetailPanel() {
        JPanel detailPanel = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel();
        removeButton = new JButton("Remove");
        removeButton.addActionListener(this);
        removeButton.setActionCommand(REMOVE_BUTTON);
        JScrollPane detailScrollPane = new JScrollPane(detailTable);
        detailScrollPane.setBorder(BorderFactory.createTitledBorder("Registered Events"));
        detailPanel.add(detailScrollPane, BorderLayout.CENTER);
        studentPersonalInformation = new StudentPersonalInformation();
        detailPanel.add(studentPersonalInformation, BorderLayout.NORTH);
        buttonPanel.add(removeButton);
        detailPanel.add(buttonPanel, BorderLayout.SOUTH);
        return detailPanel;
    }

    // Creates the panel for event detail view
    private JPanel setUpEventDetailPanel() {
        JPanel detailPanel = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel();
        eventRemoveButton = new JButton("Remove");
        eventRemoveButton.addActionListener(this);
        eventRemoveButton.setActionCommand(EVENTREMOVE_BUTTON);
        JScrollPane detailScrollPane = new JScrollPane(eventDetailTable);
        detailScrollPane.setBorder(BorderFactory.createTitledBorder("Students"));
        detailPanel.add(detailScrollPane, BorderLayout.CENTER);
        eventInformation = new EventInformation();
        detailPanel.add(eventInformation, BorderLayout.NORTH);
        buttonPanel.add(eventRemoveButton);
        detailPanel.add(buttonPanel, BorderLayout.SOUTH);
        return detailPanel;
    }

    // ----------------------- ActionListener -----------------------

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        System.out.println(e);
        switch (command) {
            case STUDENT_RADIOBUTTON:
                showStudentView();
                break;
            case EVENT_RADIOBUTTON:
                showEventView();
                break;
            case REPORT_RADIOBUTTON:
                showReportView();
                break;
            case QUARTER_COMBOBOX:
                String duration = (String) ((JComboBox<?>) e.getSource()).getSelectedItem();
                refreshReport(duration);
                reportTable.repaint();
                break;
            case DETAIL_BUTTON:
                showStudentDetailView();
                break;
            case EVENTDETAIL_BUTTON:
                showEventDetailView();
                break;
            case RETURN_BUTTON:
                showStudentView();
                break;
            case EVENTRETURN_BUTTON:
                showEventView();
                break;
            case STUDENTADD_BUTTON:
                studentAddButton.setVisible(false);
                addStudentOrEventMenu.setVisible(true);
                addStudentOrEventMenu.hideDurationTextField();
                break;
            case EVENTADD_BUTTON:
                eventAddButton.setVisible(false);
                addStudentOrEventMenu.setVisible(true);
                addStudentOrEventMenu.hideComboBoxClassYear();
                break;
            case REMOVE_BUTTON:
                try {
                    tracker.removeStudent(tracker.getIdBasedOnName(selectedTarget));
                    refreshStudentTable();
                    refreshReportTable();
                    studentTable.repaint();
                    showStudentView();
                    studentTable.clearSelection();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                break;
            case EVENTREMOVE_BUTTON:
                try {
                    tracker.removeEvent(tracker.getEventIdBasedOnName(selectedTarget));
                    refreshEventTable();
                    eventTable.repaint();
                    showEventView();
                    eventTable.clearSelection();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                break;
            case STUDENTUPDATE_BUTTON:
                try {
                    tracker.setStudentName(selectedTarget, studentPersonalInformation.name.getValue());
                    String selectedValue = (String) studentPersonalInformation.classComboBox.getSelectedItem();
                    int selectedIntValue = Integer.parseInt(selectedValue);
                    tracker.setStudentYear(tracker.getIdBasedOnName(selectedTarget), selectedIntValue);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                break;
            case EVENTUPDATE_BUTTON:
                try {
                    tracker.setEventName(selectedTarget, eventInformation.name.getValue());
                    String selectedValue = (String) eventInformation.eventDurationComboBox.getSelectedItem();
                    tracker.setEventDuration(tracker.getEventIdBasedOnName(selectedTarget), selectedValue);
                    tracker.setEventPoints(tracker.getEventIdBasedOnName(selectedTarget),
                            Integer.parseInt(eventInformation.Points.getValue()));
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                break;
            case SUBMIT_BUTTON:
                if (addStudentOrEventMenu.comboBoxClassYearPanel.isVisible()) {
                    try {
                        if (!addStudentOrEventMenu.name.getValue().isEmpty() &&
                                !Pattern.matches("[0-9]+", addStudentOrEventMenu.name.getValue())) {
                            String selectedValue = (String) classYearComboBox.getSelectedItem();
                            int selectedIntValue = Integer.parseInt(selectedValue);
                            tracker.addStudent(addStudentOrEventMenu.name.getValue(), selectedIntValue);
                            refreshStudentTable();
                            refreshReportTable();
                            refreshAllDetailTables();
                            studentTable.repaint();
                            addStudentOrEventMenu.setVisible(false);
                            studentAddButton.setVisible(true);
                            eventAddButton.setVisible(false);
                        } else {
                            System.out.println("Invalid student input");
                        }
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                } else if (addStudentOrEventMenu.comboBoxDurationPanel.isVisible()) {
                    try {
                        if (!addStudentOrEventMenu.name.getValue().isEmpty() &&
                                !addStudentOrEventMenu.points.getValue().isEmpty() &&
                                !Pattern.matches("[0-9]+", addStudentOrEventMenu.name.getValue()) &&
                                !Pattern.matches("[a-zA-Z]+", addStudentOrEventMenu.points.getValue())) {
                            String selectedValue = (String) durationComboBox.getSelectedItem();
                            tracker.addEvent(addStudentOrEventMenu.name.getValue(),
                                    Integer.parseInt(addStudentOrEventMenu.points.getValue()),
                                    selectedValue);
                            refreshEventTable();
                            refreshAllDetailTables();
                            eventTable.repaint();
                            addStudentOrEventMenu.setVisible(false);
                            eventAddButton.setVisible(true);
                        } else {
                            System.out.println("Invalid event input");
                        }
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
                break;
            default:
                break;
        }
    }

    // ----------------------- Refresh Methods -----------------------

    public void refreshDetails(String selectedTarget) {
        detailViewDataModel.refresh(selectedTarget);
        studentPersonalInformation.name.setTextField(selectedTarget);
        int classYear = tracker.students.get(tracker.getIdBasedOnName(selectedTarget)).getClassYear();
        int selectionIndex = Arrays.asList(2023, 2024, 2025, 2026, 2027).indexOf(classYear);
        studentPersonalInformation.classComboBox.setSelectedIndex(selectionIndex);
        studentPersonalInformation.totalPoints.setValue(String.valueOf(
                tracker.totalPoints(tracker.getIdBasedOnName(selectedTarget))));
    }

    public void refreshEventDetails(String selectedTarget) {
        eventDetailViewDataModel.refresh(selectedTarget);
        eventInformation.name.setTextField(selectedTarget);
        String eventDuration = tracker.events.get(tracker.getEventIdBasedOnName(selectedTarget)).getDuration();
        int selectionIndex = Arrays.asList("2023Q1", "2023Q2", "2023Q3", "2023Q4").indexOf(eventDuration);
        eventInformation.eventDurationComboBox.setSelectedIndex(selectionIndex);
        eventInformation.Points.setTextField(String.valueOf(
                tracker.events.get(tracker.getEventIdBasedOnName(selectedTarget)).getPoints()));
    }

    public void refreshReport(String duration) {
        reportViewDataModel.refresh(duration);
        quarterlyWinnerInformation.topStudent.setValue(tracker.getTopStudentOfQuarter(duration).getName());
        quarterlyWinnerInformation.randomWinner.setValue(tracker.getRandomWinner().getName());
    }

    public void refreshStudentTable() {
        studentViewDataModel.refresh();
    }

    public void refreshEventTable() {
        eventViewDataModel.refresh();
    }

    public void refreshReportTable() {
        reportViewDataModel.refreshReportTable();
    }

    public void refreshTextFieldText() {
        addStudentOrEventMenu.name.setTextField("enter the name");
        addStudentOrEventMenu.points.setTextField("enter the points");
    }

    public void refreshAllDetailTables() {
        detailViewDataModel.refreshTable();
        eventDetailViewDataModel.refreshTable();
    }

    // ----------------------- Inner Classes -----------------------

    private static class LabeledValue extends JPanel {
        private final JLabel nameLabel = new JLabel();
        private final JLabel valueLabel = new JLabel();

        public LabeledValue(String name) {
            setLayout(new FlowLayout(FlowLayout.RIGHT));
            nameLabel.setText(name + ":");
            add(nameLabel);
            add(valueLabel);
        }

        public void setValue(String value) {
            valueLabel.setText(value);
        }
    }

    private static class StudentPersonalInformation extends JPanel {
        public TextValue name = new TextValue("Student Name", selectedTarget);
        public LabeledValue totalPoints = new LabeledValue("Total Points");
        public JComboBox<String> classComboBox;

        public StudentPersonalInformation() {
            JPanel updatePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            studentUpdateButton = new JButton("Update");
            studentUpdateButton.addActionListener(removeButton.getActionListeners()[0]);
            studentUpdateButton.setActionCommand(STUDENTUPDATE_BUTTON);
            updatePanel.add(studentUpdateButton);

            JPanel comboBoxClassYearPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JLabel classYear = new JLabel("Year of Class:");
            classComboBox = new JComboBox<>(classYearStrings);
            classComboBox.setSelectedIndex(0);
            classComboBox.setPreferredSize(name.textField.getPreferredSize());
            classComboBox.setRenderer(new CenteredComboBoxRenderer());
            comboBoxClassYearPanel.add(classYear);
            comboBoxClassYearPanel.add(classComboBox);

            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(BorderFactory.createTitledBorder("Student Personal Information"));
            add(name);
            add(comboBoxClassYearPanel);
            add(totalPoints);
            add(updatePanel);
        }
    }

    private static class EventInformation extends JPanel {
        public TextValue name = new TextValue("Event name", selectedTarget);
        public TextValue Points = new TextValue("Points", "");
        public JComboBox<String> eventDurationComboBox;

        public EventInformation() {
            JPanel updatePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            eventUpdateButton = new JButton("Update");
            eventUpdateButton.addActionListener(removeButton.getActionListeners()[0]);
            eventUpdateButton.setActionCommand(EVENTUPDATE_BUTTON);
            updatePanel.add(eventUpdateButton);

            JPanel comboBoxDurationPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JLabel duration = new JLabel("Duration:");
            eventDurationComboBox = new JComboBox<>(durationStrings);
            eventDurationComboBox.setSelectedIndex(0);
            eventDurationComboBox.setPreferredSize(name.textField.getPreferredSize());
            eventDurationComboBox.setRenderer(new CenteredComboBoxRenderer());
            comboBoxDurationPanel.add(duration);
            comboBoxDurationPanel.add(eventDurationComboBox);

            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(BorderFactory.createTitledBorder("Event information"));
            add(name);
            add(comboBoxDurationPanel);
            add(Points);
            add(updatePanel);
        }
    }

    private static class TextValue extends JPanel {
        private final JTextField textField;

        public TextValue(String name, String text) {
            setLayout(new FlowLayout(FlowLayout.RIGHT));
            textField = new JTextField(text, 10);
            JLabel nameLabel = new JLabel(name + ":");
            add(nameLabel);
            add(textField);
            textField.setHorizontalAlignment(SwingConstants.CENTER);
            textField.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (textField.getText().equals("enter the name")
                            || textField.getText().equals("enter the points")
                            || textField.getText().equals("enter text here"))
                        textField.setText("");
                }
            });
        }

        public String getValue() {
            return textField.getText();
        }

        public void setTextField(String value) {
            textField.setText(value);
        }
    }

    private static class AddStudentOrEventMenu extends JPanel {
        public TextValue name = new TextValue("Name", "enter the name");
        public TextValue points = new TextValue("Points", "enter the points");
        JPanel comboBoxClassYearPanel;
        JPanel comboBoxDurationPanel;

        public AddStudentOrEventMenu(JButton submitButton) {
            comboBoxClassYearPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JLabel classYear = new JLabel("ClassOf:");
            classYearComboBox = new JComboBox<>(classYearStrings);
            classYearComboBox.setSelectedIndex(0);
            classYearComboBox.setPreferredSize(name.textField.getPreferredSize());
            classYearComboBox.setRenderer(new CenteredComboBoxRenderer());
            comboBoxClassYearPanel.add(classYear);
            comboBoxClassYearPanel.add(classYearComboBox);

            comboBoxDurationPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JLabel duration = new JLabel("Duration:");
            durationComboBox = new JComboBox<>(durationStrings);
            durationComboBox.setSelectedIndex(0);
            durationComboBox.setPreferredSize(name.textField.getPreferredSize());
            durationComboBox.setRenderer(new CenteredComboBoxRenderer());
            comboBoxDurationPanel.add(duration);
            comboBoxDurationPanel.add(durationComboBox);

            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(BorderFactory.createTitledBorder("AddMenu"));
            add(name);
            add(comboBoxClassYearPanel);
            add(comboBoxDurationPanel);
            add(points);
            add(submitButton);
            submitButton.setHorizontalAlignment(SwingConstants.CENTER);
            comboBoxClassYearPanel.setVisible(false);
            comboBoxDurationPanel.setVisible(false);
            points.setVisible(false);
        }

        public void hideComboBoxClassYear() {
            comboBoxClassYearPanel.setVisible(false);
            comboBoxDurationPanel.setVisible(true);
            points.setVisible(true);
        }

        public void hideDurationTextField() {
            comboBoxDurationPanel.setVisible(false);
            comboBoxClassYearPanel.setVisible(true);
            points.setVisible(false);
        }
    }

    private static class QuarterlyWinnerInformation extends JPanel {
        public LabeledValue topStudent = new LabeledValue("Top Student");
        public LabeledValue randomWinner = new LabeledValue("Random Winner");

        public QuarterlyWinnerInformation() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(BorderFactory.createTitledBorder("Winner Information"));
            add(topStudent);
            add(randomWinner);
        }
    }

    // ----------------------- Table Models -----------------------

    public static class StudentViewDataModel extends AbstractTableModel {
        private List<Integer> listOfName;
        public static final String[] COLUMN_FIELDS = {"ID", "Name", "Class Of", "Total Points"};

        public StudentViewDataModel(StudentTracker studentTracker) {
            listOfName = new ArrayList<>(studentTracker.students.keySet());
            Collections.sort(listOfName);
        }

        public void refresh() {
            listOfName = new ArrayList<>(tracker.students.keySet());
            Collections.sort(listOfName);
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return tracker.students.size();
        }

        @Override
        public int getColumnCount() {
            return COLUMN_FIELDS.length;
        }

        @Override
        public Object getValueAt(int row, int col) {
            Student student = tracker.students.get(listOfName.get(row));
            return switch (col) {
                case 0 -> student.getId();
                case 1 -> student.getName();
                case 2 -> student.getClassYear();
                case 3 -> tracker.totalPoints(student.getId());
                default -> null;
            };
        }

        @Override
        public String getColumnName(int col) {
            return COLUMN_FIELDS[col];
        }

        @Override
        public Class<?> getColumnClass(int col) {
            return switch (col) {
                case 0, 2, 3 -> Integer.class;
                case 1 -> String.class;
                default -> throw new RuntimeException("bad col:" + col);
            };
        }
    }

    public static class EventViewDataModel extends AbstractTableModel {
        private List<Integer> listOfEvent;
        public static final String[] COLUMN_FIELDS = {"ID", "Name", "Duration", "Points"};

        public EventViewDataModel(StudentTracker studentTracker) {
            listOfEvent = new ArrayList<>(studentTracker.events.keySet());
            Collections.sort(listOfEvent);
        }

        public void refresh() {
            listOfEvent = new ArrayList<>(tracker.events.keySet());
            Collections.sort(listOfEvent);
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return tracker.events.size();
        }

        @Override
        public int getColumnCount() {
            return COLUMN_FIELDS.length;
        }

        @Override
        public Object getValueAt(int row, int col) {
            Events event = tracker.events.get(listOfEvent.get(row));
            return switch (col) {
                case 0 -> event.getId();
                case 1 -> event.getName();
                case 2 -> event.getDuration();
                case 3 -> event.getPoints();
                default -> null;
            };
        }

        @Override
        public String getColumnName(int col) {
            return COLUMN_FIELDS[col];
        }

        @Override
        public Class<?> getColumnClass(int col) {
            return switch (col) {
                case 0, 3 -> Integer.class;
                case 1, 2 -> String.class;
                default -> throw new RuntimeException("bad col:" + col);
            };
        }
    }

    public static class DetailViewDataModel extends AbstractTableModel {
        private List<Integer> listOfRegistration;
        private List<Integer> listOfEvent;
        private String selectedTarget;
        private final MainFrame mainFrame;
        public static final String[] COLUMN_FIELDS = {"ID", "Name", "Duration", "Points", "Y/N"};

        public DetailViewDataModel(String selectedTarget, MainFrame mainFrame) {
            listOfEvent = new ArrayList<>(tracker.events.keySet());
            Collections.sort(listOfEvent);
            this.selectedTarget = selectedTarget;
            this.mainFrame = mainFrame;
            listOfRegistration = tracker.registration.getOrDefault(tracker.getIdBasedOnName(selectedTarget), new ArrayList<>());
            Collections.sort(listOfRegistration);
        }

        public void refresh(String selectedTarget) {
            this.selectedTarget = selectedTarget;
            listOfRegistration = tracker.registration.getOrDefault(tracker.getIdBasedOnName(selectedTarget), new ArrayList<>());
            Collections.sort(listOfRegistration);
        }

        public void refreshTable() {
            listOfEvent = new ArrayList<>(tracker.events.keySet());
            Collections.sort(listOfEvent);
        }

        @Override
        public int getRowCount() {
            return listOfEvent.size();
        }

        @Override
        public int getColumnCount() {
            return COLUMN_FIELDS.length;
        }

        @Override
        public Object getValueAt(int row, int col) {
            Events event = tracker.events.get(listOfEvent.get(row));
            return switch (col) {
                case 0 -> event.getId();
                case 1 -> event.getName();
                case 2 -> event.getDuration();
                case 3 -> event.getPoints();
                case 4 -> registered(event);
                default -> null;
            };
        }

        @Override
        public String getColumnName(int col) {
            return COLUMN_FIELDS[col];
        }

        @Override
        public Class<?> getColumnClass(int col) {
            return switch (col) {
                case 0, 3 -> Integer.class;
                case 1, 2 -> String.class;
                case 4 -> Boolean.class;
                default -> throw new RuntimeException("bad col:" + col);
            };
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return true;
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            Events event = tracker.events.get(listOfEvent.get(row));
            if (Boolean.TRUE.equals(value)) {
                try {
                    tracker.register(tracker.getIdBasedOnName(selectedTarget), event.getId());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                try {
                    tracker.dropoff(tracker.getIdBasedOnName(selectedTarget), event.getId());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            mainFrame.refreshDetails(selectedTarget);
            fireTableCellUpdated(row, col);
        }

        private boolean registered(Events event) {
            return tracker.registration.getOrDefault(tracker.getIdBasedOnName(selectedTarget), new ArrayList<>())
                    .contains(event.getId());
        }
    }

    public static class EventDetailViewDataModel extends AbstractTableModel {
        private List<Integer> listOfRegistration;
        private List<Integer> listOfStudent;
        private String selectedTarget;
        private final MainFrame mainFrame;
        public static final String[] COLUMN_FIELDS = {"ID", "Name", "Class Year", "Total Points", "Y/N"};

        public EventDetailViewDataModel(String selectedTarget, MainFrame mainFrame) {
            listOfStudent = new ArrayList<>(tracker.students.keySet());
            Collections.sort(listOfStudent);
            this.selectedTarget = selectedTarget;
            this.mainFrame = mainFrame;
            listOfRegistration = new ArrayList<>();
            for (Map.Entry<Integer, List<Integer>> entry : tracker.registration.entrySet()) {
                if (entry.getValue().contains(tracker.getEventIdBasedOnName(selectedTarget))) {
                    listOfRegistration.add(entry.getKey());
                }
            }
            Collections.sort(listOfRegistration);
        }

        public void refresh(String selectedTarget) {
            this.selectedTarget = selectedTarget;
            listOfRegistration = new ArrayList<>();
            for (Map.Entry<Integer, List<Integer>> entry : tracker.registration.entrySet()) {
                if (entry.getValue().contains(tracker.getEventIdBasedOnName(selectedTarget))) {
                    listOfRegistration.add(entry.getKey());
                }
            }
            Collections.sort(listOfRegistration);
        }

        public void refreshTable() {
            listOfStudent = new ArrayList<>(tracker.students.keySet());
            Collections.sort(listOfStudent);
        }

        @Override
        public int getRowCount() {
            return listOfStudent.size();
        }

        @Override
        public int getColumnCount() {
            return COLUMN_FIELDS.length;
        }

        @Override
        public Object getValueAt(int row, int col) {
            Student student = tracker.students.get(listOfStudent.get(row));
            return switch (col) {
                case 0 -> student.getId();
                case 1 -> student.getName();
                case 2 -> student.getClassYear();
                case 3 -> tracker.totalPoints(student.getId());
                case 4 -> registered(student);
                default -> null;
            };
        }

        @Override
        public String getColumnName(int col) {
            return COLUMN_FIELDS[col];
        }

        @Override
        public Class<?> getColumnClass(int col) {
            return switch (col) {
                case 0, 2, 3 -> Integer.class;
                case 1 -> String.class;
                case 4 -> Boolean.class;
                default -> throw new RuntimeException("bad col:" + col);
            };
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return true;
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            Student student = tracker.students.get(listOfStudent.get(row));
            if (Boolean.TRUE.equals(value)) {
                try {
                    tracker.register(student.getId(), tracker.getEventIdBasedOnName(selectedTarget));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                try {
                    tracker.dropoff(student.getId(), tracker.getEventIdBasedOnName(selectedTarget));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            mainFrame.refreshEventDetails(selectedTarget);
            fireTableCellUpdated(row, col);
        }

        private boolean registered(Student student) {
            return tracker.registration.getOrDefault(student.getId(), new ArrayList<>())
                    .contains(tracker.getEventIdBasedOnName(selectedTarget));
        }
    }

    public static class ReportViewDataModel extends AbstractTableModel {
        private List<Integer> listOfName;
        private String duration = "2021Q1";
        public static final String[] COLUMN_FIELDS = {"ID", "Name", "Class Of", "Total Points"};

        public ReportViewDataModel(StudentTracker studentTracker) {
            listOfName = new ArrayList<>(studentTracker.students.keySet());
            Collections.sort(listOfName);
        }

        public void refresh(String duration) {
            this.duration = duration;
        }

        public void refreshReportTable() {
            listOfName = new ArrayList<>(tracker.students.keySet());
            Collections.sort(listOfName);
        }

        @Override
        public int getRowCount() {
            return tracker.students.size();
        }

        @Override
        public int getColumnCount() {
            return COLUMN_FIELDS.length;
        }

        @Override
        public Object getValueAt(int row, int col) {
            Student student = tracker.students.get(listOfName.get(row));
            return switch (col) {
                case 0 -> student.getId();
                case 1 -> student.getName();
                case 2 -> student.getClassYear();
                case 3 -> tracker.getQuarterlyPersonReport(duration, student.getId());
                default -> null;
            };
        }

        @Override
        public String getColumnName(int col) {
            return COLUMN_FIELDS[col];
        }

        @Override
        public Class<?> getColumnClass(int col) {
            return switch (col) {
                case 0, 2, 3 -> Integer.class;
                case 1 -> String.class;
                default -> throw new RuntimeException("bad col:" + col);
            };
        }
    }

    // ----------------------- Custom Renderers -----------------------

    static class CenteredComboBoxRenderer extends JLabel implements ListCellRenderer<Object> {
        public CenteredComboBoxRenderer() {
            setHorizontalAlignment(CENTER);
            setOpaque(true);
        }
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            setText(value.toString());
            setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
            setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
            return this;
        }
    }

    static class CenteredTableCellRenderer extends DefaultTableCellRenderer {
        public CenteredTableCellRenderer() {
            setHorizontalAlignment(CENTER);
        }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            if (value instanceof Integer) {
                value = String.valueOf(value);
            }
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
    }
}
