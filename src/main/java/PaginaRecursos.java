import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class PaginaRecursos extends JFrame {

    public PaginaRecursos() {
        setTitle("Mundial 2026 - Gestão de Recursos");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1300, 850);

        JPanel contentorPrincipal = new JPanel(new BorderLayout(0, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(0, 0, new Color(230, 240, 20), getWidth(), getHeight(), new Color(10, 140, 50));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(20, 30, 10, 30));

        JLabel titulo = new JLabel("RECURSOS E ESPAÇOS", SwingConstants.LEFT);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 36));
        titulo.setForeground(new Color(0, 74, 35));
        header.add(titulo, BorderLayout.WEST);

        // Forçar a transparência do JTabbedPane
        UIManager.put("TabbedPane.contentOpaque", false);
        JTabbedPane abas = new JTabbedPane();
        abas.setOpaque(false);
        abas.setBackground(new Color(255, 255, 255, 120)); // Abas translúcidas
        abas.setFont(new Font("SansSerif", Font.BOLD, 18));
        abas.setBorder(new EmptyBorder(10, 20, 20, 20));

        abas.addTab("LOGÍSTICA DA SELEÇÃO", criarPainelSelecao());
        abas.addTab("INFRAESTRUTURAS GERAIS", criarPainelEstadiosGeral());

        contentorPrincipal.add(header, BorderLayout.NORTH);
        contentorPrincipal.add(abas, BorderLayout.CENTER);

        setContentPane(contentorPrincipal);
        setLocationRelativeTo(null);
        abas.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
            @Override
            protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
            }

            @Override
            protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
            }

            @Override
            protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (isSelected) {
                    g2.setColor(new Color(0, 100, 40));
                } else {
                    g2.setColor(new Color(0, 120, 50));
                }

                g2.fillRoundRect(x, y + 2, w, h - 2, 10, 10);
                g2.dispose();
            }

            @Override
            protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics, int tabIndex, String title, Rectangle textRect, boolean isSelected) {
                g.setColor(Color.WHITE);
                super.paintText(g, tabPlacement, font, metrics, tabIndex, title, textRect, isSelected);
            }

            @Override
            protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect, boolean isSelected) {
            }

            @Override
            protected Insets getTabInsets(int tabPlacement, int tabIndex) {
                return new Insets(4, 15, 4, 15);
            }
        });
    }

    private JPanel criarPainelSelecao() {
        JPanel painel = new JPanel(new BorderLayout(0, 20));
        painel.setOpaque(false);

        JPanel topoSelecao = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        topoSelecao.setOpaque(false);

        JLabel lbl = new JLabel("Configurar para:");
        lbl.setFont(new Font("SansSerif", Font.BOLD, 18));
        lbl.setForeground(new Color(0, 74, 35));

        String[] equipas = {"Selecione a Seleção...", "Portugal", "Brasil", "Argentina", "Alemanha"};
        JComboBox<String> combo = new JComboBox<>(equipas);
        combo.setFont(new Font("SansSerif", Font.BOLD, 14));
        combo.setPreferredSize(new Dimension(220, 30));
        combo.setBackground(Color.WHITE);
        combo.setFocusable(false);

        topoSelecao.add(lbl);
        topoSelecao.add(combo);

        JPanel grid = new JPanel(new GridLayout(1, 3, 20, 0));
        grid.setOpaque(false);
        grid.add(criarPainelHotel());
        grid.add(criarPainelCentroTreino());
        grid.add(criarPainelTransportes());

        painel.add(topoSelecao, BorderLayout.NORTH);
        painel.add(grid, BorderLayout.CENTER);

        return painel;
    }


    private JPanel criarPainelEstadiosGeral() {
        JPanel painel = new JPanel(new BorderLayout());
        painel.setOpaque(false);
        painel.setBorder(new EmptyBorder(20, 0, 0, 0));

        JPanel cardLargo = new JPanel(new GridLayout(1, 2, 30, 0));
        cardLargo.setOpaque(false);

        cardLargo.add(criarPainelEstadioLotacao());
        cardLargo.add(criarPainelEstadioRelvado());

        painel.add(cardLargo, BorderLayout.CENTER);
        return painel;
    }

    private JPanel criarPainelHotel() {
        JPanel card = criarCardBase("ALOJAMENTO" , null );
        card.add(criarLabelCampo("Atribuir Hotel Parceiro:"));
        card.add(criarDropdown(new String[]{"Copacabana Palace", "Fasano", "Pestana Rio", "Hilton Barra"}));
        card.add(Box.createVerticalStrut(15));
        card.add(criarLabelCampo("Datas Check-in / Check-out:"));
        card.add(criarCampoTexto("DD/MM/AAAA - DD/MM/AAAA"));
        card.add(Box.createVerticalGlue());
        card.add(criarBotaoAcao("CONFIRMAR ESTADIA"));
        return card;
    }

    private JPanel criarPainelCentroTreino() {
        JPanel card = criarCardBase("CENTRO DE TREINO", null );
        card.add(criarLabelCampo("Alocação Exclusiva:"));
        card.add(criarDropdown(new String[]{"CT Granja Comary", "CT Ninho do Urubu", "Cidade do Galo"}));
        card.add(Box.createVerticalStrut(15));
        card.add(criarLabelCampo("Horário de Treino Fixo:"));
        card.add(criarDropdown(new String[]{"Manhã (09:00 - 12:00)", "Tarde (15:00 - 18:00)"}));
        card.add(Box.createVerticalGlue());
        card.add(criarBotaoAcao("ALOCAR CENTRO"));
        return card;
    }

    private JPanel criarPainelTransportes() {
        JPanel card = criarCardBase("DESLOCAÇÕES", null );
        card.add(criarLabelCampo("Alocação de Veículo:"));
        card.add(criarDropdown(new String[]{"Autocarro Oficial Seleção", "Voo Charter Privado"}));
        card.add(Box.createVerticalStrut(15));
        card.add(criarLabelCampo("Rotas de Dia de Jogo:"));
        card.add(criarCampoTexto("Ex: Hotel -> Estádio"));
        card.add(Box.createVerticalStrut(15));
        card.add(criarLabelCampo("Registo de Viagem (Data/Hora):"));
        card.add(criarCampoTexto("Ex: 12/06 14:00"));
        card.add(Box.createVerticalGlue());
        card.add(criarBotaoAcao("AGENDAR VIAGEM"));
        return card;
    }

    private JPanel criarPainelEstadioLotacao() {
        JPanel card = criarCardBase("ESTÁDIOS - LOTAÇÃO", null );
        card.add(criarLabelCampo("Selecionar Estádio:"));
        card.add(criarDropdown(new String[]{"Maracanã", "Arena Corinthians", "Mineirão"}));
        card.add(Box.createVerticalStrut(15));
        card.add(criarLabelCampo("Ajustar Vagas Setor VIP:"));
        card.add(criarCampoTexto("Ex: 5000"));
        card.add(Box.createVerticalGlue());
        card.add(criarBotaoAcao("ATUALIZAR LOTAÇÃO"));
        return card;
    }

    private JPanel criarPainelEstadioRelvado() {
        JPanel card = criarCardBase("MANUTENÇÃO - RELVADO", null );
        card.add(criarLabelCampo("Selecionar Estádio:"));
        card.add(criarDropdown(new String[]{"Maracanã", "Arena Corinthians", "Mineirão"}));
        card.add(Box.createVerticalStrut(15));
        card.add(criarLabelCampo("Estado Atual do Relvado:"));
        card.add(criarDropdown(new String[]{"Excelente", "Bom", "Intervenção Urgente"}));
        card.add(Box.createVerticalGlue());
        card.add(criarBotaoAcao("REGISTAR MANUTENÇÃO"));
        return card;
    }

    private JPanel criarCardBase(String titulo, String sub) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 180));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lblT = new JLabel(titulo);
        lblT.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblT.setForeground(new Color(0, 74, 35));
        lblT.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblS = new JLabel(sub);
        lblS.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblS.setForeground(new Color(0, 50, 0));
        lblS.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(lblT);
        card.add(lblS);
        card.add(Box.createVerticalStrut(20));
        return card;
    }

    private JLabel criarLabelCampo(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 15));
        lbl.setForeground(new Color(0, 50, 0));
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        return lbl;
    }

    private JComboBox<String> criarDropdown(String[] opcoes) {
        JComboBox<String> combo = new JComboBox<>(opcoes);
        combo.setMaximumSize(new Dimension(300, 35));
        combo.setAlignmentX(Component.CENTER_ALIGNMENT);
        return combo;
    }

    private JTextField criarCampoTexto(String placeholder) {
        JTextField txt = new JTextField(placeholder);
        txt.setMaximumSize(new Dimension(300, 35));
        txt.setHorizontalAlignment(JTextField.CENTER);
        txt.setAlignmentX(Component.CENTER_ALIGNMENT);
        return txt;
    }

    private JButton criarBotaoAcao(String texto) {
        JButton btn = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Efeito visual ao passar o rato e ao clicar
                if (getModel().isPressed()) {
                    g2.setColor(new Color(0, 50, 20)); // Verde mais escuro
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(20, 100, 50)); // Verde mais claro
                } else {
                    g2.setColor(new Color(0, 74, 35)); // Verde original
                }

                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.dispose();
                super.paintComponent(g);
            }
        };

        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 15));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false); // Bloqueia o estilo padrão do Windows
        btn.setBorderPainted(false);     // Remove a borda cinzenta do Windows
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(250, 45));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> new PaginaRecursos().setVisible(true));
    }
}