/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package Hotel_Reservation_System;

/**
 *
 * @author imash
 */
public class PanelRoomSelection extends javax.swing.JPanel {
    private int currentGuestId; 
    private String currentGuestName;
   private String selectedRoomNumber = "";
   private javax.swing.JTable tblRooms = new javax.swing.JTable();
    private javax.swing.JScrollPane tableScrollPane;
    
    public PanelRoomSelection() {
        initComponents();
        setupTableStructure();
         initCustomListeners();
        loadRoomsGrid();
    }
    
    public PanelRoomSelection(int guestId, String guestName) {
        initComponents();
        
        this.currentGuestId = guestId;
        this.currentGuestName = guestName;
        setupTableStructure();
        initCustomListeners();
        loadRoomsGrid();
       
    }
    private void setupTableStructure() {
        tableScrollPane = new javax.swing.JScrollPane();
        tblRooms.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
        tblRooms.setRowHeight(28); 
        
        
        tblRooms.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = tblRooms.getSelectedRow();
                if (selectedRow != -1) { 
                    String roomNum = tblRooms.getValueAt(selectedRow, 0).toString();
                    String status = tblRooms.getValueAt(selectedRow, 2).toString(); 
                    
                    if (status.equalsIgnoreCase("Available")) {
                        selectedRoomNumber = roomNum;
                    } else {
                        javax.swing.JOptionPane.showMessageDialog(this, 
                                "Room " + roomNum + " is already Booked! Please select an available room.", 
                                "Room Unavailable", javax.swing.JOptionPane.WARNING_MESSAGE);
                        tblRooms.clearSelection();
                        selectedRoomNumber = "";
                    }
                }
            }
        });
        
        tableScrollPane.setViewportView(tblRooms);
        tableScrollPane.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(200, 200, 200))); 
        
       
        add(tableScrollPane, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 80, 540, 210));
    }
    private void initCustomListeners() {
        cmbRoomType.addActionListener(e -> {
            loadRoomsGrid();
        });

        
       javax.swing.event.DocumentListener dateListener = new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { calculateTotalDays(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { calculateTotalDays(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { calculateTotalDays(); }
        };
        txtCheckIn.getDocument().addDocumentListener(dateListener);
        txtCheckOut.getDocument().addDocumentListener(dateListener);
    }

    
    

    private void loadRoomsGrid() {
        if (cmbRoomType.getSelectedItem() == null) return;
        String selectedType = cmbRoomType.getSelectedItem().toString();
        
        
        if (txtAmenities != null) txtAmenities.setText(getAmenitiesText(selectedType));
        if (txtPrice != null) txtPrice.setText(getRoomPrice(selectedType));
        
        
        String[] columnNames = {"Room No", "Room Type", "Availability"};
        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        String sql = "SELECT room_no, room_type, status FROM rooms WHERE LOWER(room_type) = LOWER(?)";

        try (java.sql.Connection conn = DB_Connection.connect();
             java.sql.PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setString(1, selectedType);
            java.sql.ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                String roomNum = rs.getString("room_no");
                String rType = rs.getString("room_type");
                String status = rs.getString("status");
                
                model.addRow(new Object[]{roomNum, rType, status});
            }
            
            
            tblRooms.setModel(model);
            selectedRoomNumber = ""; 
            
        } catch (Exception e) {
            e.printStackTrace();
            javax.swing.JOptionPane.showMessageDialog(this, "Table Load Error: " + e.getMessage(), "Database Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }

        this.revalidate();
        this.repaint();
    }
   
   private void executeBooking() {
        String updateRoomSql = "UPDATE rooms SET status = 'Booked' WHERE room_no = ?";
        String insertBookingSql = "INSERT INTO bookings (guest_id, room_no, check_in_date, check_out_date, adults, children, total_days) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (java.sql.Connection conn = DB_Connection.connect()) {
            conn.setAutoCommit(false);

            try (java.sql.PreparedStatement pst1 = conn.prepareStatement(updateRoomSql);
                 java.sql.PreparedStatement pst2 = conn.prepareStatement(insertBookingSql)) {
                
                pst1.setString(1, selectedRoomNumber);
                pst1.executeUpdate();

                pst2.setInt(1, currentGuestId);
                pst2.setString(2, selectedRoomNumber);
                pst2.setString(3, txtCheckIn.getText().trim());  
                pst2.setString(4, txtCheckOut.getText().trim()); 
                
                int adults = spnAdults.getText().trim().isEmpty() ? 1 : Integer.parseInt(spnAdults.getText().trim());
                int children = spnChildren.getText().trim().isEmpty() ? 0 : Integer.parseInt(spnChildren.getText().trim());
                int totalDays = Integer.parseInt(txtTotalDays.getText().trim());
                
                pst2.setInt(5, adults);
                pst2.setInt(6, children);
                pst2.setInt(7, totalDays);
                
                pst2.executeUpdate();
                conn.commit();
                
               
                String formattedGuestId = String.format("G%03d", currentGuestId);
                javax.swing.JOptionPane.showMessageDialog(this, "Room " + selectedRoomNumber + " successfully booked for Guest " + formattedGuestId + "!", "Success", javax.swing.JOptionPane.INFORMATION_MESSAGE);
                
            } catch (Exception ex) {
                conn.rollback(); 
                throw ex;
            }
        } catch (Exception e) {
            e.printStackTrace();
            

String formattedGuestId = String.format("G%03d", currentGuestId); 

javax.swing.JOptionPane.showMessageDialog(this, 
    "Room " + selectedRoomNumber + " successfully booked for Guest " + formattedGuestId + "!", 
    "Success", 
    javax.swing.JOptionPane.INFORMATION_MESSAGE);
        }
        }
    private String getAmenitiesText(String roomType) {
        if (roomType.equalsIgnoreCase("Single")) {
            return "Single Bed, Fan, Attached Bathroom, Free Wi-Fi";
        } else if (roomType.equalsIgnoreCase("Double")) {
            return "Double Bed, AC, Attached Bathroom, Smart TV, Free Wi-Fi";
        } else if (roomType.equalsIgnoreCase("Luxury")) {
            return "King Size Bed, AC, Mini Fridge, Bathtub, Smart TV, Pool Access";
        }
        return "None";
    }
    
    private String getRoomPrice(String roomType) {
        if (roomType.equalsIgnoreCase("Single")) {
            return "Rs. 5000.00";
        } else if (roomType.equalsIgnoreCase("Double")) {
            return "Rs. 8500.00";
        } else if (roomType.equalsIgnoreCase("Luxury")) {
            return "Rs. 15000.00";
        }
        return "Rs. 0.00";
    }
    private void calculateTotalDays() {
        String checkInStr = txtCheckIn.getText().trim();
        String checkOutStr = txtCheckOut.getText().trim();

        
        if (checkInStr.isEmpty() || checkOutStr.isEmpty()) {
            txtTotalDays.setText("");
            return;
        }

        
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd");

        try {
            java.time.LocalDate checkInDate = java.time.LocalDate.parse(checkInStr, formatter);
            java.time.LocalDate checkOutDate = java.time.LocalDate.parse(checkOutStr, formatter);

            
            long days = java.time.temporal.ChronoUnit.DAYS.between(checkInDate, checkOutDate);

            if (days > 0) {
                txtTotalDays.setText(String.valueOf(days));
            } else if (days == 0) {
                txtTotalDays.setText("1"); 
            } else {
                txtTotalDays.setText("Invalid Dates"); 
            }

        } catch (java.time.format.DateTimeParseException e) {
            
            txtTotalDays.setText("");
        }
    }
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        cmbRoomType = new javax.swing.JComboBox<>();
        lblAmenities = new javax.swing.JLabel();
        txtAmenities = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        txtPrice = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        txtCheckIn = new javax.swing.JTextField();
        txtCheckOut = new javax.swing.JTextField();
        spnAdults = new javax.swing.JTextField();
        spnChildren = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        txtTotalDays = new javax.swing.JTextField();
        btnBook = new javax.swing.JButton();
        btnBack2 = new javax.swing.JButton();

        setBackground(new java.awt.Color(204, 204, 255));
        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel1.setText("Room Selection");
        add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 20, 250, -1));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel2.setText("Room Type:");
        add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(44, 53, -1, -1));

        cmbRoomType.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Single", "Double", "Luxury" }));
        cmbRoomType.addActionListener(this::cmbRoomTypeActionPerformed);
        add(cmbRoomType, new org.netbeans.lib.awtextra.AbsoluteConstraints(143, 50, -1, -1));

        lblAmenities.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblAmenities.setText("Amenities:");
        add(lblAmenities, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 310, 70, 20));

        txtAmenities.setEditable(false);
        add(txtAmenities, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 310, 290, -1));

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel3.setText("Price Per Night:");
        add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 360, -1, -1));

        txtPrice.setEditable(false);
        txtPrice.addActionListener(this::txtPriceActionPerformed);
        add(txtPrice, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 360, 180, -1));

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel4.setText("Check-in Date:");
        add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 400, 90, -1));

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel5.setText("Check-out Date:");
        add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 440, -1, -1));

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel6.setText("Adults:");
        add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 470, -1, -1));

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel7.setText("Children:");
        add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 500, -1, -1));
        add(txtCheckIn, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 400, 180, -1));
        add(txtCheckOut, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 440, 180, -1));

        spnAdults.addActionListener(this::spnAdultsActionPerformed);
        add(spnAdults, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 470, 180, -1));
        add(spnChildren, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 500, 180, -1));

        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel8.setText("Total Days:");
        add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 540, 80, -1));

        txtTotalDays.setEditable(false);
        add(txtTotalDays, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 540, 180, -1));

        btnBook.setBackground(new java.awt.Color(102, 153, 255));
        btnBook.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnBook.setText("Book");
        btnBook.addActionListener(this::btnBookActionPerformed);
        add(btnBook, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 580, 100, -1));

        btnBack2.setBackground(new java.awt.Color(153, 153, 153));
        btnBack2.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnBack2.setText("← Back");
        btnBack2.addActionListener(this::btnBack2ActionPerformed);
        add(btnBack2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 10, -1, -1));
    }// </editor-fold>//GEN-END:initComponents

    private void cmbRoomTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbRoomTypeActionPerformed
       loadRoomsGrid();
    }//GEN-LAST:event_cmbRoomTypeActionPerformed

    private void txtPriceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtPriceActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtPriceActionPerformed

    private void spnAdultsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_spnAdultsActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_spnAdultsActionPerformed

    private void btnBookActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBookActionPerformed
 
    if (selectedRoomNumber == null || selectedRoomNumber.isEmpty()) {
        javax.swing.JOptionPane.showMessageDialog(this, "Please select a room from the table first!", "Warning", javax.swing.JOptionPane.WARNING_MESSAGE);
        return;
    }
    if (txtTotalDays.getText().trim().isEmpty() || txtTotalDays.getText().equals("Invalid Dates")) {
        javax.swing.JOptionPane.showMessageDialog(this, "Please enter valid Check-in and Check-out dates!", "Warning", javax.swing.JOptionPane.WARNING_MESSAGE);
        return;
    }
  
    try {
        String priceText = txtPrice.getText().replace("Rs. ", "").trim();
        double pricePerNight = Double.parseDouble(priceText);
        int totalDays = Integer.parseInt(txtTotalDays.getText().trim());
        double totalAmount = pricePerNight * totalDays;

        javax.swing.JPanel parentPanel = (javax.swing.JPanel) this.getParent();

        if (parentPanel != null) {
            parentPanel.removeAll();
            
            int adults = spnAdults.getText().trim().isEmpty() ? 1 : Integer.parseInt(spnAdults.getText().trim());
            int children = spnChildren.getText().trim().isEmpty() ? 0 : Integer.parseInt(spnChildren.getText().trim());
            
           
            PanelPayment paymentPanel = new PanelPayment(
                currentGuestId, 
                selectedRoomNumber, 
                txtCheckIn.getText().trim(), 
                txtCheckOut.getText().trim(), 
                adults, 
                children, 
                totalDays, 
                totalAmount, 
                parentPanel
            );
            
            parentPanel.add(paymentPanel);
            parentPanel.revalidate();
            parentPanel.repaint();
        }

    } catch (Exception ex) {
        System.out.println("Payment Panel Load Error: " + ex.getMessage());
    }

    }//GEN-LAST:event_btnBookActionPerformed

    private void btnBack2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBack2ActionPerformed
        
        java.awt.Window ancestor = javax.swing.SwingUtilities.getWindowAncestor(this);
        
        if (ancestor instanceof MainDashboard) {
            MainDashboard dashboard = (MainDashboard) ancestor;
            
            dashboard.showPanel("cardBookings"); 
            
        } else {
           
            java.awt.Container parent = this.getParent();
            if (parent != null && parent.getLayout() instanceof java.awt.CardLayout) {
               
                ((java.awt.CardLayout) parent.getLayout()).show(parent, "cardBookings");
            }
        }
    
    }//GEN-LAST:event_btnBack2ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBack2;
    private javax.swing.JButton btnBook;
    private javax.swing.JComboBox<String> cmbRoomType;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel lblAmenities;
    private javax.swing.JTextField spnAdults;
    private javax.swing.JTextField spnChildren;
    private javax.swing.JTextField txtAmenities;
    private javax.swing.JTextField txtCheckIn;
    private javax.swing.JTextField txtCheckOut;
    private javax.swing.JTextField txtPrice;
    private javax.swing.JTextField txtTotalDays;
    // End of variables declaration//GEN-END:variables
}
