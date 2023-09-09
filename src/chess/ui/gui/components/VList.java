package chess.ui.gui.components;

import chess.util.List;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static chess.ui.Presets.lightGray;

public class VList<T> extends JScrollPane {

    public class Item extends JPanel {

        private boolean isSelected = false;

        public Item(String text) {
            super();
            this.setOpaque(false);
            this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            this.setBorder(new EmptyBorder(7, 10, 7, 10));

            JLabel label = new JLabel(text);
            label.setFont(VList.this.getFont());
            this.add(label);
        }

        public void setSelected(boolean selected) {
            this.isSelected = selected;
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (this.isSelected) {
                Graphics2D graphics = (Graphics2D) g;
                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // offset the width a little gives more space between
                // the highlight box and the thumb
                int width = this.getWidth() - 2;
                int height = this.getHeight();
                int cornerRadius = 20;

                graphics.setColor(lightGray);
                graphics.fillRoundRect(0, 0, width, height,
                        cornerRadius, cornerRadius);
            }
            super.paintComponent(g);
        }

    }

    private final JList<T> jList;

    private final DefaultListModel<T> listModel;

    private final Set<VListListener> listeners = new HashSet<>();

    public VList(Collection<? extends T> collection) {
        DefaultListModel<T> listModel = new DefaultListModel<>();
        listModel.addAll(collection);
        this.listModel = listModel;

        JList<T> jList = new JList<>(listModel);
        jList.setOpaque(false);
        jList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Item item = new Item(value.toString());
                item.setSelected(isSelected);
                return item;
            }
        });
        jList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                VList.this.notifyListeners();
            }
        });
        jList.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                VList.this.notifyListeners();
            }
        });
        jList.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                // Why keyReleased? https://stackoverflow.com/a/40644405
                if (List.of(KeyEvent.VK_UP, KeyEvent.VK_DOWN)
                        .contains(e.getKeyCode())) {
                    VList.this.notifyListeners();
                }
            }
        });
        this.setViewportView(jList);
        this.setBorder(null);
        this.setOpaque(false);
        this.getViewport().setOpaque(false);

        this.makeVerticalScrollbar();
        this.makeHorizontalScrollbar();

        this.jList = jList;
    }

    public VList() {
        this(new List<>());
    }

    private void makeVerticalScrollbar() {
        this.verticalScrollBar.setUI(new BasicScrollBarUI() {
            @Override
            protected JButton createIncreaseButton(int orientation) {
                JButton emptyButton = new JButton();
                emptyButton.setPreferredSize(new Dimension());
                return emptyButton;
            }

            @Override
            protected JButton createDecreaseButton(int orientation) {
                JButton emptyButton = new JButton();
                emptyButton.setPreferredSize(new Dimension());
                return emptyButton;
            }

            @Override
            protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
                // transparent track, paint nothing
            }

            @Override
            protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
                Graphics2D graphics = (Graphics2D) g;
                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int thumbPadding = 2;
                int thumbWidth = thumbBounds.width - thumbPadding * 2;
                int thumbCornerRadius = 15;

                graphics.setColor(lightGray);
                graphics.fillRoundRect(thumbRect.x + thumbPadding, thumbRect.y,
                        thumbWidth, thumbBounds.height,
                        thumbCornerRadius, thumbCornerRadius);
            }
        });
        this.verticalScrollBar.setOpaque(false);
    }

    private void makeHorizontalScrollbar() {
        this.horizontalScrollBar.setUI(new BasicScrollBarUI() {
            @Override
            protected JButton createIncreaseButton(int orientation) {
                JButton emptyButton = new JButton();
                emptyButton.setPreferredSize(new Dimension());
                return emptyButton;
            }

            @Override
            protected JButton createDecreaseButton(int orientation) {
                JButton emptyButton = new JButton();
                emptyButton.setPreferredSize(new Dimension());
                return emptyButton;
            }

            @Override
            protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
                // transparent track, paint nothing
            }

            @Override
            protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
                Graphics2D graphics = (Graphics2D) g;
                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int thumbPadding = 2;
                int thumbHeight = thumbBounds.height - thumbPadding * 2;
                int thumbCornerRadius = 15;

                graphics.setColor(lightGray);
                graphics.fillRoundRect(thumbRect.x, thumbRect.y + thumbPadding,
                        thumbBounds.width, thumbHeight,
                        thumbCornerRadius, thumbCornerRadius);
            }
        });
        this.horizontalScrollBar.setOpaque(false);
    }

    private void notifyListeners() {
        this.listeners.forEach(listener -> listener.selectionUpdated(this));
    }

    public final JList<T> getJList() {
        return this.jList;
    }

    public final DefaultListModel<T> getModel() {
        return this.listModel;
    }

    public final void setSelectionMode(int mode) {
        this.jList.setSelectionMode(mode);
    }

    public final int getSelectedIndex() {
        return this.jList.getSelectedIndex();
    }

    public final @Nullable T getSelectedItem() {
        return this.jList.getSelectedValue();
    }

    public final List<T> getSelectedItems() {
        return new List<>(this.jList.getSelectedValuesList());
    }

    public final boolean hasSelection() {
        return this.getSelectedItem() != null;
    }

    public final void select(@Nullable T item) {
        this.jList.setSelectedValue(item, true);
    }

    public final void selectAll() {
        this.jList.setSelectionInterval(0, this.jList.getLeadSelectionIndex());
    }

    public final void clearSelection() {
        this.select(null);
    }

    public void updateContents(Collection<? extends T> newContents) {
        this.listModel.clear();
        this.listModel.addAll(newContents);
    }

    public final boolean removeItem(T item) {
        return this.listModel.removeElement(item);
    }

    public final void removeItems(Collection<? extends T> items) {
        for (T item : items) {
            this.removeItem(item);
        }
    }

    @FunctionalInterface
    public interface ItemRenderStrategy {
        String apply(Object value, int index, boolean isSelected, boolean cellHasFocus);
    }

    public final void setItemRenderStrategy(ItemRenderStrategy handler) {
        this.jList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Item item = new Item(handler.apply(value, index, isSelected, cellHasFocus));
                item.setSelected(isSelected);
                return item;
            }
        });
    }

    public final void addListViewListener(VListListener listener) {
        this.listeners.add(listener);
    }

    public final void removeViewListener(VListListener listener) {
        this.listeners.remove(listener);
    }

}
