package CafeJava;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

class MainFrame extends JFrame {

    private final Controller ctrl = new Controller();
    private final Color LATAR = new Color(0xf1f5f9);
    private final Color ORANYE = new Color(0xea580c);
    private final Color ABU_MUDA = new Color(0xe2e8f0);
    private final Color ABU_GARIS = new Color(0xcbd5e1);
    private final Color ABU_TEKS = new Color(0x334155);
    private final Color ORANYE_TUA = new Color(0xc2410c);
    private final Color BIRU = new Color(0x1d4ed8);
    private final Color HIJAU = new Color(0x15803d);
    private final Color MERAH = new Color(0xb91c1c);
    private final Color UNGU = new Color(0x7e22ce);
    private final Color KUNING = new Color(0xfef08a);

    private JLabel dbMenu, dbTrans, dbRev, dbStock, dbDate;
    private JTextArea dbRecent;

    private DefaultTableModel menuModel;
    private JTable menuTable;
    private JTextField menuSearch, menuName, menuPrice, menuStock;
    private JComboBox<String> menuCat;
    private int editingId = -1;

    private JPanel menuGridPanel;
    private JTextField cashSearch;
    private JComboBox<String> cashFilter;
    private JPanel cartListPanel;
    private JScrollPane cartScroll;
    private JLabel cartTotal, cartItems, cartChange;
    private JTextField cashPay;
    private JComboBox<String> cashMethod;
    private final List<Controller.CartEntry> cart = new ArrayList<>();

    private JComboBox<String> rptDate;
    private JLabel rptTrans, rptRev, rptAvg;
    private DefaultTableModel rptModel;
    private JTable rptTable;

    public MainFrame() {
        super("Cafe Java");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1240, 820);
        setMinimumSize(new Dimension(1100, 750));
        setLocationRelativeTo(null);

        UIManager.put("TabbedPane.selected", Color.WHITE);
        UIManager.put("TabbedPane.contentBorderInsets", new Insets(0, 0, 0, 0));
        UIManager.put("TabbedPane.tabInsets", new Insets(18, 24, 18, 24));
        UIManager.put("TabbedPane.unselectedTabBackground", LATAR);
        UIManager.put("TabbedPane.shadow", ABU_GARIS);
        UIManager.put("TabbedPane.darkShadow", ABU_GARIS);
        UIManager.put("TabbedPane.highlight", ABU_MUDA);
        UIManager.put("TabbedPane.lightHighlight", ABU_MUDA);

        JTabbedPane tabs = new JTabbedPane(JTabbedPane.LEFT) {
            @Override
            public void updateUI() {
                super.updateUI();
                setBorder(BorderFactory.createMatteBorder(0, 0, 0, 2, ABU_GARIS));
            }
        };
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabs.setBackground(LATAR);
        tabs.setForeground(ABU_TEKS);

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

