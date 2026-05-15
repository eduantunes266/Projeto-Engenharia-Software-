import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
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
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titulo.setForeground(new Color(0, 70, 30));
        header.add(titulo, BorderLayout.WEST);

        UIManager.put("TabbedPane.contentOpaque", false);
        JTabbedPane abas = new JTabbedPane();
        abas.setOpaque(false);
        abas.setBackground(Color.WHITE);
        abas.setFont(new Font("Segoe UI", Font.BOLD, 12));
        abas.setBorder(new EmptyBorder(10, 20, 20, 20));

        abas.addTab("HOTÉIS", criarPainelHoteisGeral());
        abas.addTab("CENTROS DE TREINO", criarPainelCentrosGeral());
        abas.addTab("ESTÁDIOS", criarPainelEstadiosGeral());
        abas.addTab("AGENDAMENTO DE VIAGENS", criarPainelViagensGeral());

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
                    g2.setColor(new Color(45, 105, 45));
                } else {
                    g2.setColor(new Color(85, 145, 60));
                }

                g2.fillRoundRect(x, y + 2, w - 10, h - 2, 8, 8);
                g2.dispose();
            }

            @Override
            protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics, int tabIndex, String title, Rectangle textRect, boolean isSelected) {
                g.setColor(Color.WHITE);
                Rectangle textRectAjustado = new Rectangle(textRect);
                textRectAjustado.x -= 5;
                super.paintText(g, tabPlacement, font, metrics, tabIndex, title, textRectAjustado, isSelected);
            }

            @Override
            protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect, boolean isSelected) {
            }

            @Override
            protected Insets getTabInsets(int tabPlacement, int tabIndex) {
                return new Insets(8, 20, 8, 30);
            }
        });
    }

    private JPanel criarPainelHoteisGeral() {
        JPanel painel = new JPanel(new BorderLayout());
        painel.setOpaque(false);
        painel.setBorder(new EmptyBorder(20, 0, 0, 0));

        JPanel cardLargo = new JPanel(new GridLayout(1, 2, 30, 0));
        cardLargo.setOpaque(false);

        String[] colunas = {"Hotel", "Cidade", "Quartos", "Equipa", "Datas"};
        Object[][] dadosIniciais = {
                {"Copacabana Palace", "Rio de Janeiro", "250", "-", "-"},
                {"Fasano", "Rio de Janeiro", "80", "Brasil", "10/06 - 25/06"},
                {"Pestana Rio", "Rio de Janeiro", "220", "-", "-"}
        };

        cardLargo.add(criarPainelTabelaGenerica("HOTÉIS CADASTRADOS", colunas, dadosIniciais, "REMOVER HOTEL"));
        cardLargo.add(criarPainelFormularioHotel());

        painel.add(cardLargo, BorderLayout.CENTER);
        return painel;
    }

    private JPanel criarPainelCentrosGeral() {
        JPanel painel = new JPanel(new BorderLayout());
        painel.setOpaque(false);
        painel.setBorder(new EmptyBorder(20, 0, 0, 0));

        JPanel cardLargo = new JPanel(new GridLayout(1, 2, 30, 0));
        cardLargo.setOpaque(false);

        String[] colunas = {"Centro", "Localização", "Equipa Vinculada"};
        Object[][] dadosIniciais = {
                {"CT Granja Comary", "Teresópolis", "Brasil"},
                {"CT Ninho do Urubu", "Rio de Janeiro", "-"},
                {"Cidade do Galo", "Belo Horizonte", "Argentina"}
        };

        cardLargo.add(criarPainelTabelaGenerica("CENTROS DE TREINO", colunas, dadosIniciais, "REMOVER CENTRO"));
        cardLargo.add(criarPainelFormularioCentro());

        painel.add(cardLargo, BorderLayout.CENTER);
        return painel;
    }



    private JPanel criarPainelEstadiosGeral() {
        JPanel painel = new JPanel(new BorderLayout());
        painel.setOpaque(false);
        painel.setBorder(new EmptyBorder(20, 0, 0, 0));

        JPanel cardLargo = new JPanel(new GridLayout(1, 2, 30, 0));
        cardLargo.setOpaque(false);

        cardLargo.add(criarPainelEstadiosTabela());
        cardLargo.add(criarPainelEstadiosFormulario());

        painel.add(cardLargo, BorderLayout.CENTER);
        return painel;
    }

    private JPanel criarPainelViagensGeral() {
        JPanel painel = new JPanel(new BorderLayout());
        painel.setOpaque(false);
        painel.setBorder(new EmptyBorder(20, 0, 0, 0));

        JPanel topoSelecao = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        topoSelecao.setOpaque(false);
        topoSelecao.add(criarLabelCampo("Configurar Viagem para:"));
        topoSelecao.add(criarDropdown(new String[]{"Portugal", "Brasil", "Argentina", "Alemanha"}));

        JPanel centro = new JPanel(new FlowLayout(FlowLayout.CENTER));
        centro.setOpaque(false);
        centro.add(criarPainelTransportes());

        painel.add(topoSelecao, BorderLayout.NORTH);
        painel.add(centro, BorderLayout.CENTER);
        return painel;
    }

    private JPanel criarPainelTabelaGenerica(String titulo, String[] colunas, Object[][] dados, String textoBotao) {
        JPanel card = criarCardBase(titulo, null);
        DefaultTableModel modelo = new DefaultTableModel(dados, colunas);
        JTable tabela = new JTable(modelo);
        tabela.setRowHeight(25);
        JScrollPane scroll = new JScrollPane(tabela);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0, 74, 35)));
        card.add(scroll);
        card.add(Box.createVerticalStrut(15));
        card.add(criarBotaoAcao(textoBotao));
        return card;
    }

    private JPanel criarPainelFormularioHotel() {
        JPanel card = criarCardBase("GESTÃO DE HOTEL", null);
        card.add(criarLabelCampo("Nome do Hotel:"));
        card.add(criarCampoTexto(""));
        card.add(Box.createVerticalStrut(10));
        card.add(criarLabelCampo("Cidade:"));
        card.add(criarCampoTexto(""));
        card.add(Box.createVerticalStrut(10));
        card.add(criarLabelCampo("Quartos Disponíveis:"));
        card.add(criarCampoTexto(""));
        card.add(Box.createVerticalStrut(10));
        card.add(criarBotaoAcao("ADICIONAR HOTEL"));
        card.add(Box.createVerticalStrut(15));
        card.add(new JSeparator());
        card.add(Box.createVerticalStrut(10));
        card.add(criarLabelCampo("Atribuir a Equipa:"));
        card.add(criarDropdown(new String[]{"Portugal", "Brasil", "Espanha"}));
        card.add(Box.createVerticalStrut(10));
        card.add(criarLabelCampo("Datas de Estadia:"));
        card.add(criarCampoTexto("DD/MM/AAAA - DD/MM/AAAA"));
        card.add(Box.createVerticalStrut(10));
        card.add(criarBotaoAcao("VINCULAR ESTADIA"));
        card.add(Box.createVerticalGlue());
        return card;
    }

    private JPanel criarPainelFormularioCentro() {
        JPanel card = criarCardBase("GESTÃO DE CENTRO", null);
        card.add(criarLabelCampo("Nome do Centro:"));
        card.add(criarCampoTexto(""));
        card.add(Box.createVerticalStrut(10));
        card.add(criarLabelCampo("Localização:"));
        card.add(criarCampoTexto(""));
        card.add(Box.createVerticalStrut(10));
        card.add(criarBotaoAcao("ADICIONAR CENTRO"));
        card.add(Box.createVerticalStrut(25));
        card.add(new JSeparator());
        card.add(Box.createVerticalStrut(15));
        card.add(criarLabelCampo("Atribuir a Equipa:"));
        card.add(criarDropdown(new String[]{"França", "Argentina", "Alemanha"}));
        card.add(Box.createVerticalStrut(10));
        card.add(criarBotaoAcao("VINCULAR TREINO"));
        card.add(Box.createVerticalGlue());
        return card;
    }

    private JPanel criarPainelEstadiosTabela() {
        JPanel card = criarCardBase("ESTÁDIOS CADASTRADOS", null);
        String[] colunas = {"Estádio", "Capacidade Total"};
        Object[][] dados = {
                {"Maracanã", "78000"},
                {"Arena Corinthians", "49205"}
        };
        DefaultTableModel modelo = new DefaultTableModel(dados, colunas);
        JTable tabela = new JTable(modelo);
        tabela.setRowHeight(25);
        JScrollPane scroll = new JScrollPane(tabela);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0, 74, 35)));
        card.add(scroll);
        card.add(Box.createVerticalStrut(15));
        card.add(criarBotaoAcao("REMOVER ESTÁDIO"));
        return card;
    }

    private JPanel criarPainelEstadiosFormulario() {
        JPanel card = criarCardBase("ADICIONAR ESTÁDIO", null);
        card.add(criarLabelCampo("Nome do Estádio:"));
        card.add(criarCampoTexto("Ex: Santiago Bernabéu"));
        card.add(Box.createVerticalStrut(10));
        card.add(criarLabelCampo("Capacidade Bancada Central:"));
        card.add(criarCampoTexto("Ex: 30000"));
        card.add(Box.createVerticalStrut(10));
        card.add(criarLabelCampo("Capacidade Topos:"));
        card.add(criarCampoTexto("Ex: 40000"));
        card.add(Box.createVerticalStrut(10));
        card.add(criarLabelCampo("Capacidade Zona VIP:"));
        card.add(criarCampoTexto("Ex: 5000"));
        card.add(Box.createVerticalGlue());
        card.add(criarBotaoAcao("REGISTAR ESTÁDIO"));
        return card;
    }

    private JPanel criarPainelTransportes() {
        JPanel card = criarCardBase("DESLOCAÇÕES", null);
        card.setPreferredSize(new Dimension(400, 500));
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
        card.add(lblT);
        if (sub != null) {
            JLabel lblS = new JLabel(sub);
            lblS.setFont(new Font("SansSerif", Font.PLAIN, 14));
            lblS.setForeground(new Color(0, 50, 0));
            lblS.setAlignmentX(Component.CENTER_ALIGNMENT);
            card.add(lblS);
        }
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
                if (getModel().isPressed()) g2.setColor(new Color(0, 50, 20));
                else if (getModel().isRollover()) g2.setColor(new Color(20, 100, 50));
                else g2.setColor(new Color(0, 74, 35));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 15));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(250, 45));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
        SwingUtilities.invokeLater(() -> new PaginaRecursos().setVisible(true));
    }
}