import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;

public class MenuMatchCenter extends JFrame {

    // Paleta de Cores Ajustada (Foco no Verde/Amarelo, menos Azul)
    private final Color DARK_GREEN = new Color(20, 70, 35); // Verde muito escuro para texto e cabeçalhos (legibilidade)
    private final Color ACCENT_YELLOW = new Color(255, 204, 0); // Amarelo vibrante para destaques
    private final Color PANEL_WHITE = Color.WHITE;
    private final Color TEXT_DARK = new Color(30, 40, 30); // Quase preto/esverdeado para o texto geral
    private final Color TABLE_GRID = new Color(220, 230, 220);
    private final Color TABLE_ROW_ALTERNATE = new Color(245, 250, 245); // Verde muito claro para linhas alternadas

    private final Font MAIN_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font BOLD_FONT = new Font("Segoe UI", Font.BOLD, 14);
    private final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 18);

    public MenuMatchCenter() {
        setTitle("FIFA World Cup™ Match Center - Gestão");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // PAINEL DE CONTEÚDO COM GRADIENTE DIAGONAL (Top-Left para Bottom-Right)
        setContentPane(new GradientPanel());
        getContentPane().setLayout(new BorderLayout(20, 20));
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(25, 25, 25, 25));

        // Adicionar os 3 módulos principais
        add(createMatchCreationPanel(), BorderLayout.NORTH);
        add(createAllGroupsPanel(), BorderLayout.CENTER);
        add(createSidebarPanel(), BorderLayout.EAST);
    }

    // Painel customizado para o fundo gradiente da imagem
    private class GradientPanel extends JPanel {
        public GradientPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            int w = getWidth(), h = getHeight();

            // Cores extraídas da imagem (Canto Sup. Esq. para Canto Inf. Dir.)
            Color colorTopLeft = new Color(230, 240, 20); // Verde Lima / Amarelado
            Color colorBottomRight = new Color(10, 140, 50); // Verde mais sólido/escuro

            // Gradiente diagonal
            GradientPaint gp = new GradientPaint(0, 0, colorTopLeft, w, h, colorBottomRight);
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, w, h);
            super.paintComponent(g);
        }
    }

    // Painéis de conteúdo translúcidos para dar destaque sem perder o fundo
    private JPanel createSubtlePanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Branco com transparência (Alpha 230 de 255)
                g2d.setColor(new Color(255, 255, 255, 230));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                super.paintComponent(g);
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(10, 10, 10, 10),
                new EmptyBorder(10, 15, 15, 15)
        ));
        return panel;
    }

    // --- 1. MÓDULO: CRIAÇÃO DE JOGOS ---
    private JPanel createMatchCreationPanel() {
        JPanel panel = createSubtlePanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 15));
        stylePanel(panel, "Gestão de Calendário - Agendar Partida");

        panel.add(createStyledLabel("Data (DD/MM):"));
        JTextField txtData = new JTextField(5);
        txtData.setFont(MAIN_FONT);
        txtData.setForeground(TEXT_DARK);
        panel.add(txtData);

        panel.add(createStyledLabel("Hora:"));
        JTextField txtHora = new JTextField(5);
        txtHora.setFont(MAIN_FONT);
        txtHora.setForeground(TEXT_DARK);
        panel.add(txtHora);

        panel.add(createStyledLabel("Estádio:"));
        String[] estadios = {"Maracanã", "Arena da Amazônia", "Itaquerão", "Arena Fonte Nova"};
        JComboBox<String> cbEstadio = new JComboBox<>(estadios);
        cbEstadio.setFont(MAIN_FONT);
        cbEstadio.setForeground(TEXT_DARK);
        cbEstadio.setBackground(PANEL_WHITE);
        panel.add(cbEstadio);

        JButton btnRegistar = new JButton("Registar Jogo");
        styleButton(btnRegistar, DARK_GREEN, Color.WHITE);
        panel.add(btnRegistar);

        return panel;
    }

    // --- 2. MÓDULO: TODAS AS FASES DE GRUPOS ---
    private JPanel createAllGroupsPanel() {
        JPanel panel = createSubtlePanel();
        panel.setLayout(new BorderLayout());
        stylePanel(panel, "Fases de Grupos - Classificações");

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(BOLD_FONT);
        tabbedPane.setForeground(DARK_GREEN);
        tabbedPane.setBackground(new Color(240, 250, 240));

        char grupoLetra = 'A';
        for (int i = 0; i < 8; i++) {
            tabbedPane.addTab(" Grupo " + (char)(grupoLetra + i) + " ", createSingleGroupTable());
        }

        panel.add(tabbedPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createSingleGroupTable() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        String[] colunas = {"Seleção", "PTS", "V", "E", "D", "GM", "GS", "DG"};
        Object[][] dados = {
                {"Brasil", 7, 2, 1, 0, 7, 2, "+5"},
                {"México", 7, 2, 1, 0, 4, 1, "+3"},
                {"Croácia", 3, 1, 0, 2, 6, 6, "0"},
                {"Camarões", 0, 0, 0, 3, 1, 9, "-8"}
        };

        DefaultTableModel model = new DefaultTableModel(dados, colunas) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        JTable table = new JTable(model);
        table.setFont(MAIN_FONT);
        table.setRowHeight(40);
        table.setFillsViewportHeight(true);
        table.setShowVerticalLines(false);
        table.setGridColor(TABLE_GRID);
        table.setForeground(TEXT_DARK);
        table.setBackground(PANEL_WHITE);
        table.setSelectionBackground(new Color(220, 245, 220)); // Seleção verde claro
        table.setSelectionForeground(DARK_GREEN);

        // Estilizar Cabeçalho com o Verde Escuro para contraste
        JTableHeader header = table.getTableHeader();
        header.setFont(BOLD_FONT);
        header.setBackground(DARK_GREEN);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 40));

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? PANEL_WHITE : TABLE_ROW_ALTERNATE);
                }
                c.setForeground(TEXT_DARK);
                setHorizontalAlignment(JLabel.CENTER);
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(PANEL_WHITE);

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    // --- 3. MÓDULO: EVENTOS E ESTATÍSTICAS (SIDEBAR) ---
    private JPanel createSidebarPanel() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 20));
        wrapper.setOpaque(false);
        wrapper.setPreferredSize(new Dimension(320, 0));

        // Topo: Ações Rápidas
        JPanel actionsPanel = createSubtlePanel();
        actionsPanel.setLayout(new GridLayout(2, 1, 0, 15));
        stylePanel(actionsPanel, "Ações Rápidas");

        JButton btnConvocatoria = new JButton("Submeter Convocatórias");
        styleButton(btnConvocatoria, DARK_GREEN, Color.WHITE);

        // Botão Destaque Amarelo
        JButton btnEventos = new JButton("Registar Eventos da Partida");
        styleButton(btnEventos, ACCENT_YELLOW, DARK_GREEN);

        actionsPanel.add(btnConvocatoria);
        actionsPanel.add(btnEventos);

        // Fundo: Live: Match Center
        JPanel statsPanel = createSubtlePanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        stylePanel(statsPanel, "Live: Match Center");

        JLabel lblMatch = new JLabel("Brasil 3 - 1 Croácia");
        lblMatch.setFont(TITLE_FONT);
        lblMatch.setForeground(DARK_GREEN);
        lblMatch.setHorizontalAlignment(SwingConstants.CENTER);
        lblMatch.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblMatch.setBorder(new EmptyBorder(0, 0, 15, 0));

        statsPanel.add(lblMatch);
        statsPanel.add(createStatLabel("Tempo: 90+2' (Fim da Partida)", "⏳", 10));
        statsPanel.add(createStatLabel("Golos: Neymar Jr. (2), Oscar (1)", "⚽", 10));
        statsPanel.add(createStatLabel("Posse de Bola: 58% - 42%", "📊", 10));
        statsPanel.add(createStatLabel("Cartões: 2 Amarelos", "🟨", 10));

        wrapper.add(actionsPanel, BorderLayout.NORTH);
        wrapper.add(statsPanel, BorderLayout.CENTER);

        return wrapper;
    }

    // --- UTILITÁRIOS DE DESIGN ---
    private void stylePanel(JPanel panel, String title) {
        TitledBorder border = BorderFactory.createTitledBorder(
                new LineBorder(DARK_GREEN, 1, true),
                title
        );
        border.setTitleFont(TITLE_FONT);
        border.setTitleColor(DARK_GREEN);

        panel.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(0, 0, 0, 0),
                border
        ));
    }

    private void styleButton(JButton button, Color bgColor, Color fgColor) {
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setFont(BOLD_FONT);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(bgColor.darker(), 1, true),
                new EmptyBorder(12, 20, 12, 20)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(BOLD_FONT);
        label.setForeground(DARK_GREEN);
        return label;
    }

    private JLabel createStatLabel(String text, String icon, int paddingBottom) {
        JLabel label = new JLabel(icon + "  " + text);
        label.setFont(MAIN_FONT);
        label.setForeground(TEXT_DARK);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setBorder(new EmptyBorder(0, 10, paddingBottom, 0));
        return label;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            new MenuMatchCenter().setVisible(true);
        });
    }
}