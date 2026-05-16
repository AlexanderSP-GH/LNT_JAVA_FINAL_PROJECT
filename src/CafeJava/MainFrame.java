package CafeJava;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

class MainFrame extends JFrame {

    private final Controller ctrl = new Controller();
    private final Color BG       = new Color(0xf8fafc);
    private final Color AMBER    = new Color(0xd97706);
    private final Color SLATE100 = new Color(0xf1f5f9);
    private final Color SLATE200 = new Color(0xe2e8f0);
    private final Color SLATE600 = new Color(0x475569);
    private final Color AMBER700 = new Color(0xb45309);
    private final Color BLUE700  = new Color(0x1e40af);
    private final Color GREEN700 = new Color(0x166534);
    private final Color RED500   = new Color(0xef4444);
    private final Color AMBER50  = new Color(0xfffbeb);
    private final Color BLUE50   = new Color(0xeff6ff);
    private final Color GREEN50  = new Color(0xf0fdf4);
    private final Color PURPLE50 = new Color(0xfaf5ff);
    private final Color RED50    = new Color(0xfef2f2);
    private final Color SEL_BG   = new Color(0xfef3c7);

    // Dashboard
    private JLabel dbMenu, dbTrans, dbRev, dbStock, dbDate;
    private JTextArea dbRecent;

    // Menu
    private DefaultTableModel menuModel;
    private JTable menuTable;
    private JTextField menuSearch, menuName, menuPrice, menuStock;
    private JComboBox<String> menuCat;
    private int editingId = -1;

    // Kasir
    private JPanel menuGridPanel;
    private JTextField cashSearch;
    private JComboBox<String> cashFilter;
    private JPanel cartListPanel;
    private JScrollPane cartScroll;
    private JLabel cartTotal, cartItems, cartChange;
    private JTextField cashPay;
    private JComboBox<String> cashMethod;
    private final List<Controller.CartEntry> cart = new ArrayList<>();

    // Laporan
    private JComboBox<String> rptDate;
    private JLabel rptTrans, rptRev, rptAvg;
    private DefaultTableModel rptModel;
    private JTable rptTable;

    public MainFrame() {
        super("Cafe Java");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1150, 700);
        setMinimumSize(new Dimension(920, 580));
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane(JTabbedPane.LEFT);
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabs.setBackground(SLATE100);

        tabs.addTab("Dashboard",   buildDashboard());
        tabs.addTab("Kelola Menu", buildMenuPanel());
        tabs.addTab("Kasir",       buildCashierPanel());
        tabs.addTab("Laporan",     buildReportPanel());

