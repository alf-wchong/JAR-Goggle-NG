package chongwm.utility.java.jargoggle.ui;

import chongwm.utility.java.jargoggle.model.ArchiveMatch;
import chongwm.utility.java.jargoggle.model.CombineMode;
import chongwm.utility.java.jargoggle.model.SearchMode;
import chongwm.utility.java.jargoggle.model.SearchOptions;
import chongwm.utility.java.jargoggle.model.SearchProgress;
import chongwm.utility.java.jargoggle.model.SearchResult;
import chongwm.utility.java.jargoggle.model.SearchTargetScope;
import chongwm.utility.java.jargoggle.service.SearchService;
import chongwm.utility.java.jargoggle.util.PreferencesManager;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public final class MainFrame extends JFrame {
    private final DefaultListModel<Path> rootListModel = new DefaultListModel<>();
    private final JList<Path> rootList = new JList<>(rootListModel);
    private final JTextField searchField = new JTextField();
    private final JRadioButton substringButton = new JRadioButton("Substring", true);
    private final JRadioButton exactButton = new JRadioButton("Exact path/class");
    private final JRadioButton globButton = new JRadioButton("Wildcard/glob");
    private final JComboBox<SearchTargetScope> targetScopeCombo = new JComboBox<>(SearchTargetScope.values());
    private final JComboBox<CombineMode> combineModeCombo = new JComboBox<>(CombineMode.values());
    private final JCheckBox recursiveCheck = new JCheckBox("Recursive", true);
    private final JProgressBar progressBar = new JProgressBar(0, 100);
    private final JLabel progressLabel = new JLabel("Idle");
    private final JButton searchButton = new JButton("Search");
    private final JButton cancelButton = new JButton("Cancel");
    private final JTabbedPane resultTabs = new JTabbedPane();
    private final JTextArea historyArea = new JTextArea();

    private final SearchService searchService = new SearchService();
    private SearchService.SearchWorker activeWorker;
    private int resultCounter = 1;

    public MainFrame() {
        super("Jar Goggle NG");
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(1200, 760));
        setPreferredSize(new Dimension(1280, 820));
        buildUi();
        loadPreferences();
        wireActions();
        pack();
        setLocationRelativeTo(null);
    }

    private void buildUi() {
        JPanel content = new JPanel(new BorderLayout(12, 12));
        content.setBorder(new EmptyBorder(12, 12, 12, 12));
        setContentPane(content);

        JPanel topPanel = new JPanel(new GridBagLayout());
        topPanel.setBorder(BorderFactory.createTitledBorder("Search Controls"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        topPanel.add(new JLabel("Query"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1; gbc.gridwidth = 3;
        topPanel.add(searchField, gbc);
        gbc.gridx = 4; gbc.gridy = 0; gbc.gridwidth = 1; gbc.weightx = 0;
        topPanel.add(searchButton, gbc);
        gbc.gridx = 5;
        topPanel.add(cancelButton, gbc);

        ButtonGroup group = new ButtonGroup();
        group.add(substringButton);
        group.add(exactButton);
        group.add(globButton);
        JPanel modePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        modePanel.add(substringButton);
        modePanel.add(exactButton);
        modePanel.add(globButton);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        topPanel.add(new JLabel("Match mode"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1; gbc.gridwidth = 2;
        topPanel.add(modePanel, gbc);
        gbc.gridx = 3; gbc.gridy = 1; gbc.gridwidth = 1; gbc.weightx = 0;
        topPanel.add(new JLabel("Target scope"), gbc);
        gbc.gridx = 4; gbc.gridy = 1; gbc.weightx = 0.4;
        topPanel.add(targetScopeCombo, gbc);
        gbc.gridx = 5; gbc.gridy = 1; gbc.weightx = 0;
        topPanel.add(recursiveCheck, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        topPanel.add(new JLabel("Result operation"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 0.4;
        topPanel.add(combineModeCombo, gbc);
        gbc.gridx = 2; gbc.gridy = 2; gbc.weightx = 1; gbc.gridwidth = 3;
        topPanel.add(progressBar, gbc);
        gbc.gridx = 5; gbc.gridy = 2; gbc.gridwidth = 1; gbc.weightx = 0;
        topPanel.add(progressLabel, gbc);

        content.add(topPanel, BorderLayout.NORTH);

        JSplitPane bodySplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        bodySplit.setResizeWeight(0.24);
        bodySplit.setLeftComponent(buildLeftPane());
        bodySplit.setRightComponent(resultTabs);
        content.add(bodySplit, BorderLayout.CENTER);

        historyArea.setEditable(false);
        historyArea.setLineWrap(true);
        historyArea.setWrapStyleWord(true);
        historyArea.setRows(5);
        JScrollPane historyScroll = new JScrollPane(historyArea);
        historyScroll.setBorder(BorderFactory.createTitledBorder("Search History"));
        content.add(historyScroll, BorderLayout.SOUTH);

        progressBar.setStringPainted(true);
        progressBar.setValue(0);
        cancelButton.setEnabled(false);

        resultTabs.addTab("Welcome", buildWelcomePanel());
    }

    private JPanel buildLeftPane() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Roots"));

        rootList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        rootList.setVisibleRowCount(14);
        panel.add(new JScrollPane(rootList), BorderLayout.CENTER);

        JPanel buttons = new JPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.Y_AXIS));
        JButton addFolder = new JButton("Add Folder");
        JButton addArchive = new JButton("Add Archive");
        JButton remove = new JButton("Remove Selected");
        JButton clear = new JButton("Clear All");
        for (JButton button : List.of(addFolder, addArchive, remove, clear)) {
            button.setAlignmentX(LEFT_ALIGNMENT);
            button.setMaximumSize(new Dimension(Integer.MAX_VALUE, button.getPreferredSize().height));
            buttons.add(button);
            buttons.add(Box.createVerticalStrut(6));
        }
        panel.add(buttons, BorderLayout.SOUTH);

        addFolder.addActionListener(e -> chooseRoot(true));
        addArchive.addActionListener(e -> chooseRoot(false));
        remove.addActionListener(e -> {
            List<Path> selected = rootList.getSelectedValuesList();
            for (Path path : selected) {
                rootListModel.removeElement(path);
            }
            persistRoots();
        });
        clear.addActionListener(e -> {
            rootListModel.clear();
            persistRoots();
        });

        return panel;
    }

    private JPanel buildWelcomePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JTextArea text = new JTextArea();
        text.setEditable(false);
        text.setBorder(new EmptyBorder(12, 12, 12, 12));
        text.setText("Jar Goggle NG\n\n" +
                "- Add root folders or specific JAR/ZIP archives on the left\n" +
                "- Choose substring, exact, or glob matching\n" +
                "- Search all roots, current result set only, or both\n" +
                "- Combine new searches with the current result tab using replace, intersect, union, or subtract\n" +
                "- Use cancel to stop a long-running scan\n");
        panel.add(text, BorderLayout.CENTER);
        return panel;
    }

    private void wireActions() {
        searchButton.addActionListener(e -> startSearch());
        cancelButton.addActionListener(e -> cancelSearch());
        resultTabs.addChangeListener(new TabSelectionListener());
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                persistRoots();
                dispose();
            }
        });
    }

    private void loadPreferences() {
        for (Path path : PreferencesManager.loadRoots()) {
            if (!rootListModel.contains(path)) {
                rootListModel.addElement(path);
            }
        }
    }

    private void chooseRoot(boolean directory) {
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        chooser.setFileSelectionMode(directory ? JFileChooser.DIRECTORIES_ONLY : JFileChooser.FILES_ONLY);
        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }
        for (var file : chooser.getSelectedFiles()) {
            Path path = file.toPath().toAbsolutePath().normalize();
            if (!directory && !isArchive(path)) {
                continue;
            }
            if (!rootListModel.contains(path)) {
                rootListModel.addElement(path);
            }
        }
        persistRoots();
    }

    private void persistRoots() {
        List<Path> roots = new ArrayList<>();
        for (int i = 0; i < rootListModel.size(); i++) {
            roots.add(rootListModel.get(i));
        }
        PreferencesManager.saveRoots(roots);
    }

    private SearchMode selectedSearchMode() {
        if (exactButton.isSelected()) {
            return SearchMode.EXACT;
        }
        if (globButton.isSelected()) {
            return SearchMode.GLOB;
        }
        return SearchMode.SUBSTRING;
    }

    private void startSearch() {
        String query = searchField.getText().trim();
        if (query.isBlank()) {
            JOptionPane.showMessageDialog(this, "Enter a search query.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        List<Path> roots = currentRoots();
        SearchTargetScope scope = (SearchTargetScope) targetScopeCombo.getSelectedItem();
        if ((scope == SearchTargetScope.ALL_ROOTS || scope == SearchTargetScope.CURRENT_RESULT_AND_ROOTS) && roots.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Add at least one root folder or archive.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        SearchResult currentResult = selectedSearchResult();
        if ((scope == SearchTargetScope.CURRENT_RESULT_ONLY || scope == SearchTargetScope.CURRENT_RESULT_AND_ROOTS) && currentResult == null) {
            JOptionPane.showMessageDialog(this, "Select a result tab first for this target scope.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        SearchOptions options = new SearchOptions(
                query,
                selectedSearchMode(),
                recursiveCheck.isSelected(),
                scope,
                (CombineMode) combineModeCombo.getSelectedItem(),
                roots,
                currentResult == null ? List.of() : currentResult.archivePaths()
        );

        setSearching(true);
        activeWorker = searchService.createWorker(options, currentResult, this::updateProgress);
        activeWorker.addPropertyChangeListener(evt -> {
            switch (evt.getPropertyName()) {
                case "progress" -> progressBar.setValue((Integer) evt.getNewValue());
                case "state" -> {
                    if (activeWorker != null && activeWorker.isDone()) {
                        onSearchFinished();
                    }
                }
                default -> {
                }
            }
        });
        activeWorker.execute();
    }

    private void onSearchFinished() {
        try {
            SearchResult result = activeWorker.get();
            addResultTab(result);
            appendHistory(result);
            progressLabel.setText("Done: " + result.archiveCount() + " archive(s), " + result.entryCount() + " entry match(es)");
        } catch (java.util.concurrent.CancellationException ex) {
            progressLabel.setText("Cancelled");
        } catch (Exception ex) {
            Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
            JOptionPane.showMessageDialog(this,
                    "Search failed: " + cause.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            progressLabel.setText("Failed");
        } finally {
            setSearching(false);
            progressBar.setValue(0);
            progressBar.setString("0 / 0");
            activeWorker = null;
        }
    }

    private void updateProgress(SearchProgress progress) {
        String archive = progress.currentArchive() == null ? "" : progress.currentArchive();
        progressBar.setString(progress.totalArchives() == 0 ? "0 / 0" : progress.scannedArchives() + " / " + progress.totalArchives());
        progressLabel.setText("Scanning: " + archive + " | matched archives: " + progress.matchedArchives());
    }

    private void cancelSearch() {
        if (activeWorker != null) {
            activeWorker.cancel(true);
        }
    }

    private void setSearching(boolean searching) {
        searchButton.setEnabled(!searching);
        cancelButton.setEnabled(searching);
        if (searching) {
            progressBar.setValue(0);
            progressBar.setString("0 / 0");
            progressLabel.setText("Starting...");
        }
    }

    private List<Path> currentRoots() {
        List<Path> roots = new ArrayList<>();
        for (int i = 0; i < rootListModel.size(); i++) {
            Path path = rootListModel.get(i);
            if (Files.exists(path)) {
                roots.add(path);
            }
        }
        return roots;
    }

    private SearchResult selectedSearchResult() {
        int index = resultTabs.getSelectedIndex();
        if (index < 0) {
            return null;
        }
        var component = resultTabs.getComponentAt(index);
        if (component instanceof ResultPanel panel) {
            return panel.result();
        }
        return null;
    }

    private void addResultTab(SearchResult result) {
        ResultPanel panel = new ResultPanel(result);
        String title = "R" + resultCounter++;
        resultTabs.addTab(title, panel);
        resultTabs.setSelectedComponent(panel);
    }

    private void appendHistory(SearchResult result) {
        String timestamp = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.systemDefault())
                .format(result.createdAt());
        historyArea.append(timestamp + "\n");
        historyArea.append(result.name() + "\n");
        historyArea.append(result.description() + "\n");
        historyArea.append("Archives: " + result.archiveCount() + ", entries: " + result.entryCount() + "\n\n");
        historyArea.setCaretPosition(historyArea.getDocument().getLength());
    }

    private boolean isArchive(Path path) {
        String name = path.getFileName().toString().toLowerCase();
        return name.endsWith(".jar") || name.endsWith(".zip");
    }

    private final class TabSelectionListener implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent e) {
            SearchResult result = selectedSearchResult();
            if (result == null) {
                return;
            }
            progressLabel.setText("Selected: " + result.archiveCount() + " archive(s), " + result.entryCount() + " entry match(es)");
        }
    }

    private static final class ResultPanel extends JPanel {
        private final SearchResult result;

        private ResultPanel(SearchResult result) {
            super(new BorderLayout(8, 8));
            this.result = result;
            setBorder(new EmptyBorder(8, 8, 8, 8));
            add(buildSummary(), BorderLayout.NORTH);
            add(buildTree(), BorderLayout.CENTER);
        }

        private JPanel buildSummary() {
            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(2, 2, 2, 2);
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;

            gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
            panel.add(new JLabel("Name"), gbc);
            gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1;
            panel.add(new JLabel(result.name()), gbc);
            gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
            panel.add(new JLabel("Summary"), gbc);
            gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1;
            panel.add(new JLabel(result.description() + " · Archives: " + result.archiveCount() + " · Entries: " + result.entryCount()), gbc);
            return panel;
        }

        private JScrollPane buildTree() {
            DefaultMutableTreeNode root = new DefaultMutableTreeNode(result.name());
            for (ArchiveMatch match : result.matches()) {
                DefaultMutableTreeNode archiveNode = new DefaultMutableTreeNode(match.archivePath());
                for (String entry : match.entries()) {
                    archiveNode.add(new DefaultMutableTreeNode(entry));
                }
                root.add(archiveNode);
            }
            JTree tree = new JTree(new DefaultTreeModel(root));
            tree.setRootVisible(false);
            tree.setShowsRootHandles(true);
            tree.setCellRenderer(new ResultTreeRenderer());
            expandFirstLevel(tree, root);
            return new JScrollPane(tree);
        }

        private void expandFirstLevel(JTree tree, DefaultMutableTreeNode root) {
            for (int i = 0; i < root.getChildCount(); i++) {
                tree.expandRow(i + 1);
            }
        }

        public SearchResult result() {
            return result;
        }
    }
}
