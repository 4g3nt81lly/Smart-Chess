package chess.ui.gui.views;

import chess.model.player.Agent;
import chess.model.player.Player;
import chess.ui.PlayerLoader;
import chess.ui.gui.components.HPanel;
import chess.ui.gui.components.ModalWindow;
import chess.ui.gui.components.VList;
import chess.ui.gui.components.VPanel;
import chess.ui.throwables.LoadAgentsException;
import chess.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

import static javax.swing.JOptionPane.YES_NO_OPTION;
import static javax.swing.JOptionPane.YES_OPTION;

public final class PreferencesWindow extends ModalWindow {

    private final JTabbedPane tabbedPane;

    private VList<Class<? extends Player>> loadedPlayersList;

    private JButton offloadPlayerButton;

    public PreferencesWindow() {
        super(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFocusable(false);

        JPanel generalTabView = this.makeGeneralTabView();
        tabbedPane.addTab("General", generalTabView);

        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        this.addActionStrip(mainPanel);

        this.tabbedPane = tabbedPane;

        this.add(mainPanel);

        this.pack();
        this.setLocationRelativeTo(null);
    }

    private void addActionStrip(JPanel mainPanel) {
        HPanel actionStrip = new HPanel();
        actionStrip.setHorizontalAlignment(RIGHT_ALIGNMENT);

        actionStrip.addSpacer();

        JButton doneButton = new JButton("Done");
        doneButton.setFocusable(false);
        doneButton.addActionListener(e -> this.dismiss());
        actionStrip.add(doneButton);

        this.getRootPane().setDefaultButton(doneButton);

        mainPanel.add(actionStrip, BorderLayout.SOUTH);
    }

    @SuppressWarnings("unchecked")
    private JPanel makeGeneralTabView() {
        VPanel contentPanel = new VPanel();
        contentPanel.setHorizontalAlignment(LEFT_ALIGNMENT);
        contentPanel.setBorder(new EmptyBorder(10, 20, 10, 20));
        contentPanel.setOpaque(false);

        JLabel loadedAgentsLabel = new JLabel("Loaded Agents");
        contentPanel.add(loadedAgentsLabel);

        contentPanel.addSpacer(15);

        HPanel playersControlPanel = new HPanel();
        playersControlPanel.setVerticalAlignment(Component.TOP_ALIGNMENT);
        playersControlPanel.setOpaque(false);

        VPanel editPlayersActions = new VPanel();
        editPlayersActions.setHorizontalAlignment(LEFT_ALIGNMENT);
        editPlayersActions.setOpaque(false);

        JButton loadJARButton = new JButton("Load JAR…");
        loadJARButton.setFocusable(false);
        loadJARButton.addActionListener(e -> this.loadNewAgent(false));
        editPlayersActions.add(loadJARButton);

        JButton loadFromDirButton = new JButton("Load from Dir…");
        loadFromDirButton.setToolTipText("Load new agents from a directory of class files.");
        loadFromDirButton.setFocusable(false);
        loadFromDirButton.addActionListener(e -> this.loadNewAgent(true));
        editPlayersActions.add(loadFromDirButton);

        JButton offloadPlayerButton = new JButton("Offload");
        offloadPlayerButton.setFocusable(false);
        offloadPlayerButton.setEnabled(false);
        offloadPlayerButton.addActionListener(e -> this.offloadSelectedPlayer());
        editPlayersActions.add(offloadPlayerButton);
        this.offloadPlayerButton = offloadPlayerButton;

        VList<Class<? extends Player>> loadedPlayersList = new VList<>();
        Dimension fixedSize = new Dimension(300, 250);
        loadedPlayersList.setPreferredSize(fixedSize);
        loadedPlayersList.setMaximumSize(fixedSize);
        loadedPlayersList.addListViewListener(listView -> this.revalidateOffloadPlayerButton());
        loadedPlayersList.setItemRenderStrategy((value, index, isSelected, cellHasFocus) -> {
            Class<? extends Player> playerClass = (Class<? extends Player>) value;
            return PlayerLoader.getPlayerDescription(playerClass);
        });
        playersControlPanel.add(loadedPlayersList);
        this.loadedPlayersList = loadedPlayersList;
        this.reloadLoadedPlayersList();

        playersControlPanel.addSpacer(10);
        playersControlPanel.add(editPlayersActions);

        contentPanel.add(playersControlPanel);
        return contentPanel;
    }

    private void reloadLoadedPlayersList() {
        this.loadedPlayersList.updateContents(PlayerLoader.getLoadedPlayers());
    }

    private void revalidateOffloadPlayerButton() {
        List<Class<? extends Player>> selectedPlayerClasses = this.loadedPlayersList.getSelectedItems();
        this.offloadPlayerButton.setEnabled(
                !selectedPlayerClasses.isEmpty()
                        && selectedPlayerClasses.andMap(PlayerLoader::canOffload)
        );
    }

    private void loadNewAgent(boolean fromDirectory) {
        if (fromDirectory) {
            ViewUtils.selectDirectory(this, "Choose a directory.")
                    .ifPresent(this::loadNewAgent);
        } else {
            ViewUtils.selectFile(this, "Choose a JAR file.",
                    (file, name) -> name.endsWith(".jar"))
                    .ifPresent(this::loadNewAgent);
        }
    }

    private void loadNewAgent(File file) {
        try {
            PlayerLoader.loadNewAgents(file, this::selectAgentsToLoad);
            this.reloadLoadedPlayersList();
        } catch (IOException e) {
            ViewUtils.alert(this, "Error",
                    "File '" + file + "' could not be loaded as an agent.",
                    JOptionPane.ERROR_MESSAGE);
        } catch (LoadAgentsException exception) {
            ViewUtils.alert(this, "Error",
                    exception.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    @SuppressWarnings("unchecked")
    private void selectAgentsToLoad(List<Class<? extends Agent>> agents) {
        // nothing to select, load the only agent
        if (agents.size() == 1) return;

        ModalWindow selectionDialog = new ModalWindow(this);
        selectionDialog.setFocusResignable(false);

        VPanel panel = new VPanel();
        panel.setHorizontalAlignment(LEFT_ALIGNMENT);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.add(
                new JLabel("Hold Shift or Command/Ctrl for selecting multiple agents to load.")
        );
        panel.addSpacer(20);

        VList<Class<? extends Agent>> agentsList = new VList<>(agents);
        agentsList.setItemRenderStrategy((value, index, isSelected, cellHasFocus) -> {
            Class<? extends Player> playerClass = (Class<? extends Player>) value;
            return PlayerLoader.getPlayerDescription(playerClass);
        });
        agentsList.getJList().addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    selectionDialog.close();
                }
            }
        });
        agents.clear();
        panel.add(agentsList);

        HPanel actionStrip = new HPanel();
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFocusable(false);
        cancelButton.addActionListener(e -> {
            selectionDialog.close();
        });
        JButton loadButton = new JButton("Load");
        loadButton.setFocusable(false);
        loadButton.addActionListener(e -> {
            agents.addAll(agentsList.getSelectedItems());
            selectionDialog.close();
        });
        selectionDialog.getRootPane().setDefaultButton(loadButton);

        actionStrip.add(cancelButton);
        actionStrip.addSpacer();
        actionStrip.add(loadButton);

        panel.add(actionStrip);

        selectionDialog.add(panel);

        selectionDialog.pack();
        selectionDialog.centerRelativeToOwner(this);
        selectionDialog.display();
    }

    private void offloadSelectedPlayer() {
        List<Class<? extends Player>> selectedPlayerClasses = this.loadedPlayersList.getSelectedItems();
        int userResponse = ViewUtils.confirm(this, "Confirm",
                "Proceed to offload " + selectedPlayerClasses.size()
                        + " selected agent(s)?", YES_NO_OPTION);
        if (userResponse == YES_OPTION) {
            selectedPlayerClasses.forEach(PlayerLoader::offloadPlayer);
            this.reloadLoadedPlayersList();
            this.revalidateOffloadPlayerButton();
        }
    }

    @Override
    protected void clickedElsewhere() {
        super.clickedElsewhere();
        loadedPlayersList.clearSelection();
        this.revalidateOffloadPlayerButton();
    }
}