        setContentPane(tabs);
        tabs.addChangeListener(e -> {
            switch (tabs.getSelectedIndex()) {
                case 0: refreshDashboard(); break;
                case 1: refreshMenuTable(); break;
                case 3: refreshReport();   break;
            }
        });
        refreshDashboard();
    }

    // Dashboard
    private JPanel buildDashboard() {
        JPanel p = new JPanel(new BorderLayout(12, 12));
        p.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        p.setBackground(BG);

        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setOpaque(false);
        hdr.add(lbl("Dashboard", Font.BOLD, 24), BorderLayout.WEST);
        dbDate = lbl("", Font.PLAIN, 14);
        dbDate.setForeground(new Color(0x64748b));
        hdr.add(dbDate, BorderLayout.EAST);
        p.add(hdr, BorderLayout.NORTH);

        JPanel stats = new JPanel(new GridLayout(1, 4, 12, 0));
        stats.setOpaque(false);
        dbMenu  = addStat(stats, "Total Menu",         BLUE50,  BLUE700);
        dbTrans = addStat(stats, "Transaksi Hari Ini", GREEN50, GREEN700);
        dbRev   = addStat(stats, "Pendapatan Hari Ini",AMBER50, AMBER700);
        dbStock = addStat(stats, "Nilai Stok",         PURPLE50,new Color(0x6b21a8));
        p.add(stats, BorderLayout.CENTER);

        JPanel recent = new JPanel(new BorderLayout());
        recent.setBackground(Color.WHITE);
        recent.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(SLATE200),
            "Transaksi Terbaru", 0, 0, new Font("Segoe UI", Font.BOLD, 13)));
        dbRecent = new JTextArea();
        dbRecent.setEditable(false);
        dbRecent.setFont(new Font("Monospaced", Font.PLAIN, 12));
        dbRecent.setBackground(Color.WHITE);
        recent.add(new JScrollPane(dbRecent), BorderLayout.CENTER);
        p.add(recent, BorderLayout.SOUTH);
        return p;
    }

    private JLabel addStat(JPanel parent, String title, Color bg, Color fg) {
        JPanel card = new JPanel(new BorderLayout(4, 4));
        card.setBackground(bg);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(SLATE200),
            BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        JLabel val = lbl("...", Font.BOLD, 24);
        val.setForeground(fg);
        card.add(val, BorderLayout.CENTER);
        JLabel lab = lbl(title, Font.PLAIN, 12);
        lab.setForeground(new Color(0x64748b));
        card.add(lab, BorderLayout.SOUTH);
        parent.add(card);
        return val;
    }

    void refreshDashboard() {
        dbDate.setText(LocalDate.now().format(
            DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy",
            new java.util.Locale("id", "ID"))));

        List<MenuItem> menus = ctrl.getAllMenu();
        long mkn = menus.stream().filter(m -> m.getCategory().equals("makanan")).count();
        long mnm = menus.stream().filter(m -> m.getCategory().equals("minuman")).count();
        dbMenu.setText(String.format("%d Menu (%d Makanan, %d Minuman)", menus.size(), mkn, mnm));

        String today = LocalDate.now().toString();
        dbTrans.setText(String.valueOf(ctrl.countOrders(today)));
        dbRev.setText("Rp " + Controller.formatRupiah(ctrl.sumRevenue(today)));
        double sv = menus.stream().mapToDouble(m -> m.getPrice() * m.getStock()).sum();
        int ts = menus.stream().mapToInt(MenuItem::getStock).sum();
        dbStock.setText("Rp " + Controller.formatRupiah(sv) + "  (" + ts + " item)");

        List<Order> orders = ctrl.getOrdersByDate(today);
        StringBuilder sb = new StringBuilder();
        int max = Math.min(orders.size(), 8);
        for (int i = 0; i < max; i++) {
            Order o = orders.get(i);
            sb.append(String.format("%-30s Rp %10s  [%s]\n",
                o.getId(), Controller.formatRupiah(o.getTotal()),
                o.getPaymentMethod().toUpperCase()));
        }
        if (orders.isEmpty()) sb.append("(belum ada transaksi hari ini)");
        dbRecent.setText(sb.toString());
    }

    // Menu
    private JPanel buildMenuPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(BorderFactory.createEmptyBorder(20, 24, 16, 24));
        p.setBackground(BG);

        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setOpaque(false);
        hdr.add(lbl("Kelola Menu", Font.BOLD, 24), BorderLayout.WEST);
        JButton addBtn = btnPrimary("+ Tambah Menu");
        addBtn.addActionListener(e -> { clearMenuForm(); menuName.requestFocus(); });
        hdr.add(addBtn, BorderLayout.EAST);
        p.add(hdr, BorderLayout.NORTH);

        JPanel searchRow = new JPanel(new BorderLayout(8, 0));
        searchRow.setOpaque(false);
        searchRow.add(lbl("Cari:", Font.PLAIN, 13), BorderLayout.WEST);
        menuSearch = new JTextField();
        menuSearch.setFont(font(13));
        menuSearch.setBorder(insetBorder(6, 10));
        menuSearch.addActionListener(e -> refreshMenuTable());
        searchRow.add(menuSearch, BorderLayout.CENTER);
        JButton searchBtn = btnDefault("Cari");
        searchBtn.addActionListener(e -> refreshMenuTable());
        searchRow.add(searchBtn, BorderLayout.EAST);
        JPanel topBar = new JPanel(new BorderLayout(0, 6));
        topBar.setOpaque(false);
        topBar.add(searchRow, BorderLayout.NORTH);

        menuModel = new DefaultTableModel(
            new String[]{"ID", "Nama Menu", "Kategori", "Harga", "Stok"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        menuTable = new JTable(menuModel);
        menuTable.setRowHeight(34);
        menuTable.setFont(font(13));
        menuTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        menuTable.getTableHeader().setBackground(SLATE100);
        menuTable.setSelectionBackground(SEL_BG);
        menuTable.setSelectionForeground(Color.BLACK);
        menuTable.setGridColor(SLATE200);
        JScrollPane sp = new JScrollPane(menuTable);
        sp.setBorder(BorderFactory.createLineBorder(SLATE200));
        sp.setPreferredSize(new Dimension(0, 260));
        topBar.add(sp, BorderLayout.CENTER);

        p.add(topBar, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(8, 8));
        bottom.setOpaque(false);

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(SLATE200),
            BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        form.add(lbl("Nama:", Font.PLAIN, 13));
        menuName = new JTextField(14); menuName.setFont(font(13)); form.add(menuName);
        form.add(lbl("Kategori:", Font.PLAIN, 13));
        menuCat = new JComboBox<>(new String[]{"Makanan", "Minuman"});
        menuCat.setFont(font(13)); form.add(menuCat);
        form.add(lbl("Harga:", Font.PLAIN, 13));
        menuPrice = new JTextField(8); menuPrice.setFont(font(13)); form.add(menuPrice);
        form.add(lbl("Stok:", Font.PLAIN, 13));
        menuStock = new JTextField(5); menuStock.setFont(font(13)); form.add(menuStock);
        form.add(btnPrimary("Simpan"));
        ((JButton)form.getComponent(form.getComponentCount()-1)).addActionListener(e -> saveMenu());
        form.add(btnDefault("Batal"));
        ((JButton)form.getComponent(form.getComponentCount()-1)).addActionListener(e -> clearMenuForm());
        bottom.add(form, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);
        JButton edBtn = btnDefault("Edit Terpilih");
        edBtn.addActionListener(e -> editSelectedMenu()); actions.add(edBtn);
        JButton delBtn = btnDanger("Hapus Terpilih");
        delBtn.addActionListener(e -> deleteSelectedMenu()); actions.add(delBtn);
        bottom.add(actions, BorderLayout.SOUTH);

        p.add(bottom, BorderLayout.SOUTH);
        refreshMenuTable();
        return p;
    }

    void refreshMenuTable() {
        menuModel.setRowCount(0);
        for (MenuItem m : ctrl.searchMenu(menuSearch.getText().trim())) {
            menuModel.addRow(new Object[]{m.getId(), m.getName(), m.getCategory(),
                "Rp " + Controller.formatRupiah(m.getPrice()), m.getStock()});
        }
    }

    private void saveMenu() {
        String name = menuName.getText().trim();
        String cat  = ((String) menuCat.getSelectedItem()).toLowerCase();
        String pr   = menuPrice.getText().trim();
        String st   = menuStock.getText().trim();
        List<String> errs = ctrl.validateMenu(name, cat, pr, st);
        if (!errs.isEmpty()) {
            JOptionPane.showMessageDialog(this, String.join("\n", errs), "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        double price = Double.parseDouble(pr);
        int stock = Integer.parseInt(st);
        if (editingId > 0) ctrl.updateMenu(editingId, name, cat, price, stock);
        else ctrl.addMenu(name, cat, price, stock);
        clearMenuForm();
        refreshMenuTable();
    }

    private void editSelectedMenu() {
        int row = menuTable.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Pilih menu dulu!"); return; }
        int id = (int) menuModel.getValueAt(row, 0);
        MenuItem m = Database.get().menuFindById(id);
        if (m != null) {
            editingId = m.getId();
            menuName.setText(m.getName());
            menuCat.setSelectedItem(m.getCategory().equals("makanan") ? "Makanan" : "Minuman");
            menuPrice.setText(String.valueOf((int) m.getPrice()));
            menuStock.setText(String.valueOf(m.getStock()));
        }
    }

    private void deleteSelectedMenu() {
        int row = menuTable.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Pilih menu dulu!"); return; }
        int id = (int) menuModel.getValueAt(row, 0);
        String name = (String) menuModel.getValueAt(row, 1);
        int c = JOptionPane.showConfirmDialog(this,
            "Yakin hapus \"" + name + "\"?\n\n" +
            "Menu yang sudah pernah dipesan akan tetap muncul\n" +
            "di laporan dengan keterangan \"(dihapus)\".",
            "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (c == JOptionPane.YES_OPTION) {
            boolean ok = ctrl.deleteMenu(id);
            if (ok) {
                clearMenuForm();
                refreshMenuTable();
                JOptionPane.showMessageDialog(this, "Menu berhasil dihapus.", "Info", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Gagal menghapus menu.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearMenuForm() {
        menuName.setText(""); menuCat.setSelectedIndex(0);
        menuPrice.setText(""); menuStock.setText("");
        editingId = -1;
    }

    // Kasir
    private JPanel buildCashierPanel() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBorder(BorderFactory.createEmptyBorder(20, 24, 16, 24));
        p.setBackground(BG);
        p.add(lbl("Kasir", Font.BOLD, 24), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setResizeWeight(0.60);
        split.setDividerLocation(620);
        split.setLeftComponent(buildCashierLeft());
        split.setRightComponent(buildCashierRight());
        p.add(split, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildCashierLeft() {
        JPanel wrap = new JPanel(new BorderLayout(6, 6));
        wrap.setBackground(Color.WHITE);
        wrap.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.setOpaque(false);
        row.add(lbl("Cari:", Font.PLAIN, 13));
        cashSearch = new JTextField(12); cashSearch.setFont(font(13));
        cashSearch.addActionListener(e -> refreshMenuGrid()); row.add(cashSearch);
        cashFilter = new JComboBox<>(new String[]{"Semua", "Makanan", "Minuman"});
        cashFilter.setFont(font(13));
        cashFilter.addActionListener(e -> refreshMenuGrid()); row.add(cashFilter);
        JButton srchBtn = btnDefault("Cari");
        srchBtn.addActionListener(e -> refreshMenuGrid()); row.add(srchBtn);
        JButton refBtn = btnDefault("Refresh");
        refBtn.addActionListener(e -> refreshMenuGrid()); row.add(refBtn);
        wrap.add(row, BorderLayout.NORTH);

        menuGridPanel = new JPanel();
        menuGridPanel.setLayout(new GridLayout(0, 2, 10, 10));
        menuGridPanel.setOpaque(false);

        JPanel gridWrapper = new JPanel(new BorderLayout());
        gridWrapper.setOpaque(false);
        gridWrapper.add(menuGridPanel, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(gridWrapper);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        wrap.add(scroll, BorderLayout.CENTER);

        refreshMenuGrid();
        return wrap;
    }

    void refreshMenuGrid() {
        menuGridPanel.removeAll();
        List<MenuItem> items = ctrl.searchMenu(cashSearch.getText().trim());
        String f = (String) cashFilter.getSelectedItem();
        if ("Makanan".equals(f)) items = items.stream().filter(m -> m.getCategory().equals("makanan")).toList();
        if ("Minuman".equals(f)) items = items.stream().filter(m -> m.getCategory().equals("minuman")).toList();

        for (MenuItem item : items) {
            JPanel card = new JPanel(new BorderLayout(6, 4));
            card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SLATE200),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
            card.setBackground(Color.WHITE);
            card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            card.setPreferredSize(new Dimension(200, 80));
            card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

            boolean isFood = item.getCategory().equals("makanan");
            JLabel badge = lbl(isFood ? "Makanan" : "Minuman", Font.BOLD, 10);
            badge.setForeground(isFood ? new Color(0xea580c) : new Color(0x2563eb));
            card.add(badge, BorderLayout.NORTH);

            JPanel info = new JPanel(new GridLayout(2, 1, 0, 2));
            info.setOpaque(false);
            info.add(lbl(item.getName(), Font.BOLD, 13));
            JLabel priceLbl = lbl("Rp " + Controller.formatRupiah(item.getPrice()), Font.BOLD, 15);
            priceLbl.setForeground(AMBER700);
            info.add(priceLbl);
            card.add(info, BorderLayout.CENTER);

            JPanel right = new JPanel(new BorderLayout(0, 4));
            right.setOpaque(false);
            JLabel stLbl = lbl("Stok: " + item.getStock(), Font.PLAIN, 11);
            stLbl.setForeground(item.getStock() <= 5 ? RED500 : new Color(0x94a3b8));
            stLbl.setHorizontalAlignment(SwingConstants.RIGHT);
            right.add(stLbl, BorderLayout.NORTH);
            JLabel addLbl = lbl("[ + ]", Font.BOLD, 16);
            addLbl.setForeground(AMBER);
            addLbl.setHorizontalAlignment(SwingConstants.RIGHT);
            right.add(addLbl, BorderLayout.SOUTH);
            card.add(right, BorderLayout.EAST);

            if (item.getStock() > 0) {
                card.addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent e)  { addToCart(item); }
                    public void mouseEntered(MouseEvent e)  { card.setBackground(AMBER50); }
                    public void mouseExited(MouseEvent e)   { card.setBackground(Color.WHITE); }
                });
            } else {
                card.setBackground(SLATE100);
            }
            menuGridPanel.add(card);
        }
        menuGridPanel.revalidate();
        menuGridPanel.repaint();
    }

    // Keranjang
    private JPanel buildCashierRight() {
        JPanel outer = new JPanel(new BorderLayout(6, 6));
        outer.setBackground(Color.WHITE);
        outer.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setOpaque(false);
        hdr.add(lbl("Keranjang", Font.BOLD, 16), BorderLayout.WEST);
        cartItems = lbl("0 item", Font.PLAIN, 12);
        cartItems.setForeground(new Color(0x64748b));
        hdr.add(cartItems, BorderLayout.EAST);
        outer.add(hdr, BorderLayout.NORTH);

        cartListPanel = new JPanel();
        cartListPanel.setLayout(new BoxLayout(cartListPanel, BoxLayout.Y_AXIS));
        cartListPanel.setOpaque(false);
        cartScroll = new JScrollPane(cartListPanel);
        cartScroll.setBorder(null);
        cartScroll.setPreferredSize(new Dimension(0, 220));
        outer.add(cartScroll, BorderLayout.CENTER);

        JButton clrBtn = btnDanger("Kosongkan Keranjang");
        clrBtn.addActionListener(e -> { cart.clear(); refreshCart(); });
        JPanel clrPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        clrPanel.setOpaque(false);
        clrPanel.add(clrBtn);
        outer.add(clrPanel, BorderLayout.SOUTH);

        JPanel bottom = new JPanel(new GridBagLayout());
        bottom.setOpaque(false);
        bottom.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, SLATE200));
        bottom.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(4, 0, 4, 0);

        g.gridx = 0; g.gridy = 0; bottom.add(lbl("Total:", Font.PLAIN, 13), g);
        g.gridx = 1; cartTotal = lbl("Rp 0", Font.BOLD, 22);
        cartTotal.setForeground(AMBER700); bottom.add(cartTotal, g);

        g.gridx = 0; g.gridy = 1; bottom.add(lbl("Metode:", Font.PLAIN, 13), g);
        g.gridx = 1; cashMethod = new JComboBox<>(new String[]{"Cash", "QRIS", "Debit"});
        cashMethod.setFont(font(13));
        cashMethod.addActionListener(e -> {
            boolean isCash = "Cash".equals(cashMethod.getSelectedItem());
            cashPay.setEnabled(isCash);
            if (!isCash) { cashPay.setText(""); cartChange.setText(""); }
        });
        bottom.add(cashMethod, g);

        g.gridx = 0; g.gridy = 2; bottom.add(lbl("Bayar:", Font.PLAIN, 13), g);
        g.gridx = 1;
        JPanel payRow = new JPanel(new BorderLayout(4, 0)); payRow.setOpaque(false);
        cashPay = new JTextField(10); cashPay.setFont(font(13));
        payRow.add(cashPay, BorderLayout.CENTER);
        cartChange = lbl("", Font.BOLD, 12);
        cartChange.setForeground(new Color(0x16a34a));
        payRow.add(cartChange, BorderLayout.EAST);
        bottom.add(payRow, g);

        g.gridx = 0; g.gridy = 3; g.gridwidth = 2;
        JButton chkBtn = btnPrimary("Bayar Sekarang");
        chkBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        chkBtn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        chkBtn.addActionListener(e -> checkout());
        bottom.add(chkBtn, g);

        cashPay.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateChange(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { updateChange(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { updateChange(); }
        });

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(outer, BorderLayout.CENTER);
        wrap.add(bottom, BorderLayout.SOUTH);
        return wrap;
    }

    private void addToCart(MenuItem item) {
        int existing = 0;
        for (Controller.CartEntry ce : cart)
            if (ce.item.getId() == item.getId()) existing = ce.qty;
        if (existing >= item.getStock()) {
            JOptionPane.showMessageDialog(this, "Stok tidak cukup! Tersedia: " + item.getStock());
            return;
        }
        boolean found = false;
        for (Controller.CartEntry ce : cart)
            if (ce.item.getId() == item.getId()) { ce.qty++; found = true; break; }
        if (!found) cart.add(new Controller.CartEntry(item, 1));
        refreshCart();
    }

    private void cartPlus(Controller.CartEntry ce) {
        if (ce.qty >= ce.item.getStock()) {
            JOptionPane.showMessageDialog(this, "Stok tidak cukup!");
            return;
        }
        ce.qty++;
        refreshCart();
    }

    private void cartMinus(Controller.CartEntry ce) {
        if (ce.qty <= 1) {
            cart.remove(ce);
        } else {
            ce.qty--;
        }
        refreshCart();
    }

    void refreshCart() {
        cartListPanel.removeAll();
        double total = 0; int tq = 0;

        for (Controller.CartEntry ce : cart) {
            double sub = ce.item.getPrice() * ce.qty;
            total += sub; tq += ce.qty;

            JPanel row = new JPanel(new BorderLayout(8, 0));
            row.setOpaque(true);
            row.setBackground(Color.WHITE);
            row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, SLATE200),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

            JLabel nameLbl = lbl(ce.item.getName(), Font.PLAIN, 12);
            nameLbl.setPreferredSize(new Dimension(100, 20));
            row.add(nameLbl, BorderLayout.WEST);

            JPanel ctrl = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
            ctrl.setOpaque(false);

            JButton minus = new JButton("-");
            minus.setFont(new Font("Segoe UI", Font.BOLD, 10));
            minus.setPreferredSize(new Dimension(32, 24));
            minus.setMargin(new Insets(0, 0, 0, 0));
            minus.setFocusPainted(false);
            minus.addActionListener(e -> cartMinus(ce));
            ctrl.add(minus);

            JLabel qtyLbl = lbl(String.valueOf(ce.qty), Font.BOLD, 13);
            qtyLbl.setPreferredSize(new Dimension(24, 20));
            qtyLbl.setHorizontalAlignment(SwingConstants.CENTER);
            ctrl.add(qtyLbl);

            JButton plus = new JButton("+");
            plus.setFont(new Font("Segoe UI", Font.BOLD, 10));
            plus.setPreferredSize(new Dimension(32, 24));
            plus.setMargin(new Insets(0, 0, 0, 0));
            plus.setFocusPainted(false);
            plus.addActionListener(e -> cartPlus(ce));
            ctrl.add(plus);
            row.add(ctrl, BorderLayout.CENTER);
            JLabel subLbl = lbl("Rp " + Controller.formatRupiah(sub), Font.BOLD, 12);
            subLbl.setForeground(AMBER700);
            row.add(subLbl, BorderLayout.EAST);

            cartListPanel.add(row);
        }

        if (cart.isEmpty()) {
            JLabel emptyLbl = lbl("(keranjang kosong)", Font.ITALIC, 13);
            emptyLbl.setForeground(new Color(0x94a3b8));
            emptyLbl.setHorizontalAlignment(SwingConstants.CENTER);
            emptyLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
            cartListPanel.add(emptyLbl);
        }

        cartTotal.setText("Rp " + Controller.formatRupiah(total));
        cartItems.setText(tq + " item");
        cartListPanel.revalidate();
        cartListPanel.repaint();
        updateChange();
    }

    private void updateChange() {
        double total = cart.stream().mapToDouble(ce -> ce.item.getPrice() * ce.qty).sum();
        String ps = cashPay.getText().trim();
        if (!ps.isEmpty()) {
            try {
                double pay = Double.parseDouble(ps);
                if (pay >= total)
                    cartChange.setText("Kembali: Rp " + Controller.formatRupiah(pay - total));
                else
                    cartChange.setText("Kurang Rp " + Controller.formatRupiah(total - pay));
            } catch (NumberFormatException e) {
                cartChange.setText("Invalid");
            }
        } else {
            cartChange.setText("");
        }
    }

    private void checkout() {
        if (cart.isEmpty()) { JOptionPane.showMessageDialog(this, "Keranjang kosong!"); return; }
        String methodName = (String) cashMethod.getSelectedItem();
        String method = methodName.toLowerCase(); // cash/qris/debit
        double total = cart.stream().mapToDouble(ce -> ce.item.getPrice() * ce.qty).sum();
        double payAmount = total;

        if ("cash".equals(method)) {
            String ps = cashPay.getText().trim();
            if (ps.isEmpty()) { JOptionPane.showMessageDialog(this, "Masukkan jumlah pembayaran!"); return; }
            try { payAmount = Double.parseDouble(ps); }
            catch (NumberFormatException e) { JOptionPane.showMessageDialog(this, "Nominal tidak valid!"); return; }
            if (payAmount < total) {
                JOptionPane.showMessageDialog(this, "Pembayaran kurang!\nTotal: Rp " + Controller.formatRupiah(total));
                return;
            }
        }

        Order order = ctrl.checkout(cart, method, payAmount);

        StringBuilder sb = new StringBuilder();
        sb.append("================================\n");
        sb.append("          CAFE JAVA\n");
        sb.append("================================\n");
        sb.append("ID    : ").append(order.getId()).append("\n");
        sb.append("Tgl   : ").append(order.getOrderDate()).append("\n");
        sb.append("Metode: ").append(methodName.toUpperCase()).append("\n\n");
        for (Controller.CartEntry ce : cart)
            sb.append(String.format("%-20s x%-2d  Rp%8s\n",
                ce.item.getName(), ce.qty,
                Controller.formatRupiah(ce.item.getPrice() * ce.qty)));
        sb.append("\n--------------------------------\n");
        sb.append(String.format("TOTAL     Rp %15s\n", Controller.formatRupiah(total)));
        sb.append(String.format("BAYAR     Rp %15s\n", Controller.formatRupiah(payAmount)));
        if (order.getChangeAmount() > 0)
            sb.append(String.format("KEMBALI   Rp %15s\n", Controller.formatRupiah(order.getChangeAmount())));
        sb.append("================================\n");
        sb.append("       Terima kasih!\n");
        sb.append("================================\n");

        JTextArea ta = new JTextArea(sb.toString());
        ta.setFont(new Font("Monospaced", Font.PLAIN, 13));
        ta.setEditable(false);
        ta.setBackground(Color.WHITE);
        JScrollPane sc = new JScrollPane(ta);
        sc.setPreferredSize(new Dimension(390, 400));
        JOptionPane.showMessageDialog(this, sc, "Struk Pembayaran", JOptionPane.INFORMATION_MESSAGE);

        cart.clear(); refreshCart();
        cashPay.setText(""); cartChange.setText("");
        refreshMenuGrid();
    }

    // Laporan
    private JPanel buildReportPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(BorderFactory.createEmptyBorder(20, 24, 16, 24));
        p.setBackground(BG);

        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setOpaque(false);
        hdr.add(lbl("Laporan Harian", Font.BOLD, 24), BorderLayout.WEST);

        JPanel dp = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        dp.setOpaque(false);
        dp.add(lbl("Tanggal:", Font.PLAIN, 13));
        rptDate = new JComboBox<>();
        rptDate.setFont(font(13));
        rptDate.addActionListener(e -> refreshReport());
        dp.add(rptDate);
        JButton refBtn = btnDefault("Refresh");
        refBtn.addActionListener(e -> refreshReport());
        dp.add(refBtn);
        hdr.add(dp, BorderLayout.EAST);
        p.add(hdr, BorderLayout.NORTH);

        JPanel stats = new JPanel(new GridLayout(1, 3, 12, 0));
        stats.setOpaque(false);
        rptTrans = addStat(stats, "Total Transaksi",       BLUE50,  new Color(0x0369a1));
        rptRev   = addStat(stats, "Total Pendapatan",      AMBER50, AMBER700);
        rptAvg   = addStat(stats, "Rata-rata / Transaksi", GREEN50, GREEN700);
        p.add(stats, BorderLayout.CENTER);

        rptModel = new DefaultTableModel(
            new String[]{"Order ID", "Waktu", "Item (Qty)", "Total", "Metode"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        rptTable = new JTable(rptModel);
        rptTable.setRowHeight(34);
        rptTable.setFont(font(13));
        rptTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        rptTable.getTableHeader().setBackground(SLATE100);
        rptTable.setSelectionBackground(SEL_BG);
        rptTable.setSelectionForeground(Color.BLACK);
        rptTable.setGridColor(SLATE200);
        JScrollPane sp = new JScrollPane(rptTable);
        sp.setPreferredSize(new Dimension(0, 260));
        sp.setBorder(BorderFactory.createLineBorder(SLATE200));
        p.add(sp, BorderLayout.SOUTH);

        refreshReport();
        return p;
    }

    void refreshReport() {
        rptDate.removeAllItems();
        List<String> dates = ctrl.getOrderDates();
        String today = LocalDate.now().toString();
        if (!dates.contains(today))
            rptDate.addItem(today + " (belum ada transaksi)");
        for (String d : dates)
            rptDate.addItem(d.equals(today) ? d + " (hari ini)" : d);
        if (rptDate.getItemCount() == 0)
            rptDate.addItem(today + " (belum ada transaksi)");

        String sel = (String) rptDate.getSelectedItem();
        if (sel == null) return;
        String date = sel.split(" ")[0];

        int cnt = ctrl.countOrders(date);
        double rev = ctrl.sumRevenue(date);
        rptTrans.setText(String.valueOf(cnt));
        rptRev.setText("Rp " + Controller.formatRupiah(rev));
        rptAvg.setText("Rp " + Controller.formatRupiah(cnt > 0 ? rev / cnt : 0));

        rptModel.setRowCount(0);
        for (Order o : ctrl.getOrdersByDate(date)) {
            List<OrderDetail> details = ctrl.getOrderDetails(o.getId());
            String time = o.getCreatedAt() != null && o.getCreatedAt().length() >= 16
                ? o.getCreatedAt().substring(11, 19) : "-";
            StringBuilder items = new StringBuilder();
            for (OrderDetail d : details)
                items.append(d.getMenuName()).append(" x").append(d.getQuantity()).append(", ");
            if (items.length() > 2) items.setLength(items.length() - 2);
            rptModel.addRow(new Object[]{o.getId(), time, items.toString(),
                "Rp " + Controller.formatRupiah(o.getTotal()),
                o.getPaymentMethod().toUpperCase()});
        }
        if (cnt > 0)
            rptModel.addRow(new Object[]{"", "", "TOTAL PENDAPATAN",
                "Rp " + Controller.formatRupiah(rev), ""});
    }

    private Font font(int size) {
        return new Font("Segoe UI", Font.PLAIN, size);
    }

    private JLabel lbl(String text, int style, int size) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", style, size));
        return l;
    }

    private javax.swing.border.Border insetBorder(int top, int horiz) {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xcbd5e1)),
            BorderFactory.createEmptyBorder(top, horiz, top, horiz));
    }

    private JButton btnPrimary(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setBackground(AMBER);
        b.setForeground(Color.WHITE);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(7, 14, 7, 14));
        return b;
    }

    private JButton btnDefault(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        b.setBackground(SLATE100);
        b.setForeground(SLATE600);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(7, 14, 7, 14));
        return b;
    }

    private JButton btnDanger(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        b.setBackground(RED50);
        b.setForeground(new Color(0xdc2626));
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(7, 14, 7, 14));
        return b;
    }
}