    private JPanel buildDashboard() {
        JPanel p = new JPanel(new BorderLayout(24, 24));
        p.setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));
        p.setBackground(Color.WHITE);

        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setOpaque(false);
        hdr.add(lbl("Dashboard", Font.BOLD, 28), BorderLayout.WEST);
        dbDate = lbl("", Font.PLAIN, 15);
        dbDate.setForeground(ABU_TEKS);
        hdr.add(dbDate, BorderLayout.EAST);
        p.add(hdr, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;

        JPanel stats = new JPanel(new GridLayout(2, 2, 20, 20));
        stats.setOpaque(false);
        dbMenu  = addStat(stats, "Total Menu",         BIRU);
        dbTrans = addStat(stats, "Transaksi Hari Ini", HIJAU);
        dbRev   = addStat(stats, "Pendapatan Hari Ini",ORANYE_TUA);
        dbStock = addStat(stats, "Nilai Stok",         UNGU);

        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weighty = 0.40;
        gbc.insets = new Insets(0, 0, 24, 0);
        contentPanel.add(stats, gbc);

        JPanel recent = new JPanel(new BorderLayout(0, 12));
        recent.setBackground(Color.WHITE);
        recent.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ABU_GARIS, 1),
            BorderFactory.createEmptyBorder(20, 22, 20, 22)
        ));
        
        JLabel titleLabel = lbl("Transaksi Terbaru", Font.BOLD, 18);
        titleLabel.setForeground(ABU_TEKS);
        recent.add(titleLabel, BorderLayout.NORTH);
        
        dbRecent = new JTextArea();
        dbRecent.setEditable(false);
        dbRecent.setFont(new Font("Consolas", Font.PLAIN, 14));
        dbRecent.setBackground(LATAR);
        dbRecent.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        
        JScrollPane scrollRecent = new JScrollPane(dbRecent);
        scrollRecent.setBorder(BorderFactory.createLineBorder(ABU_MUDA));
        recent.add(scrollRecent, BorderLayout.CENTER);

        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weighty = 0.60;
        gbc.insets = new Insets(0, 0, 0, 0);
        contentPanel.add(recent, gbc);

        p.add(contentPanel, BorderLayout.CENTER);
        return p;
    }

    private JLabel addStat(JPanel parent, String title, Color borderFg) {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 6, 0, 0, borderFg),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ABU_MUDA, 1),
                BorderFactory.createEmptyBorder(20, 22, 20, 22)
            )
        ));
        
        JLabel val = lbl("...", Font.BOLD, 24);
        val.setForeground(borderFg);
        card.add(val, BorderLayout.CENTER);
        
        JLabel lab = lbl(title, Font.PLAIN, 14);
        lab.setForeground(ABU_TEKS);
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
        dbStock.setText("Rp " + Controller.formatRupiah(sv) + " (" + ts + " item)");

        List<Order> orders = ctrl.getOrdersByDate(today);
        StringBuilder sb = new StringBuilder();
        int max = Math.min(orders.size(), 10);
        for (int i = 0; i < max; i++) {
            Order o = orders.get(i);
            sb.append(String.format("%-45s Rp %14s   [%s]\n",
                o.getId(), Controller.formatRupiah(o.getTotal()),
                o.getPaymentMethod().toUpperCase()));
        }
        if (orders.isEmpty()) sb.append("(belum ada transaksi hari ini)");
        dbRecent.setText(sb.toString());
    }

    private JPanel buildMenuPanel() {
        JPanel p = new JPanel(new BorderLayout(20, 20));
        p.setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));
        p.setBackground(Color.WHITE);

        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setOpaque(false);
        hdr.add(lbl("Kelola Menu", Font.BOLD, 28), BorderLayout.WEST);
        JButton addBtn = btnPrimary("+ Tambah Menu");
        addBtn.addActionListener(e -> { clearMenuForm(); menuName.requestFocus(); });
        hdr.add(addBtn, BorderLayout.EAST);
        p.add(hdr, BorderLayout.NORTH);

        JPanel mainContent = new JPanel(new BorderLayout(0, 16));
        mainContent.setOpaque(false);

        JPanel searchRow = new JPanel(new BorderLayout(12, 0));
        searchRow.setOpaque(false);
        searchRow.add(lbl("Cari:", Font.BOLD, 14), BorderLayout.WEST);
        menuSearch = new JTextField();
        menuSearch.setFont(font(14));
        menuSearch.setBorder(insetBorder(10, 14));
        menuSearch.addActionListener(e -> refreshMenuTable());
        searchRow.add(menuSearch, BorderLayout.CENTER);
        JButton searchBtn = btnDefault("Cari");
        searchBtn.addActionListener(e -> refreshMenuTable());
        searchRow.add(searchBtn, BorderLayout.EAST);
        mainContent.add(searchRow, BorderLayout.NORTH);

        menuModel = new DefaultTableModel(
            new String[]{"ID", "Nama Menu", "Kategori", "Harga", "Stok"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        menuTable = new JTable(menuModel);
        menuTable.setRowHeight(42);
        menuTable.setFont(font(14));
        menuTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        menuTable.getTableHeader().setBackground(LATAR);
        menuTable.getTableHeader().setForeground(ABU_TEKS);
        menuTable.getTableHeader().setPreferredSize(new Dimension(0, 44));
        menuTable.setSelectionBackground(KUNING);
        menuTable.setSelectionForeground(Color.BLACK);
        menuTable.setGridColor(ABU_MUDA);
        
        JScrollPane sp = new JScrollPane(menuTable);
        sp.setBorder(BorderFactory.createLineBorder(ABU_GARIS));
        mainContent.add(sp, BorderLayout.CENTER);
        p.add(mainContent, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(16, 16));
        bottom.setOpaque(false);

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 12));
        form.setBackground(LATAR);
        form.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ABU_GARIS),
            BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        
        form.add(lbl("Nama:", Font.PLAIN, 14));
        menuName = new JTextField(16); menuName.setFont(font(14)); menuName.setBorder(insetBorder(8, 12)); form.add(menuName);
        
        form.add(lbl("Kategori:", Font.PLAIN, 14));
        menuCat = new JComboBox<>(new String[]{"Makanan", "Minuman"});
        menuCat.setFont(font(14)); form.add(menuCat);
        
        form.add(lbl("Harga:", Font.PLAIN, 14));
        menuPrice = new JTextField(9); menuPrice.setFont(font(14)); menuPrice.setBorder(insetBorder(8, 12)); form.add(menuPrice);
        
        form.add(lbl("Stok:", Font.PLAIN, 14));
        menuStock = new JTextField(6); menuStock.setFont(font(14)); menuStock.setBorder(insetBorder(8, 12)); form.add(menuStock);
        
        JButton saveBtn = btnPrimary("Simpan");
        saveBtn.addActionListener(e -> saveMenu());
        form.add(saveBtn);
        
        JButton cancelBtn = btnDefault("Batal");
        cancelBtn.addActionListener(e -> clearMenuForm());
        form.add(cancelBtn);
        bottom.add(form, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
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
        List<MenuItem> items = new ArrayList<>(ctrl.searchMenu(menuSearch.getText().trim()));
        items.sort(Comparator.comparingInt(MenuItem::getId));
        for (MenuItem m : items) {
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

    private JPanel buildCashierPanel() {
        JPanel p = new JPanel(new BorderLayout(20, 20));
        p.setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));
        p.setBackground(Color.WHITE);
        p.add(lbl("Kasir", Font.BOLD, 28), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setResizeWeight(0.55);
        split.setDividerLocation(640);
        split.setDividerSize(8);
        split.setBorder(null);
        split.setLeftComponent(buildCashierLeft());
        split.setRightComponent(buildCashierRight());
        p.add(split, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildCashierLeft() {
        JPanel wrap = new JPanel(new BorderLayout(16, 16));
        wrap.setBackground(Color.WHITE);
        wrap.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ABU_GARIS),
            BorderFactory.createEmptyBorder(16, 18, 16, 18)
        ));

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        row.setOpaque(false);
        row.add(lbl("Cari:", Font.BOLD, 14));
        cashSearch = new JTextField(16); cashSearch.setFont(font(14));
        cashSearch.setBorder(insetBorder(8, 12));
        cashSearch.addActionListener(e -> refreshMenuGrid()); row.add(cashSearch);
        
        cashFilter = new JComboBox<>(new String[]{"Semua", "Makanan", "Minuman"});
        cashFilter.setFont(font(14));
        cashFilter.addActionListener(e -> refreshMenuGrid()); row.add(cashFilter);
        
        JButton srchBtn = btnDefault("Cari");
        srchBtn.addActionListener(e -> refreshMenuGrid()); row.add(srchBtn);
        
        JButton refBtn = btnDefault("Refresh");
        refBtn.addActionListener(e -> refreshMenuGrid()); row.add(refBtn);
        wrap.add(row, BorderLayout.NORTH);

        menuGridPanel = new JPanel();
        menuGridPanel.setLayout(new GridLayout(0, 2, 16, 16));
        menuGridPanel.setOpaque(false);

        JPanel gridWrapper = new JPanel(new BorderLayout());
        gridWrapper.setOpaque(false);
        gridWrapper.add(menuGridPanel, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(gridWrapper);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(26);
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
            JPanel card = new JPanel(new BorderLayout(10, 8));
            card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ABU_MUDA, 1),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));
            card.setBackground(Color.WHITE);
            card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            card.setPreferredSize(new Dimension(220, 110));
            card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

            boolean isFood = item.getCategory().equals("makanan");
            JLabel badge = lbl(isFood ? "Makanan" : "Minuman", Font.BOLD, 12);
            badge.setForeground(isFood ? ORANYE : BIRU);
            card.add(badge, BorderLayout.NORTH);

            JPanel info = new JPanel(new GridLayout(2, 1, 0, 6));
            info.setOpaque(false);
            info.add(lbl(item.getName(), Font.BOLD, 15));
            JLabel priceLbl = lbl("Rp " + Controller.formatRupiah(item.getPrice()), Font.BOLD, 16);
            priceLbl.setForeground(ORANYE_TUA);
            info.add(priceLbl);
            card.add(info, BorderLayout.CENTER);

            JPanel right = new JPanel(new BorderLayout(0, 8));
            right.setOpaque(false);
            JLabel stLbl = lbl("Stok: " + item.getStock(), Font.PLAIN, 13);
            stLbl.setForeground(item.getStock() <= 5 ? MERAH : ABU_TEKS);
            stLbl.setHorizontalAlignment(SwingConstants.RIGHT);
            right.add(stLbl, BorderLayout.NORTH);
            JLabel addLbl = lbl("[ + ]", Font.BOLD, 20);
            addLbl.setForeground(ORANYE);
            addLbl.setHorizontalAlignment(SwingConstants.RIGHT);
            right.add(addLbl, BorderLayout.SOUTH);
            card.add(right, BorderLayout.EAST);

            if (item.getStock() > 0) {
                card.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e)  { addToCart(item); }
                    @Override
                    public void mouseEntered(MouseEvent e)  { card.setBackground(LATAR); }
                    @Override
                    public void mouseExited(MouseEvent e)   { card.setBackground(Color.WHITE); }
                });
            } else {
                card.setBackground(LATAR);
            }
            menuGridPanel.add(card);
        }
        menuGridPanel.revalidate();
        menuGridPanel.repaint();
    }

    private JPanel buildCashierRight() {
        JPanel outer = new JPanel(new BorderLayout(12, 12));
        outer.setBackground(Color.WHITE);
        outer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ABU_GARIS),
            BorderFactory.createEmptyBorder(16, 18, 12, 18)
        ));

        JPanel outerHdr = new JPanel(new BorderLayout());
        outerHdr.setOpaque(false);
        outerHdr.add(lbl("Keranjang Belanja", Font.BOLD, 18), BorderLayout.WEST);
        cartItems = lbl("0 Item", Font.PLAIN, 13);
        cartItems.setForeground(ABU_TEKS);
        outerHdr.add(cartItems, BorderLayout.EAST);
        outer.add(outerHdr, BorderLayout.NORTH);

        cartListPanel = new JPanel();
        cartListPanel.setLayout(new BoxLayout(cartListPanel, BoxLayout.Y_AXIS));
        cartListPanel.setOpaque(false);

        JPanel scrollContentWrapper = new JPanel(new BorderLayout());
        scrollContentWrapper.setOpaque(false);
        scrollContentWrapper.add(cartListPanel, BorderLayout.NORTH);
        
        cartScroll = new JScrollPane(scrollContentWrapper);
        cartScroll.setBorder(BorderFactory.createLineBorder(ABU_MUDA));
        cartScroll.getVerticalScrollBar().setUnitIncrement(14);
        outer.add(cartScroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new GridBagLayout());
        bottom.setOpaque(false);
        bottom.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, ABU_GARIS),
            BorderFactory.createEmptyBorder(12, 0, 0, 0)
        ));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(4, 0, 4, 0);

        g.gridx = 0; g.gridy = 0; g.weightx = 0.0;
        bottom.add(lbl("Total Nominal:", Font.PLAIN, 13), g);
        g.gridx = 1; g.weightx = 1.0;
        cartTotal = lbl("Rp 0", Font.BOLD, 22);
        cartTotal.setForeground(ORANYE_TUA);
        cartTotal.setHorizontalAlignment(SwingConstants.RIGHT);
        bottom.add(cartTotal, g);

        g.gridx = 0; g.gridy = 1; g.weightx = 0.0;
        bottom.add(lbl("Metode Bayar:", Font.PLAIN, 13), g);
        g.gridx = 1; g.weightx = 1.0;
        cashMethod = new JComboBox<>(new String[]{"Cash", "QRIS", "Debit"});
        cashMethod.setFont(font(13));
        cashMethod.addActionListener(e -> {
            boolean isCash = "Cash".equals(cashMethod.getSelectedItem());
            cashPay.setEnabled(isCash);
            if (!isCash) { cashPay.setText(""); cartChange.setText(""); }
        });
        bottom.add(cashMethod, g);

        g.gridx = 0; g.gridy = 2; g.weightx = 0.0;
        bottom.add(lbl("Bayar (Cash):", Font.PLAIN, 13), g);
        g.gridx = 1; g.weightx = 1.0;
        JPanel payRow = new JPanel(new BorderLayout(8, 0));
        payRow.setOpaque(false);
        cashPay = new JTextField(10);
        cashPay.setFont(font(13));
        cashPay.setBorder(insetBorder(5, 10));
        payRow.add(cashPay, BorderLayout.CENTER);
        cartChange = lbl("", Font.BOLD, 13);
        cartChange.setForeground(HIJAU);
        payRow.add(cartChange, BorderLayout.EAST);
        bottom.add(payRow, g);

        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 8, 0));
        btnPanel.setOpaque(false);
        
        JButton clrBtn = btnDanger("Kosongkan");
        clrBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        clrBtn.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        clrBtn.addActionListener(e -> { cart.clear(); refreshCart(); });
        btnPanel.add(clrBtn);

        JButton chkBtn = btnPrimary("Proses Bayar");
        chkBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        chkBtn.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        chkBtn.addActionListener(e -> checkout());
        btnPanel.add(chkBtn);

        g.gridx = 0; g.gridy = 3; g.gridwidth = 2; g.weightx = 1.0;
        g.insets = new Insets(10, 0, 2, 0);
        bottom.add(btnPanel, g);

        cashPay.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateChange(); }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { updateChange(); }
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { updateChange(); }
        });

        outer.add(bottom, BorderLayout.SOUTH);
        return outer;
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

            JPanel row = new JPanel(new GridBagLayout());
            row.setOpaque(true);
            row.setBackground(Color.WHITE);
            row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, ABU_MUDA),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));

            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;

            c.gridx = 0; c.gridy = 0; c.weightx = 1.0; c.gridwidth = 1;
            JLabel nameLbl = lbl(ce.item.getName(), Font.BOLD, 14);
            nameLbl.setForeground(ABU_TEKS);
            row.add(nameLbl, c);

            c.gridx = 1; c.gridy = 0; c.weightx = 0.0;
            JPanel ctrlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
            ctrlPanel.setOpaque(false);

            JButton minus = new JButton("-");
            minus.setFont(new Font("Segoe UI", Font.BOLD, 12));
            minus.setPreferredSize(new Dimension(28, 24));
            minus.setMargin(new Insets(0, 0, 0, 0));
            minus.setFocusPainted(false);
            minus.addActionListener(e -> cartMinus(ce));
            ctrlPanel.add(minus);

            JTextField qtyField = new JTextField(String.valueOf(ce.qty), 3);
            qtyField.setFont(new Font("Segoe UI", Font.BOLD, 13));
            qtyField.setHorizontalAlignment(JTextField.CENTER);
            qtyField.setPreferredSize(new Dimension(34, 24));
            qtyField.addActionListener(e -> {
                try {
                    int val = Integer.parseInt(qtyField.getText().trim());
                    if (val <= 0) {
                        cart.remove(ce);
                    } else if (val > ce.item.getStock()) {
                        JOptionPane.showMessageDialog(this, "Stok tidak cukup! Maksimal: " + ce.item.getStock());
                        qtyField.setText(String.valueOf(ce.qty));
                    } else {
                        ce.qty = val;
                    }
                    refreshCart();
                } catch (NumberFormatException ex) {
                    qtyField.setText(String.valueOf(ce.qty));
                }
            });
            ctrlPanel.add(qtyField);

            JButton plus = new JButton("+");
            plus.setFont(new Font("Segoe UI", Font.BOLD, 12));
            plus.setPreferredSize(new Dimension(28, 24));
            plus.setMargin(new Insets(0, 0, 0, 0));
            plus.setFocusPainted(false);
            plus.addActionListener(e -> cartPlus(ce));
            ctrlPanel.add(plus);
            row.add(ctrlPanel, c);

            c.gridx = 0; c.gridy = 1; c.weightx = 1.0; c.gridwidth = 2;
            c.insets = new Insets(6, 0, 0, 0);
            JLabel subLbl = lbl("Rp " + Controller.formatRupiah(sub), Font.BOLD, 13);
            subLbl.setForeground(ORANYE);
            row.add(subLbl, c);

            cartListPanel.add(row);
        }

        if (cart.isEmpty()) {
            JLabel emptyLbl = lbl("(keranjang kosong)", Font.ITALIC, 13);
            emptyLbl.setForeground(ABU_TEKS);
            emptyLbl.setHorizontalAlignment(SwingConstants.CENTER);
            emptyLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
            cartListPanel.add(Box.createVerticalStrut(32));
            cartListPanel.add(emptyLbl);
        }

        cartTotal.setText("Rp " + Controller.formatRupiah(total));
        cartItems.setText(tq + " Item");
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
        String method = methodName.toLowerCase();
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
        sc.setPreferredSize(new Dimension(390, 420));
        JOptionPane.showMessageDialog(this, sc, "Struk Pembayaran", JOptionPane.INFORMATION_MESSAGE);

        cart.clear(); refreshCart();
        cashPay.setText(""); cartChange.setText("");
        refreshMenuGrid();
    }

    private JPanel buildReportPanel() {
        JPanel p = new JPanel(new BorderLayout(16, 16));
        p.setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));
        p.setBackground(Color.WHITE);

        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setOpaque(false);
        hdr.add(lbl("Laporan Harian", Font.BOLD, 28), BorderLayout.WEST);

        JPanel dp = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        dp.setOpaque(false);
        dp.add(lbl("Tanggal:", Font.BOLD, 14));
        rptDate = new JComboBox<>();
        rptDate.setFont(font(14));
        rptDate.addActionListener(e -> refreshReport());
        dp.add(rptDate);
        JButton refBtn = btnDefault("Refresh");
        refBtn.addActionListener(e -> refreshReport());
        dp.add(refBtn);
        hdr.add(dp, BorderLayout.EAST);
        p.add(hdr, BorderLayout.NORTH);

        JPanel mainContent = new JPanel(new BorderLayout(0, 20));
        mainContent.setOpaque(false);

        JPanel stats = new JPanel(new GridLayout(1, 3, 20, 0));
        stats.setOpaque(false);
        rptTrans = addStat(stats, "Total Transaksi",       BIRU);
        rptRev   = addStat(stats, "Total Pendapatan",      ORANYE_TUA);
        rptAvg   = addStat(stats, "Rata-rata / Transaksi", HIJAU);
        mainContent.add(stats, BorderLayout.NORTH);

        rptModel = new DefaultTableModel(
            new String[]{"Order ID", "Waktu", "Item (Qty)", "Total", "Metode"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        rptTable = new JTable(rptModel);
        rptTable.setRowHeight(42);
        rptTable.setFont(font(14));
        rptTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        rptTable.getTableHeader().setBackground(LATAR);
        rptTable.getTableHeader().setForeground(ABU_TEKS);
        rptTable.getTableHeader().setPreferredSize(new Dimension(0, 44));
        rptTable.setSelectionBackground(KUNING);
        rptTable.setSelectionForeground(Color.BLACK);
        rptTable.setGridColor(ABU_MUDA);
        
        JScrollPane sp = new JScrollPane(rptTable);
        sp.setBorder(BorderFactory.createLineBorder(ABU_GARIS));
        mainContent.add(sp, BorderLayout.CENTER);
        
        p.add(mainContent, BorderLayout.CENTER);

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
            BorderFactory.createLineBorder(ABU_GARIS),
            BorderFactory.createEmptyBorder(top, horiz, top, horiz));
    }

    private JButton btnPrimary(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setBackground(ORANYE);
        b.setForeground(Color.WHITE);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));
        return b;
    }

    private JButton btnDefault(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        b.setBackground(LATAR);
        b.setForeground(ABU_TEKS);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));
        return b;
    }

    private JButton btnDanger(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        b.setBackground(LATAR);
        b.setForeground(MERAH);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));
        return b;
    }
}